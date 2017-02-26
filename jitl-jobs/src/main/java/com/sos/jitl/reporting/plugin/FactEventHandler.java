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
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.jitl.dailyplan.db.DailyPlanAdjustment;
import com.sos.jitl.dailyplan.job.CheckDailyPlanOptions;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.job.report.FactJobOptions;
import com.sos.jitl.reporting.model.report.FactModel;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class FactEventHandler extends JobSchedulerPluginEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactEventHandler.class);

	private final String className = FactEventHandler.class.getSimpleName();
	private static final String JOB_CHAIN_CREATE_DAILY_PLAN = "/sos/dailyplan/CreateDailyPlan";
	private SOSHibernateFactory reportingFactory;
	private SOSHibernateFactory schedulerFactory;
	// wait iterval after db executions in seconds
	private int waitInterval = 10;
	private boolean useNotificationPlugin = false;

	public FactEventHandler(boolean useNotification) {
		useNotificationPlugin = useNotification;
	}

	@Override
	public void onPrepare(SchedulerXmlCommandExecutor xmlExecutor, EventHandlerSettings settings) {
		super.onPrepare(xmlExecutor, settings);
	}

	@Override
	public void onActivate(PluginMailer mailer) {
		super.onActivate(mailer);

		String method = "onActivate";
		try {
			initConnectionFactories();

			EventType[] observedEventTypes = new EventType[] { EventType.TaskStarted, EventType.TaskEnded,
					EventType.OrderStepStarted, EventType.OrderStepEnded, EventType.OrderFinished };
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

		SOSHibernateStatelessConnection reportingConnection = null;
		SOSHibernateStatelessConnection schedulerConnection = null;
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

			reportingConnection = createConnection(this.reportingFactory);
			schedulerConnection = createConnection(this.schedulerFactory);
			try {
				factModel = new FactModel(reportingConnection, schedulerConnection,
						createFactOptions(useNotificationPlugin));
				factModel.init(getMailer(), getSettings().getConfigDirectory());
				factModel.process();

				if (createDailyPlanEvents.size() > 0 && !createDailyPlanEvents.contains(EventType.TaskEnded.name())
						&& !createDailyPlanEvents.contains(EventType.TaskClosed.name())) {

					LOGGER.debug(String.format("skip executeDailyPlan: found not ended %s events",
							JOB_CHAIN_CREATE_DAILY_PLAN));
				} else {
					try {
						LOGGER.debug(String.format("executeDailyPlan ..."));
						executeDailyPlan(reportingConnection);
					} catch (Exception e) {
						if (isClosed()) {
							Exception ex = new Exception(
									String.format("error on executeDailyPlan due plugin close %s", e.toString()), e);
							LOGGER.warn(String.format("%s: %s", method, ex.toString()), e);
						} else {
							Exception ex = new Exception(String.format("error on executeDailyPlan %s", e.toString()),
									e);
							LOGGER.error(String.format("%s: %s", method, ex.toString()), e);
							getMailer().sendOnError(className, method, ex);
						}
					}
				}
			} catch (Exception e) {
				if (isClosed()) {
					Exception ex = new Exception(
							String.format("error on executeFacts due plugin close %s", e.toString()), e);
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
			if (factModel != null) {
				factModel.exit();
			}
			closeConnection(reportingConnection);
			closeConnection(schedulerConnection);
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
		options.hibernate_configuration_file_scheduler
				.setValue(getSettings().getHibernateConfigurationScheduler().toString());
		options.max_history_age.setValue("30m");
		options.force_max_history_age.value(false);
		options.execute_notification_plugin.setValue(String.valueOf(executeNotificationPlugin));
		return options;
	}

	private void executeDailyPlan(SOSHibernateStatelessConnection rc) throws Exception {
		String method = "executeDailyPlan";
		try {
			CheckDailyPlanOptions options = new CheckDailyPlanOptions();
			options.scheduler_id.setValue(getSettings().getSchedulerId());
			options.dayOffset.setValue("0");
			try {
				options.configuration_file
						.setValue(getSettings().getHibernateConfigurationReporting().toFile().getCanonicalPath());
			} catch (Exception e) {
			}

			DailyPlanAdjustment dp = new DailyPlanAdjustment(rc);
			dp.setOptions(options);
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

	private void createReportingConnectionFactory(Path configFile) throws Exception {
		reportingFactory = new SOSHibernateFactory(configFile);
		reportingFactory.setConnectionIdentifier("reporting");
		reportingFactory.setAutoCommit(false);
		reportingFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		reportingFactory.addClassMapping(DBLayer.getReportingClassMapping());
		reportingFactory.addClassMapping(DBLayer.getInventoryClassMapping());
		reportingFactory.addClassMapping(com.sos.jitl.notification.db.DBLayer.getNotificationClassMapping());
		reportingFactory.build();
	}

	private void createSchedulerConnectionFactory(Path configFile) throws Exception {
		schedulerFactory = new SOSHibernateFactory(configFile);
		schedulerFactory.setConnectionIdentifier("scheduler");
		schedulerFactory.setAutoCommit(false);
		schedulerFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
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
