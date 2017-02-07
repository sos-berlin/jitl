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
import com.sos.hibernate.classes.SOSHibernateStatelessConnection;
import com.sos.jitl.classes.event.EventHandlerSettings;
import com.sos.jitl.classes.event.JobSchedulerPluginEventHandler;
import com.sos.jitl.dailyplan.db.DailyPlanAdjustment;
import com.sos.jitl.dailyplan.job.CheckDailyPlanOptions;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.job.report.FactJobOptions;
import com.sos.jitl.reporting.model.report.FactModel;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class FactEventHandler extends JobSchedulerPluginEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactEventHandler.class);

	private FactJobOptions factOptions;
	private CheckDailyPlanOptions dailyPlanOptions;
	private SOSHibernateFactory reportingFactory;
	private SOSHibernateFactory schedulerFactory;
	// wait iterval after db executions in seconds
	private int waitInterval = 15;
	private EventType[] observedEventTypes;
	private String createDailyPlanJobChain = "/sos/dailyplan/CreateDailyPlan";
	private boolean hasErrorOnPrepare = false;

	private FactModel factModel;
	private boolean executeNotificationPlugin = false;

	public FactEventHandler() {
		this.observedEventTypes = new EventType[] { EventType.TaskStarted, EventType.TaskEnded,
				EventType.OrderStepStarted, EventType.OrderStepEnded, EventType.OrderFinished };
	}

	@Override
	public void onPrepare(SchedulerXmlCommandExecutor xmlExecutor, VariableSet variables,
			EventHandlerSettings settings) {
		super.onPrepare(xmlExecutor, variables, settings);
		String method = "onPrepare";
		initFactOptions();
		initDailyPlanOptions();

		hasErrorOnPrepare = false;
		try {
			factModel = new FactModel(factOptions);
			factModel.init(settings.getConfigDirectory());
			initFactories();
		} catch (Exception e) {
			hasErrorOnPrepare = true;
			LOGGER.error(String.format("%s: %s", method, e.toString()), e);
		}
	}

	@Override
	public void onActivate() {
		super.onActivate();

		if (hasErrorOnPrepare) {
			LOGGER.warn(String.format("skip onActivate due onPrepare errors"));
		} else {
			start(this.observedEventTypes);
		}
	}

	@Override
	public void onNonEmptyEvent(Long eventId, JsonArray events) {
		LOGGER.debug(String.format("onNonEmptyEvent: eventId=%s", eventId));

		SOSHibernateStatelessConnection reportingConnection = null;
		SOSHibernateStatelessConnection schedulerConnection = null;
		try {
			ArrayList<String> createDailyPlanEvents = new ArrayList<String>();
			if (events != null && events.size() > 0) {
				for (int i = 0; i < events.size(); i++) {
					JsonObject jo = events.getJsonObject(i);
					String joType = jo.getString(EventKey.TYPE.name());
					String key = getEventKey(jo);
					if (key != null) {
						if (key.toLowerCase().contains(createDailyPlanJobChain.toLowerCase())) {
							createDailyPlanEvents.add(joType);
						}
					}
				}
			}

			reportingConnection = createConnection(this.reportingFactory);
			schedulerConnection = createConnection(this.schedulerFactory);
			try {
				factModel.setConnections(reportingConnection, schedulerConnection);
				factModel.process();

				if (createDailyPlanEvents.size() > 0 && !createDailyPlanEvents.contains(EventType.TaskEnded.name())
						&& !createDailyPlanEvents.contains(EventType.TaskClosed.name())) {

					LOGGER.debug(
							String.format("skip executeDailyPlan: found not ended %s events", createDailyPlanJobChain));
				} else {
					try {
						LOGGER.debug(String.format("executeDailyPlan ..."));
						executeDailyPlan(reportingConnection);
					} catch (Exception e) {
						LOGGER.error(String.format("error on executeDailyPlan: %s", e.toString()), e);
					}
				}
			} catch (Exception e) {
				LOGGER.error(String.format("error on executeFacts: %s", e.toString()), e);
			}

		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
		} finally {
			closeConnection(reportingConnection);
			closeConnection(schedulerConnection);
			wait(waitInterval);
		}
		super.onNonEmptyEvent(eventId, events);
	}

	@Override
	public void onEmptyEvent(Long eventId) {
		super.onEmptyEvent(eventId);
	}

	@Override
	public void onRestart(Long eventId, JsonArray events) {
		LOGGER.debug(String.format("onRestart: eventId=%s", eventId));
	}

	@Override
	public void close() {
		super.close();
		
		if(factModel != null){
			factModel.exit();
		}
		
		closeReportingFactory();
		closeSchedulerFactory();

		this.factOptions = null;
		this.dailyPlanOptions = null;
	}

	private void initFactories() throws Exception {
		createReportingFactory(getSettings().getHibernateConfigurationReporting());
		createSchedulerFactory(getSettings().getHibernateConfigurationScheduler());
	}

	private void initFactOptions() {
		factOptions = new FactJobOptions();
		factOptions.current_scheduler_id.setValue(getSettings().getSchedulerId());
		factOptions.current_scheduler_hostname.setValue(getSettings().getHost());
		factOptions.current_scheduler_http_port.setValue(getSettings().getHttpPort());
		factOptions.hibernate_configuration_file.setValue(getSettings().getHibernateConfigurationReporting().toString());
		factOptions.hibernate_configuration_file_scheduler.setValue(getSettings().getHibernateConfigurationScheduler().toString());
		factOptions.max_history_age.setValue("30m");
		factOptions.force_max_history_age.value(false);
		factOptions.execute_notification_plugin.setValue(String.valueOf(executeNotificationPlugin));
	}

	private void initDailyPlanOptions() {
		dailyPlanOptions = new CheckDailyPlanOptions();
		dailyPlanOptions.scheduler_id.setValue(getSettings().getSchedulerId());
		dailyPlanOptions.dayOffset.setValue("1");
		try {
			dailyPlanOptions.configuration_file
					.setValue(getSettings().getHibernateConfigurationReporting().toFile().getCanonicalPath());
		} catch (Exception e) {
		}
	}

	private void executeDailyPlan(SOSHibernateStatelessConnection rc) throws Exception {
		String method = "executeDailyPlan";
		try {
			DailyPlanAdjustment dp = new DailyPlanAdjustment(rc);
			dp.setOptions(dailyPlanOptions);
			dp.setTo(new Date());
			rc.beginTransaction();
			dp.adjustWithHistory();
			rc.commit();
		} catch (Exception e) {
			try {
				rc.rollback();
			} catch (Exception ex) {
				LOGGER.warn(String.format("%s: %s", method, ex.toString()), ex);
			}
			throw new Exception(String.format("%s: %s", method, e.toString()), e);
		}
	}

	private SOSHibernateStatelessConnection createConnection(SOSHibernateFactory factory) throws Exception {
		SOSHibernateStatelessConnection conn = new SOSHibernateStatelessConnection(factory);
		conn.setConnectionIdentifier(factory.getConnectionIdentifier());
		conn.connect();
		return conn;
	}

	private void closeConnection(SOSHibernateStatelessConnection conn) {
		if (conn != null) {
			conn.disconnect();
		}
	}

	private void createReportingFactory(Path configFile) throws Exception {
		this.reportingFactory = new SOSHibernateFactory(configFile);
		this.reportingFactory.setConnectionIdentifier("reporting");
		this.reportingFactory.setAutoCommit(false);
		this.reportingFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		this.reportingFactory.addClassMapping(DBLayer.getReportingClassMapping());
		this.reportingFactory.addClassMapping(DBLayer.getInventoryClassMapping());
		this.reportingFactory.build();
	}

	private void createSchedulerFactory(Path configFile) throws Exception {
		this.schedulerFactory = new SOSHibernateFactory(configFile);
		this.schedulerFactory.setConnectionIdentifier("scheduler");
		this.schedulerFactory.setAutoCommit(false);
		this.schedulerFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		this.schedulerFactory.addClassMapping(DBLayer.getSchedulerClassMapping());
		this.schedulerFactory.build();
	}

	private void closeReportingFactory() {
		if (this.reportingFactory != null) {
			this.reportingFactory.close();
			this.reportingFactory = null;
		}
	}

	private void closeSchedulerFactory() {
		if (this.schedulerFactory != null) {
			this.schedulerFactory.close();
			this.schedulerFactory = null;
		}
	}

}
