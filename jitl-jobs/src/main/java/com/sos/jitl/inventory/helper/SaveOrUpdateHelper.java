package com.sos.jitl.inventory.helper;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.inventory.db.DBLayerInventory;
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
import com.sos.jitl.reporting.helper.ReportUtil;


public class SaveOrUpdateHelper {
    
    public static Long saveOrUpdateFile(DBLayerInventory inventoryDbLayer, DBItemInventoryFile file, List<DBItemInventoryFile> dbFiles) throws Exception {
        Long id = null;
        if(dbFiles.contains(file)) {
            for (DBItemInventoryFile dbItem : dbFiles) {
                if(dbItem.equals(file)) {
                    dbItem.setFileName(file.getFileName());
                    dbItem.setFileBaseName(file.getFileBaseName());
                    dbItem.setFileDirectory(file.getFileDirectory());
                    dbItem.setFileType(file.getFileType());
                    dbItem.setFileCreated(file.getFileCreated());
                    dbItem.setFileModified(file.getFileModified());
                    dbItem.setFileLocalCreated(file.getFileLocalCreated());
                    dbItem.setFileLocalModified(file.getFileLocalModified());
                    dbItem.setModified(ReportUtil.getCurrentDateTime());
                    inventoryDbLayer.getConnection().update(dbItem);
                    id = dbItem.getId();
                    break;
                }
            }
        } else {
            file.setCreated(ReportUtil.getCurrentDateTime());
            file.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.saveOrUpdate(file);
            id = file.getId();
        }
        return id;
    }
    
    public static Long saveOrUpdateJob(DBLayerInventory inventoryDbLayer, DBItemInventoryJob job, List<DBItemInventoryJob> dbJobs) throws Exception {
        Long id = null;
        if(dbJobs.contains(job)) {
            for (DBItemInventoryJob dbItem : dbJobs) {
                if(dbItem.equals(job)) {
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
                    dbItem.setModified(ReportUtil.getCurrentDateTime());
                    inventoryDbLayer.getConnection().update(dbItem);
                    id = dbItem.getId();
                    break;
                }
            }
        } else {
            job.setCreated(ReportUtil.getCurrentDateTime());
            job.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.saveOrUpdate(job);
            id = job.getId();
        }
        return id;
    }
    
    public static Long saveOrUpdateJobChain(DBLayerInventory inventoryDbLayer, DBItemInventoryJobChain jobChain, List<DBItemInventoryJobChain> dbJobChains)
            throws Exception {
        Long id = null;
        if(dbJobChains.contains(jobChain)) {
            for (DBItemInventoryJobChain dbItem : dbJobChains) {
                if(dbItem.equals(jobChain)) {
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
                    inventoryDbLayer.getConnection().update(dbItem);
                    id = dbItem.getId();
                    break;
                }
            }
        } else {
            jobChain.setCreated(ReportUtil.getCurrentDateTime());
            jobChain.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.saveOrUpdate(jobChain);
            id = jobChain.getId();
        }
        return id;
    }
    
    public static Long saveOrUpdateJobChainNode(DBLayerInventory inventoryDbLayer, DBItemInventoryJobChainNode jobChainNode,
            List<DBItemInventoryJobChainNode> dbJobChainNodes) throws Exception {
        Long id = null;
        if(dbJobChainNodes.contains(jobChainNode)) {
            for (DBItemInventoryJobChainNode dbItem : dbJobChainNodes) {
                if(dbItem.equals(jobChainNode)) {
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
                    inventoryDbLayer.getConnection().update(dbItem);
                    id = dbItem.getId();
                    break;
                }
            }
        } else {
            jobChainNode.setCreated(ReportUtil.getCurrentDateTime());
            jobChainNode.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.saveOrUpdate(jobChainNode);
            id = jobChainNode.getId();
        }
        return id;
    }
    
    public static Long saveOrUpdateOrder(DBLayerInventory inventoryDbLayer, DBItemInventoryOrder order, List<DBItemInventoryOrder> dbOrders)
            throws Exception {
        Long id = null;
        if(dbOrders.contains(order)) {
            for (DBItemInventoryOrder dbItem : dbOrders) {
                if(dbItem.equals(order)) {
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
                    dbItem.setModified(ReportUtil.getCurrentDateTime());
                    inventoryDbLayer.getConnection().update(dbItem);
                    id = dbItem.getId();
                    break;
                }
            }
        } else {
            order.setCreated(ReportUtil.getCurrentDateTime());
            order.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.saveOrUpdate(order);
            id = order.getId();
        }
        return id;
    }
    
    public static Long saveOrUpdateProcessClass(DBLayerInventory inventoryDbLayer, DBItemInventoryProcessClass processClass,
            List<DBItemInventoryProcessClass> dbProcessClasses) throws Exception {
        Long id = null;
        if(dbProcessClasses.contains(processClass)) {
            for (DBItemInventoryProcessClass dbItem : dbProcessClasses) {
                if(dbItem.equals(processClass)) {
                    dbItem.setName(processClass.getName());
                    dbItem.setBasename(processClass.getBasename());
                    dbItem.setHasAgents(processClass.getHasAgents());
                    dbItem.setMaxProcesses(processClass.getMaxProcesses());
                    dbItem.setModified(ReportUtil.getCurrentDateTime());
                    inventoryDbLayer.getConnection().update(dbItem);
                    id = dbItem.getId();
                    break;
                }
            }
        } else {
            processClass.setCreated(ReportUtil.getCurrentDateTime());
            processClass.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.saveOrUpdate(processClass);
            id = processClass.getId();
        }
        return id;
    }
    
