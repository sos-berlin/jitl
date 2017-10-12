package com.sos.jitl.inventory.helper;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.inventory.db.DBLayerInventory;
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
import com.sos.jitl.reporting.helper.ReportUtil;

public class SaveOrUpdateHelper {

    private static List<DBItemInventoryFile> dbFiles;
    private static List<DBItemInventoryJob> dbJobs;
    private static List<DBItemInventoryJobChain> dbJobChains;
    private static List<DBItemInventoryJobChainNode> dbJobChainNodes;
    private static List<DBItemInventoryOrder> dbOrders;
    private static List<DBItemInventoryProcessClass> dbProcessClasses;
    private static List<DBItemInventorySchedule> dbSchedules;
    private static List<DBItemInventoryLock> dbLocks;
    private static List<DBItemInventoryAppliedLock> dbAppliedLocks;
    private static List<DBItemInventoryAgentCluster> dbAgentClusters;
    private static List<DBItemInventoryAgentClusterMember> dbAgentClusterMembers;
    private static List<DBItemInventoryAgentInstance> dbAgentInstances;

    public static Long saveOrUpdateFile(DBLayerInventory inventoryDbLayer, DBItemInventoryFile file, List<DBItemInventoryFile> dbFiles)
            throws SOSHibernateException {
        Long id = null;
        if (dbFiles.contains(file)) {
            DBItemInventoryFile dbItem = dbFiles.get(dbFiles.indexOf(file));
            dbItem.setFileName(file.getFileName());
            dbItem.setFileBaseName(file.getFileBaseName());
            dbItem.setFileDirectory(file.getFileDirectory());
            dbItem.setFileType(file.getFileType());
            dbItem.setFileCreated(file.getFileCreated());
            dbItem.setFileModified(file.getFileModified());
            dbItem.setFileLocalCreated(file.getFileLocalCreated());
            dbItem.setFileLocalModified(file.getFileLocalModified());
            dbItem.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().update(dbItem);
            id = dbItem.getId();
        } else {
            file.setCreated(ReportUtil.getCurrentDateTime());
            file.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().save(file);
            id = file.getId();
        }
        return id;
    }

    public static Long saveOrUpdateJob(DBLayerInventory inventoryDbLayer, DBItemInventoryJob job, List<DBItemInventoryJob> dbJobs)
            throws SOSHibernateException {
        Long id = null;
        if (dbJobs.contains(job)) {
            DBItemInventoryJob dbItem = dbJobs.get(dbJobs.indexOf(job));
            dbItem.setName(job.getName());
            dbItem.setBaseName(job.getBaseName());
            dbItem.setHasDescription(job.getHasDescription());
            dbItem.setIsOrderJob(job.getIsOrderJob());
            dbItem.setIsRuntimeDefined(job.getIsRuntimeDefined());
            dbItem.setMaxTasks(job.getMaxTasks());
            dbItem.setProcessClass(job.getProcessClass());
            dbItem.setProcessClassId(job.getProcessClassId());
            dbItem.setProcessClassName(job.getProcessClassName());
            dbItem.setSchedule(job.getSchedule());
            dbItem.setScheduleId(job.getScheduleId());
            dbItem.setScheduleName(job.getScheduleName());
            dbItem.setTitle(job.getTitle());
            dbItem.setUsedInJobChains(job.getUsedInJobChains());
            dbItem.setRunTimeIsTemporary(false);
            dbItem.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().update(dbItem);
            id = dbItem.getId();
        } else {
            job.setRunTimeIsTemporary(false);
            job.setCreated(ReportUtil.getCurrentDateTime());
            job.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().save(job);
            id = job.getId();
        }
        return id;
    }

