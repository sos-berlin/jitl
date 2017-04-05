package com.sos.jitl.reporting.model.report;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private CounterCreateResult counterTriggerAggregated;
    private CounterCreateResult counterExecutionAggregated;
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
                aggregateTriggers(this.options.current_scheduler_id.getValue());
                aggregateExecutions(this.options.current_scheduler_id.getValue());
                aggregateTasks(this.options.current_scheduler_id.getValue());
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

    public void aggregateTriggers(String schedulerId) throws Exception {
        String method = "aggregateTriggers";

        int countExecutionDates = 0;
        int countTotal = 0;
        ScrollableResults results = null;
        try {
            LOGGER.info(String.format("%s", method));
            DateTime start = new DateTime();

            getDbLayer().getSession().beginTransaction();
            Criteria criteria = getDbLayer().getResultsUncompletedTriggers(largeResultFetchSizeReporting, schedulerId);
            results = criteria.scroll(ScrollMode.FORWARD_ONLY);
            while (results.next()) {
                countTotal++;

                DBItemReportTrigger item = (DBItemReportTrigger) results.get(0);
                if (item == null || item.getId() == null) {
                    throw new Exception("trigger or trigger.getId() is NULL");
                }

                getDbLayer().removeExecutionDates(EReferenceType.TRIGGER, item.getId());

                insertExecutionDate(EReferenceType.TRIGGER, item.getSchedulerId(), item.getHistoryId(), item.getId(), item.getStartTime(), item
                        .getEndTime());

                item.setResultsCompleted(true);
                getDbLayer().getSession().update(item);

                countExecutionDates++;

                if (countTotal % options.log_info_step.value() == 0) {
                    LOGGER.info(String.format("%s: %s entries processed ...", method, options.log_info_step.value()));
                }
            }
            getDbLayer().getSession().commit();

            counterTriggerAggregated.setTotalUncompleted(countTotal);
            counterTriggerAggregated.setExecutionsDates(countExecutionDates);
            LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));

        } catch (Exception ex) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception e) {
                LOGGER.warn(String.format("%s: rollback %s", method, ex.toString()), ex);
            }
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        } finally {
            if (results != null) {
                results.close();
            }
        }
    }

    public void aggregateExecutions(String schedulerId) throws Exception {
        String method = "aggregateExecutions";

        int countExecutionDates = 0;
        int countTotal = 0;
        ScrollableResults results = null;
        try {
            LOGGER.info(String.format("%s", method));
            DateTime start = new DateTime();

            getDbLayer().getSession().beginTransaction();
            Criteria criteria = getDbLayer().getResultsUncompletedExecutions(largeResultFetchSizeReporting, schedulerId);
            results = criteria.scroll(ScrollMode.FORWARD_ONLY);
            while (results.next()) {
                countTotal++;

                DBItemReportExecution item = (DBItemReportExecution) results.get(0);
                if (item == null || item.getId() == null) {
                    throw new Exception("item or item.getId() is NULL");
                }

                getDbLayer().removeExecutionDates(EReferenceType.EXECUTION, item.getId());

                insertExecutionDate(EReferenceType.EXECUTION, item.getSchedulerId(), item.getHistoryId(), item.getId(), item.getStartTime(), item
                        .getEndTime());

                item.setResultsCompleted(true);
                getDbLayer().getSession().update(item);

                countExecutionDates++;

                if (countTotal % options.log_info_step.value() == 0) {
                    LOGGER.info(String.format("%s: %s entries processed ...", method, options.log_info_step.value()));
                }
            }
            getDbLayer().getSession().commit();

            counterExecutionAggregated.setTotalUncompleted(countTotal);
            counterExecutionAggregated.setExecutionsDates(countExecutionDates);
            LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));

        } catch (Exception ex) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception e) {
                LOGGER.warn(String.format("%s: rollback %s", method, ex.toString()), ex);
            }
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        } finally {
            if (results != null) {
                results.close();
            }
        }
    }

    public void aggregateTasks(String schedulerId) throws Exception {
        String method = "aggregateTasks";

        int countExecutionDates = 0;
        int countTotal = 0;
        ScrollableResults results = null;
        try {
            LOGGER.info(String.format("%s", method));
            DateTime start = new DateTime();

            getDbLayer().getSession().beginTransaction();
            Criteria criteria = getDbLayer().getResultsUncompletedTasks(largeResultFetchSizeReporting, schedulerId);
            results = criteria.scroll(ScrollMode.FORWARD_ONLY);
            while (results.next()) {
                countTotal++;

                DBItemReportTask item = (DBItemReportTask) results.get(0);
                if (item == null || item.getId() == null) {
                    throw new Exception("item or item.getId() is NULL");
                }

                getDbLayer().removeExecutionDates(EReferenceType.TASK, item.getId());

                insertExecutionDate(EReferenceType.TASK, item.getSchedulerId(), item.getHistoryId(), item.getId(), item.getStartTime(), item
                        .getEndTime());

                item.setResultsCompleted(true);
                getDbLayer().getSession().update(item);

                countExecutionDates++;

                if (countTotal % options.log_info_step.value() == 0) {
                    LOGGER.info(String.format("%s: %s entries processed ...", method, options.log_info_step.value()));
                }
            }
            getDbLayer().getSession().commit();

            counterTaskAggregated.setTotalUncompleted(countTotal);
            counterTaskAggregated.setExecutionsDates(countExecutionDates);
            LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));

        } catch (Exception ex) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception e) {
                LOGGER.warn(String.format("%s: rollback %s", method, ex.toString()), ex);
            }
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        } finally {
            if (results != null) {
                results.close();
            }
        }
    }

    private void initCounters() throws Exception {
        counterTriggerAggregated = new CounterCreateResult();
        counterExecutionAggregated = new CounterCreateResult();
        counterTaskAggregated = new CounterCreateResult();
    }

    private void logSummary(DateTime start) throws Exception {
        String method = "logSummary";

        LOGGER.info(String.format("%s[trigger]: total=%s, inserted execution dates=%s", method, counterTriggerAggregated.getTotalUncompleted(),
                counterTriggerAggregated.getExecutionsDates()));

        LOGGER.info(String.format("%s[execution]: total=%s, inserted execution dates=%s", method, counterExecutionAggregated.getTotalUncompleted(),
                counterExecutionAggregated.getExecutionsDates()));

        LOGGER.info(String.format("%s[task]: total=%s, inserted execution dates=%s", method, counterTaskAggregated.getTotalUncompleted(),
                counterTaskAggregated.getExecutionsDates()));

        LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
    }
}
