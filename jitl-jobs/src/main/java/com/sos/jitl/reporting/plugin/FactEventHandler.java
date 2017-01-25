package com.sos.jitl.reporting.plugin;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateStatelessConnection;
import com.sos.jitl.dailyplan.db.DailyPlanAdjustment;
import com.sos.jitl.dailyplan.job.CheckDailyPlanOptions;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.job.report.FactJobOptions;
import com.sos.jitl.reporting.model.report.FactModel;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class FactEventHandler extends ReportingEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactEventHandler.class);

	private FactJobOptions factOptions;
	private CheckDailyPlanOptions dailyPlanOptions;
	private SOSHibernateConnection reportingConnection;
	private SOSHibernateConnection schedulerConnection;
	// wait iterval after db executions in seconds
	private int waitInterval = 15;
	private ArrayList<String> observedEventTypes;
	private String createDailyPlanJobChain = "/sos/dailyplan/CreateDailyPlan";
	private boolean hasErrorOnPrepare = false;

	public FactEventHandler() {
		setPathParamForEventId("/not_exists/");
	}

	@Override
	public void onPrepare(SchedulerXmlCommandExecutor xmlExecutor, VariableSet variables, SchedulerAnswer answer) {
		super.onPrepare(xmlExecutor, variables, answer);
		String method="onPrepare";
		initObservedEvents();
		initFactOptions();
		initDailyPlanOptions();

		hasErrorOnPrepare = false;
		try {
			initConnections();
		} catch (Exception e) {
			hasErrorOnPrepare = true;
			LOGGER.error(String.format("%s: %s",method,e.toString()), e);
		}
	}

	@Override
	public void onActivate() {
		super.onActivate();

		if (hasErrorOnPrepare) {
			LOGGER.warn(String.format("skip onActivate due onPrepare errors"));
		} else {
			start();
		}
	}

	@Override
	public void onNonEmptyEvent(Long eventId, String type, JsonArray events) {
		LOGGER.debug(String.format("onNonEmptyEvent: eventId=%s, type=%s", eventId, type));

		try {
			try {
				boolean executeFacts = false;
				ArrayList<String> createDailyPlanEvents = new ArrayList<String>();
				if (events != null && events.size() > 0) {
					// for (JsonObject event :
					// events.getJsonArray("eventSnapshots").getValuesAs(JsonObject.class))
					// {
					for (int i = 0; i < events.size(); i++) {
						JsonObject jo = events.getJsonObject(i);
						String joType = jo.getString(EventKey.TYPE.name());

						if (checkEvents(joType)) {
							executeFacts = true;
						}

						String key = getEventKey(jo);
						if (key != null) {
							if (key.toLowerCase().contains(createDailyPlanJobChain.toLowerCase())) {
								createDailyPlanEvents.add(joType);
							}
						}
					}
				}

				if (executeFacts) {
					tryDbConnect(this.reportingConnection);
					tryDbConnect(this.schedulerConnection);

					boolean executeDailyPlan = true;
					try {
						executeFacts();
					} catch (Exception e) {
						executeDailyPlan = false;
						LOGGER.error(String.format("error on executeFacts: %s", e.toString()), e);
					}

					if (executeDailyPlan) {
						if (createDailyPlanEvents.size() > 0
								&& !createDailyPlanEvents.contains(EventType.TaskEnded.name())
								&& !createDailyPlanEvents.contains(EventType.TaskClosed.name())) {

							LOGGER.debug(String.format("skip executeDailyPlan: found not ended %s events",
									createDailyPlanJobChain));
						} else {
							try {
								LOGGER.debug(String.format("executeDailyPlan ..."));
								executeDailyPlan();
							} catch (Exception e) {
								LOGGER.error(String.format("error on executeDailyPlan: %s", e.toString()), e);
							}
						}
					} else {
						LOGGER.debug(String.format("skip executeDailyPlan: due executeFacts errors"));
					}

					wait(waitInterval);
				} else {
					LOGGER.debug(String.format("skip: not found observed events"));
				}
			} catch (Exception e) {
				LOGGER.error(e.toString(), e);
			}
			super.onNonEmptyEvent(eventId, type, events);
		} catch (Exception ex) {
			LOGGER.error(String.format("%s", ex.toString()));
		}
	}

	@Override
	public void onEmptyEvent(Long eventId) {

		LOGGER.debug(String.format("onEmptyEvent: eventId=%s", eventId));
		try {
			try {
				tryDbDisconnect(this.reportingConnection);
				tryDbDisconnect(this.schedulerConnection);
			} catch (Exception e) {

			}
			super.onEmptyEvent(eventId);
		} catch (Exception ex) {
			LOGGER.error(String.format("%s", ex.toString()));
		}
	}

	@Override
	public void onRestart(Long eventId, String type, JsonArray events) {
		LOGGER.debug(String.format("onRestart: eventId=%s, type=%s", eventId, type));
	}

	@Override
	public void close() {
		super.close();

		destroyReportingConnection();
		destroySchedulerConnection();

		this.factOptions = null;
		this.dailyPlanOptions = null;
	}

	private void initConnections() throws Exception {
		createReportingConnection(getSchedulerAnswer().getHibernateConfigPath());
		createSchedulerConnection(getSchedulerAnswer().getHibernateConfigPath());
	}

	private void initObservedEvents() {
		observedEventTypes = new ArrayList<String>();
		observedEventTypes.add(EventType.TaskStarted.name());
		observedEventTypes.add(EventType.TaskEnded.name());
		observedEventTypes.add(EventType.OrderStepStarted.name());
		observedEventTypes.add(EventType.OrderStepEnded.name());
		observedEventTypes.add(EventType.OrderFinished.name());
	}

	private void initFactOptions() {
		factOptions = new FactJobOptions();
		factOptions.current_scheduler_id.setValue(getSchedulerAnswer().getSchedulerId());
		factOptions.current_scheduler_hostname.setValue(getSchedulerAnswer().getHostname());
		factOptions.current_scheduler_http_port.setValue(getSchedulerAnswer().getHttpPort());
		factOptions.max_history_age.setValue("30m");
		factOptions.force_max_history_age.value(false);
	}

	private void initDailyPlanOptions() {
		dailyPlanOptions = new CheckDailyPlanOptions();
		dailyPlanOptions.scheduler_id.setValue(getSchedulerAnswer().getSchedulerId());
		dailyPlanOptions.dayOffset.setValue("1");
		try {
			dailyPlanOptions.configuration_file
					.setValue(getSchedulerAnswer().getHibernateConfigPath().toFile().getCanonicalPath());
		} catch (Exception e) {
		}
	}

	private boolean checkEvents(String type) {
		if (type != null && observedEventTypes.contains(type)) {
			return true;
		}
		return false;
	}

	private void executeFacts() throws Exception {
		FactModel model = new FactModel(this.reportingConnection, this.schedulerConnection, factOptions);
		model.process();
	}

	private void executeDailyPlan() throws Exception {
		try {
			DailyPlanAdjustment dp = new DailyPlanAdjustment(this.reportingConnection);
			dp.setOptions(dailyPlanOptions);
			dp.setTo(new Date());
			this.reportingConnection.beginTransaction();
			dp.adjustWithHistory();
			this.reportingConnection.commit();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			this.reportingConnection.rollback();
			throw new Exception(e);
		}
	}

	private void tryDbConnect(SOSHibernateConnection conn) throws Exception {
		String method = "tryDbConnect";
		if (conn == null) {
			throw new Exception(String.format("%s: connection is NULL", method));
		}
		if (conn.getCurrentSession() == null) {
			conn.connect();
		} else {
			try {
				Connection jdbcConnection = conn.getJdbcConnection();
				if (jdbcConnection.isClosed() || jdbcConnection.isValid(1)) {
					conn.connect();
				}
			} catch (Exception e) {
				LOGGER.warn(String.format("%s[%s]: %s", method, conn.getConnectionIdentifier(), e.toString()), e);
				conn.connect();
			}
		}
	}

	private void tryDbDisconnect(SOSHibernateConnection conn) throws Exception {
		if (conn.getCurrentSession() != null) {
			conn.disconnect();
		}
	}

	private void createReportingConnection(Path configFile) throws Exception {
		SOSHibernateFactory factory = new SOSHibernateFactory(configFile);
		factory.setConnectionIdentifier("reporting");
		factory.setAutoCommit(false);
		factory.setIgnoreAutoCommitTransactions(true);
		factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		factory.addClassMapping(DBLayer.getReportingClassMapping());
		factory.addClassMapping(DBLayer.getInventoryClassMapping());
		factory.build();

		this.reportingConnection = new SOSHibernateStatelessConnection(factory);
		this.reportingConnection.setConnectionIdentifier(factory.getConnectionIdentifier());
	}

	private void createSchedulerConnection(Path configFile) throws Exception {
		SOSHibernateFactory factory = new SOSHibernateFactory(configFile);
		factory.setConnectionIdentifier("scheduler");
		factory.setAutoCommit(false);
		factory.setIgnoreAutoCommitTransactions(true);
		factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		factory.addClassMapping(DBLayer.getSchedulerClassMapping());
		factory.build();

		this.schedulerConnection = new SOSHibernateStatelessConnection(factory);
		this.schedulerConnection.setConnectionIdentifier(factory.getConnectionIdentifier());
	}

	private void destroyReportingConnection() {
		if (this.reportingConnection != null) {
			this.reportingConnection.disconnect();
			this.reportingConnection.getFactory().close();

			this.reportingConnection = null;
		}
	}

	private void destroySchedulerConnection() {
		if (this.schedulerConnection != null) {
			this.schedulerConnection.disconnect();
			this.schedulerConnection.getFactory().close();

			this.schedulerConnection = null;
		}
	}

}
