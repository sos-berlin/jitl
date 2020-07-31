package com.sos.jitl.inventory.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.classes.Latin1ToUtf8;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.UtcTimeHelper;
import com.sos.jitl.dailyplan.db.Calendar2DB;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.inventory.exceptions.SOSInventoryModelProcessingException;
import com.sos.jitl.inventory.helper.Calendar2DBHelper;
import com.sos.jitl.inventory.helper.HttpHelper;
import com.sos.jitl.inventory.helper.InventoryRuntimeHelper;
import com.sos.jitl.inventory.helper.SaveOrUpdateHelper;
import com.sos.jitl.reporting.db.DBItemDocumentation;
import com.sos.jitl.reporting.db.DBItemDocumentationUsage;
import com.sos.jitl.reporting.db.DBItemInventoryAgentCluster;
import com.sos.jitl.reporting.db.DBItemInventoryAgentClusterMember;
import com.sos.jitl.reporting.db.DBItemInventoryAgentInstance;
import com.sos.jitl.reporting.db.DBItemInventoryAppliedLock;
import com.sos.jitl.reporting.db.DBItemInventoryClusterCalendarUsage;
import com.sos.jitl.reporting.db.DBItemInventoryFile;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryJob;
import com.sos.jitl.reporting.db.DBItemInventoryJobChain;
import com.sos.jitl.reporting.db.DBItemInventoryJobChainNode;
import com.sos.jitl.reporting.db.DBItemInventoryLock;
import com.sos.jitl.reporting.db.DBItemInventoryOrder;
import com.sos.jitl.reporting.db.DBItemInventoryProcessClass;
import com.sos.jitl.reporting.db.DBItemInventorySchedule;
import com.sos.jitl.reporting.db.DBItemSubmission;
import com.sos.jitl.reporting.db.DBItemSubmittedObject;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.helper.EConfigFileExtensions;
import com.sos.jitl.reporting.helper.EStartCauses;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.reporting.plugin.FactEventHandler.CustomEventType;
import com.sos.scheduler.SOSJobSchedulerGlobal;
import com.sos.scheduler.engine.data.events.custom.VariablesCustomEvent;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

import sos.xml.SOSXMLXPath;

