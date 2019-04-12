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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import com.sos.exception.SOSBadRequestException;
import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.exceptions.SOSHibernateInvalidSessionException;
import com.sos.hibernate.exceptions.SOSHibernateObjectOperationException;
import com.sos.jitl.dailyplan.db.Calendar2DB;
import com.sos.jitl.dailyplan.db.DailyPlanCalender2DBFilter;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.inventory.exceptions.SOSInventoryEventProcessingException;
import com.sos.jitl.inventory.helper.AgentHelper;
import com.sos.jitl.inventory.helper.Calendar2DBHelper;
import com.sos.jitl.inventory.helper.HttpHelper;
import com.sos.jitl.inventory.helper.InventoryRuntimeHelper;
import com.sos.jitl.inventory.helper.SaveOrUpdateHelper;
import com.sos.jitl.inventory.model.DocumentationDBLayer;
import com.sos.jitl.inventory.model.InventoryModel;
import com.sos.jitl.reporting.db.DBItemDocumentation;
import com.sos.jitl.reporting.db.DBItemDocumentationUsage;
import com.sos.jitl.reporting.db.DBItemInventoryAgentCluster;
import com.sos.jitl.reporting.db.DBItemInventoryAgentClusterMember;
import com.sos.jitl.reporting.db.DBItemInventoryAgentInstance;
import com.sos.jitl.reporting.db.DBItemInventoryClusterCalendarUsage;
import com.sos.jitl.reporting.db.DBItemInventoryFile;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryJob;
import com.sos.jitl.reporting.db.DBItemInventoryJobChain;
import com.sos.jitl.reporting.db.DBItemInventoryJobChainNode;
import com.sos.jitl.reporting.db.DBItemInventoryLock;
import com.sos.jitl.reporting.db.DBItemInventoryOperatingSystem;
import com.sos.jitl.reporting.db.DBItemInventoryOrder;
import com.sos.jitl.reporting.db.DBItemInventoryProcessClass;
import com.sos.jitl.reporting.db.DBItemInventorySchedule;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.helper.EConfigFileExtensions;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.reporting.helper.ReportXmlHelper;
import com.sos.jitl.reporting.plugin.FactEventHandler.CustomEventType;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;
import com.sos.scheduler.engine.data.events.custom.VariablesCustomEvent;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

import sos.xml.SOSXMLXPath;

public class InventoryEventUpdateUtil {

