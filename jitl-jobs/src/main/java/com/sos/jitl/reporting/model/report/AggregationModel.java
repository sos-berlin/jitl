package com.sos.jitl.reporting.model.report;

import java.sql.ResultSet;
import java.util.Date;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.hibernate.classes.SOSHibernateBatchProcessor;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateResultSetProcessor;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportExecutionDate;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.DBItemReportTriggerResult;
import com.sos.jitl.reporting.helper.CounterCreateResult;
import com.sos.jitl.reporting.helper.CounterUpdate;
import com.sos.jitl.reporting.helper.EReferenceType;
import com.sos.jitl.reporting.helper.EStartCauses;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.reporting.job.report.AggregationJobOptions;
import com.sos.jitl.reporting.model.IReportingModel;
import com.sos.jitl.reporting.model.ReportingModel;

public class AggregationModel extends ReportingModel implements IReportingModel {

    private Logger logger = LoggerFactory.getLogger(AggregationModel.class);

    private AggregationJobOptions options;

    private CounterCreateResult counterCreateResult;
    private CounterUpdate counterUpdate;

    private Optional<Integer> largeResultFetchSizeReporting = Optional.empty();

    public AggregationModel(SOSHibernateConnection reportingConn, AggregationJobOptions opt) throws Exception {

        super(reportingConn);
        options = opt;

        try {
            int fetchSize = options.large_result_fetch_size.value();
            if (fetchSize != -1) {
                largeResultFetchSizeReporting = Optional.of(fetchSize);
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public void process() throws Exception {
        String method = "process";

        DateTime start = new DateTime();
        try {
            logger.info(String.format("%s: batch_size = %s, large_result_fetch_size = %s", method, options.batch_size.value(),
                    options.large_result_fetch_size.getValue()));

            initCounters();

            if (options.force_update_from_inventory.value()) {
                updateReportingFromInventory(false);
            }

            if (options.execute_aggregation.value()) {
                if (!options.force_update_from_inventory.value()) {
                    updateReportingFromInventory(true);
                }

                createReportingResults();
                completeReportingResults();
            } else {
                logger.info(String.format("%s: skip processing. option \"execute_aggregation\" = false", method));
            }
            logSummary(start);

        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    private void updateReportingFromInventory(boolean updateOnlyResultUncompletedEntries) throws Exception {
        String method = "updateReportingFromInventory";

        logger.info(String.format("%s: updateOnlyResultUncompletedEntries = %s", method, updateOnlyResultUncompletedEntries));

        try {
            getDbLayer().getConnection().beginTransaction();
            counterUpdate.setTriggers(getDbLayer().updateReportingTriggerFromInventory(updateOnlyResultUncompletedEntries));
            getDbLayer().getConnection().commit();
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            logger.warn(String.format("%s: %s", method, ex.toString()));
        }

        try {
            getDbLayer().getConnection().beginTransaction();
            counterUpdate.setExecutions(getDbLayer().updateReportingExecutionFromInventory(updateOnlyResultUncompletedEntries));
            getDbLayer().getConnection().commit();

        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            logger.warn(String.format("%s: %s", method, ex.toString()));
        }
    }

    private DBItemReportExecutionDate insertReportingExecutionDate(EReferenceType type, String schedulerId, Long historyId, Long id, Date startDate,
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

        DBItemReportExecutionDate item =
                createReportExecutionDate(schedulerId, historyId, type.value(), id, startDay, startWeek, startQuarter, startMonth, startYear, endDay,
                        endWeek, endQuarter, endMonth, endYear);

        return item;
    }

    public void createReportingResults() throws Exception {
        String method = "createReportingResults";

        SOSHibernateBatchProcessor bpResults = new SOSHibernateBatchProcessor(getDbLayer().getConnection());
        SOSHibernateBatchProcessor bpExecutionDates = new SOSHibernateBatchProcessor(getDbLayer().getConnection());

        SOSHibernateResultSetProcessor rspTriggers = new SOSHibernateResultSetProcessor(getDbLayer().getConnection());
        SOSHibernateResultSetProcessor rspExecutions = new SOSHibernateResultSetProcessor(getDbLayer().getConnection());

        int countBatchTriggerResults = 0;
        int countBatchExecutionDates = 0;
        int countTriggerResults = 0;
        int countExecutionDates = 0;
        int countTotal = 0;
        try {
            logger.info(String.format("%s", method));

            DateTime start = new DateTime();

            bpResults.createInsertBatch(DBItemReportTriggerResult.class);
            bpExecutionDates.createInsertBatch(DBItemReportExecutionDate.class);

            // all we be added as batch insert - on this place no commit or
            // rollback
            Criteria crTriggers = getDbLayer().getResultUncompletedTriggersCriteria(largeResultFetchSizeReporting);
            ResultSet rsTriggers = rspTriggers.createResultSet(crTriggers, ScrollMode.FORWARD_ONLY, largeResultFetchSizeReporting);
            while (rsTriggers.next()) {
                countTotal++;

                if (countTotal % options.batch_size.value() == 0) {
                    countBatchTriggerResults += ReportUtil.getBatchSize(bpResults.executeBatch());
                    countBatchExecutionDates += ReportUtil.getBatchSize(bpExecutionDates.executeBatch());
                }

                DBItemReportTrigger trigger = (DBItemReportTrigger) rspTriggers.get();
                if (trigger == null || trigger.getId() == null) {
                    throw new Exception("trigger or trigger.getId() is NULL");
                }

                DBItemReportExecution firstExecution = null;
                DBItemReportExecution lastExecution = null;
                Long maxStep = new Long(0);

                try {
                    Criteria crExecutions =
                            getDbLayer().getResultUncompletedTriggerExecutionsCriteria(largeResultFetchSizeReporting, trigger.getId());
                    ResultSet rsExecutions = rspExecutions.createResultSet(crExecutions, ScrollMode.FORWARD_ONLY, largeResultFetchSizeReporting);
                    while (rsExecutions.next()) {
                        DBItemReportExecution execution = (DBItemReportExecution) rspExecutions.get();

                        if (execution.getStep().equals(new Long(1))) {
                            firstExecution = execution;
                        }
                        if (execution.getStep() > maxStep) {

                            lastExecution = execution;
                            maxStep = execution.getStep();
                        }
                        DBItemReportExecutionDate exd =
                                insertReportingExecutionDate(EReferenceType.EXECUTION, execution.getSchedulerId(), execution.getHistoryId(),
                                        execution.getId(), execution.getStartTime(), execution.getEndTime());

                        bpExecutionDates.addBatch(exd);
                        countExecutionDates++;
                    }
                } catch (Exception ex) {
                    throw new Exception(SOSHibernateConnection.getException(ex));
                } finally {
                    rspExecutions.close();
                }
                DBItemReportExecutionDate exd =
                        insertReportingExecutionDate(EReferenceType.TRIGGER, trigger.getSchedulerId(), trigger.getHistoryId(), trigger.getId(),
                                trigger.getStartTime(), trigger.getEndTime());

                bpExecutionDates.addBatch(exd);
                countExecutionDates++;

                if (firstExecution == null) {
                    firstExecution = lastExecution;
                }

                String startCause = firstExecution == null ? "unknown" : firstExecution.getCause();
                if (startCause.equals(EStartCauses.ORDER.value())) {
                    String jcStartCause = getDbLayer().getInventoryJobChainStartCause(trigger.getSchedulerId(), trigger.getParentName());
                    if (!SOSString.isEmpty(jcStartCause)) {
                        startCause = jcStartCause;
                    }
                }

                Long steps = lastExecution == null ? new Long(0) : lastExecution.getStep();
                boolean error = lastExecution == null ? false : lastExecution.getError();
                String errorCode = lastExecution == null ? null : lastExecution.getErrorCode();
                String errorText = lastExecution == null ? null : lastExecution.getErrorText();

                DBItemReportTriggerResult rtr =
                        createReportTriggerResults(trigger.getSchedulerId(), trigger.getHistoryId(), trigger.getId(), startCause, steps, error,
                                errorCode, errorText);

                bpResults.addBatch(rtr);

                countTriggerResults++;

                if (countTotal % options.log_info_step.value() == 0) {
                    logger.info(String.format("%s: %s entries processed ...", method, options.log_info_step.value()));
                }
            }

            countBatchTriggerResults += ReportUtil.getBatchSize(bpResults.executeBatch());
            countBatchExecutionDates += ReportUtil.getBatchSize(bpExecutionDates.executeBatch());

            if (counterCreateResult != null) {
                counterCreateResult.setTotalUncompleted(countTotal);
                counterCreateResult.setTriggerResults(countTriggerResults);
                counterCreateResult.setExecutionsDates(countExecutionDates);
                counterCreateResult.setTriggerResultsBatch(countBatchTriggerResults);
                counterCreateResult.setExecutionsDatesBatch(countBatchExecutionDates);
            }

            logger.info(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
        } catch (Exception ex) {
            Throwable e = SOSHibernateConnection.getException(ex);
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        } finally {
            rspTriggers.close();

            bpResults.close();
            bpExecutionDates.close();
        }
    }

    private void initCounters() throws Exception {
        counterCreateResult = new CounterCreateResult();
        counterUpdate = new CounterUpdate();
    }

    private void logSummary(DateTime start) throws Exception {
        String method = "logSummary";

        logger.info(String.format("%s: updated from inventory: triggers  = %s, executions  = %s", method, counterUpdate.getTriggers(),
                counterUpdate.getExecutions()));

        logger.info(String.format("%s: created results (total uncompleted triggers = %s): results = %s of %s, execution dates = %s of %s", method,
                counterCreateResult.getTotalUncompleted(), counterCreateResult.getTriggerResultsBatch(), counterCreateResult.getTriggerResults(),
                counterCreateResult.getExecutionsDatesBatch(), counterCreateResult.getExecutionsDates()));

        logger.info(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
    }

    private DBItemReportExecutionDate createReportExecutionDate(String schedulerId, Long historyId, Long referenceType, Long referenceId,
            Long startDay, Long startWeek, Long startQuarter, Long startMonth, Long startYear, Long endDay, Long endWeek, Long endQuarter,
            Long endMonth, Long endYear) throws Exception {

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

    private DBItemReportTriggerResult createReportTriggerResults(String schedulerId, Long historyId, Long triggerId, String startCause, Long steps,
            boolean error, String errorCode, String errorText) throws Exception {

        logger.debug(String.format(
                "createReportTriggerResults: schedulerId = %s, historyId = %s, triggerId = %s, startCause = %s, steps = %s, error = %s", schedulerId,
                historyId, triggerId, startCause, steps, error));

        DBItemReportTriggerResult item = new DBItemReportTriggerResult();

        item.setSchedulerId(schedulerId);
        item.setHistoryId(historyId);
        item.setTriggerId(triggerId);
        item.setStartCause(startCause);
        item.setSteps(steps);
        item.setError(error);
        item.setErrorCode(errorCode);
        item.setErrorText(errorText);

        item.setCreated(ReportUtil.getCurrentDateTime());
        item.setModified(ReportUtil.getCurrentDateTime());

        return item;
    }

    private void completeReportingResults() throws Exception {
        String method = "completeReportingResults";
        try {
            logger.info(String.format("%s", method));

            getDbLayer().getConnection().beginTransaction();
            getDbLayer().triggerResultCompletedQuery();
            getDbLayer().getConnection().commit();
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

}