public class InventoryModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryModel.class);
    private static final String DEFAULT_PROCESS_CLASS_NAME = "(default)";
    private static final String COMMAND = "<show_state what=\"cluster source job_chains job_chain_orders schedules\" />";
    private static final String DEFAULT_JOB_DOC_PATH = "/sos/jitl-jobs";
    private DBItemInventoryInstance inventoryInstance;
    private int countTotalJobs = 0;
    private int countSuccessJobs = 0;
    private int countTotalJobChains = 0;
    private int countSuccessJobChains = 0;
    private int countTotalOrders = 0;
    private int countSuccessOrders = 0;
    private int countNotFoundJobChainJobs = 0;
    private int countTotalLocks = 0;
    private int countTotalMonitors = 0;
    private int countSuccessLocks = 0;
    private int countTotalProcessClasses = 0;
    private int countSuccessProcessClasses = 0;
    private int countTotalSchedules = 0;
    private int countSuccessSchedules = 0;
    private LinkedHashMap<String, ArrayList<String>> notFoundJobChainJobs;
    private LinkedHashMap<String, String> errorJobChains;
    private LinkedHashMap<String, String> errorOrders;
    private LinkedHashMap<String, String> errorJobs;
    private Map<String, String> errorLocks;
    private Map<String, String> errorProcessClasses;
    private Map<String, String> errorSchedules;
    private Date started;
    private Path schedulerXmlPath;
    private String answerXml;
    private List<DBItemInventoryFile> dbFiles;
    private List<DBItemInventoryJob> dbJobs;
    private List<DBItemInventoryJobChain> dbJobChains;
    private List<DBItemInventoryOrder> dbOrders;
    private List<DBItemInventoryProcessClass> dbProcessClasses;
    private List<DBItemInventorySchedule> dbSchedules;
    private List<DBItemInventoryLock> dbLocks;
    private List<DBItemInventoryAppliedLock> dbAppliedLocks;
    private List<DBItemInventoryAgentCluster> dbAgentCLusters;
    private List<DBItemInventoryAgentClusterMember> dbAgentClusterMembers;
    private List<DBItemInventoryClusterCalendarUsage> dbCalendarUsages;
    private List<Long> dbCalendarIds;
    private DBLayerInventory inventoryDbLayer;
    private SOSXMLXPath xPathAnswerXml;
    private Integer filesDeleted;
    private Integer jobsDeleted;
    private Integer jobChainsDeleted;
    private Integer jobChainNodesDeleted;
    private Integer ordersDeleted;
    private Integer appliedLocksDeleted;
    private Integer locksDeleted;
    private Integer schedulesDeleted;
    private Integer processClassesDeleted;
    private Integer agentClustersDeleted;
    private Integer agentClusterMembersDeleted;
    private SchedulerXmlCommandExecutor xmlCommandExecutor;
    private SOSHibernateFactory factory;
    private String httpHost;
    private Integer httpPort;
    private Path liveDirectory; 
    private String timezone;
    private EventPublisher customEventBus;

    public InventoryModel(SOSHibernateFactory factory, DBItemInventoryInstance jsInstanceItem, Path schedulerXmlPath, EventPublisher customEventBus)
            throws Exception {
        this.schedulerXmlPath = schedulerXmlPath;
        this.inventoryInstance = jsInstanceItem;
        this.factory = factory;
        this.timezone = inventoryInstance.getTimeZone();
        this.customEventBus = customEventBus;
    }

    public InventoryModel(SOSHibernateFactory factory, DBItemInventoryInstance jsInstanceItem, Path schedulerXmlPath) throws Exception {
        this.schedulerXmlPath = schedulerXmlPath;
        this.inventoryInstance = jsInstanceItem;
        this.factory = factory;
        this.timezone = inventoryInstance.getTimeZone();
        this.customEventBus = null;
    }

    public void process() throws Exception {
        String method = "process";
        SOSHibernateSession connection = null;
        try {
            connection = factory.openStatelessSession();
            inventoryDbLayer = new DBLayerInventory(connection);
            String toTimeZoneString = "UTC";
            String fromTimeZoneString = DateTimeZone.getDefault().getID();
            started = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime());
            processUncommittedSubmissions();
            initCounters();
            initExistingItems();
            connection.beginTransaction();
            processSchedulerXml();
            connection.commit();
            connection.close();
            if (answerXml == null && xmlCommandExecutor != null) {
                answerXml = xmlCommandExecutor.executeXml(COMMAND);
            }
            xPathAnswerXml = new SOSXMLXPath(new StringBuffer(answerXml));
            String httpPortFromAnswerXml = xPathAnswerXml.selectSingleNodeValue("/spooler/answer/state/@http_port");
            if (httpPortFromAnswerXml != null && !httpPortFromAnswerXml.isEmpty()) {
                httpHost = HttpHelper.getHttpHost(httpPortFromAnswerXml, "localhost");
                httpPort = HttpHelper.getHttpPort(httpPortFromAnswerXml);
            }            
            processStateAnswerXML();
            connection = factory.openStatelessSession();
            inventoryDbLayer = new DBLayerInventory(connection);
            connection.beginTransaction();
            inventoryDbLayer.refreshUsedInJobChains(inventoryInstance.getId(), dbJobs);
            connection.commit();
            connection.close();
            connection = factory.openStatelessSession();
            inventoryDbLayer = new DBLayerInventory(connection);
            connection.beginTransaction();
            cleanUpInventoryAfter(started);
            connection.commit();
            connection.close();
            connection = factory.openStatelessSession();
            inventoryDbLayer = new DBLayerInventory(connection);
            updateDailyPlan(connection);
            logSummary();
            resume();
        } catch (Exception ex) {
            try {
                if (inventoryDbLayer != null && inventoryDbLayer.getSession() != null) {
                    inventoryDbLayer.getSession().rollback();
                }
            } catch (Exception e) {}
            throw new SOSInventoryModelProcessingException(String.format("%s: %s", method, ex.toString()), ex);
        } finally {
            connection.close();
            try {
                if (customEventBus != null) {
                    Map<String,String> valueMap = new HashMap<String, String>();
                    valueMap.put("DBUpdate", "finished");
                    customEventBus.publishCustomEvent(VariablesCustomEvent.keyed("InventoryInitialized", valueMap));
                    LOGGER.info("[inventory] Initialization finished. Custom Event - InventoryInitialized - published!");
                    valueMap = new HashMap<String, String>();
                    valueMap.put(CustomEventType.DailyPlanChanged.name(), "InventoryDailyPlanUpdated");
                    customEventBus.publishCustomEvent(VariablesCustomEvent.keyed("DailyPlan", valueMap));
                    LOGGER.info("[inventory] Custom Event - Daily Plan updated - published on inventory initialization!");
                } else {
                    LOGGER.info("[inventory] Custom Events not published due to errors or EventBus is NULL!");
                }
            } catch (Exception e) {
                LOGGER.info("[inventory] Custom Events not published!");
            }
        }
    }

    public void setXmlCommandExecutor(SchedulerXmlCommandExecutor xmlCommandExecutor) {
        this.xmlCommandExecutor = xmlCommandExecutor;
    }
    
    private void initExistingItems() throws Exception {
        inventoryDbLayer.getSession().beginTransaction();
        dbFiles = inventoryDbLayer.getAllFilesForInstance(inventoryInstance.getId());
        dbJobs = inventoryDbLayer.getAllJobsForInstance(inventoryInstance.getId());
        dbJobChains = inventoryDbLayer.getAllJobChainsForInstance(inventoryInstance.getId());
        dbOrders = inventoryDbLayer.getAllOrdersForInstance(inventoryInstance.getId());
        dbProcessClasses = inventoryDbLayer.getAllProcessClassesForInstance(inventoryInstance.getId());
        dbSchedules = inventoryDbLayer.getAllSchedulesForInstance(inventoryInstance.getId());
        dbLocks = inventoryDbLayer.getAllLocksForInstance(inventoryInstance.getId());
        dbAppliedLocks = inventoryDbLayer.getAllAppliedLocks();
        dbAgentCLusters = inventoryDbLayer.getAllAgentClustersForInstance(inventoryInstance.getId());
        dbAgentClusterMembers = inventoryDbLayer.getAllAgentClusterMembersForInstance(inventoryInstance.getId());
        dbCalendarUsages = inventoryDbLayer.getAllCalendarUsagesForSchedulerId(inventoryInstance.getSchedulerId());
        dbCalendarIds = inventoryDbLayer.getAllCalendarIds();
        inventoryDbLayer.getSession().commit();
    }

    private void initCounters() {
        countTotalJobs = 0;
        countTotalJobChains = 0;
        countTotalOrders = 0;
        countSuccessJobs = 0;
        countSuccessJobChains = 0;
        countSuccessOrders = 0;
        countNotFoundJobChainJobs = 0;
        countTotalLocks = 0;
        countSuccessLocks = 0;
        countTotalProcessClasses = 0;
        countSuccessProcessClasses = 0;
        countTotalSchedules = 0;
        countSuccessSchedules = 0;
        countTotalMonitors = 0;
        notFoundJobChainJobs = new LinkedHashMap<String, ArrayList<String>>();
        errorJobChains = new LinkedHashMap<String, String>();
        errorOrders = new LinkedHashMap<String, String>();
        errorJobs = new LinkedHashMap<String, String>();
        errorLocks = new LinkedHashMap<String, String>();
        errorProcessClasses = new LinkedHashMap<String, String>();
        errorSchedules = new LinkedHashMap<String, String>();
    }

    private void logSummary() {
        String method = "logSummary";
        LOGGER.debug(String.format("%s: inserted or updated jobs = %s (total %s, error = %s)", method, countSuccessJobs,
                countTotalJobs, errorJobs.size()));
        LOGGER.debug(String.format("%s: inserted or updated job chains = %s (total %s, error = %s)", method,
                countSuccessJobChains, countTotalJobChains, errorJobChains.size()));
        LOGGER.debug(String.format("%s: inserted or updated orders = %s (total %s, error = %s)", method, countSuccessOrders,
                countTotalOrders, errorOrders.size()));
        LOGGER.debug(String.format("%s: inserted or updated process classes = %s (total %s, error = %s)", method,
                countSuccessProcessClasses, countTotalProcessClasses, errorProcessClasses.size()));
        LOGGER.debug(String.format("%s: inserted or updated schedules = %s (total %s, error = %s)", method, countSuccessSchedules, 
                countTotalSchedules, errorSchedules.size()));
        LOGGER.debug(String.format("%s: inserted or updated locks = %s (total %s, error = %s)", method, countSuccessLocks,
                countTotalLocks, errorLocks.size()));
        LOGGER.debug(String.format("%s: inserted or updated monitors = %s", method, countTotalMonitors));
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
        if (countNotFoundJobChainJobs > 0) {
            LOGGER.debug(String.format("%s: jobs not found on the disc (declared in the job chains) = %s", method,
                    countNotFoundJobChainJobs));
            int i = 1;
            for (Entry<String, ArrayList<String>> entry : notFoundJobChainJobs.entrySet()) {
                LOGGER.debug(String.format("%s:     %s) %s", method, i, entry.getKey()));
                for (int j = 0; j < entry.getValue().size(); j++) {
                    LOGGER.debug(String.format("%s:         %s) %s", method, j + 1, entry.getValue().get(j)));
                }
                i++;
            }
        }
        LOGGER.debug(String.format("cleanUpInventoryAfter: delete Inventory entries older than %1$s", started.toString()));
        LOGGER.debug(String.format("%1$s old Files deleted from inventory.", filesDeleted));
        LOGGER.debug(String.format("%1$s old Jobs deleted from inventory.", jobsDeleted));
        LOGGER.debug(String.format("%1$s old JobChains deleted from inventory.", jobChainsDeleted));
        LOGGER.debug(String.format("%1$s old JobChainNodes deleted from inventory.", jobChainNodesDeleted));
        LOGGER.debug(String.format("%1$s old Orders deleted from inventory.", ordersDeleted));
        LOGGER.debug(String.format("%1$s old Locks deleted from inventory.", locksDeleted));
        LOGGER.debug(String.format("%1$s old Applied Locks deleted from inventory.", appliedLocksDeleted));
        LOGGER.debug(String.format("%1$s old Schedules deleted from inventory.", schedulesDeleted));
        LOGGER.debug(String.format("%1$s old Process Classes deleted from inventory.", processClassesDeleted));
        LOGGER.debug(String.format("%1$s old Agent Clusters deleted from inventory.", agentClustersDeleted));
        LOGGER.debug(String.format("%1$s old Agent Cluster Members deleted from inventory.", agentClusterMembersDeleted));
    }

    private void resume() throws Exception {
        if (countSuccessJobChains == 0 && countSuccessOrders == 0 && countSuccessJobs == 0 && countSuccessLocks == 0
                && countSuccessProcessClasses == 0 && countSuccessSchedules == 0) {
            throw new Exception(String.format("error occured: 0 job chains, orders, jobs locks, "
                    + "process classes or schedules inserted or updated!"));
        }
        if (!errorJobChains.isEmpty() || !errorOrders.isEmpty() || !errorJobs.isEmpty() || !errorLocks.isEmpty()
                || !errorProcessClasses.isEmpty() || !errorSchedules.isEmpty()) {
            LOGGER.warn(
                    String.format("error occured: insert or update failed by %s job chains, %s orders, %s jobs, %s locks, "
                            + "%s process classes, %s schedules", errorJobChains.size(), errorOrders.size(), errorJobs.size(),
                            errorLocks.size(), errorProcessClasses.size(), errorSchedules.size()));
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
            attrs = Files.readAttributes(schedulerXmlPath, BasicFileAttributes.class);
            fileCreated = ReportUtil.convertFileTime2UTC(attrs.creationTime());
            fileModified = ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime());
            fileLocalCreated = ReportUtil.convertFileTime2Local(attrs.creationTime());
            fileLocalModified = ReportUtil.convertFileTime2Local(attrs.lastModifiedTime());
        } catch (IOException exception) {
            LOGGER.debug(String.format("%s: cannot read file attributes. file = %s, exception = %s  ", method,
                    schedulerXmlPath.toString(), exception.toString()));
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
                "%s: file     id = %s, fileType = %s, fileName = %s, fileBasename = %s, fileDirectory = %s, fileCreated = %s,"
                + " fileModified = %s", method, item.getId(), item.getFileType(), item.getFileName(), item.getFileBaseName(),
                item.getFileDirectory(), item.getFileCreated(), item.getFileModified()));
        return item;
    }

    private Integer getJobChainNodeType(String nodeName, Element jobChainNode) {
        switch(nodeName){
        case "job_chain_node":
            if(jobChainNode.hasAttribute("job")){
                return 1;
            } else if (jobChainNode.hasAttribute("job_chain")) {
                return 2;
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
            DBItemInventoryFile dbItemFile = processFileForObjectsInSchedulerXml(DEFAULT_PROCESS_CLASS_NAME,
                    EConfigFileExtensions.PROCESS_CLASS.type());
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
            LOGGER.warn(String.format("processDefaultProcessClass: default processClass cannot be inserted, exception = %s ",
                    ex.toString()), ex);
            errorProcessClasses.put(name, ex.toString());
        }
    }

    private void processAgentCluster(Map<String,Integer> remoteSchedulers, String schedulingType, Long instanceId,
            Long processClassId) throws Exception {
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
    
    private void cleanUpInventoryAfter(Date started) throws Exception {
        filesDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_FILES, inventoryInstance.getId());
        jobsDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_JOBS, inventoryInstance.getId());
        jobChainsDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_JOB_CHAINS,
                inventoryInstance.getId());
        jobChainNodesDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_JOB_CHAIN_NODES,
                inventoryInstance.getId());
        ordersDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_ORDERS, inventoryInstance.getId());
        appliedLocksDeleted = inventoryDbLayer.deleteAppliedLocksFromDb(started, inventoryInstance.getId());
        locksDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_LOCKS, inventoryInstance.getId());
        schedulesDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_SCHEDULES,
                inventoryInstance.getId());
        processClassesDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_PROCESS_CLASSES,
                inventoryInstance.getId());
        agentClustersDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_AGENT_CLUSTER,
                inventoryInstance.getId());
        agentClusterMembersDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS,
                inventoryInstance.getId());
        // remove runtimes based on the current (deleted calendar) usage
        for (DBItemInventoryClusterCalendarUsage usage : dbCalendarUsages) {
            if (!dbCalendarIds.contains(usage.getCalendarId())) {
                inventoryDbLayer.getSession().delete(usage);
            }
        }
    }
    
    private void processSchedulerXml() throws Exception {
        SOSXMLXPath xPathSchedulerXml = new SOSXMLXPath(schedulerXmlPath);
        String maxProcesses = xPathSchedulerXml.selectSingleNodeValue(
                "/spooler/config/process_classes/process_class[not(@name)]/@max_processes");
        if(maxProcesses != null && !maxProcesses.isEmpty()) {
            processDefaultProcessClass(Integer.parseInt(maxProcesses));
        } else {
            processDefaultProcessClass(30);
        }
        String supervisor = xPathSchedulerXml.selectSingleNodeValue("/spooler/config/@supervisor");
        if (supervisor != null && !supervisor.isEmpty()) {
            DBItemInventoryInstance supervisorInstance = inventoryDbLayer.getInventorySupervisorInstance(supervisor);
            DBItemInventoryInstance updateInstance = inventoryDbLayer.getInventoryInstance(inventoryInstance.getId());
            if (supervisorInstance != null) {
                updateInstance.setSupervisorId(supervisorInstance.getId());
                inventoryDbLayer.getSession().update(updateInstance);
            }
        }
    }
    
    private void processStateAnswerXML() throws Exception {
        SOSHibernateSession connection = factory.openStatelessSession();
        try {
            inventoryDbLayer = new DBLayerInventory(connection);
            connection.beginTransaction();
            NodeList processClassNodes =
                    xPathAnswerXml.selectNodeList("/spooler/answer/state/process_classes/process_class[file_based/@file]");
            for (int i = 0; i < processClassNodes.getLength(); i++) {
                try {
                    processProcessClassFromNodes((Element)processClassNodes.item(i));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    continue;
                }
            }
            NodeList lockNodes = xPathAnswerXml.selectNodeList("/spooler/answer/state/locks/lock[file_based/@file]");
            for (int i = 0; i < lockNodes.getLength(); i++) {
                try {
                    processLockFromNodes((Element)lockNodes.item(i));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    continue;
                }
            }
            NodeList scheduleNodes = xPathAnswerXml.selectNodeList("/spooler/answer/state/schedules/schedule[file_based/@file]");
            for (int i = 0; i < scheduleNodes.getLength(); i++) {
                try {
                    processScheduleFromNodes((Element)scheduleNodes.item(i));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    continue;
                }
            }
            NodeList jobNodes = xPathAnswerXml.selectNodeList("/spooler/answer/state/jobs/job[file_based/@file]");
            for(int i = 0; i < jobNodes.getLength(); i++) {
                try {
                    processJobFromNodes((Element)jobNodes.item(i));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    continue;
                }
            }
            NodeList jobChainNodes =
                    xPathAnswerXml.selectNodeList("/spooler/answer/state/job_chains/job_chain[file_based/@file]");
            for (int i = 0; i < jobChainNodes.getLength(); i++) {
                try {
                    processJobChainFromNodes((Element)jobChainNodes.item(i));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    continue;
                }
            }
            NodeList orderNodes =
                    xPathAnswerXml.selectNodeList(
                            "/spooler/answer/state/job_chains/job_chain/job_chain_node/order_queue/order[file_based/@file or (@in_database_only='yes' and @order_source_type='Permanent')]");
            for (int i = 0; i < orderNodes.getLength(); i++) {
                try {
                    processOrderFromNodes((Element)orderNodes.item(i));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    continue;
                }
            }
            NodeList monitorNodes = xPathAnswerXml.selectNodeList("/spooler/answer/state/monitors/monitor[file_based/@file]");
            for (int i = 0; i < monitorNodes.getLength(); i++) {
                try {
                    processMonitorFromNodes((Element)monitorNodes.item(i));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    continue;
                }
            }
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception ex) {}
            throw new SOSInventoryModelProcessingException(e);
        } finally {
            connection.close();
        }
    }
    
    private DBItemInventoryFile processFile(Element element, EConfigFileExtensions fileExtension, Boolean save) throws Exception {
        String fileName = null;
        if (element.hasAttribute("path")) {
            fileName = Latin1ToUtf8.convert(element.getAttribute("path") + fileExtension.extension());
        }
        String fileBasename = fileName.substring(fileName.lastIndexOf("/") + 1);
        String fileDirectory = fileName.substring(0, fileName.lastIndexOf("/"));
        String fileType = fileExtension.type();
        String method = "  processFile";
        Date fileCreated = null;
        Date fileModified = null;
        Date fileLocalCreated = null;
        Date fileLocalModified = null;
        Path path = null;
        Element fileBasedElem = (Element) xPathAnswerXml.selectSingleNode(element, "file_based");
        if (fileBasedElem.hasAttribute("file")) {
            path = Paths.get(Latin1ToUtf8.convert(fileBasedElem.getAttribute("file")));
        } else {
            path = liveDirectory.resolve(fileName.substring(1));
        }
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            fileCreated = ReportUtil.convertFileTime2UTC(attrs.creationTime());
            fileModified = ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime());
            fileLocalCreated = ReportUtil.convertFileTime2Local(attrs.creationTime());
            fileLocalModified = ReportUtil.convertFileTime2Local(attrs.lastModifiedTime());
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
            if(save != null && save) {
                Long id = SaveOrUpdateHelper.saveOrUpdateFile(inventoryDbLayer, item, dbFiles);
                if(item.getId() == null) {
                    item.setId(id);
                }
            }
            LOGGER.debug(String.format(
                    "%s: file     id = %s, fileType = %s, fileName = %s, fileBasename = %s, fileDirectory = %s, fileCreated = %s,"
                    + " fileModified = %s", method, item.getId(), item.getFileType(), item.getFileName(), item.getFileBaseName(),
                    item.getFileDirectory(), item.getFileCreated(), item.getFileModified()));
            return item;
        } catch (IOException | IllegalArgumentException exception) {
            LOGGER.debug(String.format("%s: cannot read file attributes. file = %s, exception = %s  ", method, path.toString(),
                    exception.toString()));
            return null;
        }
    }
    
    private void processJobFromNodes(Element job) throws Exception {
        String method = "    processJob";
        DBItemInventoryFile file = processFile(job, EConfigFileExtensions.JOB, true);
        if (file != null) {
            countTotalJobs++;
            Element jobSource = (Element)xPathAnswerXml.selectSingleNode(job, "source/job");
            if (jobSource == null) {
                jobSource = getSourceFromFile(job);
            }
            try {
                DBItemInventoryJob item = new DBItemInventoryJob();
                String name = null;
                name = file.getFileName().replace(EConfigFileExtensions.JOB.extension(), "");
                item.setName(name);
                String baseName = file.getFileBaseName().replace(EConfigFileExtensions.JOB.extension(), "");
                item.setBaseName(baseName);
                String title = jobSource.getAttribute("title");
                if (title != null && !title.isEmpty()) {
                    item.setTitle(title);
                }
                String criticality = jobSource.getAttribute("criticality");
                if (criticality != null && !criticality.isEmpty()) {
                    item.setCriticality(criticality);
                }else {
                    item.setCriticality(SOSJobSchedulerGlobal.JOB_CRITICALITY.NORMAL.toString());
                }
                boolean isOrderJob = jobSource.hasAttribute("order") && "yes,1,true".contains(jobSource.getAttribute("order")
                        .toLowerCase());
                item.setIsOrderJob(isOrderJob);
                Node runTimeNode = xPathAnswerXml.selectSingleNode(jobSource, "run_time");
                boolean isRuntimeDefined = false;
                if(runTimeNode != null) {
                    if(((Element)runTimeNode).hasAttribute("schedule")) {
                        isRuntimeDefined = true;
                    } else if(runTimeNode.hasChildNodes()) {
                        NodeList childNodes = runTimeNode.getChildNodes();
                        for (int i = 0; i < childNodes.getLength(); i++) {
                            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE){
                                isRuntimeDefined = true;
                                break;
                            }
                        }
                    }
                    item.setIsRuntimeDefined(isRuntimeDefined);
                } else {
                    item.setIsRuntimeDefined(false);
                }
                item.setInstanceId(file.getInstanceId());
                item.setFileId(file.getId());
                item.setCreated(ReportUtil.getCurrentDateTime());
                item.setModified(ReportUtil.getCurrentDateTime());
                /** new Items since 1.11 */
                if (jobSource.hasAttribute("process_class")) {
                    String processClass = jobSource.getAttribute("process_class");
                    Path path = Paths.get(item.getName());
                    processClass = path.getParent().resolve(processClass).normalize().toString().replace('\\', '/');
                    DBItemInventoryProcessClass pc = processClassExists(processClass);
                    if(pc != null) {
                        item.setProcessClass(processClass);
                        item.setProcessClassName(pc.getName());
                        item.setProcessClassId(pc.getId());
                    } else {
                        item.setProcessClass(processClass);
                        Path jobPath = Paths.get(item.getName());
                        String resolvedProcessClassPath = 
                                jobPath.getParent().resolve(processClass).normalize().toString().replace('\\', '/');
                        item.setProcessClassName(resolvedProcessClassPath);
                        item.setProcessClassId(DBLayer.DEFAULT_ID);
                    }
                } else {
                    item.setProcessClass(null);
                    item.setProcessClassName(DBLayer.DEFAULT_NAME);
                    item.setProcessClassId(DBLayer.DEFAULT_ID);
                }
                String schedule = null;
                if (runTimeNode != null && ((Element) runTimeNode).hasAttribute("schedule")) {
                    schedule = ((Element) runTimeNode).getAttribute("schedule");
                }
                if (schedule != null && !schedule.isEmpty()) {
                    Path path = Paths.get(item.getName()).getParent();
                    String resolvedSchedulePath = path.resolve(schedule).normalize().toString().replace("\\", "/");
                    DBItemInventorySchedule is = scheduleExists(resolvedSchedulePath);
                    if (is != null) {
                        item.setSchedule(schedule);
                        item.setScheduleName(is.getName());
                        item.setScheduleId(is.getId());
                    } else {
                        item.setSchedule(schedule);
                        item.setScheduleName(resolvedSchedulePath);
                        item.setScheduleId(DBLayer.DEFAULT_ID);
                    }
                } else {
                    item.setSchedule(null);
                    item.setScheduleName(DBLayer.DEFAULT_NAME);
                    item.setScheduleId(DBLayer.DEFAULT_ID);
                }
                String maxTasks = jobSource.getAttribute("tasks");
                if(maxTasks != null && !maxTasks.isEmpty()) {
                    item.setMaxTasks(Integer.parseInt(maxTasks));
                } else {
                    item.setMaxTasks(1);
                }
                NodeList description = jobSource.getElementsByTagName("description");
                item.setHasDescription((description != null && description.getLength() > 0));
                Node scriptNode = xPathAnswerXml.selectSingleNode(jobSource, "script");
                boolean isYadeJob = false;
                if(scriptNode != null) {
                    if(((Element)scriptNode).hasAttribute("java_class")) {
                        String script = ((Element)scriptNode).getAttribute("java_class");
                        switch(script){
                        case "sos.scheduler.jade.JadeJob":
                        case "sos.scheduler.jade.Jade4DMZJob":
                        case "sos.scheduler.jade.SFTPSendJob":
                        case "sos.scheduler.jade.SFTPReceiveJob":
                        case "sos.scheduler.job.SOSDExJSAdapterClass":
                        case "sos.scheduler.job.SOSJade4DMZJSAdapter":
                            isYadeJob = true;
                            break;
                        default:
                            isYadeJob = false;
                        }
                    }
                    if (((Element)scriptNode).hasAttribute("language")) {
                        item.setScriptLanguage(((Element)scriptNode).getAttribute("language"));
                    }
                }
                item.setIsYadeJob(isYadeJob);
                /** End of new Items */
                Long id = SaveOrUpdateHelper.saveOrUpdateJob(inventoryDbLayer, item, dbJobs);
                if(item.getId() == null) {
                    item.setId(id);
                }
                if(!dbJobs.contains(item)) {
                    dbJobs.add(item);
                }
                
                String docuPath = xPathAnswerXml.selectSingleNodeValue((Element)jobSource, "description/include/@file");
                if (docuPath != null && (docuPath.startsWith("jobs/") || docuPath.startsWith("./jobs/"))) {
                    docuPath = docuPath.replaceFirst("jobs", DEFAULT_JOB_DOC_PATH);
                    createOrUpdateDocumentationUsage(inventoryDbLayer.getSession(), inventoryInstance.getSchedulerId(), docuPath, item.getId(),
                            item.getName(), "JOB");
                }
                LOGGER.debug(String.format("%s: job     id = %s, jobName = %s, jobBasename = %s, title = %s, isOrderJob = %s,"
                        + " isRuntimeDefined = %s", method, item.getId(), item.getName(), item.getBaseName(), item.getTitle(),
                        item.getIsOrderJob(), item.getIsRuntimeDefined()));
                countSuccessJobs++;
                NodeList lockUses = jobSource.getElementsByTagName("lock.use");
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
                            Long appLockId = SaveOrUpdateHelper.saveOrUpdateAppliedLock(inventoryDbLayer, appliedLock,
                                    dbAppliedLocks);
                            if(appliedLock.getId() == null) {
                                appliedLock.setId(appLockId);
                            }
                            dbAppliedLocks.add(appliedLock);
                        }
                    }
                }
                // for calendarUsage
                if (schedule != null && !schedule.isEmpty()) {
                    List<DBItemInventoryClusterCalendarUsage> dbCalendarUsages = null;
                    dbCalendarUsages = inventoryDbLayer.getAllCalendarUsagesForObject(inventoryInstance.getSchedulerId(), item.getName(), "JOB");
                    Path filePath = liveDirectory.resolve(file.getFileName().substring(1));                    
                    InventoryRuntimeHelper.createOrUpdateCalendarUsage(getXPathFromFile(filePath), dbCalendarUsages, item, "JOB", inventoryDbLayer,
                            liveDirectory, inventoryInstance.getSchedulerId(), timezone, false);
//                    InventoryRuntimeHelper.recalculateRuntime(inventoryDbLayer, item, dbCalendarUsages, liveDirectory, timezone);
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s: job file cannot be inserted = %s, exception = %s ", method, file.getFileName(),
                        ex.toString()), ex);
                errorJobs.put(file.getFileName() , ex.toString());
            }
        }
    }
    
    private void processJobChainFromNodes(Element jobChain) throws Exception {
        String method = "    processJobChain";
        DBItemInventoryFile file = processFile(jobChain, EConfigFileExtensions.JOB_CHAIN, true);
        if (file != null) {
            countTotalJobChains++;
            Element jobChainSource = (Element)jobChain.getElementsByTagName("job_chain").item(0);
            if (jobChainSource == null) {
                jobChainSource = getSourceFromFile(jobChain);
            }
            try {
                DBItemInventoryJobChain item = new DBItemInventoryJobChain();
                String name = null;
                name = file.getFileName().replace(EConfigFileExtensions.JOB_CHAIN.extension(), "");
                item.setName(name);
                String baseName = file.getFileBaseName().replace(EConfigFileExtensions.JOB_CHAIN.extension(), "");
                item.setBaseName(baseName);
                String title = jobChainSource.getAttribute("title");
                if (title != null && !title.isEmpty()) {
                    item.setTitle(title);
                }
                NodeList fileOrderSources = jobChainSource.getElementsByTagName("file_order_source");
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
                String maxOrders = jobChainSource.getAttribute("max_orders");
                if(maxOrders != null && !maxOrders.isEmpty()) {
                    item.setMaxOrders(Integer.parseInt(maxOrders));
                }
                item.setDistributed(jobChainSource.hasAttribute("distributed") && "yes,1,true".contains(jobChainSource.getAttribute("distributed")));
                if (jobChainSource.hasAttribute("process_class")) {
                    String processClass = jobChainSource.getAttribute("process_class");
                    Path path = Paths.get(item.getName());
                    String resolvedProcessClassPath = path.getParent().resolve(processClass).normalize().toString().replace('\\', '/');
                    DBItemInventoryProcessClass pc = processClassExists(resolvedProcessClassPath);
                    if(pc != null) {
                        item.setProcessClass(processClass);
                        item.setProcessClassName(pc.getName());
                        item.setProcessClassId(pc.getId());
                    } else {
                        item.setProcessClass(processClass);
                        item.setProcessClassName(resolvedProcessClassPath);
                        item.setProcessClassId(DBLayer.DEFAULT_ID);
                    }
                } else {
                    item.setProcessClass(null);
                    item.setProcessClassName(DBLayer.DEFAULT_NAME);
                    item.setProcessClassId(DBLayer.DEFAULT_ID);
                }
                if (jobChain.hasAttribute("file_watching_process_class")) {
                    String fwProcessClass = jobChain.getAttribute("file_watching_process_class");
                    Path path = Paths.get(item.getName());
                    String resolvedFwProcessClassPath = path.getParent().resolve(fwProcessClass).normalize().toString().replace('\\', '/');
                    DBItemInventoryProcessClass ipc = processClassExists(resolvedFwProcessClassPath);
                    if(ipc != null) {
                        item.setFileWatchingProcessClass(fwProcessClass);
                        item.setFileWatchingProcessClassName(ipc.getName());
                        item.setFileWatchingProcessClassId(ipc.getId());
                    } else {
                        item.setFileWatchingProcessClass(fwProcessClass);
                        item.setFileWatchingProcessClassName(resolvedFwProcessClassPath);
                        item.setFileWatchingProcessClassId(DBLayer.DEFAULT_ID);
                    }
                } else {
                    item.setFileWatchingProcessClass(null);
                    item.setFileWatchingProcessClassName(DBLayer.DEFAULT_NAME);
                    item.setFileWatchingProcessClassId(DBLayer.DEFAULT_ID);
                }
                /** End of new Items */
                Long id = SaveOrUpdateHelper.saveOrUpdateJobChain(inventoryDbLayer, item, dbJobChains);
                if (id != null) {
                    item.setId(id);
                }
                if(!dbJobChains.contains(item)) {
                    dbJobChains.add(item);
                }
                LOGGER.debug(String.format("%s: jobChain    id = %s, startCause = %s, jobChainName = %s, jobChainBasename = %s,"
                        + " title = %s", method, item.getId(), item.getStartCause(), item.getName(), item.getBaseName(),
                        item.getTitle()));
                
                NodeList nl = xPathAnswerXml.selectNodeList(jobChainSource, "*");
                int ordering = 1;
                inventoryDbLayer.deleteOldNodes(item);
                for (int j = 0; j < nl.getLength(); j++) {
                    Element jobChainNodeElement = (Element) nl.item(j);
                    DBItemInventoryJobChainNode nodeItem = createInventoryJobChainNode(jobChainNodeElement, item);
                    nodeItem.setInstanceId(file.getInstanceId());
                    nodeItem.setOrdering(new Long(ordering));
                    inventoryDbLayer.getSession().save(nodeItem);
                    ordering++;
                    LOGGER.debug(String.format("%s: jobChainNode     id = %s, nodeName = %s, ordering = %s, state = %s, "
                            + "nextState = %s, errorState = %s, job = %s, " + "jobName = %s", method, nodeItem.getId(),
                            nodeItem.getName(), nodeItem.getOrdering(), nodeItem.getState(), nodeItem.getNextState(),
                            nodeItem.getErrorState(), nodeItem.getJob(), nodeItem.getJobName()));
                }
                countSuccessJobChains++;
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s: job chain file cannot be inserted = %s , exception = %s", method,
                        file.getFileName(), ex.toString()), ex);
                errorJobChains.put(file.getFileName(), ex.toString());
            }
        }
    }
    
    private DBItemInventoryJobChainNode createInventoryJobChainNode(Element jobChainNodeElement, DBItemInventoryJobChain jobChain)
            throws Exception {
        String nodeName = jobChainNodeElement.getNodeName();
        String job = jobChainNodeElement.getAttribute("job");
        String state = jobChainNodeElement.getAttribute("state");
        String nextState = jobChainNodeElement.getAttribute("next_state");
        String errorState = jobChainNodeElement.getAttribute("error_state");
        DBItemInventoryJobChainNode nodeItem = new DBItemInventoryJobChainNode();
        String jobName = Paths.get(jobChain.getName()).getParent().resolve(job).normalize().toString().replace('\\', '/');
        if (job != null && !job.isEmpty()) {
            DBItemInventoryJob jobItem = jobExists(jobName);
            if(jobItem != null) {
                nodeItem.setJob(job);
                nodeItem.setJobName(jobItem.getName());
                nodeItem.setJobId(jobItem.getId());
            } else {
                nodeItem.setJob(job);
                nodeItem.setJobName(jobName);
                nodeItem.setJobId(DBLayer.DEFAULT_ID);
            }
        } else {
            nodeItem.setJob(null);
            nodeItem.setJobName(DBLayer.DEFAULT_NAME);
            nodeItem.setJobId(DBLayer.DEFAULT_ID);
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
//        nodeItem.setJob(job);
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
                String resolvedJobchainPath = Paths.get(jobChain.getName()).getParent().resolve(jobchain).normalize().toString().replace("\\", "/");
                DBItemInventoryJobChain ijc = inventoryDbLayer.getJobChain(jobChain.getInstanceId(), resolvedJobchainPath);
                if(ijc != null) {
                    nodeItem.setNestedJobChain(jobchain);
                    nodeItem.setNestedJobChainName(ijc.getName());
                    nodeItem.setNestedJobChainId(ijc.getId());
                } else {
                    nodeItem.setNestedJobChain(jobchain);
                    nodeItem.setNestedJobChainName(resolvedJobchainPath);
                    nodeItem.setNestedJobChainId(DBLayer.DEFAULT_ID);
                }
            } else {
                nodeItem.setNestedJobChain(null);
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
        // JITL-615 special for distributed orders
        if (order.getAttribute("in_database_only").equals("yes") && order.getAttribute("order_source_type").equals("Permanent") && !order
                .getAttribute("job_chain").isEmpty()) {
            order.setAttribute("path", order.getAttribute("job_chain") + "," + order.getAttribute("order"));
        }
        DBItemInventoryFile file = processFile(order, EConfigFileExtensions.ORDER, true);
        if (file != null) {
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
                String directory = (file.getFileDirectory().equals(DBLayer.DEFAULT_NAME)) ? "" : 
                    (file.getFileDirectory() + "/").replaceAll("//+", "/");
                String jobChainName = directory + jobChainBaseName;
                String orderId = baseName.substring(baseName.lastIndexOf(",") + 1);
                Node runTimeNode = xPathAnswerXml.selectSingleNode(order, "run_time");
                boolean isRuntimeDefined = false;
                if(runTimeNode != null) {
                    if(((Element)runTimeNode).hasAttribute("schedule")) {
                        isRuntimeDefined = true;
                    } else if(runTimeNode.hasChildNodes()) {
                        NodeList childNodes = runTimeNode.getChildNodes();
                        for (int i = 0; i < childNodes.getLength(); i++) {
                            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE){
                                isRuntimeDefined = true;
                                break;
                            }
                        }
                    }
                    item.setIsRuntimeDefined(isRuntimeDefined);
                } else {
                    item.setIsRuntimeDefined(false);
                }
                NodeList runtimes = order.getElementsByTagName("run_time");
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
                    Path path = Paths.get(item.getName()).getParent();
                    String resolvedSchedulePath = path.resolve(schedule).normalize().toString().replace("\\", "/");
                    DBItemInventorySchedule is = scheduleExists(resolvedSchedulePath);
                    if (is != null) {
                        item.setSchedule(schedule);
                        item.setScheduleName(is.getName());
                        item.setScheduleId(is.getId());
                    } else {
                        item.setSchedule(schedule);
                        item.setScheduleName(resolvedSchedulePath);
                        item.setScheduleId(DBLayer.DEFAULT_ID);
                    }
                } else {
                    item.setSchedule(null);
                    item.setScheduleName(DBLayer.DEFAULT_NAME);
                    item.setScheduleId(DBLayer.DEFAULT_ID);
                    // TODO update CalendarUsages ???
                }
                /** End of new Items since 1.11 */
                Long id = SaveOrUpdateHelper.saveOrUpdateOrder(inventoryDbLayer, item, dbOrders);
                if (item.getId() == null) {
                    item.setId(id);
                }
                if(!dbOrders.contains(item)) {
                    dbOrders.add(item);
                }
                LOGGER.debug(String.format("%s: order     id = %s, jobChainName = %s, orderId = %s, title = %s, "
                        + "isRuntimeDefined = %s", method, item.getId(), item.getJobChainName(), item.getOrderId(),
                        item.getTitle(), item.getIsRuntimeDefined()));
                countSuccessOrders++;
//                if (schedule != null && !schedule.isEmpty()) {
                    List<DBItemInventoryClusterCalendarUsage> dbCalendarUsages = null;
                    dbCalendarUsages = inventoryDbLayer.getAllCalendarUsagesForObject(inventoryInstance.getSchedulerId(), item.getName(), "ORDER");
                    Path filePath = liveDirectory.resolve(file.getFileName().substring(1));                    
                    InventoryRuntimeHelper.createOrUpdateCalendarUsage(getXPathFromFile(filePath), dbCalendarUsages, item, "ORDER", inventoryDbLayer,
                            liveDirectory, inventoryInstance.getSchedulerId(), timezone, false);
//                    InventoryRuntimeHelper.recalculateRuntime(inventoryDbLayer, item, dbCalendarUsages, liveDirectory, timezone);
//                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s: order file cannot be inserted = %s, exception = ", method, file.getFileName(),
                        ex.toString()), ex);
                errorOrders.put(file.getFileName(), ex.toString());
            }
        }
    }
    
    private void processProcessClassFromNodes(Element processClass) throws Exception {
        if (!processClass.getAttribute("path").isEmpty()) {
            DBItemInventoryFile file = processFile(processClass, EConfigFileExtensions.PROCESS_CLASS, false);
            if (file != null) {
                try {
                    countTotalProcessClasses++;
                    Element processClassSource = (Element)processClass.getElementsByTagName("process_class").item(0);
                    if (processClassSource == null) {
                        processClassSource = getSourceFromFile(processClass);
                    }
                    DBItemInventoryProcessClass item = new DBItemInventoryProcessClass();
                    String name = null;
                    name = file.getFileName().replace(EConfigFileExtensions.PROCESS_CLASS.extension(), "");
                    item.setName(name);
                    String baseName = file.getFileBaseName().replace(EConfigFileExtensions.PROCESS_CLASS.extension(), "");
                    item.setBasename(baseName);
                    item.setInstanceId(file.getInstanceId());
                    String maxProcesses = processClassSource.getAttribute("max_processes");
                    if(maxProcesses != null && !maxProcesses.isEmpty()) {
                        item.setMaxProcesses(Integer.parseInt(maxProcesses));
                    }
                    String remoteScheduler = processClassSource.getAttribute("remote_scheduler");
                    NodeList remoteSchedulers = processClassSource.getElementsByTagName("remote_scheduler");
                    if(remoteScheduler != null && !remoteScheduler.isEmpty()) {
                        item.setHasAgents(true);
                    } else {
                        item.setHasAgents(remoteSchedulers != null && remoteSchedulers.getLength() > 0);
                    }
                    if (!item.getHasAgents()) {
                        Long fileId = SaveOrUpdateHelper.saveOrUpdateFile(inventoryDbLayer, file, dbFiles);
                        if(file.getId() == null) {
                            file.setId(fileId);
                        }
                        item.setFileId(file.getId());
                        Long id = SaveOrUpdateHelper.saveOrUpdateProcessClass(inventoryDbLayer, item, dbProcessClasses);
                        if(item.getId() == null) {
                            item.setId(id);
                        }
                        LOGGER.debug(String.format("process: processClass     id = %s, processClassName = %s", item.getId(),
                                item.getBasename()));
                        countSuccessProcessClasses++;
                    } else {
                        file.setFileType("agent_cluster");
                        Long fileId = SaveOrUpdateHelper.saveOrUpdateFile(inventoryDbLayer, file, dbFiles);
                        if(file.getId() == null) {
                            file.setId(fileId);
                        }
                        item.setFileId(file.getId());
                        Long id = SaveOrUpdateHelper.saveOrUpdateProcessClass(inventoryDbLayer, item, dbProcessClasses);
                        if(item.getId() == null) {
                            item.setId(id);
                        }
                        if(!dbProcessClasses.contains(item)) {
                            dbProcessClasses.add(item);
                        }
                        LOGGER.debug(String.format("process: processClass     id = %s, processClassName = %s", item.getId(),
                                item.getBasename()));
                        countSuccessProcessClasses++;
                        Map<String,Integer> remoteSchedulerUrls = getRemoteSchedulersFromProcessClass(remoteSchedulers);
                        if(remoteSchedulerUrls != null && !remoteSchedulerUrls.isEmpty()) {
                            NodeList remoteSchedulersParent = processClassSource.getElementsByTagName("remote_schedulers");
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
                    LOGGER.warn(String.format("    processProcessClass: processClass file cannot be inserted = %s, "
                            + "exception = %s ", file.getFileName(), ex.toString()), ex);
                    errorProcessClasses.put(file.getFileName(), ex.toString());
                }
            }
        }
    }
    
    private void processLockFromNodes(Element lock) throws Exception {
        DBItemInventoryFile file = processFile(lock, EConfigFileExtensions.LOCK, true);
        if (file != null) {
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
                Long id = SaveOrUpdateHelper.saveOrUpdateLock(inventoryDbLayer, item, dbLocks);
                if(item.getId() == null) {
                    item.setId(id);
                }
                if(!dbLocks.contains(item)) {
                    dbLocks.add(item);
                }
                countSuccessLocks++;
            } catch (Exception ex) {
                LOGGER.warn(String.format("    processLock: lock file cannot be inserted = %s, exception = %s ",
                        file.getFileName(), ex.toString()), ex);
                errorLocks.put(file.getFileName(), ex.toString());
            }
        }
    }
    
    private void processMonitorFromNodes(Element monitor) throws Exception {
        DBItemInventoryFile file = processFile(monitor, EConfigFileExtensions.MONITOR, true);
        if (file != null) {
            countTotalMonitors++;
        }
    }
    
    private void processScheduleFromNodes(Element schedule) throws Exception {
        DBItemInventoryFile file = processFile(schedule, EConfigFileExtensions.SCHEDULE, true);
        if (file != null) {
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
                Path path = Paths.get(item.getName()).getParent();
                DBItemInventorySchedule substituteItem =
                        scheduleExists(path.resolve(item.getSubstitute()).normalize().toString().replace("\\", "/"));
                boolean pathNormalizationFailure = false;
                if(substituteItem != null) {
                    item.setSubstituteId(substituteItem.getId());
                    try {
                        item.setSubstituteName(path.resolve(substituteItem.getName()).normalize().toString().replace("\\", "/"));
                    } catch (Exception e) {
                        pathNormalizationFailure = true;
                    }
                } else {
                    item.setSubstituteId(DBLayer.DEFAULT_ID);
                    item.setSubstituteName(DBLayer.DEFAULT_NAME);
                }
                if (!pathNormalizationFailure) {
                    Long id = SaveOrUpdateHelper.saveOrUpdateSchedule(inventoryDbLayer, item, dbSchedules);
                    if (item.getId() == null) {
                        item.setId(id);
                    }
                }
                if(!dbSchedules.contains(item)) {
                    dbSchedules.add(item);
                }
                countSuccessSchedules++;
                List<DBItemInventoryClusterCalendarUsage> dbCalendarUsages = null;
                dbCalendarUsages = inventoryDbLayer.getAllCalendarUsagesForObject(inventoryInstance.getSchedulerId(), item.getName(), "SCHEDULE");
                Path filePath = liveDirectory.resolve(file.getFileName().substring(1));                    
                InventoryRuntimeHelper.createOrUpdateCalendarUsage(getXPathFromFile(filePath), dbCalendarUsages, item, "SCHEDULE", inventoryDbLayer,
                        liveDirectory, inventoryInstance.getSchedulerId(), timezone, false);
//                    InventoryRuntimeHelper.recalculateRuntime(inventoryDbLayer, item, dbCalendarUsages, liveDirectory, timezone);
            } catch (Exception ex) {
                LOGGER.warn(String.format("processSchedule: schedule file cannot be inserted = %s, exception = %s ",
                        file.getFileName(), ex.toString()), ex);
                errorSchedules.put(file.getFileName(), ex.toString());
            }
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
            if(scheduleName.equalsIgnoreCase(schedule.getName())) {
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
            if(jobName.equalsIgnoreCase(job.getName())) {
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
                remoteSchedulerUrls.put(url.toLowerCase(), ordering);
                ordering++;
            }
        }
        return remoteSchedulerUrls;
    }
    
    private void createOrUpdateDocumentationUsage (SOSHibernateSession connection, String schedulerId, String docuPath, Long jobId, String jobPath,
            String objectType) throws Exception {
        if (connection == null) {
            connection = factory.openStatelessSession();
        }
        DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);
        DBItemDocumentationUsage dbDocuUsage = dbLayer.getDocumentationUsageForAssignment(schedulerId, jobPath, objectType);
        DBItemDocumentation dbReferencedDocu = dbLayer.getDocumentation(schedulerId, docuPath);
        if (dbDocuUsage == null && dbReferencedDocu != null) {
            DBItemDocumentationUsage newDocuUsage = new DBItemDocumentationUsage();
            newDocuUsage.setDocumentationId(dbReferencedDocu.getId());
            newDocuUsage.setSchedulerId(schedulerId);
            newDocuUsage.setPath(jobPath);
            newDocuUsage.setObjectType(objectType);
            newDocuUsage.setCreated(Date.from(Instant.now()));
            newDocuUsage.setModified(newDocuUsage.getCreated());
            connection.save(newDocuUsage);
        }
    }
    
    private Element getSourceFromFile(Element element) throws Exception {
        String file = xPathAnswerXml.selectSingleNodeValue(element, "file_based/@file");
        try {
            return new SOSXMLXPath(file).getRoot();
        } catch (Exception ex) {
            throw new Exception(String.format("[%s]%s", file, ex.toString()), ex);
        }
    }
    
    private void updateDailyPlan (SOSHibernateSession session) throws Exception {
        String pluginContext = MDC.get("plugin");
        MDC.put("plugin", "dailyplan");
        Calendar2DB calendar2Db = Calendar2DBHelper.initCalendar2Db(inventoryDbLayer, inventoryInstance, httpHost, httpPort);
        calendar2Db.store();
        if (pluginContext != null) {
            MDC.put("plugin", pluginContext);
        } else {
            MDC.remove("plugin");
        }
    }
    
    public void setLiveDirectory(Path liveDirectory) {
        this.liveDirectory = liveDirectory;
    }

    private SOSXMLXPath getXPathFromFile (Path filePath) throws Exception {
        if (Files.exists(filePath)) {
            return new SOSXMLXPath(filePath);
        }
        return null;
    }
    
    private void processUncommittedSubmissions() throws Exception {
        LOGGER.info("***** processUncommittedSubmissions *****");
        inventoryDbLayer.getSession().beginTransaction();
        List<DBItemSubmission> submissions = inventoryDbLayer.getUncommittedSubmissions(inventoryInstance.getId());
        inventoryDbLayer.getSession().commit();
        for (DBItemSubmission submission : submissions) {
            inventoryDbLayer.getSession().beginTransaction();
            LOGGER.debug(String.format("***** processing submission with ID=%1$s *****", submission.getSubmissionId()));
            List<DBItemSubmittedObject> submittedObjects = inventoryDbLayer.getUncommittedSubmittedObjects(submission.getSubmissionId());
            inventoryDbLayer.getSession().commit();
            if (submittedObjects != null) {
                for (DBItemSubmittedObject submittedObject : submittedObjects) {
                    if (submittedObject.getPath() == null) {
                        continue;
                    }
                    boolean isFolder = false;
                    if (submittedObject.getPath().endsWith("/")) {
                        isFolder = true;
                    }
                    Path path = liveDirectory.resolve(submittedObject.getPath().substring(1));
                    LOGGER.info(String.format("***** processing submission for Object = %1$s *****", path.toString()));
                    if (Files.exists(path)) {
                        if (submittedObject.getToDelete()) {
                            if (isFolder) {
                                LOGGER.info(String.format("***** delete folder: %1$s *****", path.toString()));
                                for (Path p : Files.walk(path).sorted(Comparator.reverseOrder()).collect(Collectors.toSet())) {
                                    Files.delete(p);
                                }
                            } else {
                                LOGGER.info(String.format("***** delete file for Object = %1$s *****", path.toString()));
                                Files.delete(path);
                            }
                        } else {
                            if (submittedObject.getContent() == null || submittedObject.getContent().isEmpty() || isFolder) {
                                //
                            } else {
                                FileTime fileTime = Files.getLastModifiedTime(path);
                                if (!(fileTime.compareTo(FileTime.fromMillis(submittedObject.getModified().getTime())) > 0)) {
                                    LOGGER.debug(String.format("***** file date is older, overwriting file: %1$s *****", path.toString()));
                                    writeFile(submittedObject.getContent(), path);
                                } else {
                                    LOGGER.debug(String.format("***** file date is younger, not overwriting file: %1$s *****", path.toString()));
                                }
                            }
                        }
                    } else {
                        if (submittedObject.getToDelete()) {
                            //
                        } else {
                            if (isFolder) {
                                LOGGER.debug(String.format("***** create folder: %1$s *****", path.toString()));
                                Files.createDirectories(path);
                            } else {
                                LOGGER.debug(String.format("***** create file: %1$s *****", path.toString()));
                                writeFile(submittedObject.getContent(), path);
                            }
                        }
                    }
                    inventoryDbLayer.getSession().beginTransaction();
                    LOGGER.debug("***** delete processed submission *****");
                    inventoryDbLayer.getSession().delete(submission);
                    inventoryDbLayer.getSession().commit();
                    Long count = inventoryDbLayer.getUncommitedInstanceCount(submission.getSubmissionId());
                    if (count == 0) {
                        LOGGER.debug("***** LAST ENTRY! delete submitted object, too *****");
                        inventoryDbLayer.getSession().beginTransaction();
                        inventoryDbLayer.getSession().delete(submittedObject);
                        inventoryDbLayer.getSession().commit();
                    } else {
                        LOGGER.debug(String.format("***** %1$d more submission found for this submitted object, not deleting submitted object. *****",
                                count));
                    }
                }
            }
        }
    }
    
    private void writeFile(String xml, Path path) {
        if (xml != null && !xml.isEmpty()) {
            try {
                Files.write(path, xml.getBytes(StandardCharsets.ISO_8859_1));
            } catch (IOException e) {
                LOGGER.error(String.format("Error: %1$s - occurred during update of file %2$s, file not updated!", e.getMessage(), path));
            }
        }
    }
    
}