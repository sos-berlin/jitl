package com.sos.jitl.reporting.model.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.hibernate.classes.SOSHibernateBatchProcessor;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.DBItemSchedulerHistoryOrderStepReporting;
import com.sos.jitl.reporting.db.DBItemSchedulerVariableReporting;
import com.sos.jitl.reporting.helper.CounterRemove;
import com.sos.jitl.reporting.helper.CounterSynchronize;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.reporting.job.report.FactJobOptions;
import com.sos.jitl.reporting.model.IReportingModel;
import com.sos.jitl.reporting.model.ReportingModel;
import com.sos.scheduler.history.db.SchedulerTaskHistoryDBItem;

public class FactModel extends ReportingModel implements IReportingModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactModel.class);
    private FactJobOptions options;
    private SOSHibernateConnection schedulerConnection;
    private DBItemSchedulerVariableReporting schedulerVariable;
    private CounterRemove counterOrderRemoved;
    private CounterRemove counterStandaloneRemoved;
    private CounterRemove counterOrderUncompletedRemoved;
    private CounterRemove counterStandaloneUncompletedRemoved;
    private CounterSynchronize counterOrderSyncUncompleted;
    private CounterSynchronize counterOrderSync;
    private CounterSynchronize counterStandaloneSyncUncompleted;
    private CounterSynchronize counterStandaloneSync;
    private int maxHistoryAge;
    private int maxUncompletedAge;
    private Optional<Integer> largeResultFetchSizeReporting = Optional.empty();
    private Optional<Integer> largeResultFetchSizeScheduler = Optional.empty();
    private ArrayList<Long> synchronizedOrderTaskIds;

    public FactModel(SOSHibernateConnection reportingConn, SOSHibernateConnection schedulerConn, FactJobOptions opt) throws Exception {
        super(reportingConn);
        if (schedulerConn == null) {
            throw new Exception("schedulerConn is NULL");
        }
        schedulerConnection = schedulerConn;
        options = opt;
        largeResultFetchSizeReporting = getFetchSize(options.large_result_fetch_size.value());
        largeResultFetchSizeScheduler = getFetchSize(options.large_result_fetch_size_scheduler.value());
        maxHistoryAge = ReportUtil.resolveAge2Minutes(options.max_history_age.getValue());
        maxUncompletedAge = ReportUtil.resolveAge2Minutes(options.max_uncompleted_age.getValue());
    }
    
    @Override
    public void process() throws Exception {
        String method = "process";
        Date dateTo = ReportUtil.getCurrentDateTime();
        Long dateToAsMinutes = dateTo.getTime() / 1000 / 60;
        Date dateFrom = null;
        DateTime start = new DateTime();
        try {
            LOGGER.debug(String.format("%s: batch_size = %s, large_result_fetch_size = %s", method, options.batch_size.value(),
                    options.large_result_fetch_size.getValue()));
            initCounters();
            initSynchronizing();
            
            dateFrom = getDateFrom(dateTo);
            
            removeOrder(options.current_scheduler_id.getValue(), dateFrom, dateTo);
            synchronizeOrderUncompleted(options.current_scheduler_id.getValue(), dateToAsMinutes);
            synchronizeOrder(options.current_scheduler_id.getValue(),dateFrom, dateTo,dateToAsMinutes);
            
            removeStandalone(options.current_scheduler_id.getValue(), dateFrom, dateTo);
            synchronizeStandaloneUncompleted(options.current_scheduler_id.getValue(), dateToAsMinutes);
            synchronizeStandalone(options.current_scheduler_id.getValue(),dateFrom, dateTo,dateToAsMinutes,synchronizedOrderTaskIds);
            
            finishSynchronizing(dateTo);
            logSummary(dateFrom, dateTo, start);
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    private void removeOrder(String schedulerId, Date dateFrom, Date dateTo) throws Exception {
        counterOrderRemoved = getDbLayer().removeOrder(schedulerId, dateFrom, dateTo);
    }

    private void removeStandalone(String schedulerId, Date dateFrom, Date dateTo) throws Exception {
        counterStandaloneRemoved = getDbLayer().removeStandalone(schedulerId, dateFrom, dateTo);
    }
    
    private void removeOrderUncompleted(ArrayList<Long> triggerIds) throws Exception {
        counterOrderUncompletedRemoved = getDbLayer().removeOrderUncompleted(triggerIds);
    }

    private void removeStandaloneUncompleted(ArrayList<Long> executionIds) throws Exception {
        counterStandaloneUncompletedRemoved = getDbLayer().removeStandaloneUncompleted(executionIds);
    }
    
    private void finishSynchronizing(Date dateTo) throws Exception {
        String method = "finishSynchronizing";
        try {
            LOGGER.info(String.format("%s: dateTo = %s", method, ReportUtil.getDateAsString(dateTo)));
            schedulerConnection.beginTransaction();
            schedulerVariable.setNumericValue(new Long(maxHistoryAge));
            schedulerVariable.setTextValue(ReportUtil.getDateAsString(dateTo));
            getDbLayer().updateSchedulerVariable(schedulerConnection, schedulerVariable);
            schedulerConnection.commit();
        } catch (Exception ex) {
            schedulerConnection.rollback();
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    private void initSynchronizing() throws Exception {
        String method = "initSynchronizing";
        try {
            LOGGER.info(String.format("%s", method));
            synchronizedOrderTaskIds = new ArrayList<Long>();
            
            schedulerConnection.beginTransaction();
            schedulerVariable = getDbLayer().getSchedulerVariabe(schedulerConnection);
            if (schedulerVariable == null) {
                schedulerVariable = getDbLayer().createSchedulerVariable(schedulerConnection, new Long(maxHistoryAge), null);
            }
            schedulerConnection.commit();
        } catch (Exception ex) {
            schedulerConnection.rollback();
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    private void synchronizeOrderUncompleted(String schedulerId, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeOrderUncompleted";
        ScrollableResults sr = null;
        try {
            LOGGER.info(String.format("%s", method));
            if (schedulerId != null && !schedulerId.isEmpty()) {
                ArrayList<Long> triggerIds = new ArrayList<Long>();
                ArrayList<Long> orderHistoryIds = new ArrayList<Long>();
                Criteria cr = getDbLayer().getOrderSyncUncomplitedIds(largeResultFetchSizeReporting, schedulerId);
                sr = cr.scroll(ScrollMode.FORWARD_ONLY);
                while (sr.next()) {
                    triggerIds.add((Long) sr.get(0));
                    orderHistoryIds.add((Long) sr.get(1));
                }
                sr.close();
                sr = null;
                if (triggerIds != null && !triggerIds.isEmpty()) {
                	removeOrderUncompleted(triggerIds);
                    cr = getDbLayer().getSchedulerHistoryOrderSteps(schedulerConnection, largeResultFetchSizeScheduler, schedulerId, null, null, orderHistoryIds,null);
                    counterOrderSyncUncompleted = synchronizeOrderHistory(cr, dateToAsMinutes);
                }
            }
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        } finally {
            if (sr != null) {
                try {
                    sr.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    private void synchronizeStandaloneUncompleted(String schedulerId, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeStandaloneUncompleted";
        ScrollableResults sr = null;
        try {
            LOGGER.info(String.format("%s", method));
            if (schedulerId != null && !schedulerId.isEmpty()) {
            	ArrayList<Long> executionIds = new ArrayList<Long>();
            	ArrayList<Long> taskHistoryIds = new ArrayList<Long>();
                Criteria cr = getDbLayer().getStandaloneSyncUncomplitedIds(largeResultFetchSizeReporting, schedulerId);
                sr = cr.scroll(ScrollMode.FORWARD_ONLY);
                while (sr.next()) {
                	Long executionId = (Long) sr.get(0);
                	Long taskHistoryId = (Long) sr.get(1);
                	executionIds.add(executionId);
                	if(!taskHistoryIds.contains(taskHistoryId)){
                		taskHistoryIds.add(taskHistoryId);
                	}
                }
                sr.close();
                sr = null;
                if (executionIds != null && !executionIds.isEmpty()) {
                	removeStandaloneUncompleted(executionIds);
                	cr = getDbLayer().getSchedulerHistoryTasks(schedulerConnection, largeResultFetchSizeScheduler, schedulerId, null, null, null,taskHistoryIds);
                    counterStandaloneSyncUncompleted = synchronizeStandaloneHistory(cr,schedulerId, dateToAsMinutes);
                }
            }
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        } finally {
            if (sr != null) {
                try {
                    sr.close();
                } catch (Exception ex) {
                }
            }
        }
    }
    
    private void synchronizeOrder(String schedulerId, Date dateFrom, Date dateTo,Long dateToAsMinutes) throws Exception {
        String method = "synchronizeOrder";
        try {
            LOGGER.info(String.format("%s: schedulerId = %s, dateFrom = %s, dateTo = %s", method, schedulerId, ReportUtil.getDateAsString(dateFrom),
                    ReportUtil.getDateAsString(dateTo)));
            
            Criteria cr = getDbLayer().getSchedulerHistoryOrderSteps(schedulerConnection, largeResultFetchSizeScheduler, schedulerId, dateFrom, dateTo, null,null);
            counterOrderSync = synchronizeOrderHistory(cr,dateToAsMinutes);
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }
    
    private void synchronizeStandalone(String schedulerId, Date dateFrom, Date dateTo,Long dateToAsMinutes,ArrayList<Long> excludedTaskIds) throws Exception {
        String method = "synchronizeStandalone";
        try {
            LOGGER.info(String.format("%s: schedulerId = %s, dateFrom = %s, dateTo = %s", method, schedulerId, ReportUtil.getDateAsString(dateFrom),
                    ReportUtil.getDateAsString(dateTo)));
                        
            Criteria cr = getDbLayer().getSchedulerHistoryTasks(schedulerConnection, largeResultFetchSizeScheduler, schedulerId, dateFrom, dateTo,excludedTaskIds ,null);
            counterStandaloneSync = synchronizeStandaloneHistory(cr,schedulerId,dateToAsMinutes);
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
     }
    
    private boolean calculateIsSyncCompleted(Date startTime, Date endTime, Long dateToAsMinutes){
    	boolean syncCompleted = false;
    	if(endTime == null){
    		if (maxUncompletedAge > 0) {
                Long startTimeMinutes = startTime.getTime() / 1000 / 60;
                Long diffMinutes = dateToAsMinutes - startTimeMinutes;
                if (diffMinutes > maxUncompletedAge) {
                    syncCompleted = true;
                }
            }
    	}
    	else{
    		syncCompleted = true;
    	}
    	return syncCompleted;
    }

    private CounterSynchronize synchronizeStandaloneHistory(Criteria criteria, String schedulerId, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeStandaloneHistory";
        ScrollableResults sr = null;
        SOSHibernateBatchProcessor bp = new SOSHibernateBatchProcessor(getDbLayer().getConnection());
        CounterSynchronize counter = new CounterSynchronize();
        try {
            LOGGER.debug(String.format("%s", method));
            DateTime start = new DateTime();
            HashMap<Long, Long> insertedTriggers = new HashMap<Long, Long>();
            bp.createInsertBatch(DBItemReportExecution.class);
            int countTotal = 0;
            int countSkip = 0;
            int countTriggers = 0;
            int countExecutions = 0;
            int countBatchExecutions = 0;
            getDbLayer().getConnection().beginTransaction();
            sr = criteria.scroll(ScrollMode.FORWARD_ONLY);
            while (sr.next()) {
                countTotal++;
                SchedulerTaskHistoryDBItem task = (SchedulerTaskHistoryDBItem) sr.get(0);
                try {
                	if(task.getJobName().equals("(Spooler)")){
                		countSkip++;
                		continue;
                	}
                	
                	if (countTotal % options.batch_size.value() == 0) {
                        countBatchExecutions += ReportUtil.getBatchSize(bp.executeBatch());
                    }
                	                	
                	Long triggerId = new Long(0);
                	Long step = new Long(1);
                	String state = null;
                    Date startTime = task.getStartTime();
                	Date endTime = task.getEndTime();
                	boolean isError = task.isError();
                	String errorCode = task.getErrorCode();
                	String errorText = task.getErrorText();
                	boolean syncCompleted = calculateIsSyncCompleted(task.getStartTime(),task.getEndTime(),dateToAsMinutes);
                    
                	String cause = task.getCause() == null ? "standalone" : task.getCause();
                    
                	if(cause.equals("order")){
                	   ArrayList<Long> taskHistoryIds = new ArrayList<Long>();
                	   taskHistoryIds.add(task.getId());
                	   
                	   Criteria criteriaOrderSteps = getDbLayer().getSchedulerHistoryOrderSteps(schedulerConnection, largeResultFetchSizeScheduler, schedulerId, null, null, null,taskHistoryIds);
                	   @SuppressWarnings("unchecked")
                       List<DBItemSchedulerHistoryOrderStepReporting> orderSteps = criteriaOrderSteps.list();
                	   if(orderSteps != null && orderSteps.size() > 0){
                	       DBItemSchedulerHistoryOrderStepReporting orderStep = orderSteps.get(0);
                	       
                	       if (insertedTriggers.containsKey(orderStep.getOrderHistoryId())) {
                               triggerId = insertedTriggers.get(orderStep.getOrderHistoryId());
                           } else {
                               DBItemReportTrigger rt = getDbLayer().getTrigger(orderStep.getOrderSchedulerId(),orderStep.getOrderHistoryId());
                               if(rt == null){
                                   boolean triggerSyncCompleted = calculateIsSyncCompleted(orderStep.getOrderStartTime(),orderStep.getOrderEndTime(),dateToAsMinutes);
                                   rt = getDbLayer().createReportTrigger(orderStep.getOrderSchedulerId(), orderStep.getOrderHistoryId(), orderStep.getOrderId(), orderStep.getOrderTitle(),
                                               ReportUtil.getFolderFromName(orderStep.getOrderJobChain()), orderStep.getOrderJobChain(), ReportUtil.getBasenameFromName(orderStep.getOrderJobChain()), null, orderStep.getOrderState(),
                                               orderStep.getOrderStateText(), orderStep.getOrderStartTime(), orderStep.getOrderEndTime(), triggerSyncCompleted);
                                   countTriggers++;
                               }
                               triggerId = rt.getId();
                               insertedTriggers.put(orderStep.getOrderHistoryId(), triggerId);
                           }
                	       step = orderStep.getStepStep();
                	       startTime = orderStep.getStepStartTime();
                	       endTime = orderStep.getStepEndTime();
                	       state = orderStep.getStepState();
                	       isError = orderStep.isStepError();
                           errorCode = orderStep.getStepErrorCode();
                           errorText = orderStep.getStepErrorText();
                           syncCompleted = endTime != null;
                	   }
                	}
                	
                    DBItemReportExecution re =
                           getDbLayer().createReportExecution(task.getSchedulerId(), task.getId(),triggerId,task.getClusterMemberId(),task.getSteps(), step,
                                    ReportUtil.getFolderFromName(task.getJobName()), task.getJobName(), ReportUtil.getBasenameFromName(task.getJobName()), null, startTime,
                                    endTime, state, cause,task.getExitCode(), isError, errorCode,
                                    errorText, task.getAgentUrl(),syncCompleted);
                    bp.addBatch(re);
                    countExecutions++;
                } catch (Exception e) {
                    throw new Exception(SOSHibernateConnection.getException(e));
                }
                if (countTotal % options.log_info_step.value() == 0) {
                    LOGGER.info(String.format("%s: %s history steps processed ...", method, options.log_info_step.value()));
                }
            }
            countBatchExecutions += ReportUtil.getBatchSize(bp.executeBatch());
            getDbLayer().getConnection().commit();
            LOGGER.info(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
            
            counter.setTotal(countTotal);
            counter.setSkip(countSkip);
            counter.setTriggers(countTriggers);
            counter.setExecutions(countExecutions);
            counter.setExecutionsBatch(countBatchExecutions);
            LOGGER.info(String.format("%s: total history steps = %s, triggers = %s, executions = %s of %s, skip = %s ", method,
            		counter.getTotal(), counter.getTriggers(), counter.getExecutionsBatch(),
            		counter.getExecutions(), counter.getSkip()));
            
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            Throwable e = SOSHibernateConnection.getException(ex);
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        } finally {
            bp.close();
            try {
                if (sr != null) {
                    sr.close();
                }
            } catch (Exception ex) {
            }
        }
        return counter;
    }

    private CounterSynchronize synchronizeOrderHistory(Criteria criteria, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeOrderHistory";
        ScrollableResults sr = null;
        SOSHibernateBatchProcessor bp = new SOSHibernateBatchProcessor(getDbLayer().getConnection());
        CounterSynchronize counter = new CounterSynchronize();
        try {
            LOGGER.debug(String.format("%s", method));
            DateTime start = new DateTime();
            bp.createInsertBatch(DBItemReportExecution.class);
            HashMap<Long, Long> inserted = new HashMap<Long, Long>();
            int countTotal = 0;
            int countSkip = 0;
            int countTriggers = 0;
            int countExecutions = 0;
            int countBatchExecutions = 0;
            getDbLayer().getConnection().beginTransaction();
            sr = criteria.scroll(ScrollMode.FORWARD_ONLY);
            while (sr.next()) {
                countTotal++;
                DBItemSchedulerHistoryOrderStepReporting step = (DBItemSchedulerHistoryOrderStepReporting) sr.get(0);
                if (step.getOrderHistoryId() == null && step.getOrderId() == null && step.getOrderStartTime() == null) {
                    countSkip++;
                    LOGGER.debug(String.format("%s: %s) order object is null. step = %s, historyId = %s ", method, countTotal, step.getStepState(),
                            step.getStepHistoryId()));
                    continue;
                }
                if (step.getTaskId() == null && step.getTaskJobName() == null && step.getTaskCause() == null) {
                    countSkip++;
                    LOGGER.debug(String.format("%s: %s) task object is null. jobChain = %s, order = %s, step = %s, taskId = %s ", method, countTotal,
                            step.getOrderJobChain(), step.getOrderId(), step.getStepState(), step.getStepTaskId()));
                    continue;
                }

                if(!synchronizedOrderTaskIds.contains(step.getTaskId())){
                	synchronizedOrderTaskIds.add(step.getTaskId());
                }
                
                LOGGER.debug(String.format("%s: %s) schedulerId = %s, orderHistoryId = %s, jobChain = %s, order id = %s, step = %s, step state = %s",
                        method, countTotal, step.getOrderSchedulerId(), step.getOrderHistoryId(), step.getOrderJobChain(), step.getOrderId(),
                        step.getStepStep(), step.getStepState()));
                Long triggerId = new Long(0);
                try {
                    if (countTotal % options.batch_size.value() == 0) {
                        countBatchExecutions += ReportUtil.getBatchSize(bp.executeBatch());
                    }
                    if (inserted.containsKey(step.getOrderHistoryId())) {
                        triggerId = inserted.get(step.getOrderHistoryId());
                    } else {
                    	boolean syncCompleted = calculateIsSyncCompleted(step.getOrderStartTime(),step.getOrderEndTime(),dateToAsMinutes);
                        DBItemReportTrigger rt =
                                getDbLayer().createReportTrigger(step.getOrderSchedulerId(), step.getOrderHistoryId(), step.getOrderId(), step.getOrderTitle(),
                                        ReportUtil.getFolderFromName(step.getOrderJobChain()), step.getOrderJobChain(), ReportUtil.getBasenameFromName(step.getOrderJobChain()), null, step.getOrderState(),
                                        step.getOrderStateText(), step.getOrderStartTime(), step.getOrderEndTime(), syncCompleted);
                        countTriggers++;
                        triggerId = rt.getId();
                        inserted.put(step.getOrderHistoryId(), triggerId);
                    }
                    DBItemReportExecution re =
                            getDbLayer().createReportExecution(step.getOrderSchedulerId(), step.getTaskId(), triggerId, step.getTaskClusterMemberId(), step.getTaskSteps(), step.getStepStep(),
                                    ReportUtil.getFolderFromName(step.getTaskJobName()), step.getTaskJobName(), ReportUtil.getBasenameFromName(step.getTaskJobName()), null, step.getStepStartTime(),
                                    step.getStepEndTime(), step.getStepState(), step.getTaskCause(),step.getTaskExitCode(), step.isStepError(), step.getStepErrorCode(),
                                    step.getStepErrorText(), step.getAgentUrl(),step.getStepEndTime()!=null);
                    bp.addBatch(re);
                    countExecutions++;
                } catch (Exception e) {
                    throw new Exception(SOSHibernateConnection.getException(e));
                }
                if (countTotal % options.log_info_step.value() == 0) {
                    LOGGER.info(String.format("%s: %s history steps processed ...", method, options.log_info_step.value()));
                }
            }
            countBatchExecutions += ReportUtil.getBatchSize(bp.executeBatch());
            getDbLayer().getConnection().commit();
            LOGGER.info(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
           	
            counter.setTotal(countTotal);
            counter.setSkip(countSkip);
            counter.setTriggers(countTriggers);
            counter.setExecutions(countExecutions);
            counter.setExecutionsBatch(countBatchExecutions);
            
            LOGGER.debug(String.format("%s: total = %s, triggers = %s, executions = %s of %s, skip = %s ", method,
                		counter.getTotal(), counter.getTriggers(), counter.getExecutionsBatch(),
                		counter.getExecutions(), counter.getSkip()));
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            Throwable e = SOSHibernateConnection.getException(ex);
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        } finally {
            bp.close();
            try {
                if (sr != null) {
                    sr.close();
                }
            } catch (Exception ex) {
            }
        }
        return counter;
    }
    
    private void initCounters() throws Exception {
    	counterOrderRemoved = new CounterRemove();
    	counterOrderUncompletedRemoved = new CounterRemove();
        counterOrderSync = new CounterSynchronize();
    	counterOrderSyncUncompleted = new CounterSynchronize();
    
    	counterStandaloneRemoved = new CounterRemove();
        counterStandaloneUncompletedRemoved = new CounterRemove();
    	counterStandaloneSync = new CounterSynchronize();
    	counterStandaloneSyncUncompleted = new CounterSynchronize();
    }

    private void logSummary(Date dateFrom, Date dateTo, DateTime start) throws Exception {
        String method = "logSummary";
        String from = ReportUtil.getDateAsString(dateFrom);
        String to = ReportUtil.getDateAsString(dateTo);
        
        //Order
        String range = "order";
        LOGGER.info(String.format("%s[%s]: removed (%s to %s): triggers = %s, executions = %s", method,range, from, to, counterOrderRemoved.getTriggers(),
                counterOrderRemoved.getExecutions()));
        LOGGER.info(String.format("%s[%s]: removed results: results = %s, trigger dates = %s, execution dates = %s", method,range, counterOrderRemoved.getTriggerResults(),
        		counterOrderRemoved.getTriggerDates(),counterOrderRemoved.getExecutionDates()));
        LOGGER.info(String.format("%s[%s]: synchronized old uncompleted: total = %s, triggers = %s, executions = %s of %s, skip = %s ",
                method,range, counterOrderSyncUncompleted.getTotal(), counterOrderSyncUncompleted.getTriggers(), counterOrderSyncUncompleted.getExecutionsBatch(),
                counterOrderSyncUncompleted.getExecutions(), counterOrderSyncUncompleted.getSkip()));
        LOGGER.info(String.format(
                "%s[%s]: synchronized new (%s to %s): total history steps = %s, triggers = %s, executions = %s of %s, skip = %s ", method,range, from,
                to, counterOrderSync.getTotal(), counterOrderSync.getTriggers(), counterOrderSync.getExecutionsBatch(),
                counterOrderSync.getExecutions(), counterOrderSync.getSkip()));
        
        //Standalone
        range = "standalone";
        LOGGER.info(String.format("%s[%s]: removed (%s to %s): executions = %s, execution dates = %s", method,range, from, to, 
        		counterStandaloneRemoved.getExecutions(),counterStandaloneRemoved.getExecutionDates()));
        LOGGER.info(String.format("%s[%s]: removed old uncompleted: executions = %s, execution dates = %s", method,range, 
        		counterStandaloneUncompletedRemoved.getExecutions(),counterStandaloneUncompletedRemoved.getExecutionDates()));
        LOGGER.info(String.format("%s[%s]: synchronized old uncompleted: total = %s, executions = %s of %s, skip = %s ",
                method,range, counterStandaloneSyncUncompleted.getTotal(), counterStandaloneSyncUncompleted.getExecutionsBatch(),
                counterStandaloneSyncUncompleted.getExecutions(), counterStandaloneSyncUncompleted.getSkip()));
        LOGGER.info(String.format(
                "%s[%s]: synchronized new (%s to %s): total history tasks = %s, executions = %s of %s, skip = %s ", method,range, from,
                to, counterStandaloneSync.getTotal(), counterStandaloneSync.getExecutionsBatch(),
                counterStandaloneSync.getExecutions(), counterStandaloneSync.getSkip()));
        
        
        LOGGER.info(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
    }

    private Date getDateFrom(Date dateTo) throws Exception {
        String method = "getDateFrom";
        Long currentMaxAge = new Long(maxHistoryAge);
        Long storedMaxAge = (schedulerVariable.getNumericValue() == null) ? new Long(0) : schedulerVariable.getNumericValue();
        Date dateFrom = SOSString.isEmpty(schedulerVariable.getTextValue()) ? null : ReportUtil.getDateFromString(schedulerVariable.getTextValue());
        LOGGER.debug(String.format("%s: currentMaxAge = %s, storedMaxAge = %s, dateFrom = %s", method, currentMaxAge, storedMaxAge,
                ReportUtil.getDateAsString(dateFrom)));
        if (dateFrom != null) {
            if (options.force_max_history_age.value()) {
                dateFrom = null;
            } else {
                if (currentMaxAge > 0) {
                    Long startTimeMinutes = dateFrom.getTime() / 1000 / 60;
                    Long endTimeMinutes = dateTo.getTime() / 1000 / 60;
                    Long diffMinutes = endTimeMinutes - startTimeMinutes;
                    if (diffMinutes > currentMaxAge) {
                        dateFrom = null;
                    }
                    LOGGER.debug(String.format("%s: diffMinutes = %s, currentMaxAge = %s, dateFrom = %s", method, diffMinutes, currentMaxAge,
                            ReportUtil.getDateAsString(dateFrom)));
                }
            }
        }
        if (dateFrom == null && currentMaxAge > 0) {
            dateFrom = ReportUtil.getDateTimeMinusMinutes(dateTo, currentMaxAge);
            LOGGER.debug(String.format("%s: dateTo = %s - currentMaxAge = %s, dateFrom = %s", method, ReportUtil.getDateAsString(dateTo),
                    currentMaxAge, ReportUtil.getDateAsString(dateFrom)));
        }
        LOGGER.info(String.format("%s: dateFrom = %s (storedDateFrom = %s, max_history_age = %s (%s minutes), storedMaxAge = %s minutes)", method,
                ReportUtil.getDateAsString(dateFrom), schedulerVariable.getTextValue(), options.max_history_age.getValue(), currentMaxAge, storedMaxAge));
        return dateFrom;
    }

    public CounterSynchronize getCounterOrderSync() {
        return counterOrderSync;
    }

    public CounterSynchronize getCounterOrderSyncUncompleted() {
        return counterOrderSyncUncompleted;
    }

    public CounterSynchronize getCounterStandaloneSync() {
        return counterStandaloneSync;
    }

    public CounterSynchronize getCounterStandaloneSyncUncompleted() {
        return counterStandaloneSyncUncompleted;
    }
}