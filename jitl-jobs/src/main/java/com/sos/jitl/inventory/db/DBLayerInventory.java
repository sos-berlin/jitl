package com.sos.jitl.inventory.db;

import java.util.Date;
import java.util.List;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBItemInventoryAgentCluster;
import com.sos.jitl.reporting.db.DBItemInventoryAgentClusterMember;
import com.sos.jitl.reporting.db.DBItemInventoryAgentInstance;
import com.sos.jitl.reporting.db.DBItemInventoryAppliedLock;
import com.sos.jitl.reporting.db.DBItemInventoryFile;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryJob;
import com.sos.jitl.reporting.db.DBItemInventoryJobChain;
import com.sos.jitl.reporting.db.DBItemInventoryJobChainNode;
import com.sos.jitl.reporting.db.DBItemInventoryLock;
import com.sos.jitl.reporting.db.DBItemInventoryOrder;
import com.sos.jitl.reporting.db.DBItemInventoryProcessClass;
import com.sos.jitl.reporting.db.DBItemInventorySchedule;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.helper.ReportUtil;

@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class DBLayerInventory extends DBLayer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerInventory.class);

    public DBLayerInventory(SOSHibernateSession connection) {
        super(connection);
    }

    public DBItemInventoryInstance getInventoryInstance(String schedulerId, String schedulerHost, Integer schedulerPort) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_INSTANCES);
            sql.append(" where upper(schedulerId) = :schedulerId");
            sql.append(" and upper(hostname) = :hostname");
            sql.append(" and port = :port");
            sql.append(" order by id asc");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId.toUpperCase());
            query.setParameter("hostname", schedulerHost.toUpperCase());
            query.setParameter("port", schedulerPort);
            List<DBItemInventoryInstance> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public DBItemInventoryInstance getInventoryInstance(String schedulerHost, Integer schedulerPort) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_INSTANCES);
            sql.append(" where upper(hostname) = :hostname");
            sql.append(" and port = :port");
            sql.append(" order by id asc");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("hostname", schedulerHost.toUpperCase());
            query.setParameter("port", schedulerPort);
            List<DBItemInventoryInstance> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public DBItemInventoryInstance getInventoryInstance(Long id) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_INSTANCES);
            sql.append(" where id = :id");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("id", id);
            List<DBItemInventoryInstance> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }
    
    public DBItemInventoryInstance getInventoryInstance(String url) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_INSTANCES);
            sql.append(" where url = :url");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("url", url.toLowerCase());
            List<DBItemInventoryInstance> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }
    
    public DBItemInventoryInstance getInventorySupervisorInstance(String commandUrl) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_INSTANCES);
            sql.append(" where commandUrl = :commandUrl");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("commandUrl", commandUrl.toLowerCase());
            List<DBItemInventoryInstance> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }
    
    public DBItemInventoryJob getInventoryJob(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_JOBS);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryJob> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public DBItemInventoryJobChain getInventoryJobChain(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_JOB_CHAINS);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryJobChain> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public DBItemInventoryOrder getInventoryOrder(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_ORDERS);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryOrder> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public DBItemInventoryProcessClass getInventoryProcessClass(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_PROCESS_CLASSES);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryProcessClass> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public DBItemInventorySchedule getInventorySchedule(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_SCHEDULES);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventorySchedule> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public DBItemInventoryLock getInventoryLock(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_LOCKS);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryLock> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }
    
    public String getProcessClassName(Long instanceId, String basename) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_PROCESS_CLASSES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", basename);
        List<DBItemInventoryProcessClass> result = query.list();
        if(result != null && !result.isEmpty()){
            return result.get(0).getName();
        }
        return "";
    }
    
    public DBItemInventoryProcessClass getProcessClassIfExists(Long instanceId, String processClass, String processClassName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_PROCESS_CLASSES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        sql.append(" and name = :name");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", processClass);
        query.setParameter("name", processClassName);
        List<DBItemInventoryProcessClass> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public DBItemInventorySchedule getScheduleIfExists(Long instanceId, String schedule, String scheduleName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        sql.append(" and name = :name");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", schedule);
        query.setParameter("name", scheduleName);
        List<DBItemInventorySchedule> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }
    
    public DBItemInventoryFile getInventoryFile(Long instanceId, String fileName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_FILES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and fileName = :fileName");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("fileName", fileName);
        List<DBItemInventoryFile> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public DBItemInventorySchedule getSubstituteIfExists(String substitute, Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and name = :name");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("name", substitute);
        List<DBItemInventorySchedule> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }
    
    public Long saveOrUpdateSchedule(DBItemInventorySchedule newSchedule) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and fileId = :fileId");
        sql.append(" and name = :name");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", newSchedule.getInstanceId());
        query.setParameter("fileId", newSchedule.getFileId());
        query.setParameter("name", newSchedule.getName());
        List<DBItemInventorySchedule> result = query.list();
        if (result != null && !result.isEmpty()) {
            DBItemInventorySchedule classFromDb = result.get(0);
            classFromDb.setBasename(newSchedule.getBasename());
            classFromDb.setTitle(newSchedule.getTitle());
            classFromDb.setSubstitute(newSchedule.getSubstitute());
            classFromDb.setSubstituteId(newSchedule.getSubstituteId());
            classFromDb.setSubstituteName(newSchedule.getSubstituteName());
            classFromDb.setSubstituteValidFrom(newSchedule.getSubstituteValidFrom());
            classFromDb.setSubstituteValidTo(newSchedule.getSubstituteValidTo());
            classFromDb.setModified(ReportUtil.getCurrentDateTime());
            getSession().update(classFromDb);
            return classFromDb.getId();
        } else {
            newSchedule.setCreated(ReportUtil.getCurrentDateTime());
            newSchedule.setModified(ReportUtil.getCurrentDateTime());
            getSession().save(newSchedule);
            return newSchedule.getId();
        }
    }
    
    public Long getJobChainId(Long instanceId, String name) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("select id from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAINS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and name = :name");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("name", name);
        return (Long)query.uniqueResult();
    }
    
    public DBItemInventoryJob getJobIfExists(Long instanceId, String job, String jobName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOBS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and baseName = :baseName");
        sql.append(" and name = :name");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("baseName", job);
        query.setParameter("name", jobName);
        List<DBItemInventoryJob> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public String getJobChainName(Long instanceId, String basename) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAINS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and baseName = :baseName");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("baseName", basename);
        List<DBItemInventoryJobChain> result = query.list();
        if(result != null && !result.isEmpty()){
            return result.get(0).getName();
        }
        return "";
    }

    public DBItemInventoryJobChain getJobChain(Long instanceId, String name) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAINS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and name = :name");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("name", name);
        List<DBItemInventoryJobChain> result = query.getResultList();
        if(result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public DBItemInventoryJobChain getJobChainIfExists(Long instanceId, String jobChain, String jobChainName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAINS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and baseName = :baseName");
        sql.append(" and name = :name");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("baseName", jobChain);
        query.setParameter("name", jobChainName);
        List<DBItemInventoryJobChain> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public DBItemInventoryJobChainNode getJobChainNodeIfExists(Long instanceId, Long jobChainId, String state) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAIN_NODES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and jobChainId = :jobChainId");
        sql.append(" and state = :state");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("jobChainId", jobChainId);
        query.setParameter("state", state);
        List<DBItemInventoryJobChainNode> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public DBItemInventoryJobChainNode getJobChainNodeIfExists(Long instanceId, Long jobChainId, Integer nodeType, String state, String directory,
            String regex) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAIN_NODES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and jobChainId = :jobChainId");
        if (nodeType == 3) {
            sql.append(" and directory = :directory");
            if (regex != null) {
                sql.append(" and regex = :regex");
            }
        } else {
            sql.append(" and state = :state");
        }
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("jobChainId", jobChainId);
        if (nodeType == 3) {
            query.setParameter("directory", directory);
            if (regex != null) {
               query.setParameter("regex", regex); 
            }
        } else {
            query.setParameter("state", state);
        }
        List<DBItemInventoryJobChainNode> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public List<DBItemInventoryJobChainNode> getJobChainNodes(Long instanceId, Long jobChainId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAIN_NODES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and jobChainId = :jobChainId");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("jobChainId", jobChainId);
        return (List<DBItemInventoryJobChainNode>)query.list();
    }

    public List<DBItemInventoryJob> getAllJobsForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOBS);
        sql.append(" where instanceId = :instanceId");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryJob>)query.list();
    }
    
    public List<DBItemInventoryJobChain> getAllJobChainsForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAINS);
        sql.append(" where instanceId = :instanceId");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryJobChain>)query.list();
    }
    
    public List<DBItemInventoryJobChainNode> getAllJobChainNodesForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAIN_NODES);
        sql.append(" where instanceId = :instanceId");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryJobChainNode>)query.list();
    }
    
    public List<DBItemInventoryOrder> getAllOrdersForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_ORDERS);
        sql.append(" where instanceId = :instanceId");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryOrder>)query.list();
    }
    
    public List<DBItemInventoryProcessClass> getAllProcessClassesForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_PROCESS_CLASSES);
        sql.append(" where instanceId = :instanceId");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryProcessClass>)query.list();
    }
    
    public List<DBItemInventorySchedule> getAllSchedulesForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventorySchedule>)query.list();
    }
    
    public List<DBItemInventoryAppliedLock> getAllAppliedLocks() throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_APPLIED_LOCKS);
        Query query = getSession().createQuery(sql.toString());
        return (List<DBItemInventoryAppliedLock>)query.list();
    }
    
    public List<DBItemInventoryLock> getAllLocksForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_LOCKS);
        sql.append(" where instanceId = :instanceId");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryLock>)query.list();
    }
    
    public List<DBItemInventoryAgentCluster> getAllAgentClustersForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_AGENT_CLUSTER);
        sql.append(" where instanceId = :instanceId");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryAgentCluster>)query.list();
    }
    
    public List<DBItemInventoryAgentClusterMember> getAllAgentClusterMembersForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS);
        sql.append(" where instanceId = :instanceId");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryAgentClusterMember>)query.list();
    }
    
    public List<DBItemInventoryAgentInstance> getAllAgentInstancesForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_AGENT_INSTANCES);
        sql.append(" where instanceId = :instanceId");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryAgentInstance>)query.list();
    }
    
    public List<DBItemInventoryFile> getAllFilesForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_FILES);
        sql.append(" where instanceId = :instanceId");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryFile>)query.list();
    }
    
    public void refreshUsedInJobChains(Long instanceId, List<DBItemInventoryJob> jobs) throws Exception {
        for (DBItemInventoryJob job : jobs) {
            LOGGER.debug(String.format("refreshUsedInJobChains : job   id=%1$s    name=%2$s ", job.getId(), job.getName()));
            job.setUsedInJobChains(getUsedInJobChains(job.getId(), job.getInstanceId()));
            getSession().update(job);
        }
    }
    
    private Integer getUsedInJobChains(Long jobId, Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("select jobChainId from ");
        sql.append(DBLayer.DBITEM_INVENTORY_JOB_CHAIN_NODES);
        sql.append(" where instanceId = :instanceId");
//        sql.append(" and jobId is not null");
        sql.append(" and jobId = :jobId");
        sql.append(" group by jobChainId");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("jobId", jobId);
        query.setParameter("instanceId", instanceId);
        List<Object> jobChainIds = query.list();
        if(jobChainIds != null) {
            return jobChainIds.size();
        }
        return null;
    }
    
    public DBItemInventoryAgentInstance getInventoryAgentInstanceFromDb (String url, Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_AGENT_INSTANCES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and url = :url");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("url", url);
        List<DBItemInventoryAgentInstance> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public int deleteItemsFromDb(Date started, String tableName, Long instanceId) throws Exception {
        LOGGER.debug(String.format("delete: items from %2$s before = %1$s and instanceId = %3$d with query.executeUpdate()", started.toString(), tableName, instanceId));
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ");
        sql.append(tableName);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and modified < :modifiedDate");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("modifiedDate", started, TemporalType.TIMESTAMP);
        return query.executeUpdate();
    }
    
    public int deleteAppliedLocksFromDb(Date started, Long instanceId) throws Exception {
        LOGGER.debug(String.format("delete: appliedLocks before = %1$s  and instanceId = %2$d with query.executeUpdate()", started.toString(), instanceId));
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ");
        sql.append(DBLayer.DBITEM_INVENTORY_APPLIED_LOCKS).append(" appliedLocks ");
        sql.append("where appliedLocks.id in (select locks.id from ");
        sql.append(DBLayer.DBITEM_INVENTORY_LOCKS).append(" locks");
        sql.append(" where locks.instanceId = :instanceId");
        sql.append(" and locks.modified < :modified )");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("modified", started, TemporalType.TIMESTAMP);
        return query.executeUpdate();
    }
    
    public int deleteOldNodes(DBItemInventoryJobChain jobChain) throws Exception {
        LOGGER.debug(String.format("delete old JobChainNodes for JobChain = %1$s and instanceId = %2$d with query.executeUpdate()", jobChain.getName(), jobChain.getInstanceId()));
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ");
        sql.append(DBLayer.DBITEM_INVENTORY_JOB_CHAIN_NODES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and jobChainId = :jobChainId)");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("instanceId", jobChain.getInstanceId());
        query.setParameter("jobChainId", jobChain.getId());
        return query.executeUpdate();
    }
    
    public DBItemInventoryLock getLockByName(String name) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_LOCKS);
        sql.append(" where basename = :basename");
        Query query = getSession().createQuery(sql.toString());
        query.setParameter("basename", name);
        List<DBItemInventoryLock> result = query.list();
        if(result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public int updateInventoryLiveDirectory(Long instanceId, String liveDirectory) throws Exception {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("update ");
            sql.append(DBITEM_INVENTORY_INSTANCES);
            sql.append(" set liveDirectory = :liveDirectory");
            sql.append(" where id = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            query.setParameter("liveDirectory", liveDirectory);
            return query.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

}