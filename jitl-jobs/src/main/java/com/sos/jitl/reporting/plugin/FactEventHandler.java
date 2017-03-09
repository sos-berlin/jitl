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
        order, standalone
    };

    public static final String CUSTOM_EVENT_KEY = FactEventHandler.class.getSimpleName();

    private static final Logger LOGGER = LoggerFactory.getLogger(FactEventHandler.class);
    private final String className = FactEventHandler.class.getSimpleName();
    private static final String JOB_CHAIN_CREATE_DAILY_PLAN = "/sos/dailyplan/CreateDailyPlan";
    private SOSHibernateFactory reportingFactory;
    private SOSHibernateFactory schedulerFactory;
    private boolean useNotificationPlugin = false;
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
            initConnectionFactories();

            EventType[] observedEventTypes = new EventType[] { EventType.TaskStarted, EventType.TaskEnded, EventType.OrderStepStarted,
                    EventType.OrderStepEnded, EventType.OrderFinished };
            start(observedEventTypes);
        } catch (Exception e) {
            LOGGER.error(String.format("%s: %s", method, e.toString()), e);
            getMailer().sendOnError(className, method, e);
        }
    }

    @Override
    public void onNonEmptyEvent(Long eventId, JsonArray events) {
        String method = "onNonEmptyEvent";
        LOGGER.debug(String.format("%s: eventId=%s", method, eventId));

        if (isClosed()) {
            return;
        }

        getCustomEvents().clear();
        SOSHibernateSession reportingSession = null;
        SOSHibernateSession schedulerSession = null;
        FactModel factModel = null;
        try {
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

            reportingSession = this.reportingFactory.openStatelessSession();
            schedulerSession = this.schedulerFactory.openStatelessSession();
            try {
                factModel = new FactModel(reportingSession, schedulerSession, createFactOptions(useNotificationPlugin));
                factModel.init(getMailer(), getSettings().getConfigDirectory());
                factModel.process();

                if (factModel.isChanged()) {
                    ArrayList<String> customEventValues = new ArrayList<String>();
                    if (factModel.isOrderChanged()) {
                        customEventValues.add(CustomEventTypeValue.order.name());
                    }
                    if (factModel.isStandaloneChanged()) {
                        customEventValues.add(CustomEventTypeValue.standalone.name());
                    }
                    String customEventValue = addCustomEventValue(CUSTOM_EVENT_KEY, CustomEventType.ReportingChanged.name(), customEventValues);

                    if (createDailyPlanEvents.size() > 0 && !createDailyPlanEvents.contains(EventType.TaskEnded.name()) && !createDailyPlanEvents
                            .contains(EventType.TaskClosed.name())) {

                        LOGGER.debug(String.format("%s: skip executeDailyPlan: found not ended %s events", method, JOB_CHAIN_CREATE_DAILY_PLAN));
                    } else {
                        try {
                            LOGGER.debug(String.format("%s: executeDailyPlan ...", method));
                            executeDailyPlan(reportingSession, customEventValue);
                        } catch (Exception e) {
                            if (isClosed()) {
                                Exception ex = new Exception(String.format("error on executeDailyPlan due plugin close %s", e.toString()), e);
                                LOGGER.warn(String.format("%s: %s", method, ex.toString()), e);
                            } else {
                                Exception ex = new Exception(String.format("error on executeDailyPlan %s", e.toString()), e);
                                LOGGER.error(String.format("%s: %s", method, ex.toString()), e);
                                getMailer().sendOnError(className, method, ex);
                            }
                        }
                    }
                } else {
                    LOGGER.debug(String.format("%s: skip executeDailyPlan: 0 reporting changes", method));
                }
            } catch (Exception e) {
                if (isClosed()) {
                    Exception ex = new Exception(String.format("error on executeFacts due plugin close %s", e.toString()), e);
                    LOGGER.warn(String.format("%s: %s", method, ex.toString()), e);
                } else {
                    Exception ex = new Exception(String.format("error on executeFacts %s", e.toString()), e);
                    LOGGER.error(String.format("%s: %s", method, ex.toString()), e);
                    getMailer().sendOnError(className, method, ex);
                }
            }

        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
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

    @Override
    public void close() {
        super.close();

        closeReportingConnectionFactory();
        closeSchedulerConnectionFactory();
    }

    private void initConnectionFactories() throws Exception {
        createReportingConnectionFactory(getSettings().getHibernateConfigurationReporting());
        createSchedulerConnectionFactory(getSettings().getHibernateConfigurationScheduler());
    }

    private FactJobOptions createFactOptions(boolean executeNotificationPlugin) {
        FactJobOptions options = new FactJobOptions();
        options.current_scheduler_id.setValue(getSettings().getSchedulerId());
        options.current_scheduler_hostname.setValue(getSettings().getHost());
        options.current_scheduler_http_port.setValue(getSettings().getHttpPort());
        options.hibernate_configuration_file.setValue(getSettings().getHibernateConfigurationReporting().toString());
        options.hibernate_configuration_file_scheduler.setValue(getSettings().getHibernateConfigurationScheduler().toString());
        options.max_history_age.setValue("30m");
        options.force_max_history_age.value(false);
        options.execute_notification_plugin.setValue(String.valueOf(executeNotificationPlugin));
        return options;
    }

    private void executeDailyPlan(SOSHibernateSession reportingSession, String customEventValue) throws Exception {
        String method = "executeDailyPlan";
        try {
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
                LOGGER.debug(String.format("%s: daily plan was updated", method));
                addCustomEventValue(CUSTOM_EVENT_KEY, CustomEventType.DailyPlanChanged.name(), customEventValue);
            } else {
                LOGGER.debug(String.format("%s: daily plan was not updated", method));
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

    private void createReportingConnectionFactory(Path configFile) throws Exception {
        reportingFactory = new SOSHibernateFactory(configFile);
        reportingFactory.setIdentifier("reporting");
        reportingFactory.setAutoCommit(false);
        reportingFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        reportingFactory.addClassMapping(DBLayer.getReportingClassMapping());
        reportingFactory.addClassMapping(DBLayer.getInventoryClassMapping());
        reportingFactory.addClassMapping(com.sos.jitl.notification.db.DBLayer.getNotificationClassMapping());
        reportingFactory.build();
    }

    private void createSchedulerConnectionFactory(Path configFile) throws Exception {
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

    private void closeReportingConnectionFactory() {
        if (reportingFactory != null) {
            reportingFactory.close();
            reportingFactory = null;
        }
    }

    private void closeSchedulerConnectionFactory() {
        if (schedulerFactory != null) {
            schedulerFactory.close();
            schedulerFactory = null;
        }
    }

}
