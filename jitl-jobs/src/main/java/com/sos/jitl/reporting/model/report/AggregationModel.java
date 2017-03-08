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
import com.sos.hibernate.classes.SOSHibernateStatelessSession;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportExecutionDate;
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
    private CounterCreateResult counterStandaloneAggregated;
    private Optional<Integer> largeResultFetchSizeReporting = Optional.empty();

    public AggregationModel(SOSHibernateStatelessSession reportingConn, AggregationJobOptions opt) throws Exception {

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
                aggregateStandalone(this.options.current_scheduler_id.getValue());
                completeAggregation(this.options.current_scheduler_id.getValue());
            } else {
                LOGGER.info(String.format("%s: skip processing. option \"execute_aggregation\" = false", method));
            }
            logSummary(start);

        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }
 
    private DBItemReportExecutionDate insertReportingExecutionDate(EReferenceType type, String schedulerId, Long historyId, Long id, Date startDate, Date endDate)
            throws Exception {

        String method = "insertReportingExecutionDate";

        if (startDate == null) {
            throw new Exception(String.format("%s: startDate is NULL (type = %s, schedulerId = %s, historyId = %s, id = %s) ", method, type.value(), schedulerId, historyId, id));
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

        DBItemReportExecutionDate item = createReportExecutionDate(schedulerId, historyId, type.value(), id, startDay, startWeek, startQuarter, startMonth, startYear, endDay,
                endWeek, endQuarter, endMonth, endYear);
        
        getDbLayer().getSession().save(item);
        return item;
    }

    public void aggregateStandalone(String schedulerId) throws Exception {
        String method = "aggregateStandalone";

        SOSHibernateResultSetProcessor rspExecutions = new SOSHibernateResultSetProcessor(getDbLayer().getSession());

        int countBatchExecutionDates = 0;
        int countExecutionDates = 0;
        int countTotal = 0;
        try {
            LOGGER.info(String.format("%s", method));
            getDbLayer().getSession().beginTransaction();
            
            DateTime start = new DateTime();
   
            Criteria crExecutions = getDbLayer().getStandaloneResultsUncompletedExecutions(largeResultFetchSizeReporting,schedulerId);
            ResultSet rsExecutions = rspExecutions.createResultSet(crExecutions, ScrollMode.FORWARD_ONLY, largeResultFetchSizeReporting);
            while (rsExecutions.next()) {
                countTotal++;

                DBItemReportExecution execution = (DBItemReportExecution) rspExecutions.get();
                DBItemReportExecutionDate exd = insertReportingExecutionDate(EReferenceType.EXECUTION, execution.getSchedulerId(), execution.getHistoryId(), execution.getId(),
                        execution.getStartTime(), execution.getEndTime());

                countExecutionDates++;
            }
         
            getDbLayer().getSession().commit();
            
            if (counterStandaloneAggregated != null) {
                counterStandaloneAggregated.setTotalUncompleted(countTotal);
                counterStandaloneAggregated.setExecutionsDates(countExecutionDates);
                counterStandaloneAggregated.setExecutionsDatesBatch(countBatchExecutionDates);
            }

            LOGGER.info(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
            
        } catch (Exception ex) {
        	getDbLayer().getSession().rollback();
        	
            throw new Exception(SOSHibernateSession.getException(ex));
        } finally {
            if (rspExecutions != null) {
                rspExecutions.close();
            }
        }
    }

    public void aggregateOrder(String schedulerId) throws Exception {
        String method = "aggregateOrder";

        SOSHibernateResultSetProcessor rspTriggers = new SOSHibernateResultSetProcessor(getDbLayer().getSession());
        SOSHibernateResultSetProcessor rspExecutions = new SOSHibernateResultSetProcessor(getDbLayer().getSession());

        int countBatchExecutionDates = 0;
        int countExecutionDates = 0;
        int countTotal = 0;
        try {
            LOGGER.info(String.format("%s", method));
            
            getDbLayer().getSession().beginTransaction();
            
            DateTime start = new DateTime();
            Criteria crTriggers = getDbLayer().getOrderResultsUncompletedTriggers(largeResultFetchSizeReporting,schedulerId);
            ResultSet rsTriggers = rspTriggers.createResultSet(crTriggers, ScrollMode.FORWARD_ONLY, largeResultFetchSizeReporting);
            while (rsTriggers.next()) {
                countTotal++;

                DBItemReportTrigger trigger = (DBItemReportTrigger) rspTriggers.get();
                if (trigger == null || trigger.getId() == null) {
                    throw new Exception("trigger or trigger.getId() is NULL");
                }

                try {
                    Criteria crExecutions = getDbLayer().getOrderResultsUncompletedExecutions(largeResultFetchSizeReporting, trigger.getId());
                    ResultSet rsExecutions = rspExecutions.createResultSet(crExecutions, ScrollMode.FORWARD_ONLY, largeResultFetchSizeReporting);
                    while (rsExecutions.next()) {
                        DBItemReportExecution execution = (DBItemReportExecution) rspExecutions.get();
                         DBItemReportExecutionDate exd = insertReportingExecutionDate(EReferenceType.EXECUTION, execution.getSchedulerId(), execution.getHistoryId(), execution
                                .getId(), execution.getStartTime(), execution.getEndTime());
                        countExecutionDates++;
                    }
                } catch (Exception ex) {
                    throw new Exception(SOSHibernateSession.getException(ex));
                } finally {
                    rspExecutions.close();
                }
                DBItemReportExecutionDate exd = insertReportingExecutionDate(EReferenceType.TRIGGER, trigger.getSchedulerId(), trigger.getHistoryId(), trigger.getId(), trigger
                            .getStartTime(), trigger.getEndTime());

                countExecutionDates++;

                if (countTotal % options.log_info_step.value() == 0) {
                   LOGGER.info(String.format("%s: %s entries processed ...", method, options.log_info_step.value()));
                }
                
                if (counterOrderAggregated != null) {
                    counterOrderAggregated.setTotalUncompleted(countTotal);
                    counterOrderAggregated.setExecutionsDates(countExecutionDates);
                    counterOrderAggregated.setExecutionsDatesBatch(countBatchExecutionDates);
                }

                LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
            }
            
            getDbLayer().getSession().commit();
        } catch (Exception ex) {
        	getDbLayer().getSession().rollback();
        	
            Throwable e = SOSHibernateSession.getException(ex);
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        } finally {
            rspTriggers.close();
        }
    }

    private void initCounters() throws Exception {
        counterOrderAggregated = new CounterCreateResult();
        counterStandaloneAggregated = new CounterCreateResult();
    }

    private void logSummary(DateTime start) throws Exception {
        String method = "logSummary";

        String range = "order";
        LOGGER.info(String.format("%s[%s]: aggregated (total uncompleted triggers = %s): execution dates = %s of %s", method, range, counterOrderAggregated
                .getTotalUncompleted(), counterOrderAggregated.getExecutionsDatesBatch(), counterOrderAggregated.getExecutionsDates()));

        range = "standalone";
        LOGGER.info(String.format("%s[%s]: aggregated (total uncompleted executions = %s): execution dates = %s of %s", method, range, counterStandaloneAggregated
                .getTotalUncompleted(), counterStandaloneAggregated.getExecutionsDatesBatch(), counterStandaloneAggregated.getExecutionsDates()));

        LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
    }

    private DBItemReportExecutionDate createReportExecutionDate(String schedulerId, Long historyId, Long referenceType, Long referenceId, Long startDay, Long startWeek,
            Long startQuarter, Long startMonth, Long startYear, Long endDay, Long endWeek, Long endQuarter, Long endMonth, Long endYear) throws Exception {

        DBItemReportExecutionDate item = new DBItemReportExecutionDate();

        item.setSchedulerId(schedulerId);
        item.setHistoryId(historyId);
        item.setReferenceType(referenceType);
        item.setReferenceId(referenceId);
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

        return item;
    }

    private void completeAggregation(String schedulerId) throws Exception {
        String method = "completeAggregation";
        try {
            LOGGER.info(String.format("%s: schedulerId = %s", method, schedulerId));

            getDbLayer().getSession().beginTransaction();
            getDbLayer().triggerResultCompletedQuery(schedulerId);
            getDbLayer().executionResultCompletedQuery(schedulerId);
            getDbLayer().getSession().commit();
        } catch (Exception ex) {
            getDbLayer().getSession().rollback();
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

}
