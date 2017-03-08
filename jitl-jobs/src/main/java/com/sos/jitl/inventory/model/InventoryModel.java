package com.sos.jitl.inventory.model;

import java.io.IOException;
import java.io.StringReader;
import java.net.SocketException;
import java.net.URISyntaxException;
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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.xml.SOSXMLXPath;

import com.sos.exception.BadRequestException;
import com.sos.exception.SOSException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.UtcTimeHelper;
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
import com.sos.jitl.restclient.JobSchedulerRestApiClient;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class InventoryModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryModel.class);
    private static final String DEFAULT_PROCESS_CLASS_NAME = "(default)";
    private static final String COMMAND = "<show_state what=\"cluster source job_chains job_chain_orders schedules\" />";
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
    private Path schedulerXmlPath;
    private Path schedulerLivePath;
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
    private SOSHibernateSession connection;


    public InventoryModel(SOSHibernateFactory factory, DBItemInventoryInstance jsInstanceItem, Path schedulerXmlPath) throws Exception {
        this.schedulerXmlPath = schedulerXmlPath;
        this.schedulerLivePath = Paths.get(jsInstanceItem.getLiveDirectory());
        this.connection = new SOSHibernateSession(factory);
        this.inventoryDbLayer = new DBLayerInventory(connection);
        this.inventoryInstance = jsInstanceItem;
    }

    public void process() throws Exception {
        String method = "process";
        try {
            connection.connect();
            String toTimeZoneString = "UTC";
            String fromTimeZoneString = DateTimeZone.getDefault().getID();
            started = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime());
            initCounters();
            initExistingItems();
            connection.beginTransaction();
            processSchedulerXml();
            connection.commit();
            if (answerXml == null && xmlCommandExecutor != null) {
                answerXml = xmlCommandExecutor.executeXml(COMMAND);
            }
//            LOGGER.info(answerXml);
            xPathAnswerXml = new SOSXMLXPath(new StringBuffer(answerXml));
            if(waitUntilSchedulerIsRunning()) {
                processStateAnswerXML();
                connection.reconnect();
                connection.beginTransaction();
                inventoryDbLayer.refreshUsedInJobChains(inventoryInstance.getId(), dbJobs);
                connection.commit();
                connection.reconnect();
                connection.beginTransaction();
                cleanUpInventoryAfter(started);
                connection.commit();
                logSummary();
                resume();
            }
        } catch (Exception ex) {
            try {
                inventoryDbLayer.getSession().rollback();
            } catch (Exception e) {
                // no exception handling
            }
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        } finally {
            connection.disconnect();
        }
    }

    public void setXmlCommandExecutor(SchedulerXmlCommandExecutor xmlCommandExecutor) {
        this.xmlCommandExecutor = xmlCommandExecutor;
    }
    
//    private boolean waitUntilSchedulerIsRunning_() throws Exception {
//        return waitUntilSchedulerIsRunning(false);
//    }
    