    public static Long saveOrUpdateJobChain(DBLayerInventory inventoryDbLayer, DBItemInventoryJobChain jobChain,
            List<DBItemInventoryJobChain> dbJobChains) throws SOSHibernateException {
        Long id = null;
        if (dbJobChains.contains(jobChain)) {
            DBItemInventoryJobChain dbItem = dbJobChains.get(dbJobChains.indexOf(jobChain));
            dbItem.setName(jobChain.getName());
            dbItem.setBaseName(jobChain.getBaseName());
            dbItem.setDistributed(jobChain.getDistributed());
            dbItem.setProcessClass(jobChain.getProcessClass());
            dbItem.setProcessClassId(jobChain.getProcessClassId());
            dbItem.setProcessClassName(jobChain.getProcessClassName());
            dbItem.setFileWatchingProcessClass(jobChain.getFileWatchingProcessClass());
            dbItem.setFileWatchingProcessClassId(jobChain.getFileWatchingProcessClassId());
            dbItem.setFileWatchingProcessClassName(jobChain.getFileWatchingProcessClassName());
            dbItem.setMaxOrders(jobChain.getMaxOrders());
            dbItem.setStartCause(jobChain.getStartCause());
            dbItem.setTitle(jobChain.getTitle());
            dbItem.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().update(dbItem);
            id = dbItem.getId();
        } else {
            jobChain.setCreated(ReportUtil.getCurrentDateTime());
            jobChain.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().save(jobChain);
            id = jobChain.getId();
        }
        return id;
    }

    public static Long saveOrUpdateJobChainNode(DBLayerInventory inventoryDbLayer, DBItemInventoryJobChainNode jobChainNode,
            List<DBItemInventoryJobChainNode> dbJobChainNodes) throws SOSHibernateException {
        Long id = null;
        if (dbJobChainNodes.contains(jobChainNode)) {
            DBItemInventoryJobChainNode dbItem = dbJobChainNodes.get(dbJobChainNodes.indexOf(jobChainNode));
            dbItem.setName(jobChainNode.getName());
            dbItem.setDelay(jobChainNode.getDelay());
            dbItem.setDirectory(jobChainNode.getDirectory());
            dbItem.setErrorState(jobChainNode.getErrorState());
            dbItem.setFileSinkOp(jobChainNode.getFileSinkOp());
            dbItem.setJob(jobChainNode.getJob());
            dbItem.setJobName(jobChainNode.getJobName());
            dbItem.setJobId(jobChainNode.getJobId());
            dbItem.setMovePath(jobChainNode.getMovePath());
            dbItem.setNestedJobChain(jobChainNode.getNestedJobChain());
            dbItem.setNestedJobChainId(jobChainNode.getNestedJobChainId());
            dbItem.setNestedJobChainName(jobChainNode.getNestedJobChainName());
            dbItem.setNextState(jobChainNode.getNextState());
            dbItem.setNodeType(jobChainNode.getNodeType());
            dbItem.setOnError(jobChainNode.getOnError());
            dbItem.setOrdering(jobChainNode.getOrdering());
            dbItem.setRegex(jobChainNode.getRegex());
            dbItem.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().update(dbItem);
            id = dbItem.getId();
        } else {
            jobChainNode.setCreated(ReportUtil.getCurrentDateTime());
            jobChainNode.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().save(jobChainNode);
            id = jobChainNode.getId();
        }
        return id;
    }

    public static Long saveOrUpdateOrder(DBLayerInventory inventoryDbLayer, DBItemInventoryOrder order,
            List<DBItemInventoryOrder> dbOrders) throws SOSHibernateException {
        Long id = null;
        if (dbOrders.contains(order)) {
            DBItemInventoryOrder dbItem = dbOrders.get(dbOrders.indexOf(order));
            dbItem.setName(order.getName());
            dbItem.setBaseName(order.getBaseName());
            dbItem.setEndState(order.getEndState());
            dbItem.setInitialState(order.getInitialState());
            dbItem.setIsRuntimeDefined(order.getIsRuntimeDefined());
            dbItem.setJobChainId(order.getJobChainId());
            dbItem.setJobChainName(order.getJobChainName());
            dbItem.setOrderId(order.getOrderId());
            dbItem.setPriority(order.getPriority());
            dbItem.setSchedule(order.getSchedule());
            dbItem.setScheduleId(order.getScheduleId());
            dbItem.setScheduleName(order.getScheduleName());
            dbItem.setTitle(order.getTitle());
            if (dbItem.getRunTimeIsTemporary() == null) {
                dbItem.setRunTimeIsTemporary(false);
            }
            dbItem.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().update(dbItem);
            id = dbItem.getId();
        } else {
            order.setRunTimeIsTemporary(false);
            order.setCreated(ReportUtil.getCurrentDateTime());
            order.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().save(order);
            id = order.getId();
        }
        return id;
    }

