package com.sos.jitl.reporting.db;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.helper.EReferenceType;
import com.sos.jitl.reporting.helper.EStartCauses;
import com.sos.jitl.reporting.helper.InventoryInfo;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.schedulerhistory.db.SchedulerOrderStepHistoryDBItem;
import com.sos.jitl.schedulerhistory.db.SchedulerTaskHistoryDBItem;
import com.sos.scheduler.model.answers.Order;

import sos.util.SOSDuration;
import sos.util.SOSDurations;

public class DBLayerReporting extends DBLayer {

    final Logger LOGGER = LoggerFactory.getLogger(DBLayerReporting.class);

    public DBLayerReporting(SOSHibernateSession conn) {
        super(conn);
    }

    public DBItemReportTask updateTask(DBItemReportTask item, DBItemSchedulerHistory task, boolean syncCompleted) throws Exception {

        item.setClusterMemberId(task.getClusterMemberId());
        item.setSteps(task.getSteps());
        item.setStartTime(task.getStartTime());
        item.setEndTime(task.getEndTime());
        item.setCause(task.getCause());
        item.setExitCode(task.getExitCode());
        item.setError(task.isError());
        item.setErrorCode(task.getErrorCode());
        item.setErrorText(task.getErrorText());
        item.setSyncCompleted(syncCompleted);
        item.setAgentUrl(task.getAgentUrl());
        item.setModified(ReportUtil.getCurrentDateTime());

        getSession().update(item);
        return item;
    }

    public DBItemReportTask insertTask(DBItemSchedulerHistory task, InventoryInfo inventoryInfo, boolean isOrder, boolean syncCompleted)
            throws Exception {
        DBItemReportTask item = new DBItemReportTask();

        item.setSchedulerId(task.getSpoolerId());
        item.setHistoryId(task.getId());
        item.setIsOrder(isOrder);
        item.setClusterMemberId(task.getClusterMemberId());
        item.setSteps(task.getSteps());
        item.setFolder(ReportUtil.getFolderFromName(task.getJobName()));
        item.setName(task.getJobName());
        item.setBasename(ReportUtil.getBasenameFromName(task.getJobName()));
        item.setTitle(inventoryInfo.getTitle());
        item.setStartTime(task.getStartTime());
        item.setEndTime(task.getEndTime());
        item.setCause(task.getCause());
        item.setExitCode(task.getExitCode());
        item.setError(task.isError());
        item.setErrorCode(task.getErrorCode());
        item.setErrorText(task.getErrorText());
        item.setAgentUrl(task.getAgentUrl());
        item.setIsRuntimeDefined(inventoryInfo.getIsRuntimeDefined());
        item.setSyncCompleted(syncCompleted);
        item.setResultsCompleted(false);

        item.setCreated(ReportUtil.getCurrentDateTime());
        item.setModified(ReportUtil.getCurrentDateTime());

        getSession().save(item);
        return item;
    }

    public DBItemReportTask insertTaskByOrderStep(DBItemSchedulerHistoryOrderStepReporting step, InventoryInfo inventoryInfo, boolean syncCompleted)
            throws Exception {
        DBItemReportTask item = new DBItemReportTask();

        String notFoundJob = step.getOrderJobChain() + "/UnknownJob";
        String jobName = inventoryInfo.getName() == null ? notFoundJob : inventoryInfo.getName();
        String clusterMemberId = inventoryInfo.getClusterMemberIdFromInstance();
        Integer steps = new Integer(1);
        Date startTime = step.getStepStartTime();
        Date endTime = null;
        String cause = EStartCauses.ORDER.value();
        Integer exitCode = new Integer(step.isStepError() ? 1 : 0);
        boolean error = step.isStepError();
        String errorCode = step.getStepErrorCode();
        String errorText = step.getStepErrorText();
        String agentUrl = inventoryInfo.getUrl();

        if (step.getTaskId() != null) {
            jobName = step.getTaskJobName();
            clusterMemberId = step.getTaskClusterMemberId();
            steps = step.getTaskSteps();
            startTime = step.getTaskStartTime();
            endTime = step.getTaskEndTime();
            cause = step.getTaskCause();
            exitCode = step.getTaskExitCode();
            error = step.isTaskError();
            errorCode = step.getTaskErrorCode();
            errorText = step.getTaskErrorText();
            agentUrl = step.getTaskAgentUrl();
        }

        item.setSchedulerId(step.getOrderSchedulerId());
        item.setHistoryId(step.getStepTaskId());
        item.setIsOrder(true);
        item.setClusterMemberId(clusterMemberId);
        item.setSteps(steps);
        item.setFolder(ReportUtil.getFolderFromName(jobName));
        item.setName(jobName);
        item.setBasename(ReportUtil.getBasenameFromName(jobName));
        item.setTitle(inventoryInfo.getTitle());
        item.setStartTime(startTime);
        item.setEndTime(endTime);
        item.setCause(cause);
        item.setExitCode(exitCode);
        item.setError(error);
        item.setErrorCode(errorCode);
        item.setErrorText(errorText);
        item.setAgentUrl(agentUrl);
        item.setIsRuntimeDefined(inventoryInfo.getIsRuntimeDefined());
        item.setSyncCompleted(syncCompleted);
        item.setResultsCompleted(false);

        item.setCreated(ReportUtil.getCurrentDateTime());
        item.setModified(ReportUtil.getCurrentDateTime());

        getSession().save(item);
        return item;
    }

