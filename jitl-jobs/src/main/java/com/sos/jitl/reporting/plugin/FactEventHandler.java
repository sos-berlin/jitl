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
	private SOSHibernateFactory reportingFactory;
	private SOSHibernateFactory schedulerFactory;
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
		String method = "onPrepare";
		initObservedEvents();
		initFactOptions();
		initDailyPlanOptions();

		hasErrorOnPrepare = false;
		try {
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
			start();
		}
	}

	@Override
	public void onNonEmptyEvent(Long eventId, String type, JsonArray events) {
		LOGGER.debug(String.format("onNonEmptyEvent: eventId=%s, type=%s", eventId, type));

		SOSHibernateStatelessConnection reportingConnection = null;
		SOSHibernateStatelessConnection schedulerConnection = null;
		boolean executeFacts = false;
		try {
			ArrayList<String> createDailyPlanEvents = new ArrayList<String>();
			if (events != null && events.size() > 0) {
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
				reportingConnection = createConnection(this.reportingFactory);
				schedulerConnection = createConnection(this.schedulerFactory);
				try {
					executeFacts(reportingConnection, schedulerConnection);

					if (createDailyPlanEvents.size() > 0 && !createDailyPlanEvents.contains(EventType.TaskEnded.name())
							&& !createDailyPlanEvents.contains(EventType.TaskClosed.name())) {

						LOGGER.debug(String.format("skip executeDailyPlan: found not ended %s events",
								createDailyPlanJobChain));
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

			} else {
				LOGGER.debug(String.format("skip: not found observed events"));
			}
		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
		} finally {
			closeConnection(reportingConnection);
			closeConnection(schedulerConnection);
			if (executeFacts) {
				wait(waitInterval);
			}
		}
		super.onNonEmptyEvent(eventId, type, events);
	}

	@Override
	public void onEmptyEvent(Long eventId) {
		super.onEmptyEvent(eventId);
	}

	@Override
	public void onRestart(Long eventId, String type, JsonArray events) {
		LOGGER.debug(String.format("onRestart: eventId=%s, type=%s", eventId, type));
	}

	@Override
	public void close() {
		super.close();

		closeReportingFactory();
		closeSchedulerFactory();

		this.factOptions = null;
		this.dailyPlanOptions = null;
	}

	private void initFactories() throws Exception {
		createReportingFactory(getSchedulerAnswer().getHibernateConfigPath());
		createSchedulerFactory(getSchedulerAnswer().getHibernateConfigPath());
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

	private void executeFacts(SOSHibernateStatelessConnection rc, SOSHibernateStatelessConnection sc) throws Exception {
		FactModel model = new FactModel(rc, sc, factOptions);
		model.process();
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
		this.reportingFactory.setIgnoreAutoCommitTransactions(true);
		this.reportingFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		this.reportingFactory.addClassMapping(DBLayer.getReportingClassMapping());
		this.reportingFactory.addClassMapping(DBLayer.getInventoryClassMapping());
		this.reportingFactory.build();
	}

	private void createSchedulerFactory(Path configFile) throws Exception {
		this.schedulerFactory = new SOSHibernateFactory(configFile);
		this.schedulerFactory.setConnectionIdentifier("scheduler");
		this.schedulerFactory.setAutoCommit(false);
		this.schedulerFactory.setIgnoreAutoCommitTransactions(true);
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
