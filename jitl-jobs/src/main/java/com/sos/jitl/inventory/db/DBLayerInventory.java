package com.sos.jitl.inventory.db;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
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

public class DBLayerInventory extends DBLayer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerInventory.class);

    public DBLayerInventory(SOSHibernateConnection connection) {
        super(connection);
    }

    @SuppressWarnings("unchecked")
    public DBItemInventoryInstance getInventoryInstance(String schedulerId, String schedulerHost, Integer schedulerPort) throws Exception {
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
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemInventoryInstance getInventoryInstance(String schedulerHost, Integer schedulerPort) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_INSTANCES);
            sql.append(" where upper(hostname) = :hostname");
            sql.append(" and port = :port");
            sql.append(" order by id asc");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("hostname", schedulerHost.toUpperCase());
            query.setParameter("port", schedulerPort);
            List<DBItemInventoryInstance> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemInventoryInstance getInventoryInstance(String url) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_INSTANCES);
            sql.append(" where url = :url");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("url", url.toLowerCase());
            List<DBItemInventoryInstance> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }
    
    @SuppressWarnings("unchecked")
    public DBItemInventoryJob getInventoryJob(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_JOBS);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryJob> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemInventoryJobChain getInventoryJobChain(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_JOB_CHAINS);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryJobChain> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemInventoryOrder getInventoryOrder(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_ORDERS);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryOrder> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemInventoryProcessClass getInventoryProcessClass(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_PROCESS_CLASSES);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryProcessClass> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemInventorySchedule getInventorySchedule(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_SCHEDULES);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventorySchedule> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemInventoryLock getInventoryLock(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_LOCKS);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryLock> result = query.list();
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getProcessClassName(Long instanceId, String basename) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_PROCESS_CLASSES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", basename);
        List<DBItemInventoryProcessClass> result = query.list();
        if(result != null && !result.isEmpty()){
            return result.get(0).getName();
        }
        return "";
    }
    
    @SuppressWarnings("unchecked")
    public DBItemInventoryProcessClass getProcessClassIfExists(Long instanceId, String processClass, String processClassName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_PROCESS_CLASSES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        sql.append(" and name = :name");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", processClass);
        query.setParameter("name", processClassName);
        List<DBItemInventoryProcessClass> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public String getScheduleName(Long instanceId, String basename) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", basename);
        List<DBItemInventorySchedule> result = query.list();
        if(result != null && !result.isEmpty()){
            return result.get(0).getName();
        }
        return "";
    }
    
    @SuppressWarnings("unchecked")
    public DBItemInventorySchedule getScheduleIfExists(Long instanceId, String schedule, String scheduleName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        sql.append(" and name = :name");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", schedule);
        query.setParameter("name", scheduleName);
        List<DBItemInventorySchedule> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public DBItemInventoryFile getInventoryFile(Long instanceId, String fileName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_FILES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and fileName = :fileName");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("fileName", fileName);
        List<DBItemInventoryFile> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public DBItemInventorySchedule getSubstituteIfExists(String substitute, Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", substitute);
        List<DBItemInventorySchedule> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public Long saveOrUpdateSchedule(DBItemInventorySchedule newSchedule) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and fileId = :fileId");
        sql.append(" and name = :name");
        Query query = getConnection().createQuery(sql.toString());
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
            getConnection().update(classFromDb);
            return classFromDb.getId();
        } else {
            newSchedule.setCreated(ReportUtil.getCurrentDateTime());
            newSchedule.setModified(ReportUtil.getCurrentDateTime());
            getConnection().save(newSchedule);
            return newSchedule.getId();
        }
    }
    
    public Long getJobChainId(Long instanceId, String basename) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("select id from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAINS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", basename);
        return (Long)query.uniqueResult();
    }
    
    @SuppressWarnings("unchecked")
    public DBItemInventoryJob getJobIfExists(Long instanceId, String job, String jobName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOBS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and baseName = :baseName");
        sql.append(" and name = :name");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("baseName", job);
        query.setParameter("name", jobName);
        List<DBItemInventoryJob> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public String getJobChainName(Long instanceId, String basename) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAINS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and baseName = :baseName");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("baseName", basename);
        List<DBItemInventoryJobChain> result = query.list();
        if(result != null && !result.isEmpty()){
            return result.get(0).getName();
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    public DBItemInventoryJobChain getJobChainIfExists(Long instanceId, String jobChain, String jobChainName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAINS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and baseName = :baseName");
        sql.append(" and name = :name");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("baseName", jobChain);
        query.setParameter("name", jobChainName);
        List<DBItemInventoryJobChain> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public DBItemInventoryJobChainNode getJobChainNodeIfExists(Long instanceId, Long jobChainId, String state) throws Exception {
        // TODO additional constraint
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAIN_NODES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and jobChainId = :jobChainId");
        sql.append(" and state = :state");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("jobChainId", jobChainId);
        query.setParameter("state", state);
        List<DBItemInventoryJobChainNode> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
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
        Query query = getConnection().createQuery(sql.toString());
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
        List<DBItemInventoryJobChainNode> result = query.list();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<DBItemInventoryJobChainNode> getJobChainNodes(Long instanceId, Long jobChainId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAIN_NODES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and jobChainId = :jobChainId");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("jobChainId", jobChainId);
        return (List<DBItemInventoryJobChainNode>)query.list();
    }

//    public int deleteItems(List<DBItemInventoryJobChainNode> items) throws Exception {
//        boolean first = true;
//        StringBuilder sql = new StringBuilder();
//        sql.append("delete from ");
//        sql.append(DBITEM_INVENTORY_JOB_CHAIN_NODES);
//        sql.append(" where id in (");
//        for(DBItemInventoryJobChainNode item : items) {
//            if(first) {
//                sql.append(item.getId().toString());
//                first = false;
//            } else {
//                sql.append(", ");
//                sql.append(item.getId().toString());
//            }
//        }
//        sql.append(")");
//        Query query = getConnection().createQuery(sql.toString());
//        return query.executeUpdate();
//    }
//    
    public void update(Object item) throws Exception {
        LOGGER.debug(String.format("update: item = %s", item));
        Object currentSession = getConnection().getCurrentSession();
        if (currentSession == null) {
            throw new Exception(String.format("currentSession is NULL"));
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.update(item);
//            session.flush();
        } else if (currentSession instanceof StatelessSession) {
            StatelessSession session = ((StatelessSession) currentSession);
            session.update(item);
        }
    }

    public void save(Object item) throws Exception {
        LOGGER.debug(String.format("save: item = %s", item));
        Object currentSession = getConnection().getCurrentSession();
        if (currentSession == null) {
            throw new Exception(String.format("currentSession is NULL"));
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.save(item);
//            session.flush();
        } else if (currentSession instanceof StatelessSession) {
            StatelessSession session = ((StatelessSession) currentSession);
            session.insert(item);
        }
    }

    public Object saveOrUpdate(Object item) throws Exception {
        Object currentSession = getConnection().getCurrentSession();
        if (currentSession == null) {
            throw new Exception(String.format("currentSession is NULL"));
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.saveOrUpdate(item);
//            session.flush();
        } else if (currentSession instanceof StatelessSession) {
            throw new Exception(String.format("saveOrUpdate method is not allowed for this session instance: %s", currentSession.toString()));
        }
        return item;
    }

    public void delete(Object item) throws Exception {
        Object currentSession = getConnection().getCurrentSession();
        if (currentSession == null) {
            throw new Exception(String.format("currentSession is NULL"));
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.delete(item);
//            session.flush();
        } else if (currentSession instanceof StatelessSession) {
            StatelessSession session = ((StatelessSession) currentSession);
            session.delete(item);
        }
    }

    @SuppressWarnings("unchecked")
    public List<DBItemInventoryJob> getAllJobsForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOBS);
        sql.append(" where instanceId = :instanceId");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryJob>)query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<DBItemInventoryJobChain> getAllJobChainsForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAINS);
        sql.append(" where instanceId = :instanceId");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryJobChain>)query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<DBItemInventoryJobChainNode> getAllJobChainNodesForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAIN_NODES);
        sql.append(" where instanceId = :instanceId");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryJobChainNode>)query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<DBItemInventoryOrder> getAllOrdersForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_ORDERS);
        sql.append(" where instanceId = :instanceId");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryOrder>)query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<DBItemInventoryProcessClass> getAllProcessClassesForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_PROCESS_CLASSES);
        sql.append(" where instanceId = :instanceId");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryProcessClass>)query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<DBItemInventorySchedule> getAllSchedulesForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventorySchedule>)query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<DBItemInventoryAppliedLock> getAllAppliedLocks() throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_APPLIED_LOCKS);
        Query query = getConnection().createQuery(sql.toString());
        return (List<DBItemInventoryAppliedLock>)query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<DBItemInventoryLock> getAllLocksForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_LOCKS);
        sql.append(" where instanceId = :instanceId");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryLock>)query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<DBItemInventoryAgentCluster> getAllAgentClustersForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_AGENT_CLUSTER);
        sql.append(" where instanceId = :instanceId");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryAgentCluster>)query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<DBItemInventoryAgentClusterMember> getAllAgentClusterMembersForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS);
        sql.append(" where instanceId = :instanceId");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryAgentClusterMember>)query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<DBItemInventoryAgentInstance> getAllAgentInstancesForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_AGENT_INSTANCES);
        sql.append(" where instanceId = :instanceId");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryAgentInstance>)query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<DBItemInventoryFile> getAllFilesForInstance(Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_FILES);
        sql.append(" where instanceId = :instanceId");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return (List<DBItemInventoryFile>)query.list();
    }
    
    public void refreshUsedInJobChains(Long instanceId, List<DBItemInventoryJob> jobs) throws Exception {
        for (DBItemInventoryJob job : jobs) {
            LOGGER.debug(String.format("refreshUsedInJobChains : job   id=%1$s    name=%2$s ", job.getId(), job.getName()));
            job.setUsedInJobChains(getUsedInJobChains(job.getId()));
            getConnection().update(job);
        }
    }
    
    private Integer getUsedInJobChains(Long jobId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from ");
        sql.append(DBLayer.DBITEM_INVENTORY_JOB_CHAIN_NODES);
        sql.append(" where jobId = :jobId group by jobChainId");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("jobId", jobId);
        Long usedInJobChains = (Long)query.uniqueResult();
        if(usedInJobChains != null) {
            return usedInJobChains.intValue();
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public DBItemInventoryAgentInstance getInventoryAgentInstanceFromDb (String url, Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_AGENT_INSTANCES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and url = :url");
        Query query = getConnection().createQuery(sql.toString());
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
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ");
        sql.append(tableName);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and modified < :modified");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setTimestamp("modified", started);
        int count = query.executeUpdate();
        return count;
    }
    
    public int deleteAppliedLocksFromDb(Date started, Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ");
        sql.append(DBLayer.DBITEM_INVENTORY_APPLIED_LOCKS).append(" appliedLocks ");
        sql.append("where appliedLocks.id in (select locks.id from ");
        sql.append(DBLayer.DBITEM_INVENTORY_LOCKS).append(" locks");
        sql.append(" where locks.instanceId = :instanceId");
        sql.append(" and locks.modified < :modified )");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setTimestamp("modified", started);
        int count = query.executeUpdate();
        return count;
    }
    
    @SuppressWarnings("unchecked")
    public DBItemInventoryLock getLockByName(String name) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_LOCKS);
        sql.append(" where basename = :basename");
        Query query = getConnection().createQuery(sql.toString());
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
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            query.setParameter("liveDirectory", liveDirectory);
            return query.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

}