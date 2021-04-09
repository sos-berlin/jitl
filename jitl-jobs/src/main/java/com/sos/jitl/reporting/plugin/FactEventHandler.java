package com.sos.jitl.reporting.plugin;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sos.hibernate.classes.SOSHibernate;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.dailyplan.db.DailyPlanAdjustment;
import com.sos.jitl.dailyplan.job.CheckDailyPlanOptions;
import com.sos.jitl.eventhandler.EventMeta.EventKey;
import com.sos.jitl.eventhandler.EventMeta.EventType;
import com.sos.jitl.eventhandler.handler.LoopEventHandler;
import com.sos.jitl.eventhandler.plugin.notifier.Notifier;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.exceptions.SOSReportingConcurrencyException;
import com.sos.jitl.reporting.exceptions.SOSReportingInvalidSessionException;
import com.sos.jitl.reporting.exceptions.SOSReportingLockException;
import com.sos.jitl.reporting.job.report.FactJobOptions;
import com.sos.jitl.reporting.model.report.FactModel;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class FactEventHandler extends LoopEventHandler {

    public static enum CustomEventType {
        DailyPlanChanged, ReportingChanged, YADETransferFinished // see yade-engine YadeHistory
    }

    public static enum CustomEventTypeValue {
        order, order_standalone, standalone
    }

    public static final String CUSTOM_EVENT_KEY = FactEventHandler.class.getSimpleName();;
    private static final String JOB_CHAIN_CREATE_DAILY_PLAN = "/sos/dailyplan/CreateDailyPlan";;
    private static final Logger LOGGER = LoggerFactory.getLogger(FactEventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private String customEventValue = null;
    private boolean rerun = false;
    private boolean firstEventProcessed = false;
    private SOSHibernateFactory reportingFactory;
    private SOSHibernateFactory schedulerFactory;
    private boolean useNotificationPlugin = false;
    // wait interval after db executions in seconds
    private int waitInterval = 2;
    private Notifier pluginNotifier;

    public FactEventHandler(SchedulerXmlCommandExecutor xmlExecutor, EventPublisher eventBus) {
        super(xmlExecutor, eventBus);
    }

    @Override
    public void onActivate(Notifier notifier) {
        super.onActivate(notifier);

        String method = "onActivate";
        try {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]create db factories...", method));
            }
            createReportingFactory(getSettings().getHibernateConfigurationReporting());
            createSchedulerFactory(getSettings().getHibernateConfigurationScheduler());

            if (useNotificationPlugin) {
                pluginNotifier = getNotifier().newInstance();
            }

            EventType[] observedEventTypes = new EventType[] { EventType.TaskStarted, EventType.TaskClosed, EventType.OrderStepStarted,
                    EventType.OrderStepEnded, EventType.OrderFinished, EventType.OrderRemoved, EventType.OrderResumed, EventType.OrderWaitingInTask };

            LOGGER.info(String.format("[%s]start...", method));
            start(observedEventTypes);
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
            getNotifier().notifyOnError(method, e);
        }
    }

    @Override
    public void onProcessingEnd(Long eventId) {
        if (isDebugEnabled) {
            LOGGER.debug("[onProcessingEnd]close db factories ...");
        }
        closeSchedulerFactory();
        closeReportingFactory();
        LOGGER.info("[onProcessingEnd]closed");
    }

    @Override
    public void onEmptyEvent(Long eventId) {
        if (rerun || !firstEventProcessed) {
            if (isDebugEnabled) {
                if (firstEventProcessed) {
                    LOGGER.debug(String.format("[onEmptyEvent][rerun]%s", eventId));
                } else {
                    LOGGER.debug(String.format("[onEmptyEvent][firstEvent]%s", eventId));
                }
            }
            execute(eventId, null, false);
        }
    }

    @Override
    public void onNonEmptyEvent(Long eventId, JsonArray events) {
        rerun = false;
        execute(eventId, events, true);
    }

    private void execute(Long eventId, JsonArray events, boolean onNonEmptyEvent) {
        String method = "execute";
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][onNonEmptyEvent=%s]", method, eventId, onNonEmptyEvent));
        }
        SOSHibernateSession reportingSession = null;
        SOSHibernateSession schedulerSession = null;
        FactModel factModel = null;
        int waitIntervalOnFinally = waitInterval;
        customEventValue = null;
        firstEventProcessed = true;

        try {
            reportingSession = reportingFactory.openStatelessSession();
            schedulerSession = schedulerFactory.openStatelessSession();

            factModel = executeFacts(reportingSession, schedulerSession, events, useNotificationPlugin);
            if (factModel.isLocked()) {
                rerun = true;
            } else {
                executeDailyPlan(reportingSession, factModel.isChanged(), events);
                rerun = false;
            }
            if (getNotifier().smartNotifyOnRecovery()) {
                LOGGER.info(String.format("[%s]recovered from previous error", method));
            }
        } catch (SOSReportingConcurrencyException e) {
            rerun = true;
            LOGGER.warn(String.format("[%s]%s", method, e.toString()), e);
        } catch (SOSReportingLockException e) {
            rerun = true;
            LOGGER.warn(String.format("[%s]%s", method, e.toString()), e);
        } catch (SOSReportingInvalidSessionException e) {
            rerun = true;
            waitIntervalOnFinally = getWaitIntervalOnError();
            getNotifier().smartNotifyOnError(getClass(), e);
        } catch (Throwable e) {
            rerun = true;
            getNotifier().smartNotifyOnError(getClass(), e);
        } finally {
            if (factModel != null) {
                factModel.exit();
            }
            if (reportingSession != null) {
                reportingSession.close();
            }
            if (schedulerSession != null) {
                schedulerSession.close();
            }
            wait(waitIntervalOnFinally);
        }
    }

    private void executeDailyPlan(SOSHibernateSession reportingSession, boolean hasReportingChanges, JsonArray events) throws Exception {
        String method = "executeDailyPlan";
        if (events == null) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip]events is null", method));
            }
            return;
        }
        if (!hasReportingChanges) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip]0 reporting changes", method));
            }
            return;
        }
        ArrayList<String> createDailyPlanEvents = getCreateDailyPlanEvents(events);
        if (createDailyPlanEvents.size() > 0 && !createDailyPlanEvents.contains(EventType.TaskEnded.name()) && !createDailyPlanEvents.contains(
                EventType.TaskClosed.name())) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip]found not ended %s events", method, JOB_CHAIN_CREATE_DAILY_PLAN));
            }
            return;
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s]execute ...", method));
        }
        CheckDailyPlanOptions options = new CheckDailyPlanOptions();
        options.scheduler_id.setValue(getSettings().getSchedulerId());
        options.dayOffset.setValue("0");
        try {
            options.configuration_file.setValue(getSettings().getHibernateConfigurationReporting().toFile().getCanonicalPath());
        } catch (Exception e) {
        }
        String pluginContext = MDC.get("plugin");
        MDC.put("plugin", "dailyplan");
        DailyPlanAdjustment dp = new DailyPlanAdjustment(reportingSession);
        dp.setOptions(options);
        dp.setTo(new Date());

        int count = 0;
        boolean run = true;
        while (run) {
            count++;
            try {
                dp.adjustWithHistory();
                run = false;
            } catch (Exception e) {
                Exception lae = SOSHibernate.findLockException(e);
                if (lae == null) {
                    throw e;
                } else {
                    if (count >= FactModel.MAX_RERUNS) {
                        throw new SOSReportingLockException(e);
                    } else {
                        LOGGER.warn(String.format("[%s]%s occured, wait %ss and try again (%s of %s) ...", method, lae.getClass().getName(),
                                FactModel.RERUN_INTERVAL, count, FactModel.MAX_RERUNS));
                        Thread.sleep(FactModel.RERUN_INTERVAL * 1000);
                    }
                }
            }
        }

        if (dp.isDailyPlanUpdated()) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]daily plan was changed, publish CustomEventType.DailyPlanChanged event", method));
            }
            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.DailyPlanChanged.name(), customEventValue);
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]daily plan was not changed", method));
            }
        }
        if (pluginContext != null) {
            MDC.put("plugin", pluginContext);
        } else {
            MDC.remove("plugin");
        }
    }

    private FactModel executeFacts(SOSHibernateSession reportingSession, SOSHibernateSession schedulerSession, JsonArray events,
            boolean executeNotificationPlugin) throws Exception {
        LOGGER.debug("[executeFacts]execute ...");

        FactModel factModel = null;
        FactJobOptions options = new FactJobOptions();
        options.current_scheduler_id.setValue(getSettings().getSchedulerId());
        options.current_scheduler_hostname.setValue(getSettings().getHost());
        options.current_scheduler_http_port.setValue(getSettings().getHttpPort());
        options.hibernate_configuration_file.setValue(getSettings().getHibernateConfigurationReporting().toString());
        options.hibernate_configuration_file_scheduler.setValue(getSettings().getHibernateConfigurationScheduler().toString());
        options.max_history_tasks.setValue("200000");
        options.max_history_age.setValue("1h");
        options.force_max_history_age.value(false);
        options.execute_notification_plugin.setValue(String.valueOf(executeNotificationPlugin));
        options.wait_interval.setValue(String.valueOf(waitInterval));

        factModel = new FactModel(reportingSession, schedulerSession, options, events);
        factModel.init(pluginNotifier, getSettings().getConfigDirectory());
        factModel.process();

        if (factModel.isChanged()) {
            if (factModel.isOrdersChanged() && factModel.isTasksChanged()) {
                customEventValue = CustomEventTypeValue.order_standalone.name();
            } else if (factModel.isOrdersChanged()) {
                customEventValue = CustomEventTypeValue.order.name();
            } else if (factModel.isTasksChanged()) {
                customEventValue = CustomEventTypeValue.standalone.name();
            }
            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.ReportingChanged.name(), customEventValue);
            publishTransferHistory(factModel);
        }
        return factModel;
    }

    private void publishTransferHistory(FactModel factModel) {
        if (factModel.getTransferHistory().getTransferIds().size() > 0) {
            Iterator<Long> it = factModel.getTransferHistory().getTransferIds().iterator();
            while (it.hasNext()) {
                publishCustomEvent(CustomEventType.YADETransferFinished.name(), "transferId", String.valueOf(it.next()));
            }
            factModel.getTransferHistory().resetTransferIds();
        }
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

    public void setUseNotificationPlugin(boolean useNotification) {
        useNotificationPlugin = useNotification;
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

    private void createReportingFactory(Path configFile) throws Exception {
        reportingFactory = new SOSHibernateFactory(configFile);
        reportingFactory.setIdentifier("reporting");
        reportingFactory.setAutoCommit(false);
        reportingFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        reportingFactory.addClassMapping(DBLayer.getReportingClassMapping());
        reportingFactory.addClassMapping(DBLayer.getInventoryClassMapping());
        reportingFactory.addClassMapping(DBLayer.getYadeClassMapping());
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
}
