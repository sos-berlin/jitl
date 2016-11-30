package com.sos.jitl.inventory.db;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateConnection;
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
            if (!result.isEmpty()) {
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
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemInventoryInstance getInventoryInstance(String url) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_INSTANCES);
            sql.append(" where url = :url");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("url", url.toLowerCase());
                return (DBItemInventoryInstance)query.uniqueResult();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }
    
    public DBItemInventoryJob getInventoryJob(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_JOBS);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            return (DBItemInventoryJob)query.uniqueResult();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemInventoryJobChain getInventoryJobChain(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_JOB_CHAINS);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            return (DBItemInventoryJobChain)query.uniqueResult();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemInventoryOrder getInventoryOrder(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_ORDERS);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            return (DBItemInventoryOrder)query.uniqueResult();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemInventoryProcessClass getInventoryProcessClass(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_PROCESS_CLASSES);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            return (DBItemInventoryProcessClass)query.uniqueResult();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemInventorySchedule getInventorySchedule(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_SCHEDULES);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            return (DBItemInventorySchedule)query.uniqueResult();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemInventoryLock getInventoryLock(Long instanceId, String name) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_INVENTORY_LOCKS);
            sql.append(" where name = :name");
            sql.append(" and instanceId = :instanceId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("instanceId", instanceId);
            return (DBItemInventoryLock)query.uniqueResult();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }
    
    public String getProcessClassName(Long instanceId, String basename) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_PROCESS_CLASSES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", basename);
        DBItemInventoryProcessClass result = (DBItemInventoryProcessClass)query.uniqueResult();
        if(result != null){
            return result.getName();
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
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", processClass);
        query.setParameter("name", processClassName);
        return (DBItemInventoryProcessClass)query.uniqueResult();
    }

    public String getScheduleName(Long instanceId, String basename) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", basename);
        DBItemInventorySchedule result = (DBItemInventorySchedule)query.uniqueResult();
        if(result != null){
            return result.getName();
        }
        return "";
    }
    
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
        return (DBItemInventorySchedule)query.uniqueResult();
    }
    
    public DBItemInventoryFile getInventoryFile(Long instanceId, String fileName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_FILES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and fileName = :fileName");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("fileName", fileName);
        return (DBItemInventoryFile)query.uniqueResult();
    }

    public DBItemInventorySchedule getSubstituteIfExists(String substitute, Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", substitute);
        return (DBItemInventorySchedule)query.uniqueResult();
    }
    
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
        DBItemInventorySchedule result = (DBItemInventorySchedule)query.uniqueResult();
        if (result != null) {
            DBItemInventorySchedule classFromDb = result;
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
        return (DBItemInventoryJob)query.uniqueResult();
    }

    public String getJobChainName(Long instanceId, String basename) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAINS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and baseName = :baseName");
        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("baseName", basename);
        DBItemInventoryJobChain result = (DBItemInventoryJobChain)query.uniqueResult();
        if(result != null){
            return result.getName();
        }
        return "";
    }

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
        return (DBItemInventoryJobChain)query.uniqueResult();
    }

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
        return (DBItemInventoryJobChainNode)query.uniqueResult();
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

    public int deleteItems(List<DBItemInventoryJobChainNode> items) throws Exception {
        boolean first = true;
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ");
        sql.append(DBITEM_INVENTORY_JOB_CHAIN_NODES);
        sql.append(" where id in (");
        for(DBItemInventoryJobChainNode item : items) {
            if(first) {
                sql.append(item.getId().toString());
                first = false;
            } else {
                sql.append(", ");
                sql.append(item.getId().toString());
            }
        }
        sql.append(")");
        Query query = getConnection().createQuery(sql.toString());
        return query.executeUpdate();
    }
    
    public Object saveOrUpdate(Object item) throws Exception {
        Object currentSession = getConnection().getCurrentSession();
        if (currentSession == null) {
            throw new Exception(String.format("currentSession is NULL"));
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.saveOrUpdate(item);
            session.flush();
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
            session.flush();
        } else if (currentSession instanceof StatelessSession) {
            StatelessSession session = ((StatelessSession) currentSession);
            session.delete(item);
        }
    }

}