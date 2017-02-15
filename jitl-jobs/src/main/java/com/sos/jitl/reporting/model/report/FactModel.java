package com.sos.jitl.reporting.model.report;

import java.nio.file.Path;
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

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateStatelessConnection;
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.jitl.notification.helper.NotificationReportExecution;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportInventoryInfo;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.DBItemReportTriggerResult;
import com.sos.jitl.reporting.db.DBItemSchedulerHistory;
import com.sos.jitl.reporting.db.DBItemSchedulerHistoryOrderStepReporting;
import com.sos.jitl.reporting.db.DBItemSchedulerOrderStepHistory;
import com.sos.jitl.reporting.db.DBItemSchedulerVariableReporting;
import com.sos.jitl.reporting.helper.CounterRemove;
import com.sos.jitl.reporting.helper.CounterSynchronize;
import com.sos.jitl.reporting.helper.EStartCauses;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.reporting.job.report.FactJobOptions;
import com.sos.jitl.reporting.model.IReportingModel;
import com.sos.jitl.reporting.model.ReportingModel;
import com.sos.jitl.reporting.plugin.FactNotificationPlugin;

import sos.util.SOSString;

public class FactModel extends ReportingModel implements IReportingModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactModel.class);
    private FactJobOptions options;
    private SOSHibernateStatelessConnection schedulerConnection;
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
    private Date dateFrom = null;
    private FactNotificationPlugin notificationPlugin;
       
    public FactModel(FactJobOptions opt) throws Exception{
    	options = opt;
        
    	largeResultFetchSizeReporting = getFetchSize(options.large_result_fetch_size.value());
        largeResultFetchSizeScheduler = getFetchSize(options.large_result_fetch_size_scheduler.value());
        maxHistoryAge = ReportUtil.resolveAge2Minutes(options.max_history_age.getValue());
        maxUncompletedAge = ReportUtil.resolveAge2Minutes(options.max_uncompleted_age.getValue());
        
        registerPlugin();
    }
    
    public void init(PluginMailer mailer, Path configDirectory){
    	pluginOnInit(mailer,configDirectory, this.options.hibernate_configuration_file.getValue());
    }
    
    public void exit(){
    	pluginOnExit();
    }
    
    public void setConnections(SOSHibernateStatelessConnection reportingConn, SOSHibernateStatelessConnection schedulerConn) throws Exception {
        setReportingConnection(reportingConn);
        schedulerConnection = schedulerConn;
    }
    
    @Override
    public void process() throws Exception {
        String method = "process";
        Date dateTo = ReportUtil.getCurrentDateTime();
        Long dateToAsMinutes = dateTo.getTime() / 1000 / 60;
        DateTime start = new DateTime();
        try {
            LOGGER.debug(String.format("%s: large_result_fetch_size = %s", method, options.large_result_fetch_size.getValue()));
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
            LOGGER.debug(String.format("%s: dateTo = %s", method, ReportUtil.getDateAsString(dateTo)));
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
            LOGGER.debug(String.format("%s", method));
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
            LOGGER.debug(String.format("%s", method));
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
            LOGGER.debug(String.format("%s", method));
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
            LOGGER.debug(String.format("%s: schedulerId = %s, dateFrom = %s, dateTo = %s", method, schedulerId, ReportUtil.getDateAsString(dateFrom),
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
            LOGGER.debug(String.format("%s: schedulerId = %s, dateFrom = %s, dateTo = %s", method, schedulerId, ReportUtil.getDateAsString(dateFrom),
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
        CounterSynchronize counter = new CounterSynchronize();
        try {
            LOGGER.debug(String.format("%s", method));
            DateTime start = new DateTime();
            HashMap<Long, Long> insertedTriggers = new HashMap<Long, Long>();
            HashMap<Long, DBItemReportTrigger> insertedTriggerObjects = new HashMap<Long, DBItemReportTrigger>();
            int countTotal = 0;
            int countSkip = 0;
            int countTriggers = 0;
            int countExecutions = 0;
            getDbLayer().getConnection().beginTransaction();
            List<DBItemSchedulerHistory> result = criteria.list();
            for(int i=0;i<result.size();i++) {
                countTotal++;
                DBItemSchedulerHistory task = result.get(i);
                try {
                	if(task.getJobName().equals("(Spooler)")){
                		LOGGER.debug(String.format("%s: %s) skip jobName = %s",
                                method, countTotal, task.getJobName()));
                		countSkip++;
                		continue;
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
                               
                               LOGGER.debug(String.format("%s: %s) use triggerId=%s", method, countTotal, triggerId));
                           } else {
                               DBItemReportTrigger rt = getDbLayer().getTrigger(orderStep.getOrderSchedulerId(),orderStep.getOrderHistoryId());
                               if(rt == null){
                                   boolean triggerSyncCompleted = calculateIsSyncCompleted(orderStep.getOrderStartTime(),orderStep.getOrderEndTime(),dateToAsMinutes);
                                   DBItemReportInventoryInfo tii = getInventoryInfo(getDbLayer().getInventoryInfoForTrigger(largeResultFetchSizeReporting, orderStep.getOrderSchedulerId(),options.current_scheduler_hostname.getValue(),options.current_scheduler_http_port.value(),orderStep.getOrderId(),orderStep.getOrderJobChain()));
                                   LOGGER.debug(String.format("%s: %s) getInventoryInfoForTrigger(orderId=%s, jobChain=%s): tii.getTitle=%s, tii.getIsRuntimeDefined=%s",method, countTotal, orderStep.getOrderId(),orderStep.getOrderJobChain(),tii.getTitle(), tii.getIsRuntimeDefined()));
                                   
                                   rt = getDbLayer().createReportTrigger(orderStep.getOrderSchedulerId(), orderStep.getOrderHistoryId(), orderStep.getOrderId(), orderStep.getOrderTitle(),
                                               ReportUtil.getFolderFromName(orderStep.getOrderJobChain()), orderStep.getOrderJobChain(), ReportUtil.getBasenameFromName(orderStep.getOrderJobChain()), tii.getTitle(), orderStep.getOrderState(),
                                               orderStep.getOrderStateText(), orderStep.getOrderStartTime(), orderStep.getOrderEndTime(), triggerSyncCompleted,tii.getIsRuntimeDefined());
                                   countTriggers++;
                                   
                                   createReportTriggerResult(rt,orderStep.getTaskCause());
                                   
                                   LOGGER.debug(String.format("%s: %s) trigger (%s) inserted for taskId = %s",
                                           method, countTotal, rt.getId(),task.getId()));
                                   
                                   if(this.notificationPlugin != null){
                                   		insertedTriggerObjects.put(triggerId,rt);
                                   }
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
                           
                           LOGGER.debug(String.format("%s: %s) schedulerId = %s, orderHistoryId = %s, jobChain = %s, order id = %s, step = %s, step state = %s",
                                   method, countTotal, orderStep.getOrderSchedulerId(), orderStep.getOrderHistoryId(), orderStep.getOrderJobChain(), orderStep.getOrderId(),
                                   orderStep.getStepStep(), orderStep.getStepState()));
                	   }
                	}
                	
                	DBItemReportExecution re = getDbLayer().getExecution(schedulerId, task.getId(), triggerId, step);
                	if(re == null){
                		LOGGER.debug(String.format("%s: %s) insert: schedulerId = %s, taskHistoryId = %s, triggerId = %s, step = %s, jobName = %s, cause = %s, syncCompleted = %s",
                                method, countTotal, task.getSpoolerId(), task.getId(), triggerId, step, task.getJobName(), cause, syncCompleted));
                   	
                		DBItemReportInventoryInfo eii = getInventoryInfo(getDbLayer().getInventoryInfoForExecution(largeResultFetchSizeReporting,task.getSpoolerId(),options.current_scheduler_hostname.getValue(), options.current_scheduler_http_port.value(),task.getJobName(),false));
                		LOGGER.debug(String.format("%s: %s) getInventoryInfoForExecution(jobName=%s): eii.getTitle=%s, eii.getIsRuntimeDefined=%s",method, countTotal, task.getJobName(),eii.getTitle(), eii.getIsRuntimeDefined()));
                        
                		re = getDbLayer().createReportExecution(task.getSpoolerId(), task.getId(),triggerId,task.getClusterMemberId(),task.getSteps(), step,
                                    ReportUtil.getFolderFromName(task.getJobName()), task.getJobName(), ReportUtil.getBasenameFromName(task.getJobName()), eii.getTitle(), startTime,
                                    endTime, state, cause,task.getExitCode(), isError, errorCode,
                                    errorText, task.getAgentUrl(),syncCompleted,eii.getIsRuntimeDefined());
                		
                		getDbLayer().getConnection().save(re);
                		countExecutions++;
                		
                		re.setTaskStartTime(task.getStartTime());
                        re.setTaskEndTime(task.getEndTime());
                        
                        if(re.getTriggerId() > new Long(0)){
                        	pluginOnProcess(insertedTriggerObjects, re);
                        }
                        
                	}
                	else{
                		countSkip++;
                		LOGGER.debug(String.format("%s: %s) skip (already exist): schedulerId = %s, taskHistoryId = %s, triggerId = %s, step = %s, jobName = %s, cause = %s, syncCompleted = %s",
                                method, countTotal, task.getSpoolerId(), task.getId(), triggerId, step, task.getJobName(), cause, syncCompleted));
                	}
                } catch (Exception e) {
                    throw new Exception(SOSHibernateConnection.getException(e));
                }
                if (countTotal % options.log_info_step.value() == 0) {
                    LOGGER.info(String.format("%s: %s history steps processed ...", method, options.log_info_step.value()));
                }
            }
            getDbLayer().getConnection().commit();
            LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
            
            counter.setTotal(countTotal);
            counter.setSkip(countSkip);
            counter.setTriggers(countTriggers);
            counter.setExecutions(countExecutions);
            LOGGER.debug(String.format("%s: total history steps = %s, triggers = %s, executions = %s, skip = %s ", method,
            		counter.getTotal(), counter.getTriggers(),counter.getExecutions(), counter.getSkip()));
            
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            Throwable e = SOSHibernateConnection.getException(ex);
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        } 
        return counter;
    }

    private CounterSynchronize synchronizeOrderHistory(Criteria criteria, Long dateToAsMinutes) throws Exception {
        String method = "synchronizeOrderHistory";
        CounterSynchronize counter = new CounterSynchronize();
        try {
            LOGGER.debug(String.format("%s", method));
            DateTime start = new DateTime();
            HashMap<Long, Long> inserted = new HashMap<Long, Long>();
            HashMap<Long, DBItemReportTrigger> insertedTriggerObjects = new HashMap<Long, DBItemReportTrigger>();
            int countTotal = 0;
            int countSkip = 0;
            int countTriggers = 0;
            int countExecutions = 0;
            getDbLayer().getConnection().beginTransaction();
            List<DBItemSchedulerHistoryOrderStepReporting> result = criteria.list();
            for(int i=0;i<result.size();i++) {
                countTotal++;
                DBItemSchedulerHistoryOrderStepReporting step = result.get(i);
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
                    if (inserted.containsKey(step.getOrderHistoryId())) {
                        triggerId = inserted.get(step.getOrderHistoryId());
                        
                        LOGGER.debug(String.format("%s: %s) use triggerId=%s", method, countTotal, triggerId));
                        
                    } else {
                        DBItemReportInventoryInfo ii = getInventoryInfo(getDbLayer().getInventoryInfoForTrigger(largeResultFetchSizeReporting, step.getOrderSchedulerId(), options.current_scheduler_hostname.getValue(),options.current_scheduler_http_port.value(), step.getOrderId(),step.getOrderJobChain()));
                        LOGGER.debug(String.format("%s: %s) getInventoryInfoForTrigger(orderId=%s, jobChain=%s): ii.getTitle=%s, ii.getIsRuntimeDefined=%s",method, countTotal, step.getOrderId(),step.getOrderJobChain(), ii.getTitle(), ii.getIsRuntimeDefined()));
                        
                        boolean syncCompleted = calculateIsSyncCompleted(step.getOrderStartTime(),step.getOrderEndTime(),dateToAsMinutes);
                        DBItemReportTrigger rt =
                                getDbLayer().createReportTrigger(step.getOrderSchedulerId(), step.getOrderHistoryId(), step.getOrderId(), step.getOrderTitle(),
                                        ReportUtil.getFolderFromName(step.getOrderJobChain()), step.getOrderJobChain(), ReportUtil.getBasenameFromName(step.getOrderJobChain()), ii.getTitle(), step.getOrderState(),
                                        step.getOrderStateText(), step.getOrderStartTime(), step.getOrderEndTime(), syncCompleted,ii.getIsRuntimeDefined());
                        countTriggers++;
                        triggerId = rt.getId();
                        inserted.put(step.getOrderHistoryId(), triggerId);
                        LOGGER.debug(String.format("%s: %s) trigger created rt.getId = %s", method, countTotal, rt.getId()));
                        
                        createReportTriggerResult(rt,step.getTaskCause());
                        
                        if(this.notificationPlugin != null){
                        	insertedTriggerObjects.put(triggerId,rt);
                        }
                    }
                    
                    DBItemReportInventoryInfo eii = getInventoryInfo(getDbLayer().getInventoryInfoForExecution(largeResultFetchSizeReporting,step.getOrderSchedulerId(),options.current_scheduler_hostname.getValue(),options.current_scheduler_http_port.value(),step.getTaskJobName(),false));
                    LOGGER.debug(String.format("%s: %s) getInventoryInfoForExecution(jobName=%s): eii.getTitle=%s, eii.getIsRuntimeDefined=%s",method, countTotal, step.getTaskJobName(), eii.getTitle(), eii.getIsRuntimeDefined()));
                    
                    DBItemReportExecution re =
                            getDbLayer().createReportExecution(step.getOrderSchedulerId(), step.getTaskId(), triggerId, step.getTaskClusterMemberId(), step.getTaskSteps(), step.getStepStep(),
                                    ReportUtil.getFolderFromName(step.getTaskJobName()), step.getTaskJobName(), ReportUtil.getBasenameFromName(step.getTaskJobName()), eii.getTitle(), step.getStepStartTime(),
                                    step.getStepEndTime(), step.getStepState(), step.getTaskCause(),step.getTaskExitCode(), step.isStepError(), step.getStepErrorCode(),
                                    step.getStepErrorText(), step.getAgentUrl(),step.getStepEndTime()!=null,eii.getIsRuntimeDefined());
                    
                    LOGGER.debug(String.format("%s: %s) save execution for triggerId = %s", method, countTotal, triggerId));
                    
                    getDbLayer().getConnection().save(re);
                    
                    re.setTaskStartTime(step.getTaskStartTime());
                    re.setTaskEndTime(step.getTaskEndTime());
                    countExecutions++;
                    
                    pluginOnProcess(insertedTriggerObjects, re);
                    
                } catch (Exception e) {
                    throw new Exception(SOSHibernateConnection.getException(e));
                }
                if (countTotal % options.log_info_step.value() == 0) {
                    LOGGER.info(String.format("%s: %s history steps processed ...", method, options.log_info_step.value()));
                }
            }
            getDbLayer().getConnection().commit();
            LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
           	
            counter.setTotal(countTotal);
            counter.setSkip(countSkip);
            counter.setTriggers(countTriggers);
            counter.setExecutions(countExecutions);
            
            LOGGER.debug(String.format("%s: total = %s, triggers = %s, executions = %s, skip = %s ", method,
                		counter.getTotal(), counter.getTriggers(), counter.getExecutions(), counter.getSkip()));
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            Throwable e = SOSHibernateConnection.getException(ex);
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        } 
        return counter;
    }
    
    private void createReportTriggerResult(DBItemReportTrigger rt, String startCause) throws Exception{
    	String method = "createReportTriggerResult";
    	if (startCause.equals(EStartCauses.ORDER.value())) {
            String jcStartCause = getDbLayer().getInventoryJobChainStartCause(rt.getSchedulerId(), this.options.current_scheduler_hostname.getValue(), this.options.current_scheduler_http_port.value(),rt.getParentName());
            if (!SOSString.isEmpty(jcStartCause)) {
                startCause = jcStartCause;
            }
         }
         LOGGER.debug(String.format("%s: rt.getHistoryId=%s, startCause=%s",method,rt.getHistoryId(), startCause));
    	 DBItemSchedulerOrderStepHistory lastStep = getDbLayer().getSchedulerOrderHistoryLastStep(schedulerConnection, rt.getHistoryId());
         if(lastStep != null){
        	if(lastStep.getId() == null){
        		throw new Exception(String.format("%s: lastStep.id for historyId=%s is null",method,rt.getHistoryId()));
        	}
        	        	
        	LOGGER.debug(String.format("%s: schedulerId=%s, historyId=%s, triggerId=%s, startCause=%s, lastStep.id.step=%s, lastStep.isError=%s,lastStep.errorCode=%s, lastStep.errorText=%s",method,rt.getSchedulerId(), rt.getHistoryId(), rt.getId(), startCause, lastStep.getId().getStep(), lastStep.isError(),lastStep.getErrorCode(), lastStep.getErrorText()));
       	 	
        	DBItemReportTriggerResult rtr = getDbLayer().createReportTriggerResults(rt.getSchedulerId(), rt.getHistoryId(), rt.getId(), startCause, lastStep.getId().getStep(), lastStep.isError(),
         			lastStep.getErrorCode(), lastStep.getErrorText());
         	getDbLayer().getConnection().save(rtr);
         	
         	LOGGER.debug(String.format("%s: DBItemReportTriggerResult created for rt.getId()=%s",method,rt.getId()));
         }
         else{
        	LOGGER.debug(String.format("%s: not create DBItemReportTriggerResult. last step not found for rt.getId()=%s",method,rt.getId()));
         }
    }
    
    private DBItemReportInventoryInfo getInventoryInfo(List<Object[]> infos){
    	DBItemReportInventoryInfo item = new DBItemReportInventoryInfo();
        item.setTitle(null);
        item.setIsRuntimeDefined(false);
        if(infos != null && infos.size()>0){
            try{
                Object[] row = infos.get(0);
                item.setTitle((String)row[0]);
                item.setIsRuntimeDefined((row[1]+"").equals("1"));
            }
            catch(Exception ex){
                LOGGER.warn(String.format("can't create DBItemReportInventoryInfo object: %s",ex.toString()));
            }
        }
        return item;
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
        LOGGER.debug(String.format("[%s to %s][%s][removed]triggers=%s, executions=%s", from, to, range, counterOrderRemoved.getTriggers(),
                counterOrderRemoved.getExecutions()));
        LOGGER.debug(String.format("[%s to %s][%s][removed results]results=%s, trigger dates=%s, execution dates=%s", from, to,range, counterOrderRemoved.getTriggerResults(),
        		counterOrderRemoved.getTriggerDates(),counterOrderRemoved.getExecutionDates()));
        LOGGER.info(String.format(
                "[%s to %s][%s][new]history steps=%s, triggers=%s, executions=%s, skip=%s [old]total=%s, triggers=%s, executions=%s, skip=%s", from,
                to,range,counterOrderSync.getTotal(), counterOrderSync.getTriggers(),
                counterOrderSync.getExecutions(), counterOrderSync.getSkip(),counterOrderSyncUncompleted.getTotal(),counterOrderSyncUncompleted.getTriggers(),
                counterOrderSyncUncompleted.getExecutions(), counterOrderSyncUncompleted.getSkip()));
        
        //Standalone
        range = "standalone";
        LOGGER.debug(String.format("[%s to %s][%s][removed]executions=%s, execution dates=%s", from, to, range, 
        		counterStandaloneRemoved.getExecutions(),counterStandaloneRemoved.getExecutionDates()));
        LOGGER.debug(String.format("[%s to %s][%s][removed old uncompleted]executions=%s, execution dates=%s", from, to, range, 
        		counterStandaloneUncompletedRemoved.getExecutions(),counterStandaloneUncompletedRemoved.getExecutionDates()));
        LOGGER.info(String.format(
                "[%s to %s][%s][new]history tasks=%s, executions=%s, triggers=%s, skip=%s [old]total=%s, executions=%s, triggers=%s, skip=%s", from,to,
                range, counterStandaloneSync.getTotal(),
                counterStandaloneSync.getExecutions(), counterStandaloneSync.getTriggers(), counterStandaloneSync.getSkip(),
                counterStandaloneSyncUncompleted.getTotal(), 
                counterStandaloneSyncUncompleted.getExecutions(), counterStandaloneSyncUncompleted.getTriggers(), counterStandaloneSyncUncompleted.getSkip()));
        
        LOGGER.debug(String.format("%s: duration = %s", method, ReportUtil.getDuration(start, new DateTime())));
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
        LOGGER.debug(String.format("%s: dateFrom = %s (storedDateFrom = %s, max_history_age = %s (%s minutes), storedMaxAge = %s minutes)", method,
                ReportUtil.getDateAsString(dateFrom), schedulerVariable.getTextValue(), options.max_history_age.getValue(), currentMaxAge, storedMaxAge));
        return dateFrom;
    }
	
    private void registerPlugin(){
    	if(this.options.execute_notification_plugin.value()){
    		this.notificationPlugin = new FactNotificationPlugin();
    	}
    }
    
    private void pluginOnInit(PluginMailer mailer,Path configDirectory,String hibernateFile){
    	if(this.notificationPlugin != null){
    		this.notificationPlugin.init(mailer,configDirectory,hibernateFile);
    		if(this.notificationPlugin.getHasErrorOnInit()){
    			this.notificationPlugin.exit();
    			this.notificationPlugin = null;
    		}
    	}
    }
    
    private void pluginOnProcess(HashMap<Long, DBItemReportTrigger> insertedTriggers, DBItemReportExecution re){
    	if(this.notificationPlugin == null || insertedTriggers == null){
    		return;
    	}
    	
    	if(re.getTriggerId().equals(new Long(0))){
    		return;
    	}
    	if(insertedTriggers.containsKey(re.getTriggerId())){
    		LOGGER.debug(String.format("pluginOnProcess: triggerId=%s",re.getTriggerId()));
        	
    		DBItemReportTrigger rt = insertedTriggers.get(re.getTriggerId());
    		NotificationReportExecution item = this.notificationPlugin.convert(rt,re);
    		    		
    		this.notificationPlugin.process(item);
    	}
    	else{
    		LOGGER.debug(String.format("skip pluginOnProcess: triggerId=%s",re.getTriggerId()));
     	}
    }
    
    private void pluginOnExit(){
    	if(this.notificationPlugin != null){
    		this.notificationPlugin.exit();
    	}
    }
    
    public Date getDateFrom(){
        return this.dateFrom; 
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