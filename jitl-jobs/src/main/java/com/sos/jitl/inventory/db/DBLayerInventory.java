package com.sos.jitl.inventory.db;

import java.util.Date;
import java.util.List;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSDBException;
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

    public DBItemInventoryInstance getInventoryInstance(String schedulerId, String schedulerHost, Integer schedulerPort) throws SOSDBException {
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
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public DBItemInventoryInstance getInventoryInstance(String schedulerHost, Integer schedulerPort) throws SOSDBException {
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
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public DBItemInventoryInstance getInventoryInstance(Long id) throws SOSDBException {
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
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public DBItemInventoryInstance getInventoryInstance(String url) throws SOSDBException {
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
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public DBItemInventoryInstance getInventorySupervisorInstance(String commandUrl) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_INSTANCES);
            sql.append(" where lower(commandUrl) = :commandUrl order by modified desc");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("commandUrl", commandUrl.toLowerCase());
            List<DBItemInventoryInstance> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public DBItemInventoryJob getInventoryJob(Long instanceId, String name) throws SOSDBException {
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
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public DBItemInventoryJobChain getInventoryJobChain(Long instanceId, String name) throws SOSDBException {
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
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public DBItemInventoryOrder getInventoryOrder(Long instanceId, String name) throws SOSDBException {
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
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public DBItemInventoryProcessClass getInventoryProcessClass(Long instanceId, String name) throws SOSDBException {
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
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public DBItemInventorySchedule getInventorySchedule(Long instanceId, String name) throws SOSDBException {
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
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public DBItemInventoryLock getInventoryLock(Long instanceId, String name) throws SOSDBException {
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
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public String getProcessClassName(Long instanceId, String basename) throws SOSDBException {
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public DBItemInventoryProcessClass getProcessClassIfExists(Long instanceId, String processClass) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_PROCESS_CLASSES);
            sql.append(" where instanceId = :instanceId");
            sql.append(" and name = :name");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            query.setParameter("name", processClass);
            List<DBItemInventoryProcessClass> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public DBItemInventorySchedule getScheduleIfExists(Long instanceId, String schedule, String scheduleName) throws SOSDBException {
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public DBItemInventoryFile getInventoryFile(Long instanceId, String fileName) throws SOSDBException {
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public DBItemInventorySchedule getSubstituteIfExists(String substitute, Long instanceId) throws SOSDBException {
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public Long saveOrUpdateSchedule(DBItemInventorySchedule newSchedule) throws SOSDBException {
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public Long getJobChainId(Long instanceId, String name) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select id from ");
            sql.append(DBITEM_INVENTORY_JOB_CHAINS);
            sql.append(" where instanceId = :instanceId");
            sql.append(" and name = :name");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            query.setParameter("name", name);
            return (Long)query.uniqueResult();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public DBItemInventoryJob getJobIfExists(Long instanceId, String job, String jobName) throws SOSDBException {
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public String getJobChainName(Long instanceId, String basename) throws SOSDBException {
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public DBItemInventoryJobChain getJobChain(Long instanceId, String name) throws SOSDBException {
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public DBItemInventoryJobChain getJobChainIfExists(Long instanceId, String jobChain, String jobChainName) throws SOSDBException {
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public DBItemInventoryJobChainNode getJobChainNodeIfExists(Long instanceId, Long jobChainId, String state) throws SOSDBException {
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public DBItemInventoryJobChainNode getJobChainNodeIfExists(Long instanceId, Long jobChainId, Integer nodeType, String state, String directory,
            String regex) throws SOSDBException {
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public List<DBItemInventoryJobChainNode> getJobChainNodes(Long instanceId, Long jobChainId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_JOB_CHAIN_NODES);
            sql.append(" where instanceId = :instanceId");
            sql.append(" and jobChainId = :jobChainId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            query.setParameter("jobChainId", jobChainId);
            return (List<DBItemInventoryJobChainNode>)query.list();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public List<DBItemInventoryJob> getAllJobsForInstance(Long instanceId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_JOBS);
            sql.append(" where instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            return (List<DBItemInventoryJob>)query.list();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public List<DBItemInventoryJobChain> getAllJobChainsForInstance(Long instanceId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_JOB_CHAINS);
            sql.append(" where instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            return (List<DBItemInventoryJobChain>)query.list();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public List<DBItemInventoryJobChainNode> getAllJobChainNodesForInstance(Long instanceId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_JOB_CHAIN_NODES);
            sql.append(" where instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            return (List<DBItemInventoryJobChainNode>)query.list();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public List<DBItemInventoryOrder> getAllOrdersForInstance(Long instanceId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_ORDERS);
            sql.append(" where instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            return (List<DBItemInventoryOrder>)query.list();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public List<DBItemInventoryProcessClass> getAllProcessClassesForInstance(Long instanceId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_PROCESS_CLASSES);
            sql.append(" where instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            return (List<DBItemInventoryProcessClass>)query.list();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public List<DBItemInventorySchedule> getAllSchedulesForInstance(Long instanceId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_SCHEDULES);
            sql.append(" where instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            return (List<DBItemInventorySchedule>)query.list();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public List<DBItemInventoryAppliedLock> getAllAppliedLocks() throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_APPLIED_LOCKS);
            Query query = getSession().createQuery(sql.toString());
            return (List<DBItemInventoryAppliedLock>)query.list();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public List<DBItemInventoryLock> getAllLocksForInstance(Long instanceId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_LOCKS);
            sql.append(" where instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            return (List<DBItemInventoryLock>)query.list();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public List<DBItemInventoryAgentCluster> getAllAgentClustersForInstance(Long instanceId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_AGENT_CLUSTER);
            sql.append(" where instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            return (List<DBItemInventoryAgentCluster>)query.list();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public List<DBItemInventoryAgentClusterMember> getAllAgentClusterMembersForInstance(Long instanceId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS);
            sql.append(" where instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            return (List<DBItemInventoryAgentClusterMember>)query.list();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public List<DBItemInventoryAgentInstance> getAllAgentInstancesForInstance(Long instanceId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_AGENT_INSTANCES);
            sql.append(" where instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            return (List<DBItemInventoryAgentInstance>)query.list();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public List<DBItemInventoryFile> getAllFilesForInstance(Long instanceId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_FILES);
            sql.append(" where instanceId = :instanceId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            return (List<DBItemInventoryFile>)query.list();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public List<DBItemInventoryJob> getAllJobsFromJobChain(Long instanceId, Long jobChainId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ");
            sql.append(DBITEM_INVENTORY_JOBS);
            sql.append(" where name in");
            sql.append(" (select jobName from ").append(DBITEM_INVENTORY_JOB_CHAIN_NODES);
            sql.append(" where instanceId = :instanceId and jobChainId = :jobChainId");
            sql.append(" group by jobName)");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            query.setParameter("jobChainId", jobChainId);
            return (List<DBItemInventoryJob>)query.getResultList();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public void refreshUsedInJobChains(Long instanceId, List<DBItemInventoryJob> jobs) throws SOSDBException {
        for (DBItemInventoryJob job : jobs) {
            refreshUsedInJobChains(instanceId, job);
        }
    }
    
    public void refreshUsedInJobChains(Long instanceId, DBItemInventoryJob job) throws SOSDBException {
        LOGGER.debug(String.format("refreshUsedInJobChains: job   id=%1$s    name=%2$s ", job.getId(), job.getName()));
        job.setUsedInJobChains(getUsedInJobChains(job.getName(), job.getInstanceId()));
        getSession().update(job);
    }
    
    private Integer getUsedInJobChains(String jobName, Long instanceId) throws SOSDBException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select jobChainId from ");
            sql.append(DBLayer.DBITEM_INVENTORY_JOB_CHAIN_NODES);
            sql.append(" where instanceId = :instanceId");
            sql.append(" and jobName = :jobName");
            sql.append(" group by jobChainId");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("jobName", jobName);
            query.setParameter("instanceId", instanceId);
            List<Object> jobChainIds = query.list();
            if(jobChainIds != null) {
                return jobChainIds.size();
            }
            return null;
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public DBItemInventoryAgentInstance getInventoryAgentInstanceFromDb (String url, Long instanceId) throws SOSDBException {
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public int deleteItemsFromDb(Date started, String tableName, Long instanceId) throws SOSDBException {
        LOGGER.debug(String.format("delete: items from %2$s before = %1$s and instanceId = %3$d with query.executeUpdate()", started.toString(), tableName, instanceId));
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ");
            sql.append(tableName);
            sql.append(" where instanceId = :instanceId");
            sql.append(" and modified < :modifiedDate");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            query.setParameter("modifiedDate", started, TemporalType.TIMESTAMP);
            return query.executeUpdate();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public int deleteAppliedLocksFromDb(Date started, Long instanceId) throws SOSDBException {
        LOGGER.debug(String.format("delete: appliedLocks before = %1$s  and instanceId = %2$d with query.executeUpdate()", started.toString(), instanceId));
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public int deleteOldNodes(DBItemInventoryJobChain jobChain) throws SOSDBException {
        LOGGER.debug(String.format("delete old JobChainNodes for JobChain = %1$s and instanceId = %2$d with query.executeUpdate()", jobChain.getName(), jobChain.getInstanceId()));
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ");
            sql.append(DBLayer.DBITEM_INVENTORY_JOB_CHAIN_NODES);
            sql.append(" where instanceId = :instanceId");
            sql.append(" and jobChainId = :jobChainId)");
            Query query = getSession().createQuery(sql.toString());
            query.setParameter("instanceId", jobChain.getInstanceId());
            query.setParameter("jobChainId", jobChain.getId());
            return query.executeUpdate();
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }
    
    public DBItemInventoryLock getLockByName(String name) throws SOSDBException {
        try {
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
        } catch (Exception ex) {
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

    public int updateInventoryLiveDirectory(Long instanceId, String liveDirectory) throws SOSDBException {
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
            throw SOSHibernateSession.getSOSDBException(ex);
        }
    }

}