    @SuppressWarnings("unused")
    private static final String APPLICATION_HEADER_XML_VALUE = "application/xml";
    @SuppressWarnings("unused")
    private static final String WEBSERVICE_PARAM_VALUE_FILEBASED_DETAILED = "FileBasedDetailed";
    private static final String APPLICATION_HEADER_JSON_VALUE = "application/json";
    private static final String WEBSERVICE_PARAM_VALUE_FILEBASED_OVERVIEW = "FileBasedOverview";
    private static final String WEBSERVICE_PARAM_VALUE_FILEBASED_EVENT = "FileBasedEvent";
    private static final String WEBSERVICE_PARAM_VALUE_SCHEDULER_EVENT = "SchedulerEvent";
    private static final String WEBSERVICE_PARAM_VALUE_CALENDAR_EVENT = "VariablesCustomEvent";
    private static final String WEBSERVICE_PARAM_VALUE_CALENDAR_EVENT_KEY = "CalendarUsageUpdated";
    private static final String WEBSERVICE_PARAM_KEY_RETURN = "return";
    private static final String WEBSERVICE_PARAM_KEY_AFTER = "after";
    private static final String WEBSERVICE_PARAM_KEY_TIMEOUT = "timeout";
    private static final String WEBSERVICE_PARAM_VALUE_TIMEOUT = "60s";
    private static final Integer HTTP_CLIENT_SOCKET_TIMEOUT = 75000;
    @SuppressWarnings("unused")
    private static final String WEBSERVICE_COMMAND_URL = "/jobscheduler/master/api/command";
    private static final String WEBSERVICE_FILE_BASED_URL = "/jobscheduler/master/api/fileBased";
    private static final String WEBSERVICE_EVENTS_URL = "/jobscheduler/master/api/event";
    private static final String ACCEPT_HEADER_KEY = "Accept";
    private static final String CONTENT_TYPE_HEADER_KEY = "Content-Type";
    private static final String POST_BODY_JSON_KEY = "path";
    private static final String POST_BODY_JSON_VALUE = "/";
    private static final String EVENT_TYPE = "TYPE";
    private static final String EVENT_TYPE_REMOVED = "FileBasedRemoved";
    private static final String EVENT_STATE_KEY = "state";
    private static final String EVENT_STATE_VALUE_STOPPING = "stopping";
    private static final String EVENT_TYPE_UPDATED = "FileBasedUpdated";
    private static final String CUSTOM_EVENT_TYPE_DAILYPLAN_UPDATED = "InventoryDailyPlanUpdated";
    private static final String EVENT_TYPE_NON_EMPTY = "NonEmpty";
    private static final String EVENT_TYPE_EMPTY = "Empty";
    private static final String EVENT_TYPE_TORN = "Torn";
    private static final String EVENT_KEY = "key";
    private static final String EVENT_ID = "eventId";
    private static final String EVENT_SNAPSHOT = "eventSnapshots";
    private static final String EVENT_SCHEDULER_STATE_CHANGED = "SchedulerStateChanged";
    private static final String JS_OBJECT_TYPE_JOB = "Job";
    private static final String CALENDAR_OBJECT_TYPE_JOB = "JOB";
    private static final String JS_OBJECT_TYPE_JOBCHAIN = "JobChain";
    private static final String JS_OBJECT_TYPE_ORDER = "Order";
    private static final String CALENDAR_OBJECT_TYPE_ORDER = "ORDER";
    private static final String JS_OBJECT_TYPE_PROCESS_CLASS = "ProcessClass";
    private static final String JS_OBJECT_TYPE_SCHEDULE = "Schedule";
    private static final String CALENDAR_OBJECT_TYPE_SCHEDULE = "SCHEDULE";
    private static final String JS_OBJECT_TYPE_LOCK = "Lock";
    private static final String JS_OBJECT_TYPE_FOLDER = "Folder";
    private static final String FILE_TYPE_JOB = "job";
    private static final String FILE_TYPE_JOBCHAIN = "job_chain";
    private static final String FILE_TYPE_ORDER = "order";
    private static final String FILE_TYPE_PROCESS_CLASS = "process_class";
    private static final String FILE_TYPE_AGENT_CLUSTER = "agent_cluster";
    private static final String FILE_TYPE_SCHEDULE = "schedule";
    private static final String FILE_TYPE_LOCK = "lock";
    private static final String DEFAULT_JOB_DOC_PATH = "/sos/jitl-jobs";
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEventUpdateUtil.class);
    private Map<String, List<JsonObject>> groupedEvents = new HashMap<String, List<JsonObject>>();
    private String webserviceUrl = null;
    private SOSHibernateFactory factory = null;
    private DBItemInventoryInstance instance = null;
    private DBLayerInventory dbLayer = null;
    private Path liveDirectory = null;
    private Path cacheDirectory = null;
    private Long eventId = null;
    private String lastEventKey = null;
    private Long lastEventId = 0L;
    private Long newEventId = 0L;
    private List<DbItem> saveOrUpdateItems = new ArrayList<DbItem>();
    private Set<DBItemInventoryJobChainNode> saveOrUpdateNodeItems = new HashSet<DBItemInventoryJobChainNode>();
    private Set<DbItem> deleteItems = new HashSet<DbItem>();
    private Map<String, NodeList> jobChainNodesToSave = new HashMap<String, NodeList>();
    private Map<DBItemInventoryProcessClass, NodeList> remoteSchedulersToSave = 
            new HashMap<DBItemInventoryProcessClass, NodeList>();
    private Map<String, SOSXMLXPath> pcXpaths = new HashMap<String, SOSXMLXPath>();
    private JobSchedulerRestApiClient restApiClient;
    private CloseableHttpClient httpClient;
    private boolean closed = false;
    private String host;
    private Integer port;
    private EventPublisher customEventBus;
    private Map<String, Map<String, String>> eventVariables = new HashMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> dailyPlanEventVariables = new HashMap<String, Map<String, String>>();
    private boolean hasDbErrors = false;
    private Path schedulerXmlPath;
    private SchedulerXmlCommandExecutor xmlCommandExecutor;
    private String schedulerId;
    private String answerXml;
    private Set<DBItemInventoryAgentInstance> agentsToDelete;
    private Set<DBItemInventoryAgentClusterMember> agentClusterMembersToDelete;
    private Map<Long, String> schedulesToCheckForUpdate = new HashMap<Long, String>();
    private Set<DBItemInventoryJob> jobsForDailyPlanUpdate = new HashSet<DBItemInventoryJob>();
    private Set<DBItemInventoryOrder> ordersForDailyPlanUpdate = new HashSet<DBItemInventoryOrder>();
    private Set<DBItemInventorySchedule> schedulesForDailyPlanUpdate = new HashSet<DBItemInventorySchedule>();
    private Boolean isWindows;
    private String hostFromHttpPort;
    private String httpPort;
    private String timezone;
    private Integer recurringExecution = 0;
    
    public InventoryEventUpdateUtil(String host, Integer port, SOSHibernateFactory factory, EventPublisher customEventBus,
            Path schedulerXmlPath, String schedulerId, String httpPort) {
        this.factory = factory;
        this.httpPort = httpPort;
        this.hostFromHttpPort = HttpHelper.getHttpHost(httpPort, "127.0.0.1");
        this.webserviceUrl = "http://" + hostFromHttpPort + ":" + port;
        this.host = host;
        this.port = port;
        this.customEventBus = customEventBus;
        this.schedulerXmlPath = schedulerXmlPath;
        this.schedulerId = schedulerId;
        this.cacheDirectory = this.schedulerXmlPath.getParent().resolve("cache");
        initInstance();
        initRestClient();
    }

    public InventoryEventUpdateUtil(String host, Integer port, SOSHibernateFactory factory, Path schedulerXmlPath, 
            String schedulerId, String answerXml, String httpPort) {
        this.factory = factory;
        this.httpPort = httpPort;
        this.hostFromHttpPort = HttpHelper.getHttpHost(httpPort, "127.0.0.1");
        this.webserviceUrl = "http://" + hostFromHttpPort + ":" + port;
        this.host = host;
        this.port = port;
        this.customEventBus = null;
        this.schedulerXmlPath = schedulerXmlPath;
        this.schedulerId = schedulerId;
        this.answerXml = answerXml;
        this.cacheDirectory = this.schedulerXmlPath.getParent().resolve("cache");
        initInstance();
        initRestClient();
    }

    public void execute() throws Exception {
        LOGGER.debug("[inventory] Processing of FileBasedEvents started!");
        while (eventId == null) {
            eventId = initOverviewRequest();
        }
        lastEventId = eventId;
        while (!closed) {
            try {
                if (hasDbErrors) {
                    processAgain();
                }
                execute(lastEventId, lastEventKey);
                lastEventId = newEventId;
            } catch (SOSHibernateInvalidSessionException e) {
                hasDbErrors = true;
                saveOrUpdateItems.clear();
                saveOrUpdateNodeItems.clear();
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
        if (!closed) {
            LOGGER.debug("[inventory] -- Processing FileBasedEvents --");
            JsonObject result = getFileBasedEvents(eventId);
            String type = result.getString(EVENT_TYPE);
            newEventId = result.getJsonNumber(EVENT_ID).longValue();
            JsonArray events = result.getJsonArray(EVENT_SNAPSHOT);
            if (events != null && !events.isEmpty()) {
                processEventType(type, events, lastKey);
            } else if (EVENT_TYPE_EMPTY.equalsIgnoreCase(type)) {
                lastEventKey = lastKey;
            }
        }
    }

    private void initInstance() {
        SOSHibernateSession dbConnection = null;
        try {
            dbConnection = factory.openStatelessSession("inventory");
            dbLayer = new DBLayerInventory(dbConnection);
            instance = dbLayer.getInventoryInstance(schedulerId, host, port);
            if (instance != null) {
                liveDirectory = Paths.get(instance.getLiveDirectory());
                DBItemInventoryOperatingSystem os = dbLayer.getInventoryOpSysById(instance.getOsId());
                if(os != null) {
                    if (os.getName().equalsIgnoreCase("windows")) {
                        isWindows = os.getName().equalsIgnoreCase("windows");
                    } else {
                        isWindows = false;
                    }
                }
            }
            timezone = instance.getTimeZone();
        } catch (SOSHibernateInvalidSessionException e) {
            LOGGER.error(String.format(
                    "[inventory] session error occured while receiving inventory instance from db with host: %1$s and "
                    + "port: %2$d; error: %3$s", host, port, e.getMessage()), e);
            hasDbErrors = true;
        } catch (Exception e) {
            LOGGER.error(String.format("[inventory] error occured receiving inventory instance from db with host: %1$s and "
                    + "port: %2$d; error: %3$s", host, port, e.getMessage()), e);
        } finally {
            if (dbConnection != null) {
                dbConnection.close();
            }
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
    
    private void processAgain() throws Exception {
        if (!closed) {
            try {
                if (recurringExecution < 3) {
                    recurringExecution++;
                    execute(lastEventId, lastEventKey);
                } else {
                    recurringExecution = 0;
                    if (newEventId != 0L && newEventId != lastEventId) {
                        lastEventId = newEventId;
                    }
                    LOGGER.debug("[inventory] tried to process events three times unsuccessfully, complete configuration update started instead!");
                    InventoryModel modelProcessing = new InventoryModel(factory, instance, schedulerXmlPath);
                    modelProcessing.setAnswerXml(answerXml);
                    modelProcessing.setXmlCommandExecutor(xmlCommandExecutor);
                    modelProcessing.process();
                    LOGGER.debug("[inventory] complete configuration update finished!");
                }
                hasDbErrors = false;
            } catch (SOSHibernateInvalidSessionException e) {
                hasDbErrors = true;
                throw e;
            } catch (Exception e) {
                throw new SOSInventoryEventProcessingException(e);
            }
        }
    }

    public void restartExecution() {
        if (!closed) {
            cleanup();
            if (httpClient == null) {
                initRestClient();
            } else {
                if (restApiClient != null) {
                    restApiClient.closeHttpClient();
                } else {
                    try {
                        httpClient.close();
                    } catch (IOException e) {}
                }
                initRestClient();
            }
        }
    }

    private void cleanup() {
        eventId = null;
        if (groupedEvents != null) {
            groupedEvents.clear();
        }
        if (saveOrUpdateItems != null) {
            saveOrUpdateItems.clear();
        }
        if (saveOrUpdateNodeItems != null) {
            saveOrUpdateNodeItems.clear();
        }
        if (deleteItems != null) {
            deleteItems.clear();
        }
        if (agentsToDelete != null) {
            agentsToDelete.clear();
        }
        if (agentClusterMembersToDelete != null) {
            agentClusterMembersToDelete.clear();
        }
        if (eventVariables != null) {
            eventVariables.clear();
        }
        if (dailyPlanEventVariables != null) {
            dailyPlanEventVariables.clear();
        }
        if (remoteSchedulersToSave != null) {
            remoteSchedulersToSave.clear();
        }
        if (jobChainNodesToSave != null) {
            jobChainNodesToSave.clear();
        }
        if (schedulesToCheckForUpdate != null) {
            schedulesToCheckForUpdate.clear();
        }
        if (jobsForDailyPlanUpdate != null) {
            jobsForDailyPlanUpdate.clear();
        }
        if (ordersForDailyPlanUpdate != null) {
            ordersForDailyPlanUpdate.clear();
        }
        if (schedulesForDailyPlanUpdate != null) {
            schedulesForDailyPlanUpdate.clear();
        }
        SaveOrUpdateHelper.clearExisitingItems();
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
    }

    private void groupEvents(JsonArray events, String lastKey) {
        String state = null;
        for (int i = 0; i < events.size(); i++) {
            if(((JsonObject) events.getJsonObject(i)).getString(EVENT_TYPE).equals(EVENT_SCHEDULER_STATE_CHANGED)) {
                state = ((JsonObject) events.getJsonObject(i)).getString(EVENT_STATE_KEY);
                if(state.equals(EVENT_STATE_VALUE_STOPPING)) {
                    closed = true;
                    break;
                }
            } else {
                continue;
            }
        }
        if (state == null || (state != null && !EVENT_STATE_VALUE_STOPPING.equalsIgnoreCase(state))) {
            for (JsonObject event : events.getValuesAs(JsonObject.class)) {
                List<JsonObject> pathEvents = new ArrayList<JsonObject>();
                String key = event.getString(EVENT_KEY, null);
                String type = event.getString(EVENT_TYPE, null);
                if (key == null || type == null) {
                    continue;
                }
                if (lastKey == null) {
                    lastKey = key;
                } else if (!lastKey.equals(key)) {
                    pathEvents.clear();
                    lastKey = key;
                }
                if ((WEBSERVICE_PARAM_VALUE_CALENDAR_EVENT.equals(type) && WEBSERVICE_PARAM_VALUE_CALENDAR_EVENT_KEY.equals(key)) 
                        || (type.startsWith("FileBased") &&  key.contains(":"))) {
                    pathEvents.add(event);
                    if (groupedEvents.containsKey(lastKey)) {
                        addToExistingGroup(lastKey, pathEvents);
                    } else {
                        groupedEvents.put(lastKey, pathEvents);
                    }
                } else {
                    continue;
                }
            }
        }
        lastEventKey = lastKey;
    }

    private void processGroupedEvents(Map<String, List<JsonObject>> events) throws Exception {
        sortGroupedEvents(events);
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
    
    private void sortGroupedEvents(Map<String, List<JsonObject>> events) {
        Comparator<JsonObject> eventGroupComparator = new Comparator<JsonObject>() {
            @Override
            public int compare(JsonObject o1, JsonObject o2) {
                if(((JsonObject)o1).getString(EVENT_TYPE).equals(EVENT_TYPE_REMOVED)) {
                    return -1;
                }
                return 0;
            }
        };
        for (List<JsonObject> list : events.values()) {
            Collections.sort(list, eventGroupComparator);
        }
        Comparator<String> eventGroupKeyComparator = new Comparator<String>() {
            
            @Override
            public int compare(String o1, String o2) {
                List<JsonObject> listO1 = groupedEvents.get(o1);
                for (JsonObject o1Object : listO1) {
                    if (o1Object.getString(EVENT_TYPE).equals(EVENT_TYPE_REMOVED)) {
                        return -1;
                    }
                }
                return 0;
            }
        };
        Map<String, List<JsonObject>> sortedEvents = new TreeMap<String, List<JsonObject>>(eventGroupKeyComparator);
        for (String key : events.keySet()) {
            sortedEvents.put(key, events.get(key));
        }
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

    private void processDbTransaction() throws SOSHibernateException, SOSInventoryEventProcessingException {
        if (!closed) {
            Map<DBItemInventoryJobChain, List<DBItemInventoryJob>> processedJobChains =
                    new HashMap<DBItemInventoryJobChain, List<DBItemInventoryJob>>();
            SOSHibernateSession dbConnection = null;
            try {
                dbConnection = factory.openStatelessSession("inventory");
                dbLayer = new DBLayerInventory(dbConnection);
                Set<DbItem> processedItems = new HashSet<>();
                LOGGER.debug("[inventory] processing of DB transactions started");
                dbLayer.getSession().beginTransaction();
                SaveOrUpdateHelper.clearExisitingItems();
                SaveOrUpdateHelper.initExistingItems(dbLayer, instance);
                Long fileId = null;
                String filePath = null;
                for (DbItem item : saveOrUpdateItems) {
                    try {
                        if (item instanceof DBItemInventoryFile) {
                            Long id = SaveOrUpdateHelper.saveOrUpdateItem(dbLayer, item);
                            LOGGER.debug("processed file got id from autoincrement: " + id.toString());
                            fileId = id;
                            filePath = ((DBItemInventoryFile) item).getFileName();
                            LOGGER.debug(String.format("[inventory] file %1$s saved or updated", filePath));
                        } else {
                            if (item instanceof DBItemInventoryJob) {
                                jobsForDailyPlanUpdate.add((DBItemInventoryJob)item);
                            } else if (item instanceof DBItemInventoryOrder) {
                                ordersForDailyPlanUpdate.add((DBItemInventoryOrder)item);
                            } else if (item instanceof DBItemInventorySchedule) {
                                schedulesForDailyPlanUpdate.add((DBItemInventorySchedule)item); 
                            }
                            if (filePath != null && fileId != null) {
                                String name = getName(item);
                                if (name != null && !name.isEmpty() && filePath.contains(name)) {
                                    setFileId(item, fileId);
                                }
                                LOGGER.debug("save or update item: " + getName(item) + " !");
                                Long id = SaveOrUpdateHelper.saveOrUpdateItem(dbLayer, item);
                                processedItems.add(item);
                                LOGGER.debug("processed JobSchedulerObject got id from autoincrement: " + id.toString());
                                LOGGER.debug(String.format("[inventory] item %1$s saved or updated", name));
                                if (item instanceof DBItemInventoryJobChain) {
                                    if (processedJobChains.keySet().contains((DBItemInventoryJobChain) item)) {
                                        processedJobChains.get((DBItemInventoryJobChain) item).addAll(
                                                dbLayer.getAllJobsFromJobChain(((DBItemInventoryJobChain) item).getInstanceId(),
                                                        ((DBItemInventoryJobChain) item).getId()));
                                    } else {
                                        processedJobChains.put((DBItemInventoryJobChain) item,
                                                dbLayer.getAllJobsFromJobChain(((DBItemInventoryJobChain) item).getInstanceId(), 
                                                        ((DBItemInventoryJobChain) item).getId()));
                                    }
                                    NodeList nl = jobChainNodesToSave.get(getName(item));
                                    dbLayer.deleteOldNodes((DBItemInventoryJobChain) item);
                                    SaveOrUpdateHelper.clearExistingJobChainNodes();
                                    SaveOrUpdateHelper.initExisitingJobChainNodes(dbLayer, instance);
                                    createJobChainNodes(nl, (DBItemInventoryJobChain) item);
                                } else if (item instanceof DBItemInventoryProcessClass) {
                                    NodeList nl = remoteSchedulersToSave.get(item);
                                    saveAgentClusters(dbConnection, (DBItemInventoryProcessClass) item, nl);
                                } else if (item instanceof DBItemInventorySchedule) {
                                    Long scheduleId = id;
                                    String scheduleName = ((DBItemInventorySchedule)item).getName();
                                    schedulesToCheckForUpdate.put(scheduleId, scheduleName);
                                }
                                fileId = null;
                                filePath = null;
                            } else {
                                Long id = SaveOrUpdateHelper.saveOrUpdateItem(dbLayer, item);
                                LOGGER.debug("save or update item: " + getName(item) + " !");
                                LOGGER.debug(String.format("[inventory] item %1$s saved or updated", getName(item)));
                                if (item instanceof DBItemInventoryJobChain) {
                                    if (processedJobChains.keySet().contains((DBItemInventoryJobChain) item)) {
                                        processedJobChains.get((DBItemInventoryJobChain) item).addAll(
                                                dbLayer.getAllJobsFromJobChain(((DBItemInventoryJobChain) item).getInstanceId(),
                                                        ((DBItemInventoryJobChain) item).getId()));
                                    } else {
                                        processedJobChains.put((DBItemInventoryJobChain) item,
                                                dbLayer.getAllJobsFromJobChain(((DBItemInventoryJobChain) item).getInstanceId(),
                                                        ((DBItemInventoryJobChain) item).getId()));
                                    }
                                    NodeList nl = jobChainNodesToSave.get(getName(item));
                                    dbLayer.deleteOldNodes((DBItemInventoryJobChain) item);
                                    SaveOrUpdateHelper.clearExistingJobChainNodes();
                                    SaveOrUpdateHelper.initExisitingJobChainNodes(dbLayer, instance);
                                    createJobChainNodes(nl, (DBItemInventoryJobChain) item);
                                } else if (item instanceof DBItemInventoryProcessClass) {
                                    NodeList nl = remoteSchedulersToSave.get(item);
                                    saveAgentClusters(dbConnection, (DBItemInventoryProcessClass) item, nl);
                                } else if (item instanceof DBItemInventorySchedule) {
                                    Long scheduleId = id;
                                    String scheduleName = ((DBItemInventorySchedule)item).getName();
                                    schedulesToCheckForUpdate.put(scheduleId, scheduleName);
                                }
                            }
                        }
                    } catch (ConstraintViolationException e) {
                        LOGGER.debug(e.toString(), e);
                        continue;
                    } catch (SOSHibernateObjectOperationException e) {
                        LOGGER.debug(e.toString(), e);
                        continue;
                    }
                }
                if (saveOrUpdateNodeItems != null) {
                    for (DBItemInventoryJobChainNode node : saveOrUpdateNodeItems) {
                        SaveOrUpdateHelper.saveOrUpdateItem(dbLayer, node);
                        LOGGER.debug(String.format("[inventory] job chain nodes for item %1$s saved or updated",
                                node.getName()));
                    }
                }
                if (deleteItems != null) {
                    for (DbItem item : deleteItems) {
                        if (item instanceof DBItemInventoryJob) {
                            jobsForDailyPlanUpdate.add((DBItemInventoryJob)item);
                        } else if (item instanceof DBItemInventoryOrder) {
                            ordersForDailyPlanUpdate.add((DBItemInventoryOrder)item);
                        } else if (item instanceof DBItemInventorySchedule) {
                            schedulesForDailyPlanUpdate.add((DBItemInventorySchedule)item); 
                        }
                        dbLayer.getSession().delete(item);
                        if (getName(item) != null) {
                            LOGGER.debug("delete item from DB: " + getName(item) + " !");
                            LOGGER.debug(String.format("[inventory] item %1$s deleted", getName(item)));
                        }
                    }
                    deleteItems.clear();
                }
                if (processedJobChains != null && !processedJobChains.isEmpty()) {
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
                }
                if (processedItems != null && !processedItems.isEmpty()) {
                    for (DbItem processedItem : processedItems) {
                        if (processedItem instanceof DBItemInventoryJob) {
                            // check JobChainNodes if an entry for this jobname exists
                            DBItemInventoryJob job = (DBItemInventoryJob)processedItem; 
                            if (job.getIsOrderJob()) {
                                List<DBItemInventoryJobChainNode> jobChainNodes = dbLayer.getJobsJobChainNodes(job.getName(), job.getInstanceId());
                                if (jobChainNodes != null && !jobChainNodes.isEmpty()) {
                                    // update the Job usedInJobChains column
                                    job.setUsedInJobChains(jobChainNodes.size());
                                    dbLayer.getSession().update(job);
                                    for (DBItemInventoryJobChainNode node : jobChainNodes) {
                                        // update the JobChainNode with the correct JobId
                                        node.setJobId(job.getId());
                                        dbLayer.getSession().update(node);
                                    }
                                }
                            }
                        } else if (processedItem instanceof DBItemInventoryProcessClass) {
                            DBItemInventoryProcessClass pc = (DBItemInventoryProcessClass) processedItem;
                            if (pc.getHasAgents()) {
                                List<DBItemInventoryJob> jobs = dbLayer.getJobsForProcessClass(pc.getName(), pc.getInstanceId());
                                if (jobs != null && !jobs.isEmpty()) {
                                    for (DBItemInventoryJob job : jobs) {
                                        job.setProcessClassId(pc.getId());
                                        dbLayer.getSession().update(job);
                                    }
                                }
                            }
                        }
                    }
                }
                dbLayer.getSession().commit();
                dbLayer.getSession().beginTransaction();
                if (agentsToDelete != null) {
                    for (DBItemInventoryAgentInstance agent : agentsToDelete) {
                        dbLayer.getSession().delete(agent);
                        if (agent.getUrl() != null) {
                            LOGGER.debug(String.format("[inventory] agent with URL %1$s deleted", agent.getUrl()));
                        }
                    }
                    agentsToDelete.clear();
                }
                if (agentClusterMembersToDelete != null) {
                    for (DBItemInventoryAgentClusterMember member : agentClusterMembersToDelete) {
                        dbLayer.getSession().delete(member);
                        if (member.getUrl() != null) {
                            LOGGER.debug(String.format("[inventory] agentCluster member with URL %1$s deleted",
                                    member.getUrl()));
                        }
                    }
                    agentClusterMembersToDelete.clear();
                }
                if (schedulesToCheckForUpdate != null) {
                    for(Long scheduleId : schedulesToCheckForUpdate.keySet()) {
                        SaveOrUpdateHelper.updateScheduleIdForOrders(dbLayer, instance.getId(), scheduleId, 
                                schedulesToCheckForUpdate.get(scheduleId));
                        SaveOrUpdateHelper.updateScheduleIdForJobs(dbLayer, instance.getId(), scheduleId,
                                schedulesToCheckForUpdate.get(scheduleId));
                    }
                    schedulesToCheckForUpdate.clear();
                }
                dbLayer.getSession().commit();
                if (customEventBus != null && !hasDbErrors) {
                    for (String key : eventVariables.keySet()) {
                        customEventBus.publishCustomEvent(VariablesCustomEvent.keyed(key, eventVariables.get(key)));
                        LOGGER.info(String.format("[inventory] Custom Event - inventory updated - published on object %1$s!", key));
                    }
                    eventVariables.clear();
                } else {
                    LOGGER.debug("[inventory] Custom Events not published due to errors or EventBus is NULL!");
                }
                LOGGER.debug("[inventory] processing of DB transactions finished");
                try {
                    updateDailyPlan();
                    if (customEventBus != null && !hasDbErrors) {
                        for (String key : dailyPlanEventVariables.keySet()) {
                            customEventBus.publishCustomEvent(VariablesCustomEvent.keyed(key, dailyPlanEventVariables.get(key)));
                            LOGGER.info(String.format("[inventory] Custom Event - Daily Plan updated - published on object %1$s!", key));
                        }
                        dailyPlanEventVariables.clear();
                    } else {
                        LOGGER.debug("[inventory] Custom Events not published due to errors or EventBus is NULL!");
                    }
                    LOGGER.debug("[inventory] processing of DailyPlan creating DB transactions finished");
                } catch (Exception e) {
                    LOGGER.warn("[inventory] Error occurred updating Daily Plan: ", e);
                }
            } catch (SOSHibernateInvalidSessionException e) {
                hasDbErrors = true;
                processedJobChains.clear();
                if (!closed) {
                    LOGGER.error("[inventory] processing of DB transactions not finished due to errors, processing rollback: "
                            + e.toString(), e);
                    throw e;
                }
            } catch (Exception e) {
                processedJobChains.clear();
                if (!closed) {
                    LOGGER.warn("[inventory] processing of DB transactions not finished due to errors: "
                            + e.toString(), e);
                    throw new SOSInventoryEventProcessingException(e);
                }
            } finally {
                if (dbConnection != null) {
                    dbConnection.close();
                }
            }
        }
    }
    
    private void updateDailyPlan() throws Exception {
        Map<String, String> values = new HashMap<String, String>();
        boolean hasItemsToUpdate = false;
        Calendar2DB calendar2Db = Calendar2DBHelper.initCalendar2Db(dbLayer, instance, hostFromHttpPort, port);
        if(!jobsForDailyPlanUpdate.isEmpty()) {
            hasItemsToUpdate = true;
            for(DBItemInventoryJob job : jobsForDailyPlanUpdate) {
                DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter = new DailyPlanCalender2DBFilter();
                dailyPlanCalender2DBFilter.setForJob(job.getName());
                calendar2Db.addDailyplan2DBFilter(dailyPlanCalender2DBFilter, instance.getId()); 
            }
        }
        jobsForDailyPlanUpdate.clear();
        if (!ordersForDailyPlanUpdate.isEmpty()) {
            hasItemsToUpdate = true;
            for (DBItemInventoryOrder order : ordersForDailyPlanUpdate) {
                DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter = new DailyPlanCalender2DBFilter();
                dailyPlanCalender2DBFilter.setForJobChain(order.getJobChainName());
                dailyPlanCalender2DBFilter.setForOrderId(order.getOrderId());
                calendar2Db.addDailyplan2DBFilter(dailyPlanCalender2DBFilter, instance.getId());                
            }
        }
        ordersForDailyPlanUpdate.clear();
        if (!schedulesForDailyPlanUpdate.isEmpty()) {
            hasItemsToUpdate = true;
            for (DBItemInventorySchedule schedule : schedulesForDailyPlanUpdate) {
                DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter = new DailyPlanCalender2DBFilter();
                dailyPlanCalender2DBFilter.setForSchedule(schedule.getName());
                calendar2Db.addDailyplan2DBFilter(dailyPlanCalender2DBFilter, instance.getId());                
            }
        }
        schedulesForDailyPlanUpdate.clear();
        if (hasItemsToUpdate) {
            calendar2Db.processDailyplan2DBFilter();
            values.put(CustomEventType.DailyPlanChanged.name(), CUSTOM_EVENT_TYPE_DAILYPLAN_UPDATED);
            dailyPlanEventVariables.put("DailyPlan", values);
        }
    }

    private void processEventType(String type, JsonArray events, String lastKey) throws Exception {
        if (!closed) {
            switch (type) {
            case EVENT_TYPE_NON_EMPTY:
                groupEvents(events, lastKey);
                for (String key : groupedEvents.keySet()) {
                    LOGGER.debug(key);
                }
                processGroupedEvents(groupedEvents);
                break;
            case EVENT_TYPE_TORN:
                restartExecution();
                break;
            }
        }
    }

    private Long processEvent(JsonObject event) throws SOSHibernateException, SOSInventoryEventProcessingException, Exception {
        String key = null;
        SOSHibernateSession dbConnection = null;
        try {
            if (!closed && event != null) {
                dbConnection = factory.openStatelessSession("inventory");
                dbLayer = new DBLayerInventory(dbConnection);
                key = event.getString(EVENT_KEY);
                eventId = event.getJsonNumber(EVENT_ID).longValue();
                String objectType = null;
                String path = null;
                if (key.equals(WEBSERVICE_PARAM_VALUE_CALENDAR_EVENT_KEY)) {
                    objectType = event.getJsonObject("variables").getString("objectType");
                    path = event.getJsonObject("variables").getString("path");
                    key = objectType + ":" + path;
                } else if (key.contains(":")) {
                    String[] keySplit = key.split(":");
                    objectType = keySplit[0];
                    if (keySplit.length > 1) {
                        path = keySplit[1];
                    }
                } else {
                   return eventId; 
                }
                if (path != null && !path.isEmpty()) {
                    switch (objectType) {
                    case JS_OBJECT_TYPE_JOB:
                    case CALENDAR_OBJECT_TYPE_JOB:
                        processJobEvent(path, event, key);
                        break;
                    case JS_OBJECT_TYPE_JOBCHAIN:
                        processJobChainEvent(path, event, key);
                        break;
                    case JS_OBJECT_TYPE_ORDER:
                    case CALENDAR_OBJECT_TYPE_ORDER:
                        processOrderEvent(path, event, key);
                        break;
                    case JS_OBJECT_TYPE_PROCESS_CLASS:
                        processProcessClassEvent(path, event, key);
                        break;
                    case JS_OBJECT_TYPE_SCHEDULE:
                    case CALENDAR_OBJECT_TYPE_SCHEDULE:
                        processScheduleEvent(path, event, key);
                        break;
                    case JS_OBJECT_TYPE_LOCK:
                        processLockEvent(path, event, key);
                        break;
                    case JS_OBJECT_TYPE_FOLDER:
                        break;
                    }
                }
            }
            return eventId;
        } catch (SOSHibernateInvalidSessionException e) {
            hasDbErrors = true;
            if (!closed) {
                throw e;
            }
            return null;
        } catch (Exception e) {
            if (!closed) {
                LOGGER.error(String.format("[inventory] error occured processing event on %1$s", key), e);
                throw new SOSInventoryEventProcessingException(e);
            }
            return null;
        } finally {
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }

    private Path fileExists(String path) {
        if (!closed) {
            String normalizePath = path.replaceFirst("^/+", "");
            Path p = liveDirectory.resolve(normalizePath);
            if (!Files.exists(p)) {
                p = cacheDirectory.resolve(normalizePath);
                if (!Files.exists(p)) {
                    p = null;
                }
            }
            return p;
        } else {
            return null;
        }
    }

    private DBItemInventoryFile createNewInventoryFile(Long instanceId, Path filePath, String name, String type) {
        if (!closed) {
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
                    LOGGER.warn(String.format("[inventory] cannot read file attributes. file = %1$s, exception = %2$s:%3$s",
                            filePath.toString(), e.getClass().getSimpleName(), e.getMessage()), e);
                } catch (Exception e) {
                    LOGGER.warn("[inventory] cannot convert files create and modified timestamps! " + e.getMessage(), e);
                }
            }
            return dbFile;
        } else {
            return null;
        }
    }

    private void processJobEvent(String path, JsonObject event, String key) throws Exception {
        if (!closed) {
            Map<String, String> values = new HashMap<String, String>();
            try {
                Date now = Date.from(Instant.now());
                LOGGER.debug(String.format("[inventory] processing event on JOB: %1$s with path: %2$s", 
                        Paths.get(path).getFileName(), Paths.get(path).getParent()));
                Path filePath = fileExists(path + EConfigFileExtensions.JOB.extension());
                if (filePath != null) {
                    LOGGER.debug("filePath: " + filePath.toString());
                } else {
                    LOGGER.debug("filePath: null");
                }
                Long instanceId = null;
                if (instance != null) {
                    instanceId = instance.getId();
                    DBItemInventoryJob job = null;
                    if (isWindows) {
                        job = dbLayer.getInventoryJobCaseInsensitive(instanceId, path);
                        LOGGER.debug("OS is Windows");
                    } else {
                        job = dbLayer.getInventoryJob(instanceId, path);
                        LOGGER.debug("OS is Linux");
                    }
                    DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId, path 
                            + EConfigFileExtensions.JOB.extension());
                    boolean fileExists = filePath != null;
                    LOGGER.debug("file exists: " + fileExists);
                    if (fileExists) {
                        LOGGER.debug("file found: going to add/update");
                        if (file == null) {
                            file = createNewInventoryFile(instanceId, filePath, path + EConfigFileExtensions.JOB.extension(),
                                    FILE_TYPE_JOB);
                            file.setCreated(now);
                        } else {
                            try {
                                BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                                file.setFileName((path + EConfigFileExtensions.JOB.extension()).replace('\\', '/'));
                                Path updatedPath = Paths.get(path);
                                String fileDirectory = updatedPath.getParent().toString().replace('\\', '/');
                                file.setFileDirectory(fileDirectory);
                                file.setModified(now);
                                file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                                file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                            } catch (IOException e) {
                                LOGGER.warn(String.format(
                                        "[inventory] cannot read file attributes. file = %1$s, exception = %2$s:%3$s",
                                        filePath.toString(), e.getClass().getSimpleName(), e.getMessage()), e);
                            } catch (Exception e) {
                                LOGGER.warn("[inventory] cannot convert files create and modified timestamps! "
                                        + e.getMessage(), e);
                            }
                        }
                        if (job == null) {
                            LOGGER.debug("job not found in DB: create new entry");
                            job = new DBItemInventoryJob();
                            job.setCreated(now);
                            job.setInstanceId(instanceId);
                            job.setFileId(file.getId());
                        } else {
                            LOGGER.debug("job found in DB: updating entry");
                        }
                        job.setName(path);
                        job.setBaseName(Paths.get(path).getFileName().toString());
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
                            if (processClass != null) {
                                Path jobPath = Paths.get(job.getName());
                                String resolvedProcessClassPath = 
                                        jobPath.getParent().resolve(processClass).normalize().toString().replace('\\', '/');
                                DBItemInventoryProcessClass ipc = 
                                        dbLayer.getProcessClassIfExists(instanceId, resolvedProcessClassPath);
                                if (ipc != null) {
                                    job.setProcessClass(processClass);
                                    job.setProcessClassName(ipc.getName());
                                    job.setProcessClassId(ipc.getId());
                                } else {
                                    job.setProcessClass(processClass);
                                    job.setProcessClassName(resolvedProcessClassPath);
                                    job.setProcessClassId(DBLayer.DEFAULT_ID);
                                }
                            } else {
                                job.setProcessClass(null);
                                job.setProcessClassId(DBLayer.DEFAULT_ID);
                                job.setProcessClassName(DBLayer.DEFAULT_NAME);
                            }
                        } else {
                            job.setProcessClass(null);
                            job.setProcessClassId(DBLayer.DEFAULT_ID);
                            job.setProcessClassName(DBLayer.DEFAULT_NAME);
                        }
                        String schedule = ReportXmlHelper.getScheduleFromRuntime(xPath);
                        if (schedule != null && !schedule.isEmpty()) {
                            String scheduleName = 
                                    Paths.get(path).getParent().resolve(schedule).normalize().toString().replace("\\", "/");
                            DBItemInventorySchedule is = dbLayer.getScheduleIfExists(instanceId, scheduleName);
                            if (is != null) {
                                job.setSchedule(schedule);
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
                        if (maxTasks != null && !maxTasks.isEmpty()) {
                            job.setMaxTasks(Integer.parseInt(maxTasks));
                        } else {
                            job.setMaxTasks(1);
                        }
                        Boolean hasDescription = ReportXmlHelper.hasDescription(xPath);
                        if (hasDescription != null) {
                            job.setHasDescription(ReportXmlHelper.hasDescription(xPath));
                        }
                        // determine if the Job is a YADE Job
                        Node scriptNode = xPath.selectSingleNode("/job/script");
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
                                job.setScriptLanguage(((Element)scriptNode).getAttribute("language"));
                            }
                        }
                        job.setIsYadeJob(isYadeJob);
                        job.setModified(now);
                        file.setModified(now);
                        if ((schedule == null || schedule.isEmpty())) {
                            updateRuntimeAndCalendarUsage("JOB", job, xPath);
                        }
                        saveOrUpdateItems.add(file);
                        saveOrUpdateItems.add(job);
                        String docuPath = xPath.selectSingleNodeValue("description/include/@file");
                        if (docuPath != null && (docuPath.startsWith("jobs/") || docuPath.startsWith("./jobs/"))) {
                            docuPath = docuPath.replaceFirst("jobs", DEFAULT_JOB_DOC_PATH);
                            createOrUpdateDocumentationUsage(dbLayer.getSession(), instance.getSchedulerId(), docuPath, job.getId(), job.getName(), "JOB");
                        }
                        values.put("InventoryEventUpdateFinished", EVENT_TYPE_UPDATED);
                    } else if (!fileExists && job != null) {
                        deleteCalendarUsages(job);
                        deleteItems.add(job);
                        if (file != null) {
                            deleteItems.add(file);
                        }
                        values.put("InventoryEventUpdateFinished", EVENT_TYPE_REMOVED);
                    }
                    eventVariables.put(key, values);
                }
            } catch (SOSHibernateInvalidSessionException e) {
                hasDbErrors = true;
                throw e;
            } catch (SAXParseException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void processJobChainEvent(String path, JsonObject event, String key) throws Exception {
        if (!closed) {
            Map<String, String> values = new HashMap<String, String>();
            try {
                Date now = Date.from(Instant.now());
                Path filePath = fileExists(path + EConfigFileExtensions.JOB_CHAIN.extension());
                if (filePath != null) {
                    LOGGER.debug("filePath: " + filePath.toString());
                } else {
                    LOGGER.debug("filePath: null");
                }
                Long instanceId = null;
                if (instance != null) {
                    instanceId = instance.getId();
                    LOGGER.debug(String.format("[inventory] processing event on JOBCHAIN: %1$s with path: %2$s",
                            Paths.get(path).getFileName(), Paths.get(path).getParent()));
                    DBItemInventoryJobChain jobChain = null;
                    if (isWindows) {
                        jobChain = dbLayer.getInventoryJobChainCaseInsensitive(instanceId, path);
                        LOGGER.debug("OS is Windows");
                    } else {
                        jobChain = dbLayer.getInventoryJobChain(instanceId, path);
                        LOGGER.debug("OS is Linux");
                    }
                    DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId, path
                            + EConfigFileExtensions.JOB_CHAIN.extension());
                    boolean fileExists = filePath != null;
                    LOGGER.debug("file exists: " + fileExists);
                    if (fileExists) {
                        LOGGER.debug("file found: going to add/update");
                        if (file == null) {
                            file = createNewInventoryFile(instanceId, filePath, path
                                            + EConfigFileExtensions.JOB_CHAIN.extension(), FILE_TYPE_JOBCHAIN);
                            file.setCreated(now);
                        } else {
                            try {
                                BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                                file.setFileName((path + EConfigFileExtensions.JOB_CHAIN.extension()).replace('\\', '/'));
                                Path updatedPath = Paths.get(path);
                                String fileDirectory = updatedPath.getParent().toString().replace('\\', '/');
                                file.setFileDirectory(fileDirectory);
                                file.setModified(now);
                                file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                                file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                            } catch (IOException e) {
                                LOGGER.warn(String.format(
                                        "[inventory] cannot read file attributes. file = %1$s, exception = %2$s:%3$s",
                                        filePath.toString(), e.getClass().getSimpleName(), e.getMessage()), e);
                            } catch (Exception e) {
                                LOGGER.warn("[inventory] cannot convert files create and modified timestamps! "
                                        + e.getMessage(), e);
                            }
                        }
                        if (jobChain == null) {
                            LOGGER.debug("jobChain not found in DB: create new entry");
                            jobChain = new DBItemInventoryJobChain();
                            jobChain.setInstanceId(instanceId);
                            jobChain.setCreated(now);
                        } else {
                            LOGGER.debug("jobChain found in DB: updating entry");
                        }
                        jobChain.setName(path);
                        jobChain.setBaseName(Paths.get(path).getFileName().toString());
                        SOSXMLXPath xpath = new SOSXMLXPath(filePath.toString());
                        if (xpath.getRoot() == null) {
                            throw new SOSInventoryEventProcessingException("xpath document element missing");
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
                            String resolvedProcessClassPath =
                                    jobChainPath.getParent().resolve(processClass).normalize().toString().replace('\\', '/');
                            DBItemInventoryProcessClass ipc =
                                    dbLayer.getProcessClassIfExists(instanceId, resolvedProcessClassPath);
                            if (ipc != null) {
                                jobChain.setProcessClass(processClass);
                                jobChain.setProcessClassName(ipc.getName());
                                jobChain.setProcessClassId(ipc.getId());
                            } else {
                                jobChain.setProcessClass(processClass);
                                jobChain.setProcessClassName(resolvedProcessClassPath);
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
                            String resolvedFwProcessClassPath =
                                    jobChainPath.getParent().resolve(fwProcessClass).normalize().toString().replace('\\', '/');
                            DBItemInventoryProcessClass ipc =
                                    dbLayer.getProcessClassIfExists(instanceId, resolvedFwProcessClassPath);
                            if (ipc != null) {
                                jobChain.setFileWatchingProcessClass(fwProcessClass);
                                jobChain.setFileWatchingProcessClassName(ipc.getName());
                                jobChain.setFileWatchingProcessClassId(ipc.getId());
                            } else {
                                jobChain.setFileWatchingProcessClass(fwProcessClass);
                                jobChain.setFileWatchingProcessClassName(resolvedFwProcessClassPath);
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
                        List<DBItemInventoryJobChainNode> nodes = dbLayer.getJobChainNodes(instanceId, jobChain.getId());
                        if (nodes != null && !nodes.isEmpty()) {
                            deleteItems.addAll(nodes);
                        }
                        deleteItems.add(jobChain);
                        if (file != null) {
                            deleteItems.add(file);
                        }
                        values.put("InventoryEventUpdateFinished", EVENT_TYPE_REMOVED);
                    }
                    eventVariables.put(key, values);
                }
            } catch (SOSHibernateInvalidSessionException e) {
                hasDbErrors = true;
                throw e;
            } catch (SAXParseException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void createJobChainNodes(NodeList nl, DBItemInventoryJobChain jobChain) throws Exception {
        if (!closed) {
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
                        if (job != null && !job.isEmpty()) {
                            Path jobPath = Paths.get(jobChain.getName()).getParent().resolve(job).normalize();
                            jobName = jobPath.toString().replace("\\", "/");
                            if (jobName != null && !jobName.isEmpty()) {
                                node.setJobName(jobName);
                            } else {
                                node.setJobName(DBLayer.DEFAULT_NAME);
                            }
                            node.setJob(job);
                            DBItemInventoryJob jobDbItem = dbLayer.getJobIfExists(jobChain.getInstanceId(), jobName);
                            if (jobDbItem != null) {
                                node.setJobId(jobDbItem.getId());
                            } else {
                                node.setJobId(DBLayer.DEFAULT_ID);
                            }
                        } else {
                            node.setJob(null);
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
                                String nestedJobChain = 
                                        jobChainPath.getParent().resolve(jobchain).normalize().toString().replace('\\', '/');
                                DBItemInventoryJobChain ijc = dbLayer.getJobChain(jobChain.getInstanceId(), nestedJobChain);
                                if (ijc != null) {
                                    node.setNestedJobChain(jobchain);
                                    node.setNestedJobChainName(ijc.getName());
                                    node.setNestedJobChainId(ijc.getId());
                                } else {
                                    node.setNestedJobChain(jobchain);
                                    node.setNestedJobChainName(nestedJobChain);
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
            } catch (SOSHibernateInvalidSessionException e) {
                hasDbErrors = true;
                throw e;
            }
        }
    }
    
    private void processOrderEvent(String path, JsonObject event, String key) throws Exception {
        if (!closed) {
            Map<String, String> values = new HashMap<String, String>();
            try {
                Date now = Date.from(Instant.now());
                Path filePath = fileExists(path + EConfigFileExtensions.ORDER.extension());
                if (filePath != null) {
                    LOGGER.debug("filePath: " + filePath.toString());
                } else {
                    LOGGER.debug("filePath: null");
                }
                Long instanceId = null;
                if (instance != null) {
                    instanceId = instance.getId();
                    LOGGER.debug(String.format("[inventory] processing event on ORDER: %1$s with path: %2$s",
                            Paths.get(path).getFileName(), Paths.get(path).getParent()));
                    DBItemInventoryOrder order = null;
                    if (isWindows) {
                        order = dbLayer.getInventoryOrderCaseInsensitive(instanceId, path);
                        LOGGER.debug("OS is Windows");
                    } else {
                        order = dbLayer.getInventoryOrder(instanceId, path);
                        LOGGER.debug("OS is Linux");
                    }
                    DBItemInventoryFile file =
                            dbLayer.getInventoryFile(instanceId, path + EConfigFileExtensions.ORDER.extension());
                    boolean fileExists = filePath != null;
                    LOGGER.debug("file exists: " + fileExists);
                    if (fileExists) {
                        LOGGER.debug("file found: going to add/update");
                        if (file == null) {
                            file = createNewInventoryFile(instanceId, filePath, path + EConfigFileExtensions.ORDER.extension(),
                                    FILE_TYPE_ORDER);
                            file.setCreated(now);
                        } else {
                            try {
                                BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                                file.setFileName((path + EConfigFileExtensions.ORDER.extension()).replace('\\', '/'));
                                Path updatedPath = Paths.get(path);
                                String fileDirectory = updatedPath.getParent().toString().replace('\\', '/');
                                file.setFileDirectory(fileDirectory);
                                file.setModified(now);
                                file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                                file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                            } catch (IOException e) {
                                LOGGER.warn(String.format(
                                        "[inventory] cannot read file attributes. file = %1$s, exception = %2$s:%3$s",
                                        filePath.toString(), e.getClass().getSimpleName(), e.getMessage()), e);
                            } catch (Exception e) {
                                LOGGER.warn("[inventory] cannot convert files create and modified timestamps! "
                                        + e.getMessage(), e);
                            }
                        }
                        String baseName = Paths.get(path).getFileName().toString();
                        if (order == null) {
                            LOGGER.debug("order not found in DB: create new entry");
                            order = new DBItemInventoryOrder();
                            order.setInstanceId(instanceId);
                            order.setCreated(now);
                        } else {
                            LOGGER.debug("order found in DB: updating entry");
                        }
                        SOSXMLXPath xpath = new SOSXMLXPath(filePath.toString());
                        if (xpath.getRoot() == null) {
                            throw new SOSInventoryEventProcessingException(String.format("xpath document element missing"));
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
                            String scheduleName = 
                                    Paths.get(path).getParent().resolve(schedule).normalize().toString().replace("\\", "/");
                            DBItemInventorySchedule is = dbLayer.getScheduleIfExists(instanceId, scheduleName);
                            if (is != null) {
                                order.setSchedule(schedule);
                                order.setScheduleName(is.getName());
                                order.setScheduleId(is.getId());
                            } else {
                                order.setSchedule(schedule);
                                order.setScheduleName(scheduleName);
                                order.setScheduleId(DBLayer.DEFAULT_ID);
                            }
                        } else {
                            order.setSchedule(null);
                            order.setScheduleId(DBLayer.DEFAULT_ID);
                            order.setScheduleName(DBLayer.DEFAULT_NAME);
                        }
                        order.setModified(now);
                        file.setModified(now);
                        if ((schedule == null || schedule.isEmpty())) {
                            updateRuntimeAndCalendarUsage("ORDER", order, xpath);
                        }
                        saveOrUpdateItems.add(file);
                        saveOrUpdateItems.add(order);
                        values.put("InventoryEventUpdateFinished", EVENT_TYPE_UPDATED);
                    } else if (!fileExists && order != null) {
                        deleteCalendarUsages(order);
                        deleteItems.add(order);
                        if (file != null) {
                            deleteItems.add(file);
                        }
                        values.put("InventoryEventUpdateFinished", EVENT_TYPE_REMOVED);
                    }
                    eventVariables.put(key, values);
                }
            } catch (SOSHibernateInvalidSessionException e) {
                hasDbErrors = true;
                throw e;
            } catch (SAXParseException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void processProcessClassEvent(String path, JsonObject event, String key) throws Exception {
        if (!closed) {
            Map<String, String> values = new HashMap<String, String>();
            try {
                Date now = Date.from(Instant.now());
                Path filePath = fileExists(path + EConfigFileExtensions.PROCESS_CLASS.extension());
                Long instanceId = null;
                if (instance != null) {
                    instanceId = instance.getId();
                    LOGGER.debug(String.format("[inventory] processing event on PROCESS_CLASS: %1$s with path: %2$s", 
                            Paths.get(path).getFileName(), Paths.get(path).getParent()));
                    DBItemInventoryProcessClass pc = null;
                    if (isWindows) {
                        pc = dbLayer.getInventoryProcessClassCaseInsensitive(instanceId, path);
                    } else {
                        pc = dbLayer.getInventoryProcessClass(instanceId, path);
                    }
                    DBItemInventoryFile file = 
                            dbLayer.getInventoryFile(instanceId, path + EConfigFileExtensions.PROCESS_CLASS.extension());
                    boolean fileExists = filePath != null;
                    if (fileExists) {
                        SOSXMLXPath xpath = new SOSXMLXPath(filePath);
                        if (xpath.getRoot() == null) {
                            throw new Exception(String.format("xpath document element missing"));
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
                                file.setFileName((path + EConfigFileExtensions.PROCESS_CLASS.extension()).replace('\\', '/'));
                                Path updatedPath = Paths.get(path);
                                String fileDirectory = updatedPath.getParent().toString().replace('\\', '/');
                                file.setFileDirectory(fileDirectory);
                                file.setModified(now);
                                file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                                file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                                file.setFileType(fileType);
                            } catch (IOException e) {
                                LOGGER.warn(String.format(
                                        "[inventory] cannot read file attributes. file = %1$s, exception = %2$s:%3$s",
                                        filePath.toString(), e.getClass().getSimpleName(), e.getMessage()), e);
                            } catch (Exception e) {
                                LOGGER.warn("[inventory] cannot convert files create and modified timestamps! "
                                        + e.getMessage(), e);
                            }
                        }
                        if (pc == null) {
                            pc = new DBItemInventoryProcessClass();
                            pc.setInstanceId(instanceId);
                            pc.setCreated(now);
                            pc.setFileId(file.getId());
                        }
                        pc.setName(path);
                        pc.setBasename(Paths.get(path).getFileName().toString());
                        pc.setMaxProcesses(ReportXmlHelper.getMaxProcesses(xpath));
                        pc.setHasAgents(hasAgent);
                        pc.setModified(now);
                        file.setModified(now);
                        saveOrUpdateItems.add(file);
                        saveOrUpdateItems.add(pc);
                        NodeList remoteSchedulersParent = xpath.selectNodeList("/process_class/remote_schedulers");
                        remoteSchedulersToSave.put(pc, remoteSchedulersParent);
                        pcXpaths.put(pc.getName(), xpath);
                        values.put("InventoryEventUpdateFinished", EVENT_TYPE_UPDATED);
                    } else if (!fileExists && pc != null) {
                        deleteItems.add(pc);
                        if (file != null) {
                            deleteItems.add(file);
                        }
                        values.put("InventoryEventUpdateFinished", EVENT_TYPE_REMOVED);
                    }
                    eventVariables.put(key, values);
                }
            } catch (SOSHibernateInvalidSessionException e) {
                hasDbErrors = true;
                throw e;
            } catch (SAXParseException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void processScheduleEvent(String path, JsonObject event, String key) throws Exception {
        if (!closed) {
            Map<String, String> values = new HashMap<String, String>();
            try {
                Date now = Date.from(Instant.now());
                Path filePath = fileExists(path + EConfigFileExtensions.SCHEDULE.extension());
                Long instanceId = null;
                if (instance != null) {
                    instanceId = instance.getId();
                    LOGGER.debug(String.format("[inventory] processing event on SCHEDULE: %1$s with path: %2$s",
                            Paths.get(path).getFileName(), Paths.get(path).getParent()));
                    DBItemInventorySchedule schedule = null;
                    if (isWindows) {
                        schedule = dbLayer.getInventoryScheduleCaseInsensitive(instanceId, path);
                    } else {
                        schedule = dbLayer.getInventorySchedule(instanceId, path);
                    }
                    DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId,
                            path + EConfigFileExtensions.SCHEDULE.extension());
                    boolean fileExists = filePath != null;
                    if (fileExists) {
                        if (file == null) {
                            file = createNewInventoryFile(instanceId, filePath,
                                            path + EConfigFileExtensions.SCHEDULE.extension(), FILE_TYPE_SCHEDULE);
                            file.setCreated(now);
                        } else {
                            try {
                                BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                                file.setFileName((path + EConfigFileExtensions.SCHEDULE.extension()).replace('\\', '/'));
                                Path updatedPath = Paths.get(path);
                                String fileDirectory = updatedPath.getParent().toString().replace('\\', '/');
                                file.setFileDirectory(fileDirectory);
                                file.setModified(now);
                                file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                                file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                            } catch (IOException e) {
                                LOGGER.warn(String.format(
                                        "[inventory] cannot read file attributes. file = %1$s, exception = %2$s:%3$s",
                                        filePath.toString(), e.getClass().getSimpleName(), e.getMessage()), e);
                            } catch (Exception e) {
                                LOGGER.warn("[inventory] cannot convert files create and modified timestamps! "
                                        + e.getMessage(), e);
                            }
                        }
                        if (schedule == null) {
                            schedule = new DBItemInventorySchedule();
                            schedule.setFileId(file.getId());
                            schedule.setInstanceId(instanceId);
                            schedule.setCreated(now);
                        }
                        schedule.setName(path);
                        schedule.setBasename(Paths.get(path).getFileName().toString());
                        SOSXMLXPath xpath = new SOSXMLXPath(filePath.toString());
                        if (xpath.getRoot() == null) {
                            throw new SOSInventoryEventProcessingException(String.format("xpath document element missing"));
                        }
                        schedule.setTitle(ReportXmlHelper.getTitle(xpath));
                        schedule.setSubstitute(ReportXmlHelper.getSubstitute(xpath));
                        schedule.setSubstituteValidFrom(
                                ReportXmlHelper.getSubstituteValidFromTo(xpath, "valid_from", timezone));
                        schedule.setSubstituteValidTo(ReportXmlHelper.getSubstituteValidFromTo(xpath, "valid_to", timezone));
                        boolean pathNormalizationFailure = false;
                        Path parentPath = Paths.get(schedule.getName()).getParent();
                        DBItemInventorySchedule substituteItem =
                                dbLayer.getSubstituteIfExists(
                                        parentPath.resolve(schedule.getSubstitute()).normalize().toString().replace("\\", "/"),
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
                            updateRuntimeAndCalendarUsage("SCHEDULE", schedule, xpath);
                            List<DBItemInventoryClusterCalendarUsage> dbCalendarUsages = 
                                    dbLayer.getAllCalendarUsagesForObject(schedulerId, schedule.getName(), "SCHEDULE");
                            InventoryRuntimeHelper.recalculateRuntime(dbLayer, schedule, dbCalendarUsages, liveDirectory, timezone);
                            saveOrUpdateItems.add(file);
                            saveOrUpdateItems.add(schedule);
                            values.put("InventoryEventUpdateFinished", EVENT_TYPE_UPDATED);
                        }
                    } else if (!fileExists && schedule != null) {
                        deleteCalendarUsages(schedule);
                        deleteItems.add(schedule);
                        if (file != null) {
                            deleteItems.add(file);
                        }
                        values.put("InventoryEventUpdateFinished", EVENT_TYPE_REMOVED);
                    }
                    eventVariables.put(key, values);
                }
            } catch (SOSHibernateInvalidSessionException e) {
                hasDbErrors = true;
                throw e;
            } catch (SAXParseException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void processLockEvent(String path, JsonObject event, String key) throws Exception {
        if (!closed) {
            Map<String, String> values = new HashMap<String, String>();
            try {
                Date now = Date.from(Instant.now());
                Path filePath = fileExists(path + EConfigFileExtensions.LOCK.extension());
                Long instanceId = null;
                if (instance != null) {
                    instanceId = instance.getId();
                    LOGGER.debug(String.format("[inventory] processing event on LOCK: %1$s with path: %2$s",
                            Paths.get(path).getFileName(), Paths.get(path).getParent()));
                    DBItemInventoryLock lock = null;
                    if (isWindows) {
                        lock = dbLayer.getInventoryLockCaseInsensitive(instanceId, path);
                    } else {
                        lock = dbLayer.getInventoryLock(instanceId, path);
                    }
                    DBItemInventoryFile file = dbLayer.getInventoryFile(instanceId,
                            path + EConfigFileExtensions.LOCK.extension());
                    boolean fileExists = filePath != null;
                    if (fileExists) {
                        if (file == null) {
                            file = createNewInventoryFile(instanceId, filePath, path + EConfigFileExtensions.LOCK.extension(),
                                    FILE_TYPE_LOCK);
                            file.setCreated(now);
                        } else {
                            try {
                                BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                                file.setFileName((path + EConfigFileExtensions.LOCK.extension()).replace('\\', '/'));
                                Path updatedPath = Paths.get(path);
                                String fileDirectory = updatedPath.getParent().toString().replace('\\', '/');
                                file.setFileDirectory(fileDirectory);
                                file.setModified(now);
                                file.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                                file.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
                            } catch (IOException e) {
                                LOGGER.warn(String.format(
                                        "[inventory] cannot read file attributes. file = %1$s, exception = %2$s:%3$s",
                                        filePath.toString(), e.getClass().getSimpleName(), e.getMessage()), e);
                            } catch (Exception e) {
                                LOGGER.warn("[inventory] cannot convert files create and modified timestamps! "
                                        + e.getMessage(), e);
                            }
                        }
                        if (lock == null) {
                            lock = new DBItemInventoryLock();
                            lock.setInstanceId(instanceId);
                            lock.setFileId(file.getId());
                            lock.setCreated(now);
                        }
                        lock.setName(path);
                        lock.setBasename(Paths.get(path).getFileName().toString());
                        SOSXMLXPath xpath = new SOSXMLXPath(filePath.toString());
                        if (xpath.getRoot() == null) {
                            throw new SOSInventoryEventProcessingException(String.format("xpath document element missing"));
                        }
                        lock.setMaxNonExclusive(ReportXmlHelper.getMaxNonExclusive(xpath));
                        lock.setModified(now);
                        file.setModified(now);
                        saveOrUpdateItems.add(file);
                        saveOrUpdateItems.add(lock);
                        values.put("InventoryEventUpdateFinished", EVENT_TYPE_UPDATED);
                    } else if (!fileExists && lock != null) {
                        deleteItems.add(lock);
                        if (file != null) {
                            deleteItems.add(file);
                        }
                        values.put("InventoryEventUpdateFinished", EVENT_TYPE_REMOVED);
                    }
                    eventVariables.put(key, values);
                }
            } catch (SOSHibernateInvalidSessionException e) {
                hasDbErrors = true;
                throw e;
            } catch (SAXParseException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void saveAgentClusters(SOSHibernateSession dbConnection, DBItemInventoryProcessClass pc, NodeList nl) throws Exception {
        if (!closed) {
            agentsToDelete = new HashSet<DBItemInventoryAgentInstance>();
            Map<String, Integer> remoteSchedulers = getRemoteSchedulersFromProcessClass(pcXpaths.get(pc.getName()));
            String remoteScheduler = pcXpaths.get(pc.getName()).selectSingleNodeValue("/process_class/@remote_scheduler");
            if (remoteSchedulers == null || remoteSchedulers.isEmpty()) {
                remoteSchedulers = new HashMap<String, Integer>();
                if (remoteScheduler != null && !remoteScheduler.isEmpty()) {
                    remoteSchedulers.put(remoteScheduler.toLowerCase() , 1);
                } else {
                    remoteScheduler = pcXpaths.get(pc.getName())
                            .selectSingleNodeValue("/process_class/remote_schedulers/remote_scheduler/@remote_scheduler");
                    if (remoteScheduler != null && !remoteScheduler.isEmpty()) {
                        remoteSchedulers.put(remoteScheduler.toLowerCase() , 1);
                    }
                }
            }
            if (remoteSchedulers != null && !remoteSchedulers.isEmpty()) {
                List<DBItemInventoryAgentInstance> agentsFromDb = dbLayer.getAllAgentInstancesForInstance(instance.getId());
                List<String> agentUrls = AgentHelper.getAgentInstanceUrls(instance, httpPort);
                for (DBItemInventoryAgentInstance agent : agentsFromDb) {
                    if (!agentUrls.contains(agent.getUrl())) {
                        agentsToDelete.add(agent);
                    }
                }
                Set<DBItemInventoryAgentInstance> newAgentsToAdd = new HashSet<DBItemInventoryAgentInstance>();
                for (String agentUrl : agentUrls) {
                    boolean found = false;
                    for (DBItemInventoryAgentInstance agent : agentsFromDb) {
                        if (agent.getUrl().equals(agentUrl)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        DBItemInventoryAgentInstance agentToSave = AgentHelper.createNewAgent(instance, agentUrl, dbConnection, true);
                        newAgentsToAdd.add(agentToSave);
                    }
                }
                for (DBItemInventoryAgentInstance agent : newAgentsToAdd) {
                    SaveOrUpdateHelper.saveOrUpdateAgentInstance(agent, dbConnection);
                }
                if (nl != null && nl.getLength() > 0) {
                    Element remoteSchedulerParent = (Element) nl.item(0);
                    String schedulingType = remoteSchedulerParent.getAttribute("select");
                    if (schedulingType != null && !schedulingType.isEmpty()) {
                        processAgentCluster(remoteSchedulers, schedulingType, pc.getInstanceId(), pc.getId());
                    } else if (remoteSchedulers.size() == 1) {
                        processAgentCluster(remoteSchedulers, "single", pc.getInstanceId(), pc.getId());
                    } else {
                        processAgentCluster(remoteSchedulers, "first", pc.getInstanceId(), pc.getId());
                    }
                } else {
                    processAgentCluster(remoteSchedulers, "single", pc.getInstanceId(), pc.getId());
                }
            }
        }
    }

    private Map<String,Integer> getRemoteSchedulersFromProcessClass(SOSXMLXPath xpath) throws Exception {
        NodeList remoteSchedulers = xpath.selectNodeList("remote_schedulers/remote_scheduler");
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
    
    private void markRemovedAgentClusterMembersForLaterDelete(
            List<DBItemInventoryAgentClusterMember> actualAgentClusterMembers) throws SOSHibernateException {
        if (!closed) {
            agentClusterMembersToDelete = new HashSet<DBItemInventoryAgentClusterMember>();
            Set<DBItemInventoryAgentClusterMember> dbClusterMembers = new HashSet<DBItemInventoryAgentClusterMember>();
            for (DBItemInventoryAgentClusterMember actualMember : actualAgentClusterMembers) {
                dbClusterMembers.addAll(dbLayer.getAllAgentClusterMembersForInstanceAndCluster(actualMember.getInstanceId(),
                        actualMember.getAgentClusterId()));
            }
            for (DBItemInventoryAgentClusterMember member : dbClusterMembers) {
                if (!actualAgentClusterMembers.contains(member)) {
                    agentClusterMembersToDelete.add(member);
                }
            }
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

    private void processAgentCluster(Map<String,Integer> remoteSchedulers, String schedulingType, Long instanceId,
            Long processClassId) throws Exception {
        if (!closed) {
            Integer numberOfAgents = remoteSchedulers.size();
            DBItemInventoryAgentCluster agentCluster = new DBItemInventoryAgentCluster();
            agentCluster.setInstanceId(instanceId);
            agentCluster.setProcessClassId(processClassId);
            agentCluster.setNumberOfAgents(numberOfAgents);
            agentCluster.setSchedulingType(schedulingType);
            Long clusterId = 
                    SaveOrUpdateHelper.saveOrUpdateAgentCluster(dbLayer, agentCluster, SaveOrUpdateHelper.getAgentClusters());
            List<DBItemInventoryAgentClusterMember> members = new ArrayList<DBItemInventoryAgentClusterMember>();
            for (String agentUrl : remoteSchedulers.keySet()) {
                DBItemInventoryAgentInstance agent = dbLayer.getInventoryAgentInstanceFromDb(agentUrl, instanceId);
                if (agent != null) {
                    Integer ordering = remoteSchedulers.get(agent.getUrl().toLowerCase());
                    if (ordering == null) {
                        ordering = 1;
                    }
                    DBItemInventoryAgentClusterMember agentClusterMember = new DBItemInventoryAgentClusterMember();
                    agentClusterMember.setInstanceId(instanceId);
                    agentClusterMember.setAgentClusterId(clusterId);
                    agentClusterMember.setAgentInstanceId(agent.getId());
                    agentClusterMember.setUrl(agent.getUrl());
                    agentClusterMember.setOrdering(ordering);
                    SaveOrUpdateHelper.saveOrUpdateAgentClusterMember(dbLayer, agentClusterMember,
                            SaveOrUpdateHelper.getAgentClusterMembers());
                    members.add(agentClusterMember);
                }
            }
            markRemovedAgentClusterMembersForLaterDelete(members);
        }
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
                    return jsonEventId.longValue();
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
                uriBuilder.addParameter(WEBSERVICE_PARAM_KEY_RETURN, WEBSERVICE_PARAM_VALUE_FILEBASED_EVENT + ","
                + WEBSERVICE_PARAM_VALUE_SCHEDULER_EVENT + "," + WEBSERVICE_PARAM_VALUE_CALENDAR_EVENT);
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
                newEventId = jsonEventId.longValue();
                LOGGER.debug(String.format("[inventory] eventId received from FileBasedEvents: %1$d", newEventId));
                return result;
            } catch (Exception e) {
                if (!closed) {
                    LOGGER.error(e.getMessage(), e);
                    throw e;
                }
            }
        } else {
            throw new SOSInventoryEventProcessingException("[inventory] JobScheduler is closed!");
        }
        return null;
    }
    
    private JsonObject getJsonObjectFromResponse(URI uri, boolean withBody)
            throws SOSInventoryEventProcessingException, Exception {
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
                    throw new SOSInventoryEventProcessingException(
                            "[inventory] Unexpected content type '" + contentType + "'. Response: " + response);
                }
            case 400:
                throw new SOSBadRequestException(
                        "[inventory] Unexpected content type '" + contentType + "'. Response: " + response);
            default:
                throw new SOSBadRequestException("[inventory] " + httpReplyCode + " "
                        + restApiClient.getHttpResponse().getStatusLine().getReasonPhrase());
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
    
    private void deleteCalendarUsages(DbItem item) throws SOSHibernateException {
        List<DBItemInventoryClusterCalendarUsage> calendarUsages = dbLayer.getCalendarUsagesToDelete(item);
        for (DBItemInventoryClusterCalendarUsage dbCalendarUsage : calendarUsages) {
            deleteItems.add(dbCalendarUsage);
        }
    }
    
    private void updateRuntimeAndCalendarUsage(String type, DbItem dbItem, SOSXMLXPath xPath) throws Exception {
        List<DBItemInventoryClusterCalendarUsage> dbCalendarUsages = null;
        dbLayer.getSession().beginTransaction();
        if ("ORDER".equals(type)) {
            dbCalendarUsages = dbLayer.getAllCalendarUsagesForObject(schedulerId, ((DBItemInventoryOrder)dbItem).getName(), type);
            InventoryRuntimeHelper.createOrUpdateCalendarUsage(xPath, dbCalendarUsages, dbItem, type, dbLayer, liveDirectory, schedulerId, timezone);
        } else if ("JOB".equals(type)) {
            dbCalendarUsages = dbLayer.getAllCalendarUsagesForObject(schedulerId, ((DBItemInventoryJob)dbItem).getName(), type);
            InventoryRuntimeHelper.createOrUpdateCalendarUsage(xPath, dbCalendarUsages, dbItem, type, dbLayer, liveDirectory, schedulerId, timezone);
        } else if ("SCHEDULE".equals(type)) {
            dbCalendarUsages = dbLayer.getAllCalendarUsagesForObject(schedulerId, ((DBItemInventorySchedule)dbItem).getName(), type);
            InventoryRuntimeHelper.createOrUpdateCalendarUsage(xPath, dbCalendarUsages, dbItem, type, dbLayer, liveDirectory, schedulerId, timezone);
        }
        dbLayer.getSession().commit();
    }
    
}