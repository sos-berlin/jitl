package com.sos.jitl.reporting.model.inventory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.xml.SOSXMLXPath;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.inventory.data.ProcessInitialInventoryUtil;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.inventory.helper.SaveOrUpdateHelper;
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
import com.sos.jitl.reporting.helper.EConfigFileExtensions;
import com.sos.jitl.reporting.helper.EStartCauses;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.reporting.job.inventory.InventoryJobOptions;
import com.sos.jitl.reporting.model.IReportingModel;
import com.sos.jitl.reporting.model.ReportingModel;

public class InventoryModel extends ReportingModel implements IReportingModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryModel.class);
    private static final String DEFAULT_PROCESS_CLASS_NAME = "(default)";
    private InventoryJobOptions options;
    private DBItemInventoryInstance inventoryInstance;
    private int countTotalJobs = 0;
    private int countSuccessJobs = 0;
    private int countTotalJobChains = 0;
    private int countSuccessJobChains = 0;
    private int countTotalOrders = 0;
    private int countSuccessOrders = 0;
    private int countNotFoundedJobChainJobs = 0;
    private int countTotalLocks = 0;
    private int countSuccessLocks = 0;
    private int countTotalProcessClasses = 0;
    private int countSuccessProcessClasses = 0;
    private int countTotalSchedules = 0;
    private int countSuccessSchedules = 0;
    private LinkedHashMap<String, ArrayList<String>> notFoundedJobChainJobs;
    private LinkedHashMap<String, String> errorJobChains;
    private LinkedHashMap<String, String> errorOrders;
    private LinkedHashMap<String, String> errorJobs;
    private Map<String, String> errorLocks;
    private Map<String, String> errorProcessClasses;
    private Map<String, String> errorSchedules;
    private Date started;
    private String schedulerXmlPath;
    private String schedulerLivePath;
    private String answerXml;
    private List<DBItemInventoryFile> dbFiles;
    private List<DBItemInventoryJob> dbJobs;
    private List<DBItemInventoryJobChain> dbJobChains;
    private List<DBItemInventoryJobChainNode> dbJobChainNodes;
    private List<DBItemInventoryOrder> dbOrders;
    private List<DBItemInventoryProcessClass> dbProcessClasses;
    private List<DBItemInventorySchedule> dbSchedules;
    private List<DBItemInventoryLock> dbLocks;
    private List<DBItemInventoryAppliedLock> dbAppliedLocks;
    private List<DBItemInventoryAgentCluster> dbAgentCLusters;
    private List<DBItemInventoryAgentClusterMember> dbAgentClusterMembers;
    private DBLayerInventory inventoryDbLayer;
    private SOSXMLXPath xPathAnswerXml;

    public InventoryModel(SOSHibernateConnection reportingConn, InventoryJobOptions opt) throws Exception {
        super(reportingConn);
        this.options = opt;
        this.schedulerXmlPath = options.schedulerData.getValue() + "/config/scheduler.xml";
        this.schedulerLivePath = options.schedulerData.getValue() + "/config/live";
        this.inventoryDbLayer = new DBLayerInventory(reportingConn);
    }

    @Override
    public void process() throws Exception {
        String method = "process";
        try {
            initCounters();
            started = ReportUtil.getCurrentDateTime();
            getDbLayer().getConnection().beginTransaction();
            initInventoryInstance();
            initExistingItems();
            processSchedulerXml();
            processStateAnswerXML();
            inventoryDbLayer.refreshUsedInJobChains(inventoryInstance.getId(), dbJobs);
            String cleanUpLog = cleanUpInventoryAfter(started);
            logSummary();
            resume();
            LOGGER.debug(cleanUpLog);
            getDbLayer().getConnection().commit();
        } catch (Exception ex) {
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {
                // no exception handling
            }
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    private void initExistingItems() throws Exception {
        dbFiles = inventoryDbLayer.getAllFilesForInstance(inventoryInstance.getId());
        dbJobs = inventoryDbLayer.getAllJobsForInstance(inventoryInstance.getId());
        dbJobChains = inventoryDbLayer.getAllJobChainsForInstance(inventoryInstance.getId());
        dbJobChainNodes = inventoryDbLayer.getAllJobChainNodesForInstance(inventoryInstance.getId());
        dbOrders = inventoryDbLayer.getAllOrdersForInstance(inventoryInstance.getId());
        dbProcessClasses = inventoryDbLayer.getAllProcessClassesForInstance(inventoryInstance.getId());
        dbSchedules = inventoryDbLayer.getAllSchedulesForInstance(inventoryInstance.getId());
        dbLocks = inventoryDbLayer.getAllLocksForInstance(inventoryInstance.getId());
        dbAppliedLocks = inventoryDbLayer.getAllAppliedLocks();
        dbAgentCLusters = inventoryDbLayer.getAllAgentClustersForInstance(inventoryInstance.getId());
        dbAgentClusterMembers = inventoryDbLayer.getAllAgentClusterMembersForInstance(inventoryInstance.getId());
    }
    
    private void initInventoryInstance() throws Exception {
        setInventoryInstance();
    }

    private void initCounters() {
        countTotalJobs = 0;
        countTotalJobChains = 0;
        countTotalOrders = 0;
        countSuccessJobs = 0;
        countSuccessJobChains = 0;
        countSuccessOrders = 0;
        countNotFoundedJobChainJobs = 0;
        countTotalLocks = 0;
        countSuccessLocks = 0;
        countTotalProcessClasses = 0;
        countSuccessProcessClasses = 0;
        countTotalSchedules = 0;
        countSuccessSchedules = 0;
        notFoundedJobChainJobs = new LinkedHashMap<String, ArrayList<String>>();
        errorJobChains = new LinkedHashMap<String, String>();
        errorOrders = new LinkedHashMap<String, String>();
        errorJobs = new LinkedHashMap<String, String>();
        errorLocks = new LinkedHashMap<String, String>();
        errorProcessClasses = new LinkedHashMap<String, String>();
        errorSchedules = new LinkedHashMap<String, String>();
    }

    private void logSummary() {
        String method = "logSummary";
        LOGGER.debug(String.format("%s: inserted or updated job chains = %s (total %s, error = %s)", method, countSuccessJobChains, 
                countTotalJobChains, errorJobChains.size()));
        LOGGER.debug(String.format("%s: inserted or updated orders = %s (total %s, error = %s)", method, countSuccessOrders, countTotalOrders, 
                errorOrders.size()));
        LOGGER.debug(String.format("%s: inserted or updated jobs = %s (total %s, error = %s)", method, countSuccessJobs, countTotalJobs, 
                errorJobs.size()));
        LOGGER.debug(String.format("%s: inserted or updated locks = %s (total %s, error = %s)", method, countSuccessLocks, countTotalLocks,
                errorLocks.size()));
        LOGGER.debug(String.format("%s: inserted or updated process classes = %s (total %s, error = %s)", method, countSuccessProcessClasses,
                countTotalProcessClasses, errorProcessClasses.size()));
        LOGGER.debug(String.format("%s: inserted or updated schedules = %s (total %s, error = %s)", method, countSuccessSchedules, 
                countTotalSchedules, errorSchedules.size()));
        if (!errorJobChains.isEmpty()) {
            LOGGER.debug(String.format("%s:   errors by insert or update job chains:", method));
            int i = 1;
            for (Entry<String, String> entry : errorJobChains.entrySet()) {
                LOGGER.debug(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (!errorOrders.isEmpty()) {
            LOGGER.debug(String.format("%s:   errors by insert or update orders:", method));
            int i = 1;
            for (Entry<String, String> entry : errorOrders.entrySet()) {
                LOGGER.debug(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (!errorJobs.isEmpty()) {
            LOGGER.debug(String.format("%s:   errors by insert or update jobs:", method));
            int i = 1;
            for (Entry<String, String> entry : errorJobs.entrySet()) {
                LOGGER.debug(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (!errorLocks.isEmpty()) {
            LOGGER.debug(String.format("%s:   errors by insert or update locks:", method));
            int i = 1;
            for (Entry<String, String> entry : errorLocks.entrySet()) {
                LOGGER.debug(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (!errorProcessClasses.isEmpty()) {
            LOGGER.debug(String.format("%s:   errors by insert or update process classes:", method));
            int i = 1;
            for (Entry<String, String> entry : errorProcessClasses.entrySet()) {
                LOGGER.debug(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (!errorSchedules.isEmpty()) {
            LOGGER.debug(String.format("%s:   errors by insert or update schedules:", method));
            int i = 1;
            for (Entry<String, String> entry : errorSchedules.entrySet()) {
                LOGGER.debug(String.format("%s:     %s) %s: %s", method, i, entry.getKey(), entry.getValue()));
                i++;
            }
        }
        if (countNotFoundedJobChainJobs > 0) {
            LOGGER.debug(String.format("%s: jobs not found on the disc (declared in the job chains) = %s", method, countNotFoundedJobChainJobs));
            int i = 1;
            for (Entry<String, ArrayList<String>> entry : notFoundedJobChainJobs.entrySet()) {
                LOGGER.debug(String.format("%s:     %s) %s", method, i, entry.getKey()));
                for (int j = 0; j < entry.getValue().size(); j++) {
                    LOGGER.debug(String.format("%s:         %s) %s", method, j + 1, entry.getValue().get(j)));
                }
                i++;
            }
        }
    }

    private void resume() throws Exception {
        if (countSuccessJobChains == 0 && countSuccessOrders == 0 && countSuccessJobs == 0 && countSuccessLocks == 0
                && countSuccessProcessClasses == 0 && countSuccessSchedules == 0) {
            throw new Exception(String.format("error occured: 0 job chains, orders, jobs locks, process classes or schedules inserted or updated!"));
        }
        if (!errorJobChains.isEmpty() || !errorOrders.isEmpty() || !errorJobs.isEmpty() || !errorLocks.isEmpty()
                || !errorProcessClasses.isEmpty() || !errorSchedules.isEmpty()) {
            LOGGER.warn(String.format("error occured: insert or update failed by %s job chains, %s orders, %s jobs, %s locks, %s process classes, "
                    + "%s schedules", errorJobChains.size(), errorOrders.size(), errorJobs.size(), errorLocks.size(), errorProcessClasses.size(),
                    errorSchedules.size()));
        }
    }

    private DBItemInventoryFile processFileForObjectsInSchedulerXml(String objName, String fileType) throws Exception {
        String method = "  processFileForObjectsInSchedulerXml";
        Date fileCreated = null;
        Date fileModified = null;
        Date fileLocalCreated = null;
        Date fileLocalModified = null;
        BasicFileAttributes attrs = null;
        try {
            attrs = Files.readAttributes(Paths.get(schedulerXmlPath), BasicFileAttributes.class);
            fileCreated = ReportUtil.convertFileTime2UTC(attrs.creationTime());
            fileModified = ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime());
            fileLocalCreated = ReportUtil.convertFileTime2Local(attrs.creationTime());
            fileLocalModified = ReportUtil.convertFileTime2Local(attrs.lastModifiedTime());
        } catch (IOException exception) {
            LOGGER.debug(String.format("%s: cannot read file attributes. file = %s, exception = %s  ", method, schedulerXmlPath,
                    exception.toString()));
        }
        DBItemInventoryFile item = new DBItemInventoryFile();
        item.setInstanceId(inventoryInstance.getId());
        item.setFileType(fileType);
        item.setFileName("/" + objName);
        item.setFileBaseName(objName);
        item.setFileDirectory("/");
        item.setFileCreated(fileCreated);
        item.setFileModified(fileModified);
        item.setFileLocalCreated(fileLocalCreated);
        item.setFileLocalModified(fileLocalModified);
        Long id = SaveOrUpdateHelper.saveOrUpdateFile(inventoryDbLayer, item, dbFiles);
        if(item.getId() == null) {
            item.setId(id);
        }
        LOGGER.debug(String.format(
                "%s: file     id = %s, fileType = %s, fileName = %s, fileBasename = %s, fileDirectory = %s, fileCreated = %s, fileModified = %s",
                method, item.getId(), item.getFileType(), item.getFileName(), item.getFileBaseName(), item.getFileDirectory(),
                item.getFileCreated(), item.getFileModified()));
        return item;
    }

    private void setInventoryInstance() throws Exception {
        String method = "setInventoryInstance";
        ProcessInitialInventoryUtil dataUtil = 
                new ProcessInitialInventoryUtil(options.hibernate_configuration_file.getValue(), getDbLayer().getConnection());
        DBItemInventoryInstance instanceFromState = dataUtil.getDataFromJobscheduler(answerXml);
        DBItemInventoryInstance ii = inventoryDbLayer.getInventoryInstance(instanceFromState.getSchedulerId(),
                instanceFromState.getHostname(), instanceFromState.getPort());
        String liveDirectory = ReportUtil.normalizePath(options.current_scheduler_configuration_directory.getValue());
        if (ii == null) {
            LOGGER.debug(String.format("%s: create new instance. schedulerId = %s, hostname = %s, port = %s, configuration directory = %s", method,
                    instanceFromState.getSchedulerId(), instanceFromState.getHostname(), instanceFromState.getPort(), liveDirectory));
            ii = new DBItemInventoryInstance();
            ii.setSchedulerId(instanceFromState.getSchedulerId());
            ii.setHostname(instanceFromState.getHostname());
            ii.setLiveDirectory(options.current_scheduler_configuration_directory.getValue());
            ii.setCreated(ReportUtil.getCurrentDateTime());
            ii.setModified(ReportUtil.getCurrentDateTime());
            /** new Items since 1.11 */
            ii.setPort(instanceFromState.getPort());
            ii.setOsId(instanceFromState.getOsId());
            ii.setVersion(instanceFromState.getVersion());
            ii.setUrl(instanceFromState.getUrl());
            ii.setCommandUrl(instanceFromState.getCommandUrl());
            ii.setTimeZone(instanceFromState.getTimeZone());
            ii.setClusterType(instanceFromState.getClusterType());
            ii.setPrecedence(instanceFromState.getPrecedence());
            ii.setDbmsName(instanceFromState.getDbmsName());
            ii.setDbmsVersion(instanceFromState.getDbmsVersion());
            ii.setStartedAt(instanceFromState.getStartedAt());
            ii.setSupervisorId(instanceFromState.getSupervisorId());
            /** End of new Items since 1.11 */
            getDbLayer().getConnection().save(ii);
        } else {
            getDbLayer().updateInventoryLiveDirectory(ii.getId(), liveDirectory);
        }
        inventoryInstance = ii;
    }

    private Integer getJobChainNodeType(String nodeName, Element jobChainNode) {
        switch(nodeName){
        case "job_chain_node":
            if(jobChainNode.hasAttribute("job")){
                return 1;
            } else {
                return 5;
            }
        case "job_chain_node.job_chain":
            return 2;
        case "file_order_source":
            return 3;
        case "file_order_sink":
            return 4;
        case "job_chain_node.end":
            return 5;
        }
        return null;
    }
    
    private void processDefaultProcessClass(Integer maxProcesses) throws Exception {
        countTotalProcessClasses++;
        String name = "/" + DEFAULT_PROCESS_CLASS_NAME;
        try {
            DBItemInventoryFile dbItemFile = processFileForObjectsInSchedulerXml(DEFAULT_PROCESS_CLASS_NAME, EConfigFileExtensions.PROCESS_CLASS.type());
            DBItemInventoryProcessClass item = new DBItemInventoryProcessClass();
            item.setInstanceId(dbItemFile.getInstanceId());
            item.setFileId(dbItemFile.getId());
            item.setName(name);
            item.setBasename(DEFAULT_PROCESS_CLASS_NAME);
            item.setMaxProcesses(maxProcesses);
            item.setHasAgents(false);
            Long id = SaveOrUpdateHelper.saveOrUpdateProcessClass(inventoryDbLayer, item, dbProcessClasses);
            if(item.getId() == null) {
                item.setId(id);
            }
            countSuccessProcessClasses++;
        } catch (Exception ex) {
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {}
            LOGGER.warn(String.format("processProcessClass: default processClass cannot be inserted, exception = %s ",
                    ex.toString()), ex);
            errorProcessClasses.put(name, ex.toString());
        }
    }

    private void processAgentCluster(Map<String,Integer> remoteSchedulers, String schedulingType, Long instanceId, Long processClassId)
            throws Exception {
        Integer numberOfAgents = remoteSchedulers.size();
        DBItemInventoryAgentCluster agentCluster = new DBItemInventoryAgentCluster();
        agentCluster.setInstanceId(instanceId);
        agentCluster.setProcessClassId(processClassId);
        agentCluster.setNumberOfAgents(numberOfAgents);
        agentCluster.setSchedulingType(schedulingType);
        Long clusterId = SaveOrUpdateHelper.saveOrUpdateAgentCluster(inventoryDbLayer, agentCluster, dbAgentCLusters);
        for(String agentUrl : remoteSchedulers.keySet()) {
            DBItemInventoryAgentInstance agent = inventoryDbLayer.getInventoryAgentInstanceFromDb(agentUrl, instanceId);
            if(agent != null) {
                Integer ordering = remoteSchedulers.get(agent.getUrl().toLowerCase());
                DBItemInventoryAgentClusterMember agentClusterMember = new DBItemInventoryAgentClusterMember();
                agentClusterMember.setInstanceId(instanceId);
                agentClusterMember.setAgentClusterId(clusterId);
                agentClusterMember.setAgentInstanceId(agent.getId());
                agentClusterMember.setUrl(agent.getUrl());
                agentClusterMember.setOrdering(ordering);
                SaveOrUpdateHelper.saveOrUpdateAgentClusterMember(inventoryDbLayer, agentClusterMember, dbAgentClusterMembers);
            }
        }
    }
    
    private String cleanUpInventoryAfter(Date started) throws Exception {
        Integer jobsDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_JOBS, inventoryInstance.getId());
        Integer jobChainsDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_JOB_CHAINS, inventoryInstance.getId());
        Integer jobChainNodesDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_JOB_CHAIN_NODES, inventoryInstance.getId());
        Integer ordersDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_ORDERS, inventoryInstance.getId());
        Integer appliedLocksDeleted = inventoryDbLayer.deleteAppliedLocksFromDb(started, inventoryInstance.getId());
        Integer locksDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_LOCKS, inventoryInstance.getId());
        Integer schedulesDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_SCHEDULES, inventoryInstance.getId());
        Integer processClassesDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_PROCESS_CLASSES, inventoryInstance.getId());
        Integer agentClustersDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_AGENT_CLUSTER, inventoryInstance.getId());
        Integer agentClusterMembersDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS,
                inventoryInstance.getId());
        StringBuilder strb = new StringBuilder();
        strb.append(String.format("cleanUpInventoryAfter: delete Inventory entries older than %1$s", started.toString())).append("\n");
        strb.append(String.format("%s old Jobs deleted from inventory.", jobsDeleted.toString())).append("\n");
        strb.append(String.format("%s old JobChains deleted from inventory.", jobChainsDeleted.toString())).append("\n");
        strb.append(String.format("%s old JobChainNodes deleted from inventory.", jobChainNodesDeleted.toString())).append("\n");
        strb.append(String.format("%s old Orders deleted from inventory.", ordersDeleted.toString())).append("\n");
        strb.append(String.format("%s old Locks deleted from inventory.", locksDeleted.toString())).append("\n");
        strb.append(String.format("%s old Applied Locks deleted from inventory.", appliedLocksDeleted.toString())).append("\n");
        strb.append(String.format("%s old Schedules deleted from inventory.", schedulesDeleted.toString())).append("\n");
        strb.append(String.format("%s old Process Classes deleted from inventory.", processClassesDeleted.toString())).append("\n");
        strb.append(String.format("%s old Agent Clusters deleted from inventory.", agentClustersDeleted.toString())).append("\n");
        strb.append(String.format("%s old Agent Cluster Members deleted from inventory.", agentClusterMembersDeleted.toString()));
        return strb.toString();
    }
    
    private void processSchedulerXml() throws Exception {
        SOSXMLXPath xPathSchedulerXml = new SOSXMLXPath(schedulerXmlPath);
        String maxProcesses =
                xPathSchedulerXml.selectSingleNodeValue("/spooler/config/process_classes/process_class[not(@name)]/@max_processes");
        if(maxProcesses != null && !maxProcesses.isEmpty()) {
            processDefaultProcessClass(Integer.parseInt(maxProcesses));
        } else {
            processDefaultProcessClass(30);
        }
        String supervisor = xPathSchedulerXml.selectSingleNodeValue("/spooler/config/@supervisor");
        if (supervisor != null && !supervisor.isEmpty()) {
            String[] supervisorSplit = supervisor.split(":");
            String supervisorHost = supervisorSplit[0];
            Integer supervisorPort = Integer.parseInt(supervisorSplit[1]);
            // depends on jobscheduler(and supervisor too) using http_port only
            // at the moment jobscheduler instances are saved with http port only, 
            // as long as supervisor port is still the tcp port, no instance will be found in db 
            // and supervisorId won´t be updated
            DBItemInventoryInstance supervisorInstance = inventoryDbLayer.getInventoryInstance(supervisorHost, supervisorPort);
            if (supervisorInstance != null) {
                inventoryInstance.setSupervisorId(supervisorInstance.getId());
            }
        }
    }
    
    private void processStateAnswerXML() throws Exception {
        InputStream inStream = new ByteArrayInputStream(answerXml.getBytes());
        xPathAnswerXml = new SOSXMLXPath(inStream);
        NodeList jobNodes = xPathAnswerXml.selectNodeList("/spooler/answer/state/jobs/job");
        for(int i = 0; i < jobNodes.getLength(); i++) {
            processJobFromNodes((Element)jobNodes.item(i));
        }
        NodeList jobChainNodes = xPathAnswerXml.selectNodeList("/spooler/answer/state/job_chains/job_chain");
        for (int i = 0; i < jobChainNodes.getLength(); i++) {
            processJobChainFromNodes((Element)jobChainNodes.item(i));
        }
        NodeList orderNodes =
                xPathAnswerXml.selectNodeList("/spooler/answer/state/job_chains/job_chain/job_chain_node/order_queue/order[file_based/@file]");
        for (int i = 0; i < orderNodes.getLength(); i++) {
            processOrderFromNodes((Element)orderNodes.item(i));
        }
        NodeList processClassNodes = xPathAnswerXml.selectNodeList("/spooler/answer/state/process_classes/process_class");
        for (int i = 0; i < processClassNodes.getLength(); i++) {
            processProcessClassFromNodes((Element)processClassNodes.item(i));
        }
        NodeList lockNodes = xPathAnswerXml.selectNodeList("/spooler/answer/state/locks/lock");
        for (int i = 0; i < lockNodes.getLength(); i++) {
            processLockFromNodes((Element)lockNodes.item(i));
        }
        NodeList scheduleNodes = xPathAnswerXml.selectNodeList("/spooler/answer/state/schedules/schedule");
        for (int i = 0; i < scheduleNodes.getLength(); i++) {
            processScheduleFromNodes((Element)scheduleNodes.item(i));
        }
    }
    
    private DBItemInventoryFile processFile(Element element, EConfigFileExtensions fileExtension) throws Exception {
        String fileName = null;
        if (element.hasAttribute("path")) {
            fileName = element.getAttribute("path") + fileExtension.extension();
        }
        String fileBasename = fileName.substring(fileName.lastIndexOf("/") + 1);
        String fileDirectory = fileName.substring(0, fileName.lastIndexOf("/"));
        String fileType = fileExtension.type();
        String method = "  processFile";
        Date fileCreated = null;
        Date fileModified = null;
        Date fileLocalCreated = null;
        Date fileLocalModified = null;
        Path path = Paths.get(schedulerLivePath,fileName);
        BasicFileAttributes attrs = null;
        try {
            attrs = Files.readAttributes(path, BasicFileAttributes.class);
            fileCreated = ReportUtil.convertFileTime2UTC(attrs.creationTime());
            fileModified = ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime());
            fileLocalCreated = ReportUtil.convertFileTime2Local(attrs.creationTime());
            fileLocalModified = ReportUtil.convertFileTime2Local(attrs.lastModifiedTime());
        } catch (IOException exception) {
            LOGGER.debug(String.format("%s: cannot read file attributes. file = %s, exception = %s  ", method, path.toString(),
                    exception.toString()));
        }
        DBItemInventoryFile item = new DBItemInventoryFile();
        item.setInstanceId(inventoryInstance.getId());
        item.setFileType(fileType);
        item.setFileName(fileName);
        item.setFileBaseName(fileBasename);
        item.setFileDirectory(fileDirectory);
        item.setFileCreated(fileCreated);
        item.setFileModified(fileModified);
        item.setFileLocalCreated(fileLocalCreated);
        item.setFileLocalModified(fileLocalModified);
        Long id = SaveOrUpdateHelper.saveOrUpdateFile(inventoryDbLayer, item, dbFiles);
        if(item.getId() == null) {
            item.setId(id);
        }
        LOGGER.debug(String.format(
                "%s: file     id = %s, fileType = %s, fileName = %s, fileBasename = %s, fileDirectory = %s, fileCreated = %s, fileModified = %s",
                method, item.getId(), item.getFileType(), item.getFileName(), item.getFileBaseName(), item.getFileDirectory(),
                item.getFileCreated(), item.getFileModified()));
        return item;
    }
    
    private void processJobFromNodes(Element job) throws Exception {
        String method = "    processJob";
        countTotalJobs++;
        DBItemInventoryFile file = processFile(job, EConfigFileExtensions.JOB);
        try {
            DBItemInventoryJob item = new DBItemInventoryJob();
            String name = null;
            name = file.getFileName().replace(EConfigFileExtensions.JOB.extension(), "");
            item.setName(name);
            String baseName = file.getFileBaseName().replace(EConfigFileExtensions.JOB.extension(), "");
            item.setBaseName(baseName);
            String title = job.getAttribute("title");
            if (title != null && !title.isEmpty()) {
                item.setTitle(title);
            }
            boolean isOrderJob = job.getAttribute("order") != null && "yes".equals(job.getAttribute("order").toLowerCase());
            item.setIsOrderJob(isOrderJob);
            NodeList runtimes = job.getElementsByTagName("run_time");
            item.setIsRuntimeDefined((runtimes != null && runtimes.getLength() > 0));
            item.setInstanceId(file.getInstanceId());
            item.setFileId(file.getId());
            item.setCreated(ReportUtil.getCurrentDateTime());
            item.setModified(ReportUtil.getCurrentDateTime());
            /** new Items since 1.11 */
            if (job.hasAttribute("process_class")) {
                String processClass = job.getAttribute("process_class");
                DBItemInventoryProcessClass pc = processClassExists(processClass);
                if(pc != null) {
                    item.setProcessClass(pc.getBasename());
                    item.setProcessClassName(pc.getName());
                    item.setProcessClassId(pc.getId());
                } else {
                    item.setProcessClass(processClass);
                    item.setProcessClassName(processClass.substring(processClass.lastIndexOf("/" + 1)));
                    item.setProcessClassId(DBLayer.DEFAULT_ID);
                }
            } else {
                item.setProcessClassId(DBLayer.DEFAULT_ID);
                item.setProcessClassName(DBLayer.DEFAULT_NAME);
            }
            Node runtime = runtimes.item(0);
            String schedule = null;
            if (runtime != null && ((Element) runtime).hasAttribute("schedule")) {
                schedule = ((Element) runtime).getAttribute("schedule");
            }
            if (schedule != null && !schedule.isEmpty()) {
                DBItemInventorySchedule is = scheduleExists(schedule);
                if (is != null) {
                    item.setSchedule(is.getBasename());
                    item.setScheduleName(is.getName());
                    item.setScheduleId(is.getId());
                } else {
                    item.setSchedule(schedule);
                    item.setScheduleName(DBLayer.DEFAULT_NAME);
                    item.setScheduleId(DBLayer.DEFAULT_ID);
                }
            } else {
                item.setScheduleId(DBLayer.DEFAULT_ID);
                item.setScheduleName(DBLayer.DEFAULT_NAME);
            }
            String maxTasks = job.getAttribute("tasks");
            if(maxTasks != null && !maxTasks.isEmpty()) {
                item.setMaxTasks(Integer.parseInt(maxTasks));
            } else {
                item.setMaxTasks(0);
            }
            NodeList description = job.getElementsByTagName("description");
            item.setHasDescription((description != null && description.getLength() > 0));
            /** End of new Items */
            Long id = SaveOrUpdateHelper.saveOrUpdateJob(inventoryDbLayer, item, dbJobs);
            if(item.getId() == null) {
                item.setId(id);
            }
            LOGGER.debug(String.format("%s: job     id = %s, jobName = %s, jobBasename = %s, title = %s, isOrderJob = %s, isRuntimeDefined = %s",
                    method, item.getId(), item.getName(), item.getBaseName(), item.getTitle(), item.getIsOrderJob(), item.getIsRuntimeDefined()));
            countSuccessJobs++;
            NodeList lockUses = job.getElementsByTagName("lock.use");
            if(lockUses != null && lockUses.getLength() > 0) {
                for(int i = 0; i < lockUses.getLength(); i++) {
                    Element lockUse = (Element)lockUses.item(i); 
                    String lockName = lockUse.getAttribute("lock");
                    if(lockName.contains("/")) {
                        lockName = lockName.substring(lockName.lastIndexOf("/") + 1);
                    }
                    DBItemInventoryLock lock = lockExists(lockName);
                    if(lock != null) {
                        DBItemInventoryAppliedLock appliedLock = appliedLockExists(lock.getId(), item.getId());
                        if(appliedLock == null) {
                            appliedLock = new DBItemInventoryAppliedLock();
                            appliedLock.setJobId(item.getId());
                            appliedLock.setLockId(lock.getId());
                        }
                        Long appLockId = SaveOrUpdateHelper.saveOrUpdateAppliedLock(inventoryDbLayer, appliedLock, dbAppliedLocks);
                        if(appliedLock.getId() == null) {
                            appliedLock.setId(appLockId);
                        }
                        dbAppliedLocks.add(appliedLock);
                    }
                }
            }
        } catch (Exception ex) {
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {}
            LOGGER.warn(String.format("%s: job file cannot be inserted = %s, exception = %s ", method, file.getFileName(), ex.toString()), ex);
            errorJobs.put(file.getFileName() , ex.toString());
        }
    }
    
    private void processJobChainFromNodes(Element jobChain) throws Exception {
        String method = "    processJobChain";
        DBItemInventoryFile file = processFile(jobChain, EConfigFileExtensions.JOB_CHAIN);
        countTotalJobChains++;
        try {
            DBItemInventoryJobChain item = new DBItemInventoryJobChain();
            String name = null;
            name = file.getFileName().replace(EConfigFileExtensions.JOB_CHAIN.extension(), "");
            item.setName(name);
            String baseName = file.getFileBaseName().replace(EConfigFileExtensions.JOB_CHAIN.extension(), "");
            item.setBaseName(baseName);
            String title = jobChain.getAttribute("title");
            if (title != null && !title.isEmpty()) {
                item.setTitle(title);
            }
            NodeList fileOrderSources = jobChain.getElementsByTagName("file_order_source");
            String startCause = null;
            if (fileOrderSources != null && fileOrderSources.getLength() > 0) {
                startCause = EStartCauses.FILE_TRIGGER.value();
            } else {
                startCause = EStartCauses.ORDER.value();
            }
            item.setStartCause(startCause);
            item.setInstanceId(file.getInstanceId());
            item.setFileId(file.getId());
            item.setStartCause(startCause);
            item.setCreated(ReportUtil.getCurrentDateTime());
            item.setModified(ReportUtil.getCurrentDateTime());
            /** new Items since 1.11 */
            String maxOrders = jobChain.getAttribute("max_orders");
            if(maxOrders != null && !maxOrders.isEmpty()) {
                item.setMaxOrders(Integer.parseInt(maxOrders));
            }
            item.setDistributed("yes".equalsIgnoreCase(jobChain.getAttribute("distributed")));
            if (jobChain.hasAttribute("process_class")) {
                String processClass = jobChain.getAttribute("process_class");
                DBItemInventoryProcessClass pc = processClassExists(processClass);
                if(pc != null) {
                    item.setProcessClass(pc.getBasename());
                    item.setProcessClassName(pc.getName());
                    item.setProcessClassId(pc.getId());
                } else {
                    item.setProcessClass(processClass);
                    item.setProcessClassName(processClass.substring(processClass.lastIndexOf("/" + 1)));
                    item.setProcessClassId(DBLayer.DEFAULT_ID);
                }
            } else {
                item.setProcessClassId(DBLayer.DEFAULT_ID);
                item.setProcessClassName(DBLayer.DEFAULT_NAME);
            }
            if (jobChain.hasAttribute("file_watching_process_class")) {
                String fwProcessClass = jobChain.getAttribute("file_watching_process_class");
                DBItemInventoryProcessClass ipc = processClassExists(fwProcessClass);
                if(ipc != null) {
                    item.setFileWatchingProcessClass(ipc.getBasename());
                    item.setFileWatchingProcessClassName(ipc.getName());
                    item.setFileWatchingProcessClassId(ipc.getId());
                } else {
                    item.setFileWatchingProcessClass(fwProcessClass);
                    item.setFileWatchingProcessClassName(fwProcessClass.substring(fwProcessClass.lastIndexOf("/" + 1)));
                    item.setFileWatchingProcessClassId(DBLayer.DEFAULT_ID);
                }
            } else {
                item.setFileWatchingProcessClassId(DBLayer.DEFAULT_ID);
                item.setFileWatchingProcessClassName(DBLayer.DEFAULT_NAME);
            }
            /** End of new Items */
            Long id = SaveOrUpdateHelper.saveOrUpdateJobChain(inventoryDbLayer, item, dbJobChains);
            if (id != null) {
                item.setId(id);
            }
            LOGGER.debug(String.format("%s: jobChain    id = %s, startCause = %s, jobChainName = %s, jobChainBasename = %s, title = %s", method,
                    item.getId(), item.getStartCause(), item.getName(), item.getBaseName(), item.getTitle()));
            NodeList nl = jobChain.getElementsByTagName("job_chain_node");
            int ordering = 1;
            for (int j = 0; j < nl.getLength(); j++) {
                Element jobChainNodeElement = (Element) nl.item(j);
                DBItemInventoryJobChainNode nodeItem = createInventoryJobChainNode(jobChainNodeElement, item);
                nodeItem.setInstanceId(file.getInstanceId());
                nodeItem.setOrdering(new Long(ordering));
                Long nodeId = SaveOrUpdateHelper.saveOrUpdateJobChainNode(inventoryDbLayer, nodeItem, dbJobChainNodes);
                if (nodeId != null) {
                    nodeItem.setId(nodeId);
                }
                ordering++;
                LOGGER.debug(String.format(
                        "%s: jobChainNode     id = %s, nodeName = %s, ordering = %s, state = %s, nextState = %s, errorState = %s, job = %s, "
                                + "jobName = %s", method, nodeItem.getId(), nodeItem.getName(), nodeItem.getOrdering(), nodeItem.getState(),
                                nodeItem.getNextState(), nodeItem.getErrorState(), nodeItem.getJob(), nodeItem.getJobName()));
            }
            countSuccessJobChains++;
        } catch (Exception ex) {
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {}
            LOGGER.warn(String.format("%s: job chain file cannot be inserted = %s , exception = %s", method, file.getFileName(),
                    ex.toString()), ex);
            errorJobChains.put(file.getFileName(), ex.toString());
        }
    }
    
    private DBItemInventoryJobChainNode createInventoryJobChainNode(Element jobChainNodeElement, DBItemInventoryJobChain jobChain) throws Exception {
        String nodeName = jobChainNodeElement.getNodeName();
        String job = jobChainNodeElement.getAttribute("job");
        String state = jobChainNodeElement.getAttribute("state");
        String nextState = jobChainNodeElement.getAttribute("next_state");
        String errorState = jobChainNodeElement.getAttribute("error_state");
        DBItemInventoryJobChainNode nodeItem = new DBItemInventoryJobChainNode();
        if (job != null && !job.isEmpty()) {
            DBItemInventoryJob jobItem = jobExists(job);
            if(jobItem != null) {
                nodeItem.setJobName(jobItem.getName());
                nodeItem.setJobId(jobItem.getId());
            } else {
                nodeItem.setJobId(DBLayer.DEFAULT_ID);
                nodeItem.setJobName(job);
            }
        } else {
            nodeItem.setJobId(DBLayer.DEFAULT_ID);
            nodeItem.setJobName(DBLayer.DEFAULT_NAME);
        }
        nodeItem.setJobChainId(jobChain.getId());
        nodeItem.setName(nodeName);
        nodeItem.setState(state);
        nodeItem.setNextState(nextState);
        nodeItem.setErrorState(errorState);
        nodeItem.setCreated(ReportUtil.getCurrentDateTime());
        nodeItem.setModified(ReportUtil.getCurrentDateTime());
        nodeItem.setNestedJobChainId(DBLayer.DEFAULT_ID);
        nodeItem.setNestedJobChainName(DBLayer.DEFAULT_NAME);
        /** new Items since 1.11 */
        nodeItem.setJob(job);
        nodeItem.setNodeType(getJobChainNodeType(nodeName, jobChainNodeElement));
        switch (nodeItem.getNodeType()) {
        case 1:
            if(jobChainNodeElement.hasAttribute("delay")) {
                String delay = jobChainNodeElement.getAttribute("delay");
                if(delay != null && !delay.isEmpty()) {
                    nodeItem.setDelay(Integer.parseInt(delay));
                }
            }
            if(jobChainNodeElement.hasAttribute("on_error")) {
                nodeItem.setOnError(jobChainNodeElement.getAttribute("on_error"));
            }
            break;
        case 2:
            if (jobChainNodeElement.hasAttribute("job_chain")) {
                String jobchain = jobChainNodeElement.getAttribute("job_chain");
                String jobchainName = inventoryDbLayer.getJobChainName(inventoryInstance.getId(), jobchain);
                DBItemInventoryJobChain ijc = inventoryDbLayer.getJobChainIfExists(jobChain.getInstanceId(), jobchain, jobchainName);
                if(ijc != null) {
                    nodeItem.setNestedJobChain(ijc.getBaseName());
                    nodeItem.setNestedJobChainName(ijc.getName());
                    nodeItem.setNestedJobChainId(ijc.getId());
                } else {
                    nodeItem.setNestedJobChain(jobchain);
                    nodeItem.setNestedJobChainName(jobchainName);
                    nodeItem.setNestedJobChainId(DBLayer.DEFAULT_ID);
                }
            } else {
                nodeItem.setNestedJobChainId(DBLayer.DEFAULT_ID);
                nodeItem.setNestedJobChainName(DBLayer.DEFAULT_NAME);
            }
            break;
        case 3:
            nodeItem.setDirectory(jobChainNodeElement.getAttribute("directory"));
            if (jobChainNodeElement.hasAttribute("regex")) {
                nodeItem.setRegex(jobChainNodeElement.getAttribute("regex"));
            }
            break;
        case 4:
            if (jobChainNodeElement.hasAttribute("move_to")) {
                nodeItem.setMovePath(jobChainNodeElement.getAttribute("move_to"));
                nodeItem.setFileSinkOp(1);
            } else {
                nodeItem.setFileSinkOp(2);
            }
            break;
        default:
            break;
        }
        /** End of new Items */
        return nodeItem;
    }

    private void processOrderFromNodes(Element order) throws Exception {
        String method = "    processOrder";
        DBItemInventoryFile file = processFile(order, EConfigFileExtensions.ORDER);
        countTotalOrders++;
        try {
            DBItemInventoryOrder item = new DBItemInventoryOrder();
            String name = null;
            name = file.getFileName().replace(EConfigFileExtensions.ORDER.extension(), "");
            item.setName(name);
            String baseName = file.getFileBaseName().replace(EConfigFileExtensions.ORDER.extension(), "");
            item.setBaseName(baseName);
            String title = order.getAttribute("title");
            if (title != null && !title.isEmpty()) {
                item.setTitle(title);
            }
            String jobChainBaseName = baseName.substring(0, baseName.indexOf(","));
            String directory = (file.getFileDirectory().equals(DBLayer.DEFAULT_NAME)) ? "" : file.getFileDirectory() + "/";
            String jobChainName = directory + jobChainBaseName;
            String orderId = baseName.substring(jobChainBaseName.length() + 1);
            NodeList runtimes = order.getElementsByTagName("run_time");
            item.setIsRuntimeDefined((runtimes != null && runtimes.getLength() > 0));
            item.setInstanceId(file.getInstanceId());
            item.setFileId(file.getId());
            item.setJobChainName(jobChainName);
            item.setOrderId(orderId);
            item.setCreated(ReportUtil.getCurrentDateTime());
            item.setModified(ReportUtil.getCurrentDateTime());
            /** new Items since 1.11 */
            DBItemInventoryJobChain jobChain = jobChainExists(jobChainBaseName);
            if(jobChain != null) {
                item.setJobChainId(jobChain.getId());
            } else {
                item.setJobChainId(DBLayer.DEFAULT_ID);
            }
            if(order.hasAttribute("state")) {
                item.setInitialState(order.getAttribute("state"));
            }
            if(order.hasAttribute("end_state")) {
                item.setEndState(order.getAttribute("end_state"));
            }
            if(order.hasAttribute("priority")) {
                String priority = order.getAttribute("priority");
                if(priority != null && !priority.isEmpty()) {
                    item.setPriority(Integer.parseInt(priority));
                }
            }
            String schedule = null;
            Node runtime = runtimes.item(0);
            if (runtime != null && ((Element) runtime).hasAttribute("schedule")) {
                schedule = ((Element) runtime).getAttribute("schedule");
            }
            if (schedule != null && !schedule.isEmpty()) {
                DBItemInventorySchedule is = scheduleExists(schedule);
                if (is != null) {
                    item.setSchedule(is.getBasename());
                    item.setScheduleName(is.getName());
                    item.setScheduleId(is.getId());
                } else {
                    item.setSchedule(schedule);
                    item.setScheduleName(DBLayer.DEFAULT_NAME);
                    item.setScheduleId(DBLayer.DEFAULT_ID);
                }
            } else {
                item.setSchedule("");
                item.setScheduleId(DBLayer.DEFAULT_ID);
                item.setScheduleName(DBLayer.DEFAULT_NAME);
            }
            /** End of new Items since 1.11 */
            Long id = SaveOrUpdateHelper.saveOrUpdateOrder(inventoryDbLayer, item, dbOrders);
            if (item.getId() == null) {
                item.setId(id);
            }
            LOGGER.debug(String.format("%s: order     id = %s, jobChainName = %s, orderId = %s, title = %s, isRuntimeDefined = %s", method,
                    item.getId(), item.getJobChainName(), item.getOrderId(), item.getTitle(), item.getIsRuntimeDefined()));
            countSuccessOrders++;
        } catch (Exception ex) {
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {}
            LOGGER.warn(String.format("%s: order file cannot be inserted = %s, exception = ", method, file.getFileName(), ex.toString()), ex);
            errorOrders.put(file.getFileName(), ex.toString());
        }
    }
    
    private void processProcessClassFromNodes(Element processClass) throws Exception {
        countTotalProcessClasses++;
        if (!processClass.getAttribute("path").isEmpty()) {
            DBItemInventoryFile file = processFile(processClass, EConfigFileExtensions.PROCESS_CLASS);
            try {
                DBItemInventoryProcessClass item = new DBItemInventoryProcessClass();
                String name = null;
                name = file.getFileName().replace(EConfigFileExtensions.PROCESS_CLASS.extension(), "");
                item.setName(name);
                String baseName = file.getFileBaseName().replace(EConfigFileExtensions.PROCESS_CLASS.extension(), "");
                item.setBasename(baseName);
                item.setInstanceId(file.getInstanceId());
                item.setFileId(file.getId());
                String maxProcesses = processClass.getAttribute("max_processes");
                if(maxProcesses != null && !maxProcesses.isEmpty()) {
                    item.setMaxProcesses(Integer.parseInt(maxProcesses));
                }
                String remoteScheduler = processClass.getAttribute("remote_scheduler");
                NodeList remoteSchedulers = processClass.getElementsByTagName("remote_scheduler");
                if(remoteScheduler != null && !remoteScheduler.isEmpty()) {
                    item.setHasAgents(true);
                } else {
                    item.setHasAgents(remoteSchedulers != null && remoteSchedulers.getLength() > 0);
                }
                Long id = SaveOrUpdateHelper.saveOrUpdateProcessClass(inventoryDbLayer, item, dbProcessClasses);
                if(item.getId() == null) {
                    item.setId(id);
                }
                countSuccessProcessClasses++;
                if (item.getHasAgents()) {
                    Map<String,Integer> remoteSchedulerUrls = getRemoteSchedulersFromProcessClass(remoteSchedulers);
                    if(remoteSchedulerUrls != null && !remoteSchedulerUrls.isEmpty()) {
                        NodeList remoteSchedulersParent = processClass.getElementsByTagName("remote_schedulers");
                        if(remoteSchedulersParent != null && remoteSchedulersParent.getLength() > 0) {
                            Element remoteSchedulerParent = (Element)remoteSchedulersParent.item(0);
                            String schedulingType = remoteSchedulerParent.getAttribute("select");
                            if(schedulingType != null && !schedulingType.isEmpty()) {
                                processAgentCluster(remoteSchedulerUrls, schedulingType, item.getInstanceId(), item.getId());
                            } else if (remoteSchedulerUrls.size() == 1) {
                                processAgentCluster(remoteSchedulerUrls, "single", item.getInstanceId(), item.getId());
                            } else {
                                processAgentCluster(remoteSchedulerUrls, "first", item.getInstanceId(), item.getId());
                            }
                        }
                    } else {
                        remoteSchedulerUrls = new HashMap<String, Integer>();
                        if(remoteScheduler != null && !remoteScheduler.isEmpty()) {
                            remoteSchedulerUrls.put(remoteScheduler.toLowerCase(), 1);
                            processAgentCluster(remoteSchedulerUrls, "single", item.getInstanceId(), item.getId());
                        }
                    }
                }
            } catch (Exception ex) {
                try {
                    getDbLayer().getConnection().rollback();
                } catch (Exception e) {}
                LOGGER.warn(String.format("    processProcessClass: processClass file cannot be inserted = %s, exception = %s ", file.getFileName(),
                        ex.toString()), ex);
                errorProcessClasses.put(file.getFileName(), ex.toString());
            }
        }
    }
    
    private void processLockFromNodes(Element lock) throws Exception {
        DBItemInventoryFile file = processFile(lock, EConfigFileExtensions.LOCK);
        countTotalLocks++;
        try {
            DBItemInventoryLock item = new DBItemInventoryLock();
            String name = null;
            name = file.getFileName().replace(EConfigFileExtensions.LOCK.extension(), "");
            item.setName(name);
            String baseName = file.getFileBaseName().replace(EConfigFileExtensions.LOCK.extension(), "");
            item.setBasename(baseName);
            item.setInstanceId(file.getInstanceId());
            item.setFileId(file.getId());
            String maxNonExclusive = lock.getAttribute("max_non_exclusive");
            if (maxNonExclusive != null && !maxNonExclusive.isEmpty()) {
                item.setMaxNonExclusive(Integer.parseInt(maxNonExclusive));
            }
            SaveOrUpdateHelper.saveOrUpdateLock(inventoryDbLayer, item, dbLocks);
            countSuccessLocks++;
        } catch (Exception ex) {
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {}
            LOGGER.warn(String.format("    processLock: lock file cannot be inserted = %s, exception = %s ", file.getFileName(), ex.toString()), ex);
            errorLocks.put(file.getFileName(), ex.toString());
        }
    }
    
    private void processScheduleFromNodes(Element schedule) throws Exception {
        DBItemInventoryFile file = processFile(schedule, EConfigFileExtensions.SCHEDULE);
        countTotalSchedules++;
        try {
            DBItemInventorySchedule item = new DBItemInventorySchedule();
            String name = null;
            name = file.getFileName().replace(EConfigFileExtensions.SCHEDULE.extension(), "");
            item.setName(name);
            String baseName = file.getFileBaseName().replace(EConfigFileExtensions.SCHEDULE.extension(), "");
            item.setBasename(baseName);
            item.setInstanceId(file.getInstanceId());
            item.setFileId(file.getId());
            String title = schedule.getAttribute("title");
            if (title != null && !title.isEmpty()) {
                item.setTitle(title);
            }
            item.setSubstitute(schedule.getAttribute("substitute"));
            String timezone = inventoryInstance.getTimeZone();
            item.setSubstituteValidFrom(getSubstituteValidFromTo(schedule, "valid_from", timezone));
            item.setSubstituteValidTo(getSubstituteValidFromTo(schedule, "valid_to", timezone));
            DBItemInventorySchedule substituteItem = scheduleExists(item.getSubstitute());
            if(substituteItem != null) {
                item.setSubstituteId(substituteItem.getId());
                item.setSubstituteName(substituteItem.getName());
            } else {
                item.setSubstituteId(DBLayer.DEFAULT_ID);
                item.setSubstituteName(DBLayer.DEFAULT_NAME);
            }
            SaveOrUpdateHelper.saveOrUpdateSchedule(inventoryDbLayer, item, dbSchedules);
            countSuccessSchedules++;
        } catch (Exception ex) {
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {}
            LOGGER.warn(String.format("processSchedule: schedule file cannot be inserted = %s, exception = %s ", file.getFileName(), ex.toString()), ex);
            errorSchedules.put(file.getFileName(), ex.toString());
        }
    }
    
    private Date getSubstituteValidFromTo(Element schedule, String attribute, String timezone) throws Exception {
        String validFromTo = schedule.getAttribute(attribute);
        if(validFromTo != null && !validFromTo.isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.parse(validFromTo, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.of(timezone));
            Instant valid = zdt.toInstant();
            return Date.from(valid);
        } else {
            return null;
        } 
    }
    
    private DBItemInventoryProcessClass processClassExists(String pcName) {
        for(DBItemInventoryProcessClass pc : dbProcessClasses) {
            if(pcName.equalsIgnoreCase(pc.getName())) {
                return pc;
            } else {
                continue;
            }
        }
        return null;
    }
    
    private DBItemInventorySchedule scheduleExists(String scheduleName) {
        for(DBItemInventorySchedule schedule : dbSchedules) {
            if(scheduleName.equalsIgnoreCase(schedule.getBasename())) {
                return schedule;
            } else {
                continue;
            }
        }
        return null;
    }
    
    private DBItemInventoryLock lockExists(String lockName) {
        for(DBItemInventoryLock lock : dbLocks) {
            if(lockName.equalsIgnoreCase(lock.getBasename())) {
                return lock;
            } else {
                continue;
            }
        }
        return null;
    }
    
    private DBItemInventoryJob jobExists(String jobName) {
        for(DBItemInventoryJob job : dbJobs) {
            if(jobName.equalsIgnoreCase(job.getBaseName())) {
                return job;
            } else {
                continue;
            }
        }
        return null;
    }
    
    private DBItemInventoryJobChain jobChainExists(String jobChainName) {
        for(DBItemInventoryJobChain jobChain : dbJobChains) {
            if(jobChainName.equalsIgnoreCase(jobChain.getBaseName())) {
                return jobChain;
            } else {
                continue;
            }
        }
        return null;
    }
    
    private DBItemInventoryAppliedLock appliedLockExists(Long lockId, Long jobId) {
        for(DBItemInventoryAppliedLock appliedLock : dbAppliedLocks) {
            if(lockId == appliedLock.getLockId() && jobId == appliedLock.getJobId()) {
                return appliedLock;
            } else {
                continue;
            }
        }
        return null;
    }
    
    public void setAnswerXml(String answerXml) {
        this.answerXml = answerXml;
    }
    
    public static Map<String,Integer> getRemoteSchedulersFromProcessClass(NodeList remoteSchedulers) throws Exception {
        int ordering = 1;
        Map<String, Integer> remoteSchedulerUrls = new HashMap<String, Integer>();
        for (int i = 0; i < remoteSchedulers.getLength(); i++) {
            Element remoteScheduler = (Element)remoteSchedulers.item(i);
            String url = remoteScheduler.getAttribute("remote_scheduler");
            if(url != null && !url.isEmpty()) {
                remoteSchedulerUrls.put(url, ordering);
                ordering++;
            }
        }
        return remoteSchedulerUrls;
    }
    
}