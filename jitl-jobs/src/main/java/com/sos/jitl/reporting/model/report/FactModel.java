package com.sos.jitl.reporting.model.report;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.LockTimeoutException;
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
import com.sos.jitl.reporting.db.DBLayerReporting;
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
    /** result rerun interval in seconds */
    private static final long RERUN_INTERVAL = 2;
    private static final int MAX_RERUNS = 3;
    private FactJobOptions options;
    private SOSHibernateSession schedulerSession;
    private CounterSynchronize counterOrderSyncUncompleted;
    private CounterSynchronize counterOrderSync;
    private CounterSynchronize counterTaskSyncUncompleted;
    private CounterSynchronize counterTaskSync;
    private CounterSynchronize counterTaskSyncNotFounded;
    private boolean isChanged = false;
    private boolean isOrdersChanged = false;
    private boolean isTasksChanged = false;
    private int maxHistoryAge;
    private int maxUncompletedAge;
    private Long maxHistoryTasks;
    private List<Long> uncompletedTaskHistoryIds;
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

            synchronizeUncompletedTasks(options.current_scheduler_id.getValue(), dateToAsMinutes);
            synchronizeTasks(options.current_scheduler_id.getValue(), dateFrom, dateTo, dateToAsMinutes);

            synchronizeUncompletedOrders(options.current_scheduler_id.getValue(), dateToAsMinutes);
            synchronizeOrders(options.current_scheduler_id.getValue(), dateFrom, dateTo, dateToAsMinutes);

            synchronizeNotFounded();

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

    @SuppressWarnings("unchecked")
    private void synchronizeUncompletedOrders(String schedulerId, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeUncompletedOrders";
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
                    LOGGER.info(String.format("%s: found %s uncompleted orders in the reporting db", method, size));
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
    private void synchronizeUncompletedTasks(String schedulerId, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeUncompletedTasks";
        LOGGER.debug(String.format("%s", method));
        uncompletedTaskHistoryIds = new ArrayList<Long>();
        try {
            Criteria cr = getDbLayer().getTaskSyncUncomplitedHistoryIds(largeResultFetchSizeReporting, schedulerId);
            List<Long> result = cr.list();
            for (int i = 0; i < result.size(); i++) {
                Long historyId = result.get(i);
                if (!uncompletedTaskHistoryIds.contains(historyId)) {
                    uncompletedTaskHistoryIds.add(historyId);
                }
            }
        } catch (Exception e) {
            throw new Exception(String.format("%s: error on getTaskSyncUncomplitedHistoryIds: %s", method, e.toString()), e);
        }

        if (!uncompletedTaskHistoryIds.isEmpty()) {
            try {
                int size = uncompletedTaskHistoryIds.size();
                LOGGER.debug(String.format("%s: found %s uncompleted tasks in the reporting db", method, size));
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
                            subList = uncompletedTaskHistoryIds.subList(i, (i + SOSHibernateSession.LIMIT_IN_CLAUSE));
                        } else {
                            subList = uncompletedTaskHistoryIds.subList(i, size);
                        }
                        Criteria cr = getDbLayer().getSchedulerHistoryTasks(schedulerSession, largeResultFetchSizeScheduler, schedulerId, subList);
                        CounterSynchronize counter = synchronizeTaskHistory(cr, true, schedulerId, dateToAsMinutes);
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
                    Criteria cr = getDbLayer().getSchedulerHistoryTasks(schedulerSession, largeResultFetchSizeScheduler, schedulerId,
                            uncompletedTaskHistoryIds);
                    counterTaskSyncUncompleted = synchronizeTaskHistory(cr, true, schedulerId, dateToAsMinutes);
                }
            } catch (Exception e) {
                throw new Exception(String.format("%s: error on synchronizeTaskHistory: %s", method, e.toString()), e);
            }
        }

    }

    private void synchronizeOrders(String schedulerId, Date dateFrom, Date dateTo, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeOrders";
        try {
            LOGGER.debug(String.format("%s: schedulerId = %s, dateFrom = %s, dateTo = %s", method, schedulerId, ReportUtil.getDateAsString(dateFrom),
                    ReportUtil.getDateAsString(dateTo)));

            Criteria cr = getDbLayer().getSchedulerHistoryOrderSteps(schedulerSession, largeResultFetchSizeScheduler, schedulerId, dateFrom, dateTo);
            counterOrderSync = synchronizeOrderHistory(cr, dateToAsMinutes);
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    private void synchronizeTasks(String schedulerId, Date dateFrom, Date dateTo, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeTasks";
        try {
            LOGGER.debug(String.format("%s: schedulerId = %s, dateFrom = %s, dateTo = %s", method, schedulerId, ReportUtil.getDateAsString(dateFrom),
                    ReportUtil.getDateAsString(dateTo)));

            Criteria cr = getDbLayer().getSchedulerHistoryTasks(schedulerSession, largeResultFetchSizeScheduler, schedulerId, dateFrom, dateTo);
            counterTaskSync = synchronizeTaskHistory(cr, false, schedulerId, dateToAsMinutes);
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

    private synchronized CounterSynchronize synchronizeTaskHistory(Criteria criteria, boolean isUncomplete, String schedulerId, Long dateToAsMinutes)
            throws Exception {
        String method = "synchronizeTaskHistory";
        List<DBItemSchedulerHistory> result = getSchedulerHistoryTasks(criteria);
        CounterSynchronize counter = null;
        int count = 0;
        boolean run = true;
        while (run) {
            count++;
            try {
                counter = synchronizeTaskHistory(result, isUncomplete, schedulerId, dateToAsMinutes);
                run = false;
            } catch (Throwable e) {
                if (count >= MAX_RERUNS) {
                    throw e;
                } else {
                    Throwable te = findLockException(e);
                    if (te == null) {
                        throw e;
                    } else {
                        LOGGER.warn(String.format("%s: %s occured, wait %ss and try again (%s of %s) ...", method, te.getClass().getName(),
                                RERUN_INTERVAL, count, MAX_RERUNS));
                        Thread.sleep(RERUN_INTERVAL * 1000);
                    }
                }
            }
        }
        return counter;
    }

    @SuppressWarnings("unchecked")
    private List<DBItemSchedulerHistory> getSchedulerHistoryTasks(Criteria criteria) throws Exception {
        String method = "getSchedulerHistoryTasks";
        List<DBItemSchedulerHistory> result = null;
        try {
            schedulerSession.beginTransaction();
            result = criteria.list();
            schedulerSession.commit();
        } catch (Exception e) {
            try {
                schedulerSession.rollback();
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s: schedulerConnection %s", method, ex.toString()), ex);
            }
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        }
        return result;
    }

    private CounterSynchronize synchronizeTaskHistory(List<DBItemSchedulerHistory> result, boolean isUncomplete, String schedulerId,
            Long dateToAsMinutes) throws Exception {
        String method = "synchronizeTaskHistory";
        CounterSynchronize counter = new CounterSynchronize();
        try {
            LOGGER.debug(String.format("%s", method));
            DateTime start = new DateTime();
            int counterTotal = 0;
            int counterSkip = 0;
            int counterInserted = 0;
            int counterUpdated = 0;
            int totalSize = result.size();
            LOGGER.debug(String.format("%s: found %s tasks in the scheduler db", method, totalSize));

            getDbLayer().getSession().beginTransaction();
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
                        infos = getDbLayer().getInventoryJobInfoByJobName(options.current_scheduler_id.getValue(), options.current_scheduler_hostname
                                .getValue(), options.current_scheduler_http_port.value(), task.getJobName());
                    } catch (Exception e) {
                        throw new Exception(String.format("error on getInventoryJobInfoByJobName: %s", e.toString()), e);
                    }
                    InventoryInfo inventoryInfo = getInventoryInfo(infos);
                    LOGGER.debug(String.format("%s: %s) getInventoryJobInfoByJobName(jobName=%s): eii.getTitle=%s, eii.getIsRuntimeDefined=%s",
                            method, counterTotal, task.getJobName(), inventoryInfo.getTitle(), inventoryInfo.getIsRuntimeDefined()));

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

                if (isUncomplete && uncompletedTaskHistoryIds.contains(task.getId())) {
                    uncompletedTaskHistoryIds.remove(task.getId());
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
        List<DBItemSchedulerHistoryOrderStepReporting> result = getSchedulerHistoryOrderSteps(criteria);
        CounterSynchronize counter = null;
        int count = 0;
        boolean run = true;
        while (run) {
            count++;
            try {
                counter = synchronizeOrderHistory(result, dateToAsMinutes);
                run = false;
            } catch (Throwable e) {
                if (count >= MAX_RERUNS) {
                    throw e;
                } else {
                    Throwable te = findLockException(e);
                    if (te == null) {
                        throw e;
                    } else {
                        LOGGER.warn(String.format("%s: %s occured, wait %ss and try again (%s of %s) ...", method, te.getClass().getName(),
                                RERUN_INTERVAL, count, MAX_RERUNS));
                        Thread.sleep(RERUN_INTERVAL * 1000);
                    }
                }
            }
        }
        return counter;
    }

    @SuppressWarnings("unchecked")
    private List<DBItemSchedulerHistoryOrderStepReporting> getSchedulerHistoryOrderSteps(Criteria criteria) throws Exception {
        String method = "getSchedulerHistoryOrderSteps";
        List<DBItemSchedulerHistoryOrderStepReporting> result = null;
        try {
            schedulerSession.beginTransaction();
            result = criteria.list();
            schedulerSession.commit();
        } catch (Exception e) {
            try {
                schedulerSession.rollback();
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s: schedulerConnection %s", method, ex.toString()), ex);
            }
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        }
        return result;
    }

    private CounterSynchronize synchronizeOrderHistory(List<DBItemSchedulerHistoryOrderStepReporting> result, Long dateToAsMinutes) throws Exception {
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
            int totalSize = result.size();
            LOGGER.info(String.format("%s: found %s order steps in the scheduler db", method, totalSize));

            getDbLayer().getSession().beginTransaction();
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
                    LOGGER.debug(String.format("%s: %s) task object is null. jobChain=%s, order=%s, orderHistoryId=%s, step=%s, taskId=%s ", method,
                            counterTotal, step.getOrderJobChain(), step.getOrderId(), step.getOrderHistoryId(), step.getStepState(), step
                                    .getStepTaskId()));

                    reportTask = getDbLayer().getTask(step.getOrderSchedulerId(), step.getStepTaskId());
                    if (reportTask == null || reportTask.getBasename().equals(DBLayerReporting.NOT_FOUNDED_JOB_BASENAME)) {
                        List<Object[]> infos = null;
                        try {
                            infos = getDbLayer().getInventoryJobInfoByJobChain(options.current_scheduler_id.getValue(),
                                    options.current_scheduler_hostname.getValue(), options.current_scheduler_http_port.value(), step
                                            .getOrderJobChain(), step.getStepState());
                        } catch (Exception e) {
                            throw new Exception(String.format("error on getInventoryJobInfoByJobChain: %s", e.toString()), e);
                        }
                        InventoryInfo taskInventoryInfo = getInventoryInfo(infos);
                        if (reportTask == null) {
                            reportTask = getDbLayer().insertTaskByOrderStep(step, taskInventoryInfo, false);
                            LOGGER.debug(String.format("%s: %s) task created: id=%s, name=%s", method, counterTotal, reportTask.getId(), reportTask
                                    .getName()));
                            counterInsertedTasks++;
                        } else {
                            LOGGER.debug(String.format("%s: %s) task already exist: id=%s, name=%s. try to find the correct name ...", method,
                                    counterTotal, reportTask.getId(), reportTask.getName()));
                            if (taskInventoryInfo != null && taskInventoryInfo.getName() != null) {
                                reportTask.setFolder(ReportUtil.getFolderFromName(taskInventoryInfo.getName()));
                                reportTask.setName(taskInventoryInfo.getName());
                                reportTask.setBasename(ReportUtil.getBasenameFromName(taskInventoryInfo.getName()));
                                reportTask.setTitle(taskInventoryInfo.getTitle());
                                reportTask.setModified(ReportUtil.getCurrentDateTime());
                                getDbLayer().getSession().update(reportTask);
                                LOGGER.debug(String.format("%s: %s) task updated: id=%s, name=%s", method, counterTotal, reportTask.getId(),
                                        reportTask.getName()));
                            }
                        }
                    } else {
                        LOGGER.debug(String.format("%s: %s) task already exist: id=%s, name=%s", method, counterTotal, reportTask.getId(), reportTask
                                .getName()));
                    }
                    step.setTaskId(reportTask.getHistoryId());
                    step.setTaskStartTime(reportTask.getStartTime());
                    step.setTaskJobName(reportTask.getName());
                    step.setTaskClusterMemberId(reportTask.getClusterMemberId());
                    step.setTaskAgentUrl(reportTask.getAgentUrl());
                    step.setTaskCause(reportTask.getCause());
                }

                LOGGER.debug(String.format(
                        "%s: %s) schedulerId=%s, orderHistoryId=%s, jobChain=%s, orderId=%s, step=%s, stepState=%s, taskJobName=%s", method,
                        counterTotal, step.getOrderSchedulerId(), step.getOrderHistoryId(), step.getOrderJobChain(), step.getOrderId(), step
                                .getStepStep(), step.getStepState(), step.getTaskJobName()));
                DBItemReportTrigger rt = null;
                if (triggerObjects.containsKey(step.getOrderHistoryId())) {
                    rt = triggerObjects.get(step.getOrderHistoryId());
                    LOGGER.debug(String.format("%s: %s) use trigger: id=%s, name=%s", method, counterTotal, rt.getId(), rt.getName()));
                } else {
                    boolean syncCompleted = calculateIsSyncCompleted(step.getOrderStartTime(), step.getOrderEndTime(), dateToAsMinutes);
                    rt = getDbLayer().getTrigger(step.getOrderSchedulerId(), step.getOrderHistoryId());
                    if (rt == null) {
                        List<Object[]> infos = null;
                        try {
                            infos = getDbLayer().getInventoryOrderInfoByJobChain(step.getOrderSchedulerId(), options.current_scheduler_hostname
                                    .getValue(), options.current_scheduler_http_port.value(), step.getOrderId(), step.getOrderJobChain());
                        } catch (Exception e) {
                            throw new Exception(String.format("error on getInventoryOrderInfoByJobChain: %s", e.toString()), e);
                        }
                        InventoryInfo triggerInventoryInfo = getInventoryInfo(infos);
                        LOGGER.debug(String.format(
                                "%s: %s) getInventoryOrderInfoByJobChain(orderId=%s, jobChain=%s): ii.getTitle=%s, ii.getIsRuntimeDefined=%s", method,
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
                        LOGGER.debug(String.format("%s: %s) trigger created: id=%s, name=%s", method, counterTotal, rt.getId(), rt.getName()));

                        counterInsertedTriggers++;
                    } else {
                        if (rt.getSyncCompleted() != syncCompleted || step.getOrderEndTime() != null) {
                            try {
                                rt = getDbLayer().updateTrigger(rt, step, syncCompleted);
                            } catch (Exception e) {
                                throw new Exception(String.format("error on updateTrigger: %s", e.toString()), e);
                            }
                            LOGGER.debug(String.format("%s: %s) trigger updated: id=%s, name=%s", method, counterTotal, rt.getId(), rt.getName()));
                            counterUpdatedTriggers++;
                        }
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
                                infos = getDbLayer().getInventoryJobInfoByJobName(options.current_scheduler_id.getValue(),
                                        options.current_scheduler_hostname.getValue(), options.current_scheduler_http_port.value(), ReportUtil
                                                .normalizeDbItemPath(step.getTaskJobName()));
                            } catch (Exception e) {
                                throw new Exception(String.format("error on getInventoryJobInfoByJobName: %s", e.toString()), e);
                            }
                            InventoryInfo inventoryInfo = getInventoryInfo(infos);
                            boolean syncCompleted = calculateIsSyncCompleted(step.getTaskStartTime(), step.getTaskEndTime(), dateToAsMinutes);
                            reportTask = getDbLayer().insertTaskByOrderStep(step, inventoryInfo, syncCompleted);
                            LOGGER.debug(String.format("%s: %s) reportTask created: id=%s, name=%s", method, counterTotal, reportTask.getId(),
                                    reportTask.getName()));
                            counterInsertedTasks++;
                        }
                    }
                    try {
                        boolean syncCompleted = calculateIsSyncCompleted(step.getStepStartTime(), step.getStepEndTime(), dateToAsMinutes);
                        re = getDbLayer().insertExecution(step, rt, reportTask, syncCompleted);
                    } catch (Exception e) {
                        throw new Exception(String.format("error on insertExecution: %s", e.toString()), e);
                    }
                    LOGGER.debug(String.format("%s: %s) execution created: id=%s, rt.id=%s, name=%s", method, counterTotal, re.getId(), rt.getId(), re
                            .getName()));

                    counterInsertedExecutions++;
                } else {
                    try {
                        boolean syncCompleted = calculateIsSyncCompleted(step.getStepStartTime(), step.getStepEndTime(), dateToAsMinutes);
                        re = getDbLayer().updateExecution(re, step, syncCompleted);
                    } catch (Exception e) {
                        throw new Exception(String.format("error on updateExecution: %s", e.toString()));
                    }
                    LOGGER.debug(String.format("%s: %s) execution updated: id=%s, rt.id=%s, name=%s", method, counterTotal, re.getId(), rt.getId(), re
                            .getId()));

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
                    LOGGER.debug(String.format("%s: %s) trigger result updated: id=%s, name=%s, resultSteps=%s", method, counterTotal, rt.getId(), rt
                            .getName(), rt.getResultSteps()));

                    triggerObjects.put(step.getOrderHistoryId(), rt);
                    counterUpdatedTriggers++;
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

    private Throwable findLockException(Throwable e) {
        Throwable cause = e;
        Throwable lockException = null;
        while (cause.getCause() != null) {
            cause = cause.getCause();
            if (cause != null) {
                if (cause instanceof LockAcquisitionException || cause instanceof LockTimeoutException) {
                    lockException = cause;
                    break;
                }
            }
        }
        return lockException;
    }

    private synchronized void synchronizeNotFounded() throws Exception {
        String method = "synchronizeNotFounded";
        counterTaskSyncNotFounded = new CounterSynchronize();
        int count = 0;
        boolean run = true;
        while (run) {
            count++;
            try {
                counterTaskSyncNotFounded = synchronizeNotFoundedTasks();
                run = false;
            } catch (Throwable e) {
                if (count >= MAX_RERUNS) {
                    throw e;
                } else {
                    Throwable te = findLockException(e);
                    if (te == null) {
                        throw e;
                    } else {
                        LOGGER.warn(String.format("%s: %s occured, wait %ss and try again (%s of %s) ...", method, te.getClass().getName(),
                                RERUN_INTERVAL, count, MAX_RERUNS));
                        Thread.sleep(RERUN_INTERVAL * 1000);
                    }
                }
            }
        }

    }

    private CounterSynchronize synchronizeNotFoundedTasks() throws Exception {
        String method = "synchronizeNotFoundedTasks";
        CounterSynchronize counter = new CounterSynchronize();
        int size = uncompletedTaskHistoryIds.size();
        int counterUpdatedTasks = 0;
        int counterUpdatedExecutions = 0;
        int counterSkip = 0;
        if (size > 0) {
            LOGGER.debug(String.format("%s: size=%s", method, size));

            try {
                getDbLayer().getSession().beginTransaction();
                for (Long historyId : uncompletedTaskHistoryIds) {
                    DBItemReportTask reportTask = getDbLayer().getTask(options.current_scheduler_id.getValue(), historyId);
                    if (reportTask == null) {
                        LOGGER.debug(String.format("%s: not found reportTasks for schedulerId=%s, historyId=%s", method, options.current_scheduler_id
                                .getValue(), historyId));
                    } else {
                        LOGGER.debug(String.format("%s: found reportTask.id=%s (historyId=%s)", method, reportTask.getId(), historyId));
                        if (reportTask.getIsOrder()) {
                            List<DBItemReportExecution> executions = getDbLayer().getExecutionsByTask(reportTask.getId());
                            if (executions == null || executions.size() == 0) {
                                LOGGER.debug(String.format("%s: not found executions for taskId=%s", method, reportTask.getId()));

                            } else {
                                LOGGER.debug(String.format("%s: found %s executions for taskId=%s", method, executions.size(), reportTask.getId()));
                                boolean doUpdate = false;
                                if (reportTask.getBasename().equals(DBLayerReporting.NOT_FOUNDED_JOB_BASENAME)) {
                                    DBItemReportExecution firstExecution = executions.get(0);
                                    List<Object[]> infos = null;
                                    try {
                                        infos = getDbLayer().getInventoryJobInfoByJobChain(options.current_scheduler_id.getValue(),
                                                options.current_scheduler_hostname.getValue(), options.current_scheduler_http_port.value(),
                                                firstExecution.getFolder(), firstExecution.getState());
                                    } catch (Exception e) {
                                        throw new Exception(String.format("error on getInventoryJobInfoByJobChain: %s", e.toString()), e);
                                    }
                                    InventoryInfo taskInventoryInfo = getInventoryInfo(infos);
                                    if (taskInventoryInfo != null && taskInventoryInfo.getName() != null) {
                                        reportTask.setFolder(ReportUtil.getFolderFromName(taskInventoryInfo.getName()));
                                        reportTask.setName(taskInventoryInfo.getName());
                                        reportTask.setBasename(ReportUtil.getBasenameFromName(taskInventoryInfo.getName()));
                                        reportTask.setTitle(taskInventoryInfo.getTitle());
                                        doUpdate = true;
                                    }
                                }

                                DBItemReportExecution lastExecutionWithEndTime = null;
                                Date endTime = reportTask.getEndTime();
                                boolean syncCompleted = false;
                                for (DBItemReportExecution execution : executions) {
                                    LOGGER.debug(String.format("%s: execution (id=%s, error=%s, syncCompleted=%s, endTime=%s)", method, execution
                                            .getId(), execution.getError(), execution.getSyncCompleted(), execution.getEndTime()));

                                    if (execution.getSyncCompleted()) {
                                        syncCompleted = true;
                                    }
                                    if (execution.getEndTime() != null) {
                                        if (endTime == null) {
                                            lastExecutionWithEndTime = execution;
                                        } else {
                                            if (execution.getEndTime().getTime() > endTime.getTime()) {
                                                lastExecutionWithEndTime = execution;
                                            }
                                        }
                                    }

                                    if (execution.getBasename().equals(DBLayerReporting.NOT_FOUNDED_JOB_BASENAME) && doUpdate) {
                                        execution.setFolder(reportTask.getFolder());
                                        execution.setName(reportTask.getName());
                                        execution.setBasename(reportTask.getBasename());
                                        execution.setTitle(reportTask.getTitle());
                                        execution.setModified(ReportUtil.getCurrentDateTime());
                                        getDbLayer().getSession().update(execution);
                                        counterUpdatedExecutions++;
                                    }
                                }
                                if (syncCompleted) {
                                    reportTask.setSyncCompleted(true);
                                    doUpdate = true;
                                }
                                if (lastExecutionWithEndTime != null) {
                                    LOGGER.debug(String.format("%s: found last execution (id=%s, error=%s)", method, lastExecutionWithEndTime.getId(),
                                            lastExecutionWithEndTime.getError()));
                                    reportTask.setEndTime(lastExecutionWithEndTime.getEndTime());
                                    reportTask.setSyncCompleted(true);
                                    if (!reportTask.getError()) {
                                        reportTask.setError(lastExecutionWithEndTime.getError());
                                        reportTask.setErrorCode(lastExecutionWithEndTime.getErrorCode());
                                        reportTask.setErrorText(lastExecutionWithEndTime.getErrorText());
                                    }
                                    doUpdate = true;
                                }

                                if (doUpdate) {
                                    reportTask.setModified(ReportUtil.getCurrentDateTime());
                                    getDbLayer().getSession().update(reportTask);
                                    counterUpdatedTasks++;
                                } else {
                                    counterSkip++;
                                }
                            }
                        } else {
                            LOGGER.debug(String.format("%s: task has isOrder=0", method));
                            reportTask.setSyncCompleted(true);
                            reportTask.setModified(ReportUtil.getCurrentDateTime());
                            getDbLayer().getSession().update(reportTask);
                            counterUpdatedTasks++;
                        }
                    }
                }
                getDbLayer().getSession().commit();
            } catch (Exception e) {
                try {
                    getDbLayer().getSession().rollback();
                } catch (Exception ex) {
                    LOGGER.warn(String.format("%s: rollback %s", method, ex.toString()), ex);
                }
                throw new Exception(String.format("%s: %s", method, e.toString()), e);
            }
        } else {
            LOGGER.debug(String.format("%s: skip", method));
        }
        counter.setTotal(size);
        counter.setSkip(counterSkip);
        counter.setUpdatedTasks(counterUpdatedTasks);
        counter.setUpdatedExecutions(counterUpdatedExecutions);
        return counter;
    }

    private InventoryInfo getInventoryInfo(List<Object[]> infos) {
        InventoryInfo item = new InventoryInfo();
        item.setSchedulerId(options.current_scheduler_id.getValue());
        item.setHostname(options.current_scheduler_hostname.getValue());
        item.setPort(options.current_scheduler_http_port.value());

        item.setName(null);
        item.setTitle(null);
        item.setIsRuntimeDefined(false);

        item.setIsOrderJob(false);

        item.setClusterType(null);
        item.setUrl(null);
        item.setOrdering(new Integer(0));

        if (infos != null && infos.size() > 0) {
            try {
                for (int i = 0; i < infos.size(); i++) {
                    Object[] row = infos.get(i);
                    item.setName((String) row[0]);
                    item.setTitle((String) row[1]);
                    item.setIsRuntimeDefined((row[2] + "").equals("1"));
                    if (row.length > 3) {
                        item.setIsOrderJob((row[3] + "").equals("1"));
                    }
                    if (row.length > 4) {
                        item.setClusterType((String) row[4]);
                        item.setUrl((String) row[5]);
                        item.setOrdering(row[6] == null ? null : Integer.parseInt(row[6].toString()));
                        if (item.getOrdering() != null && item.getOrdering().equals(new Integer(1))) {
                            break;
                        }
                    }
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
        counterTaskSyncNotFounded = new CounterSynchronize();
    }

    private void setChangedSummary() throws Exception {
        isChanged = false;
        isOrdersChanged = false;
        isTasksChanged = false;
        if (counterOrderSync.getInsertedTriggers() > 0 || counterOrderSync.getUpdatedTriggers() > 0 || counterOrderSync.getInsertedExecutions() > 0
                || counterOrderSync.getUpdatedExecutions() > 0 || counterOrderSyncUncompleted.getInsertedTriggers() > 0 || counterOrderSyncUncompleted
                        .getUpdatedTriggers() > 0 || counterOrderSyncUncompleted.getInsertedExecutions() > 0 || counterOrderSyncUncompleted
                                .getUpdatedExecutions() > 0 || counterTaskSyncNotFounded.getUpdatedExecutions() > 0) {
            isOrdersChanged = true;
            isChanged = true;
        }
        if (counterOrderSync.getInsertedTasks() > 0 || counterOrderSyncUncompleted.getInsertedTasks() > 0 || counterTaskSync.getInsertedTasks() > 0
                || counterTaskSync.getUpdatedTasks() > 0 || counterTaskSyncUncompleted.getInsertedTasks() > 0 || counterTaskSyncUncompleted
                        .getUpdatedTasks() > 0 || counterTaskSyncNotFounded.getUpdatedTasks() > 0) {
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
                        "[%s to %s UTC][%s][new]history tasks=%s, tasks(inserted=%s, updated=%s), skip=%s [old]total=%s, tasks(inserted=%s, updated=%s), skip=%s [not founded]total=%s, tasks(updated=%s), skip=%s",
                        from, to, range, counterTaskSync.getTotal(), counterTaskSync.getInsertedTasks(), counterTaskSync.getUpdatedTasks(),
                        counterTaskSync.getSkip(), counterTaskSyncUncompleted.getTotal(), counterTaskSyncUncompleted.getInsertedTasks(),
                        counterTaskSyncUncompleted.getUpdatedTasks(), counterTaskSyncUncompleted.getSkip(), counterTaskSyncNotFounded.getTotal(),
                        counterTaskSyncNotFounded.getUpdatedTasks(), counterTaskSyncNotFounded.getSkip()));
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