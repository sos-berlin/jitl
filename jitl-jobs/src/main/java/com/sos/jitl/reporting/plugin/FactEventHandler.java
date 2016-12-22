package com.sos.jitl.reporting.plugin;

import java.sql.SQLException;

import javax.json.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class FactEventHandler extends ReportingEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactEventHandler.class);
	
	@Override
	public void onPrepare(SchedulerXmlCommandExecutor xmlExecutor,
			VariableSet variables, SOSHibernateConnection conn,
			SchedulerAnswer answer) throws Exception {
		super.onPrepare(xmlExecutor, variables, conn, answer);
	}

	@Override
	public void onActivate() throws Exception {

		//getConnection().connect();
		createHttpClient();

		start(EventType.TaskEvent);

		// if not connected reconnect
		// getConnection().reconnect();

	}

	@Override
	public void onNonEmptyEvent(EventType eventType, Long eventId, String type,
			JsonArray events) {

		LOGGER.info("onNonEmptyEvent: eventType = "+eventType+", eventId = "+eventId);
				
		try {
			if(getConnection().getJdbcConnection() == null ||
					getConnection().getJdbcConnection().isClosed()){
				getConnection().connect();
			}
			
			
			super.onNonEmptyEvent(eventType, eventId, type, events);
		}
		catch(Exception ex){
			LOGGER.error(String.format("%s",ex.toString()));
		}
	}

	@Override
	public void onEmptyEvent(EventType eventType, Long eventId) {
		
		LOGGER.info("onEmptyEvent: eventType = "+eventType+", eventId = "+eventId);
		try{
			if(getConnection().getJdbcConnection() != null &&
					!getConnection().getJdbcConnection().isClosed()){
				getConnection().disconnect();
			}
			
			super.onEmptyEvent(eventType, eventId);
		}
		catch(Exception ex){
			LOGGER.error(String.format("%s",ex.toString()));
		}
	}
	
	
	@Override
	public void onRestart(EventType eventType,Long eventId) {
		LOGGER.info("onRestart: eventType = "+eventType+", eventId = "+eventId);
		
	}

	@Override
	public void close() throws Exception {
		closeHttpClient();
		getConnection().disconnect();
	}

}
