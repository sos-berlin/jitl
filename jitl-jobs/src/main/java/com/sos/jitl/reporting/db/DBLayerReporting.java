package com.sos.jitl.reporting.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
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
import com.sos.jitl.reporting.helper.CounterRemove;
import com.sos.jitl.reporting.helper.EReferenceType;
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

    public DBItemReportTrigger createReportTrigger(String schedulerId, Long historyId, String name, String title, String parentFolder,
            String parentName, String parentBasename, String parentTitle, String state, String stateText, Date startTime, Date endTime,
            boolean synCompleted, boolean isRuntimeDefined, String resultStartCause, Long resultSteps, boolean resultError, String resultErrorCode,
            String resultErrorText) throws Exception {
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
            item.setResultStartCause(resultStartCause);
            item.setResultSteps(resultSteps);
            item.setResultError(resultError);
            item.setResultErrorCode(resultErrorCode);
            item.setResultErrorText(resultErrorText);
            item.setCreated(ReportUtil.getCurrentDateTime());
            item.setModified(ReportUtil.getCurrentDateTime());
            getSession().save(item);
            return item;
        } catch (Exception e) {
            throw new Exception(String.format("createReportTrigger: %s", e.toString()), e);
        }
    }

    public DBItemReportExecution createReportExecution(String schedulerId, Long historyId, Long triggerId, String clusterMemberId, Integer steps,
            Long step, String folder, String name, String basename, String title, Date startTime, Date endTime, String state, String cause,
            Integer exitCode, Boolean error, String errorCode, String errorText, String agentUrl, boolean synCompleted, boolean isRuntimeDefined)
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
        Criteria cr = getSession().createCriteria(DBItemReportExecution.class, new String[] { "id", "historyId" }, null);
        cr.add(Restrictions.eq("schedulerId", schedulerId));
        cr.add(Restrictions.eq("triggerId", new Long(0)));
        cr.add(Restrictions.eq("syncCompleted", false));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getOrderSyncUncomplitedIds(Optional<Integer> fetchSize, String schedulerId) throws Exception {
        Criteria cr = getSession().createCriteria(DBItemReportTrigger.class, new String[] { "id", "historyId" }, null);
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
            return getSession().createQuery(sql.toString()).executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public int removeExecutions() throws Exception {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("delete");
            sql.append(" from ");
            sql.append(DBITEM_REPORT_EXECUTIONS);
            sql.append(" where suspended = true");
            return getSession().createQuery(sql.toString()).executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
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
                q = getSession().createQuery(sql.toString());
                q.setParameter("schedulerId", schedulerId);
                q.setParameter("dateTo", dateTo);
                if (dateFrom != null) {
                    q.setParameter("dateFrom", dateFrom);
                }
                result = q.executeUpdate();
            }
            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public int setUncompletedTriggersAsRemoved(String schedulerId) throws Exception {
        try {
            StringBuilder sql = null;
            int result = 0;
            sql = new StringBuilder();
            sql.append("update ").append(DBITEM_REPORT_TRIGGERS);
            sql.append(" set suspended = true");
            sql.append(" where syncCompleted = false");
            sql.append(" and schedulerId=:schedulerId");

            Query q = getSession().createQuery(sql.toString());
            q.setParameter("schedulerId", schedulerId);
            result = q.executeUpdate();

            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public int setUncompletedTriggersAsRemoved(List<Long> ids) throws Exception {
        try {
            StringBuilder sql = null;
            int result = 0;
            sql = new StringBuilder();
            sql.append("update ").append(DBITEM_REPORT_TRIGGERS);
            sql.append(" set suspended = true");
            sql.append(" where id in :ids ");

            Query q = getSession().createQuery(sql.toString());
            q.setParameterList("ids", ids);
            result = q.executeUpdate();

            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public int setUncompletedStandaloneExecutionsAsRemoved(String schedulerId) throws Exception {
        try {
            StringBuilder sql = null;
            int result = 0;

            sql = new StringBuilder();
            sql.append("update ");
            sql.append(DBITEM_REPORT_EXECUTIONS);
            sql.append(" set suspended = true");
            sql.append(" where syncCompleted = false");
            sql.append(" and triggerId = 0");
            sql.append(" and schedulerId =:schedulerId");
            Query q = getSession().createQuery(sql.toString());
            q.setParameter("schedulerId", schedulerId);
            result = q.executeUpdate();

            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public int setUncompletedStandaloneExecutionsAsRemoved(List<Long> ids) throws Exception {
        try {
            StringBuilder sql = null;
            int result = 0;

            sql = new StringBuilder();
            sql.append("update ");
            sql.append(DBITEM_REPORT_EXECUTIONS);
            sql.append(" set suspended = true");
            sql.append(" where id in :ids ");
            Query q = getSession().createQuery(sql.toString());
            q.setParameterList("ids", ids);
            result = q.executeUpdate();
            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
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
            Query q = getSession().createQuery(sql.toString());
            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public int setStandaloneExecutionsAsRemoved(String schedulerId, Date dateFrom, Date dateTo) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("update ");
            sql.append(DBITEM_REPORT_EXECUTIONS + " ");
            sql.append("set suspended = true ");
            sql.append("where triggerId = 0 ");
            sql.append("and schedulerId = :schedulerId ");
            sql.append("and startTime <= :dateTo ");
            if (dateFrom != null) {
                sql.append(" and startTime >= :dateFrom ");
            }
            Query q = getSession().createQuery(sql.toString());
            q.setParameter("schedulerId", schedulerId);
            q.setParameter("dateTo", dateTo);
            if (dateFrom != null) {
                q.setParameter("dateFrom", dateFrom);
            }
            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
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
            Query q = getSession().createQuery(sql.toString());
            q.setParameter("referenceType", EReferenceType.TRIGGER.value());
            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
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
            Query q = getSession().createQuery(sql.toString());
            q.setParameter("referenceType", EReferenceType.EXECUTION.value());
            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
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

    public CounterRemove removeOrder(String schedulerId, Date dateFrom, Date dateTo) throws Exception {
        CounterRemove counter = new CounterRemove();
        try {
            getSession().beginTransaction();
            int markedAsRemoved = setTriggersAsRemoved(schedulerId, dateFrom, dateTo);
            getSession().commit();

            if (markedAsRemoved != 0) {
                getSession().beginTransaction();
                setOrderExecutionsAsRemoved();
                getSession().commit();

                getSession().beginTransaction();
                counter.setTriggerDates(removeTriggerDates());
                counter.setExecutionDates(removeExecutionDates());
                getSession().commit();

                getSession().beginTransaction();
                counter.setTriggers(removeTriggers());
                getSession().commit();

                getSession().beginTransaction();
                counter.setExecutions(removeExecutions());
                getSession().commit();
            }
        } catch (Exception e) {
            try {
                getSession().rollback();
            } catch (Exception ex) {
                LOGGER.warn(String.format("removeOrder: %s", ex.toString()), ex);
            }
            throw e;
        }

        return counter;
    }

    public CounterRemove removeStandalone(String schedulerId, Date dateFrom, Date dateTo) throws Exception {
        CounterRemove counter = new CounterRemove();
        try {
            getSession().beginTransaction();
            int markedAsRemoved = setStandaloneExecutionsAsRemoved(schedulerId, dateFrom, dateTo);
            getSession().commit();

            if (markedAsRemoved != 0) {
                getSession().beginTransaction();
                counter.setExecutionDates(removeExecutionDates());
                getSession().commit();

                getSession().beginTransaction();
                counter.setExecutions(removeExecutions());
                getSession().commit();
            }
        } catch (Exception e) {
            try {
                getSession().rollback();
            } catch (Exception ex) {
                LOGGER.warn(String.format("removeStandalone: %s", ex.toString()), ex);
            }
            throw e;
        }
        return counter;
    }

    public CounterRemove removeOrderUncompleted(String schedulerId, List<Long> ids) throws Exception {
        CounterRemove counter = new CounterRemove();
        try {
            getSession().beginTransaction();

            int markedAsRemoved = 0;
            if (ids == null) {
                markedAsRemoved = setUncompletedTriggersAsRemoved(schedulerId);
            } else {
                markedAsRemoved = setUncompletedTriggersAsRemoved(ids);
            }
            if (markedAsRemoved != 0) {
                setOrderExecutionsAsRemoved();
                counter.setTriggerDates(removeTriggerDates());
                counter.setExecutionDates(removeExecutionDates());
                counter.setTriggers(removeTriggers());
                counter.setExecutions(removeExecutions());
            }

            getSession().commit();
        } catch (Exception e) {
            try {
                getSession().rollback();
            } catch (Exception ex) {
                LOGGER.warn(String.format("removeOrderUncompleted: %s", ex.toString()), ex);
            }
            throw e;
        }
        return counter;
    }

    public CounterRemove removeStandaloneUncompleted(String schedulerId, List<Long> ids) throws Exception {
        CounterRemove counter = new CounterRemove();
        try {
            getSession().beginTransaction();

            int markedAsRemoved = 0;
            if (ids == null) {
                markedAsRemoved = setUncompletedStandaloneExecutionsAsRemoved(schedulerId);
            } else {
                markedAsRemoved = setUncompletedStandaloneExecutionsAsRemoved(ids);
            }
            if (markedAsRemoved != 0) {
                counter.setExecutionDates(removeExecutionDates());
                counter.setExecutions(removeExecutions());
            }

            getSession().commit();
        } catch (Exception e) {
            try {
                getSession().rollback();
            } catch (Exception ex) {
                LOGGER.warn(String.format("removeStandaloneUncompleted: %s", ex.toString()), ex);
            }
            throw e;
        }
        return counter;
    }

    public DBItemReportVariable createReportVariable(String name, Long numericValue, String textValue) throws Exception {
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

    public void updateReportVariable(DBItemReportVariable item) throws Exception {
        try {
            getSession().update(item);
        } catch (Exception e) {
            throw new Exception(String.format("updateReportVariable: %s", e.toString()), e);
        }
    }

    private String quote(String fieldName) {
        return getSession().getFactory().quoteFieldName(fieldName);
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

    public int triggerResultCompletedQuery(String schedulerId) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("update ");
            sql.append(DBITEM_REPORT_TRIGGERS);
            sql.append(" set resultsCompleted = true");
            sql.append(" where resultsCompleted = false");
            sql.append(" and syncCompleted = true");
            sql.append(" and schedulerId = :schedulerId");
            Query q = getSession().createQuery(sql.toString());
            q.setParameter("schedulerId", schedulerId);
            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
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
            Query q = getSession().createQuery(sql.toString());
            q.setParameter("schedulerId", schedulerId);
            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public Criteria getOrderResultsUncompletedTriggers(Optional<Integer> fetchSize, String schedulerId) throws Exception {
        String[] fields = new String[] { "id", "schedulerId", "historyId", "parentName", "startTime", "endTime" };
        Criteria cr = getSession().createCriteria(DBItemReportTrigger.class, fields);
        cr.add(Restrictions.eq("schedulerId", schedulerId));
        cr.add(Restrictions.eq("resultsCompleted", false));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getOrderResultsUncompletedExecutions(Optional<Integer> fetchSize, Long triggerId) throws Exception {
        String[] fields = new String[] { "id", "schedulerId", "historyId", "triggerId", "step", "name", "startTime", "endTime", "state", "cause",
                "error", "errorCode", "errorText" };
        Criteria cr = getSession().createCriteria(DBItemReportExecution.class, fields);
        cr.add(Restrictions.eq("triggerId", triggerId));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getStandaloneResultsUncompletedExecutions(Optional<Integer> fetchSize, String schedulerId) throws Exception {
        String[] fields = new String[] { "id", "schedulerId", "historyId", "triggerId", "step", "name", "startTime", "endTime", "state", "cause",
                "error", "errorCode", "errorText" };
        Criteria cr = getSession().createCriteria(DBItemReportExecution.class, fields);
        cr.add(Restrictions.eq("schedulerId", schedulerId));
        cr.add(Restrictions.eq("triggerId", new Long(0)));
        cr.add(Restrictions.eq("resultsCompleted", false));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getSchedulerHistoryTasks(SOSHibernateSession schedulerSession, Optional<Integer> fetchSize, String schedulerId, Date dateFrom,
            Date dateTo, List<Long> excludedTaskIds, ArrayList<Long> taskIds) throws Exception {

        Criteria cr = schedulerSession.createCriteria(DBItemSchedulerHistory.class);
        cr.add(Restrictions.eq("spoolerId", schedulerId));
        if (dateTo != null) {
            cr.add(Restrictions.le("startTime", dateTo));
            if (dateFrom != null) {
                cr.add(Restrictions.ge("startTime", dateFrom));
            }
        }
        if (excludedTaskIds != null && excludedTaskIds.size() > 0) {
            cr.add(Restrictions.not(SOSHibernateSession.createInCriterion("id", excludedTaskIds)));
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

    @SuppressWarnings("rawtypes")
    public List<Object[]> getInventoryJobInfo(String schedulerId, String schedulerHostname, int schedulerHttpPort, String jobChainName, String stepState)
            throws Exception {

        StringBuffer query = new StringBuffer("select");
        query.append(" " + quote("ii.SCHEDULER_ID"));
        query.append(" ," + quote("ii.HOSTNAME"));
        query.append(" ," + quote("ii.PORT"));
        query.append(" ," + quote("ii.CLUSTER_TYPE"));
        query.append(" ," + quote("ij.NAME"));
        query.append(" ," + quote("ij.TITLE"));
        query.append(" ," + quote("ij.IS_RUNTIME_DEFINED"));
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
        return executeQueryList(q); //results.isEmpty() ? null : (String) results.get(0);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<Object[]> getInventoryInfoForTrigger(String schedulerId, String schedulerHostname, int schedulerHttpPort, String orderId,
            String jobChainName) throws Exception {

        StringBuffer query = new StringBuffer("select ");
        query.append(quote("ijc.TITLE"));
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<Object[]> getInventoryInfoForExecution(String schedulerId, String schedulerHostname, int schedulerHttpPort, String jobName,
            boolean isOrderJob) throws Exception {

        StringBuffer query = new StringBuffer("select ");
        query.append(quote("ij.TITLE"));
        query.append(" ," + quote("ij.IS_RUNTIME_DEFINED"));
        query.append(" from " + TABLE_INVENTORY_JOBS + " ij");
        query.append(" ," + TABLE_INVENTORY_INSTANCES + " ii ");
        query.append(" where ");
        query.append(quote("ij.INSTANCE_ID") + "=" + quote("ii.ID"));
        query.append(" and " + quote("ii.SCHEDULER_ID") + "= :schedulerId");
        query.append(" and upper(" + quote("ii.HOSTNAME") + ")= :schedulerHostname");
        query.append(" and " + quote("ii.PORT") + "= :schedulerHttpPort");
        query.append(" and " + quote("ij.NAME") + "= :jobName");
        if (isOrderJob) {
            query.append(" and " + quote("ij.IS_ORDER_JOB") + "= 1");
        }
        
        NativeQuery q = getSession().createNativeQuery(query.toString());
        q.setReadOnly(true);
        q.setParameter("schedulerId", schedulerId);
        q.setParameter("schedulerHostname", schedulerHostname.toUpperCase());
        q.setParameter("schedulerHttpPort", schedulerHttpPort);
        q.setParameter("jobName", ReportUtil.normalizeDbItemPath(jobName));
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

    @SuppressWarnings("unchecked")
    public DBItemSchedulerOrderStepHistory getSchedulerOrderHistoryLastStep(SOSHibernateSession schedulerSession, Long historyId) throws Exception {
        StringBuffer query = new StringBuffer("from ");
        query.append(DBItemSchedulerOrderStepHistory.class.getSimpleName() + " osh1 ");
        query.append("where osh1.id.historyId = :historyId ");
        query.append("and osh1.id.step = (");
        query.append("select max(osh2.id.step) from ");
        query.append(DBItemSchedulerOrderStepHistory.class.getSimpleName() + " osh2 ");
        query.append("where osh2.id.historyId = :historyId ");
        query.append(") ");

        Query<DBItemSchedulerOrderStepHistory> q = schedulerSession.createQuery(query.toString());
        q.setParameter("historyId", historyId);
        q.setReadOnly(true);

        List<DBItemSchedulerOrderStepHistory> result = q.getResultList();
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
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
            Date dateFrom, Date dateTo, List<Long> orderHistoryIds, ArrayList<Long> taskHistoryIds) throws Exception {

        int orderHistoryIdsSize = orderHistoryIds == null ? 0 : orderHistoryIds.size();
        int taskHistoryIdsSize = taskHistoryIds == null ? 0 : taskHistoryIds.size();

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
        } else if (taskHistoryIdsSize > 0) {
            if (taskHistoryIdsSize > 1) {
                cr.add(Restrictions.in("h.id", taskHistoryIds));
            } else {
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
    public DBItemReportTrigger getTrigger(String schedulerId, Long orderHistoryId) throws Exception {
        String sql = String.format("from %s  where schedulerId=:schedulerId and historyId=:historyId", DBITEM_REPORT_TRIGGERS);
        Query<DBItemReportTrigger> query = getSession().createQuery(sql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("historyId", orderHistoryId);

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
        // from Table REPORT_EXECUTIONS
        jobName = jobName.replaceFirst("^/", "");
        try {
            List<DBItemReportExecution> result = null;
            String sql = String.format("from %s where error=0 and name = :jobName order by startTime desc", DBITEM_REPORT_EXECUTIONS);
            LOGGER.debug(sql);
            Query<DBItemReportExecution> query = getSession().createQuery(sql);
            query.setParameter("jobName", jobName);
            if (limit > 0) {
                query.setMaxResults(limit);
            }
            result = query.getResultList();
            SOSDurations durations = new SOSDurations();
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
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }
}