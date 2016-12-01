package com.sos.jitl.inventory.data;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sos.xml.SOSXMLXPath;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.inventory.db.DBLayerInventory;
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
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.reporting.helper.ReportXmlHelper;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;


public class InventoryEventUpdateUtil {

    @SuppressWarnings("unused")
    private static final String APPLICATION_HEADER_XML_VALUE = "application/xml";
    @SuppressWarnings("unused")
    private static final String WEBSERVICE_PARAM_VALUE_FILEBASED_DETAILED = "FileBasedDetailed";
    private static final String APPLICATION_HEADER_JSON_VALUE = "application/json";
    private static final String WEBSERVICE_PARAM_VALUE_FILEBASED_OVERVIEW = "FileBasedOverview";
    private static final String WEBSERVICE_PARAM_VALUE_FILEBASED_EVENT = "FileBasedEvent";
    private static final String WEBSERVICE_PARAM_KEY_RETURN = "return";
    private static final String WEBSERVICE_PARAM_KEY_AFTER = "after";
    private static final String WEBSERVICE_PARAM_KEY_TIMEOUT = "timeout";
    private static final String WEBSERVICE_PARAM_VALUE_TIMEOUT = "60s";
    private static final Integer HTTP_CLIENT_SOCKET_TIMEOUT = 65000;
    private static final String WEBSERVICE_FILE_BASED_URL = "/jobscheduler/master/api/fileBased";
    private static final String WEBSERVICE_EVENTS_URL = "/jobscheduler/master/api/event";
    private static final String ACCEPT_HEADER_KEY = "Accept";
    private static final String CONTENT_TYPE_HEADER_KEY = "Content-Type";
    private static final String POST_BODY_JSON_KEY = "path";
    private static final String POST_BODY_JSON_VALUE = "/";
    private static final String EVENT_TYPE = "TYPE";
    private static final String EVENT_TYPE_NON_EMPTY = "NonEmpty";
    private static final String EVENT_TYPE_EMPTY = "Empty";
    private static final String EVENT_TYPE_TORN = "Torn";
    private static final String EVENT_KEY = "key";
    private static final String EVENT_ID = "eventId";
    private static final String EVENT_SNAPSHOT = "eventSnapshots";
    private static final String JS_OBJECT_TYPE_JOB = "Job";
    private static final String JS_OBJECT_TYPE_JOBCHAIN = "JobChain";
    private static final String JS_OBJECT_TYPE_ORDER = "Order";
    private static final String JS_OBJECT_TYPE_PROCESS_CLASS = "ProcessClass";
    private static final String JS_OBJECT_TYPE_SCHEDULE = "Schedule";
    private static final String JS_OBJECT_TYPE_LOCK = "Lock";
    private static final String JS_OBJECT_TYPE_FOLDER = "Folder";
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEventUpdateUtil.class);
    private Map<String, List<JsonObject>> groupedEvents = new HashMap<String, List<JsonObject>>();
    private String masterUrl = null;
    private SOSHibernateConnection dbConnection = null;
    private DBItemInventoryInstance instance = null;
    private DBLayerInventory dbLayer = null;
    private String liveDirectory = null;
    private Long eventId = null;
    private List<DbItem> saveOrUpdateItems = new ArrayList<DbItem>();
    private List<DBItemInventoryJobChainNode> saveOrUpdateNodeItems = new ArrayList<DBItemInventoryJobChainNode>();
    private List<DbItem> deleteItems = new ArrayList<DbItem>();
    private Map<DBItemInventoryJobChain, NodeList> jobChainNodesToSave = new HashMap<DBItemInventoryJobChain, NodeList>();
    private JobSchedulerRestApiClient restApiClient;
    private CloseableHttpClient httpClient;
    
    public InventoryEventUpdateUtil(String masterUrl, SOSHibernateConnection connection) {
        this.masterUrl = masterUrl;
        this.dbConnection = connection;
        dbLayer = new DBLayerInventory(dbConnection);
        initInstance();
        restApiClient = new JobSchedulerRestApiClient();
        restApiClient.setAutoCloseHttpClient(false);
        restApiClient.setSocketTimeout(HTTP_CLIENT_SOCKET_TIMEOUT);
        restApiClient.createHttpClient();
        httpClient = restApiClient.getHttpClient(); 
    }
    
    public void execute() {
        try {
            eventId = initOverviewRequest();
            JsonObject result = getFileBasedEvents(eventId);
            String type = result.getString(EVENT_TYPE);
            JsonArray events = result.getJsonArray(EVENT_SNAPSHOT);
            if(events != null && !events.isEmpty()) {
                processEventType(type, events, null);
            } else if(EVENT_TYPE_EMPTY.equalsIgnoreCase(type)) {
                execute(result.getJsonNumber(EVENT_ID).longValue(), null);
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Error executing events! message: %1$s", e.getMessage()), e);
            LOGGER.warn("Restarting execution of events!");
            restartExecution();
        }
    }
    
    private void execute(Long eventId, String lastKey) throws Exception {
        JsonObject result = getFileBasedEvents(eventId);
        String type = result.getString(EVENT_TYPE);
        JsonArray events = result.getJsonArray(EVENT_SNAPSHOT);
        if(events != null && !events.isEmpty()) {
            processEventType(type, events, lastKey);
        } else if(EVENT_TYPE_EMPTY.equalsIgnoreCase(type)) {
            execute(result.getJsonNumber(EVENT_ID).longValue(), lastKey);
        }
    }
    
    private void initInstance() {
        try {
            instance = dbLayer.getInventoryInstance(masterUrl);
            liveDirectory = instance.getLiveDirectory();
        } catch (Exception e) {
            LOGGER.error(String.format("error occured receiving inventory instance from db with url: %1$s; error: &2$s", masterUrl,
                    e.getMessage()), e);
        }
    }
    
    private void restartExecution() {
        eventId = null;
        groupedEvents.clear();
        saveOrUpdateItems.clear();
        saveOrUpdateNodeItems.clear();
        deleteItems.clear();
        execute();
    }
    
    private JsonObject getLastEvent(String key, List<JsonObject> events) {
        JsonObject lastEvent = null;
        for(JsonObject event : events) {
            if(eventId == null) {
                eventId = event.getJsonNumber(EVENT_ID).longValue();
                lastEvent = event;
            } else {
                eventId = event.getJsonNumber(EVENT_ID).longValue();
                lastEvent = event;
            }
        }
        return lastEvent;
    }
    
    private void addToExistingGroup(String path, List<JsonObject> events) {
        List<JsonObject> existingGroup = groupedEvents.get(path);
        existingGroup.addAll(events);
        groupedEvents.put(path, existingGroup);
    }
    
    private void groupEvents(JsonArray events, String lastKey) {
        for (int i= 0; i < events.size(); i++) {
            List<JsonObject> pathEvents = new ArrayList<JsonObject>();
            String key = ((JsonObject)events.getJsonObject(i)).getString(EVENT_KEY);
            if(lastKey == null) {
                lastKey = key;
                pathEvents.add((JsonObject)events.get(i));
            } else if (lastKey.equalsIgnoreCase(key)) {
                pathEvents.add((JsonObject)events.get(i));
            } else if (!lastKey.equalsIgnoreCase(key)) {
                pathEvents.clear();
                lastKey = key;
                pathEvents.add((JsonObject)events.get(i));
            }
            if(groupedEvents.containsKey(lastKey)) {
                addToExistingGroup(lastKey, pathEvents);
            } else {
                groupedEvents.put(lastKey, pathEvents);
            }
        }
    }
    
    private void processGroupedEvents(Map<String, List<JsonObject>> groupedEvents) throws Exception {
        String lastKey = null;
        for (String key : groupedEvents.keySet()) {
            lastKey = key;
            JsonObject event = getLastEvent(key, groupedEvents.get(key));
            eventId = processEvent(event);
        }
        processDbTransaction();
        saveOrUpdateItems.clear();
        saveOrUpdateNodeItems.clear();
        deleteItems.clear();
        groupedEvents.clear();
        execute(eventId, lastKey);
    }

    private String getName(DbItem item) {
        if (item instanceof DBItemInventoryJob) {
            return ((DBItemInventoryJob) item).getName();
        } else if (item instanceof DBItemInventoryJobChain) {
            return ((DBItemInventoryJobChain) item).getName();
        } else if (item instanceof DBItemInventoryOrder) {
            return ((DBItemInventoryOrder) item).getName();
        } else if (item instanceof DBItemInventoryProcessClass) {
            return ((DBItemInventoryProcessClass) item).getName();
        } else if (item instanceof DBItemInventorySchedule) {
            return ((DBItemInventorySchedule) item).getName();
        } else if (item instanceof DBItemInventoryLock) {
            return ((DBItemInventoryLock) item).getName();
        } else {
            return null;
        }
    }
    
    private void setFileId(DbItem item, Long fileId) {
        if (item instanceof DBItemInventoryJob) {
            ((DBItemInventoryJob) item).setFileId(fileId);
        } else if (item instanceof DBItemInventoryJobChain) {
            ((DBItemInventoryJobChain) item).setFileId(fileId);
        } else if (item instanceof DBItemInventoryOrder) {
            ((DBItemInventoryOrder) item).setFileId(fileId);
        } else if (item instanceof DBItemInventoryProcessClass) {
            ((DBItemInventoryProcessClass) item).setFileId(fileId);
        } else if (item instanceof DBItemInventorySchedule) {
            ((DBItemInventorySchedule) item).setFileId(fileId);
        } else if (item instanceof DBItemInventoryLock) {
            ((DBItemInventoryLock) item).setFileId(fileId);
        }
    }
    
    private void processDbTransaction() {
        try {
            dbConnection.beginTransaction();
            Long fileId = null;
            String filePath = null;
            for (DbItem item : saveOrUpdateItems) {
                if(item instanceof DBItemInventoryFile) {
                    dbLayer.saveOrUpdate(item);
                    fileId = ((DBItemInventoryFile) item).getId();
                    filePath = ((DBItemInventoryFile) item).getFileName();
                } else {
                    if (filePath != null && fileId != null) {
                        String name = getName(item);
                        if (name != null && !name.isEmpty() && filePath.contains(name)) {
                            setFileId(item, fileId);
                        }
                        dbLayer.saveOrUpdate(item);
                        if (item instanceof DBItemInventoryJobChain) {
                            NodeList nl = jobChainNodesToSave.get(item);
                            createOrUpdateJobChainNodes(nl, (DBItemInventoryJobChain)item);
                        }
                        fileId = null;
                        filePath = null;
                    } else {
                        dbLayer.saveOrUpdate(item);
                        if (item instanceof DBItemInventoryJobChain) {
                            NodeList nl = jobChainNodesToSave.get(item);
                            createOrUpdateJobChainNodes(nl, (DBItemInventoryJobChain)item);
                        }
                    }
                }
            }
            for (DBItemInventoryJobChainNode node : saveOrUpdateNodeItems) {
                dbLayer.saveOrUpdate(node);
            }
            for (DbItem item : deleteItems) {
                dbLayer.delete(item);
            }
            dbConnection.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    private void processEventType(String type, JsonArray events, String lastKey) throws Exception {
        switch(type) {
        case EVENT_TYPE_NON_EMPTY :
            groupEvents(events, lastKey);
            processGroupedEvents(groupedEvents);
            break;
        case EVENT_TYPE_TORN :
            restartExecution();
            break;
        }
    }
    
    private Long processEvent(JsonObject event) throws Exception {
        if (event != null) {
            String key = event.getString(EVENT_KEY);
            String[] keySplit = key.split(":");
            String objectType = keySplit[0];
            String path = keySplit[1];
            eventId = event.getJsonNumber(EVENT_ID).longValue();
            switch (objectType) {
            case JS_OBJECT_TYPE_JOB:
                processJobEvent(path, event);
                break;
            case JS_OBJECT_TYPE_JOBCHAIN:
                processJobChainEvent(path, event);
                break;
            case JS_OBJECT_TYPE_ORDER:
                processOrderEvent(path, event);
                break;
            case JS_OBJECT_TYPE_PROCESS_CLASS:
                processProcessClassEvent(path, event);
                break;
            case JS_OBJECT_TYPE_SCHEDULE:
                processScheduleEvent(path, event);
                break;
            case JS_OBJECT_TYPE_LOCK:
                processLockEvent(path, event);
                break;
            case JS_OBJECT_TYPE_FOLDER:
                break;
            }
        }
        return eventId;
    }
    
    private DBItemInventoryFile createNewInventoryFile(Long instanceId, String name, String type) {
        DBItemInventoryFile dbFile = new DBItemInventoryFile();
        Path path = Paths.get(name);
        Path absolutePath = Paths.get(liveDirectory, name);
        String fileDirectory = path.getParent().toString();
        String fileBaseName = path.getFileName().toString();
        dbFile.setFileBaseName(fileBaseName);
        dbFile.setFileDirectory(fileDirectory);
        dbFile.setFileName(name);
        dbFile.setFileType(type.toLowerCase());
        dbFile.setInstanceId(instanceId);
        if (Files.exists(absolutePath)) {
            try {
                BasicFileAttributes attrs = Files.readAttributes(absolutePath, BasicFileAttributes.class);
                dbFile.setFileCreated(ReportUtil.convertFileTime2UTC(attrs.creationTime()));
                dbFile.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                dbFile.setFileLocalCreated(ReportUtil.convertFileTime2Local(attrs.creationTime()));
                dbFile.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
            } catch (IOException e) {
                LOGGER.warn(String.format("cannot read file attributes. file = %1$s, exception = %2$s", path.toString(), e.getMessage()), e);
            } catch (Exception e) {
                LOGGER.warn("cannot convert files create and modified timestamps! " + e.getMessage(), e);
            }
        }
        return dbFile;
    }
    
    private void processJobEvent(String path, JsonObject event) throws Exception {
        Date now = Date.from(Instant.now());
        LOGGER.debug(String.format("processing event on JOB: %1$s with path: %2$s", Paths.get(path).getFileName(), Paths.get(path).getParent()));
        Path filePath = Paths.get(liveDirectory, path + EConfigFileExtensions.JOB.extension());
        Long instanceId = instance.getId();
        DBItemInventoryJob job = dbLayer.getInventoryJob(instanceId, path);
        DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId, path + EConfigFileExtensions.JOB.extension());
        // fileSystem File exists AND db job exists -> update
        // db file NOT exists AND db job NOT exists -> add
        boolean fileExists = Files.exists(filePath);
        if((fileExists && job != null) || (fileExists && file == null && job == null)) {
            if (file == null) {
                file = createNewInventoryFile(instanceId, path + EConfigFileExtensions.JOB.extension(), JS_OBJECT_TYPE_JOB);
                file.setCreated(now);
            } else {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(Paths.get(liveDirectory, path + EConfigFileExtensions.JOB.extension()), 
                            BasicFileAttributes.class);
                    file.setModified(now);
                    file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                    file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                } catch (IOException e) {
                    LOGGER.warn(String.format("cannot read file attributes. file = %1$s, exception = %2$s",
                            Paths.get(liveDirectory, path + EConfigFileExtensions.JOB.extension()).toString(), e.getMessage()), e);
                } catch (Exception e) {
                    LOGGER.warn("cannot convert files create and modified timestamps! " + e.getMessage(), e);
                }
            }
            if (job == null) {
                job = new DBItemInventoryJob();
                job.setCreated(now);
                job.setInstanceId(instanceId);
                job.setFileId(file.getId());
                job.setName(path);
                job.setBaseName(Paths.get(path).getFileName().toString());
            }
            SOSXMLXPath xPath = new SOSXMLXPath(filePath.toString());
            String title = ReportXmlHelper.getTitle(xPath);
            boolean isOrderJob = ReportXmlHelper.isOrderJob(xPath);
            boolean isRuntimeDefined = ReportXmlHelper.isRuntimeDefined(xPath);
            job.setTitle(title);
            job.setIsOrderJob(isOrderJob);
            job.setIsRuntimeDefined(isRuntimeDefined);
            if (xPath.getRoot().hasAttribute("process_class")) {
                String processClass = ReportXmlHelper.getProcessClass(xPath);
                String processClassName = dbLayer.getProcessClassName(instanceId, processClass);
                DBItemInventoryProcessClass ipc = dbLayer.getProcessClassIfExists(instanceId, processClass, processClassName);
                if(ipc != null) {
                    job.setProcessClass(ipc.getBasename());
                    job.setProcessClassName(ipc.getName());
                    job.setProcessClassId(ipc.getId());
                } else {
                    job.setProcessClass(processClass);
                    job.setProcessClassName(processClassName);
                    job.setProcessClassId(DBLayer.DEFAULT_ID);
                }
            } else {
                job.setProcessClassId(DBLayer.DEFAULT_ID);
                job.setProcessClassName(DBLayer.DEFAULT_NAME);
            }
            String schedule = ReportXmlHelper.getScheduleFromRuntime(xPath);
            if(schedule != null && !schedule.isEmpty()) {
              String scheduleName = dbLayer.getScheduleName(instanceId, schedule);
              DBItemInventorySchedule is = dbLayer.getScheduleIfExists(instanceId, schedule, scheduleName);
              if(is != null) {
                  job.setSchedule(is.getBasename());
                  job.setScheduleName(is.getName());
                  job.setScheduleId(is.getId());
              } else {
                  job.setSchedule(schedule);
                  job.setScheduleName(scheduleName);
                  job.setScheduleId(DBLayer.DEFAULT_ID);
              }
            } else {
                job.setScheduleId(DBLayer.DEFAULT_ID);
                job.setScheduleName(DBLayer.DEFAULT_NAME);
            }
            String maxTasks = xPath.getRoot().getAttribute("tasks");
            if(maxTasks != null && !maxTasks.isEmpty()) {
                job.setMaxTasks(Integer.parseInt(maxTasks));
            } else {
                job.setMaxTasks(0);
            }
            Boolean hasDescription = ReportXmlHelper.hasDescription(xPath);
            if(hasDescription != null) {
                job.setHasDescription(ReportXmlHelper.hasDescription(xPath));
            }
            job.setModified(now);
            file.setModified(now);
            saveOrUpdateItems.add(file);
            saveOrUpdateItems.add(job);
        } else if (!fileExists && job != null) {
            // fileSystem file NOT exists AND job exists -> delete
            deleteItems.add(job);
            // if file exists in db delete item too
            if(file != null) {
                deleteItems.add(file);
            }
        }
    }
    
    private void processJobChainEvent(String path, JsonObject event) throws Exception {
        Date now = Date.from(Instant.now());
        Path filePath = Paths.get(liveDirectory, path + EConfigFileExtensions.JOB_CHAIN.extension());
        Long instanceId = instance.getId();
        LOGGER.debug(String.format("processing event on JOBCHAIN: %1$s with path: %2$s", Paths.get(path).getFileName(), Paths.get(path).getParent()));
        DBItemInventoryJobChain jobChain = dbLayer.getInventoryJobChain(instanceId, path);
        DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId, path + EConfigFileExtensions.JOB_CHAIN.extension());
        // fileSystem File exists AND db schedule exists -> update
        // db file NOT exists AND db schedule NOT exists -> add
        boolean fileExists = Files.exists(filePath);
        if((fileExists && jobChain != null) || (fileExists && file == null && jobChain == null)) {
            if (file == null) {
                file = createNewInventoryFile(instanceId, path + EConfigFileExtensions.JOB_CHAIN.extension(), JS_OBJECT_TYPE_JOBCHAIN);
                file.setCreated(now);
            } else {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(Paths.get(liveDirectory, path + EConfigFileExtensions.JOB_CHAIN.extension()), 
                            BasicFileAttributes.class);
                    file.setModified(now);
                    file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                    file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                } catch (IOException e) {
                    LOGGER.warn(String.format("cannot read file attributes. file = %1$s, exception = %2$s", path.toString(), e.getMessage()), e);
                } catch (Exception e) {
                    LOGGER.warn("cannot convert files create and modified timestamps! " + e.getMessage(), e);
                }
            }
            if (jobChain == null) {
                jobChain = new DBItemInventoryJobChain();
                jobChain.setInstanceId(instanceId);
                jobChain.setName(path);
                jobChain.setBaseName(Paths.get(path).getFileName().toString());
                jobChain.setCreated(now);
            }
            SOSXMLXPath xpath = new SOSXMLXPath(filePath.toString());
            if (xpath.getRoot() == null) {
                throw new Exception(String.format("xpath root missing"));
            }
            String title = ReportXmlHelper.getTitle(xpath);
            String startCause = ReportXmlHelper.getJobChainStartCause(xpath);
            jobChain.setTitle(title);
            jobChain.setStartCause(startCause);
            String maxOrders = xpath.getRoot().getAttribute("max_orders");
            if(maxOrders != null && !maxOrders.isEmpty()) {
                jobChain.setMaxOrders(Integer.parseInt(maxOrders));
            }
            jobChain.setDistributed("yes".equalsIgnoreCase(xpath.getRoot().getAttribute("distributed")));
            if (xpath.getRoot().hasAttribute("process_class")) {
                String processClass = ReportXmlHelper.getProcessClass(xpath);
                String processClassName = dbLayer.getProcessClassName(instanceId, processClass);
                DBItemInventoryProcessClass ipc = dbLayer.getProcessClassIfExists(instanceId, processClass, processClassName);
                if(ipc != null) {
                    jobChain.setProcessClass(ipc.getBasename());
                    jobChain.setProcessClassName(ipc.getName());
                    jobChain.setProcessClassId(ipc.getId());
                } else {
                    jobChain.setProcessClass(processClass);
                    jobChain.setProcessClassName(processClassName);
                    jobChain.setProcessClassId(DBLayer.DEFAULT_ID);
                }
            } else {
                jobChain.setProcessClassId(DBLayer.DEFAULT_ID);
                jobChain.setProcessClassName(DBLayer.DEFAULT_NAME);
            }
            if (xpath.getRoot().hasAttribute("file_watching_process_class")) {
                String fwProcessClass = ReportXmlHelper.getFileWatchingProcessClass(xpath);
                String fwProcessClassName = dbLayer.getProcessClassName(instanceId, fwProcessClass);
                DBItemInventoryProcessClass ipc = dbLayer.getProcessClassIfExists(instanceId, fwProcessClass, fwProcessClassName);
                if(ipc != null) {
                    jobChain.setFileWatchingProcessClass(ipc.getBasename());
                    jobChain.setFileWatchingProcessClassName(ipc.getName());
                    jobChain.setFileWatchingProcessClassId(ipc.getId());
                } else {
                    jobChain.setFileWatchingProcessClass(fwProcessClass);
                    jobChain.setFileWatchingProcessClassName(fwProcessClassName);
                    jobChain.setFileWatchingProcessClassId(DBLayer.DEFAULT_ID);
                }
            } else {
                jobChain.setFileWatchingProcessClassId(DBLayer.DEFAULT_ID);
                jobChain.setFileWatchingProcessClassName(DBLayer.DEFAULT_NAME);
            }
            NodeList nl = ReportXmlHelper.getRootChilds(xpath);
            jobChain.setModified(now);
            file.setModified(now);
            saveOrUpdateItems.add(file);
            saveOrUpdateItems.add(jobChain);
            jobChainNodesToSave.put(jobChain, nl);
        } else if (!fileExists && jobChain != null) {
            // fileSystem file NOT exists AND db jobChain exists -> delete
            // first delete All Nodes of the jobChain then the jobChain itself
            List<DBItemInventoryJobChainNode> nodes = dbLayer.getJobChainNodes(instanceId, jobChain.getId());
            if (nodes != null && !nodes.isEmpty()) {
                deleteItems.addAll(nodes);
            }
            deleteItems.add(jobChain);
            // if file exists in db delete item too
            if(file != null) {
                deleteItems.add(file);
            }
        }
    }
    
    private void createOrUpdateJobChainNodes(NodeList nl, DBItemInventoryJobChain jobChain) throws Exception {
        Date now = Date.from(Instant.now());
        int ordering = 1;
        if (nl != null) {
            for (int j = 0; j < nl.getLength(); j++) {
                Element jobChainNodeElement = (Element) nl.item(j);
                String jobName = null;
                String nodeName = jobChainNodeElement.getNodeName();
                String job = jobChainNodeElement.getAttribute("job");
                String state = jobChainNodeElement.getAttribute("state");
                String nextState = jobChainNodeElement.getAttribute("next_state");
                String errorState = jobChainNodeElement.getAttribute("error_state");

                DBItemInventoryJobChainNode node = dbLayer.getJobChainNodeIfExists(jobChain.getInstanceId(), jobChain.getId(), state);
                if (node == null) {
                    node = new DBItemInventoryJobChainNode();
                    node.setInstanceId(jobChain.getInstanceId());
                    node.setJobChainId(jobChain.getId());
                    node.setState(state);
                    node.setCreated(now);
                }
                node.setJobName(jobName);
                node.setName(nodeName);
                node.setNextState(nextState);
                node.setErrorState(errorState);
                node.setCreated(ReportUtil.getCurrentDateTime());
                node.setModified(ReportUtil.getCurrentDateTime());
                node.setNestedJobChainId(DBLayer.DEFAULT_ID);
                node.setNestedJobChainName(DBLayer.DEFAULT_NAME);
                /** new Items since 1.11 */
                node.setJob(job);
                DBItemInventoryJob jobDbItem = dbLayer.getJobIfExists(jobChain.getInstanceId(), job, jobName);
                if (jobDbItem != null) {
                    node.setJobId(jobDbItem.getId());
                } else {
                    node.setJobId(DBLayer.DEFAULT_ID);
                }
                node.setNodeType(getJobChainNodeType(nodeName, jobChainNodeElement));
                switch (node.getNodeType()) {
                case 1:
                    if (jobChainNodeElement.hasAttribute("delay")) {
                        String delay = jobChainNodeElement.getAttribute("delay");
                        if (delay != null && !delay.isEmpty()) {
                            node.setDelay(Integer.parseInt(delay));
                        }
                    }
                    if (jobChainNodeElement.hasAttribute("on_error")) {
                        node.setOnError(jobChainNodeElement.getAttribute("on_error"));
                    }
                    break;
                case 2:
                    if (jobChainNodeElement.hasAttribute("job_chain")) {
                        String jobchain = jobChainNodeElement.getAttribute("job_chain");
                        String jobchainName = dbLayer.getJobChainName(jobChain.getInstanceId(), jobchain);
                        DBItemInventoryJobChain ijc = dbLayer.getJobChainIfExists(jobChain.getInstanceId(), jobchain, jobchainName);
                        if (ijc != null) {
                            node.setNestedJobChain(ijc.getBaseName());
                            node.setNestedJobChainName(ijc.getName());
                            node.setNestedJobChainId(ijc.getId());
                        } else {
                            node.setNestedJobChain(jobchain);
                            node.setNestedJobChainName(jobchainName);
                            node.setNestedJobChainId(DBLayer.DEFAULT_ID);
                        }
                    } else {
                        node.setNestedJobChainId(DBLayer.DEFAULT_ID);
                        node.setNestedJobChainName(DBLayer.DEFAULT_NAME);
                    }
                    break;
                case 3:
                    node.setDirectory(jobChainNodeElement.getAttribute("directory"));
                    if (jobChainNodeElement.hasAttribute("regex")) {
                        node.setRegex(jobChainNodeElement.getAttribute("regex"));
                    }
                    break;
                case 4:
                    if (jobChainNodeElement.hasAttribute("move_to")) {
                        node.setMovePath(jobChainNodeElement.getAttribute("move_to"));
                        node.setFileSinkOp(1);
                    } else {
                        node.setFileSinkOp(2);
                    }
                    break;
                default:
                    break;
                }
                node.setInstanceId(jobChain.getInstanceId());
                node.setOrdering(new Long(ordering));
                node.setModified(now);
                saveOrUpdateNodeItems.add(node);
                ordering++;
            }
        }
    }
    
    private void processOrderEvent(String path, JsonObject event) throws Exception {
        Date now = Date.from(Instant.now());
        Path filePath = Paths.get(liveDirectory, path + EConfigFileExtensions.ORDER.extension());
        Long instanceId = instance.getId();
        LOGGER.debug(String.format("processing event on ORDER: %1$s with path: %2$s", Paths.get(path).getFileName(), Paths.get(path).getParent()));
        DBItemInventoryOrder order = dbLayer.getInventoryOrder(instanceId, path);
        DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId, path + EConfigFileExtensions.ORDER.extension());
        // fileSystem File exists AND db schedule exists -> update
        // db file NOT exists AND db schedule NOT exists -> add
        boolean fileExists = Files.exists(filePath);
        if((fileExists && order != null) || (fileExists && file == null && order == null)) {
            if (file == null) {
                file = createNewInventoryFile(instanceId, path + EConfigFileExtensions.ORDER.extension(), JS_OBJECT_TYPE_ORDER);
                file.setCreated(now);
            } else {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(Paths.get(liveDirectory, path + EConfigFileExtensions.ORDER.extension()), 
                            BasicFileAttributes.class);
                    file.setModified(now);
                    file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                    file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                } catch (IOException e) {
                    LOGGER.warn(String.format("cannot read file attributes. file = %1$s, exception = %2$s", path.toString(), e.getMessage()), e);
                } catch (Exception e) {
                    LOGGER.warn("cannot convert files create and modified timestamps! " + e.getMessage(), e);
                }
            }
            String baseName = Paths.get(path).getFileName().toString();
            if (order == null) {
                order = new DBItemInventoryOrder();
                order.setInstanceId(instanceId);
                order.setName(path);
                order.setBaseName(baseName);
                order.setCreated(now);
            }
            SOSXMLXPath xpath = new SOSXMLXPath(filePath.toString());
            if (xpath.getRoot() == null) {
                throw new Exception(String.format("xpath root missing"));
            }
            String title = ReportXmlHelper.getTitle(xpath);
            String jobChainBaseName = baseName.substring(0, baseName.indexOf(","));
            String jobChainName = path.substring(0, path.indexOf(","));
            String orderId = baseName.substring(jobChainBaseName.length() + 1);
            boolean isRuntimeDefined = ReportXmlHelper.isRuntimeDefined(xpath);
            order.setFileId(file.getId());
            order.setJobChainName(jobChainName);
            order.setName(path);
            order.setBaseName(baseName);
            order.setOrderId(orderId);
            order.setTitle(title);
            order.setIsRuntimeDefined(isRuntimeDefined);
            /** new Items since 1.11 */
            Long jobChainId = dbLayer.getJobChainId(instanceId, jobChainBaseName);
            if (jobChainId != null) {
                order.setJobChainId(jobChainId);
            } else {
                order.setJobChainId(DBLayer.DEFAULT_ID);
            }
            if(xpath.getRoot().hasAttribute("state")) {
                order.setInitialState(xpath.getRoot().getAttribute("state"));
            }
            if(xpath.getRoot().hasAttribute("end_state")) {
                order.setEndState(xpath.getRoot().getAttribute("end_state"));
            }
            if(xpath.getRoot().hasAttribute("priority")) {
                String priority = xpath.getRoot().getAttribute("priority");
                if(priority != null && !priority.isEmpty()) {
                    order.setPriority(Integer.parseInt(priority));
                }
            }
            String schedule = ReportXmlHelper.getScheduleFromRuntime(xpath);
            if(schedule != null && !schedule.isEmpty()) {
              String scheduleName = dbLayer.getScheduleName(instanceId, schedule);
              DBItemInventorySchedule is = dbLayer.getScheduleIfExists(instanceId, schedule, scheduleName);
              if(is != null) {
                  order.setSchedule(is.getBasename());
                  order.setScheduleName(is.getName());
                  order.setScheduleId(is.getId());
              } else {
                  order.setSchedule(schedule);
                  order.setScheduleName(scheduleName);
                  order.setScheduleId(DBLayer.DEFAULT_ID);
              }
            } else {
                order.setScheduleId(DBLayer.DEFAULT_ID);
                order.setScheduleName(DBLayer.DEFAULT_NAME);
            }
            order.setSchedule(schedule);
            order.setModified(now);
            file.setModified(now);
            saveOrUpdateItems.add(file);
            saveOrUpdateItems.add(order);
        } else if (!fileExists && order != null) {
            // fileSystem file NOT exists AND db schedule exists -> delete
            deleteItems.add(order);
            // if file exists in db delete item too
            if(file != null) {
                deleteItems.add(file);
            }
        }
    }
    
    private void processProcessClassEvent(String path, JsonObject event) throws Exception {
        Date now = Date.from(Instant.now());
        Path filePath = Paths.get(liveDirectory, path + EConfigFileExtensions.PROCESS_CLASS.extension());
        Long instanceId = instance.getId();
        LOGGER.debug(String.format("processing event on PROCESS_CLASS: %1$s with path: %2$s", Paths.get(path).getFileName(), 
                Paths.get(path).getParent()));
        DBItemInventoryProcessClass pc = dbLayer.getInventoryProcessClass(instanceId, path);
        DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId, path + EConfigFileExtensions.PROCESS_CLASS.extension());
        // fileSystem File exists AND db schedule exists -> update
        // db file NOT exists AND db schedule NOT exists -> add
        boolean fileExists = Files.exists(filePath);
        if((fileExists && pc != null) || (fileExists && file == null && pc == null)) {
            if (file == null) {
                file = createNewInventoryFile(instanceId, path + EConfigFileExtensions.PROCESS_CLASS.extension(), JS_OBJECT_TYPE_PROCESS_CLASS);
                file.setCreated(now);
            } else {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(
                            Paths.get(liveDirectory, path + EConfigFileExtensions.PROCESS_CLASS.extension()), BasicFileAttributes.class);
                    file.setModified(now);
                    file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                    file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                } catch (IOException e) {
                    LOGGER.warn(String.format("cannot read file attributes. file = %1$s, exception = %2$s", path.toString(), e.getMessage()), e);
                } catch (Exception e) {
                    LOGGER.warn("cannot convert files create and modified timestamps! " + e.getMessage(), e);
                }
            }
            if (pc == null) {
                pc = new DBItemInventoryProcessClass();
                pc.setInstanceId(instanceId);
                pc.setName(path);
                pc.setBasename(Paths.get(path).getFileName().toString());
                pc.setCreated(now);
            }
            SOSXMLXPath xpath = new SOSXMLXPath(filePath.toString());
            if (xpath.getRoot() == null) {
                throw new Exception(String.format("xpath root missing"));
            }
            pc.setFileId(file.getId());
            pc.setMaxProcesses(ReportXmlHelper.getMaxProcesses(xpath));
            pc.setHasAgents(ReportXmlHelper.hasAgents(xpath));
            pc.setModified(now);
            file.setModified(now);
            saveOrUpdateItems.add(file);
            saveOrUpdateItems.add(pc);
        } else if (!fileExists && pc != null) {
            // fileSystem file NOT exists AND db schedule exists -> delete
            deleteItems.add(pc);
            // if file exists in db delete item too
            if(file != null) {
                deleteItems.add(file);
            }
        }
    }
    
    private void processScheduleEvent(String path, JsonObject event) throws Exception {
        Date now = Date.from(Instant.now());
        Path filePath = Paths.get(liveDirectory, path + EConfigFileExtensions.SCHEDULE.extension());
        Long instanceId = instance.getId();
        LOGGER.debug(String.format("processing event on SCHEDULE: %1$s with path: %2$s", Paths.get(path).getFileName(), Paths.get(path).getParent()));
        DBItemInventorySchedule schedule = dbLayer.getInventorySchedule(instanceId, path);
        DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId, path + EConfigFileExtensions.SCHEDULE.extension());
        // fileSystem File exists AND db schedule exists -> update
        // db file NOT exists AND db schedule NOT exists -> add
        boolean fileExists = Files.exists(filePath);
        if((fileExists && schedule != null) || (fileExists && file == null && schedule == null)) {
            if (file == null) {
                file = createNewInventoryFile(instanceId, path + EConfigFileExtensions.SCHEDULE.extension(), JS_OBJECT_TYPE_SCHEDULE);
                file.setCreated(now);
            } else {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(Paths.get(liveDirectory, path + EConfigFileExtensions.SCHEDULE.extension()), 
                            BasicFileAttributes.class);
                    file.setModified(now);
                    file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                    file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                } catch (IOException e) {
                    LOGGER.warn(String.format("cannot read file attributes. file = %1$s, exception = %2$s", path.toString(), e.getMessage()), e);
                } catch (Exception e) {
                    LOGGER.warn("cannot convert files create and modified timestamps! " + e.getMessage(), e);
                }
            }
            if (schedule == null) {
                schedule = new DBItemInventorySchedule();
                schedule.setInstanceId(instanceId);
                schedule.setName(path);
                schedule.setBasename(Paths.get(path).getFileName().toString());
                schedule.setCreated(now);
            }
            SOSXMLXPath xpath = new SOSXMLXPath(filePath.toString());
            if (xpath.getRoot() == null) {
                throw new Exception(String.format("xpath root missing"));
            }
            schedule.setFileId(file.getId());
            schedule.setTitle(ReportXmlHelper.getTitle(xpath));
            schedule.setSubstitute(ReportXmlHelper.getSubstitute(xpath));
            String timezone = instance.getTimeZone();
            schedule.setSubstituteValidFrom(ReportXmlHelper.getSubstituteValidFromTo(xpath, "valid_from", timezone));
            schedule.setSubstituteValidTo(ReportXmlHelper.getSubstituteValidFromTo(xpath, "valid_to", timezone));
            DBItemInventorySchedule substituteItem = dbLayer.getSubstituteIfExists(schedule.getSubstitute(), schedule.getInstanceId());
            if(substituteItem != null) {
                schedule.setSubstituteId(substituteItem.getId());
                schedule.setSubstituteName(substituteItem.getName());
            } else {
                schedule.setSubstituteId(DBLayer.DEFAULT_ID);
                schedule.setSubstituteName(DBLayer.DEFAULT_NAME);
            }
            schedule.setModified(now);
            file.setModified(now);
            saveOrUpdateItems.add(file);
            saveOrUpdateItems.add(schedule);
        } else if (!fileExists && schedule != null) {
            // fileSystem file NOT exists AND db schedule exists -> delete
            deleteItems.add(schedule);
            // if file exists in db delete item too
            if(file != null) {
                deleteItems.add(file);
            }
        }
    }
    
    private void processLockEvent(String path, JsonObject event) throws Exception {
        Date now = Date.from(Instant.now());
        Path filePath = Paths.get(liveDirectory, path + EConfigFileExtensions.LOCK.extension());
        Long instanceId = instance.getId();
        LOGGER.debug(String.format("processing event on LOCK: %1$s with path: %2$s", Paths.get(path).getFileName(), Paths.get(path).getParent()));
        DBItemInventoryLock lock = dbLayer.getInventoryLock(instanceId, path);
        DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId, path + EConfigFileExtensions.LOCK.extension());
        // fileSystem File exists AND db schedule exists -> update
        // db file NOT exists AND db schedule NOT exists -> add
        boolean fileExists = Files.exists(filePath);
        if((fileExists && lock != null) || (fileExists && file == null && lock == null)) {
            if (file == null) {
                file = createNewInventoryFile(instanceId, path + EConfigFileExtensions.LOCK.extension(), JS_OBJECT_TYPE_LOCK);
                file.setCreated(now);
            } else {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(Paths.get(liveDirectory, path + EConfigFileExtensions.LOCK.extension()), 
                            BasicFileAttributes.class);
                    file.setModified(now);
                    file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                    file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                } catch (IOException e) {
                    LOGGER.warn(String.format("cannot read file attributes. file = %1$s, exception = %2$s", path.toString(), e.getMessage()), e);
                } catch (Exception e) {
                    LOGGER.warn("cannot convert files create and modified timestamps! " + e.getMessage(), e);
                }
            }
            if (lock == null) {
                lock = new DBItemInventoryLock();
                lock.setInstanceId(instanceId);
                lock.setName(path);
                lock.setBasename(Paths.get(path).getFileName().toString());
                lock.setCreated(now);
            }
            SOSXMLXPath xpath = new SOSXMLXPath(filePath.toString());
            if (xpath.getRoot() == null) {
                throw new Exception(String.format("xpath root missing"));
            }
            lock.setFileId(file.getId());
            lock.setMaxNonExclusive(ReportXmlHelper.getMaxNonExclusive(xpath));
            lock.setModified(now);
            file.setModified(now);
            saveOrUpdateItems.add(file);
            saveOrUpdateItems.add(lock);
        } else if (!fileExists && lock != null) {
            // fileSystem file NOT exists AND db schedule exists -> delete
            deleteItems.add(lock);
            // if file exists in db delete item too
            if(file != null) {
                deleteItems.add(file);
            }
        }
    }
    
    private Integer getJobChainNodeType(String nodeName, Element jobChainNode) {
        switch(nodeName){
        case "job_chain_node":
            if(jobChainNode.hasAttribute(JS_OBJECT_TYPE_JOB.toLowerCase())){
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
    
    private Long initOverviewRequest() {
        StringBuilder connectTo = new StringBuilder();
        connectTo.append(masterUrl);
        connectTo.append(WEBSERVICE_FILE_BASED_URL);
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder();
            uriBuilder.setPath(connectTo.toString());
            uriBuilder.addParameter(WEBSERVICE_PARAM_KEY_RETURN, WEBSERVICE_PARAM_VALUE_FILEBASED_OVERVIEW);
            JsonObject result = getJsonObjectFromResponse(uriBuilder.build(), true);
            LOGGER.info(result.toString());
            JsonNumber jsonEventId = result.getJsonNumber(EVENT_ID);
            if (jsonEventId != null) {
                return jsonEventId.longValue();
            }
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
    
    private JsonObject getFileBasedEvents(Long eventId) throws Exception {
        StringBuilder connectTo = new StringBuilder();
        connectTo.append(masterUrl);
        connectTo.append(WEBSERVICE_EVENTS_URL);
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(connectTo.toString());
            uriBuilder.addParameter(WEBSERVICE_PARAM_KEY_RETURN, WEBSERVICE_PARAM_VALUE_FILEBASED_EVENT);
            uriBuilder.addParameter(WEBSERVICE_PARAM_KEY_TIMEOUT, WEBSERVICE_PARAM_VALUE_TIMEOUT);
            uriBuilder.addParameter(WEBSERVICE_PARAM_KEY_AFTER, eventId.toString());
            JsonObject result = getJsonObjectFromResponse(uriBuilder.build(), false);
            LOGGER.info(result.toString());
            return result;
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
    
    private JsonObject getJsonObjectFromResponse(URI uri, boolean withBody) throws Exception {
        restApiClient.addHeader(CONTENT_TYPE_HEADER_KEY, APPLICATION_HEADER_JSON_VALUE);
        restApiClient.addHeader(ACCEPT_HEADER_KEY, APPLICATION_HEADER_JSON_VALUE);
        String response = null;
        if(withBody) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add(POST_BODY_JSON_KEY, POST_BODY_JSON_VALUE);
            response = restApiClient.postRestService(uri, builder.build().toString());
        } else {
            response = restApiClient.postRestService(uri, null);
        }
        int httpReplyCode = restApiClient.statusCode();
        String contentType = restApiClient.getResponseHeader(CONTENT_TYPE_HEADER_KEY);
        JsonObject json = null;
        if (contentType.contains(APPLICATION_HEADER_JSON_VALUE)) {
            JsonReader rdr = Json.createReader(new StringReader(response));
            json = rdr.readObject();
        }
        switch (httpReplyCode) {
        case 200:
            if (json != null) {
                return json;
            } else {
                throw new Exception("Unexpected content type '" + contentType + "'. Response: " + response);
            }
        case 400:
            // TO DO check Content-Type
            // for now the exception is plain/text instead of JSON
            // throw message item value
            if (json != null) {
                throw new Exception(json.getString("message"));
            } else {
                throw new Exception("Unexpected content type '" + contentType + "'. Response: " + response);
            }
        default:
            throw new Exception(httpReplyCode + " " + restApiClient.getHttpResponse().getStatusLine().getReasonPhrase());
        }
    }
    
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

}