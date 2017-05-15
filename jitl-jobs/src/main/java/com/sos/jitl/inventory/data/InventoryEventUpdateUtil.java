package com.sos.jitl.inventory.data;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.sos.exception.SOSException;
import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.inventory.model.InventoryModel;
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
import com.sos.scheduler.engine.data.events.custom.VariablesCustomEvent;
import com.sos.scheduler.engine.eventbus.EventBus;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

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
    private static final Integer HTTP_CLIENT_SOCKET_TIMEOUT = 75000;
    private static final String WEBSERVICE_FILE_BASED_URL = "/jobscheduler/master/api/fileBased";
    private static final String WEBSERVICE_EVENTS_URL = "/jobscheduler/master/api/event";
    private static final String ACCEPT_HEADER_KEY = "Accept";
    private static final String CONTENT_TYPE_HEADER_KEY = "Content-Type";
    private static final String POST_BODY_JSON_KEY = "path";
    private static final String POST_BODY_JSON_VALUE = "/";
    private static final String EVENT_TYPE = "TYPE";
    private static final String EVENT_TYPE_REMOVED = "FileBasedRemoved";
    private static final String EVENT_TYPE_UPDATED = "FileBasedUpdated";
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
    private static final String FILE_TYPE_JOB = "job";
    private static final String FILE_TYPE_JOBCHAIN = "job_chain";
    private static final String FILE_TYPE_ORDER = "order";
    private static final String FILE_TYPE_PROCESS_CLASS = "process_class";
    private static final String FILE_TYPE_AGENT_CLUSTER = "agent_cluster";
    private static final String FILE_TYPE_SCHEDULE = "schedule";
    private static final String FILE_TYPE_LOCK = "lock";
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEventUpdateUtil.class);
    private Map<String, List<JsonObject>> groupedEvents = new HashMap<String, List<JsonObject>>();
    private String webserviceUrl = null;
    private SOSHibernateFactory factory = null;
    private DBItemInventoryInstance instance = null;
    private DBLayerInventory dbLayer = null;
    private String liveDirectory = null;
    private String cacheDirectory = "config/cache";
    private Long eventId = null;
    private String lastEventKey = null;
    private Long lastEventId = 0L;
    private List<DbItem> saveOrUpdateItems = new ArrayList<DbItem>();
    private List<DBItemInventoryJobChainNode> saveOrUpdateNodeItems = new ArrayList<DBItemInventoryJobChainNode>();
    private List<DbItem> deleteItems = new ArrayList<DbItem>();
    private Map<String, NodeList> jobChainNodesToSave = new HashMap<String, NodeList>();
    private JobSchedulerRestApiClient restApiClient;
    private CloseableHttpClient httpClient;
    private boolean closed = false;
    private String host;
    private Integer port;
    private SOSHibernateSession dbConnection = null;
    private EventBus customEventBus;
    private Map<String, Map<String, String>> eventVariables = new HashMap<String, Map<String, String>>();
    private boolean hasDbErrors = false;
    private Map<String, List<JsonObject>> backlogEvents = new HashMap<String, List<JsonObject>>();
    private Path schedulerXmlPath;
    private SchedulerXmlCommandExecutor xmlCommandExecutor;
    private String schedulerId;

    public InventoryEventUpdateUtil(String host, Integer port, SOSHibernateFactory factory, EventBus customEventBus,
            Path schedulerXmlPath, String schedulerId) {
        this.factory = factory;
        this.webserviceUrl = "http://localhost:" + port;
        this.host = host;
        this.port = port;
        this.customEventBus = customEventBus;
        this.schedulerXmlPath = schedulerXmlPath;
        this.schedulerId = schedulerId;
        initInstance();
        initRestClient();
    }

    public void execute() throws Exception {
        LOGGER.debug("[inventory] Processing of FileBasedEvents started!");
        eventId = initOverviewRequest();
        lastEventId = eventId;
        while (!closed) {
            try {
               if (hasDbErrors) {
                    LOGGER.debug("[inventory] hasDbErrors = true - processing of backlogged events started");
                    processBackloggedEvents();
                }
                execute(lastEventId, lastEventKey);
            } catch (SOSHibernateException e) {
                hasDbErrors = true;
                if (!closed) {
                    restartExecution();
                } else {
                    LOGGER.info("[inventory] execute: processing stopped.");
                }
            } catch (Exception e) {
                if (!closed) {
                    restartExecution();
                } else {
                    LOGGER.info("[inventory] execute: processing stopped.");
                }
            }
        }
    }

    private void execute(Long eventId, String lastKey) throws Exception {
        LOGGER.debug("[inventory] -- Processing FileBasedEvents --");
        JsonObject result = getFileBasedEvents(eventId);
        String type = result.getString(EVENT_TYPE);
        lastEventId = result.getJsonNumber(EVENT_ID).longValue();
        JsonArray events = result.getJsonArray(EVENT_SNAPSHOT);
        if (events != null && !events.isEmpty()) {
            processEventType(type, events, lastKey);
        } else if (EVENT_TYPE_EMPTY.equalsIgnoreCase(type)) {
            lastEventKey = lastKey;
        }
    }

    private void initInstance() {
        try {
            dbConnection = factory.openSession("inventory");
            dbLayer = new DBLayerInventory(dbConnection);
            instance = dbLayer.getInventoryInstance(schedulerId, host, port);
            if (instance != null) {
                liveDirectory = instance.getLiveDirectory();
            }
        } catch (Exception e) {
            LOGGER.error(String.format("[inventory] error occured receiving inventory instance from db with host: %1$s and "
                    + "port: %2$d; error: %3$s", host, port, e.getMessage()), e);
            hasDbErrors = true;
        } finally {
            dbConnection.close();
        }
    }

    private void initRestClient() {
        restApiClient = new JobSchedulerRestApiClient();
        restApiClient.setAutoCloseHttpClient(false);
        restApiClient.setSocketTimeout(HTTP_CLIENT_SOCKET_TIMEOUT);
        restApiClient.addHeader(CONTENT_TYPE_HEADER_KEY, APPLICATION_HEADER_JSON_VALUE);
        restApiClient.addHeader(ACCEPT_HEADER_KEY, APPLICATION_HEADER_JSON_VALUE);
        restApiClient.addHeader("Cache-Control", "no-cache, no-store, no-transform, must-revalidate");
        restApiClient.createHttpClient();
        httpClient = restApiClient.getHttpClient();
    }

    private void processBackloggedEvents() throws SOSHibernateException, Exception {
        try {
            hasDbErrors = false;
//            dbConnection.clearSession();
            dbConnection = factory.openSession("inventory");
            dbLayer = new DBLayerInventory(dbConnection);
            if (backlogEvents != null && !backlogEvents.isEmpty()) {
                LOGGER.info("[inventory] processing of backlogged events started due to an occurence of a previous error");
                if (backlogEvents.size() > 100) {
                    LOGGER.info("[inventory] backlog of events too long, complete configuration update started instead");
                    initInstance();
                    InventoryModel modelProcessing = new InventoryModel(factory, instance, schedulerXmlPath);
                    modelProcessing.setXmlCommandExecutor(xmlCommandExecutor);
                    modelProcessing.process();
                    LOGGER.info("[inventory] complete configuration update finished");
                    backlogEvents.clear();
                    LOGGER.debug("[inventory] backlogEvents cleared");
                } else {
                    processGroupedEvents(backlogEvents);
                    LOGGER.info("[inventory] processing of backlogged events finished");
                    backlogEvents.clear();
                    LOGGER.debug("[inventory] backlogEvents cleared");
                }
            }
        } catch (Exception e) {
            try {
                dbConnection.rollback();
            } catch (Exception e1) {
                // TODO exception handling for rollback
            } finally {
                dbConnection.close();
            }
            throw e;
        }
    }

    public void restartExecution() {
        cleanup();
        if (httpClient == null) {
            initRestClient();
        } else {
            if (restApiClient != null) {
                restApiClient.closeHttpClient();
            } else {
                try {
                    httpClient.close();
                } catch (IOException e) {
                }
            }
            initRestClient();
        }
    }

    private void cleanup() {
        eventId = null;
        groupedEvents.clear();
        saveOrUpdateItems.clear();
        saveOrUpdateNodeItems.clear();
        deleteItems.clear();
        eventVariables.clear();
    }

    private JsonObject getLastEvent(String key, List<JsonObject> events) {
        JsonObject lastEvent = null;
        for (JsonObject event : events) {
            eventId = event.getJsonNumber(EVENT_ID).longValue();
            lastEvent = event;
        }
        return lastEvent;
    }

    private void addToExistingGroup(String path, List<JsonObject> events) {
        List<JsonObject> existingGroup = groupedEvents.get(path);
        existingGroup.addAll(events);
        groupedEvents.put(path, existingGroup);
        backlogEvents.put(path, existingGroup);
    }

    private void groupEvents(JsonArray events, String lastKey) {
        for (int i = 0; i < events.size(); i++) {
            List<JsonObject> pathEvents = new ArrayList<JsonObject>();
            String key = ((JsonObject) events.getJsonObject(i)).getString(EVENT_KEY);
            if (lastKey == null) {
                lastKey = key;
                pathEvents.add((JsonObject) events.get(i));
            } else if (lastKey.equalsIgnoreCase(key)) {
                pathEvents.add((JsonObject) events.get(i));
            } else if (!lastKey.equalsIgnoreCase(key)) {
                pathEvents.clear();
                lastKey = key;
                pathEvents.add((JsonObject) events.get(i));
            }
            if (groupedEvents.containsKey(lastKey)) {
                addToExistingGroup(lastKey, pathEvents);
            } else {
                groupedEvents.put(lastKey, pathEvents);
                backlogEvents.put(lastKey, pathEvents);
            }
        }
        lastEventKey = lastKey;
    }

    private void processGroupedEvents(Map<String, List<JsonObject>> events) throws Exception {
        String lastKey = null;
        for (String key : events.keySet()) {
            lastKey = key;
            JsonObject event = getLastEvent(key, events.get(key));
            eventId = processEvent(event);
        }
        processDbTransaction();
        saveOrUpdateItems.clear();
        saveOrUpdateNodeItems.clear();
        deleteItems.clear();
        eventVariables.clear();
        groupedEvents.clear();
        lastEventKey = lastKey;
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

    private void processDbTransaction() throws SOSHibernateException {
        Map<DBItemInventoryJobChain, List<DBItemInventoryJob>> processedJobChains = new HashMap<DBItemInventoryJobChain,
                List<DBItemInventoryJob>>();
        try {
            LOGGER.debug("[inventory] processing of DB transactions started");
            dbConnection = factory.openSession("inventory");
            dbLayer = new DBLayerInventory(dbConnection);
            dbConnection.beginTransaction();
            Long fileId = null;
            String filePath = null;
            for (DbItem item : saveOrUpdateItems) {
                if (item instanceof DBItemInventoryFile) {
                    dbConnection.saveOrUpdate(item);
                    fileId = ((DBItemInventoryFile) item).getId();
                    filePath = ((DBItemInventoryFile) item).getFileName();
                    LOGGER.debug(String.format("[inventory] file %1$s saved or updated", filePath));
                } else {
                    if (filePath != null && fileId != null) {
                        String name = getName(item);
                        if (name != null && !name.isEmpty() && filePath.contains(name)) {
                            setFileId(item, fileId);
                        }
                        dbConnection.saveOrUpdate(item);
                        LOGGER.debug(String.format("[inventory] item %1$s saved or updated", name));
                        if (item instanceof DBItemInventoryJobChain) {
                            if (processedJobChains.keySet().contains((DBItemInventoryJobChain) item)) {
                                processedJobChains.get((DBItemInventoryJobChain) item).addAll(
                                        dbLayer.getAllJobsFromJobChain(((DBItemInventoryJobChain) item).getInstanceId(),
                                                ((DBItemInventoryJobChain) item).getId()));
                            } else {
                                processedJobChains.put((DBItemInventoryJobChain) item, dbLayer.getAllJobsFromJobChain(
                                        ((DBItemInventoryJobChain) item).getInstanceId(),
                                        ((DBItemInventoryJobChain) item).getId()));
                            }
                            NodeList nl = jobChainNodesToSave.get(getName(item));
                            dbLayer.deleteOldNodes((DBItemInventoryJobChain) item);
                            createJobChainNodes(nl, (DBItemInventoryJobChain) item);
                        }
                        fileId = null;
                        filePath = null;
                    } else {
                        dbConnection.saveOrUpdate(item);
                        LOGGER.debug(String.format("[inventory] item %1$s saved or updated", getName(item)));
                        if (item instanceof DBItemInventoryJobChain) {
                            if (processedJobChains.keySet().contains((DBItemInventoryJobChain) item)) {
                                processedJobChains.get((DBItemInventoryJobChain) item).addAll(
                                        dbLayer.getAllJobsFromJobChain(((DBItemInventoryJobChain) item).getInstanceId(),
                                                ((DBItemInventoryJobChain) item).getId()));
                            } else {
                                processedJobChains.put((DBItemInventoryJobChain) item, dbLayer.getAllJobsFromJobChain(
                                        ((DBItemInventoryJobChain) item).getInstanceId(),
                                        ((DBItemInventoryJobChain) item).getId()));
                            }
                            NodeList nl = jobChainNodesToSave.get(getName(item));
                            dbLayer.deleteOldNodes((DBItemInventoryJobChain) item);
                            createJobChainNodes(nl, (DBItemInventoryJobChain) item);
                        }
                    }
                }
            }
            for (DBItemInventoryJobChainNode node : saveOrUpdateNodeItems) {
                dbConnection.saveOrUpdate(node);
                LOGGER.debug(String.format("[inventory] job chain nodes for item %1$s saved or updated", node.getName()));
            }
            for (DBItemInventoryJobChain jobChain : processedJobChains.keySet()) {
                Set<DBItemInventoryJob> jobsToUpdate = new HashSet<DBItemInventoryJob>();
                jobsToUpdate.addAll(processedJobChains.get(jobChain));
                jobsToUpdate.addAll(dbLayer.getAllJobsFromJobChain(jobChain.getInstanceId(), jobChain.getId()));
                for (DBItemInventoryJob job : jobsToUpdate) {
                    job.setModified(Date.from(Instant.now()));
                }
                List<DBItemInventoryJob> toUpdate = new ArrayList<DBItemInventoryJob>();
                toUpdate.addAll(jobsToUpdate);
                dbLayer.refreshUsedInJobChains(jobChain.getInstanceId(), toUpdate);
            }
            processedJobChains.clear();
            for (DbItem item : deleteItems) {
                dbConnection.delete(item);
                LOGGER.debug(String.format("[inventory] item %1$s deleted", getName(item)));
            }
            dbConnection.commit();
            if (customEventBus != null && !hasDbErrors) {
                for (String key : eventVariables.keySet()) {
                    customEventBus.publishJava(VariablesCustomEvent.keyed(key, eventVariables.get(key)));
                    LOGGER.info(String.format("[inventory] Custom Event published on object %1$s!", key));
                }
                eventVariables.clear();
            } else {
                LOGGER.debug("[inventory] Custom Events not published due to errors or EventBus is NULL!");
            }
            LOGGER.debug("[inventory] processing of DB transactions finished");
            dbConnection.close();
        } catch (Exception e) {
            hasDbErrors = true;
            try {
                dbConnection.rollback();
            } catch (Exception e1) {
                // TODO: exception handling for rollback
            } finally {
                processedJobChains.clear();
                dbConnection.close();
            }
            if (!closed) {
//                LOGGER.error("[inventory] processing of DB transactions not finished due to errors, processing rollback");
                throw new SOSHibernateException(
                        "[inventory] processing of DB transactions not finished due to errors, processing rollback", e.getCause());
            }
        }
    }

    private void processEventType(String type, JsonArray events, String lastKey) throws Exception {
        if (!closed) {
            switch (type) {
            case EVENT_TYPE_NON_EMPTY:
                groupEvents(events, lastKey);
                processGroupedEvents(groupedEvents);
                break;
            case EVENT_TYPE_TORN:
                restartExecution();
                break;
            }
        }
    }

    private Long processEvent(JsonObject event) throws SOSHibernateException, Exception {
        String key = null;
        try {
            if (!closed && event != null) {
                key = event.getString(EVENT_KEY);
                String[] keySplit = key.split(":");
                String objectType = keySplit[0];
                String path = keySplit[1];
                eventId = event.getJsonNumber(EVENT_ID).longValue();
                switch (objectType) {
                case JS_OBJECT_TYPE_JOB:
                    processJobEvent(path, event, key);
                    break;
                case JS_OBJECT_TYPE_JOBCHAIN:
                    processJobChainEvent(path, event, key);
                    break;
                case JS_OBJECT_TYPE_ORDER:
                    processOrderEvent(path, event, key);
                    break;
                case JS_OBJECT_TYPE_PROCESS_CLASS:
                    processProcessClassEvent(path, event, key);
                    break;
                case JS_OBJECT_TYPE_SCHEDULE:
                    processScheduleEvent(path, event, key);
                    break;
                case JS_OBJECT_TYPE_LOCK:
                    processLockEvent(path, event, key);
                    break;
                case JS_OBJECT_TYPE_FOLDER:
                    break;
                }
            }
            return eventId;
        } catch (Exception e) {
            if (!closed) {
                LOGGER.error(String.format("[inventory] error occured processing event on %1$s", key), e);
                throw e;
            }
            return null;
        }
    }

    private Path fileExists(String path) {
        String normalizePath = path.replaceFirst("^/+", "");
        Path p = Paths.get(liveDirectory, normalizePath);
        if (!Files.exists(p)) {
            p = Paths.get(cacheDirectory, normalizePath);
            if (!Files.exists(p)) {
                p = null;
            }
        }
        return p;
    }

    private DBItemInventoryFile createNewInventoryFile(Long instanceId, Path filePath, String name, String type) {
        DBItemInventoryFile dbFile = new DBItemInventoryFile();
        Path path = Paths.get(name);
        String fileDirectory = path.getParent().toString().replace('\\', '/');
        String fileBaseName = path.getFileName().toString();
        dbFile.setFileBaseName(fileBaseName);
        dbFile.setFileDirectory(fileDirectory);
        dbFile.setFileName(name.replace('\\', '/'));
        dbFile.setFileType(type.toLowerCase());
        dbFile.setInstanceId(instanceId);
        if (filePath != null) {
            try {
                BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                dbFile.setFileCreated(ReportUtil.convertFileTime2UTC(attrs.creationTime()));
                dbFile.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                dbFile.setFileLocalCreated(ReportUtil.convertFileTime2Local(attrs.creationTime()));
                dbFile.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
            } catch (IOException e) {
                LOGGER.warn(String.format("[inventory] cannot read file attributes. file = %1$s, exception = %2$s",
                        filePath.toString(), e
                        .getMessage()), e);
            } catch (Exception e) {
                LOGGER.warn("[inventory] cannot convert files create and modified timestamps! " + e.getMessage(), e);
            }
        }
        return dbFile;
    }

    private void processJobEvent(String path, JsonObject event, String key) throws Exception {
        Map<String, String> values = new HashMap<String, String>();
        try {
            dbConnection = factory.openSession("inventory");
            dbLayer = new DBLayerInventory(dbConnection);
            Date now = Date.from(Instant.now());
            LOGGER.debug(String.format("[inventory] processing event on JOB: %1$s with path: %2$s", Paths.get(path).getFileName(),
                    Paths.get(path).getParent()));
            Path filePath = fileExists(path + EConfigFileExtensions.JOB.extension());
            Long instanceId = null;
            if (instance != null) {
                instanceId = instance.getId();
                DBItemInventoryJob job = dbLayer.getInventoryJob(instanceId, path);
                DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId, path + EConfigFileExtensions.JOB.extension());
                // fileSystem File exists AND db job exists -> update
                // db file NOT exists AND db job NOT exists -> add
                boolean fileExists = filePath != null;
                if ((fileExists && job != null) || (fileExists && file == null && job == null)) {
                    if (file == null) {
                        file = createNewInventoryFile(instanceId, filePath, path + EConfigFileExtensions.JOB.extension(),
                                FILE_TYPE_JOB);
                        file.setCreated(now);
                    } else {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                            file.setModified(now);
                            file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                            file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                        } catch (IOException e) {
                            LOGGER.warn(String.format("[inventory] cannot read file attributes. file = %1$s, exception = %2$s",
                                    filePath.toString(), e.getMessage()), e);
                        } catch (Exception e) {
                            LOGGER.warn("[inventory] cannot convert files create and modified timestamps! " + e.getMessage(), e);
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
                    job.setRunTimeIsTemporary(false);
                    if (xPath.getRoot().hasAttribute("process_class")) {
                        String processClass = ReportXmlHelper.getProcessClass(xPath);
                        Path jobPath = Paths.get(job.getName());
                        processClass = jobPath.getParent().resolve(processClass).normalize().toString().replace('\\', '/');
                        DBItemInventoryProcessClass ipc = dbLayer.getProcessClassIfExists(instanceId, processClass);
                        if (ipc != null) {
                            job.setProcessClass(processClass);
                            job.setProcessClassName(ipc.getName());
                            job.setProcessClassId(ipc.getId());
                        } else {
                            job.setProcessClass(processClass);
                            job.setProcessClassName(DBLayer.DEFAULT_NAME);
                            job.setProcessClassId(DBLayer.DEFAULT_ID);
                        }
                    } else {
                        job.setProcessClass(null);
                        job.setProcessClassId(DBLayer.DEFAULT_ID);
                        job.setProcessClassName(DBLayer.DEFAULT_NAME);
                    }
                    String schedule = ReportXmlHelper.getScheduleFromRuntime(xPath);
                    if (schedule != null && !schedule.isEmpty()) {
                        String scheduleName = Paths.get(path).getParent().resolve(schedule).normalize().toString()
                                .replace("\\", "/");
                        DBItemInventorySchedule is = dbLayer.getScheduleIfExists(instanceId, schedule, scheduleName);
                        if (is != null) {
                            job.setSchedule(scheduleName);
                            job.setScheduleName(is.getName());
                            job.setScheduleId(is.getId());
                        } else {
                            job.setSchedule(scheduleName);
                            job.setScheduleName(DBLayer.DEFAULT_NAME);
                            job.setScheduleId(DBLayer.DEFAULT_ID);
                        }
                    } else {
                        job.setScheduleId(DBLayer.DEFAULT_ID);
                        job.setScheduleName(DBLayer.DEFAULT_NAME);
                    }
                    String maxTasks = xPath.getRoot().getAttribute("tasks");
                    if (maxTasks != null && !maxTasks.isEmpty()) {
                        job.setMaxTasks(Integer.parseInt(maxTasks));
                    } else {
                        job.setMaxTasks(1);
                    }
                    Boolean hasDescription = ReportXmlHelper.hasDescription(xPath);
                    if (hasDescription != null) {
                        job.setHasDescription(ReportXmlHelper.hasDescription(xPath));
                    }
                    job.setModified(now);
                    file.setModified(now);
                    saveOrUpdateItems.add(file);
                    saveOrUpdateItems.add(job);
                    values.put("InventoryEventUpdateFinished", EVENT_TYPE_UPDATED);
                } else if (!fileExists && job != null) {
                    // fileSystem file NOT exists AND job exists -> delete
                    deleteItems.add(job);
                    // if file exists in db delete item too
                    if (file != null) {
                        deleteItems.add(file);
                    }
                    values.put("InventoryEventUpdateFinished", EVENT_TYPE_REMOVED);
                }
            }
            eventVariables.put(key, values);
            dbConnection.close();
        } catch (SOSHibernateException e) {
            hasDbErrors = true;
            throw e;
        }
    }

    private void processJobChainEvent(String path, JsonObject event, String key) throws Exception {
        Map<String, String> values = new HashMap<String, String>();
        try {
            dbConnection = factory.openSession("inventory");
            dbLayer = new DBLayerInventory(dbConnection);
            Date now = Date.from(Instant.now());
            Path filePath = fileExists(path + EConfigFileExtensions.JOB_CHAIN.extension());
            Long instanceId = null;
            if (instance != null) {
                instanceId = instance.getId();
                LOGGER.debug(String.format("[inventory] processing event on JOBCHAIN: %1$s with path: %2$s",
                        Paths.get(path).getFileName(), Paths.get(path).getParent()));
                DBItemInventoryJobChain jobChain = dbLayer.getInventoryJobChain(instanceId, path);
                DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId,
                        path + EConfigFileExtensions.JOB_CHAIN.extension());
                // fileSystem File exists AND db schedule exists -> update
                // db file NOT exists AND db schedule NOT exists -> add
                boolean fileExists = filePath != null;
                if ((fileExists && jobChain != null) || (fileExists && file == null && jobChain == null)) {
                    if (file == null) {
                        file = createNewInventoryFile(instanceId, filePath, path + EConfigFileExtensions.JOB_CHAIN.extension(),
                                FILE_TYPE_JOBCHAIN);
                        file.setCreated(now);
                    } else {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                            file.setModified(now);
                            file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                            file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                        } catch (IOException e) {
                            LOGGER.warn(String.format("[inventory] cannot read file attributes. file = %1$s, exception = %2$s",
                                    filePath.toString(), e.getMessage()), e);
                        } catch (Exception e) {
                            LOGGER.warn("[inventory] cannot convert files create and modified timestamps! " + e.getMessage(), e);
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
                    if (maxOrders != null && !maxOrders.isEmpty()) {
                        jobChain.setMaxOrders(Integer.parseInt(maxOrders));
                    }
                    jobChain.setDistributed("yes".equalsIgnoreCase(xpath.getRoot().getAttribute("distributed")));
                    if (xpath.getRoot().hasAttribute(FILE_TYPE_PROCESS_CLASS)) {
                        String processClass = ReportXmlHelper.getProcessClass(xpath);
                        Path jobChainPath = Paths.get(jobChain.getName());
                        processClass = jobChainPath.getParent().resolve(processClass).normalize().toString().replace('\\', '/');
                        DBItemInventoryProcessClass ipc = dbLayer.getProcessClassIfExists(instanceId, processClass);
                        if (ipc != null) {
                            jobChain.setProcessClass(processClass);
                            jobChain.setProcessClassName(ipc.getName());
                            jobChain.setProcessClassId(ipc.getId());
                        } else {
                            jobChain.setProcessClass(processClass);
                            jobChain.setProcessClassName(DBLayer.DEFAULT_NAME);
                            jobChain.setProcessClassId(DBLayer.DEFAULT_ID);
                        }
                    } else {
                        jobChain.setProcessClass(null);
                        jobChain.setProcessClassName(DBLayer.DEFAULT_NAME);
                        jobChain.setProcessClassId(DBLayer.DEFAULT_ID);
                    }
                    if (xpath.getRoot().hasAttribute("file_watching_process_class")) {
                        String fwProcessClass = ReportXmlHelper.getFileWatchingProcessClass(xpath);
                        Path jobChainPath = Paths.get(jobChain.getName());
                        fwProcessClass = jobChainPath.getParent().resolve(fwProcessClass).normalize().toString()
                                .replace('\\', '/');
                        DBItemInventoryProcessClass ipc = dbLayer.getProcessClassIfExists(instanceId, fwProcessClass);
                        if (ipc != null) {
                            jobChain.setFileWatchingProcessClass(fwProcessClass);
                            jobChain.setFileWatchingProcessClassName(ipc.getName());
                            jobChain.setFileWatchingProcessClassId(ipc.getId());
                        } else {
                            jobChain.setFileWatchingProcessClass(fwProcessClass);
                            jobChain.setFileWatchingProcessClassName(DBLayer.DEFAULT_NAME);
                            jobChain.setFileWatchingProcessClassId(DBLayer.DEFAULT_ID);
                        }
                    } else {
                        jobChain.setFileWatchingProcessClass(null);
                        jobChain.setFileWatchingProcessClassName(DBLayer.DEFAULT_NAME);
                        jobChain.setFileWatchingProcessClassId(DBLayer.DEFAULT_ID);
                    }
                    NodeList nl = ReportXmlHelper.getRootChilds(xpath);
                    jobChain.setModified(now);
                    file.setModified(now);
                    saveOrUpdateItems.add(file);
                    saveOrUpdateItems.add(jobChain);
                    jobChainNodesToSave.put(jobChain.getName(), nl);
                    values.put("InventoryEventUpdateFinished", EVENT_TYPE_UPDATED);
                } else if (!fileExists && jobChain != null) {
                    // fileSystem file NOT exists AND db jobChain exists -> delete
                    // first delete All Nodes of the jobChain then the jobChain itself
                    List<DBItemInventoryJobChainNode> nodes = dbLayer.getJobChainNodes(instanceId, jobChain.getId());
                    if (nodes != null && !nodes.isEmpty()) {
                        deleteItems.addAll(nodes);
                    }
                    deleteItems.add(jobChain);
                    // if file exists in db delete item too
                    if (file != null) {
                        deleteItems.add(file);
                    }
                    values.put("InventoryEventUpdateFinished", EVENT_TYPE_REMOVED);
                }
            }
            eventVariables.put(key, values);
            dbConnection.close();
        } catch (SOSHibernateException e) {
            hasDbErrors = true;
            throw e;
        }
    }

    private void createJobChainNodes(NodeList nl, DBItemInventoryJobChain jobChain) throws Exception {
        Date now = Date.from(Instant.now());
        int ordering = 1;
        try {
            if (nl != null) {
                for (int j = 0; j < nl.getLength(); j++) {
                    Element jobChainNodeElement = (Element) nl.item(j);
                    String jobName = null;
                    String nodeName = jobChainNodeElement.getNodeName();
                    String job = jobChainNodeElement.getAttribute(FILE_TYPE_JOB);
                    String state = jobChainNodeElement.getAttribute("state");
                    String nextState = jobChainNodeElement.getAttribute("next_state");
                    String errorState = jobChainNodeElement.getAttribute("error_state");
                    Integer nodeType = getJobChainNodeType(nodeName, jobChainNodeElement);
                    String directory = jobChainNodeElement.getAttribute("directory");
                    String regex = null;
                    if (jobChainNodeElement.hasAttribute("regex")) {
                        regex = jobChainNodeElement.getAttribute("regex");
                    }

                    DBItemInventoryJobChainNode node =
                            dbLayer.getJobChainNodeIfExists(jobChain.getInstanceId(), jobChain.getId(), nodeType, state,
                                    directory, regex);
                    if (node == null) {
                        node = new DBItemInventoryJobChainNode();
                        node.setInstanceId(jobChain.getInstanceId());
                        node.setJobChainId(jobChain.getId());
                        node.setState(state);
                        node.setCreated(now);
                    }
                    node.setName(nodeName);
                    node.setNextState(nextState);
                    node.setErrorState(errorState);
                    node.setCreated(ReportUtil.getCurrentDateTime());
                    node.setModified(ReportUtil.getCurrentDateTime());
                    node.setNestedJobChainId(DBLayer.DEFAULT_ID);
                    node.setNestedJobChainName(DBLayer.DEFAULT_NAME);
                    /** new Items since 1.11 */
                    if (job != null && !job.isEmpty()) {
                        Path jobPath = Paths.get(jobChain.getName()).getParent().resolve(job).normalize();
                        jobName = jobPath.toString().replace("\\", "/");
                        if (jobName != null && !jobName.isEmpty()) {
                            node.setJobName(jobName);
                        } else {
                            node.setJobName(DBLayer.DEFAULT_NAME);
                        }
                        node.setJob(job);
                        DBItemInventoryJob jobDbItem = dbLayer.getJobIfExists(jobChain.getInstanceId(), job, jobName);
                        if (jobDbItem != null) {
                            node.setJobId(jobDbItem.getId());
                        } else {
                            node.setJobId(DBLayer.DEFAULT_ID);
                        }
                    } else {
                        node.setJobId(DBLayer.DEFAULT_ID);
                        node.setJobName(DBLayer.DEFAULT_NAME);
                    }
                    node.setNodeType(nodeType);
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
                        if (jobChainNodeElement.hasAttribute(FILE_TYPE_JOBCHAIN)) {
                            String jobchain = jobChainNodeElement.getAttribute(FILE_TYPE_JOBCHAIN);
                            Path jobChainPath = Paths.get(jobChain.getName());
                            String nestedJobChain = jobChainPath.getParent().resolve(jobchain).normalize().toString()
                                    .replace('\\', '/');
                            DBItemInventoryJobChain ijc = dbLayer.getJobChain(jobChain.getInstanceId(), nestedJobChain);
                            if (ijc != null) {
                                node.setNestedJobChain(nestedJobChain);
                                node.setNestedJobChainName(ijc.getName());
                                node.setNestedJobChainId(ijc.getId());
                            } else {
                                node.setNestedJobChain(nestedJobChain);
                                node.setNestedJobChainName(DBLayer.DEFAULT_NAME);
                                node.setNestedJobChainId(DBLayer.DEFAULT_ID);
                            }
                        } else {
                            node.setNestedJobChain(null);
                            node.setNestedJobChainId(DBLayer.DEFAULT_ID);
                            node.setNestedJobChainName(DBLayer.DEFAULT_NAME);
                        }
                        break;
                    case 3:
                        node.setDirectory(directory);
                        if (regex != null) {
                            node.setRegex(regex);
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
        } catch (SOSHibernateException e) {
            hasDbErrors = true;
            throw e;
        }
    }

    private void processOrderEvent(String path, JsonObject event, String key) throws Exception {
        Map<String, String> values = new HashMap<String, String>();
        try {
            dbConnection = factory.openSession("inventory");
            dbLayer = new DBLayerInventory(dbConnection);
            Date now = Date.from(Instant.now());
            Path filePath = fileExists(path + EConfigFileExtensions.ORDER.extension());
            Long instanceId = null;
            if (instance != null) {
                instanceId = instance.getId();
                LOGGER.debug(String.format("[inventory] processing event on ORDER: %1$s with path: %2$s",
                        Paths.get(path).getFileName(), Paths.get(path).getParent()));
                DBItemInventoryOrder order = dbLayer.getInventoryOrder(instanceId, path);
                DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId, path + EConfigFileExtensions.ORDER.extension());
                // fileSystem File exists AND db schedule exists -> update
                // db file NOT exists AND db schedule NOT exists -> add
                boolean fileExists = filePath != null;
                if ((fileExists && order != null) || (fileExists && file == null && order == null)) {
                    if (file == null) {
                        file = createNewInventoryFile(instanceId, filePath, path + EConfigFileExtensions.ORDER.extension(),
                                FILE_TYPE_ORDER);
                        file.setCreated(now);
                    } else {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                            file.setModified(now);
                            file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                            file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                        } catch (IOException e) {
                            LOGGER.warn(String.format("[inventory] cannot read file attributes. file = %1$s, exception = %2$s",
                                    filePath.toString(), e.getMessage()), e);
                        } catch (Exception e) {
                            LOGGER.warn("[inventory] cannot convert files create and modified timestamps! " + e.getMessage(), e);
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
                    String jobChainName = path.substring(0, path.indexOf(","));
                    String orderId = baseName.substring(baseName.lastIndexOf(",") + 1);
                    boolean isRuntimeDefined = ReportXmlHelper.isRuntimeDefined(xpath);
                    order.setFileId(file.getId());
                    order.setJobChainName(jobChainName);
                    order.setName(path);
                    order.setBaseName(baseName);
                    order.setOrderId(orderId);
                    order.setTitle(title);
                    order.setIsRuntimeDefined(isRuntimeDefined);
                    order.setRunTimeIsTemporary(false);
                    /** new Items since 1.11 */
                    Long jobChainId = dbLayer.getJobChainId(instanceId, jobChainName);
                    if (jobChainId != null) {
                        order.setJobChainId(jobChainId);
                    } else {
                        order.setJobChainId(DBLayer.DEFAULT_ID);
                    }
                    if (xpath.getRoot().hasAttribute("state")) {
                        order.setInitialState(xpath.getRoot().getAttribute("state"));
                    }
                    if (xpath.getRoot().hasAttribute("end_state")) {
                        order.setEndState(xpath.getRoot().getAttribute("end_state"));
                    }
                    if (xpath.getRoot().hasAttribute("priority")) {
                        String priority = xpath.getRoot().getAttribute("priority");
                        if (priority != null && !priority.isEmpty()) {
                            order.setPriority(Integer.parseInt(priority));
                        }
                    }
                    String schedule = ReportXmlHelper.getScheduleFromRuntime(xpath);
                    if (schedule != null && !schedule.isEmpty()) {
                        String scheduleName = Paths.get(path).getParent().resolve(schedule).normalize().toString()
                                .replace("\\", "/");
                        DBItemInventorySchedule is = dbLayer.getScheduleIfExists(instanceId, schedule, scheduleName);
                        if (is != null) {
                            order.setSchedule(scheduleName);
                            order.setScheduleName(is.getName());
                            order.setScheduleId(is.getId());
                        } else {
                            order.setSchedule(scheduleName);
                            order.setScheduleName(DBLayer.DEFAULT_NAME);
                            order.setScheduleId(DBLayer.DEFAULT_ID);
                        }
                    } else {
                        order.setSchedule(null);
                        order.setScheduleId(DBLayer.DEFAULT_ID);
                        order.setScheduleName(DBLayer.DEFAULT_NAME);
                    }
                    order.setModified(now);
                    file.setModified(now);
                    saveOrUpdateItems.add(file);
                    saveOrUpdateItems.add(order);
                    values.put("InventoryEventUpdateFinished", EVENT_TYPE_UPDATED);
                } else if (!fileExists && order != null) {
                    // fileSystem file NOT exists AND db schedule exists -> delete
                    deleteItems.add(order);
                    // if file exists in db delete item too
                    if (file != null) {
                        deleteItems.add(file);
                    }
                    values.put("InventoryEventUpdateFinished", EVENT_TYPE_REMOVED);
                }
            }
            eventVariables.put(key, values);
            dbConnection.close();
        } catch (SOSHibernateException e) {
            hasDbErrors = true;
            throw e;
        }
    }

    private void processProcessClassEvent(String path, JsonObject event, String key) throws Exception {
        Map<String, String> values = new HashMap<String, String>();
        try {
            dbConnection = factory.openSession("inventory");
            dbLayer = new DBLayerInventory(dbConnection);
            Date now = Date.from(Instant.now());
            Path filePath = fileExists(path + EConfigFileExtensions.PROCESS_CLASS.extension());
            Long instanceId = null;
            if (instance != null) {
                instanceId = instance.getId();
                LOGGER.debug(String.format("[inventory] processing event on PROCESS_CLASS: %1$s with path: %2$s",
                        Paths.get(path).getFileName(), Paths.get(path).getParent()));
                DBItemInventoryProcessClass pc = dbLayer.getInventoryProcessClass(instanceId, path);
                DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId,
                        path + EConfigFileExtensions.PROCESS_CLASS.extension());
                // fileSystem File exists AND db schedule exists -> update
                // db file NOT exists AND db schedule NOT exists -> add
                boolean fileExists = filePath != null;
                if ((fileExists && pc != null) || (fileExists && file == null && pc == null)) {
                    SOSXMLXPath xpath = new SOSXMLXPath(filePath);
                    if (xpath.getRoot() == null) {
                        throw new Exception(String.format("xpath root missing"));
                    }
                    boolean hasAgent = ReportXmlHelper.hasAgents(xpath);
                    String fileType = hasAgent ? FILE_TYPE_AGENT_CLUSTER : FILE_TYPE_PROCESS_CLASS;

                    if (file == null) {
                        file = createNewInventoryFile(instanceId, filePath,
                                path + EConfigFileExtensions.PROCESS_CLASS.extension(), fileType);
                        file.setCreated(now);
                    } else {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                            file.setModified(now);
                            file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                            file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                            file.setFileType(fileType);
                        } catch (IOException e) {
                            LOGGER.warn(String.format("[inventory] cannot read file attributes. file = %1$s, exception = %2$s",
                                    filePath.toString(), e.getMessage()), e);
                        } catch (Exception e) {
                            LOGGER.warn("[inventory] cannot convert files create and modified timestamps! " + e.getMessage(), e);
                        }
                    }
                    if (pc == null) {
                        pc = new DBItemInventoryProcessClass();
                        pc.setInstanceId(instanceId);
                        pc.setName(path);
                        pc.setBasename(Paths.get(path).getFileName().toString());
                        pc.setCreated(now);
                    }
                    pc.setFileId(file.getId());
                    pc.setMaxProcesses(ReportXmlHelper.getMaxProcesses(xpath));
                    pc.setHasAgents(hasAgent);
                    pc.setModified(now);
                    file.setModified(now);
                    saveOrUpdateItems.add(file);
                    saveOrUpdateItems.add(pc);
                    values.put("InventoryEventUpdateFinished", EVENT_TYPE_UPDATED);
                } else if (!fileExists && pc != null) {
                    // fileSystem file NOT exists AND db schedule exists -> delete
                    deleteItems.add(pc);
                    // if file exists in db delete item too
                    if (file != null) {
                        deleteItems.add(file);
                    }
                    values.put("InventoryEventUpdateFinished", EVENT_TYPE_REMOVED);
                }
            }
            eventVariables.put(key, values);
            dbConnection.close();
        } catch (SOSHibernateException e) {
            hasDbErrors = true;
            throw e;
        }
    }

    private void processScheduleEvent(String path, JsonObject event, String key) throws Exception {
        Map<String, String> values = new HashMap<String, String>();
        try {
            dbConnection = factory.openSession("inventory");
            dbLayer = new DBLayerInventory(dbConnection);
            Date now = Date.from(Instant.now());
            Path filePath = fileExists(path + EConfigFileExtensions.SCHEDULE.extension());
            Long instanceId = null;
            if (instance != null) {
                instanceId = instance.getId();
                LOGGER.debug(String.format("[inventory] processing event on SCHEDULE: %1$s with path: %2$s",
                        Paths.get(path).getFileName(), Paths.get(path).getParent()));
                DBItemInventorySchedule schedule = dbLayer.getInventorySchedule(instanceId, path);
                DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId, path + EConfigFileExtensions.SCHEDULE.extension());
                // fileSystem File exists AND db schedule exists -> update
                // db file NOT exists AND db schedule NOT exists -> add
                boolean fileExists = filePath != null;
                if ((fileExists && schedule != null) || (fileExists && file == null && schedule == null)) {
                    if (file == null) {
                        file = createNewInventoryFile(instanceId, filePath, path + EConfigFileExtensions.SCHEDULE.extension(),
                                FILE_TYPE_SCHEDULE);
                        file.setCreated(now);
                    } else {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                            file.setModified(now);
                            file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                            file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                        } catch (IOException e) {
                            LOGGER.warn(String.format("[inventory] cannot read file attributes. file = %1$s, exception = %2$s",
                                    filePath.toString(), e.getMessage()), e);
                        } catch (Exception e) {
                            LOGGER.warn("[inventory] cannot convert files create and modified timestamps! " + e.getMessage(), e);
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
                    boolean pathNormalizationFailure = false;
                    Path parentPath = Paths.get(schedule.getName()).getParent();
                    DBItemInventorySchedule substituteItem =
                            dbLayer.getSubstituteIfExists(parentPath.resolve(schedule.getSubstitute()).normalize().toString()
                                    .replace("\\", "/"),
                                    schedule.getInstanceId());
                    if (substituteItem != null) {
                        schedule.setSubstituteId(substituteItem.getId());
                        try {
                            schedule.setSubstituteName(parentPath.resolve(substituteItem.getName()).normalize().toString()
                                    .replace("\\", "/"));
                        } catch (Exception e) {
                            pathNormalizationFailure = true;
                        }
                    } else {
                        schedule.setSubstituteId(DBLayer.DEFAULT_ID);
                        schedule.setSubstituteName(DBLayer.DEFAULT_NAME);
                    }
                    schedule.setModified(now);
                    file.setModified(now);
                    if (!pathNormalizationFailure) {
                        saveOrUpdateItems.add(file);
                        saveOrUpdateItems.add(schedule);
                        values.put("InventoryEventUpdateFinished", EVENT_TYPE_UPDATED);
                    }
                } else if (!fileExists && schedule != null) {
                    // fileSystem file NOT exists AND db schedule exists -> delete
                    deleteItems.add(schedule);
                    // if file exists in db delete item too
                    if (file != null) {
                        deleteItems.add(file);
                    }
                    values.put("InventoryEventUpdateFinished", EVENT_TYPE_REMOVED);
                }
            }
            eventVariables.put(key, values);
            dbConnection.close();
        } catch (SOSHibernateException e) {
            hasDbErrors = true;
            throw e;
        }
    }

    private void processLockEvent(String path, JsonObject event, String key) throws Exception {
        Map<String, String> values = new HashMap<String, String>();
        try {
            dbConnection = factory.openSession("inventory");
            dbLayer = new DBLayerInventory(dbConnection);
            Date now = Date.from(Instant.now());
            Path filePath = fileExists(path + EConfigFileExtensions.LOCK.extension());
            Long instanceId = null;
            if (instance != null) {
                instanceId = instance.getId();
                LOGGER.debug(String.format("[inventory] processing event on LOCK: %1$s with path: %2$s",
                        Paths.get(path).getFileName(), Paths.get(path).getParent()));
                DBItemInventoryLock lock = dbLayer.getInventoryLock(instanceId, path);
                DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId, path + EConfigFileExtensions.LOCK.extension());
                // fileSystem File exists AND db schedule exists -> update
                // db file NOT exists AND db schedule NOT exists -> add
                boolean fileExists = filePath != null;
                if ((fileExists && lock != null) || (fileExists && file == null && lock == null)) {
                    if (file == null) {
                        file = createNewInventoryFile(instanceId, filePath, path + EConfigFileExtensions.LOCK.extension(),
                                FILE_TYPE_LOCK);
                        file.setCreated(now);
                    } else {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                            file.setModified(now);
                            file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                            file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                        } catch (IOException e) {
                            LOGGER.warn(String.format("[inventory] cannot read file attributes. file = %1$s, exception = %2$s",
                                    filePath.toString(), e.getMessage()), e);
                        } catch (Exception e) {
                            LOGGER.warn("[inventory] cannot convert files create and modified timestamps! " + e.getMessage(), e);
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
                    values.put("InventoryEventUpdateFinished", EVENT_TYPE_UPDATED);
                } else if (!fileExists && lock != null) {
                    // fileSystem file NOT exists AND db schedule exists -> delete
                    deleteItems.add(lock);
                    // if file exists in db delete item too
                    if (file != null) {
                        deleteItems.add(file);
                    }
                    values.put("InventoryEventUpdateFinished", EVENT_TYPE_REMOVED);
                }
            }
            eventVariables.put(key, values);
            dbConnection.close();
        } catch (SOSHibernateException e) {
            hasDbErrors = true;
            throw e;
        }
    }

    private Integer getJobChainNodeType(String nodeName, Element jobChainNode) {
        switch (nodeName) {
        case "job_chain_node":
            if (jobChainNode.hasAttribute(JS_OBJECT_TYPE_JOB.toLowerCase())) {
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

    private Long initOverviewRequest() {
        if (!closed) {
            StringBuilder connectTo = new StringBuilder();
            connectTo.append(webserviceUrl);
            connectTo.append(WEBSERVICE_FILE_BASED_URL);
            URIBuilder uriBuilder;
            try {
                uriBuilder = new URIBuilder();
                uriBuilder.setPath(connectTo.toString());
                uriBuilder.addParameter(WEBSERVICE_PARAM_KEY_RETURN, WEBSERVICE_PARAM_VALUE_FILEBASED_OVERVIEW);
                JsonObject result = getJsonObjectFromResponse(uriBuilder.build(), true);
                JsonNumber jsonEventId = result.getJsonNumber(EVENT_ID);
                LOGGER.debug(String.format("[inventory] eventId received from Overview: %1$d", jsonEventId.longValue()));
                if (jsonEventId != null) {
                    lastEventId = jsonEventId.longValue();
                    return lastEventId;
                }
            } catch (Exception e) {
                if (!closed) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }

    private JsonObject getFileBasedEvents(Long eventId) throws Exception {
        if (!closed) {
            StringBuilder connectTo = new StringBuilder();
            connectTo.append(webserviceUrl);
            connectTo.append(WEBSERVICE_EVENTS_URL);
            URIBuilder uriBuilder;
            try {
                uriBuilder = new URIBuilder(connectTo.toString());
                uriBuilder.clearParameters();
                uriBuilder.addParameter(WEBSERVICE_PARAM_KEY_RETURN, WEBSERVICE_PARAM_VALUE_FILEBASED_EVENT);
                uriBuilder.addParameter(WEBSERVICE_PARAM_KEY_TIMEOUT, WEBSERVICE_PARAM_VALUE_TIMEOUT);
                uriBuilder.addParameter(WEBSERVICE_PARAM_KEY_AFTER, eventId.toString());
                LOGGER.debug(String.format("[inventory] request eventId send: %1$d", eventId));
                JsonObject result = getJsonObjectFromResponse(uriBuilder.build(), false);
                JsonNumber jsonEventId = result.getJsonNumber(EVENT_ID);
                String type = result.getString(EVENT_TYPE);
                if (EVENT_TYPE_NON_EMPTY.equalsIgnoreCase(type)) {
                    Thread.sleep(1000);
                    result = getJsonObjectFromResponse(uriBuilder.build(), false);
                }
                lastEventId = jsonEventId.longValue();
                LOGGER.debug(String.format("[inventory] eventId received from FileBasedEvents: %1$d", lastEventId));
                return result;
            } catch (Exception e) {
                if (!closed) {
                    LOGGER.error(e.getMessage(), e);
                    throw e;
                }
            }
        } else {
            throw new SOSException("[inventory] JobScheduler is closed!");
        }
        return null;
    }

    private JsonObject getJsonObjectFromResponse(URI uri, boolean withBody) throws Exception {
        if (!closed) {
            String response = null;
            if (withBody) {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                builder.add(POST_BODY_JSON_KEY, POST_BODY_JSON_VALUE);
                response = restApiClient.postRestService(uri, builder.build().toString());
            } else {
                response = restApiClient.postRestService(uri, null);
            }
            LOGGER.debug("[inventory] " + response);
            int httpReplyCode = restApiClient.statusCode();
            String contentType = restApiClient.getResponseHeader(CONTENT_TYPE_HEADER_KEY);
            switch (httpReplyCode) {
            case 200:
                JsonObject json = null;
                if (contentType.contains(APPLICATION_HEADER_JSON_VALUE)) {
                    JsonReader rdr = Json.createReader(new StringReader(response));
                    json = rdr.readObject();
                }
                if (json != null) {
                    return json;
                } else {
                    throw new Exception("[inventory] Unexpected content type '" + contentType + "'. Response: " + response);
                }
            case 400:
                throw new Exception("[inventory] Unexpected content type '" + contentType + "'. Response: " + response);
            default:
                throw new Exception("[inventory] " + httpReplyCode + " " + restApiClient.getHttpResponse().getStatusLine().getReasonPhrase());
            }
        }
        return null;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
        if (closed) {
            cleanup();
        }
    }

    public void setXmlCommandExecutor(SchedulerXmlCommandExecutor xmlCommandExecutor) {
        this.xmlCommandExecutor = xmlCommandExecutor;
    }

}