//    private boolean waitUntilSchedulerIsRunning(boolean logging) throws Exception {
//        String state = xPathAnswerXml.selectSingleNodeValue("/spooler/answer/state/@state");
//        LOGGER.info("*** JobScheduler State: "+state+" ***");
//        if ("waiting_for_activation".equals(state)) {
//            if (!logging) {
//                LOGGER.info("*** event based inventory update is paused until activation ***");
//            }
//            if (xmlCommandExecutor == null) {
//                throw new SOSException("xmlCommandExecutor is undefined");
//            }
//            Thread.sleep(30000);
//            answerXml = xmlCommandExecutor.executeXml(COMMAND);
//            xPathAnswerXml = new SOSXMLXPath(new StringBuffer(answerXml));
//            return waitUntilSchedulerIsRunning(true);
//        } else if ("running,paused".contains(state)) {
//            if (logging) {
//                LOGGER.info("*** event based inventory update is resumed caused of activation ***");
//            }
//            return true;
//        }
//        return false;
//    }

    private boolean waitUntilSchedulerIsRunning() throws Exception {
        String state = xPathAnswerXml.selectSingleNodeValue("/spooler/answer/state/@state");
        LOGGER.debug("*** JobScheduler State: "+state+" ***");
        if ("waiting_for_activation".equals(state)) {
            LOGGER.info("*** inventory configuration update is paused until activation ***");
            if (xmlCommandExecutor == null) {
                throw new SOSException("xmlCommandExecutor is undefined");
            }
            String startedAt = xPathAnswerXml.selectSingleNodeValue("/spooler/answer/state/@spooler_running_since");
            Long eventId = (startedAt != null) ? Instant.parse(startedAt).getEpochSecond()*1000 : Instant.now().getEpochSecond()*1000;
            String httpPort = xPathAnswerXml.selectSingleNodeValue("/spooler/answer/state/@http_port");
            if (httpPort != null) {
                Integer timeout = 90;
                
                URIBuilder uriBuilder = new URIBuilder();
                uriBuilder.setScheme("http");
                uriBuilder.setHost("localhost");
                uriBuilder.setPort(Integer.parseInt(httpPort));
                uriBuilder.setPath("/jobscheduler/master/api/event");
                uriBuilder.setParameter("return", "SchedulerEvent");
                uriBuilder.setParameter("timeout", timeout.toString());
                uriBuilder.setParameter("after", eventId.toString());
                
                URIBuilder uriBuilderforState = new URIBuilder();
                uriBuilderforState.setScheme("http");
                uriBuilderforState.setHost("localhost");
                uriBuilderforState.setPort(Integer.parseInt(httpPort));
                uriBuilderforState.setPath("/jobscheduler/master/api");
                
                JobSchedulerRestApiClient apiClient = new JobSchedulerRestApiClient();
                apiClient.setSocketTimeout((timeout + 5)*1000);
                apiClient.addHeader("Accept", "application/json");
                apiClient.createHttpClient();
                
                try {
                    return waitUntilSchedulerIsRunning(apiClient, uriBuilder, uriBuilderforState);
                } catch (SOSException e) {
                    throw e;
                } finally {
                    apiClient.closeHttpClient();
                }
            } else {
                throw new BadRequestException("Cannot determine http port");
            }
        }
        return true;
    }
    
    private boolean waitUntilSchedulerIsRunning(JobSchedulerRestApiClient apiClient, URIBuilder uriBuilder, URIBuilder uriBuilderforState) throws Exception {
        String response = apiClient.getRestService(uriBuilder.build());
        LOGGER.debug("*** URI: "+uriBuilder.build().toString()+" ***");
        LOGGER.debug("*** RESPONSE: "+response+" ***");
        int httpReplyCode = apiClient.statusCode();
        switch (httpReplyCode) {
        case 200:
            JsonReader rdr = Json.createReader(new StringReader(response));
            JsonObject json = rdr.readObject();
            Long newEventId = json.getJsonNumber("eventId").longValue();
            String type = json.getString("TYPE", "Empty");
            LOGGER.debug("*** TYPE: "+type+" ***");
            boolean schedulerIsRunning = false;
            boolean schedulerIsClosed = false;
            switch (type) {
            case "Torn":
                newEventId = checkStateIfEventQueueIsTorn(apiClient, uriBuilderforState);
                if (newEventId == null) {
                    schedulerIsRunning = true; 
                }
            case "Empty":
                break;
            case "NonEmpty":
                for (JsonObject event : json.getJsonArray("eventSnapshots").getValuesAs(JsonObject.class)) {
                    if ("SchedulerStateChanged".equals(event.getString("TYPE", "")) && "running,paused".contains(event.getString("state", ""))) {
                        schedulerIsRunning = true;
                        break;
                    } 
                    if ("SchedulerClosed".equals(event.getString("TYPE", ""))) {
                        schedulerIsClosed = true; 
                    }
                }
                break;
            }
            if (schedulerIsClosed) {
                return false;
            } else if (!schedulerIsRunning) {
                uriBuilder.setParameter("after", newEventId.toString());
                return waitUntilSchedulerIsRunning(apiClient, uriBuilder, uriBuilderforState);
            } else {
                answerXml = xmlCommandExecutor.executeXml(COMMAND);
                xPathAnswerXml = new SOSXMLXPath(new StringBuffer(answerXml));
                LOGGER.info("*** inventory configuration update is resumed caused of activation ***");
                return true;
            }
        case 400:
            throw new BadRequestException(httpReplyCode + " " + response);
        default:
            throw new BadRequestException(httpReplyCode + " " + apiClient.getHttpResponse().getStatusLine().getReasonPhrase());
        }
        
    }
    
    private Long checkStateIfEventQueueIsTorn(JobSchedulerRestApiClient apiClient, URIBuilder uriBuilderforState) throws SocketException, SOSException, URISyntaxException {
        String response = apiClient.getRestService(uriBuilderforState.build());
        LOGGER.debug("*** URI: "+uriBuilderforState.build().toString()+" ***");
        LOGGER.debug("*** RESPONSE: "+response+" ***");
        int httpReplyCode = apiClient.statusCode();
        switch (httpReplyCode) {
        case 200:
            JsonReader rdr = Json.createReader(new StringReader(response));
            JsonObject json = rdr.readObject();
            Long eventId = json.getJsonNumber("eventId").longValue();
            String state = json.getString("state", null);
            if ("running,paused".contains(state)) {
                return null;
            } else {
                return eventId;
            }
        case 400:
            throw new BadRequestException(httpReplyCode + " " + response);
        default:
            throw new BadRequestException(httpReplyCode + " " + apiClient.getHttpResponse().getStatusLine().getReasonPhrase());
        }
    }

    private void initExistingItems() throws Exception {
        inventoryDbLayer.getSession().beginTransaction();
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
        inventoryDbLayer.getSession().commit();
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
            attrs = Files.readAttributes(schedulerXmlPath, BasicFileAttributes.class);
            fileCreated = ReportUtil.convertFileTime2UTC(attrs.creationTime());
            fileModified = ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime());
            fileLocalCreated = ReportUtil.convertFileTime2Local(attrs.creationTime());
            fileLocalModified = ReportUtil.convertFileTime2Local(attrs.lastModifiedTime());
        } catch (IOException exception) {
            LOGGER.debug(String.format("%s: cannot read file attributes. file = %s, exception = %s  ", method, schedulerXmlPath.toString(),
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
                inventoryDbLayer.getSession().rollback();
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
    
    private void cleanUpInventoryAfter(Date started) throws Exception {
        filesDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_FILES, inventoryInstance.getId());
        jobsDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_JOBS, inventoryInstance.getId());
        jobChainsDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_JOB_CHAINS, inventoryInstance.getId());
        jobChainNodesDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_JOB_CHAIN_NODES, inventoryInstance.getId());
        ordersDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_ORDERS, inventoryInstance.getId());
        appliedLocksDeleted = inventoryDbLayer.deleteAppliedLocksFromDb(started, inventoryInstance.getId());
        locksDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_LOCKS, inventoryInstance.getId());
        schedulesDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_SCHEDULES, inventoryInstance.getId());
        processClassesDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_PROCESS_CLASSES, inventoryInstance.getId());
        agentClustersDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_AGENT_CLUSTER, inventoryInstance.getId());
        agentClusterMembersDeleted = inventoryDbLayer.deleteItemsFromDb(started, DBLayer.DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS, inventoryInstance.getId());
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
//        String supervisorHost = null;
//        Integer supervisorPort = null;
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
        try {
            connection.beginTransaction();
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
            connection.commit();
        } catch (Exception e) {
            throw e;
        } 
    }
    
    private DBItemInventoryFile processFile(Element element, EConfigFileExtensions fileExtension, Boolean save) throws Exception {
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
        Path path = schedulerLivePath.resolve(fileName.substring(1));
        BasicFileAttributes attrs = null;
        try {
            attrs = Files.readAttributes(path, BasicFileAttributes.class);
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
                    "%s: file     id = %s, fileType = %s, fileName = %s, fileBasename = %s, fileDirectory = %s, fileCreated = %s, fileModified = %s",
                    method, item.getId(), item.getFileType(), item.getFileName(), item.getFileBaseName(), item.getFileDirectory(),
                    item.getFileCreated(), item.getFileModified()));
            return item;
        } catch (IOException exception) {
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
            Element jobSource = (Element)job.getElementsByTagName("job").item(0);
            if (jobSource == null) {
                jobSource = getSourceFromFile(file.getFileName());
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
                boolean isOrderJob = jobSource.getAttribute("order") != null && "yes".equals(jobSource.getAttribute("order").toLowerCase());
                item.setIsOrderJob(isOrderJob);
                NodeList runtimes = jobSource.getElementsByTagName("run_time");
                item.setIsRuntimeDefined((runtimes != null && runtimes.getLength() > 0));
                item.setInstanceId(file.getInstanceId());
                item.setFileId(file.getId());
                item.setCreated(ReportUtil.getCurrentDateTime());
                item.setModified(ReportUtil.getCurrentDateTime());
                /** new Items since 1.11 */
                if (jobSource.hasAttribute("process_class")) {
                    String processClass = jobSource.getAttribute("process_class");
                    Path path = Paths.get(item.getName());
                    Path result = path.getParent().resolve(processClass).normalize();
                    processClass = result.toString().replace('\\', '/');
                    DBItemInventoryProcessClass pc = processClassExists(processClass);
                    if(pc != null) {
                        item.setProcessClass(pc.getBasename());
                        item.setProcessClassName(pc.getName());
                        item.setProcessClassId(pc.getId());
                    } else {
                        item.setProcessClass(processClass);
                        item.setProcessClassName(processClass.substring(processClass.lastIndexOf("/") + 1));
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
                String maxTasks = jobSource.getAttribute("tasks");
                if(maxTasks != null && !maxTasks.isEmpty()) {
                    item.setMaxTasks(Integer.parseInt(maxTasks));
                } else {
                    item.setMaxTasks(1);
                }
                NodeList description = jobSource.getElementsByTagName("description");
                item.setHasDescription((description != null && description.getLength() > 0));
                /** End of new Items */
                Long id = SaveOrUpdateHelper.saveOrUpdateJob(inventoryDbLayer, item, dbJobs);
                if(item.getId() == null) {
                    item.setId(id);
                }
                LOGGER.debug(String.format("%s: job     id = %s, jobName = %s, jobBasename = %s, title = %s, isOrderJob = %s, isRuntimeDefined = %s",
                        method, item.getId(), item.getName(), item.getBaseName(), item.getTitle(), item.getIsOrderJob(), item.getIsRuntimeDefined()));
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
                    inventoryDbLayer.getSession().rollback();
                } catch (Exception e) {}
                LOGGER.warn(String.format("%s: job file cannot be inserted = %s, exception = %s ", method, file.getFileName(), ex.toString()), ex);
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
                jobChainSource = getSourceFromFile(file.getFileName());
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
                item.setDistributed("yes".equalsIgnoreCase(jobChainSource.getAttribute("distributed")));
                if (jobChainSource.hasAttribute("process_class")) {
                    String processClass = jobChainSource.getAttribute("process_class");
                    Path path = Paths.get(item.getName());
                    Path result = path.getParent().resolve(processClass).normalize();
                    processClass = result.toString().replace('\\', '/');
                    DBItemInventoryProcessClass pc = processClassExists(processClass);
                    if(pc != null) {
                        item.setProcessClass(pc.getBasename());
                        item.setProcessClassName(pc.getName());
                        item.setProcessClassId(pc.getId());
                    } else {
                        item.setProcessClass(processClass);
                        item.setProcessClassName(processClass.substring(processClass.lastIndexOf("/") + 1));
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
                        item.setFileWatchingProcessClassName(fwProcessClass.substring(fwProcessClass.lastIndexOf("/") + 1));
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
                
                NodeList nl = xPathAnswerXml.selectNodeList(jobChainSource, "*");
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
                    inventoryDbLayer.getSession().rollback();
                } catch (Exception e) {}
                LOGGER.warn(String.format("%s: job chain file cannot be inserted = %s , exception = %s", method, file.getFileName(),
                        ex.toString()), ex);
                errorJobChains.put(file.getFileName(), ex.toString());
            }
        }
    }
    
    private DBItemInventoryJobChainNode createInventoryJobChainNode(Element jobChainNodeElement, DBItemInventoryJobChain jobChain) throws Exception {
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
                nodeItem.setJobName(jobItem.getName());
                nodeItem.setJobId(jobItem.getId());
                nodeItem.setJob(job);
            } else {
                nodeItem.setJobId(DBLayer.DEFAULT_ID);
                if (jobName != null && !jobName.isEmpty()) {
                    nodeItem.setJobName(jobName);
                } else {
                    nodeItem.setJobName(DBLayer.DEFAULT_NAME);
                }
                nodeItem.setJob(job);
            }
        } else {
            nodeItem.setJob(null);
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
                    inventoryDbLayer.getSession().rollback();
                } catch (Exception e) {}
                LOGGER.warn(String.format("%s: order file cannot be inserted = %s, exception = ", method, file.getFileName(), ex.toString()), ex);
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
                        processClassSource = getSourceFromFile(file.getFileName());
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
                        LOGGER.debug(String.format("process: processClass     id = %s, processClassName = %s", item.getId(), item.getBasename()));
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
                        LOGGER.debug(String.format("process: processClass     id = %s, processClassName = %s", item.getId(), item.getBasename()));
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
                    try {
                        inventoryDbLayer.getSession().rollback();
                    } catch (Exception e) {}
                    LOGGER.warn(String.format("    processProcessClass: processClass file cannot be inserted = %s, exception = %s ", file.getFileName(),
                            ex.toString()), ex);
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
                SaveOrUpdateHelper.saveOrUpdateLock(inventoryDbLayer, item, dbLocks);
                countSuccessLocks++;
            } catch (Exception ex) {
                try {
                    inventoryDbLayer.getSession().rollback();
                } catch (Exception e) {}
                LOGGER.warn(String.format("    processLock: lock file cannot be inserted = %s, exception = %s ", file.getFileName(), ex.toString()), ex);
                errorLocks.put(file.getFileName(), ex.toString());
            }
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
                    inventoryDbLayer.getSession().rollback();
                } catch (Exception e) {}
                LOGGER.warn(String.format("processSchedule: schedule file cannot be inserted = %s, exception = %s ", file.getFileName(), ex.toString()), ex);
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
                remoteSchedulerUrls.put(url, ordering);
                ordering++;
            }
        }
        return remoteSchedulerUrls;
    }
    
    private Element getSourceFromFile(String pathString) throws Exception {
        if(pathString.startsWith("/")) {
            return new SOSXMLXPath(schedulerLivePath.resolve(pathString.substring(1))).getRoot();
        } else {
            return new SOSXMLXPath(schedulerLivePath.resolve(pathString)).getRoot();
        }
    }
    
}