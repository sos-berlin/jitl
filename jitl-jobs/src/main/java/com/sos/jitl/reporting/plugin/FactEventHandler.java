package com.sos.jitl.reporting.plugin;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.classes.event.EventHandlerSettings;
import com.sos.jitl.classes.event.JobSchedulerEvent.EventKey;
import com.sos.jitl.classes.event.JobSchedulerEvent.EventType;
import com.sos.jitl.classes.event.JobSchedulerPluginEventHandler;
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.jitl.dailyplan.db.DailyPlanAdjustment;
import com.sos.jitl.dailyplan.job.CheckDailyPlanOptions;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.job.report.FactJobOptions;
import com.sos.jitl.reporting.model.report.FactModel;
import com.sos.scheduler.engine.eventbus.EventBus;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class FactEventHandler extends JobSchedulerPluginEventHandler {

    public static enum CustomEventType {
        DailyPlanChanged, ReportingChanged
    };

    public static enum CustomEventTypeValue {
        order, standalone, order_standalone
    };

    public static final String CUSTOM_EVENT_KEY = FactEventHandler.class.getSimpleName();

    private static final Logger LOGGER = LoggerFactory.getLogger(FactEventHandler.class);
    private final String className = FactEventHandler.class.getSimpleName();
    private static final String JOB_CHAIN_CREATE_DAILY_PLAN = "/sos/dailyplan/CreateDailyPlan";
    private SOSHibernateFactory reportingFactory;
    private SOSHibernateFactory schedulerFactory;
    private boolean useNotificationPlugin = false;
    private String customEventValue = null;
    private boolean hasErrorOnEventProcessing = false;
    // wait iterval after db executions in seconds
    private int waitInterval = 2;

    public FactEventHandler(SchedulerXmlCommandExecutor xmlExecutor, EventBus eventBus) {
        super(xmlExecutor, eventBus);
    }

    public void setUseNotificationPlugin(boolean useNotification) {
        useNotificationPlugin = useNotification;
    }

    @Override
    public void onPrepare(EventHandlerSettings settings) {
        super.onPrepare(settings);
    }

    @Override
    public void onActivate(PluginMailer mailer) {
        super.onActivate(mailer);

        String method = "onActivate";
        try {
            createReportingFactory(getSettings().getHibernateConfigurationReporting());
            createSchedulerFactory(getSettings().getHibernateConfigurationScheduler());

            EventType[] observedEventTypes = new EventType[] { EventType.TaskStarted, EventType.TaskEnded, EventType.OrderStepStarted,
                    EventType.OrderStepEnded, EventType.OrderFinished };
            start(observedEventTypes);
        } catch (Exception e) {
            LOGGER.error(String.format("%s: %s", method, e.toString()), e);
            getMailer().sendOnError(className, method, e);
        }
    }

    @Override
    public void onEnded() {
        closeRestApiClient();
        closeSchedulerFactory();
        closeReportingFactory();
    }

    @Override
    public void onNonEmptyEvent(Long eventId, JsonArray events) {
        String method = "onNonEmptyEvent";
        LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
        
        hasErrorOnEventProcessing = false;
        execute(true, eventId, events);
    }

    @Override
    public void onEmptyEvent(Long eventId) {
        if (hasErrorOnEventProcessing) {
            String method = "onEmptyEvent";
            LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
            
            execute(false, eventId, null);
        }
    }

    private void execute(boolean onNonEmptyEvent, Long eventId, JsonArray events) {
        String method = "execute";
        LOGGER.debug(String.format("%s: onNonEmptyEvent=%s, eventId=%s", method, onNonEmptyEvent, eventId));

        SOSHibernateSession reportingSession = null;
        SOSHibernateSession schedulerSession = null;
        FactModel factModel = null;
        customEventValue = null;

        try {
            reportingSession = this.reportingFactory.openStatelessSession();
            schedulerSession = this.schedulerFactory.openStatelessSession();

            factModel = executeFacts(reportingSession, schedulerSession, useNotificationPlugin);
            executeDailyPlan(reportingSession, factModel.isChanged(), events);
            hasErrorOnEventProcessing = false;
        } catch (Throwable e) {
            hasErrorOnEventProcessing = true;
            LOGGER.error(String.format("%s: %s", method, e.toString()), e);
            getMailer().sendOnError(className, method, e);
        } finally {
            publishCustomEvents();

            if (factModel != null) {
                factModel.exit();
            }
            if (reportingSession != null) {
                reportingSession.close();
            }
            if (schedulerSession != null) {
                schedulerSession.close();
            }
            wait(waitInterval);
        }
    }

    private FactModel executeFacts(SOSHibernateSession reportingSession, SOSHibernateSession schedulerSession, boolean executeNotificationPlugin)
            throws Exception {
        String method = "executeFacts";
        FactModel factModel = null;
        try {
            LOGGER.debug(String.format("%s: execute ...", method));

            FactJobOptions options = new FactJobOptions();
            options.current_scheduler_id.setValue(getSettings().getSchedulerId());
            options.current_scheduler_hostname.setValue(getSettings().getHost());
            options.current_scheduler_http_port.setValue(getSettings().getHttpPort());
            options.hibernate_configuration_file.setValue(getSettings().getHibernateConfigurationReporting().toString());
            options.hibernate_configuration_file_scheduler.setValue(getSettings().getHibernateConfigurationScheduler().toString());
            options.max_history_age.setValue("30m");
            options.force_max_history_age.value(false);
            options.execute_notification_plugin.setValue(String.valueOf(executeNotificationPlugin));

            factModel = new FactModel(reportingSession, schedulerSession, options);
            factModel.init(getMailer(), getSettings().getConfigDirectory());
            factModel.process();

            if (factModel.isChanged()) {
                if (factModel.isOrderChanged() && factModel.isStandaloneChanged()) {
                    customEventValue = CustomEventTypeValue.order_standalone.name();
                } else if (factModel.isOrderChanged()) {
                    customEventValue = CustomEventTypeValue.order.name();
                } else if (factModel.isStandaloneChanged()) {
                    customEventValue = CustomEventTypeValue.standalone.name();
                }
                addCustomEventValue(CUSTOM_EVENT_KEY, CustomEventType.ReportingChanged.name(), customEventValue);
            }

        } catch (Exception e) {
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        }
        return factModel;
    }

    private ArrayList<String> getCreateDailyPlanEvents(JsonArray events) throws Exception {
        ArrayList<String> createDailyPlanEvents = new ArrayList<String>();
        if (events != null && events.size() > 0) {
            for (int i = 0; i < events.size(); i++) {
                JsonObject jo = events.getJsonObject(i);
                String joType = jo.getString(EventKey.TYPE.name());
                String key = getEventKey(jo);
                if (key != null) {
                    if (key.toLowerCase().contains(JOB_CHAIN_CREATE_DAILY_PLAN.toLowerCase())) {
                        createDailyPlanEvents.add(joType);
                    }
                }
            }
        }
        return createDailyPlanEvents;
    }

    private void executeDailyPlan(SOSHibernateSession reportingSession, boolean hasReportingChanges, JsonArray events) throws Exception {
        String method = "executeDailyPlan";
        try {
            if (!hasReportingChanges) {
                LOGGER.debug(String.format("%s: skip execute, 0 reporting changes", method));
                return;
            }
            ArrayList<String> createDailyPlanEvents = getCreateDailyPlanEvents(events);
            if (createDailyPlanEvents.size() > 0 && !createDailyPlanEvents.contains(EventType.TaskEnded.name()) && !createDailyPlanEvents.contains(
                    EventType.TaskClosed.name())) {
                LOGGER.debug(String.format("%s: skip execute, found not ended %s events", method, JOB_CHAIN_CREATE_DAILY_PLAN));
                return;
            }

            LOGGER.debug(String.format("%s: execute ...", method));
            CheckDailyPlanOptions options = new CheckDailyPlanOptions();
            options.scheduler_id.setValue(getSettings().getSchedulerId());
            options.dayOffset.setValue("0");
            try {
                options.configuration_file.setValue(getSettings().getHibernateConfigurationReporting().toFile().getCanonicalPath());
            } catch (Exception e) {
            }

            DailyPlanAdjustment dp = new DailyPlanAdjustment(reportingSession);
            dp.setOptions(options);
            dp.setTo(new Date());
            reportingSession.beginTransaction();
            dp.adjustWithHistory();
            reportingSession.commit();

            if (dp.isDailyPlanUpdated()) {
                LOGGER.debug(String.format("%s: daily plan was changed", method));
                addCustomEventValue(CUSTOM_EVENT_KEY, CustomEventType.DailyPlanChanged.name(), customEventValue);
            } else {
                LOGGER.debug(String.format("%s: daily plan was not changed", method));
            }

        } catch (Exception e) {
            try {
                reportingSession.rollback();
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s: %s", method, ex.toString()), ex);
            }
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        }
    }

    private void createReportingFactory(Path configFile) throws Exception {
        reportingFactory = new SOSHibernateFactory(configFile);
        reportingFactory.setIdentifier("reporting");
        reportingFactory.setAutoCommit(false);
        reportingFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        reportingFactory.addClassMapping(DBLayer.getReportingClassMapping());
        reportingFactory.addClassMapping(DBLayer.getInventoryClassMapping());
        reportingFactory.addClassMapping(com.sos.jitl.notification.db.DBLayer.getNotificationClassMapping());
        reportingFactory.build();
    }

    private void createSchedulerFactory(Path configFile) throws Exception {
        schedulerFactory = new SOSHibernateFactory(configFile);
        schedulerFactory.setIdentifier("scheduler");
        schedulerFactory.setAutoCommit(true);
        Enum<SOSHibernateFactory.Dbms> dbms = schedulerFactory.getDbmsBeforeBuild();
        if (dbms.equals(SOSHibernateFactory.Dbms.MSSQL) || dbms.equals(SOSHibernateFactory.Dbms.MYSQL)) {
            schedulerFactory.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        } else {
            schedulerFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }
        schedulerFactory.addClassMapping(DBLayer.getSchedulerClassMapping());
        schedulerFactory.build();
    }

    private void closeReportingFactory() {
        if (reportingFactory != null) {
            reportingFactory.close();
            reportingFactory = null;
        }
    }

    private void closeSchedulerFactory() {
        if (schedulerFactory != null) {
            schedulerFactory.close();
            schedulerFactory = null;
        }
    }

}