    public DBItemReportTrigger insertTrigger(DBItemSchedulerHistoryOrderStepReporting step, InventoryInfo inventoryInfo, String startCause,
            boolean syncCompleted) throws Exception {
        DBItemReportTrigger item = new DBItemReportTrigger();
        item.setSchedulerId(step.getOrderSchedulerId());
        item.setHistoryId(step.getOrderHistoryId());
        item.setName(step.getOrderId());
        item.setTitle(step.getOrderTitle());
        item.setParentFolder(ReportUtil.getFolderFromName(step.getOrderJobChain()));
        item.setParentName(step.getOrderJobChain());
        item.setParentBasename(ReportUtil.getBasenameFromName(step.getOrderJobChain()));
        item.setParentTitle(inventoryInfo.getTitle());
        item.setState(step.getOrderState());
        item.setStateText(step.getOrderStateText());
        item.setStartTime(step.getOrderStartTime());
        item.setEndTime(step.getOrderEndTime());
        item.setSyncCompleted(syncCompleted);
        item.setIsRuntimeDefined(inventoryInfo.getIsRuntimeDefined());
        item.setResultStartCause(startCause);
        item.setResultSteps(new Long(0));
        item.setResultError(false);
        item.setResultErrorCode(null);
        item.setResultErrorText(null);
        item.setResultsCompleted(false);

        item.setCreated(ReportUtil.getCurrentDateTime());
        item.setModified(ReportUtil.getCurrentDateTime());

        getSession().save(item);
        return item;
    }

    public DBItemReportTrigger updateTrigger(DBItemReportTrigger item, DBItemSchedulerHistoryOrderStepReporting step, boolean syncCompleted)
            throws Exception {

        item.setEndTime(step.getOrderEndTime());
        item.setSyncCompleted(syncCompleted);
        item.setModified(ReportUtil.getCurrentDateTime());

        getSession().update(item);
        return item;
    }

    public DBItemReportTrigger updateTriggerResults(DBItemReportTrigger item, DBItemReportExecution execution) throws Exception {

        item.setResultSteps(execution.getStep());
        item.setResultError(execution.getError());
        item.setResultErrorCode(execution.getErrorCode());
        item.setResultErrorText(execution.getErrorText());
        item.setModified(ReportUtil.getCurrentDateTime());

        getSession().update(item);
        return item;
    }

    public DBItemReportExecution insertExecution(DBItemSchedulerHistoryOrderStepReporting step, DBItemReportTrigger trigger, DBItemReportTask task,
            boolean syncCompleted) throws Exception {

        DBItemReportExecution item = new DBItemReportExecution();
        item.setSchedulerId(step.getOrderSchedulerId());
        item.setHistoryId(step.getStepTaskId());
        item.setTriggerId(trigger.getId());
        item.setStep(step.getStepStep());

        item.setTaskId(task.getId());
        item.setClusterMemberId(task.getClusterMemberId());
        item.setFolder(ReportUtil.getFolderFromName(task.getName()));
        item.setName(task.getName());
        item.setBasename(ReportUtil.getBasenameFromName(task.getName()));
        item.setTitle(task.getTitle());
        item.setStartTime(step.getStepStartTime());
        item.setEndTime(step.getStepEndTime());
        item.setState(step.getStepState());
        item.setCause(task.getCause());
        item.setExitCode(task.getExitCode());
        item.setError(step.isStepError());
        item.setErrorCode(step.getStepErrorCode());
        item.setErrorText(step.getStepErrorText());
        item.setAgentUrl(task.getAgentUrl());
        item.setIsRuntimeDefined(task.getIsRuntimeDefined());
        item.setSyncCompleted(syncCompleted);
        item.setResultsCompleted(false);
        item.setCreated(ReportUtil.getCurrentDateTime());
        item.setModified(ReportUtil.getCurrentDateTime());

        getSession().save(item);
        return item;
    }

