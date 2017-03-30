package com.sos.jitl.reporting.model.report;

import java.sql.ResultSet;
import java.util.Date;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateResultSetProcessor;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportExecutionDate;
import com.sos.jitl.reporting.db.DBItemReportTask;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.helper.CounterCreateResult;
import com.sos.jitl.reporting.helper.EReferenceType;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.reporting.job.report.AggregationJobOptions;
import com.sos.jitl.reporting.model.IReportingModel;
import com.sos.jitl.reporting.model.ReportingModel;

public class AggregationModel extends ReportingModel implements IReportingModel {

    private Logger LOGGER = LoggerFactory.getLogger(AggregationModel.class);
    private AggregationJobOptions options;
    private CounterCreateResult counterOrderAggregated;
    private CounterCreateResult counterTaskAggregated;
    private Optional<Integer> largeResultFetchSizeReporting = Optional.empty();

    public AggregationModel(SOSHibernateSession reportingConn, AggregationJobOptions opt) throws Exception {

        super(reportingConn);
        options = opt;
        largeResultFetchSizeReporting = getFetchSize(options.large_result_fetch_size.value());
    }

    @Override
    public void process() throws Exception {
        String method = "process";
        try {
            LOGGER.info(String.format("%s: large_result_fetch_size = %s", method, options.large_result_fetch_size.getValue()));

            DateTime start = new DateTime();
            initCounters();

            if (options.execute_aggregation.value()) {
                aggregateOrder(this.options.current_scheduler_id.getValue());
                aggregateTask(this.options.current_scheduler_id.getValue());
            } else {
                LOGGER.info(String.format("%s: skip processing. option \"execute_aggregation\" = false", method));
            }
            logSummary(start);

        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    private DBItemReportExecutionDate insertExecutionDate(EReferenceType type, String schedulerId, Long historyId, Long id, Date startDate,
            Date endDate) throws Exception {

        String method = "insertReportingExecutionDate";

        if (startDate == null) {
            throw new Exception(String.format("%s: startDate is NULL (type = %s, schedulerId = %s, historyId = %s, id = %s) ", method, type.value(),
                    schedulerId, historyId, id));
        }

        DateTime startDateTime = new DateTime(startDate);
        Long startDay = ReportUtil.getDayOfMonth(startDateTime);
        Long startWeek = ReportUtil.getWeekOfWeekyear(startDateTime);
        Long startQuarter = ReportUtil.getQuarterOfYear(startDateTime);
        Long startMonth = ReportUtil.getMonthOfYear(startDateTime);
        Long startYear = ReportUtil.getYear(startDateTime);

        Long endDay = new Long(0);
        Long endWeek = new Long(0);
        Long endQuarter = new Long(0);
        Long endMonth = new Long(0);
        Long endYear = new Long(0);
        if (endDate != null) {
            DateTime endDateTime = new DateTime(endDate);

            endDay = ReportUtil.getDayOfMonth(endDateTime);
            endWeek = ReportUtil.getWeekOfWeekyear(endDateTime);
            endQuarter = ReportUtil.getQuarterOfYear(endDateTime);
            endMonth = ReportUtil.getMonthOfYear(endDateTime);
            endYear = ReportUtil.getYear(endDateTime);
        }

        DBItemReportExecutionDate item = new DBItemReportExecutionDate();

        item.setSchedulerId(schedulerId);
        item.setHistoryId(historyId);
        item.setReferenceType(type.value());
        item.setReferenceId(id);
        item.setStartDay(startDay);
        item.setStartWeek(startWeek);
        item.setStartMonth(startMonth);
        item.setStartQuarter(startQuarter);
        item.setStartYear(startYear);
        item.setEndDay(endDay);
        item.setEndWeek(endWeek);
        item.setEndMonth(endMonth);
        item.setEndQuarter(endQuarter);
        item.setEndYear(endYear);

        item.setCreated(ReportUtil.getCurrentDateTime());
        item.setModified(ReportUtil.getCurrentDateTime());

        getDbLayer().getSession().save(item);
        return item;
    }

    public void aggregateTask(String schedulerId) throws Exception {
        String method = "aggregateTask";

        SOSHibernateResultSetProcessor processorTasks = new SOSHibernateResultSetProcessor(getDbLayer().getSession());

        int countBatchExecutionDates = 0;
        int countExecutionDates = 0;
        int countTotal = 0;
        try {
            LOGGER.info(String.format("%s", method));
            DateTime start = new DateTime();

            getDbLayer().getSession().beginTransaction();
            Criteria criteria = getDbLayer().getTaskResultsUncompletedExecutions(largeResultFetchSizeReporting, schedulerId);
            ResultSet resultSet = processorTasks.createResultSet(criteria, ScrollMode.FORWARD_ONLY, largeResultFetchSizeReporting);
            while (resultSet.next()) {
                countTotal++;

                DBItemReportTask task = (DBItemReportTask) processorTasks.get();
                insertExecutionDate(EReferenceType.TASK, task.getSchedulerId(), task.getHistoryId(), task.getId(), task.getStartTime(), task
                        .getEndTime());

                task.setResultsCompleted(true);
                getDbLayer().getSession().update(task);

                countExecutionDates++;
            }
            getDbLayer().getSession().commit();

            counterTaskAggregated.setTotalUncompleted(countTotal);
            counterTaskAggregated.setExecutionsDates(countExecutionDates);
            counterTaskAggregated.setExecutionsDatesBatch(countBatchExecutionDates);
            LOGGER.info(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
        } catch (Exception ex) {
            getDbLayer().getSession().rollback();

            throw new Exception(SOSHibernateSession.getException(ex));
        } finally {
            if (processorTasks != null) {
                processorTasks.close();
            }
        }
    }

    public void aggregateOrder(String schedulerId) throws Exception {
        String method = "aggregateOrder";

        SOSHibernateResultSetProcessor processorTriggers = new SOSHibernateResultSetProcessor(getDbLayer().getSession());
        SOSHibernateResultSetProcessor processorExecutions = new SOSHibernateResultSetProcessor(getDbLayer().getSession());

        int countBatchExecutionDates = 0;
        int countExecutionDates = 0;
        int countTotal = 0;
        try {
            LOGGER.info(String.format("%s", method));
            DateTime start = new DateTime();

            getDbLayer().getSession().beginTransaction();
            Criteria criteriaTriggers = getDbLayer().getOrderResultsUncompletedTriggers(largeResultFetchSizeReporting, schedulerId);
            ResultSet resultSetTriggers = processorTriggers.createResultSet(criteriaTriggers, ScrollMode.FORWARD_ONLY, largeResultFetchSizeReporting);
            while (resultSetTriggers.next()) {
                countTotal++;

                DBItemReportTrigger trigger = (DBItemReportTrigger) processorTriggers.get();
                if (trigger == null || trigger.getId() == null) {
                    throw new Exception("trigger or trigger.getId() is NULL");
                }

                try {
                    Criteria criteriaExecutions = getDbLayer().getOrderResultsUncompletedExecutions(largeResultFetchSizeReporting, trigger.getId());
                    ResultSet resultSetExecutions = processorExecutions.createResultSet(criteriaExecutions, ScrollMode.FORWARD_ONLY,
                            largeResultFetchSizeReporting);
                    while (resultSetExecutions.next()) {
                        DBItemReportExecution execution = (DBItemReportExecution) processorExecutions.get();
                        insertExecutionDate(EReferenceType.EXECUTION, execution.getSchedulerId(), execution.getHistoryId(), execution.getId(),
                                execution.getStartTime(), execution.getEndTime());

                        execution.setResultsCompleted(true);
                        getDbLayer().getSession().update(execution);
                        countExecutionDates++;
                    }
                } catch (Exception ex) {
                    throw new Exception(SOSHibernateSession.getException(ex));
                } finally {
                    processorExecutions.close();
                }
                insertExecutionDate(EReferenceType.TRIGGER, trigger.getSchedulerId(), trigger.getHistoryId(), trigger.getId(), trigger.getStartTime(),
                        trigger.getEndTime());

                trigger.setResultsCompleted(true);
                getDbLayer().getSession().update(trigger);

                countExecutionDates++;

                if (countTotal % options.log_info_step.value() == 0) {
                    LOGGER.info(String.format("%s: %s entries processed ...", method, options.log_info_step.value()));
                }

                counterOrderAggregated.setTotalUncompleted(countTotal);
                counterOrderAggregated.setExecutionsDates(countExecutionDates);
                counterOrderAggregated.setExecutionsDatesBatch(countBatchExecutionDates);
                LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
            }

            getDbLayer().getSession().commit();
        } catch (Exception ex) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception e) {
                LOGGER.warn(String.format("%s: rollback %s", method, ex.toString()), ex);
            }
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        } finally {
            processorTriggers.close();
        }
    }

    private void initCounters() throws Exception {
        counterOrderAggregated = new CounterCreateResult();
        counterTaskAggregated = new CounterCreateResult();
    }

    private void logSummary(DateTime start) throws Exception {
        String method = "logSummary";

        String range = "order";
        LOGGER.info(String.format("%s[%s]: aggregated (total uncompleted triggers = %s): execution dates = %s of %s", method, range,
                counterOrderAggregated.getTotalUncompleted(), counterOrderAggregated.getExecutionsDatesBatch(), counterOrderAggregated
                        .getExecutionsDates()));

        range = "task";
        LOGGER.info(String.format("%s[%s]: aggregated (total uncompleted tasks = %s): execution dates = %s of %s", method, range,
                counterTaskAggregated.getTotalUncompleted(), counterTaskAggregated.getExecutionsDatesBatch(), counterTaskAggregated
                        .getExecutionsDates()));

        LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
    }
}
