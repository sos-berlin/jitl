package com.sos.jitl.reporting.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateConnection.Dbms;
import com.sos.jitl.reporting.helper.EReferenceType;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.scheduler.db.SchedulerInstancesDBItem;
import com.sos.scheduler.history.db.SchedulerOrderStepHistoryDBItem;

public class DBLayerReporting extends DBLayer {

    final Logger logger = LoggerFactory.getLogger(DBLayerReporting.class);

    public DBLayerReporting(SOSHibernateConnection conn) {
        super(conn);
    }

    public DBItemInventoryInstance getInventoryInstance(String schedulerId, String schedulerHost, Long schedulerPort) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_INSTANCES);
            sql.append(" where upper(schedulerId) = :schedulerId");
            sql.append(" and upper(hostname) = :hostname");
            sql.append(" and port = :port");
            sql.append(" order by id asc");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId.toUpperCase());
            query.setParameter("hostname", schedulerHost.toUpperCase());
            query.setParameter("port", schedulerPort);

            List<DBItemInventoryInstance> result = query.list();
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }


    public DBItemInventoryInstance createInventoryInstance(String schedulerId, String schedulerHost, Integer schedulerPort, String configurationDirectory)
            throws Exception {
        try {
            DBItemInventoryInstance item = new DBItemInventoryInstance();
            item.setSchedulerId(schedulerId);
            item.setHostname(schedulerHost);
            item.setPort(schedulerPort);
            item.setLiveDirectory(configurationDirectory);
            item.setCreated(ReportUtil.getCurrentDateTime());
            item.setModified(ReportUtil.getCurrentDateTime());
            getConnection().save(item);
            return item;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemInventoryFile createInventoryFile(Long instanceId, String fileType, String fileName, String fileBasename, String fileDirectory,
            Date fileCreated, Date fileModified, Date fileLocalCreated, Date fileLocalModified) throws Exception {
        try {
            DBItemInventoryFile item = new DBItemInventoryFile();
            item.setInstanceId(instanceId);
            item.setFileType(fileType);
            item.setFileName(fileName);
            item.setFileBaseName(fileBasename);
            item.setFileDirectory(fileDirectory);
            item.setFileCreated(fileCreated);
            item.setFileModified(fileModified);
            item.setFileLocalCreated(fileLocalCreated);
            item.setFileLocalModified(fileLocalModified);
            item.setCreated(ReportUtil.getCurrentDateTime());
            item.setModified(ReportUtil.getCurrentDateTime());
            getConnection().save(item);
            return item;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemInventoryOrder createInventoryOrder(Long instanceId, Long fileId, String jobChainName, String name, String basename, String orderId,
            String title, boolean isRuntimeDefined) throws Exception {
        try {
            DBItemInventoryOrder item = new DBItemInventoryOrder();
            item.setInstanceId(instanceId);
            item.setFileId(fileId);
            item.setJobChainName(jobChainName);
            item.setName(name);
            item.setBaseName(basename);
            item.setOrderId(orderId);
            item.setTitle(title);
            item.setIsRuntimeDefined(isRuntimeDefined);
            item.setCreated(ReportUtil.getCurrentDateTime());
            item.setModified(ReportUtil.getCurrentDateTime());
            getConnection().save(item);
            return item;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemInventoryJobChain createInventoryJobChain(Long instanceId, Long fileId, String startCause, String name, String basename, String title)
            throws Exception {
        try {
            DBItemInventoryJobChain item = new DBItemInventoryJobChain();
            item.setInstanceId(instanceId);
            item.setFileId(fileId);
            item.setStartCause(startCause);
            item.setName(name);
            item.setBaseName(basename);
            item.setTitle(title);
            item.setCreated(ReportUtil.getCurrentDateTime());
            item.setModified(ReportUtil.getCurrentDateTime());
            getConnection().save(item);
            return item;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemInventoryJobChainNode createInventoryJobChainNode(Long instanceId, Long jobChainId, String jobName, Long ordering, String name,
            String state, String nextState, String errorState, String job) throws Exception {
        try {
            DBItemInventoryJobChainNode item = new DBItemInventoryJobChainNode();
            item.setInstanceId(instanceId);
            item.setJobChainId(jobChainId);
            item.setJobName(jobName);
            item.setOrdering(ordering);
            item.setName(name);
            item.setState(state);
            item.setNextState(nextState);
            item.setErrorState(errorState);
            item.setJob(job);
            item.setCreated(ReportUtil.getCurrentDateTime());
            item.setModified(ReportUtil.getCurrentDateTime());
            getConnection().save(item);
            return item;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemInventoryJob createInventoryJob(Long instanceId, Long fileId, String name, String basename, String title, boolean isOrderJob,
            boolean isRuntimeDefined) throws Exception {
        try {
            DBItemInventoryJob item = new DBItemInventoryJob();
            item.setInstanceId(instanceId);
            item.setFileId(fileId);
            item.setName(name);
            item.setBaseName(basename);
            item.setTitle(title);
            item.setIsOrderJob(isOrderJob);
            item.setIsRuntimeDefined(isRuntimeDefined);
            item.setCreated(ReportUtil.getCurrentDateTime());
            item.setModified(ReportUtil.getCurrentDateTime());
            getConnection().save(item);
            return item;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int updateInventoryLiveDirectory(Long instanceId, String liveDirectory) throws Exception {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("update ");
            sql.append(DBITEM_INVENTORY_INSTANCES);
            sql.append(" set liveDirectory = :liveDirectory");
            sql.append(" where id = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            query.setParameter("liveDirectory", liveDirectory);
            return query.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }

    }

    public void cleanupInventory(Long instanceId) throws Exception {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ");
            sql.append(DBITEM_INVENTORY_ORDERS);
            sql.append(" where instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            int r = query.executeUpdate();
            sql = new StringBuilder();
            sql.append("delete from ");
            sql.append(DBITEM_INVENTORY_JOB_CHAIN_NODES);
            sql.append(" where instanceId = :instanceId");
            query = getConnection().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            r = query.executeUpdate();
            sql = new StringBuilder();
            sql.append("delete from ");
            sql.append(DBITEM_INVENTORY_JOB_CHAINS);
            sql.append(" where instanceId = :instanceId");
            query = getConnection().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            r = query.executeUpdate();
            sql = new StringBuilder();
            sql.append("delete from ");
            sql.append(DBITEM_INVENTORY_JOBS);
            sql.append(" where instanceId = :instanceId");
            query = getConnection().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            r = query.executeUpdate();
            // DBITEM_INVENTORY_FILES
            sql = new StringBuilder();
            sql.append("delete from ");
            sql.append(DBITEM_INVENTORY_FILES);
            sql.append(" where instanceId = :instanceId");
            query = getConnection().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            r = query.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemReportTrigger createReportTrigger(String schedulerId, Long historyId, String name, String title, String parentName,
            String parentBasename, String parentTitle, String state, String stateText, Date startTime, Date endTime, boolean synCompleted)
            throws Exception {
        try {
            DBItemReportTrigger item = new DBItemReportTrigger();
            item.setSchedulerId(schedulerId);
            item.setHistoryId(historyId);
            item.setName(name);
            item.setTitle(title);
            item.setParentName(parentName);
            item.setParentBasename(parentBasename);
            item.setParentTitle(parentTitle);
            item.setState(state);
            item.setStateText(stateText);
            item.setStartTime(startTime);
            item.setEndTime(endTime);
            item.setSyncCompleted(synCompleted);
            item.setIsRuntimeDefined(false);
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

    public Criteria getSyncUncomplitedReportTriggerHistoryIds(Optional<Integer> fetchSize, ArrayList<String> schedulerIds) throws Exception {
        Criteria cr = getConnection().createSingleListCriteria(DBItemReportTrigger.class, "historyId");
        Criterion cr1 = Restrictions.in("schedulerId", schedulerIds);
        Criterion cr2 = Restrictions.eq("syncCompleted", false);
        Criterion where = Restrictions.and(cr1, cr2);
        cr.add(where);
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getSyncUncomplitedReportTriggerAndHistoryIds(Optional<Integer> fetchSize, ArrayList<String> schedulerIds) throws Exception {
        Criteria cr = getConnection().createCriteria(DBItemReportTrigger.class, new String[] { "id", "historyId" }, null);
        Criterion cr1 = Restrictions.in("schedulerId", schedulerIds);
        Criterion cr2 = Restrictions.eq("syncCompleted", false);
        Criterion where = Restrictions.and(cr1, cr2);
        cr.add(where);
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getSchedulerInstancesSchedulerIds(SOSHibernateConnection schedulerConnection, Optional<Integer> fetchSize) throws Exception {
        Criteria cr = schedulerConnection.createSingleListCriteria(SchedulerInstancesDBItem.class, "schedulerId");
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public int removeReportingTriggers() throws Exception {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ").append(DBITEM_REPORT_TRIGGERS);
            sql.append(" where suspended = true");
            return getConnection().createQuery(sql.toString()).executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int removeReportingExecutions() throws Exception {
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

    public int setReportingTriggersAsRemoved(List<?> schedulerIds, Date dateFrom, Date dateTo) throws Exception {
        try {
            StringBuilder sql = null;
            Query q = null;
            int result = 0;
            if (schedulerIds != null && !schedulerIds.isEmpty()) {
                sql = new StringBuilder();
                sql.append("update ").append(DBITEM_REPORT_TRIGGERS);
                sql.append(" set suspended = true");
                sql.append(" where schedulerId in :schedulerId");
                sql.append(" and startTime <= :dateTo");
                if (dateFrom != null) {
                    sql.append(" and startTime >= :dateFrom");
                }
                q = getConnection().createQuery(sql.toString());
                q.setParameterList("schedulerId", schedulerIds);
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

    public int setReportingTriggersAsRemoved(List<Long> ids) throws Exception {
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

    public int setReportingExecutionsAsRemoved() throws Exception {
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

    public int removeReportingTriggerResults() throws Exception {
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

    public int removeReportingExecutionDates() throws Exception {
        try {
            int result;
            StringBuilder sql;
            Query q;
            sql = new StringBuilder("delete from ");
            sql.append(DBITEM_REPORT_EXECUTION_DATES);
            sql.append(" where referenceType = :referenceType");
            sql.append(" and referenceId in");
            sql.append(" (select id from ");
            sql.append(DBITEM_REPORT_TRIGGERS);
            sql.append(" where suspended = true)");
            q = getConnection().createQuery(sql.toString());
            q.setParameter("referenceType", EReferenceType.TRIGGER.value());
            result = q.executeUpdate();
            sql = new StringBuilder("delete from ");
            sql.append(DBITEM_REPORT_EXECUTION_DATES);
            sql.append(" where referenceType = :referenceType");
            sql.append(" and referenceId in");
            sql.append(" (select id from ");
            sql.append(DBITEM_REPORT_EXECUTIONS);
            sql.append(" where suspended = true)");
            q = getConnection().createQuery(sql.toString());
            q.setParameter("referenceType", EReferenceType.EXECUTION.value());
            result += q.executeUpdate();
            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

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

    public String getInventoryJobChainStartCause(String schedulerId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("select");
            sql.append(" ijc.startCause");
            sql.append(" from ");
            sql.append(DBITEM_INVENTORY_JOB_CHAINS).append(" ijc,");
            sql.append(DBITEM_INVENTORY_INSTANCES).append(" ii");
            sql.append(" where ijc.name = :name");
            sql.append(" and ii.schedulerId = :schedulerId");
            sql.append(" and ii.id = ijc.instanceId");
            Query q = getConnection().createQuery(sql.toString());
            q.setParameter("schedulerId", schedulerId);
            q.setParameter("name", name);
            return (String) q.uniqueResult();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int updateReportingExecutionFromInventory(boolean updateOnlyResultUncompletedEntries) throws Exception {
        String method = "updateReportingExecutionFromInventory";
        try {
            StringBuilder sql = null;
            int result = -1;
            Enum<SOSHibernateConnection.Dbms> dbms = getConnection().getDbms();
            // DB2 not tested
            if (dbms.equals(Dbms.ORACLE) || dbms.equals(Dbms.DB2)) {
                sql = new StringBuilder("update ");
                sql.append(TABLE_REPORT_EXECUTIONS).append(" re");
                sql.append(" set (");
                sql.append(quote("re.TITLE"));
                sql.append(" ,").append(quote("re.IS_RUNTIME_DEFINED"));
                sql.append(" ) = (");
                sql.append(" select ");
                sql.append(quote("ij.TITLE"));
                sql.append(" ,").append(quote("ij.IS_RUNTIME_DEFINED"));
                sql.append(" from ").append(TABLE_INVENTORY_JOBS).append(" ij");
                sql.append(" ,").append(TABLE_INVENTORY_INSTANCES).append(" ii");
                sql.append(" ,").append(TABLE_REPORT_TRIGGERS).append(" rt");
                sql.append(" where ").append(quote("ij.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ").append(quote("re.SCHEDULER_ID"));
                sql.append(" = ").append(quote("ii.SCHEDULER_ID"));
                sql.append(" and ").append(quote("re.TRIGGER_ID"));
                sql.append(" = ").append(quote("rt.ID"));
                sql.append(" and ").append(quote("re.NAME"));
                sql.append(" = ").append(quote("ij.NAME"));
                if (updateOnlyResultUncompletedEntries) {
                    sql.append(" and ").append(quote("rt.RESULTS_COMPLETED")).append(" = 0");
                }
                sql.append(" )");
                sql.append(" where exists(");
                sql.append(" select ");
                sql.append(quote("ij.TITLE"));
                sql.append(" ,").append(quote("ij.IS_RUNTIME_DEFINED"));
                sql.append(" from ").append(TABLE_INVENTORY_JOBS).append(" ij");
                sql.append(" ,").append(TABLE_INVENTORY_INSTANCES).append(" ii");
                sql.append(" ,").append(TABLE_REPORT_TRIGGERS).append(" rt");
                sql.append(" where ").append(quote("ij.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ").append(quote("re.SCHEDULER_ID"));
                sql.append(" = ").append(quote("ii.SCHEDULER_ID"));
                sql.append(" and ").append(quote("re.TRIGGER_ID"));
                sql.append(" = ").append(quote("rt.ID"));
                sql.append(" and ").append(quote("re.NAME"));
                sql.append(" = ").append(quote("ij.NAME"));
                if (updateOnlyResultUncompletedEntries) {
                    sql.append(" and ").append(quote("rt.RESULTS_COMPLETED")).append(" = 0");
                }
                sql.append(" )");
            } else if (dbms.equals(Dbms.MSSQL)) {
                sql = new StringBuilder("update ");
                sql.append(TABLE_REPORT_EXECUTIONS);
                sql.append(" set ").append(quote(TABLE_REPORT_EXECUTIONS + ".TITLE"));
                sql.append(" = ").append(quote("ij.TITLE"));
                sql.append(" ,");
                sql.append(quote(TABLE_REPORT_EXECUTIONS + ".IS_RUNTIME_DEFINED"));
                sql.append(" = ").append(quote("ij.IS_RUNTIME_DEFINED"));
                sql.append(" from ").append(TABLE_REPORT_EXECUTIONS).append(" re");
                sql.append(" ,").append(TABLE_INVENTORY_JOBS).append(" ij");
                sql.append(" ,").append(TABLE_INVENTORY_INSTANCES).append(" ii");
                sql.append(" ,").append(TABLE_REPORT_TRIGGERS).append(" rt");
                sql.append(" where ").append(quote("ij.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ").append(quote("re.SCHEDULER_ID"));
                sql.append(" = ").append(quote("ii.SCHEDULER_ID"));
                sql.append(" and ").append(quote("re.TRIGGER_ID"));
                sql.append(" = ").append(quote("rt.ID"));
                sql.append(" and ").append(quote("re.NAME"));
                sql.append(" = ").append(quote("ij.NAME"));
                if (updateOnlyResultUncompletedEntries) {
                    sql.append(" and ").append(quote("rt.RESULTS_COMPLETED")).append(" = 0");
                }
            } else if (dbms.equals(Dbms.MYSQL)) {
                sql = new StringBuilder("update ");
                sql.append(TABLE_REPORT_EXECUTIONS).append(" re");
                sql.append(" ,").append(TABLE_INVENTORY_JOBS).append(" ij");
                sql.append(" ,").append(TABLE_INVENTORY_INSTANCES).append(" ii");
                sql.append(" ,").append(TABLE_REPORT_TRIGGERS).append(" rt");
                sql.append(" set ").append(quote("re.TITLE"));
                sql.append(" = ").append(quote("ij.TITLE"));
                sql.append(" ,").append(quote("re.IS_RUNTIME_DEFINED"));
                sql.append(" = ").append(quote("ij.IS_RUNTIME_DEFINED"));
                sql.append(" where ").append(quote("ij.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ").append(quote("re.SCHEDULER_ID"));
                sql.append(" = ").append(quote("ii.SCHEDULER_ID"));
                sql.append(" and ").append(quote("re.TRIGGER_ID"));
                sql.append(" = ").append(quote("rt.ID"));
                sql.append(" and ").append(quote("re.NAME"));
                sql.append(" = ").append(quote("ij.NAME"));
                if (updateOnlyResultUncompletedEntries) {
                    sql.append(" and ").append(quote("rt.RESULTS_COMPLETED")).append(" = 0");
                }
            } else if (dbms.equals(Dbms.PGSQL) || dbms.equals(Dbms.SYBASE)) {
                sql = new StringBuilder("update ");
                sql.append(TABLE_REPORT_EXECUTIONS);
                sql.append(" set ").append(quote("TITLE"));
                sql.append(" = ").append(quote("ij.TITLE"));
                sql.append(" ,").append(quote("IS_RUNTIME_DEFINED"));
                sql.append(" = ").append(quote("ij.IS_RUNTIME_DEFINED"));
                sql.append(" from ").append(TABLE_INVENTORY_JOBS).append(" ij");
                sql.append(" ,").append(TABLE_INVENTORY_INSTANCES).append(" ii");
                sql.append(" ,").append(TABLE_REPORT_TRIGGERS).append(" rt");
                sql.append(" where ").append(quote("ij.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ");
                sql.append(quote(TABLE_REPORT_EXECUTIONS + ".SCHEDULER_ID"));
                sql.append(" = ").append(quote("ii.SCHEDULER_ID"));
                sql.append(" and ");
                sql.append(quote(TABLE_REPORT_EXECUTIONS + ".TRIGGER_ID"));
                sql.append(" = ").append(quote("rt.ID"));
                sql.append(" and ").append(quote(TABLE_REPORT_EXECUTIONS + ".NAME"));
                sql.append(" = ").append(quote("ij.NAME"));
                if (updateOnlyResultUncompletedEntries) {
                    sql.append(" and ").append(quote("rt.RESULTS_COMPLETED")).append(" = 0");
                }
            } else {
                logger.warn(String.format("%s: not implemented for connection %s ", method, dbms.name()));
            }
            if (sql != null) {
                result = getConnection().createSQLQuery(sql.toString()).executeUpdate();
            }
            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int updateReportingTriggerFromInventory(boolean updateOnlyResultUncompletedEntries) throws Exception {
        String method = "updateReportingTriggerFromInventory";
        try {
            StringBuilder sql = null;
            int result = -1;
            Enum<SOSHibernateConnection.Dbms> dbms = getConnection().getDbms();
            if (dbms.equals(Dbms.ORACLE) || dbms.equals(Dbms.DB2)) {
                sql = new StringBuilder("update ");
                sql.append(TABLE_REPORT_TRIGGERS).append(" rt");
                sql.append(" set (");
                sql.append(quote("rt.TITLE"));
                sql.append(" ,").append(quote("rt.PARENT_TITLE"));
                sql.append(" ,").append(quote("rt.IS_RUNTIME_DEFINED"));
                sql.append(" ) = (");
                sql.append(" select ");
                sql.append(quote("io.TITLE"));
                sql.append(" ,").append(quote("ijc.TITLE"));
                sql.append(" ,").append(quote("io.IS_RUNTIME_DEFINED"));
                sql.append(" from ").append(TABLE_INVENTORY_ORDERS).append(" io");
                sql.append(" ,").append(TABLE_INVENTORY_JOB_CHAINS).append(" ijc");
                sql.append(" ,").append(TABLE_INVENTORY_INSTANCES).append(" ii");
                sql.append(" where ").append(quote("io.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ").append(quote("ijc.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID")).append(" ");
                sql.append(" and ").append(quote("rt.SCHEDULER_ID"));
                sql.append(" = ").append(quote("ii.SCHEDULER_ID"));
                sql.append(" and ").append(quote("rt.NAME"));
                sql.append(" = ").append(quote("io.ORDER_ID"));
                sql.append(" and ").append(quote("rt.PARENT_NAME"));
                sql.append(" = ").append(quote("io.JOB_CHAIN_NAME"));
                sql.append(" and ").append(quote("io.JOB_CHAIN_NAME"));
                sql.append(" = ").append(quote("ijc.NAME"));
                sql.append(" )");
                sql.append(" where exists(");
                sql.append(" select ");
                sql.append(quote("io.TITLE"));
                sql.append(" ,").append(quote("ijc.TITLE"));
                sql.append(" ,").append(quote("io.IS_RUNTIME_DEFINED"));
                sql.append(" ");
                sql.append(" from ").append(TABLE_INVENTORY_ORDERS).append(" io");
                sql.append(" , ").append(TABLE_INVENTORY_JOB_CHAINS).append(" ijc");
                sql.append(" , ").append(TABLE_INVENTORY_INSTANCES).append(" ii");
                sql.append(" where ").append(quote("io.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ").append(quote("ijc.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ").append(quote("rt.SCHEDULER_ID"));
                sql.append(" = ").append(quote("ii.SCHEDULER_ID"));
                sql.append(" and ").append(quote("rt.NAME"));
                sql.append(" = ").append(quote("io.ORDER_ID"));
                sql.append(" and ").append(quote("rt.PARENT_NAME"));
                sql.append(" = ").append(quote("io.JOB_CHAIN_NAME"));
                sql.append(" and ").append(quote("io.JOB_CHAIN_NAME"));
                sql.append(" = ").append(quote("ijc.NAME"));
                sql.append(" )");
                if (updateOnlyResultUncompletedEntries) {
                    sql.append(" and ").append(quote("rt.RESULTS_COMPLETED")).append(" = 0");
                }
            } else if (dbms.equals(Dbms.MSSQL)) {
                sql = new StringBuilder("update ");
                sql.append(TABLE_REPORT_TRIGGERS);
                sql.append(" set ");
                sql.append(quote(TABLE_REPORT_TRIGGERS + ".TITLE"));
                sql.append(" = ").append(quote("io.TITLE"));
                sql.append(" ,");
                sql.append(quote(TABLE_REPORT_TRIGGERS + ".PARENT_TITLE"));
                sql.append(" = ").append(quote("ijc.TITLE"));
                sql.append(" ,");
                sql.append(quote(TABLE_REPORT_TRIGGERS + ".IS_RUNTIME_DEFINED"));
                sql.append(" = ").append(quote("io.IS_RUNTIME_DEFINED"));
                sql.append(" from ").append(TABLE_REPORT_TRIGGERS).append(" rt");
                sql.append(" ,").append(TABLE_INVENTORY_ORDERS).append(" io");
                sql.append(" ,").append(TABLE_INVENTORY_JOB_CHAINS).append(" ijc");
                sql.append(" ,").append(TABLE_INVENTORY_INSTANCES).append(" ii");
                sql.append(" where ").append(quote("io.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ").append(quote("ijc.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ").append(quote("rt.SCHEDULER_ID"));
                sql.append(" = ").append(quote("ii.SCHEDULER_ID"));
                sql.append(" and ").append(quote("rt.NAME"));
                sql.append(" = ").append(quote("io.ORDER_ID"));
                sql.append(" and ").append(quote("rt.PARENT_NAME"));
                sql.append(" = ").append(quote("io.JOB_CHAIN_NAME"));
                sql.append(" and ").append(quote("io.JOB_CHAIN_NAME"));
                sql.append(" = ").append(quote("ijc.NAME"));
                if (updateOnlyResultUncompletedEntries) {
                    sql.append(" and ").append(quote("rt.RESULTS_COMPLETED")).append(" = 0");
                }
            } else if (dbms.equals(Dbms.MYSQL)) {
                sql = new StringBuilder("update ");
                sql.append(TABLE_REPORT_TRIGGERS).append(" rt");
                sql.append(" ,").append(TABLE_INVENTORY_ORDERS).append(" io");
                sql.append(" ,").append(TABLE_INVENTORY_JOB_CHAINS).append(" ijc");
                sql.append(" ,").append(TABLE_INVENTORY_INSTANCES).append(" ii");
                sql.append(" set ");
                sql.append(quote("rt.TITLE"));
                sql.append(" = ").append(quote("io.TITLE"));
                sql.append(" ,").append(quote("rt.PARENT_TITLE"));
                sql.append(" = ").append(quote("ijc.TITLE"));
                sql.append(" ,").append(quote("rt.IS_RUNTIME_DEFINED"));
                sql.append(" = ").append(quote("io.IS_RUNTIME_DEFINED"));
                sql.append(" where ").append(quote("io.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ").append(quote("ijc.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ").append(quote("rt.SCHEDULER_ID"));
                sql.append(" = ").append(quote("ii.SCHEDULER_ID"));
                sql.append(" and ").append(quote("rt.NAME"));
                sql.append(" = ").append(quote("io.ORDER_ID"));
                sql.append(" and ").append(quote("rt.PARENT_NAME"));
                sql.append(" = ").append(quote("io.JOB_CHAIN_NAME"));
                sql.append(" and ").append(quote("io.JOB_CHAIN_NAME"));
                sql.append(" = ").append(quote("ijc.NAME"));
                if (updateOnlyResultUncompletedEntries) {
                    sql.append(" and ").append(quote("rt.RESULTS_COMPLETED")).append(" = 0");
                }
            } else if (dbms.equals(Dbms.PGSQL) || dbms.equals(Dbms.SYBASE)) {
                sql = new StringBuilder("update ");
                sql.append(TABLE_REPORT_TRIGGERS);
                sql.append(" set ");
                sql.append(quote("TITLE"));
                sql.append(" = ").append(quote("io.TITLE"));
                sql.append(" ,").append(quote("PARENT_TITLE"));
                sql.append(" = ").append(quote("ijc.TITLE"));
                sql.append(" ,").append(quote("IS_RUNTIME_DEFINED"));
                sql.append(" = ").append(quote("io.IS_RUNTIME_DEFINED"));
                sql.append(" from ").append(TABLE_INVENTORY_ORDERS).append(" io");
                sql.append(" ,").append(TABLE_INVENTORY_JOB_CHAINS).append(" ijc");
                sql.append(" ,").append(TABLE_INVENTORY_INSTANCES).append(" ii");
                sql.append(" where ").append(quote("io.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ").append(quote("ijc.INSTANCE_ID"));
                sql.append(" = ").append(quote("ii.ID"));
                sql.append(" and ");
                sql.append(quote(TABLE_REPORT_TRIGGERS + ".SCHEDULER_ID"));
                sql.append(" = ").append(quote("ii.SCHEDULER_ID"));
                sql.append(" and ").append(quote(TABLE_REPORT_TRIGGERS + ".NAME"));
                sql.append(" = ").append(quote("io.ORDER_ID"));
                sql.append(" and ");
                sql.append(quote(TABLE_REPORT_TRIGGERS + ".PARENT_NAME"));
                sql.append(" = ").append(quote("io.JOB_CHAIN_NAME"));
                sql.append(" and ").append(quote("io.JOB_CHAIN_NAME"));
                sql.append(" = ").append(quote("ijc.NAME"));
                if (updateOnlyResultUncompletedEntries) {
                    sql.append(" and ");
                    sql.append(quote(TABLE_REPORT_TRIGGERS + ".RESULTS_COMPLETED"));
                    sql.append(" = 0");
                }
            } else {
                logger.warn(String.format("%s: not implemented for connection %s ", method, dbms.name()));
            }
            if (sql != null) {
                result = getConnection().createSQLQuery(sql.toString()).executeUpdate();
            }
            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public Criteria getReportExecutions(Optional<Integer> fetchSize, Long triggerId) throws Exception {
        Criteria cr = getConnection().createTransform2BeanCriteria(DBItemReportExecution.class);
        cr.add(Restrictions.eq("triggerId", triggerId));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public int triggerResultCompletedQuery() throws Exception {
        try {
            StringBuilder sql = new StringBuilder("update ");
            sql.append(DBITEM_REPORT_TRIGGERS);
            sql.append(" set resultsCompleted = true");
            sql.append(" where resultsCompleted = false");
            sql.append(" and syncCompleted = true");
            return getConnection().createQuery(sql.toString()).executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public Criteria getResultUncompletedTriggersCriteria(Optional<Integer> fetchSize) throws Exception {
        String[] fields = new String[] { "id", "schedulerId", "historyId", "parentName", "startTime", "endTime" };
        Criteria cr = getConnection().createCriteria(DBItemReportTrigger.class, fields);
        cr.add(Restrictions.eq("resultsCompleted", false));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getResultUncompletedTriggerExecutionsCriteria(Optional<Integer> fetchSize, Long triggerId) throws Exception {
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

    public Criteria getSchedulerHistorySteps(SOSHibernateConnection schedulerConnection, Optional<Integer> fetchSize, Date dateFrom, Date dateTo,
            ArrayList<Long> historyIds) throws Exception {
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
        pl.add(Projections.property("oh.jobChain").as("orderJobChain"));
        pl.add(Projections.property("oh.state").as("orderState"));
        pl.add(Projections.property("oh.stateText").as("orderStateText"));
        pl.add(Projections.property("oh.startTime").as("orderStartTime"));
        pl.add(Projections.property("oh.endTime").as("orderEndTime"));
        // select field list h
        pl.add(Projections.property("h.id").as("taskId"));
        pl.add(Projections.property("h.jobName").as("taskJobName"));
        pl.add(Projections.property("h.cause").as("taskCause"));
        pl.add(Projections.property("h.agentUrl").as("taskAgentUrl"));
        cr.setProjection(pl);
        // where
        if (dateTo != null) {
            cr.add(Restrictions.le("oh.startTime", dateTo));
            if (dateFrom != null) {
                cr.add(Restrictions.ge("oh.startTime", dateFrom));
            }
        } else if (historyIds != null) {
            cr.add(Restrictions.in("oh.historyId", historyIds));
        }
        cr.setResultTransformer(Transformers.aliasToBean(DBItemSchedulerHistoryOrderStepReporting.class));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

}