    public DBItemReportExecution updateExecution(DBItemReportExecution item, DBItemSchedulerHistoryOrderStepReporting step, boolean syncCompleted)
            throws Exception {

        item.setEndTime(step.getStepEndTime());
        item.setState(step.getStepState());
        item.setCause(step.getTaskCause());
        item.setExitCode(step.getTaskExitCode());
        item.setError(step.isStepError());
        item.setErrorCode(step.getStepErrorCode());
        item.setErrorText(step.getStepErrorText());
        item.setSyncCompleted(syncCompleted);
        item.setCreated(ReportUtil.getCurrentDateTime());
        item.setModified(ReportUtil.getCurrentDateTime());

        getSession().update(item);
        return item;
    }

    public Criteria getTaskSyncUncomplitedHistoryIds(Optional<Integer> fetchSize, String schedulerId) throws Exception {
        Criteria cr = getSession().createCriteria(DBItemReportTask.class, new String[] { "historyId" }, null);
        cr.add(Restrictions.eq("schedulerId", schedulerId));
        cr.add(Restrictions.eq("syncCompleted", false));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getOrderSyncUncomplitedHistoryIds(Optional<Integer> fetchSize, String schedulerId) throws Exception {
        Criteria cr = getSession().createCriteria(DBItemReportTrigger.class, new String[] { "historyId" }, null);
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

    @SuppressWarnings("unchecked")
    public DBItemReportVariable getReportVariabe(String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_REPORT_VARIABLES);
            sql.append(" where name = :name");
            Query q = getSession().createQuery(sql.toString());
            q.setParameter("name", name);
            List<DBItemReportVariable> result = executeQueryList(q);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception e) {
            throw new Exception(String.format("getReportVariabe: %s", e.toString()), e);
        }
    }