    public static Long saveOrUpdateProcessClass(DBLayerInventory inventoryDbLayer, DBItemInventoryProcessClass processClass,
            List<DBItemInventoryProcessClass> dbProcessClasses) throws SOSHibernateException {
        Long id = null;
        if (dbProcessClasses.contains(processClass)) {
            DBItemInventoryProcessClass dbItem = dbProcessClasses.get(dbProcessClasses.indexOf(processClass));
            dbItem.setName(processClass.getName());
            dbItem.setBasename(processClass.getBasename());
            dbItem.setHasAgents(processClass.getHasAgents());
            dbItem.setMaxProcesses(processClass.getMaxProcesses());
            dbItem.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().update(dbItem);
            id = dbItem.getId();
        } else {
            processClass.setCreated(ReportUtil.getCurrentDateTime());
            processClass.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().save(processClass);
            id = processClass.getId();
        }
        return id;
    }

    public static Long saveOrUpdateSchedule(DBLayerInventory inventoryDbLayer, DBItemInventorySchedule schedule,
            List<DBItemInventorySchedule> dbSchedules) throws SOSHibernateException {
        Long id = null;
        if (dbSchedules.contains(schedule)) {
            DBItemInventorySchedule dbItem = dbSchedules.get(dbSchedules.indexOf(schedule));
            dbItem.setName(schedule.getName());
            dbItem.setBasename(schedule.getBasename());
            dbItem.setTitle(schedule.getTitle());
            dbItem.setSubstitute(schedule.getSubstitute());
            dbItem.setSubstituteId(schedule.getSubstituteId());
            dbItem.setSubstituteName(schedule.getSubstituteName());
            dbItem.setSubstituteValidFrom(schedule.getSubstituteValidFrom());
            dbItem.setSubstituteValidTo(schedule.getSubstituteValidTo());
            dbItem.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().update(dbItem);
            id = dbItem.getId();
        } else {
            schedule.setCreated(ReportUtil.getCurrentDateTime());
            schedule.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().save(schedule);
            id = schedule.getId();
        }
        return id;
    }

    public static Long saveOrUpdateLock(DBLayerInventory inventoryDbLayer, DBItemInventoryLock lock, List<DBItemInventoryLock> dbLocks)
            throws SOSHibernateException {
        Long id = null;
        if (dbLocks.contains(lock)) {
            DBItemInventoryLock dbItem = dbLocks.get(dbLocks.indexOf(lock));
            dbItem.setName(lock.getName());
            dbItem.setBasename(lock.getBasename());
            dbItem.setMaxNonExclusive(lock.getMaxNonExclusive());
            dbItem.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().update(dbItem);
            id = dbItem.getId();
        } else {
            lock.setCreated(ReportUtil.getCurrentDateTime());
            lock.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().save(lock);
            id = lock.getId();
        }
        return id;
    }

    public static Long saveOrUpdateAppliedLock(DBLayerInventory inventoryDbLayer, DBItemInventoryAppliedLock appliedLock,
            List<DBItemInventoryAppliedLock> dbAppliedLocks) throws SOSHibernateException {
        Long id = null;
        if (dbAppliedLocks.contains(appliedLock)) {
            DBItemInventoryAppliedLock dbItem = dbAppliedLocks.get(dbAppliedLocks.indexOf(appliedLock));
            dbItem.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().update(dbItem);
            id = dbItem.getId();
        } else {
            appliedLock.setCreated(ReportUtil.getCurrentDateTime());
            appliedLock.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().save(appliedLock);
            id = appliedLock.getId();
        }
        return id;
    }

