package com.sos.plugins.event;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jobscheduler.model.event.CalendarEvent;
import com.sos.jobscheduler.model.event.CalendarObjectType;
import com.sos.joc.model.calendar.Calendar;
import com.sos.scheduler.engine.eventbus.EventBus;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;


public class CustomEventHandler extends JobSchedulerPluginEventHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomEventHandler.class);
    private static final String CALENDAR_CREATED = "CalendarCreated";
    private static final String CALENDAR_UPDATED = "CalendarUpdated";
    private static final String CALENDAR_DELETED = "CalendarDeleted";
    private static final String CALENDAR_FILE_EXTENSION = ".calendar.json";
    private static final String CALENDAR_ORDER_FILE_EXTENSION = ".order.calendar.json";
    private static final String CALENDAR_JOB_FILE_EXTENSION = ".job.calendar.json";
    private static final String CALENDAR_SCHEDULE_FILE_EXTENSION = ".schedule.calendar.json";
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
    }

    @Override
    public void onPrepare(EventHandlerSettings settings) {
        super.onPrepare(settings);       
        try {
            createReportingFactory(getSettings().getHibernateConfigurationReporting());
            EventType[] observedEventTypes = new EventType[] { EventType.VariablesCustomEvent };
            start(observedEventTypes, EventOverview.FileBasedOverview);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
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
        reportingFactory.setIdentifier(CustomEventHandler.class.getSimpleName());
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
            CalendarEvent event = new ObjectMapper().convertValue(events.getJsonObject(i), CalendarEvent.class);
            Calendar calendar = null;
            String path = event.getVariables().getPath(); 
            String oldPath = event.getVariables().getOldPath();
            CalendarObjectType objectType = event.getVariables().getObjectType();
            DBItemCalendar dbCalendar = null;
            DBItemInventoryCalendarUsage dbCalendarUsage = null;
            String fileExtension = getFileExtensionFromObjectType(objectType);
            if (objectType == null) {
                dbCalendar = dbLayer.getCalendar(instanceId, path);
                if (dbCalendar != null) {
                    calendar = new ObjectMapper().readValue(dbCalendar.getConfiguration(), Calendar.class);
                }
            } else {
                dbCalendarUsage = dbLayer.getCalendarUsage(instanceId, path, objectType.name());
                if (dbCalendarUsage != null) {
                    calendar = new ObjectMapper().readValue(dbCalendarUsage.getConfiguration(), Calendar.class);
                }
            }
            Path filePath = getSettings().getLiveDirectory().resolve(path + fileExtension);
            FileOutputStream out = (FileOutputStream) Files.newOutputStream(filePath);
            switch (event.getKey()) {
            case CALENDAR_CREATED:
                if (calendar != null) {
                    try {
                        new ObjectMapper().writeValue(out, calendar);
                    } catch (FileNotFoundException e) {
                        LOGGER.error(e.getMessage(), e);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    } finally {
                        try {
                            out.close();
                        } catch (IOException e) {}
                    }
                    break;
                }
                break;
            case CALENDAR_UPDATED:
                if (calendar != null) {
                    if (oldPath != null && !oldPath.equals(path)) {
                        try {
                            Files.move(getSettings().getLiveDirectory().resolve(oldPath + fileExtension),
                                    getSettings().getLiveDirectory().resolve(path + fileExtension));
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                    try {
                        new ObjectMapper().writeValue(out, calendar);
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
                    break;
                }
                break;
            case CALENDAR_DELETED:
                if (path != null) {
                    try {
                        Files.delete(filePath);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                break;
            }
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
    
}
