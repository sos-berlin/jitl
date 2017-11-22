package com.sos.plugins.event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.classes.event.EventHandlerSettings;
import com.sos.jitl.classes.event.JobSchedulerEvent.EventOverview;
import com.sos.jitl.classes.event.JobSchedulerEvent.EventType;
import com.sos.jitl.classes.event.JobSchedulerPluginEventHandler;
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.reporting.db.DBItemCalendar;
import com.sos.jitl.reporting.db.DBItemInventoryCalendarUsage;
import com.sos.jitl.reporting.db.DBItemInventoryFile;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jobscheduler.model.event.CalendarEvent;
import com.sos.jobscheduler.model.event.CalendarObjectType;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.Calendars;
import com.sos.scheduler.engine.eventbus.EventBus;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;


public class CustomEventHandler extends JobSchedulerPluginEventHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomEventHandler.class);
    private static final String CALENDAR_CREATED = "CalendarCreated";
    private static final String CALENDAR_UPDATED = "CalendarUpdated";
    private static final String CALENDAR_DELETED = "CalendarDeleted";
    private static final String CALENDAR_USAGE_UPDATED = "CalendarUsageUpdated";
    private static final String CALENDAR_FILE_EXTENSION = ".calendar.json";
    private static final String CALENDAR_FILE_TYPE = "calendar";
    private static final String CALENDAR_ORDER_FILE_EXTENSION = ".order.calendar.json";
    private static final String CALENDAR_JOB_FILE_EXTENSION = ".job.calendar.json";
    private static final String CALENDAR_SCHEDULE_FILE_EXTENSION = ".schedule.calendar.json";
    private static final String CALENDAR_ORDER_FILE_TYPE = "order.calendar";
    private static final String CALENDAR_JOB_FILE_TYPE = "job.calendar";
    private static final String CALENDAR_SCHEDULE_FILE_TYPE = "schedule.calendar";
    @SuppressWarnings("unused")
    private String identifier;
    private SOSHibernateFactory reportingFactory;
    private boolean hasErrors = false;
    // wait interval(in seconds) after DB executions
    private int waitInterval = 1;
    private DBLayerInventory dbLayer = null;
    private Long instanceId = null;

    public CustomEventHandler(SchedulerXmlCommandExecutor xmlExecutor, EventBus eventBus) {
        super(xmlExecutor, eventBus);
    }

    @Override
    public void onActivate(PluginMailer mailer) {
        super.onActivate(mailer);
        try {
            LOGGER.info("*** CustomEventPlugin started ***");
            createReportingFactory(getSettings().getHibernateConfigurationReporting());
            EventType[] observedEventTypes = new EventType[] { EventType.VariablesCustomEvent };
            start(observedEventTypes, EventOverview.FileBasedOverview);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void onPrepare(EventHandlerSettings settings) {
        super.onPrepare(settings);       
    }

    @Override
    public void onEmptyEvent(Long eventId) {
        if (hasErrors) {
            LOGGER.debug("onEmptyEvent: eventId=" + eventId);
            execute(false, eventId, null);
        }
    }

    @Override
    public void onNonEmptyEvent(Long eventId, JsonArray events) {
        LOGGER.debug("onNonEmptyEvent: eventId=" + eventId);
        hasErrors = false;
        execute(true, eventId, events);
    }

    @Override
    public void onEnded() {
        closeRestApiClient();
        reportingFactory.close();
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;   
    }

    private void createReportingFactory(Path configFile) throws Exception {
        reportingFactory = new SOSHibernateFactory(configFile);
        reportingFactory.setIdentifier(getIdentifier());
        reportingFactory.setAutoCommit(false);
        reportingFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        reportingFactory.addClassMapping(DBLayer.getReportingClassMapping());
        reportingFactory.addClassMapping(DBLayer.getInventoryClassMapping());
        reportingFactory.build();
    }

    private void execute(boolean onNonEmptyEvent, Long eventId, JsonArray events) {
        SOSHibernateSession reportingSession = null;
        try {
            reportingSession = reportingFactory.openStatelessSession();
            dbLayer = new DBLayerInventory(reportingSession);
            DBItemInventoryInstance instance = 
                    dbLayer.getInventoryInstance(getSettings().getHttpHost(), Integer.parseInt(getSettings().getHttpPort()));
            if (instance != null) {
                instanceId = instance.getId();
            }
            processEvents(events, eventId);
        } catch (Throwable e) {
            hasErrors = true;
            LOGGER.error(e.getMessage(), e);
            getMailer().sendOnError(CustomEventHandler.class.getSimpleName(), "execute", e);
        } finally {
            if (reportingSession != null) {
                reportingSession.close();
            }
            wait(waitInterval);
        }
    }

    private void processEvents(JsonArray events, Long eventId) throws Exception {
        for (int i = 0; i < events.size(); i++) {
            CalendarEvent event = new ObjectMapper().readValue(events.getJsonObject(i).toString(), CalendarEvent.class);
            Calendar calendar = null;
            String path = event.getVariables().getPath(); 
            CalendarObjectType objectType = event.getVariables().getObjectType();
            DBItemCalendar dbCalendar = null;
            List<DBItemInventoryCalendarUsage> dbCalendarUsages = null;
            if (event.getKey().equalsIgnoreCase(CALENDAR_DELETED)) {
                processCalendarEventOnFile(event, null);
            } else if (objectType == null) {
                dbCalendar = dbLayer.getCalendar(instanceId, path);
                if (dbCalendar != null) {
                    calendar = new ObjectMapper().readValue(dbCalendar.getConfiguration(), Calendar.class);
                    processCalendarEventOnFile(event, calendar);
                }
            } else if (event.getKey().equalsIgnoreCase(CALENDAR_USAGE_UPDATED)) {
                dbCalendarUsages = dbLayer.getCalendarUsages(instanceId, path, objectType.name());
                Calendars calendars = new Calendars();
                calendars.setCalendars(new ArrayList<Calendar>());
                if (dbCalendarUsages != null) {
                    for (DBItemInventoryCalendarUsage dbCalendarUsage : dbCalendarUsages) {
                        calendar = new ObjectMapper().readValue(dbCalendarUsage.getConfiguration(), Calendar.class);
                        calendars.getCalendars().add(calendar);
                    }
                    processCalendarsEventOnFile(event, calendars);
                }
            }
        }
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
        dbFile.setCreated(Date.from(Instant.now()));
        dbFile.setModified(Date.from(Instant.now()));
        if (filePath != null) {
            try {
                BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                dbFile.setFileCreated(ReportUtil.convertFileTime2UTC(attrs.creationTime()));
                dbFile.setFileModified(ReportUtil.convertFileTime2UTC(attrs.lastModifiedTime()));
                dbFile.setFileLocalCreated(ReportUtil.convertFileTime2Local(attrs.creationTime()));
                dbFile.setFileLocalModified(ReportUtil.convertFileTime2Local(attrs.lastModifiedTime()));
            } catch (IOException e) {
                LOGGER.warn(String.format("[%1$s] cannot read file attributes. file = %2$s, exception = %3$s:%4$s",
                        getIdentifier(), filePath.toString(), e.getClass().getSimpleName(), e.getMessage()), e);
            } catch (Exception e) {
                LOGGER.warn("[" + getIdentifier() + "] cannot convert files create and modified timestamps! " + e.getMessage(), e);
            }
        }
        return dbFile;
    }

    private void processCalendarsEventOnFile(CalendarEvent event, Calendars calendars) throws Exception {
        String path = event.getVariables().getPath(); 
        String oldPath = event.getVariables().getOldPath();
        CalendarObjectType objectType = event.getVariables().getObjectType();
        String fileExtension = getFileExtensionFromObjectType(objectType);
        Path filePath = getSettings().getLiveDirectory().resolve(path.replaceFirst("^/*", "") + fileExtension);
        OutputStream out = Files.newOutputStream(filePath, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.TRUNCATE_EXISTING, 
                StandardOpenOption.WRITE);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        if (calendars != null) {
            Files.createDirectories(filePath.getParent());
            if (calendars.getCalendars() != null && !calendars.getCalendars().isEmpty()) {
                if (oldPath != null && !oldPath.equals(path)) {
                    try {
                        Files.move(getSettings().getLiveDirectory().resolve(oldPath.replaceFirst("^/*", "") + fileExtension),
                                getSettings().getLiveDirectory().resolve(path.replaceFirst("^/*", "") + fileExtension));
                        LOGGER.info(String.format("calendar usage %1$s renamed", path));
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                try {
                    new ObjectMapper().writeValue(out, calendars);
                    DBItemInventoryFile dbFile = dbLayer.getInventoryFile(instanceId, oldPath + fileExtension);
                    if (dbFile != null) {
                        dbFile.setFileName(path + fileExtension);
                        dbFile.setFileBaseName(Paths.get(path).getFileName().toString() + fileExtension);
                        dbFile.setModified(Date.from(Instant.now()));
                        dbLayer.getSession().beginTransaction();
                        dbLayer.getSession().update(dbFile);
                        dbLayer.getSession().commit();
                        LOGGER.info(String.format("calendar usage %1$s updated", path));
                    } else {
                        dbFile = dbLayer.getInventoryFile(instanceId, path + fileExtension);
                        if (dbFile != null) {
                            dbFile.setFileName(path + fileExtension);
                            dbFile.setFileBaseName(Paths.get(path).getFileName().toString() + fileExtension);
                            dbFile.setModified(Date.from(Instant.now()));
                            dbLayer.getSession().beginTransaction();
                            dbLayer.getSession().update(dbFile);
                            dbLayer.getSession().commit();
                            LOGGER.info(String.format("calendar usage %1$s updated", path));
                        } else {
                            dbFile = createNewInventoryFile(instanceId, filePath, path + fileExtension, 
                                    getFileTypeFromFileExtension(fileExtension));
                            if (dbFile != null) {
                                dbLayer.getSession().beginTransaction();
                                dbLayer.getSession().save(dbFile);
                                dbLayer.getSession().commit();
                                LOGGER.info(String.format("calendar usage %1$s saved", path));
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    try {
                        out.close();
                    } catch (IOException e) {
                    }
                }
            } else {
                if (path != null) {
                    try {
                        Files.delete(filePath);
                        DBItemInventoryFile dbFile = dbLayer.getInventoryFile(instanceId, path + fileExtension);
                        if (dbFile != null) {
                            dbLayer.getSession().beginTransaction();
                            dbLayer.getSession().delete(dbFile);
                            dbLayer.getSession().commit();
                            LOGGER.info(String.format("calendar usage %1$s deleted", path));
                        }
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    } finally {
                        try {
                            out.close();
                        } catch (IOException e) {}
                    }
                }
            }
        }
    }
    
    private void processCalendarEventOnFile(CalendarEvent event, Calendar calendar) throws Exception {
        String path = event.getVariables().getPath(); 
        String oldPath = event.getVariables().getOldPath();
        CalendarObjectType objectType = event.getVariables().getObjectType();
        String fileExtension = getFileExtensionFromObjectType(objectType);
        Path filePath = getSettings().getLiveDirectory().resolve(path.replaceFirst("^/*", "") + fileExtension);
        Files.createDirectories(filePath.getParent());
        LOGGER.info(String.format("missing directories created for %1$s", calendar.getPath()));
        OutputStream out = Files.newOutputStream(filePath, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.TRUNCATE_EXISTING, 
                StandardOpenOption.WRITE);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        switch (event.getKey()) {
        case CALENDAR_CREATED:
            if (calendar != null) {
                try {
                    objectMapper.writeValue(out, calendar);
                    DBItemInventoryFile dbFile = createNewInventoryFile(instanceId, filePath, path + fileExtension, 
                            getFileTypeFromFileExtension(fileExtension));
                    DBItemInventoryFile fileFromDb = dbLayer.getInventoryFile(instanceId, path + fileExtension);
                    if (dbFile != null && fileFromDb == null) {
                        dbLayer.getSession().beginTransaction();
                        dbLayer.getSession().save(dbFile);
                        dbLayer.getSession().commit();
                    }
                } catch (FileNotFoundException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    try {
                        out.close();
                        LOGGER.info(String.format("Calendar %1$s saved", calendar.getPath()));
                    } catch (IOException e) {}
                }
                break;
            }
            break;
        case CALENDAR_UPDATED:
            if (calendar != null) {
                if (oldPath != null && !oldPath.equals(path)) {
                    try {
                        Files.move(getSettings().getLiveDirectory().resolve(oldPath.replaceFirst("^/*", "") + fileExtension),
                                getSettings().getLiveDirectory().resolve(path.replaceFirst("^/*", "") + fileExtension));
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    } finally {
                        LOGGER.info(String.format("Calendar %1$s renamed", calendar.getPath()));
                    }
                }
                try {
                    objectMapper.writeValue(out, calendar);
                    DBItemInventoryFile dbFile = dbLayer.getInventoryFile(instanceId, oldPath + fileExtension);
                    if (dbFile != null) {
                        dbFile.setFileName(path + fileExtension);
                        dbFile.setFileBaseName(Paths.get(path).getFileName().toString());
                        dbFile.setModified(Date.from(Instant.now()));
                        dbLayer.getSession().beginTransaction();
                        dbLayer.getSession().update(dbFile);
                        dbLayer.getSession().commit();
                    } else {
                        dbFile = createNewInventoryFile(instanceId, filePath, path + fileExtension, 
                                getFileTypeFromFileExtension(fileExtension));
                        if (dbFile != null) {
                            dbLayer.getSession().beginTransaction();
                            dbLayer.getSession().save(dbFile);
                            dbLayer.getSession().commit();
                        }
                    }
                } catch (FileNotFoundException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    try {
                        out.close();
                        LOGGER.info(String.format("Calendar %1$s updated", calendar.getPath()));
                    } catch (IOException e) {
                    }
                }
                break;
            }
            break;
        case CALENDAR_DELETED:
            if (path != null) {
                try {
                    Files.delete(filePath);
                    DBItemInventoryFile dbFile = dbLayer.getInventoryFile(instanceId, path + fileExtension);
                    if (dbFile != null) {
                        dbLayer.getSession().beginTransaction();
                        dbLayer.getSession().delete(dbFile);
                        dbLayer.getSession().commit();
                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    try {
                        out.close();
                        LOGGER.info(String.format("Calendar %1$s deleted", calendar.getPath()));
                    } catch (IOException e) {
                    }
                }
            }
            break;
        }
    }
    
    private String getFileExtensionFromObjectType(CalendarObjectType type) {
        if (type == null) {
            return CALENDAR_FILE_EXTENSION;
        }
        switch (type.name()) {
        case "JOB":
            return CALENDAR_JOB_FILE_EXTENSION;
        case "ORDER":
            return CALENDAR_ORDER_FILE_EXTENSION;
        case "SCHEDULE":
            return CALENDAR_SCHEDULE_FILE_EXTENSION;
        default:
            return null;
        }
    }
    
    private String getFileTypeFromFileExtension (String fileExtension) {
        switch (fileExtension) {
        case CALENDAR_JOB_FILE_EXTENSION:
            return CALENDAR_JOB_FILE_TYPE;
        case CALENDAR_ORDER_FILE_EXTENSION:
            return CALENDAR_ORDER_FILE_TYPE;
        case CALENDAR_SCHEDULE_FILE_EXTENSION:
            return CALENDAR_SCHEDULE_FILE_TYPE;
        default:
            return CALENDAR_FILE_TYPE;
        }
    }
}