    public static Long saveOrUpdateAgentCluster(DBLayerInventory inventoryDbLayer, DBItemInventoryAgentCluster agentCluster,
            List<DBItemInventoryAgentCluster> dbAgentClusters) throws SOSHibernateException {
        Long id = null;
        if (dbAgentClusters.contains(agentCluster)) {
            DBItemInventoryAgentCluster dbItem = dbAgentClusters.get(dbAgentClusters.indexOf(agentCluster));
            dbItem.setNumberOfAgents(agentCluster.getNumberOfAgents());
            dbItem.setSchedulingType(agentCluster.getSchedulingType());
            dbItem.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().update(dbItem);
            id = dbItem.getId();
        } else {
            agentCluster.setCreated(ReportUtil.getCurrentDateTime());
            agentCluster.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().save(agentCluster);
            id = agentCluster.getId();
        }
        return id;
    }

    public static Long saveOrUpdateAgentClusterMember(DBLayerInventory inventoryDbLayer,
            DBItemInventoryAgentClusterMember agentClusterMember, List<DBItemInventoryAgentClusterMember> dbAgentClusterMembers)
                    throws SOSHibernateException {
        Long id = null;
        if (dbAgentClusterMembers.contains(agentClusterMember)) {
            DBItemInventoryAgentClusterMember dbItem = dbAgentClusterMembers.get(dbAgentClusterMembers.indexOf(agentClusterMember));
            dbItem.setUrl(agentClusterMember.getUrl());
            dbItem.setOrdering(agentClusterMember.getOrdering());
            dbItem.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().update(dbItem);
            id = dbItem.getId();
        } else {
            agentClusterMember.setCreated(ReportUtil.getCurrentDateTime());
            agentClusterMember.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.getSession().save(agentClusterMember);
            id = agentClusterMember.getId();
        }
        return id;
    }

    public static Long saveOrUpdateAgentInstance(DBItemInventoryAgentInstance agentItem, SOSHibernateSession connection)
            throws SOSHibernateException, Exception {
        Instant newDate = Instant.now();
        if (dbAgentInstances.contains(agentItem)) {
            DBItemInventoryAgentInstance agentFromDb = dbAgentInstances.get(dbAgentInstances.indexOf(agentItem));
            agentFromDb.setHostname(agentItem.getHostname());
            agentFromDb.setOsId(agentItem.getOsId());
            agentFromDb.setUrl(agentItem.getUrl());
            agentFromDb.setStartedAt(agentItem.getStartedAt());
            agentFromDb.setState(agentItem.getState());
            agentFromDb.setModified(Date.from(newDate));
            connection.update(agentFromDb);
            return agentFromDb.getId();
        } else {
            agentItem.setCreated(Date.from(newDate));
            agentItem.setModified(Date.from(newDate));
            connection.save(agentItem);
            return agentItem.getId();
        }
    }

    public static <T> Long saveOrUpdateItem(DBLayerInventory inventory, T item) throws SOSHibernateException {
        if (item instanceof DBItemInventoryFile) {
            return saveOrUpdateFile(inventory, (DBItemInventoryFile) item, dbFiles);
        } else if (item instanceof DBItemInventoryJob) {
            return saveOrUpdateJob(inventory, (DBItemInventoryJob) item, dbJobs);
        } else if (item instanceof DBItemInventoryOrder) {
            return saveOrUpdateOrder(inventory, (DBItemInventoryOrder) item, dbOrders);
        } else if (item instanceof DBItemInventoryJobChain) {
            return saveOrUpdateJobChain(inventory, (DBItemInventoryJobChain) item, dbJobChains);
        } else if (item instanceof DBItemInventoryJobChainNode) {
            return saveOrUpdateJobChainNode(inventory, (DBItemInventoryJobChainNode) item, dbJobChainNodes);
        } else if (item instanceof DBItemInventoryProcessClass) {
            return saveOrUpdateProcessClass(inventory, (DBItemInventoryProcessClass) item, dbProcessClasses);
        } else if (item instanceof DBItemInventorySchedule) {
            return saveOrUpdateSchedule(inventory, (DBItemInventorySchedule) item, dbSchedules);
        } else if (item instanceof DBItemInventoryLock) {
            return saveOrUpdateLock(inventory, (DBItemInventoryLock) item, dbLocks);
        } else if (item instanceof DBItemInventoryAppliedLock) {
            return saveOrUpdateAppliedLock(inventory, (DBItemInventoryAppliedLock) item, dbAppliedLocks);
        } else if (item instanceof DBItemInventoryAgentCluster) {
            return saveOrUpdateAgentCluster(inventory, (DBItemInventoryAgentCluster) item, dbAgentClusters);
        } else if (item instanceof DBItemInventoryAgentClusterMember) {
            return saveOrUpdateAgentClusterMember(inventory, (DBItemInventoryAgentClusterMember) item, dbAgentClusterMembers);
        } else {
            return null;
        }
    }

