package com.sos.jitl.inventory.helper;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.reporting.db.DBItemInventoryAgentCluster;
import com.sos.jitl.reporting.db.DBItemInventoryAgentClusterMember;
import com.sos.jitl.reporting.db.DBItemInventoryAppliedLock;
import com.sos.jitl.reporting.db.DBItemInventoryFile;
import com.sos.jitl.reporting.db.DBItemInventoryJob;
import com.sos.jitl.reporting.db.DBItemInventoryJobChain;
import com.sos.jitl.reporting.db.DBItemInventoryJobChainNode;
import com.sos.jitl.reporting.db.DBItemInventoryLock;
import com.sos.jitl.reporting.db.DBItemInventoryOrder;
import com.sos.jitl.reporting.db.DBItemInventoryProcessClass;
import com.sos.jitl.reporting.db.DBItemInventorySchedule;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.db.DBLayerReporting;
import com.sos.jitl.reporting.helper.ReportUtil;


public class SaveOrUpdateHelper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SaveOrUpdateHelper.class);

    @SuppressWarnings("unchecked")
    public static Long saveOrUpdateProcessClass(DBLayerReporting dbLayer, DBItemInventoryProcessClass newProcessClass) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_PROCESS_CLASSES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and fileId = :fileId");
        sql.append(" and name = :name");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", newProcessClass.getInstanceId());
        query.setParameter("fileId", newProcessClass.getFileId());
        query.setParameter("name", newProcessClass.getName());
        List<DBItemInventoryProcessClass> result = query.list();
        if (result != null && !result.isEmpty()) {
            DBItemInventoryProcessClass classFromDb = result.get(0);
            classFromDb.setBasename(newProcessClass.getBasename());
            classFromDb.setHasAgents(newProcessClass.getHasAgents());
            classFromDb.setMaxProcesses(newProcessClass.getMaxProcesses());
            classFromDb.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().update(classFromDb);
            return classFromDb.getId();
        } else {
            newProcessClass.setCreated(ReportUtil.getCurrentDateTime());
            newProcessClass.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().save(newProcessClass);
            return newProcessClass.getId();
        }
    }
 
    @SuppressWarnings("unchecked")
    public static Long saveOrUpdateLock(DBLayerReporting dbLayer, DBItemInventoryLock newLock) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_LOCKS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and fileId = :fileId");
        sql.append(" and name = :name");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", newLock.getInstanceId());
        query.setParameter("fileId", newLock.getFileId());
        query.setParameter("name", newLock.getName());
        List<DBItemInventoryLock> result = query.list();
        if (result != null && !result.isEmpty()) {
            DBItemInventoryLock classFromDb = result.get(0);
            classFromDb.setBasename(newLock.getBasename());
            classFromDb.setMaxNonExclusive(newLock.getMaxNonExclusive());
            classFromDb.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().update(classFromDb);
            return classFromDb.getId();
        } else {
            newLock.setCreated(ReportUtil.getCurrentDateTime());
            newLock.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().save(newLock);
            return newLock.getId();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Long saveOrUpdateSchedule(DBLayerReporting dbLayer, DBItemInventorySchedule newSchedule) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and fileId = :fileId");
        sql.append(" and name = :name");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
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
            dbLayer.getConnection().update(classFromDb);
            return classFromDb.getId();
        } else {
            newSchedule.setCreated(ReportUtil.getCurrentDateTime());
            newSchedule.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().save(newSchedule);
            return newSchedule.getId();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Long saveOrUpdateAgentCluster(DBLayerReporting dbLayer, DBItemInventoryAgentCluster newAgentCluster) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_AGENT_CLUSTER);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and processClassId = :processClassId");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", newAgentCluster.getInstanceId());
        query.setParameter("processClassId", newAgentCluster.getProcessClassId());
        List<DBItemInventoryAgentCluster> result = query.list();
        if (result != null && !result.isEmpty()) {
            DBItemInventoryAgentCluster classFromDb = result.get(0);
            classFromDb.setNumberOfAgents(newAgentCluster.getNumberOfAgents());
            classFromDb.setSchedulingType(newAgentCluster.getSchedulingType());
            classFromDb.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().update(classFromDb);
            return classFromDb.getId();
        } else {
            newAgentCluster.setCreated(ReportUtil.getCurrentDateTime());
            newAgentCluster.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().save(newAgentCluster);
            return newAgentCluster.getId();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Long saveOrUpdateAgentClusterMember(DBLayerReporting dbLayer, DBItemInventoryAgentClusterMember newAgentClusterMember)
            throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and agentClusterId = :agentClusterId");
        sql.append(" and agentInstanceId = :agentInstanceId");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", newAgentClusterMember.getInstanceId());
        query.setParameter("agentClusterId", newAgentClusterMember.getAgentClusterId());
        query.setParameter("agentInstanceId", newAgentClusterMember.getAgentInstanceId());
        List<DBItemInventoryAgentClusterMember> result = query.list();
        if (result != null && !result.isEmpty()) {
            DBItemInventoryAgentClusterMember classFromDb = result.get(0);
            classFromDb.setUrl(newAgentClusterMember.getUrl());
            classFromDb.setOrdering(newAgentClusterMember.getOrdering());
            classFromDb.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().update(classFromDb);
            return classFromDb.getId();
        } else {
            newAgentClusterMember.setCreated(ReportUtil.getCurrentDateTime());
            newAgentClusterMember.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().save(newAgentClusterMember);
            return newAgentClusterMember.getId();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Long saveOrUpdateJob(DBLayerReporting dbLayer, DBItemInventoryJob newJob) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_JOBS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and fileId = :fileId");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", newJob.getInstanceId());
        query.setParameter("fileId", newJob.getFileId());
        List<DBItemInventoryJob> result = query.list();
        if (result != null && !result.isEmpty()) {
            DBItemInventoryJob classFromDb = result.get(0);
            classFromDb.setName(newJob.getName());
            classFromDb.setBaseName(newJob.getBaseName());
            classFromDb.setTitle(newJob.getTitle());
            classFromDb.setIsOrderJob(newJob.getIsOrderJob());
            classFromDb.setIsRuntimeDefined(newJob.getIsRuntimeDefined());
            classFromDb.setUsedInJobChains(newJob.getUsedInJobChains());
            classFromDb.setProcessClass(newJob.getProcessClass());
            classFromDb.setProcessClassId(newJob.getProcessClassId());
            classFromDb.setProcessClassName(newJob.getProcessClassName());
            classFromDb.setSchedule(newJob.getSchedule());
            classFromDb.setScheduleId(newJob.getScheduleId());
            classFromDb.setScheduleName(newJob.getScheduleName());
            classFromDb.setMaxTasks(newJob.getMaxTasks());
            classFromDb.setHasDescription(newJob.getHasDescription());
            classFromDb.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().update(classFromDb);
            return classFromDb.getId();
        } else {
            newJob.setCreated(ReportUtil.getCurrentDateTime());
            newJob.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().save(newJob);
            return newJob.getId();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Long saveOrUpdateJobChain(DBLayerReporting dbLayer, DBItemInventoryJobChain newJobChain) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_JOB_CHAINS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and fileId = :fileId");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", newJobChain.getInstanceId());
        query.setParameter("fileId", newJobChain.getFileId());
        List<DBItemInventoryJobChain> result = query.list();
        if (result != null && !result.isEmpty()) {
            DBItemInventoryJobChain classFromDb = result.get(0);
            classFromDb.setBaseName(newJobChain.getBaseName());
            classFromDb.setDistributed(newJobChain.getDistributed());
            classFromDb.setFileWatchingProcessClass(newJobChain.getFileWatchingProcessClass());
            classFromDb.setFileWatchingProcessClassId(newJobChain.getFileWatchingProcessClassId());
            classFromDb.setFileWatchingProcessClassName(newJobChain.getFileWatchingProcessClassName());
            classFromDb.setMaxOrders(newJobChain.getMaxOrders());
            classFromDb.setName(newJobChain.getName());
            classFromDb.setProcessClass(newJobChain.getProcessClass());
            classFromDb.setProcessClassId(newJobChain.getProcessClassId());
            classFromDb.setProcessClassName(newJobChain.getProcessClassName());
            classFromDb.setStartCause(newJobChain.getStartCause());
            classFromDb.setTitle(newJobChain.getTitle());
            classFromDb.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().update(classFromDb);
            return classFromDb.getId();
        } else {
            newJobChain.setCreated(ReportUtil.getCurrentDateTime());
            newJobChain.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().save(newJobChain);
            return newJobChain.getId();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Long saveOrUpdateJobChainNode(DBLayerReporting dbLayer, DBItemInventoryJobChainNode newJobChainNode) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_JOB_CHAIN_NODES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and jobChainId = :jobChainId");
        sql.append(" and state = :state");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", newJobChainNode.getInstanceId());
        query.setParameter("jobChainId", newJobChainNode.getJobChainId());
        query.setParameter("state", newJobChainNode.getState());
        List<DBItemInventoryJobChainNode> result = query.list();
        if (result != null && !result.isEmpty()) {
            DBItemInventoryJobChainNode classFromDb = result.get(0);
            classFromDb.setDelay(newJobChainNode.getDelay());
            classFromDb.setDirectory(newJobChainNode.getDirectory());
            classFromDb.setErrorState(newJobChainNode.getErrorState());
            classFromDb.setFileSinkOp(newJobChainNode.getFileSinkOp());
            classFromDb.setJob(newJobChainNode.getJob());
            classFromDb.setJobName(newJobChainNode.getJobName());
            classFromDb.setJobId(newJobChainNode.getJobId());
            classFromDb.setMovePath(newJobChainNode.getMovePath());
            classFromDb.setName(newJobChainNode.getName());
            classFromDb.setNestedJobChain(newJobChainNode.getNestedJobChain());
            classFromDb.setNestedJobChainId(newJobChainNode.getNestedJobChainId());
            classFromDb.setNestedJobChainName(newJobChainNode.getNestedJobChainName());
            classFromDb.setNextState(newJobChainNode.getNextState());
            classFromDb.setNodeType(newJobChainNode.getNodeType());
            classFromDb.setOnError(newJobChainNode.getOnError());
            classFromDb.setOrdering(newJobChainNode.getOrdering());
            classFromDb.setRegex(newJobChainNode.getRegex());
            classFromDb.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().update(classFromDb);
            return classFromDb.getId();
        } else {
            newJobChainNode.setCreated(ReportUtil.getCurrentDateTime());
            newJobChainNode.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().save(newJobChainNode);
            return newJobChainNode.getId();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Long saveOrUpdateOrder(DBLayerReporting dbLayer, DBItemInventoryOrder newOrder) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_ORDERS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and fileId = :fileId");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", newOrder.getInstanceId());
        query.setParameter("fileId", newOrder.getFileId());
        List<DBItemInventoryOrder> result = query.list();
        if (result != null && !result.isEmpty()) {
            DBItemInventoryOrder classFromDb = result.get(0);
            classFromDb.setBaseName(newOrder.getBaseName());
            classFromDb.setEndState(newOrder.getEndState());
            classFromDb.setInitialState(newOrder.getInitialState());
            classFromDb.setIsRuntimeDefined(newOrder.getIsRuntimeDefined());
            classFromDb.setJobChainId(newOrder.getJobChainId());
            classFromDb.setJobChainName(newOrder.getJobChainName());
            classFromDb.setName(newOrder.getName());
            classFromDb.setOrderId(newOrder.getOrderId());
            classFromDb.setPriority(newOrder.getPriority());
            classFromDb.setSchedule(newOrder.getSchedule());
            classFromDb.setScheduleId(newOrder.getScheduleId());
            classFromDb.setScheduleName(newOrder.getScheduleName());
            classFromDb.setTitle(newOrder.getTitle());
            classFromDb.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().update(classFromDb);
            return classFromDb.getId();
        } else {
            newOrder.setCreated(ReportUtil.getCurrentDateTime());
            newOrder.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().save(newOrder);
            return newOrder.getId();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static DBItemInventoryProcessClass getProcessClassIfExists(DBLayerReporting dbLayer, Long instanceId, String processClass,
            String processClassName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_PROCESS_CLASSES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        sql.append(" and name = :name");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", processClass);
        query.setParameter("name", processClassName);
        List<DBItemInventoryProcessClass> result = query.list();
        if(result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static DBItemInventorySchedule getScheduleIfExists(DBLayerReporting dbLayer, Long instanceId, String schedule,
            String scheduleName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        sql.append(" and name = :name");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", schedule);
        query.setParameter("name", scheduleName);
        List<DBItemInventorySchedule> result = query.list();
        if(result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static DBItemInventoryJobChain getJobChainIfExists(DBLayerReporting dbLayer, Long instanceId, String jobChain, String jobChainName)
            throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_JOB_CHAINS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and baseName = :baseName");
        sql.append(" and name = :name");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("baseName", jobChain);
        query.setParameter("name", jobChainName);
        List<DBItemInventoryJobChain> result = query.list();
        if(result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static DBItemInventoryJob getJobIfExists(DBLayerReporting dbLayer, Long instanceId, String job, String jobName)
            throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_JOBS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and baseName = :baseName");
        sql.append(" and name = :name");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("baseName", job);
        query.setParameter("name", jobName);
        List<DBItemInventoryJob> result = query.list();
        if(result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Long saveOrUpdateAppliedLock(DBLayerReporting dbLayer, DBItemInventoryAppliedLock newAppliedLock)
            throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_APPLIED_LOCKS);
        sql.append(" where jobId = :jobId");
        sql.append(" and lockId = :lockId");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("jobId", newAppliedLock.getJobId());
        query.setParameter("lockId", newAppliedLock.getLockId());
        List<DBItemInventoryAppliedLock> result = query.list();
        if (result != null && !result.isEmpty()) {
            DBItemInventoryAppliedLock classFromDb = result.get(0);
            classFromDb.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().update(classFromDb);
            return classFromDb.getId();
        } else {
            newAppliedLock.setCreated(ReportUtil.getCurrentDateTime());
            newAppliedLock.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().save(newAppliedLock);
            return newAppliedLock.getId();
        }
    }
    
    public static void refreshUsedInJobChains(DBLayerReporting dbLayer, Long instanceId) throws Exception {
        List<DBItemInventoryJob> jobs = getAllJobs(dbLayer, instanceId);
        for (DBItemInventoryJob job : jobs) {
            LOGGER.debug(String.format("refreshUsedInJobChains : job   id=%s    name=%s ", job.getId(), job.getName()));
            job.setUsedInJobChains(getUsedInJobChains(dbLayer, job.getId()));
            dbLayer.getConnection().update(job);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static List<DBItemInventoryJob> getAllJobs(DBLayerReporting dbLayer, Long instanceId) throws Exception {
        LOGGER.debug("getAllJobs: instanceId = " + instanceId.toString());
        List<DBItemInventoryJob> jobs = new ArrayList<DBItemInventoryJob>();
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_JOBS);
        sql.append(" where instanceId = :instanceId");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        jobs = query.list();
        if(jobs != null && !jobs.isEmpty()) {
            return jobs;
        }
        return null;
    }
    
    private static Integer getUsedInJobChains(DBLayerReporting dbLayer, Long jobId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from ");
        sql.append(DBLayer.DBITEM_INVENTORY_JOB_CHAIN_NODES);
        sql.append(" where jobId = :jobId group by jobChainId");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("jobId", jobId);
        Long usedInJobChains = (Long)query.uniqueResult();
        if(usedInJobChains != null) {
            return usedInJobChains.intValue();
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public static Long saveOrUpdateFile(DBLayerReporting dbLayer, DBItemInventoryFile newFile) throws Exception {
         StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_FILES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and fileName = :fileName");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", newFile.getInstanceId());
        query.setParameter("fileName", newFile.getFileName());
        List<DBItemInventoryFile> result = query.list();
        if (result != null && !result.isEmpty()) {
            DBItemInventoryFile classFromDb = result.get(0);
            classFromDb.setFileBaseName(newFile.getFileBaseName());
            classFromDb.setFileDirectory(newFile.getFileDirectory());
            classFromDb.setFileType(newFile.getFileType());
            classFromDb.setFileCreated(newFile.getFileCreated());
            classFromDb.setFileModified(newFile.getFileModified());
            classFromDb.setFileLocalCreated(newFile.getFileLocalCreated());
            classFromDb.setFileLocalModified(newFile.getFileLocalModified());
            classFromDb.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().update(classFromDb);
            return classFromDb.getId();
        } else {
            newFile.setCreated(ReportUtil.getCurrentDateTime());
            newFile.setModified(ReportUtil.getCurrentDateTime());
            dbLayer.getConnection().save(newFile);
            return newFile.getId();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static String getJobChainName(DBLayerReporting dbLayer, Long instanceId, String basename) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_JOB_CHAINS);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and baseName = :baseName");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("baseName", basename);
        List<DBItemInventoryJobChain> result = query.list();
        if(result != null && !result.isEmpty()){
            return result.get(0).getName();
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    public static String getProcessClassName(DBLayerReporting dbLayer, Long instanceId, String basename) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_PROCESS_CLASSES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", basename);
        List<DBItemInventoryProcessClass> result = query.list();
        if(result != null && !result.isEmpty()){
            return result.get(0).getName();
        }
        return "";
    }
    
    @SuppressWarnings("unchecked")
    public static String getScheduleName(DBLayerReporting dbLayer, Long instanceId, String basename) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ");
        sql.append(DBLayer.DBITEM_INVENTORY_SCHEDULES);
        sql.append(" where instanceId = :instanceId");
        sql.append(" and basename = :basename");
        Query query = dbLayer.getConnection().createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        query.setParameter("basename", basename);
        List<DBItemInventorySchedule> result = query.list();
        if(result != null && !result.isEmpty()){
            return result.get(0).getName();
        }
        return "";
    }
    
}