package com.sos.jitl.reporting.plugin;

import javax.json.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class FactEventHandler extends ReportingEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactEventHandler.class);

	@Override
	public void onPrepare(SchedulerXmlCommandExecutor xmlExecutor, VariableSet variables, SchedulerAnswer answer,
			SOSHibernateConnection reportingConn, SOSHibernateConnection schedulerConn) {
		super.onPrepare(xmlExecutor, variables, answer, reportingConn, schedulerConn);
	}

	@Override
	public void onActivate() {

		createRestApiClient();
		try {
			//start(Overview.OrderOverview, new EventType[]{ EventType.OrderStarted });
			start(new EventType[] { EventType.OrderStepEnded });
		} catch (Exception e) {
			close();
		}
	}

	@Override
	public void onNonEmptyEvent(Overview overview, EventType[] eventTypes, Long eventId, String type,
			JsonArray events) {

		LOGGER.info(String.format("onNonEmptyEvent: overview=%s, eventTypes=%s, eventId=%s", overview,
				joinEventTypes(eventTypes), eventId));

		try {
			tryDbConnect(getReportingConnection());
			tryDbConnect(getSchedulerConnection());

			super.onNonEmptyEvent(overview, eventTypes, eventId, type, events);
		} catch (Exception ex) {
			LOGGER.error(String.format("%s", ex.toString()));
		}
	}

	@Override
	public void onEmptyEvent(Overview overview, EventType[] eventTypes, Long eventId) {

		LOGGER.info(String.format("onEmptyEvent: overview=%s, eventTypes=%s, eventId=%s", overview,
				joinEventTypes(eventTypes), eventId));
		try {
			tryDbDisconnect(getReportingConnection());
			tryDbDisconnect(getSchedulerConnection());

			super.onEmptyEvent(overview, eventTypes, eventId);
		} catch (Exception ex) {
			LOGGER.error(String.format("%s", ex.toString()));
		}
	}

	@Override
	public void onRestart(Overview overview, EventType[] eventTypes, Long eventId) {
		LOGGER.info(String.format("onRestart: overview=%s, eventTypes=%s, eventId=%s", overview,
				joinEventTypes(eventTypes), eventId));

	}

	@Override
	public void close() {
		closeRestApiClient();
		getReportingConnection().disconnect();
		getSchedulerConnection().disconnect();
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
}
