package com.sos.jitl.reporting.db;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.ScrollableResults;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.reporting.helper.EReferenceType;
import com.sos.jitl.reporting.helper.EStartCauses;
import com.sos.jitl.reporting.helper.InventoryInfo;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.schedulerhistory.db.SchedulerOrderStepHistoryDBItem;
import com.sos.jitl.schedulerhistory.db.SchedulerTaskHistoryDBItem;

import sos.util.SOSDuration;
import sos.util.SOSDurations;
import sos.util.SOSString;

public class DBLayerReporting extends DBLayer {

    public static final String NOT_FOUNDED_JOB_BASENAME = "UnknownJob";
    final Logger LOGGER = LoggerFactory.getLogger(DBLayerReporting.class);

    public DBLayerReporting(SOSHibernateSession conn) {
        super(conn);
    }

    public DBItemReportTask updateTask(DBItemReportTask item, DBItemSchedulerHistory task, boolean syncCompleted) throws SOSHibernateException {
        item.setClusterMemberId(task.getClusterMemberId());
        item.setSteps(task.getSteps());
        if (task.getStartTime() != null) {// prevent 0000-00-00 00:00
            item.setStartTime(task.getStartTime());
        }
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
            throws SOSHibernateException {
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
            throws SOSHibernateException {
        String jobName = null;
        String clusterMemberId = null;
        Integer steps = null;
        Date startTime = null;
        Date endTime = null;
        String cause = null;
        Integer exitCode = null;
        boolean error = false;
        String errorCode = null;
        String errorText = null;
        String agentUrl = null;

        if (step.getTaskId() == null) {
            String notFoundedJob = step.getOrderJobChain() + "/" + NOT_FOUNDED_JOB_BASENAME;
            jobName = SOSString.isEmpty(inventoryInfo.getName()) ? notFoundedJob : inventoryInfo.getName();
            clusterMemberId = inventoryInfo.getClusterMemberIdFromInstance();
            steps = new Integer(1);
            startTime = step.getStepStartTime();
            endTime = null;
            cause = EStartCauses.ORDER.value();
            exitCode = new Integer(step.isStepError() ? 1 : 0);
            error = step.isStepError();
            errorCode = step.getStepErrorCode();
            errorText = step.getStepErrorText();
            agentUrl = inventoryInfo.getUrl();
        } else {
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

        DBItemReportTask item = new DBItemReportTask();
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
            boolean syncCompleted) throws SOSHibernateException {
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
            throws SOSHibernateException {
        item.setEndTime(step.getOrderEndTime());
        item.setSyncCompleted(syncCompleted);
        item.setModified(ReportUtil.getCurrentDateTime());

        getSession().update(item);
        return item;
    }

    public DBItemReportTrigger updateTriggerResults(DBItemReportTrigger item, DBItemReportExecution execution,
            DBItemSchedulerHistoryOrderStepReporting historyOrderStep) throws SOSHibernateException {

        item.setState(historyOrderStep.getOrderState());
        item.setStateText(historyOrderStep.getOrderStateText());

        item.setResultSteps(execution.getStep());
        item.setResultError(execution.getError());
        item.setResultErrorCode(execution.getErrorCode());
        item.setResultErrorText(execution.getErrorText());
        item.setModified(ReportUtil.getCurrentDateTime());

        getSession().update(item);
        return item;
    }

    public DBItemReportExecution insertExecution(DBItemSchedulerHistoryOrderStepReporting step, DBItemReportTrigger trigger, DBItemReportTask task,
            boolean syncCompleted) throws SOSHibernateException {
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
            throws SOSHibernateException {
        item.setFolder(ReportUtil.getFolderFromName(step.getTaskJobName()));
        item.setName(step.getTaskJobName());
        item.setBasename(ReportUtil.getBasenameFromName(step.getTaskJobName()));

        if (item.getAgentUrl() == null) {
            item.setAgentUrl(step.getTaskAgentUrl());
        }
        if (item.getClusterMemberId() == null) {
            item.setClusterMemberId(step.getTaskClusterMemberId());
        }

        item.setEndTime(step.getStepEndTime());
        item.setState(step.getStepState());
        item.setCause(step.getTaskCause());
        item.setExitCode(step.getTaskExitCode());
        item.setError(step.isStepError());
        item.setErrorCode(step.getStepErrorCode());
        item.setErrorText(step.getStepErrorText());
        item.setSyncCompleted(syncCompleted);
        item.setModified(ReportUtil.getCurrentDateTime());

        getSession().update(item);
        return item;
    }

    public List<Long> getTaskSyncUncomplitedHistoryIds(Optional<Integer> fetchSize, String schedulerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select historyId from " + DBITEM_REPORT_TASKS);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and syncCompleted = false");

        Query<Long> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setReadOnly(true);
        if (fetchSize.isPresent()) {
            query.setFetchSize(fetchSize.get());
        }
        return getSession().getResultList(query);
    }

    public List<Long> getTasksHistoryIds(Optional<Integer> fetchSize, String schedulerId, List<Long> historyIds) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select historyId from " + DBITEM_REPORT_TASKS);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and historyId in :historyIds");

        Query<Long> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameterList("historyIds", historyIds);
        query.setReadOnly(true);
        if (fetchSize.isPresent()) {
            query.setFetchSize(fetchSize.get());
        }
        return getSession().getResultList(query);
    }

    public List<Long> getOrderSyncUncomplitedHistoryIds(Optional<Integer> fetchSize, String schedulerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select historyId from " + DBITEM_REPORT_TRIGGERS);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and syncCompleted = false");

        Query<Long> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setReadOnly(true);
        if (fetchSize.isPresent()) {
            query.setFetchSize(fetchSize.get());
        }
        return getSession().getResultList(query);
    }

    public DBItemReportVariable getReportVariabe(String name) throws SOSHibernateException {
        String hql = String.format("from %s where name = :name", DBITEM_REPORT_VARIABLES);
        Query<DBItemReportVariable> query = getSession().createQuery(hql);
        query.setParameter("name", name);
        return getSession().getSingleResult(query);
    }

    public DBItemReportVariable insertReportVariable(String name, Long numericValue, String textValue) throws SOSHibernateException {
        DBItemReportVariable item = new DBItemReportVariable();
        item.setName(name);
        item.setNumericValue(numericValue);
        item.setTextValue(textValue);
        getSession().save(item);
        return item;
    }

    public String getInventoryJobChainStartCause(String schedulerId, String schedulerHostname, int schedulerHttpPort, String name) {
        try {
            StringBuilder hql = new StringBuilder("select");
            hql.append(" ijc.startCause");
            hql.append(" from ");
            hql.append(DBITEM_INVENTORY_JOB_CHAINS).append(" ijc,");
            hql.append(DBITEM_INVENTORY_INSTANCES).append(" ii");
            hql.append(" where ijc.name = :name");
            hql.append(" and ii.schedulerId = :schedulerId");
            hql.append(" and ii.port = :schedulerHttpPort");
            hql.append(" and upper(ii.hostname) = :schedulerHostname");
            hql.append(" and ii.id = ijc.instanceId");
            Query<?> query = getSession().createQuery(hql.toString());
            query.setParameter("schedulerId", schedulerId);
            query.setParameter("schedulerHostname", schedulerHostname.toUpperCase());
            query.setParameter("schedulerHttpPort", schedulerHttpPort);
            query.setParameter("name", name);

            return getSession().getSingleValueAsString(query);
        } catch (Exception ex) {
            LOGGER.warn(String.format("getInventoryJobChainStartCause: %s", ex.toString()), ex);
        }
        return null;
    }

    public ScrollableResults getResultsUncompletedTriggers(Optional<Integer> fetchSize, String schedulerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from " + DBITEM_REPORT_TRIGGERS);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and syncCompleted = true");
        hql.append(" and resultsCompleted = false");

        Query<DBItemReportTrigger> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        if (fetchSize.isPresent()) {
            query.setFetchSize(fetchSize.get());
        }
        return getSession().scroll(query);
    }

