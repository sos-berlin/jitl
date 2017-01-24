package com.sos.jitl.reporting.plugin;

import java.io.File;
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
	private SOSHibernateFactory reportingFactory;
	private SOSHibernateFactory schedulerFactory;
	private SOSHibernateConnection reportingConnection;	
	private SOSHibernateConnection schedulerConnection;
	// wait iterval after db executions in seconds
	private int waitInterval = 15;
	private ArrayList<String> observedEventTypes;
	private String createDailyPlanJobChain = "/sos/dailyplan/CreateDailyPlan";

	public FactEventHandler() {
		setPathParamForEventId("/not_exists/");
	}

	@Override
	public void onPrepare(SchedulerXmlCommandExecutor xmlExecutor, VariableSet variables, SchedulerAnswer answer) {
		super.onPrepare(xmlExecutor, variables, answer);

		try {
			initConnections();
			initObservedEvents();
			initFactOptions();
			initDailyPlanOptions();
		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
		}
	}

	@Override
	public void onActivate() {
		super.onActivate();

		start();
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

					try {
						executeFacts();
					} catch (Exception e) {
						LOGGER.error(String.format("error on executeFacts: %s", e.toString()), e);
					}

					if (createDailyPlanEvents.size() > 0 && !createDailyPlanEvents.contains(EventType.TaskEnded.name())
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
		DailyPlanAdjustment dailyPlanAdjustment = new DailyPlanAdjustment(
				new File(dailyPlanOptions.configuration_file.getValue()));

		try {
			dailyPlanAdjustment.setOptions(dailyPlanOptions);
			dailyPlanAdjustment.setTo(new Date());
			dailyPlanAdjustment.beginTransaction();
			dailyPlanAdjustment.adjustWithHistory();
			dailyPlanAdjustment.commit();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			dailyPlanAdjustment.rollback();
			throw new Exception(e);
		} finally {
			try {
				dailyPlanAdjustment.disconnect();
			} catch (Exception e) {
			}
		}
	}

	private void tryDbConnect(SOSHibernateConnection conn) throws Exception {
		if (conn.getJdbcConnection() == null || conn.getJdbcConnection().isClosed()) {
			conn.connect();
		}
	}

	private void tryDbDisconnect(SOSHibernateConnection conn) throws Exception {
		if (conn.getJdbcConnection() != null && !conn.getJdbcConnection().isClosed()) {
			conn.disconnect();
		}
	}

	private void createReportingConnection(Path configFile) throws Exception {
	    this.reportingFactory = new SOSHibernateFactory(configFile);
	    this.reportingFactory.setConnectionIdentifier("reporting");
	    this.reportingFactory.setAutoCommit(false);
	    this.reportingFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
	    this.reportingFactory.setIgnoreAutoCommitTransactions(true);
	    this.reportingFactory.addClassMapping(DBLayer.getReportingClassMapping());
	    this.reportingFactory.addClassMapping(DBLayer.getInventoryClassMapping());
	    this.reportingFactory.open();
	    
		this.reportingConnection = new SOSHibernateStatelessConnection(this.reportingFactory);
		this.reportingConnection.setConnectionIdentifier(this.reportingFactory.getConnectionIdentifier());
	}
	
	private void createSchedulerConnection(Path configFile) throws Exception {
	    this.schedulerFactory = new SOSHibernateFactory(configFile);
	    this.schedulerFactory.setConnectionIdentifier("scheduler");
	    this.schedulerFactory.setAutoCommit(false);
	    this.schedulerFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
	    this.schedulerFactory.setIgnoreAutoCommitTransactions(true);
	    this.schedulerFactory.addClassMapping(DBLayer.getSchedulerClassMapping());
	    this.schedulerFactory.open();
	    
        this.schedulerConnection = new SOSHibernateStatelessConnection (this.schedulerFactory);
        this.schedulerConnection.setConnectionIdentifier(this.schedulerFactory.getConnectionIdentifier());
	}
	
	private void destroyReportingConnection(){
		this.reportingConnection.disconnect();
		this.reportingFactory.close();
		
		this.reportingConnection = null;
		this.reportingFactory = null;
	}
	
	private void destroySchedulerConnection(){
		this.schedulerConnection.disconnect();
		this.schedulerFactory.close();
		
		this.schedulerConnection = null;
		this.schedulerFactory = null;
	}
	
}
