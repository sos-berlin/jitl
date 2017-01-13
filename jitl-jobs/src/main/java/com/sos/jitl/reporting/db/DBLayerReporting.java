package com.sos.jitl.reporting.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.helper.CounterRemove;
import com.sos.jitl.reporting.helper.EReferenceType;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.schedulerhistory.db.SchedulerOrderStepHistoryDBItem;
import com.sos.scheduler.model.answers.Order;

import sos.util.SOSDuration;
import sos.util.SOSDurations;

public class DBLayerReporting extends DBLayer {

    final Logger LOGGER = LoggerFactory.getLogger(DBLayerReporting.class);

    public DBLayerReporting(SOSHibernateConnection conn) {
        super(conn);
    }

    public DBItemReportTrigger createReportTrigger(String schedulerId, Long historyId, String name, String title, String parentFolder,String parentName,
            String parentBasename, String parentTitle, String state, String stateText, Date startTime, Date endTime, boolean synCompleted, boolean isRuntimeDefined)
            throws Exception {
        try {
            DBItemReportTrigger item = new DBItemReportTrigger();
            item.setSchedulerId(schedulerId);
            item.setHistoryId(historyId);
            item.setName(name);
            item.setTitle(title);
            item.setParentFolder(parentFolder);
            item.setParentName(parentName);
            item.setParentBasename(parentBasename);
            item.setParentTitle(parentTitle);
            item.setState(state);
            item.setStateText(stateText);
            item.setStartTime(startTime);
            item.setEndTime(endTime);
            item.setSyncCompleted(synCompleted);
            item.setIsRuntimeDefined(isRuntimeDefined);
            item.setResultsCompleted(false);
            item.setSuspended(false);
            item.setCreated(ReportUtil.getCurrentDateTime());
            item.setModified(ReportUtil.getCurrentDateTime());
            getConnection().save(item);
            return item;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }
    
    public DBItemReportExecution createReportExecution(String schedulerId, Long historyId, Long triggerId, String clusterMemberId, Integer steps, Long step, String folder, String name, String basename,
            String title, Date startTime, Date endTime, String state, String cause,Integer exitCode, Boolean error, String errorCode, String errorText, String agentUrl,boolean synCompleted, boolean isRuntimeDefined)
            throws Exception {
        DBItemReportExecution item = new DBItemReportExecution();
        item.setSchedulerId(schedulerId);
        item.setHistoryId(historyId);
        item.setTriggerId(triggerId);
        item.setClusterMemberId(clusterMemberId);
        item.setSteps(steps);
        item.setStep(step);
        item.setFolder(folder);
        item.setName(name);
        item.setBasename(basename);
        item.setTitle(title);
        item.setStartTime(startTime);
        item.setEndTime(endTime);
        item.setState(state);
        item.setCause(cause);
        item.setExitCode(exitCode);
        item.setError(error);
        item.setErrorCode(errorCode);
        item.setErrorText(errorText);
        item.setAgentUrl(agentUrl);
        item.setIsRuntimeDefined(isRuntimeDefined);
        item.setSyncCompleted(synCompleted);
        item.setResultsCompleted(false);
        item.setSuspended(false);
        item.setCreated(ReportUtil.getCurrentDateTime());
        item.setModified(ReportUtil.getCurrentDateTime());
        return item;
    }

    public Criteria getStandaloneSyncUncomplitedIds(Optional<Integer> fetchSize, String schedulerId) throws Exception {
        Criteria cr = getConnection().createCriteria(DBItemReportExecution.class, new String[] { "id", "historyId" },null);
        cr.add(Restrictions.eq("schedulerId", schedulerId));
        cr.add(Restrictions.eq("triggerId",new Long(0)));
        cr.add(Restrictions.eq("syncCompleted", false));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }
    
    public Criteria getOrderSyncUncomplitedIds(Optional<Integer> fetchSize, String schedulerId) throws Exception {
        Criteria cr = getConnection().createCriteria(DBItemReportTrigger.class, new String[] { "id", "historyId" }, null);
        Criterion cr1 = Restrictions.eq("schedulerId", schedulerId);
        Criterion cr2 = Restrictions.eq("syncCompleted", false);
        Criterion where = Restrictions.and(cr1, cr2);
        cr.add(where);
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }
  
    public int removeTriggers() throws Exception {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ").append(DBITEM_REPORT_TRIGGERS);
            sql.append(" where suspended = true");
            return getConnection().createQuery(sql.toString()).executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int removeExecutions() throws Exception {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("delete");
            sql.append(" from ");
            sql.append(DBITEM_REPORT_EXECUTIONS);
            sql.append(" where suspended = true");
            return getConnection().createQuery(sql.toString()).executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int setTriggersAsRemoved(String schedulerId, Date dateFrom, Date dateTo) throws Exception {
        try {
            StringBuilder sql = null;
            Query q = null;
            int result = 0;
            if (schedulerId != null && !schedulerId.isEmpty()) {
                sql = new StringBuilder();
                sql.append("update ").append(DBITEM_REPORT_TRIGGERS);
                sql.append(" set suspended = true");
                sql.append(" where schedulerId = :schedulerId");
                sql.append(" and startTime <= :dateTo");
                if (dateFrom != null) {
                    sql.append(" and startTime >= :dateFrom");
                }
                q = getConnection().createQuery(sql.toString());
                q.setParameter("schedulerId", schedulerId);
                q.setParameter("dateTo", dateTo);
                if (dateFrom != null) {
                    q.setParameter("dateFrom", dateFrom);
                }
                result = q.executeUpdate();
            }
            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int setTriggersAsRemoved(List<Long> ids) throws Exception {
        try {
            StringBuilder sql = null;
            Query q = null;
            int result = 0;
            if (ids != null && !ids.isEmpty()) {
                sql = new StringBuilder();
                sql.append("update ").append(DBITEM_REPORT_TRIGGERS);
                sql.append(" set suspended = true");
                sql.append(" where id in :ids ");
                q = getConnection().createQuery(sql.toString());
                q.setParameterList("ids", ids);
                result = q.executeUpdate();
            }
            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int setExecutionsAsRemoved(List<Long> ids) throws Exception {
        try {
            StringBuilder sql = null;
            Query q = null;
            int result = 0;
            if (ids != null && !ids.isEmpty()) {
                sql = new StringBuilder();
                sql.append("update ");
                sql.append(DBITEM_REPORT_EXECUTIONS);
                sql.append(" set suspended = true");
                sql.append(" where id in :ids ");
                q = getConnection().createQuery(sql.toString());
                q.setParameterList("ids", ids);
                result = q.executeUpdate();
            }
            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }
    
    public int setOrderExecutionsAsRemoved() throws Exception {
        try {
           StringBuilder sql = new StringBuilder("update ");
            sql.append(DBITEM_REPORT_EXECUTIONS);
            sql.append(" set suspended = true");
            sql.append(" where triggerId in");
            sql.append(" (select id from ");
            sql.append(DBITEM_REPORT_TRIGGERS);
            sql.append(" where suspended = true)");
            Query q = getConnection().createQuery(sql.toString());
            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }
    
    public int setStandaloneExecutionsAsRemoved(String schedulerId, Date dateFrom, Date dateTo) throws Exception {
        try {
           StringBuilder sql = new StringBuilder("update ");
           sql.append(DBITEM_REPORT_EXECUTIONS+" ");
           sql.append("set suspended = true ");
           sql.append("where triggerId = 0 ");
           sql.append("and schedulerId = :schedulerId ");
           sql.append("and startTime <= :dateTo ");
           if (dateFrom != null) {
               	sql.append(" and startTime >= :dateFrom ");
           }
           Query q = getConnection().createQuery(sql.toString());
           q.setParameter("schedulerId", schedulerId);
           q.setParameter("dateTo", dateTo);
           if (dateFrom != null) {
               	q.setParameter("dateFrom", dateFrom);
           }
           return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int removeTriggerResults() throws Exception {
        try {
            StringBuilder sql = new StringBuilder("delete from ");
            sql.append(DBITEM_REPORT_TRIGGER_RESULTS);
            sql.append(" where triggerId in");
            sql.append(" (select id from ");
            sql.append(DBITEM_REPORT_TRIGGERS);
            sql.append(" where suspended = true)");
            Query q = getConnection().createQuery(sql.toString());
            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int removeTriggerDates() throws Exception {
        try {
            StringBuilder sql = new StringBuilder("delete from ");
            sql.append(DBITEM_REPORT_EXECUTION_DATES);
            sql.append(" where referenceType = :referenceType");
            sql.append(" and referenceId in");
            sql.append(" (select id from ");
            sql.append(DBITEM_REPORT_TRIGGERS);
            sql.append(" where suspended = true)");
            Query q = getConnection().createQuery(sql.toString());
            q.setParameter("referenceType", EReferenceType.TRIGGER.value());
            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int removeExecutionDates() throws Exception {
        try {
            StringBuilder sql = new StringBuilder("delete from ");
            sql.append(DBITEM_REPORT_EXECUTION_DATES);
            sql.append(" where referenceType = :referenceType");
            sql.append(" and referenceId in");
            sql.append(" (select id from ");
            sql.append(DBITEM_REPORT_EXECUTIONS);
            sql.append(" where suspended = true)");
            Query q = getConnection().createQuery(sql.toString());
            q.setParameter("referenceType", EReferenceType.EXECUTION.value());
            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }
    
    @SuppressWarnings("unchecked")
    public DBItemSchedulerVariableReporting getSchedulerVariabe(SOSHibernateConnection schedulerConnection) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_SCHEDULER_VARIABLES);
            sql.append(" where name = :name");
            Query q = schedulerConnection.createQuery(sql.toString());
            q.setParameter("name", TABLE_SCHEDULER_VARIABLES_REPORTING_VARIABLE);
            List<DBItemSchedulerVariableReporting> result = q.list();
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public CounterRemove removeOrder(String schedulerId, Date dateFrom, Date dateTo) throws Exception {
    	CounterRemove counter = new CounterRemove();
    	try {
            getConnection().beginTransaction();
            int markedAsRemoved = setTriggersAsRemoved(schedulerId, dateFrom, dateTo);
            getConnection().commit();
            
            if(markedAsRemoved != 0){
            	getConnection().beginTransaction();
            	setOrderExecutionsAsRemoved();
            	getConnection().commit();
            
            	getConnection().beginTransaction();
            	counter.setTriggerResults(removeTriggerResults());
            	getConnection().commit();
            
            	getConnection().beginTransaction();
            	counter.setTriggerDates(removeTriggerDates());
            	counter.setExecutionDates(removeExecutionDates());
            	getConnection().commit();
            	
            	getConnection().beginTransaction();
            	counter.setTriggers(removeTriggers());
            	getConnection().commit();
            
            	getConnection().beginTransaction();
            	counter.setExecutions(removeExecutions());
            	getConnection().commit();
            }
        } catch (Exception ex) {
            getConnection().rollback();
            throw ex;
        }
    	
    	return counter;
    }
    
    public CounterRemove removeStandalone(String schedulerId, Date dateFrom, Date dateTo) throws Exception {
    	CounterRemove counter = new CounterRemove();
    	try {
            getConnection().beginTransaction();
        	int markedAsRemoved = setStandaloneExecutionsAsRemoved(schedulerId,dateFrom,dateTo);
        	getConnection().commit();
        	
            if(markedAsRemoved != 0){
            	getConnection().beginTransaction();
            	counter.setExecutionDates(removeExecutionDates());
            	getConnection().commit();
            	
            	getConnection().beginTransaction();
            	counter.setExecutions(removeExecutions());
            	getConnection().commit();
            }
        } catch (Exception ex) {
            getConnection().rollback();
            throw ex;
        }
        return counter;
    }
    
    public CounterRemove removeOrderUncompleted(ArrayList<Long> triggerIds) throws Exception {
    	CounterRemove counter = new CounterRemove();
    	try {
            getConnection().beginTransaction();
            
            int	markedAsRemoved = setTriggersAsRemoved(triggerIds);
            if(markedAsRemoved != 0){
            	setOrderExecutionsAsRemoved();
            	counter.setTriggerResults(removeTriggerResults());
            	counter.setTriggerDates(removeTriggerDates());
            	counter.setExecutionDates(removeExecutionDates());
            	counter.setTriggers(removeTriggers());
            	counter.setExecutions(removeExecutions());
            }
            
            getConnection().commit();
        } catch (Exception ex) {
            getConnection().rollback();
            throw ex;
        }
        return counter;
    }

    public CounterRemove removeStandaloneUncompleted(ArrayList<Long> executionIds) throws Exception {
    	CounterRemove counter = new CounterRemove();
        try {
        	getConnection().beginTransaction();
            
            int markedAsRemoved = setExecutionsAsRemoved(executionIds);
            if(markedAsRemoved != 0){
            	counter.setExecutionDates(removeExecutionDates());
            	counter.setExecutions(removeExecutions());
            }
            
            getConnection().commit();
        } catch (Exception ex) {
            getConnection().rollback();
            throw ex;
        }
        return counter;
    }

    
    
    public DBItemSchedulerVariableReporting createSchedulerVariable(SOSHibernateConnection schedulerConnection, Long numericValue, String textValue)
            throws Exception {
        try {
            DBItemSchedulerVariableReporting item = new DBItemSchedulerVariableReporting();
            item.setName(TABLE_SCHEDULER_VARIABLES_REPORTING_VARIABLE);
            item.setNumericValue(numericValue);
            item.setTextValue(textValue);
            schedulerConnection.save(item);
            return item;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public void updateSchedulerVariable(SOSHibernateConnection schedulerConnection, DBItemSchedulerVariableReporting item) throws Exception {
        try {
            schedulerConnection.update(item);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    private String quote(String fieldName) {
        return getConnection().quoteFieldName(fieldName);
    }

    public String getInventoryJobChainStartCause(String schedulerId, String schedulerHostname, int schedulerHttpPort, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("select");
            sql.append(" ijc.startCause");
            sql.append(" from ");
            sql.append(DBITEM_INVENTORY_JOB_CHAINS).append(" ijc,");
            sql.append(DBITEM_INVENTORY_INSTANCES).append(" ii");
            sql.append(" where ijc.name = :name");
            sql.append(" and ii.schedulerId = :schedulerId");
            sql.append(" and ii.port = :schedulerHttpPort");
            sql.append(" and upper(ii.hostname) = :schedulerHostname");
            sql.append(" and ii.id = ijc.instanceId");
            Query q = getConnection().createQuery(sql.toString());
            q.setParameter("schedulerId", schedulerId);
            q.setParameter("schedulerHostname", schedulerHostname.toUpperCase());
            q.setParameter("schedulerHttpPort", schedulerHttpPort);
            q.setParameter("name", name);
            return (String) q.uniqueResult();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int triggerResultCompletedQuery(String schedulerId) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("update ");
            sql.append(DBITEM_REPORT_TRIGGERS);
            sql.append(" set resultsCompleted = true");
            sql.append(" where resultsCompleted = false");
            sql.append(" and syncCompleted = true");
            sql.append(" and schedulerId = :schedulerId");
            Query q = getConnection().createQuery(sql.toString());
            q.setParameter("schedulerId",schedulerId);
            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int executionResultCompletedQuery(String schedulerId) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("update ");
            sql.append(DBITEM_REPORT_EXECUTIONS);
            sql.append(" set resultsCompleted = true");
            sql.append(" where resultsCompleted = false");
            sql.append(" and syncCompleted = true");
            sql.append(" and schedulerId = :schedulerId");
            Query q = getConnection().createQuery(sql.toString());
            q.setParameter("schedulerId",schedulerId);
            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }
    
    public Criteria getOrderResultsUncompletedTriggers(Optional<Integer> fetchSize,String schedulerId) throws Exception {
        String[] fields = new String[] { "id", "schedulerId", "historyId", "parentName", "startTime", "endTime" };
        Criteria cr = getConnection().createCriteria(DBItemReportTrigger.class, fields);
        cr.add(Restrictions.eq("schedulerId",schedulerId));
        cr.add(Restrictions.eq("resultsCompleted", false));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getOrderResultsUncompletedExecutions(Optional<Integer> fetchSize, Long triggerId) throws Exception {
        String[] fields =
                new String[] { "id", "schedulerId", "historyId", "triggerId", "step", "name", "startTime", "endTime", "state", "cause", "error",
                        "errorCode", "errorText" };
        Criteria cr = getConnection().createCriteria(DBItemReportExecution.class, fields);
        cr.add(Restrictions.eq("triggerId", triggerId));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getStandaloneResultsUncompletedExecutions(Optional<Integer> fetchSize, String schedulerId) throws Exception {
        String[] fields =
                new String[] { "id", "schedulerId", "historyId", "triggerId", "step", "name", "startTime", "endTime", "state", "cause", "error",
                        "errorCode", "errorText" };
        Criteria cr = getConnection().createCriteria(DBItemReportExecution.class, fields);
        cr.add(Restrictions.eq("schedulerId",schedulerId));
        cr.add(Restrictions.eq("triggerId", new Long(0)));
        cr.add(Restrictions.eq("resultsCompleted",false));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }
    
    public Criteria getSchedulerHistoryTasks(SOSHibernateConnection schedulerConnection, Optional<Integer> fetchSize, String schedulerId, Date dateFrom, Date dateTo,
            ArrayList<Long> excludedTaskIds, ArrayList<Long> taskIds) throws Exception{
    
        Criteria cr = schedulerConnection.createCriteria(DBItemSchedulerHistory.class);
    	cr.add(Restrictions.eq("spoolerId",schedulerId));
    	if (dateTo != null) {
            cr.add(Restrictions.le("startTime", dateTo));
            if (dateFrom != null) {
                cr.add(Restrictions.ge("startTime", dateFrom));
            }
        } 
    	if (excludedTaskIds != null && excludedTaskIds.size() > 0) {
    		cr.add(Restrictions.not(SOSHibernateConnection.createInCriterion("id",excludedTaskIds)));
        }
    	if (taskIds != null && taskIds.size() > 0) {
            cr.add(SOSHibernateConnection.createInCriterion("id",taskIds));
        }
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }
    
    @SuppressWarnings("unchecked")
    public List<Object[]> getInventoryInfoForTrigger(Optional<Integer> fetchSize, String schedulerId, String schedulerHostname, int schedulerHttpPort, String orderId, String jobChainName) throws Exception{
        
        StringBuffer query = new StringBuffer("select ");
        query.append(quote("ijc.TITLE"));
        query.append(" ,"+quote("io.IS_RUNTIME_DEFINED"));
        query.append(" from "+TABLE_INVENTORY_ORDERS+" io");
        query.append(" ,"+TABLE_INVENTORY_JOB_CHAINS+" ijc");
        query.append(" ,"+TABLE_INVENTORY_INSTANCES+" ii ");
        query.append(" where ");
        query.append(quote("io.INSTANCE_ID")+"="+quote("ii.ID"));
        query.append(" and "+quote("ijc.INSTANCE_ID")+"="+quote("ii.ID"));
        query.append(" and "+quote("io.JOB_CHAIN_NAME")+"="+quote("ijc.NAME"));
        query.append(" and "+quote("ii.SCHEDULER_ID")+"= :schedulerId");
        query.append(" and upper("+quote("ii.HOSTNAME")+")= :schedulerHostname");
        query.append(" and "+quote("ii.PORT")+"= :schedulerHttpPort");
        query.append(" and "+quote("io.ORDER_ID")+"= :orderId");
        query.append(" and "+quote("io.JOB_CHAIN_NAME")+"= :jobChainName");
        
        SQLQuery q = getConnection().createSQLQuery(query.toString());
        q.setReadOnly(true);
        if (fetchSize != null && fetchSize.isPresent()) {
            q.setFetchSize(fetchSize.get());
        }
        
        DBItemReportInventoryInfo item = new DBItemReportInventoryInfo();
        q.setParameter("schedulerId",schedulerId);
        q.setParameter("schedulerHostname", schedulerHostname.toUpperCase());
        q.setParameter("schedulerHttpPort", schedulerHttpPort);
        q.setParameter("orderId",orderId);
        q.setParameter("jobChainName",item.normalizePath(jobChainName));
        return q.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<Object[]> getInventoryInfoForExecution(Optional<Integer> fetchSize, String schedulerId, String schedulerHostname, int schedulerHttpPort, String jobName, boolean isOrderJob) throws Exception{
        
        DBItemReportInventoryInfo item = new DBItemReportInventoryInfo();
        
        StringBuffer query = new StringBuffer("select ");
        query.append(quote("ij.TITLE"));
        query.append(" ,"+quote("ij.IS_RUNTIME_DEFINED"));
        query.append(" from "+TABLE_INVENTORY_JOBS+" ij");
        query.append(" ,"+TABLE_INVENTORY_INSTANCES+" ii ");
        query.append(" where ");
        query.append(quote("ij.INSTANCE_ID")+"="+quote("ii.ID"));
        query.append(" and "+quote("ii.SCHEDULER_ID")+"= :schedulerId");
        query.append(" and upper("+quote("ii.HOSTNAME")+")= :schedulerHostname");
        query.append(" and "+quote("ii.PORT")+"= :schedulerHttpPort");
        query.append(" and "+quote("ij.NAME")+"= :jobName");
        if(isOrderJob){
            query.append(" and "+quote("ij.IS_ORDER_JOB")+"= 1");
        }
        SQLQuery q = getConnection().createSQLQuery(query.toString());
        q.setReadOnly(true);
        if (fetchSize != null && fetchSize.isPresent()) {
            q.setFetchSize(fetchSize.get());
        }
        
        q.setParameter("schedulerId",schedulerId);
        q.setParameter("schedulerHostname", schedulerHostname.toUpperCase());
        q.setParameter("schedulerHttpPort", schedulerHttpPort);
        q.setParameter("jobName",item.normalizePath(jobName));
        return q.list();
    }
    
    @SuppressWarnings("unchecked")
	public DBItemSchedulerOrderStepHistory getSchedulerOrderHistoryLastStep(SOSHibernateConnection schedulerConnection, Long historyId) throws Exception{
    	StringBuffer query = new StringBuffer("from ");
        query.append(DBItemSchedulerOrderStepHistory.class.getSimpleName()+" osh1 ");
        query.append("where osh1.id.historyId = :historyId ");
        query.append("and osh1.id.step = (");
        query.append("select max(osh2.id.step) from ");
        query.append(DBItemSchedulerOrderStepHistory.class.getSimpleName()+" osh2 ");
        query.append("where osh2.id.historyId = :historyId ");
        query.append(") ");
        
        Query q = schedulerConnection.createQuery(query.toString());
        q.setParameter("historyId",historyId);
        q.setReadOnly(true);
        
        List<DBItemSchedulerOrderStepHistory> result = q.list();
        if (!result.isEmpty()) {
            return result.get(0);
        }
    	return null;
    }
    
    public DBItemReportTriggerResult createReportTriggerResults(String schedulerId, Long historyId, Long triggerId, String startCause, Long steps, boolean error, String errorCode,
            String errorText) throws Exception {

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
    
    public Criteria getSchedulerHistoryOrderSteps(SOSHibernateConnection schedulerConnection, Optional<Integer> fetchSize, String schedulerId, Date dateFrom, Date dateTo,
            ArrayList<Long> orderHistoryIds, ArrayList<Long> taskHistoryIds) throws Exception {
        
        int orderHistoryIdsSize = orderHistoryIds == null ? 0 : orderHistoryIds.size();
        int taskHistoryIdsSize = taskHistoryIds == null ? 0 : taskHistoryIds.size();
                
        Criteria cr = schedulerConnection.createCriteria(SchedulerOrderStepHistoryDBItem.class, "osh");
        // join
        cr.createAlias("osh.schedulerOrderHistoryDBItem", "oh");
        cr.createAlias("osh.schedulerTaskHistoryDBItem", "h");
        ProjectionList pl = Projections.projectionList();
        // select field list osh
        pl.add(Projections.property("osh.id.step").as("stepStep"));
        pl.add(Projections.property("osh.id.historyId").as("stepHistoryId"));
        pl.add(Projections.property("osh.taskId").as("stepTaskId"));
        pl.add(Projections.property("osh.startTime").as("stepStartTime"));
        pl.add(Projections.property("osh.endTime").as("stepEndTime"));
        pl.add(Projections.property("osh.state").as("stepState"));
        pl.add(Projections.property("osh.error").as("stepError"));
        pl.add(Projections.property("osh.errorCode").as("stepErrorCode"));
        pl.add(Projections.property("osh.errorText").as("stepErrorText"));
        // select field list oh
        pl.add(Projections.property("oh.historyId").as("orderHistoryId"));
        pl.add(Projections.property("oh.spoolerId").as("orderSchedulerId"));
        pl.add(Projections.property("oh.orderId").as("orderId"));
        pl.add(Projections.property("oh.cause").as("orderTitle"));
        pl.add(Projections.property("oh.jobChain").as("orderJobChain"));
        pl.add(Projections.property("oh.state").as("orderState"));
        pl.add(Projections.property("oh.stateText").as("orderStateText"));
        pl.add(Projections.property("oh.startTime").as("orderStartTime"));
        pl.add(Projections.property("oh.endTime").as("orderEndTime"));
        // select field list h
        pl.add(Projections.property("h.id").as("taskId"));
        pl.add(Projections.property("h.clusterMemberId").as("taskClusterMemberId"));
        pl.add(Projections.property("h.steps").as("taskSteps"));
        pl.add(Projections.property("h.jobName").as("taskJobName"));
        pl.add(Projections.property("h.exitCode").as("taskExitCode"));
        pl.add(Projections.property("h.cause").as("taskCause"));
        pl.add(Projections.property("h.agentUrl").as("taskAgentUrl"));
        cr.setProjection(pl);
        cr.add(Restrictions.eq("oh.spoolerId",schedulerId));
        cr.add(Restrictions.eq("h.spoolerId",schedulerId));
        // where
        if (dateTo != null) {
            cr.add(Restrictions.le("oh.startTime", dateTo));
            if (dateFrom != null) {
                cr.add(Restrictions.ge("oh.startTime", dateFrom));
            }
        } else if (orderHistoryIdsSize > 0) {
            if(orderHistoryIdsSize > 1){
                cr.add(Restrictions.in("oh.historyId", orderHistoryIds));
            }
            else{
                cr.add(Restrictions.eq("oh.historyId", orderHistoryIds.get(0)));
            }
        } else if (taskHistoryIdsSize > 0) {
            if(taskHistoryIdsSize > 1){
                cr.add(Restrictions.in("h.id", taskHistoryIds));
            }
            else{
                cr.add(Restrictions.eq("h.id", taskHistoryIds.get(0)));
            }
        }
        
        cr.setResultTransformer(Transformers.aliasToBean(DBItemSchedulerHistoryOrderStepReporting.class));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }
    
    @SuppressWarnings("unchecked")
    public DBItemReportTrigger getTrigger(String schedulerId, Long orderHistoryId) throws Exception{
        String sql = String.format("from %s  where schedulerId=:schedulerId and historyId=:historyId", DBITEM_REPORT_TRIGGERS);
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("schedulerId",schedulerId);
        query.setParameter("historyId",orderHistoryId);
        
        List<DBItemReportTrigger> result = query.list();
        if(result != null && result.size() > 0){
            return result.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public DBItemReportExecution getExecution(String schedulerId, Long historyId, Long triggerId, Long step) throws Exception{
        String sql = String.format("from %s  where schedulerId=:schedulerId and historyId=:historyId and triggerId=:triggerId and step=:step", DBITEM_REPORT_EXECUTIONS);
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("schedulerId",schedulerId);
        query.setParameter("historyId",historyId);
        query.setParameter("triggerId",triggerId);
        query.setParameter("step",step);
        
        List<DBItemReportExecution> result = query.list();
        if(result != null && result.size() > 0){
            return result.get(0);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public Long getOrderEstimatedDuration( Order order, int limit) throws Exception {
        // from Table REPORT_TRIGGERS
        if (order == null){
            return null;
        }
        try {
            String sql = String.format("from %s  where name = :orderId and parentName = :jobChain", DBITEM_REPORT_TRIGGERS);
            LOGGER.debug(sql.toString());
            Query query = getConnection().createQuery(sql.toString());
            query.setMaxResults(limit);
            query.setParameter("orderId", order.getId());
            query.setParameter("jobChain", order.getJobChain());
            SOSDurations durations = new SOSDurations();
            List<DBItemReportTrigger> result = query.list();
            if (result != null) {
                for (DBItemReportTrigger reportTrigger : result) {
                    SOSDuration duration = new SOSDuration();
                    duration.setStartTime(reportTrigger.getStartTime());
                    duration.setEndTime(reportTrigger.getEndTime());
                    durations.add(duration);
                }
                return durations.average();
            }
            return 0L;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }
    
    public Long getOrderEstimatedDuration(DBItemInventoryOrder order, int limit) throws Exception{
        Order orderIdentificator = new Order();
        order.setOrderId(order.getOrderId());
        order.setJobChainName(order.getJobChainName());
        return getOrderEstimatedDuration(orderIdentificator,limit);
    }

    @SuppressWarnings("unchecked")
    public Long getTaskEstimatedDuration(String jobName, int limit) throws Exception {
        // from Table REPORT_EXECUTIONS
        jobName = jobName.replaceFirst("^/","");
        try {
            String sql = String.format("from %s where error=0 and name = :jobName", DBITEM_REPORT_EXECUTIONS);
            LOGGER.debug(sql);
            Query query = getConnection().createQuery(sql);
            query.setParameter("jobName", jobName);
            query.setMaxResults(limit);
            SOSDurations durations = new SOSDurations();
            List<DBItemReportExecution> result = query.list();
            if (result != null) {
                for (DBItemReportExecution reportExecution : result) {
                    SOSDuration duration = new SOSDuration();
                    duration.setStartTime(reportExecution.getStartTime());
                    duration.setEndTime(reportExecution.getEndTime());
                    durations.add(duration);
                }
                return durations.average();
            }
            return 0L;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }
}