    public static void initExistingItems(DBLayerInventory inventory, DBItemInventoryInstance inventoryInstance)
            throws SOSHibernateException {
        dbFiles = inventory.getAllFilesForInstance(inventoryInstance.getId());
        dbJobs = inventory.getAllJobsForInstance(inventoryInstance.getId());
        dbJobChains = inventory.getAllJobChainsForInstance(inventoryInstance.getId());
        dbOrders = inventory.getAllOrdersForInstance(inventoryInstance.getId());
        dbProcessClasses = inventory.getAllProcessClassesForInstance(inventoryInstance.getId());
        dbSchedules = inventory.getAllSchedulesForInstance(inventoryInstance.getId());
        dbLocks = inventory.getAllLocksForInstance(inventoryInstance.getId());
        dbAppliedLocks = inventory.getAllAppliedLocks();
        dbAgentClusters = inventory.getAllAgentClustersForInstance(inventoryInstance.getId());
        dbAgentClusterMembers = inventory.getAllAgentClusterMembersForInstance(inventoryInstance.getId());
        dbAgentInstances = inventory.getAllAgentInstancesForInstance(inventoryInstance.getId());
    }
    
    public static List<DBItemInventoryAgentCluster> getAgentClusters() {
        return dbAgentClusters;
    }
    
    public static List<DBItemInventoryAgentClusterMember> getAgentClusterMembers() {
        return dbAgentClusterMembers;
    }
    
    public static void clearExisitingItems() {
        if (dbFiles != null) {
            dbFiles.clear();
        }
        if (dbJobs != null) {
            dbJobs.clear();
        }
        if (dbJobChains != null) {
            dbJobChains.clear();
        }
        if (dbOrders != null) {
            dbOrders.clear();
        }
        if (dbProcessClasses != null) {
            dbProcessClasses.clear();
        }
        if (dbSchedules != null) {
            dbSchedules.clear();
        }
        if (dbLocks != null) {
            dbLocks.clear();
        }
        if (dbAppliedLocks != null) {
            dbAppliedLocks.clear();
        }
        if (dbAgentClusters != null) {
            dbAgentClusters.clear();
        }
        if (dbAgentClusterMembers != null) {
            dbAgentClusterMembers.clear();
        }
        if(dbAgentInstances != null) {
            dbAgentInstances.clear();
        }
    }

    public static void initExisitingJobChainNodes(DBLayerInventory inventory, DBItemInventoryInstance inventoryInstance)
            throws SOSHibernateException {
        dbJobChainNodes = inventory.getAllJobChainNodesForInstance(inventoryInstance.getId());
    }

    public static void clearExistingJobChainNodes() {
        if (dbJobChainNodes != null) {
            dbJobChainNodes.clear();
        }
    }

    public static void updateScheduleIdForOrders(DBLayerInventory dbLayer, Long instanceId, Long scheduleId, String scheduleName)
            throws SOSHibernateException {
        List<DBItemInventoryOrder> ordersToUpdate = dbLayer.getOrdersReferencingSchedule(instanceId, scheduleName);
        if (ordersToUpdate != null) {
            for (DBItemInventoryOrder order : ordersToUpdate) {
                order.setScheduleId(scheduleId);
                dbLayer.getSession().update(order);
            }
        }
    }

    public static void updateScheduleIdForJobs(DBLayerInventory dbLayer, Long instanceId, Long scheduleId, String scheduleName)
            throws SOSHibernateException {
        List<DBItemInventoryJob> jobsToUpdate = dbLayer.getJobsReferencingSchedule(instanceId, scheduleName);
        if (jobsToUpdate != null) {
            for (DBItemInventoryJob job : jobsToUpdate) {
                job.setScheduleId(scheduleId);
                dbLayer.getSession().update(job);
            }
        }
    }

}