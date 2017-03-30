package com.sos.jitl.reporting.model.report;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.Criteria;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTask;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.DBItemReportVariable;
import com.sos.jitl.reporting.db.DBItemSchedulerHistory;
import com.sos.jitl.reporting.db.DBItemSchedulerHistoryOrderStepReporting;
import com.sos.jitl.reporting.helper.CounterSynchronize;
import com.sos.jitl.reporting.helper.EStartCauses;
import com.sos.jitl.reporting.helper.InventoryInfo;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.reporting.job.report.FactJobOptions;
import com.sos.jitl.reporting.model.IReportingModel;
import com.sos.jitl.reporting.model.ReportingModel;
import com.sos.jitl.reporting.plugin.FactNotificationPlugin;

import sos.util.SOSString;

public class FactModel extends ReportingModel implements IReportingModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactModel.class);
    private static final String TABLE_REPORTING_VARIABLES_VARIABLE_PREFIX = "reporting_";
    private FactJobOptions options;
    private SOSHibernateSession schedulerSession;
    private CounterSynchronize counterOrderSyncUncompleted;
    private CounterSynchronize counterOrderSync;
    private CounterSynchronize counterTaskSyncUncompleted;
    private CounterSynchronize counterTaskSync;
    private boolean isChanged = false;
    private boolean isOrdersChanged = false;
    private boolean isTasksChanged = false;
    private int maxHistoryAge;
    private int maxUncompletedAge;
    private Long maxHistoryTasks;
    private Optional<Integer> largeResultFetchSizeReporting = Optional.empty();
    private Optional<Integer> largeResultFetchSizeScheduler = Optional.empty();
    private FactNotificationPlugin notificationPlugin;

    public FactModel(SOSHibernateSession reportingSess, SOSHibernateSession schedulerSess, FactJobOptions opt) throws Exception {
        setReportingSession(reportingSess);
        schedulerSession = schedulerSess;
        options = opt;

        largeResultFetchSizeReporting = getFetchSize(options.large_result_fetch_size.value());
        largeResultFetchSizeScheduler = getFetchSize(options.large_result_fetch_size_scheduler.value());
        maxHistoryAge = ReportUtil.resolveAge2Minutes(options.max_history_age.getValue());
        maxHistoryTasks = new Long(options.max_history_tasks.value());
        maxUncompletedAge = ReportUtil.resolveAge2Minutes(options.max_uncompleted_age.getValue());
        registerPlugin();
    }

    public void init(PluginMailer mailer, Path configDirectory) {
        pluginOnInit(mailer, configDirectory);
    }

    public void exit() {
        pluginOnExit();
    }

    @Override
    public void process() throws Exception {
        String method = "process";
        Date dateFrom = null;
        Date dateTo = ReportUtil.getCurrentDateTime();
        Long dateToAsMinutes = dateTo.getTime() / 1000 / 60;
        DateTime start = new DateTime();
        try {
            LOGGER.debug(String.format("%s: execute_notification_plugin = %s", method, options.execute_notification_plugin.value()));
            initCounters();

            DBItemReportVariable reportingVariable = initSynchronizing();
            dateFrom = getDateFrom(reportingVariable, dateTo);

            synchronizeTaskUncompleted(options.current_scheduler_id.getValue(), dateToAsMinutes);
            synchronizeTask(options.current_scheduler_id.getValue(), dateFrom, dateTo, dateToAsMinutes);

            synchronizeOrderUncompleted(options.current_scheduler_id.getValue(), dateToAsMinutes);
            synchronizeOrder(options.current_scheduler_id.getValue(), dateFrom, dateTo, dateToAsMinutes);

            finishSynchronizing(reportingVariable, dateTo);
            setChangedSummary();
            logSummary(dateFrom, dateTo, start);
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    private void finishSynchronizing(DBItemReportVariable reportingVariable, Date dateTo) throws Exception {
        String method = "finishSynchronizing";
        try {
            LOGGER.debug(String.format("%s: dateTo = %s", method, ReportUtil.getDateAsString(dateTo)));

            getDbLayer().getSession().beginTransaction();
            reportingVariable.setNumericValue(new Long(maxHistoryAge));
            reportingVariable.setTextValue(ReportUtil.getDateAsString(dateTo));
            getDbLayer().getSession().update(reportingVariable);
            getDbLayer().getSession().commit();
        } catch (Exception e) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s: %s", method, ex.toString()), ex);
            }
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        }
    }

    private DBItemReportVariable initSynchronizing() throws Exception {
        String method = "initSynchronizing";
        DBItemReportVariable variable = null;
        try {
            String name = getSchedulerVariableName();
            LOGGER.debug(String.format("%s, name=%s", method, name));

            getDbLayer().getSession().beginTransaction();
            variable = getDbLayer().getReportVariabe(name);
            if (variable == null) {
                variable = getDbLayer().insertReportVariable(name, new Long(maxHistoryAge), null);
            }
            getDbLayer().getSession().commit();
        } catch (Exception e) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s: %s", method, ex.toString()), ex);
            }
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        }
        return variable;
    }

    private String getSchedulerVariableName() {
        String name = String.format("%s%s", TABLE_REPORTING_VARIABLES_VARIABLE_PREFIX, options.current_scheduler_id.getValue());
        if (name.length() > 255) {
            name = name.substring(0, 255);
        }
        return name.toLowerCase();
    }

    private void synchronizeOrderUncompleted(String schedulerId, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeOrderUncompleted";
        LOGGER.debug(String.format("%s", method));
        if (schedulerId != null && !schedulerId.isEmpty()) {
            List<Long> historyIds = new ArrayList<Long>();
            try {
                getDbLayer().getSession().beginTransaction();
                Criteria cr = getDbLayer().getOrderSyncUncomplitedHistoryIds(largeResultFetchSizeReporting, schedulerId);
                List<Long> result = cr.list();
                for (int i = 0; i < result.size(); i++) {
                    Long historyId = result.get(i);
                    if (!historyIds.contains(historyId)) {
                        historyIds.add(historyId);
                    }
                }
                getDbLayer().getSession().commit();
            } catch (Exception e) {
                try {
                    getDbLayer().getSession().rollback();
                } catch (Exception ex) {
                    LOGGER.warn(String.format("%s: %s", method, ex.toString()), ex);
                }
                throw new Exception(String.format("%s: error on getOrderSyncUncomplitedHistoryIds: %s", method, e.toString()), e);
            }

            if (!historyIds.isEmpty()) {
                try {
                    int size = historyIds.size();
                    LOGGER.debug(String.format("%s: historyIds.size = %s", method, size));
                    if (size > SOSHibernateSession.LIMIT_IN_CLAUSE) {
                        int counterTotal = 0;
                        int counterSkip = 0;
                        int counterInsertedTriggers = 0;
                        int counterUpdatedTriggers = 0;
                        int counterInsertedExecutions = 0;
                        int counterUpdatedExecutions = 0;
                        int counterInsertedTasks = 0;
                        int counterUpdatedTasks = 0;

                        for (int i = 0; i < size; i += SOSHibernateSession.LIMIT_IN_CLAUSE) {
                            List<Long> subList;
                            if (size > i + SOSHibernateSession.LIMIT_IN_CLAUSE) {
                                subList = historyIds.subList(i, (i + SOSHibernateSession.LIMIT_IN_CLAUSE));
                            } else {
                                subList = historyIds.subList(i, size);
                            }
                            Criteria cr = getDbLayer().getSchedulerHistoryOrderSteps(schedulerSession, largeResultFetchSizeScheduler, schedulerId,
                                    subList);
                            CounterSynchronize counter = synchronizeOrderHistory(cr, dateToAsMinutes);
                            counterTotal += counter.getTotal();
                            counterSkip += counter.getSkip();
                            counterInsertedTriggers += counter.getInsertedTriggers();
                            counterUpdatedTriggers += counter.getUpdatedTriggers();
                            counterInsertedExecutions += counter.getInsertedExecutions();
                            counterUpdatedExecutions += counter.getUpdatedExecutions();
                            counterInsertedTasks += counter.getInsertedTasks();
                            counterUpdatedTasks += counter.getUpdatedTasks();

                        }
                        counterOrderSyncUncompleted.setTotal(counterTotal);
                        counterOrderSyncUncompleted.setSkip(counterSkip);
                        counterOrderSyncUncompleted.setInsertedTriggers(counterInsertedTriggers);
                        counterOrderSyncUncompleted.setUpdatedTriggers(counterUpdatedTriggers);
                        counterOrderSyncUncompleted.setInsertedExecutions(counterInsertedExecutions);
                        counterOrderSyncUncompleted.setUpdatedExecutions(counterUpdatedExecutions);
                        counterOrderSyncUncompleted.setInsertedTasks(counterInsertedTasks);
                        counterOrderSyncUncompleted.setUpdatedTasks(counterUpdatedTasks);
                    } else {
                        Criteria cr = getDbLayer().getSchedulerHistoryOrderSteps(schedulerSession, largeResultFetchSizeScheduler, schedulerId,
                                historyIds);
                        counterOrderSyncUncompleted = synchronizeOrderHistory(cr, dateToAsMinutes);
                    }
                } catch (Exception e) {
                    throw new Exception(String.format("%s: error on synchronizeOrderHistory: %s", method, e.toString()), e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void synchronizeTaskUncompleted(String schedulerId, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeTaskUncompleted";
        LOGGER.debug(String.format("%s", method));
        List<Long> historyIds = new ArrayList<Long>();
        try {
            Criteria cr = getDbLayer().getTaskSyncUncomplitedHistoryIds(largeResultFetchSizeReporting, schedulerId);
            List<Long> result = cr.list();
            for (int i = 0; i < result.size(); i++) {
                Long historyId = result.get(i);
                if (!historyIds.contains(historyId)) {
                    historyIds.add(historyId);
                }
            }
        } catch (Exception e) {
            throw new Exception(String.format("%s: error on getTaskSyncUncomplitedHistoryIds: %s", method, e.toString()), e);
        }

        if (!historyIds.isEmpty()) {
            try {
                int size = historyIds.size();
                LOGGER.debug(String.format("%s: historyIds.size = %s", method, size));
                if (size > SOSHibernateSession.LIMIT_IN_CLAUSE) {
                    int counterTotal = 0;
                    int counterSkip = 0;
                    int counterInsertedTriggers = 0;
                    int counterUpdatedTriggers = 0;
                    int counterInsertedExecutions = 0;
                    int counterUpdatedExecutions = 0;
                    int counterInsertedTasks = 0;
                    int counterUpdatedTasks = 0;

                    for (int i = 0; i < size; i += SOSHibernateSession.LIMIT_IN_CLAUSE) {
                        List<Long> subList;
                        if (size > i + SOSHibernateSession.LIMIT_IN_CLAUSE) {
                            subList = historyIds.subList(i, (i + SOSHibernateSession.LIMIT_IN_CLAUSE));
                        } else {
                            subList = historyIds.subList(i, size);
                        }
                        Criteria cr = getDbLayer().getSchedulerHistoryTasks(schedulerSession, largeResultFetchSizeScheduler, schedulerId, subList);
                        CounterSynchronize counter = synchronizeTaskHistory(cr, schedulerId, dateToAsMinutes);
                        counterTotal += counter.getTotal();
                        counterSkip += counter.getSkip();
                        counterInsertedTriggers += counter.getInsertedTriggers();
                        counterUpdatedTriggers += counter.getUpdatedTriggers();
                        counterInsertedExecutions += counter.getInsertedExecutions();
                        counterUpdatedExecutions += counter.getUpdatedExecutions();
                        counterInsertedTasks += counter.getInsertedTasks();
                        counterUpdatedTasks += counter.getUpdatedTasks();
                    }
                    counterTaskSyncUncompleted.setTotal(counterTotal);
                    counterTaskSyncUncompleted.setSkip(counterSkip);
                    counterTaskSyncUncompleted.setInsertedTriggers(counterInsertedTriggers);
                    counterTaskSyncUncompleted.setUpdatedTriggers(counterUpdatedTriggers);
                    counterTaskSyncUncompleted.setInsertedExecutions(counterInsertedExecutions);
                    counterTaskSyncUncompleted.setUpdatedExecutions(counterUpdatedExecutions);
                    counterTaskSyncUncompleted.setInsertedTasks(counterInsertedTasks);
                    counterTaskSyncUncompleted.setUpdatedTasks(counterUpdatedTasks);

                } else {
                    Criteria cr = getDbLayer().getSchedulerHistoryTasks(schedulerSession, largeResultFetchSizeScheduler, schedulerId, historyIds);
                    counterTaskSyncUncompleted = synchronizeTaskHistory(cr, schedulerId, dateToAsMinutes);
                }
            } catch (Exception e) {
                throw new Exception(String.format("%s: error on synchronizeTaskHistory: %s", method, e.toString()), e);
            }
        }

    }

    private void synchronizeOrder(String schedulerId, Date dateFrom, Date dateTo, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeOrder";
        try {
            LOGGER.debug(String.format("%s: schedulerId = %s, dateFrom = %s, dateTo = %s", method, schedulerId, ReportUtil.getDateAsString(dateFrom),
                    ReportUtil.getDateAsString(dateTo)));

            Criteria cr = getDbLayer().getSchedulerHistoryOrderSteps(schedulerSession, largeResultFetchSizeScheduler, schedulerId, dateFrom, dateTo);
            counterOrderSync = synchronizeOrderHistory(cr, dateToAsMinutes);
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    private void synchronizeTask(String schedulerId, Date dateFrom, Date dateTo, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeTask";
        try {
            LOGGER.debug(String.format("%s: schedulerId = %s, dateFrom = %s, dateTo = %s", method, schedulerId, ReportUtil.getDateAsString(dateFrom),
                    ReportUtil.getDateAsString(dateTo)));

            Criteria cr = getDbLayer().getSchedulerHistoryTasks(schedulerSession, largeResultFetchSizeScheduler, schedulerId, dateFrom, dateTo);
            counterTaskSync = synchronizeTaskHistory(cr, schedulerId, dateToAsMinutes);
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    private boolean calculateIsSyncCompleted(Date startTime, Date endTime, Long dateToAsMinutes) {
        boolean completed = false;
        if (endTime == null) {
            if (maxUncompletedAge > 0) {
                Long startTimeMinutes = startTime.getTime() / 1000 / 60;
                Long diffMinutes = dateToAsMinutes - startTimeMinutes;
                if (diffMinutes > maxUncompletedAge) {
                    completed = true;
                }
            }
        } else {
            completed = true;
        }
        return completed;
    }

    private synchronized CounterSynchronize synchronizeTaskHistory(Criteria criteria, String schedulerId, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeTaskHistory";
        CounterSynchronize counter = new CounterSynchronize();
        try {
            LOGGER.debug(String.format("%s", method));
            DateTime start = new DateTime();
            int counterTotal = 0;
            int counterSkip = 0;
            int counterInserted = 0;
            int counterUpdated = 0;
            List<DBItemSchedulerHistory> result = null;
            try {
                schedulerSession.beginTransaction();
                result = getDbLayer().executeCriteriaList(criteria);
                schedulerSession.commit();
            } catch (Exception e) {
                try {
                    schedulerSession.rollback();
                } catch (Exception ex) {
                    LOGGER.warn(String.format("%s: schedulerConnection %s", method, ex.toString()), ex);
                }
                throw new Exception(String.format("error on executeCriteriaList: %s", e.toString()), e);
            }

            getDbLayer().getSession().beginTransaction();
            int totalSize = result.size();
            for (int i = 0; i < totalSize; i++) {
                counterTotal++;
                DBItemSchedulerHistory task = result.get(i);
                if (task.getJobName().equals("(Spooler)")) {
                    LOGGER.debug(String.format("%s: %s) skip jobName = %s", method, counterTotal, task.getJobName()));
                    counterSkip++;
                    continue;
                }

                boolean syncCompleted = calculateIsSyncCompleted(task.getStartTime(), task.getEndTime(), dateToAsMinutes);
                DBItemReportTask reportTask = getDbLayer().getTask(schedulerId, task.getId());
                if (reportTask == null) {
                    LOGGER.debug(String.format("%s: %s) insert: schedulerId = %s, historyId = %s, jobName = %s, cause = %s, syncCompleted = %s",
                            method, counterTotal, task.getSpoolerId(), task.getId(), task.getJobName(), task.getCause(), syncCompleted));

                    boolean isOrder = false;
                    if (task.getCause() != null && task.getCause().equals(EStartCauses.ORDER.value())) {
                        isOrder = true;
                    }
                    List<Object[]> infos = null;
                    try {
                        infos = getDbLayer().getInventoryInfoByJobName(task.getSpoolerId(), options.current_scheduler_hostname.getValue(),
                                options.current_scheduler_http_port.value(), task.getJobName());
                    } catch (Exception e) {
                        throw new Exception(String.format("error on getInventoryInfoByJob: %s", e.toString()), e);
                    }
                    InventoryInfo inventoryInfo = getInventoryInfo(infos, false);
                    LOGGER.debug(String.format("%s: %s) getInventoryInfoByJob(jobName=%s): eii.getTitle=%s, eii.getIsRuntimeDefined=%s", method,
                            counterTotal, task.getJobName(), inventoryInfo.getTitle(), inventoryInfo.getIsRuntimeDefined()));

                    if (!isOrder && inventoryInfo.getIsOrderJob()) {
                        isOrder = true;
                    }

                    try {
                        reportTask = getDbLayer().insertTask(task, inventoryInfo, isOrder, syncCompleted);
                    } catch (Exception e) {
                        throw new Exception(String.format("error on insertTask: %s", e.toString()), e);
                    }
                    counterInserted++;
                } else {
                    LOGGER.debug(String.format(
                            "%s: %s) update: id = %s, schedulerId = %s, historyId = %s, jobName = %s, cause = %s, syncCompleted = %s", method,
                            counterTotal, reportTask.getId(), task.getSpoolerId(), task.getId(), task.getJobName(), task.getCause(), syncCompleted));

                    try {
                        getDbLayer().updateTask(reportTask, task, syncCompleted);
                    } catch (Exception e) {
                        throw new Exception(String.format("error on updateTask: %s", e.toString()), e);
                    }
                    counterUpdated++;
                }

                if (counterTotal % options.log_info_step.value() == 0) {
                    LOGGER.info(String.format("%s: %s of %s history steps processed ...", method, counterTotal, totalSize));
                }
            }
            getDbLayer().getSession().commit();
            LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));

            counter.setTotal(counterTotal);
            counter.setSkip(counterSkip);
            counter.setInsertedTriggers(0);
            counter.setUpdatedTriggers(0);
            counter.setInsertedExecutions(0);
            counter.setUpdatedExecutions(0);
            counter.setInsertedTasks(counterInserted);
            counter.setUpdatedTasks(counterUpdated);
            LOGGER.debug(String.format("%s: total history tasks = %s, inserted = %s, updated = %s, skip = %s ", method, counter.getTotal(), counter
                    .getInsertedTasks(), counter.getUpdatedTasks(), counter.getSkip()));

        } catch (Exception e) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s: %s", method, ex.toString()), ex);
            }
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        }
        return counter;
    }

    private synchronized CounterSynchronize synchronizeOrderHistory(Criteria criteria, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeOrderHistory";
        CounterSynchronize counter = new CounterSynchronize();
        try {
            LOGGER.debug(String.format("%s", method));
            DateTime start = new DateTime();
            Map<Long, DBItemReportTrigger> triggerObjects = new HashMap<Long, DBItemReportTrigger>();
            int counterTotal = 0;
            int counterSkip = 0;
            int counterInsertedTriggers = 0;
            int counterUpdatedTriggers = 0;
            int counterInsertedExecutions = 0;
            int counterUpdatedExecutions = 0;
            int counterInsertedTasks = 0;
            int counterUpdatedTasks = 0;

            List<DBItemSchedulerHistoryOrderStepReporting> result = null;
            try {
                schedulerSession.beginTransaction();
                result = getDbLayer().executeCriteriaList(criteria); // criteria.list();
                schedulerSession.commit();
            } catch (Exception e) {
                try {
                    schedulerSession.rollback();
                } catch (Exception ex) {
                    LOGGER.warn(String.format("%s: schedulerConnection %s", method, ex.toString()), ex);
                }
                throw new Exception(String.format("error on executeCriteriaList: %s", e.toString()), e);
            }

            getDbLayer().getSession().beginTransaction();
            int totalSize = result.size();
            for (int i = 0; i < totalSize; i++) {
                counterTotal++;
                DBItemSchedulerHistoryOrderStepReporting step = result.get(i);
                if (step.getOrderHistoryId() == null && step.getOrderId() == null && step.getOrderStartTime() == null) {
                    counterSkip++;
                    LOGGER.debug(String.format("%s: %s) order object is null. step = %s, historyId = %s ", method, counterTotal, step.getStepState(),
                            step.getStepHistoryId()));
                    continue;
                }
                DBItemReportTask reportTask = null;
                // e.g. waiting_for_agent
                if (step.getTaskId() == null && step.getTaskJobName() == null && step.getTaskCause() == null) {
                    LOGGER.debug(String.format("%s: %s) task object is null. jobChain = %s, order = %s, step = %s, taskId = %s ", method,
                            counterTotal, step.getOrderJobChain(), step.getOrderId(), step.getStepState(), step.getStepTaskId()));

                    reportTask = getDbLayer().getTask(step.getOrderSchedulerId(), step.getStepTaskId());
                    if (reportTask == null) {
                        List<Object[]> infos = null;
                        try {
                            infos = getDbLayer().getInventoryFullJobInfo(step.getOrderSchedulerId(), options.current_scheduler_hostname.getValue(),
                                    options.current_scheduler_http_port.value(), step.getOrderJobChain(), step.getStepState());
                        } catch (Exception e) {
                            throw new Exception(String.format("error on getInventoryJobInfo: %s", e.toString()), e);
                        }
                        InventoryInfo taskInventoryInfo = getInventoryInfo(infos, true);
                        reportTask = getDbLayer().insertTaskByOrderStep(step, taskInventoryInfo, false);
                        LOGGER.debug(String.format("%s: %s) task created reportTask.getId = %s", method, counterTotal, reportTask.getId()));
                        counterInsertedTasks++;
                    } else {
                        LOGGER.debug(String.format("%s: %s) task already exist. reportTask.getId = %s", method, counterTotal, reportTask.getId()));
                    }
                    step.setTaskId(reportTask.getHistoryId());
                    step.setTaskStartTime(reportTask.getStartTime());
                    step.setTaskJobName(reportTask.getName());
                    step.setTaskClusterMemberId(reportTask.getClusterMemberId());
                    step.setTaskAgentUrl(reportTask.getAgentUrl());
                    step.setTaskCause(reportTask.getCause());
                }

                LOGGER.debug(String.format("%s: %s) schedulerId = %s, orderHistoryId = %s, jobChain = %s, order id = %s, step = %s, step state = %s",
                        method, counterTotal, step.getOrderSchedulerId(), step.getOrderHistoryId(), step.getOrderJobChain(), step.getOrderId(), step
                                .getStepStep(), step.getStepState()));
                DBItemReportTrigger rt = null;
                if (triggerObjects.containsKey(step.getOrderHistoryId())) {
                    rt = triggerObjects.get(step.getOrderHistoryId());
                    LOGGER.debug(String.format("%s: %s) use rt.getId=%s", method, counterTotal, rt.getId()));
                } else {
                    boolean syncCompleted = calculateIsSyncCompleted(step.getOrderStartTime(), step.getOrderEndTime(), dateToAsMinutes);
                    rt = getDbLayer().getTrigger(step.getOrderSchedulerId(), step.getOrderHistoryId());
                    if (rt == null) {
                        List<Object[]> infos = null;
                        try {
                            infos = getDbLayer().getInventoryInfoByOrderIdAndJobChain(step.getOrderSchedulerId(), options.current_scheduler_hostname.getValue(),
                                    options.current_scheduler_http_port.value(), step.getOrderId(), step.getOrderJobChain());
                        } catch (Exception e) {
                            throw new Exception(String.format("error on getInventoryInfoForTrigger: %s", e.toString()), e);
                        }
                        InventoryInfo triggerInventoryInfo = getInventoryInfo(infos, false);
                        LOGGER.debug(String.format(
                                "%s: %s) getInventoryInfoForTrigger(orderId=%s, jobChain=%s): ii.getTitle=%s, ii.getIsRuntimeDefined=%s", method,
                                counterTotal, step.getOrderId(), step.getOrderJobChain(), triggerInventoryInfo.getTitle(), triggerInventoryInfo
                                        .getIsRuntimeDefined()));

                        String startCause = step.getTaskCause();
                        if (startCause.equals(EStartCauses.ORDER.value())) {
                            String inventoryStartCause = getDbLayer().getInventoryJobChainStartCause(step.getOrderSchedulerId(),
                                    this.options.current_scheduler_hostname.getValue(), this.options.current_scheduler_http_port.value(), ReportUtil
                                            .normalizeDbItemPath(step.getOrderJobChain()));
                            if (!SOSString.isEmpty(inventoryStartCause)) {
                                startCause = inventoryStartCause;
                            }
                        }

                        try {
                            rt = getDbLayer().insertTrigger(step, triggerInventoryInfo, startCause, syncCompleted);
                        } catch (Exception e) {
                            throw new Exception(String.format("error on insertTrigger: %s", e.toString()), e);
                        }
                        LOGGER.debug(String.format("%s: %s) trigger created rt.getId = %s", method, counterTotal, rt.getId()));

                        counterInsertedTriggers++;
                    } else {
                        try {
                            rt = getDbLayer().updateTrigger(rt, step, syncCompleted);
                        } catch (Exception e) {
                            throw new Exception(String.format("error on updateTrigger: %s", e.toString()), e);
                        }
                        LOGGER.debug(String.format("%s: %s) trigger updated rt.getId = %s", method, counterTotal, rt.getId()));
                        counterUpdatedTriggers++;
                    }
                    triggerObjects.put(step.getOrderHistoryId(), rt);
                }

                DBItemReportExecution re = getDbLayer().getExecution(step.getOrderSchedulerId(), step.getStepTaskId(), rt.getId(), step
                        .getStepStep());
                if (re == null) {
                    if (reportTask == null) {
                        reportTask = getDbLayer().getTask(step.getOrderSchedulerId(), step.getStepTaskId());
                        if (reportTask == null) {// e.g. task created by splitter
                            List<Object[]> infos = null;
                            try {
                                infos = getDbLayer().getInventoryInfoByJobName(step.getOrderSchedulerId(), options.current_scheduler_hostname.getValue(),
                                        options.current_scheduler_http_port.value(), ReportUtil.normalizeDbItemPath(step.getTaskJobName()));
                            } catch (Exception e) {
                                throw new Exception(String.format("error on getInventoryJobInfo: %s", e.toString()), e);
                            }
                            InventoryInfo inventoryInfo = getInventoryInfo(infos, false);

                            boolean syncCompleted = calculateIsSyncCompleted(step.getTaskStartTime(), step.getTaskEndTime(), dateToAsMinutes);
                            reportTask = getDbLayer().insertTaskByOrderStep(step, inventoryInfo, syncCompleted);
                        }
                    }
                    try {
                        boolean syncCompleted = calculateIsSyncCompleted(step.getStepStartTime(), step.getStepEndTime(), dateToAsMinutes);
                        re = getDbLayer().insertExecution(step, rt, reportTask, syncCompleted);
                    } catch (Exception e) {
                        throw new Exception(String.format("error on insertExecution: %s", e.toString()), e);
                    }
                    LOGGER.debug(String.format("%s: %s) execution created re.getId=%s, rt.getId=%s", method, counterTotal, re.getId(), rt.getId()));

                    counterInsertedExecutions++;
                } else {
                    try {
                        boolean syncCompleted = calculateIsSyncCompleted(step.getStepStartTime(), step.getStepEndTime(), dateToAsMinutes);
                        re = getDbLayer().updateExecution(re, step, syncCompleted);
                    } catch (Exception e) {
                        throw new Exception(String.format("error on updateExecution: %s", e.toString()));
                    }
                    LOGGER.debug(String.format("%s: %s) execution updated re.getId=%s, rt.getId=%s", method, counterTotal, re.getId(), rt.getId()));

                    counterUpdatedExecutions++;
                }
                re.setTaskStartTime(step.getTaskStartTime());
                re.setTaskEndTime(step.getTaskEndTime());

                LOGGER.debug(String.format("%s: %s) step.getStepStep=%s, rt.getResultSteps=%s", method, counterTotal, step.getStepStep(), rt
                        .getResultSteps()));

                if (step.getStepStep() >= rt.getResultSteps()) {
                    try {
                        rt = getDbLayer().updateTriggerResults(rt, re);
                    } catch (Exception e) {
                        throw new Exception(String.format("error on updateTriggerResults: %s", e.toString()), e);
                    }
                    triggerObjects.put(step.getOrderHistoryId(), rt);
                }
                pluginOnProcess(rt, re);

                if (counterTotal % options.log_info_step.value() == 0) {
                    LOGGER.info(String.format("%s: %s of %s history steps processed ...", method, counterTotal, totalSize));

                    triggerObjects = null;
                    triggerObjects = new HashMap<Long, DBItemReportTrigger>();
                    triggerObjects.put(step.getOrderHistoryId(), rt);
                }
            }
            getDbLayer().getSession().commit();
            LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));

            counter.setTotal(counterTotal);
            counter.setSkip(counterSkip);
            counter.setInsertedTriggers(counterInsertedTriggers);
            counter.setUpdatedTriggers(counterUpdatedTriggers);
            counter.setInsertedExecutions(counterInsertedExecutions);
            counter.setUpdatedExecutions(counterUpdatedExecutions);
            counter.setInsertedTasks(counterInsertedTasks);
            counter.setUpdatedTasks(counterUpdatedTasks);

        } catch (Exception e) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s: rollback %s", method, ex.toString()), ex);
            }
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        }
        return counter;
    }

    private InventoryInfo getInventoryInfo(List<Object[]> infos, boolean isTaskInfo) {
        InventoryInfo item = new InventoryInfo();
        item.setSchedulerId(null);
        item.setHostname(null);
        item.setPort(new Integer(0));
        item.setClusterType(null);
        item.setName(null);
        item.setTitle(null);
        item.setUrl(null);
        item.setIsOrderJob(false);
        item.setIsRuntimeDefined(false);
        item.setOrdering(new Integer(0));

        if (infos != null && infos.size() > 0) {
            try {
                if (isTaskInfo) {
                    for (int i = 0; i < infos.size(); i++) {
                        Object[] row = infos.get(i);

                        item.setSchedulerId((String) row[0]);
                        item.setHostname((String) row[1]);
                        item.setPort((Integer) row[2]);
                        item.setClusterType((String) row[3]);
                        item.setName((String) row[4]);
                        item.setTitle((String) row[5]);
                        item.setIsRuntimeDefined((row[6] + "").equals("1"));
                        item.setIsOrderJob((row[7] + "").equals("1"));
                        item.setUrl((String) row[8]);
                        item.setOrdering(row[9] == null ? null : (Integer) row[9]);
                        if (item.getOrdering() != null && item.getOrdering().equals(new Integer(1))) {
                            break;
                        }
                    }

                } else {
                    Object[] row = infos.get(0);
                    item.setTitle((String) row[0]);
                    item.setIsRuntimeDefined((row[1] + "").equals("1"));
                    item.setIsOrderJob((row[2] + "").equals("1"));
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("can't create InventoryInfo object: %s", ex.toString()));
            }
        }
        return item;
    }

    private void initCounters() throws Exception {
        counterOrderSync = new CounterSynchronize();
        counterOrderSyncUncompleted = new CounterSynchronize();

        counterTaskSync = new CounterSynchronize();
        counterTaskSyncUncompleted = new CounterSynchronize();
    }

    private void setChangedSummary() throws Exception {
        isChanged = false;
        isOrdersChanged = false;
        isTasksChanged = false;
        if (counterOrderSync.getInsertedTriggers() > 0 || counterOrderSync.getUpdatedTriggers() > 0 || counterOrderSync.getInsertedExecutions() > 0
                || counterOrderSync.getUpdatedExecutions() > 0 || counterOrderSyncUncompleted.getInsertedTriggers() > 0 || counterOrderSyncUncompleted
                        .getUpdatedTriggers() > 0 || counterOrderSyncUncompleted.getInsertedExecutions() > 0 || counterOrderSyncUncompleted
                                .getUpdatedExecutions() > 0) {
            isOrdersChanged = true;
            isChanged = true;
        }
        if (counterOrderSync.getInsertedTasks() > 0 || counterOrderSyncUncompleted.getInsertedTasks() > 0 || counterTaskSync.getInsertedTasks() > 0
                || counterTaskSync.getUpdatedTasks() > 0 || counterTaskSyncUncompleted.getInsertedTasks() > 0 || counterTaskSyncUncompleted
                        .getUpdatedTasks() > 0) {
            isTasksChanged = true;
            isChanged = true;
        }
    }

    private void logSummary(Date dateFrom, Date dateTo, DateTime start) throws Exception {
        String method = "logSummary";
        String from = ReportUtil.getDateAsString(dateFrom);
        String to = ReportUtil.getDateAsString(dateTo);
        if (isChanged) {
            String range = "order";
            if (isOrdersChanged) {
                LOGGER.info(String.format(
                        "[%s to %s UTC][%s][new]history steps=%s, triggers(inserted=%s, updated=%s), executions(inserted=%s, updated=%s), tasks(inserted=%s), skip=%s [old]total=%s, triggers(inserted=%s, updated), executions(inserted=%s, updated=%s), tasks(inserted=%s), skip=%s",
                        from, to, range, counterOrderSync.getTotal(), counterOrderSync.getInsertedTriggers(), counterOrderSync.getUpdatedTriggers(),
                        counterOrderSync.getInsertedExecutions(), counterOrderSync.getUpdatedExecutions(), counterOrderSync.getInsertedTasks(),
                        counterOrderSync.getSkip(), counterOrderSyncUncompleted.getTotal(), counterOrderSyncUncompleted.getInsertedTriggers(),
                        counterOrderSyncUncompleted.getUpdatedTriggers(), counterOrderSyncUncompleted.getInsertedExecutions(),
                        counterOrderSyncUncompleted.getUpdatedExecutions(), counterOrderSyncUncompleted.getInsertedTasks(),
                        counterOrderSyncUncompleted.getSkip()));
            } else {
                LOGGER.info(String.format("[%s to %s UTC][%s] 0 changes", from, to, range));
            }
            range = "task";
            if (isTasksChanged) {
                LOGGER.info(String.format(
                        "[%s to %s UTC][%s][new]history tasks=%s, tasks(inserted=%s, updated=%s), skip=%s [old]total=%s, tasks(inserted=%s, updated=%s), skip=%s",
                        from, to, range, counterTaskSync.getTotal(), counterTaskSync.getInsertedTasks(), counterTaskSync.getUpdatedTasks(),
                        counterTaskSync.getSkip(), counterTaskSyncUncompleted.getTotal(), counterTaskSyncUncompleted.getInsertedTasks(),
                        counterTaskSyncUncompleted.getUpdatedTasks(), counterTaskSyncUncompleted.getSkip()));
            } else {
                LOGGER.info(String.format("[%s to %s UTC][%s] 0 changes", from, to, range));
            }
        } else {
            LOGGER.info(String.format("[%s to %s UTC] 0 changes", from, to));
        }
        LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
    }

    private Date getDateFrom(DBItemReportVariable reportingVariable, Date dateTo) throws Exception {
        String method = "getDateFrom";
        Long currentMaxAge = new Long(maxHistoryAge);
        Long storedMaxAge = reportingVariable.getNumericValue();
        Date storedDateFrom = SOSString.isEmpty(reportingVariable.getTextValue()) ? null : ReportUtil.getDateFromString(reportingVariable
                .getTextValue());
        Date dateFrom = null;
        LOGGER.debug(String.format("%s: storedDateFrom=%s, storedMaxAge=%s, currentMaxAge=%s", method, ReportUtil.getDateAsString(storedDateFrom),
                storedMaxAge, currentMaxAge));

        if (storedDateFrom == null) {// initial value
            dateFrom = ReportUtil.getDateTimeMinusMinutes(dateTo, currentMaxAge);
            LOGGER.debug(String.format("%s: dateFrom=%s (initial, %s-%s)", method, ReportUtil.getDateAsString(dateFrom), ReportUtil.getDateAsString(
                    dateTo), currentMaxAge));
        } else {
            if (options.force_max_history_age.value()) {
                LOGGER.debug(String.format("%s: dateFrom=null (force_max_history_age=true)", method));
                dateFrom = null;
            } else {
                Long startTimeMinutes = storedDateFrom.getTime() / 1000 / 60;
                Long endTimeMinutes = dateTo.getTime() / 1000 / 60;
                Long diffMinutes = endTimeMinutes - startTimeMinutes;
                if (diffMinutes > currentMaxAge) {
                    Long countHistoryTasks = getDbLayer().getCountSchedulerHistoryTasks(schedulerSession, options.current_scheduler_id.getValue(),
                            storedDateFrom);
                    if (countHistoryTasks > maxHistoryTasks) {
                        dateFrom = null;
                        LOGGER.info(String.format("%s: dateFrom=null (%s > %s, countHistoryTasks %s > %s)", method, diffMinutes, currentMaxAge,
                                countHistoryTasks, maxHistoryTasks));
                    } else {
                        dateFrom = storedDateFrom;
                        LOGGER.debug(String.format("%s: dateFrom=%s (use storedDateFrom because %s > %s, countHistoryTasks %s <= %s)", method,
                                ReportUtil.getDateAsString(dateFrom), diffMinutes, currentMaxAge, countHistoryTasks, maxHistoryTasks));
                    }
                } else {
                    dateFrom = storedDateFrom;
                    LOGGER.debug(String.format("%s: dateFrom=%s (use storedDateFrom because %s < %s)", method, ReportUtil.getDateAsString(dateFrom),
                            diffMinutes, currentMaxAge));
                }
                // dateFrom = storedDateFrom;
            }
            if (dateFrom == null) {
                dateFrom = ReportUtil.getDateTimeMinusMinutes(dateTo, currentMaxAge);
                LOGGER.debug(String.format("%s: dateFrom=%s (%s-%s)", method, ReportUtil.getDateAsString(dateFrom), ReportUtil.getDateAsString(
                        dateTo), currentMaxAge));
            }
        }
        return dateFrom;
    }

    private void registerPlugin() {
        if (options.execute_notification_plugin.value()) {
            notificationPlugin = new FactNotificationPlugin();
        }
    }

    private void pluginOnInit(PluginMailer mailer, Path configDirectory) {
        if (notificationPlugin != null) {
            notificationPlugin.init(getDbLayer().getSession(), mailer, configDirectory);
            if (notificationPlugin.getHasErrorOnInit()) {
                notificationPlugin = null;
            }
        }
    }

    private void pluginOnProcess(DBItemReportTrigger trigger, DBItemReportExecution execution) {
        if (notificationPlugin == null) {
            return;
        }
        LOGGER.debug(String.format("pluginOnProcess: trigger.id=%s, execution.id=%s", trigger.getId(), execution.getId()));
        notificationPlugin.process(notificationPlugin.convert(trigger, execution));
    }

    private void pluginOnExit() {
        if (notificationPlugin != null) {
            notificationPlugin = null;
        }
    }

    public boolean isChanged() {
        return isChanged;
    }

    public boolean isTasksChanged() {
        return isTasksChanged;
    }

    public boolean isOrdersChanged() {
        return isOrdersChanged;
    }
}