    public DBItemReportVariable insertReportVariable(String name, Long numericValue, String textValue) throws Exception {
        try {
            DBItemReportVariable item = new DBItemReportVariable();
            item.setName(name);
            item.setNumericValue(numericValue);
            item.setTextValue(textValue);
            getSession().save(item);
            return item;
        } catch (Exception e) {
            throw new Exception(String.format("createReportVariable: %s", e.toString()), e);
        }
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
            Query q = getSession().createQuery(sql.toString());
            q.setParameter("schedulerId", schedulerId);
            q.setParameter("schedulerHostname", schedulerHostname.toUpperCase());
            q.setParameter("schedulerHttpPort", schedulerHttpPort);
            q.setParameter("name", name);
            return (String) q.uniqueResult();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public Criteria getResultsUncompletedTriggers(Optional<Integer> fetchSize, String schedulerId) throws Exception {
        Criteria cr = getSession().createCriteria(DBItemReportTrigger.class);
        cr.add(Restrictions.eq("schedulerId", schedulerId));
        cr.add(Restrictions.eq("syncCompleted", true));
        cr.add(Restrictions.eq("resultsCompleted", false));
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getResultsUncompletedExecutions(Optional<Integer> fetchSize, String schedulerId) throws Exception {
        Criteria cr = getSession().createCriteria(DBItemReportExecution.class);
        cr.add(Restrictions.eq("schedulerId", schedulerId));
        cr.add(Restrictions.eq("syncCompleted", true));
        cr.add(Restrictions.eq("resultsCompleted", false));
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getResultsUncompletedTasks(Optional<Integer> fetchSize, String schedulerId) throws Exception {
        Criteria cr = getSession().createCriteria(DBItemReportTask.class);
        cr.add(Restrictions.eq("schedulerId", schedulerId));
        cr.add(Restrictions.eq("syncCompleted", true));
        cr.add(Restrictions.eq("resultsCompleted", false));
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getSchedulerHistoryTasks(SOSHibernateSession schedulerSession, Optional<Integer> fetchSize, String schedulerId,
            List<Long> taskIds) throws Exception {
        return this.getSchedulerHistoryTasks(schedulerSession, fetchSize, schedulerId, null, null, taskIds);
    }

    public Criteria getSchedulerHistoryTasks(SOSHibernateSession schedulerSession, Optional<Integer> fetchSize, String schedulerId, Date dateFrom,
            Date dateTo) throws Exception {
        return this.getSchedulerHistoryTasks(schedulerSession, fetchSize, schedulerId, dateFrom, dateTo, null);
    }

    public Criteria getSchedulerHistoryTasks(SOSHibernateSession schedulerSession, Optional<Integer> fetchSize, String schedulerId, Date dateFrom,
            Date dateTo, List<Long> taskIds) throws Exception {

        Criteria cr = schedulerSession.createCriteria(DBItemSchedulerHistory.class);
        cr.add(Restrictions.eq("spoolerId", schedulerId));
        if (dateTo != null) {
            cr.add(Restrictions.le("startTime", dateTo));
            if (dateFrom != null) {
                cr.add(Restrictions.ge("startTime", dateFrom));
            }
        }
        if (taskIds != null && taskIds.size() > 0) {
            cr.add(SOSHibernateSession.createInCriterion("id", taskIds));
        }
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    /** columns order like getInventoryJobInfoByJobChain and getInventoryOrderInfoByJobChain */
    public List<Object[]> getInventoryJobInfoByJobName(String schedulerId, String schedulerHostname, int schedulerHttpPort, String jobName)
            throws Exception {

        StringBuffer query = new StringBuffer("select ");
        query.append(quote("ij.NAME"));
        query.append(" ," + quote("ij.TITLE"));
        query.append(" ," + quote("ij.IS_RUNTIME_DEFINED"));
        query.append(" , " + quote("ij.IS_ORDER_JOB"));
        query.append(" from " + TABLE_INVENTORY_JOBS + " ij");
        query.append(" ," + TABLE_INVENTORY_INSTANCES + " ii ");
        query.append(" where ");
        query.append(quote("ij.INSTANCE_ID") + "=" + quote("ii.ID"));
        query.append(" and " + quote("ii.SCHEDULER_ID") + "= :schedulerId");
        query.append(" and upper(" + quote("ii.HOSTNAME") + ")= :schedulerHostname");
        query.append(" and " + quote("ii.PORT") + "= :schedulerHttpPort");
        query.append(" and " + quote("ij.NAME") + "= :jobName");

        NativeQuery q = getSession().createNativeQuery(query.toString());
        q.setReadOnly(true);
        q.setParameter("schedulerId", schedulerId);
        q.setParameter("schedulerHostname", schedulerHostname.toUpperCase());
        q.setParameter("schedulerHttpPort", schedulerHttpPort);
        q.setParameter("jobName", ReportUtil.normalizeDbItemPath(jobName));
        return executeQueryList(q);
    }

    @SuppressWarnings("rawtypes")
    /** columns order like getInventoryJobInfoByJobName and getInventoryOrderInfoByJobChain */
    public List<Object[]> getInventoryJobInfoByJobChain(String schedulerId, String schedulerHostname, int schedulerHttpPort, String jobChainName,
            String stepState) throws Exception {

        StringBuffer query = new StringBuffer("select ");
        query.append(quote("ij.NAME"));
        query.append(" ," + quote("ij.TITLE"));
        query.append(" ," + quote("ij.IS_RUNTIME_DEFINED"));
        query.append(" ," + quote("ij.IS_ORDER_JOB"));
        query.append(" ," + quote("ii.CLUSTER_TYPE"));
        query.append(" ," + quote("iacm.URL"));
        query.append(" ," + quote("iacm.ORDERING"));
        query.append(" from " + TABLE_INVENTORY_JOB_CHAIN_NODES + " ijcn");
        query.append(" left join " + TABLE_INVENTORY_JOB_CHAINS + " ijc");
        query.append(" on " + quote("ijcn.JOB_CHAIN_ID") + "=" + quote("ijc.ID"));
        query.append(" left join " + TABLE_INVENTORY_INSTANCES + " ii");
        query.append(" on " + quote("ijcn.INSTANCE_ID") + "=" + quote("ii.ID"));
        query.append(" left join " + TABLE_INVENTORY_JOBS + " ij");
        query.append(" on " + quote("ijcn.JOB_NAME") + "=" + quote("ij.NAME"));
        query.append(" and " + quote("ij.INSTANCE_ID") + "=" + quote("ii.ID"));
        query.append(" left outer join " + TABLE_INVENTORY_PROCESS_CLASSES + " ipc");
        query.append(" on " + quote("ij.PROCESS_CLASS_ID") + "=" + quote("ipc.ID"));
        query.append(" left outer join " + TABLE_INVENTORY_AGENT_CLUSTER + " iac");
        query.append(" on " + quote("iac.PROCESS_CLASS_ID") + "=" + quote("ipc.ID"));
        query.append(" left outer join " + TABLE_INVENTORY_AGENT_CLUSTERMEMBERS + " iacm");
        query.append(" on " + quote("iacm.AGENT_CLUSTER_ID") + "=" + quote("iac.ID"));
        query.append(" and " + quote("iacm.INSTANCE_ID") + "=" + quote("ii.ID"));
        query.append(" where");
        query.append(" " + quote("ijcn.STATE") + "= :stepState");
        query.append(" and " + quote("ijc.NAME") + "= :jobChainName");
        query.append(" and " + quote("ii.SCHEDULER_ID") + "= :schedulerId");
        query.append(" and upper(" + quote("ii.HOSTNAME") + ")= :schedulerHostname");
        query.append(" and " + quote("ii.PORT") + "= :schedulerHttpPort");

        NativeQuery q = getSession().createNativeQuery(query.toString());
        q.setReadOnly(true);
        q.setParameter("stepState", stepState);
        q.setParameter("jobChainName", ReportUtil.normalizeDbItemPath(jobChainName));
        q.setParameter("schedulerId", schedulerId);
        q.setParameter("schedulerHostname", schedulerHostname.toUpperCase());
        q.setParameter("schedulerHttpPort", schedulerHttpPort);
        return executeQueryList(q); // results.isEmpty() ? null : (String) results.get(0);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    /** columns order like getInventoryJobInfoByJobName and getInventoryJobInfoByJobChain */
    public List<Object[]> getInventoryOrderInfoByJobChain(String schedulerId, String schedulerHostname, int schedulerHttpPort, String orderId,
            String jobChainName) throws Exception {

        StringBuffer query = new StringBuffer("select ");
        query.append(quote("ijc.NAME"));
        query.append(" ," + quote("ijc.TITLE"));
        query.append(" ," + quote("io.IS_RUNTIME_DEFINED"));
        query.append(" from " + TABLE_INVENTORY_ORDERS + " io");
        query.append(" ," + TABLE_INVENTORY_JOB_CHAINS + " ijc");
        query.append(" ," + TABLE_INVENTORY_INSTANCES + " ii ");
        query.append(" where ");
        query.append(quote("io.INSTANCE_ID") + "=" + quote("ii.ID"));
        query.append(" and " + quote("ijc.INSTANCE_ID") + "=" + quote("ii.ID"));
        query.append(" and " + quote("io.JOB_CHAIN_NAME") + "=" + quote("ijc.NAME"));
        query.append(" and " + quote("ii.SCHEDULER_ID") + "= :schedulerId");
        query.append(" and upper(" + quote("ii.HOSTNAME") + ")= :schedulerHostname");
        query.append(" and " + quote("ii.PORT") + "= :schedulerHttpPort");
        query.append(" and " + quote("io.ORDER_ID") + "= :orderId");
        query.append(" and " + quote("io.JOB_CHAIN_NAME") + "= :jobChainName");

        NativeQuery q = getSession().createNativeQuery(query.toString());
        q.setReadOnly(true);
        q.setParameter("schedulerId", schedulerId);
        q.setParameter("schedulerHostname", schedulerHostname.toUpperCase());
        q.setParameter("schedulerHttpPort", schedulerHttpPort);
        q.setParameter("orderId", orderId);
        q.setParameter("jobChainName", ReportUtil.normalizeDbItemPath(jobChainName));
        return executeQueryList(q);
    }

    @SuppressWarnings("rawtypes")
    public List executeCriteriaList(Criteria criteria) throws Exception {
        /** List result = null; try{ result = criteria.list(); } catch(Exception e){ Thread.sleep(2_000); result = criteria.list(); } return result; */
        return criteria.list();
    }

    @SuppressWarnings("rawtypes")
    public List executeQueryList(Query q) throws Exception {
        /** List result = null; try{ result = q.list(); } catch(Exception e){ Thread.sleep(2_000); result = q.list(); } return result; */
        return q.getResultList();
    }

    @SuppressWarnings("rawtypes")
    public List executeQueryList(NativeQuery q) throws Exception {
        /** List result = null; try{ result = q.list(); } catch(Exception e){ Thread.sleep(2_000); result = q.list(); } return result; */
        return q.getResultList();
    }

    public Long getCountSchedulerHistoryTasks(SOSHibernateSession schedulerSession, String schedulerId, Date dateFrom) throws Exception {
        StringBuilder stmt = new StringBuilder("select count(id) from ");
        stmt.append(SchedulerTaskHistoryDBItem.class.getSimpleName());
        stmt.append(" where spoolerId =:schedulerId");
        stmt.append(" and startTime >=:dateFrom");

        Query q = schedulerSession.createQuery(stmt.toString());
        q.setParameter("schedulerId", schedulerId);
        q.setParameter("dateFrom", dateFrom);
        return (Long) q.getSingleResult();
    }

    public Criteria getSchedulerHistoryOrderSteps(SOSHibernateSession schedulerSession, Optional<Integer> fetchSize, String schedulerId,
            Date dateFrom, Date dateTo) throws Exception {
        return this.getSchedulerHistoryOrderSteps(schedulerSession, fetchSize, schedulerId, dateFrom, dateTo, null);
    }

    public Criteria getSchedulerHistoryOrderSteps(SOSHibernateSession schedulerSession, Optional<Integer> fetchSize, String schedulerId,
            List<Long> orderHistoryIds) throws Exception {
        return this.getSchedulerHistoryOrderSteps(schedulerSession, fetchSize, schedulerId, null, null, orderHistoryIds);
    }

    public Criteria getSchedulerHistoryOrderSteps(SOSHibernateSession schedulerSession, Optional<Integer> fetchSize, String schedulerId,
            Date dateFrom, Date dateTo, List<Long> orderHistoryIds) throws Exception {

        int orderHistoryIdsSize = orderHistoryIds == null ? 0 : orderHistoryIds.size();

        Criteria cr = schedulerSession.createCriteria(SchedulerOrderStepHistoryDBItem.class, "osh");
        // join
        cr.createAlias("osh.schedulerOrderHistoryDBItem", "oh");
        cr.createAlias("osh.schedulerTaskHistoryDBItem", "h", JoinType.LEFT_OUTER_JOIN);
        // cr.createAlias("osh.schedulerTaskHistoryDBItem", "h");
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
        pl.add(Projections.property("h.startTime").as("taskStartTime"));
        pl.add(Projections.property("h.endTime").as("taskEndTime"));
        pl.add(Projections.property("h.error").as("taskError"));
        pl.add(Projections.property("h.errorCode").as("taskErrorCode"));
        pl.add(Projections.property("h.errorText").as("taskErrorText"));

        cr.setProjection(pl);
        cr.add(Restrictions.eq("oh.spoolerId", schedulerId));
        // cr.add(Restrictions.eq("h.spoolerId", schedulerId));
        // where
        if (dateTo != null) {
            cr.add(Restrictions.le("oh.startTime", dateTo));
            if (dateFrom != null) {
                cr.add(Restrictions.ge("oh.startTime", dateFrom));
            }
        } else if (orderHistoryIdsSize > 0) {
            if (orderHistoryIdsSize > 1) {
                cr.add(Restrictions.in("oh.historyId", orderHistoryIds));
                // cr.add(SOSHibernateSession.createInCriterion("oh.historyId", orderHistoryIds));
            } else {
                cr.add(Restrictions.eq("oh.historyId", orderHistoryIds.get(0)));
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
    public DBItemReportTrigger getTrigger(String schedulerId, Long historyId) throws Exception {
        String sql = String.format("from %s  where schedulerId=:schedulerId and historyId=:historyId", DBITEM_REPORT_TRIGGERS);
        Query<DBItemReportTrigger> query = getSession().createQuery(sql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("historyId", historyId);

        List<DBItemReportTrigger> result = query.getResultList();
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public DBItemReportExecution getExecution(String schedulerId, Long historyId, Long triggerId, Long step) throws Exception {
        String sql = String.format("from %s  where schedulerId=:schedulerId and historyId=:historyId and triggerId=:triggerId and step=:step",
                DBITEM_REPORT_EXECUTIONS);
        Query<DBItemReportExecution> query = getSession().createQuery(sql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("historyId", historyId);
        query.setParameter("triggerId", triggerId);
        query.setParameter("step", step);

        List<DBItemReportExecution> result = query.getResultList();
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<DBItemReportExecution> getExecutionsByTask(Long taskId) throws Exception {
        String sql = String.format("from %s where taskId=:taskId", DBITEM_REPORT_EXECUTIONS);
        Query<DBItemReportExecution> query = getSession().createQuery(sql.toString());
        query.setParameter("taskId", taskId);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public DBItemReportTask getTask(String schedulerId, Long historyId) throws Exception {
        String sql = String.format("from %s  where schedulerId=:schedulerId and historyId=:historyId", DBITEM_REPORT_TASKS);
        Query<DBItemReportTask> query = getSession().createQuery(sql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("historyId", historyId);

        List<DBItemReportTask> result = query.getResultList();
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<DBItemReportExecutionDate> getExecutionDates(EReferenceType type, Long id) throws Exception {
        String sql = String.format("from %s  where referenceType=:referenceType and referenceId=:referenceId", DBITEM_REPORT_EXECUTION_DATES);
        Query<DBItemReportExecutionDate> query = getSession().createQuery(sql.toString());
        query.setParameter("referenceType", type.value());
        query.setParameter("referenceId", id);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public void removeExecutionDates(EReferenceType type, Long id) throws Exception {
        String sql = String.format("delete from %s  where referenceType=:referenceType and referenceId=:referenceId", DBITEM_REPORT_EXECUTION_DATES);
        Query<DBItemReportExecutionDate> query = getSession().createQuery(sql.toString());
        query.setParameter("referenceType", type.value());
        query.setParameter("referenceId", id);
        query.executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public Long getOrderEstimatedDuration(Order order, int limit) throws Exception {
        // from Table REPORT_TRIGGERS
        if (order == null) {
            return null;
        }
        try {
            List<DBItemReportTrigger> result = null;
            String sql = String.format("from %s  where name = :orderId and parentName = :jobChain order by startTime desc", DBITEM_REPORT_TRIGGERS);
            LOGGER.debug(sql);
            Query<DBItemReportTrigger> query = getSession().createQuery(sql.toString());
            if (limit > 0) {
                query.setMaxResults(limit);
            }
            query.setParameter("orderId", order.getId());
            query.setParameter("jobChain", order.getJobChain());
            result = query.getResultList();
            SOSDurations durations = new SOSDurations();
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
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public Long getOrderEstimatedDuration(DBItemInventoryOrder order, int limit) throws Exception {
        Order orderIdentificator = new Order();
        orderIdentificator.setId(order.getOrderId());
        orderIdentificator.setJobChain(order.getJobChainName());
        return getOrderEstimatedDuration(orderIdentificator, limit);
    }

    @SuppressWarnings("unchecked")
    public Long getTaskEstimatedDuration(String jobName, int limit) throws Exception {
        jobName = jobName.replaceFirst("^/", "");
        String sql = String.format("from %s where error=0 and name = :jobName order by startTime desc", DBITEM_REPORT_TASKS);
        Query<DBItemReportTask> query = getSession().createQuery(sql);
        query.setParameter("jobName", jobName);
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<DBItemReportTask> result = query.getResultList();
        SOSDurations durations = new SOSDurations();
        if (result != null) {
            for (DBItemReportTask reportExecution : result) {
                SOSDuration duration = new SOSDuration();
                duration.setStartTime(reportExecution.getStartTime());
                duration.setEndTime(reportExecution.getEndTime());
                durations.add(duration);
            }
            return durations.average();
        }
        return 0L;
    }

    private String quote(String fieldName) {
        return getSession().getFactory().quoteFieldName(fieldName);
    }
}