    public ScrollableResults getResultsUncompletedExecutions(Optional<Integer> fetchSize, String schedulerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from " + DBITEM_REPORT_EXECUTIONS);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and syncCompleted = true");
        hql.append(" and resultsCompleted = false");

        Query<DBItemReportTrigger> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        if (fetchSize.isPresent()) {
            query.setFetchSize(fetchSize.get());
        }
        return getSession().scroll(query);
    }

    public ScrollableResults getResultsUncompletedTasks(Optional<Integer> fetchSize, String schedulerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from " + DBITEM_REPORT_TASKS);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and syncCompleted = true");
        hql.append(" and resultsCompleted = false");

        Query<DBItemReportTrigger> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        if (fetchSize.isPresent()) {
            query.setFetchSize(fetchSize.get());
        }
        return getSession().scroll(query);
    }

    public Query<DBItemSchedulerHistory> getSchedulerHistoryTasksQuery(SOSHibernateSession schedulerSession, Optional<Integer> fetchSize,
            String schedulerId, List<Long> taskIds) throws SOSHibernateException {
        return this.getSchedulerHistoryTasksQuery(schedulerSession, fetchSize, schedulerId, null, null, taskIds);
    }

    public Query<DBItemSchedulerHistory> getSchedulerHistoryTasksQuery(SOSHibernateSession schedulerSession, Optional<Integer> fetchSize,
            String schedulerId, Date dateFrom, Date dateTo) throws SOSHibernateException {
        return this.getSchedulerHistoryTasksQuery(schedulerSession, fetchSize, schedulerId, dateFrom, dateTo, null);
    }

    public Query<DBItemSchedulerHistory> getSchedulerHistoryTasksQuery(SOSHibernateSession schedulerSession, Optional<Integer> fetchSize,
            String schedulerId, Date dateFrom, Date dateTo, List<Long> taskIds) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from " + DBItemSchedulerHistory.class.getSimpleName());
        hql.append(" where spoolerId = :schedulerId");
        if (dateTo != null) {
            hql.append(" and startTime <= :dateTo");
            if (dateFrom != null) {
                hql.append(" and startTime >= :dateFrom");
            }
        }
        if (taskIds != null && taskIds.size() > 0) {
            hql.append(" and id in :taskIds");
        }

        Query<DBItemSchedulerHistory> query = schedulerSession.createQuery(hql.toString());
        query.setReadOnly(true);
        query.setParameter("schedulerId", schedulerId);
        if (dateTo != null) {
            query.setParameter("dateTo", dateTo);
            if (dateFrom != null) {
                query.setParameter("dateFrom", dateFrom);
            }
        }
        if (taskIds != null && taskIds.size() > 0) {
            query.setParameterList("taskIds", taskIds);
        }
        if (fetchSize.isPresent()) {
            query.setFetchSize(fetchSize.get());
        }
        return query;
    }

    @SuppressWarnings("deprecation")
    public List<Map<String, String>> getInventoryJobInfoByJobName(String schedulerId, String schedulerHostname, int schedulerHttpPort, String jobName)
            throws SOSHibernateException {
        StringBuffer sql = new StringBuffer("select ");
        sql.append(quote("ij.NAME"));
        sql.append(" ," + quote("ij.TITLE"));
        sql.append(" ," + quote("ij.IS_RUNTIME_DEFINED"));
        sql.append(" , " + quote("ij.IS_ORDER_JOB"));
        sql.append(" from " + TABLE_INVENTORY_JOBS + " ij");
        sql.append(" ," + TABLE_INVENTORY_INSTANCES + " ii ");
        sql.append(" where ");
        sql.append(quote("ij.INSTANCE_ID") + "=" + quote("ii.ID"));
        sql.append(" and " + quote("ii.SCHEDULER_ID") + "= :schedulerId");
        sql.append(" and upper(" + quote("ii.HOSTNAME") + ")= :schedulerHostname");
        sql.append(" and " + quote("ii.PORT") + "= :schedulerHttpPort");
        sql.append(" and " + quote("ij.NAME") + "= :jobName");

        NativeQuery<?> query = getSession().createNativeQuery(sql.toString());
        query.setReadOnly(true);
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("schedulerHostname", schedulerHostname.toUpperCase());
        query.setParameter("schedulerHttpPort", schedulerHttpPort);
        query.setParameter("jobName", ReportUtil.normalizeDbItemPath(jobName));
        return getSession().getResultListAsStringMaps(query);
    }

    @SuppressWarnings("deprecation")
    public List<Map<String, String>> getInventoryJobInfoByJobChain(String schedulerId, String schedulerHostname, int schedulerHttpPort,
            String jobChainName, String stepState) throws SOSHibernateException {
        StringBuffer sql = new StringBuffer("select ");
        sql.append(quote("ij.NAME"));
        sql.append(" ," + quote("ij.TITLE"));
        sql.append(" ," + quote("ij.IS_RUNTIME_DEFINED"));
        sql.append(" ," + quote("ij.IS_ORDER_JOB"));
        sql.append(" ," + quote("ii.CLUSTER_TYPE"));
        sql.append(" ," + quote("iacm.URL"));
        sql.append(" ," + quote("iacm.ORDERING"));
        sql.append(" from " + TABLE_INVENTORY_JOB_CHAIN_NODES + " ijcn");
        sql.append(" left join " + TABLE_INVENTORY_JOB_CHAINS + " ijc");
        sql.append(" on " + quote("ijcn.JOB_CHAIN_ID") + "=" + quote("ijc.ID"));
        sql.append(" left join " + TABLE_INVENTORY_INSTANCES + " ii");
        sql.append(" on " + quote("ijcn.INSTANCE_ID") + "=" + quote("ii.ID"));
        sql.append(" left join " + TABLE_INVENTORY_JOBS + " ij");
        // sql.append(" on " + quote("ijcn.JOB_NAME") + "=" + quote("ij.NAME"));
        sql.append(" on " + quote("ijcn.JOB_ID") + "=" + quote("ij.ID"));
        sql.append(" and " + quote("ij.INSTANCE_ID") + "=" + quote("ii.ID"));
        sql.append(" left outer join " + TABLE_INVENTORY_PROCESS_CLASSES + " ipc");
        sql.append(" on " + quote("ij.PROCESS_CLASS_ID") + "=" + quote("ipc.ID"));
        sql.append(" left outer join " + TABLE_INVENTORY_AGENT_CLUSTER + " iac");
        sql.append(" on " + quote("iac.PROCESS_CLASS_ID") + "=" + quote("ipc.ID"));
        sql.append(" left outer join " + TABLE_INVENTORY_AGENT_CLUSTERMEMBERS + " iacm");
        sql.append(" on " + quote("iacm.AGENT_CLUSTER_ID") + "=" + quote("iac.ID"));
        sql.append(" and " + quote("iacm.INSTANCE_ID") + "=" + quote("ii.ID"));
        sql.append(" where");
        sql.append(" " + quote("ijcn.STATE") + "= :stepState");
        sql.append(" and " + quote("ijc.NAME") + "= :jobChainName");
        sql.append(" and " + quote("ii.SCHEDULER_ID") + "= :schedulerId");
        sql.append(" and upper(" + quote("ii.HOSTNAME") + ")= :schedulerHostname");
        sql.append(" and " + quote("ii.PORT") + "= :schedulerHttpPort");

        NativeQuery<?> query = getSession().createNativeQuery(sql.toString());
        query.setReadOnly(true);
        query.setParameter("stepState", stepState);
        query.setParameter("jobChainName", ReportUtil.normalizeDbItemPath(jobChainName));
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("schedulerHostname", schedulerHostname.toUpperCase());
        query.setParameter("schedulerHttpPort", schedulerHttpPort);
        return getSession().getResultListAsStringMaps(query);
    }

    @SuppressWarnings("deprecation")
    public List<Map<String, String>> getInventoryOrderInfoByJobChain(String schedulerId, String schedulerHostname, int schedulerHttpPort,
            String orderId, String jobChainName) throws SOSHibernateException {
        StringBuffer sql = new StringBuffer("select ");
        sql.append(quote("ijc.NAME"));
        sql.append(" ," + quote("ijc.TITLE"));
        sql.append(" ," + quote("io.IS_RUNTIME_DEFINED"));
        sql.append(" from " + TABLE_INVENTORY_ORDERS + " io");
        sql.append(" ," + TABLE_INVENTORY_JOB_CHAINS + " ijc");
        sql.append(" ," + TABLE_INVENTORY_INSTANCES + " ii ");
        sql.append(" where ");
        sql.append(quote("io.INSTANCE_ID") + "=" + quote("ii.ID"));
        sql.append(" and " + quote("ijc.INSTANCE_ID") + "=" + quote("ii.ID"));
        // sql.append(" and " + quote("io.JOB_CHAIN_NAME") + "=" + quote("ijc.NAME"));
        sql.append(" and " + quote("io.JOB_CHAIN_ID") + "=" + quote("ijc.ID"));
        sql.append(" and " + quote("ii.SCHEDULER_ID") + "= :schedulerId");
        sql.append(" and upper(" + quote("ii.HOSTNAME") + ")= :schedulerHostname");
        sql.append(" and " + quote("ii.PORT") + "= :schedulerHttpPort");
        sql.append(" and " + quote("io.ORDER_ID") + "= :orderId");
        sql.append(" and " + quote("io.JOB_CHAIN_NAME") + "= :jobChainName");

        NativeQuery<?> query = getSession().createNativeQuery(sql.toString());
        query.setReadOnly(true);
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("schedulerHostname", schedulerHostname.toUpperCase());
        query.setParameter("schedulerHttpPort", schedulerHttpPort);
        query.setParameter("orderId", orderId);
        query.setParameter("jobChainName", ReportUtil.normalizeDbItemPath(jobChainName));
        return getSession().getResultListAsStringMaps(query);
    }

    public Long getCountSchedulerHistoryTasks(SOSHibernateSession schedulerSession, String schedulerId, Date dateFrom) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select count(id) from ");
        hql.append(SchedulerTaskHistoryDBItem.class.getSimpleName());
        hql.append(" where spoolerId =:schedulerId");
        hql.append(" and startTime >=:dateFrom");

        Query<Long> query = schedulerSession.createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("dateFrom", dateFrom);
        return schedulerSession.getSingleValue(query);
    }

    public Query<DBItemSchedulerHistoryOrderStepReporting> getSchedulerHistoryOrderStepsQuery(SOSHibernateSession schedulerSession,
            Optional<Integer> fetchSize, String schedulerId, Date dateFrom, Date dateTo) throws SOSHibernateException {
        return this.getSchedulerHistoryOrderStepsQuery(schedulerSession, fetchSize, schedulerId, dateFrom, dateTo, null);
    }

    public Query<DBItemSchedulerHistoryOrderStepReporting> getSchedulerHistoryOrderStepsQuery(SOSHibernateSession schedulerSession,
            Optional<Integer> fetchSize, String schedulerId, List<Long> orderHistoryIds) throws SOSHibernateException {
        return this.getSchedulerHistoryOrderStepsQuery(schedulerSession, fetchSize, schedulerId, null, null, orderHistoryIds);
    }

    @SuppressWarnings("deprecation")
    public Query<DBItemSchedulerHistoryOrderStepReporting> getSchedulerHistoryOrderStepsQuery(SOSHibernateSession schedulerSession,
            Optional<Integer> fetchSize, String schedulerId, Date dateFrom, Date dateTo, List<Long> orderHistoryIds) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select");
        // select field list osh
        hql.append(" osh.id.step       as stepStep");
        hql.append(",osh.id.historyId  as stepHistoryId");
        hql.append(",osh.taskId        as stepTaskId");
        hql.append(",osh.startTime     as stepStartTime");
        hql.append(",osh.endTime       as stepEndTime");
        hql.append(",osh.state         as stepState");
        hql.append(",osh.error         as stepError");
        hql.append(",osh.errorCode     as stepErrorCode");
        hql.append(",osh.errorText     as stepErrorText");
        // select field list oh
        hql.append(",oh.historyId      as orderHistoryId");
        hql.append(",oh.spoolerId      as orderSchedulerId");
        hql.append(",oh.orderId        as orderId");
        hql.append(",oh.cause          as orderTitle");
        hql.append(",oh.jobChain       as orderJobChain");
        hql.append(",oh.state          as orderState");
        hql.append(",oh.stateText      as orderStateText");
        hql.append(",oh.startTime      as orderStartTime");
        hql.append(",oh.endTime        as orderEndTime");
        // select field list h
        hql.append(",h.id              as taskId");
        hql.append(",h.clusterMemberId as taskClusterMemberId");
        hql.append(",h.steps           as taskSteps");
        hql.append(",h.jobName         as taskJobName");
        hql.append(",h.exitCode        as taskExitCode");
        hql.append(",h.cause           as taskCause");
        hql.append(",h.agentUrl        as taskAgentUrl");
        hql.append(",h.startTime       as taskStartTime");
        hql.append(",h.endTime         as taskEndTime");
        hql.append(",h.error           as taskError");
        hql.append(",h.errorCode       as taskErrorCode");
        hql.append(",h.errorText       as taskErrorText");
        hql.append(" from " + SchedulerOrderStepHistoryDBItem.class.getSimpleName() + " osh");
        hql.append(" inner join osh.schedulerOrderHistoryDBItem oh");
        hql.append(" left outer join osh.schedulerTaskHistoryDBItem h");
        hql.append(" where oh.spoolerId = :schedulerId");
        int orderHistoryIdsSize = orderHistoryIds == null ? 0 : orderHistoryIds.size();
        if (dateTo != null) {
            hql.append(" and oh.startTime <= :dateTo");
            if (dateFrom != null) {
                hql.append(" and oh.startTime >= :dateFrom");
            }
        } else if (orderHistoryIdsSize > 0) {
            if (orderHistoryIdsSize > 1) {
                hql.append(" and oh.historyId in :orderHistoryIds");
            } else {
                hql.append(" and oh.historyId = :orderHistoryId");
            }
        }
        Query<DBItemSchedulerHistoryOrderStepReporting> query = schedulerSession.createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        if (dateTo != null) {
            query.setParameter("dateTo", dateTo);
            if (dateFrom != null) {
                query.setParameter("dateFrom", dateFrom);
            }
        } else if (orderHistoryIdsSize > 0) {
            if (orderHistoryIdsSize > 1) {
                query.setParameterList("orderHistoryIds", orderHistoryIds);
            } else {
                query.setParameter("orderHistoryId", orderHistoryIds.get(0));
            }
        }
        query.setResultTransformer(Transformers.aliasToBean(DBItemSchedulerHistoryOrderStepReporting.class));
        query.setReadOnly(true);
        if (fetchSize.isPresent()) {
            query.setFetchSize(fetchSize.get());
        }
        return query;
    }

    public DBItemReportTrigger getTrigger(String schedulerId, Long historyId) throws SOSHibernateException {
        String hql = String.format("from %s  where schedulerId=:schedulerId and historyId=:historyId", DBITEM_REPORT_TRIGGERS);
        Query<DBItemReportTrigger> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("historyId", historyId);
        return getSession().getSingleResult(query);
    }

    public DBItemReportTrigger getTrigger(Long triggerId) throws SOSHibernateException {
        String hql = String.format("from %s  where id=:triggerId", DBITEM_REPORT_TRIGGERS);
        Query<DBItemReportTrigger> query = getSession().createQuery(hql.toString());
        query.setParameter("triggerId", triggerId);
        return getSession().getSingleResult(query);
    }

    public DBItemReportExecution getExecution(String schedulerId, Long historyId, Long triggerId, Long step) throws SOSHibernateException {
        String hql = String.format("from %s  where schedulerId=:schedulerId and historyId=:historyId and triggerId=:triggerId and step=:step",
                DBITEM_REPORT_EXECUTIONS);
        Query<DBItemReportExecution> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("historyId", historyId);
        query.setParameter("triggerId", triggerId);
        query.setParameter("step", step);
        return getSession().getSingleResult(query);
    }

    public List<DBItemReportExecution> getExecutionsByTask(Long taskId) throws SOSHibernateException {
        String hql = String.format("from %s where taskId=:taskId", DBITEM_REPORT_EXECUTIONS);
        Query<DBItemReportExecution> query = getSession().createQuery(hql.toString());
        query.setParameter("taskId", taskId);
        return getSession().getResultList(query);
    }

    public int updateComplitedExecutionsByTask(DBItemReportTask reportTask) throws SOSHibernateException {
        String hql = String.format("update %s set exitCode=:exitCode  where taskId=:taskId and syncCompleted=true", DBITEM_REPORT_EXECUTIONS);
        Query<DBItemReportExecutionDate> query = getSession().createQuery(hql.toString());
        query.setParameter("exitCode", reportTask.getExitCode());
        query.setParameter("taskId", reportTask.getId());
        return getSession().executeUpdate(query);
    }

    public DBItemReportTask getTask(String schedulerId, Long historyId) throws SOSHibernateException {
        String hql = String.format("from %s  where schedulerId=:schedulerId and historyId=:historyId", DBITEM_REPORT_TASKS);
        Query<DBItemReportTask> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("historyId", historyId);
        return getSession().getSingleResult(query);
    }

    public DBItemReportExecutionDate getExecutionDate(EReferenceType type, Long id) throws SOSHibernateException {
        String hql = String.format("from %s  where referenceType=:referenceType and referenceId=:referenceId", DBITEM_REPORT_EXECUTION_DATES);
        Query<DBItemReportExecutionDate> query = getSession().createQuery(hql.toString());
        query.setParameter("referenceType", type.value());
        query.setParameter("referenceId", id);
        return getSession().getSingleResult(query);
    }

    public int removeExecutionDate(EReferenceType type, Long id) throws SOSHibernateException {
        String hql = String.format("delete from %s  where referenceType=:referenceType and referenceId=:referenceId", DBITEM_REPORT_EXECUTION_DATES);
        Query<DBItemReportExecutionDate> query = getSession().createQuery(hql.toString());
        query.setParameter("referenceType", type.value());
        query.setParameter("referenceId", id);
        return getSession().executeUpdate(query);
    }

    public int resetReportVariableLockVersion(String name) throws SOSHibernateException {
        String hql = String.format("update %s set lockVersion=0  where name=:name", DBITEM_REPORT_VARIABLES);
        Query<DBItemReportExecutionDate> query = getSession().createQuery(hql.toString());
        query.setParameter("name", name);
        return getSession().executeUpdate(query);
    }

    public Long getOrderEstimatedDuration(String jobChain, String orderId, int limit) throws SOSHibernateException {
        if (jobChain == null) {
            return null;
        }
        List<DBItemReportTrigger> result = null;
        String hql = String.format("from %s  where name=:orderId and parentName = :jobChain order by startTime desc", DBITEM_REPORT_TRIGGERS);
        LOGGER.debug(hql);
        Query<DBItemReportTrigger> query = getSession().createQuery(hql.toString());
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        query.setParameter("orderId", orderId);
        query.setParameter("jobChain", jobChain);
        result = getSession().getResultList(query);
        SOSDurations durations = new SOSDurations();

        if (result != null) {
            for (DBItemReportTrigger reportTrigger : result) {
                SOSDuration duration = new SOSDuration();
                duration.setStartTime(reportTrigger.getStartTime());
                duration.setEndTime(reportTrigger.getEndTime());
                durations.add(duration);
            }
        }
        if (durations.size() > 0) {
            return durations.average();
        }
        return 0L;

    }

    public Long getOrderEstimatedDuration(DBItemInventoryOrder order, int limit) throws SOSHibernateException {
        return getOrderEstimatedDuration(order.getJobChainName(), order.getOrderId(), limit);
    }

    public Long getTaskEstimatedDuration(String jobName, int limit) throws SOSHibernateException {
        String hql = String.format("from %s where error=0 and name = :jobName order by startTime desc", DBITEM_REPORT_TASKS);
        Query<DBItemReportTask> query = getSession().createQuery(hql);
        query.setParameter("jobName", jobName);
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<DBItemReportTask> result = getSession().getResultList(query);
        SOSDurations durations = new SOSDurations();

        if (result != null) {
            for (DBItemReportTask reportExecution : result) {
                SOSDuration duration = new SOSDuration();
                duration.setStartTime(reportExecution.getStartTime());
                duration.setEndTime(reportExecution.getEndTime());
                durations.add(duration);
            }
        }
        if (durations.size() > 0) {
            return durations.average();
        }
        return 0L;
    }

    private String quote(String fieldName) {
        return getSession().getFactory().quoteColumn(fieldName);
    }
}