    public static Long saveOrUpdateSchedule(DBLayerInventory inventoryDbLayer, DBItemInventorySchedule schedule, List<DBItemInventorySchedule> dbSchedules)
            throws Exception {
        Long id = null;
        if(dbSchedules.contains(schedule)) {
            for (DBItemInventorySchedule dbItem : dbSchedules) {
                if(dbItem.equals(schedule)) {
                    dbItem.setName(schedule.getName());
                    dbItem.setBasename(schedule.getBasename());
                    dbItem.setTitle(schedule.getTitle());
                    dbItem.setSubstitute(schedule.getSubstitute());
                    dbItem.setSubstituteId(schedule.getSubstituteId());
                    dbItem.setSubstituteName(schedule.getSubstituteName());
                    dbItem.setSubstituteValidFrom(schedule.getSubstituteValidFrom());
                    dbItem.setSubstituteValidTo(schedule.getSubstituteValidTo());
                    dbItem.setModified(ReportUtil.getCurrentDateTime());
                    inventoryDbLayer.getConnection().update(dbItem);
                    id = dbItem.getId();
                    break;
                }
            }
        } else {
            schedule.setCreated(ReportUtil.getCurrentDateTime());
            schedule.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.saveOrUpdate(schedule);
            id = schedule.getId();
        }
        return id;
    }
    
    public static Long saveOrUpdateLock(DBLayerInventory inventoryDbLayer, DBItemInventoryLock lock, List<DBItemInventoryLock> dbLocks) throws Exception {
        Long id = null;
        if(dbLocks.contains(lock)) {
            for (DBItemInventoryLock dbItem : dbLocks) {
                if(dbItem.equals(lock)) {
                    dbItem.setName(lock.getName());
                    dbItem.setBasename(lock.getBasename());
                    dbItem.setMaxNonExclusive(lock.getMaxNonExclusive());
                    dbItem.setModified(ReportUtil.getCurrentDateTime());
                    inventoryDbLayer.getConnection().update(dbItem);
                    id = dbItem.getId();
                    break;
                }
            }
        } else {
            lock.setCreated(ReportUtil.getCurrentDateTime());
            lock.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.saveOrUpdate(lock);
            id = lock.getId();
        }
        return id;
    }
    
    public static Long saveOrUpdateAppliedLock(DBLayerInventory inventoryDbLayer, DBItemInventoryAppliedLock appliedLock,
            List<DBItemInventoryAppliedLock> dbAppliedLocks) throws Exception {
        Long id = null;
        if(dbAppliedLocks.contains(appliedLock)) {
            for (DBItemInventoryAppliedLock dbItem : dbAppliedLocks) {
                if(dbItem.equals(appliedLock)) {
                    dbItem.setModified(ReportUtil.getCurrentDateTime());
                    inventoryDbLayer.getConnection().update(dbItem);
                    id = dbItem.getId();
                    break;
                }
            }
        } else {
            appliedLock.setCreated(ReportUtil.getCurrentDateTime());
            appliedLock.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.saveOrUpdate(appliedLock);
            id = appliedLock.getId();
        }
        return id;
    }
    
    public static Long saveOrUpdateAgentCluster(DBLayerInventory inventoryDbLayer, DBItemInventoryAgentCluster agentCluster,
            List<DBItemInventoryAgentCluster> dbAgentClusters) throws Exception {
        Long id = null;
        if(dbAgentClusters.contains(agentCluster)) {
            for (DBItemInventoryAgentCluster dbItem : dbAgentClusters) {
                if(dbItem.equals(agentCluster)) {
                    dbItem.setNumberOfAgents(agentCluster.getNumberOfAgents());
                    dbItem.setSchedulingType(agentCluster.getSchedulingType());
                    dbItem.setModified(ReportUtil.getCurrentDateTime());
                    inventoryDbLayer.getConnection().update(dbItem);
                    id = dbItem.getId();
                    break;
                }
            }
        } else {
            agentCluster.setCreated(ReportUtil.getCurrentDateTime());
            agentCluster.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.saveOrUpdate(agentCluster);
            id = agentCluster.getId();
        }
        return id;
    }
    
    @SuppressWarnings("unchecked")
    public static Long saveOrUpdateAgentClusterMember(DBLayerInventory inventoryDbLayer, DBItemInventoryAgentClusterMember agentClusterMember,
            List<DBItemInventoryAgentClusterMember> dbAgentClusterMembers) throws Exception {
        Long id = null;
        if(dbAgentClusterMembers.contains(agentClusterMember)) {
            for (DBItemInventoryAgentClusterMember dbItem : dbAgentClusterMembers) {
                if(dbItem.equals(agentClusterMember)) {
                    dbItem.setUrl(agentClusterMember.getUrl());
                    dbItem.setOrdering(agentClusterMember.getOrdering());
                    dbItem.setModified(ReportUtil.getCurrentDateTime());
                    inventoryDbLayer.getConnection().update(dbItem);
                    id = dbItem.getId();
                    break;
                }
            }
        } else {
            agentClusterMember.setCreated(ReportUtil.getCurrentDateTime());
            agentClusterMember.setModified(ReportUtil.getCurrentDateTime());
            inventoryDbLayer.saveOrUpdate(agentClusterMember);
            id = agentClusterMember.getId();
        }
        return id;
    }
    
}