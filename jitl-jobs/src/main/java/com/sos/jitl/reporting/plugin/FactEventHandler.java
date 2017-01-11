package com.sos.jitl.reporting.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.dailyplan.db.DailyPlanAdjustment;
import com.sos.jitl.dailyplan.job.CheckDailyPlanOptions;
import com.sos.jitl.reporting.job.report.FactJobOptions;
import com.sos.jitl.reporting.model.report.FactModel;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class FactEventHandler extends ReportingEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactEventHandler.class);
	
	private FactJobOptions factOptions;
	private CheckDailyPlanOptions dailyPlanOptions;
	
	//wait iterval after db executions in seconds
	private int waitInterval = 30;
	private ArrayList<String> observedEventTypes;
	private String createDailyPlanJobChain = "/sos/dailyplan/CreateDailyPlan";
	
	@Override
	public void onPrepare(SchedulerXmlCommandExecutor xmlExecutor, VariableSet variables, SchedulerAnswer answer,
			SOSHibernateConnection reportingConn, SOSHibernateConnection schedulerConn) {
		super.onPrepare(xmlExecutor, variables, answer, reportingConn, schedulerConn);

		initObservedEvents();
		initFactOptions();
		initDailyPlanOptions();
	}
	
	@Override
	public void onActivate() {

		createRestApiClient();
		try {
			//start(Overview.JobChainOverview, new EventType[]{ EventType.JobChainEvent });
			//start(new EventType[] { EventType.OrderEvent });
			start();
		} catch (Exception e) {
			close();
		}
	}

	@Override
	public void onNonEmptyEvent(Overview overview, EventType[] eventTypes, Long eventId, String type,
			JsonArray events) {

		LOGGER.debug(String.format("onNonEmptyEvent: overview=%s, eventTypes=%s, eventId=%s", overview,
				joinEventTypes(eventTypes), eventId));

		try {
			try{
				boolean executeFacts = false;
				ArrayList<String> createDailyPlanEvents = new ArrayList<String>();
				if(events != null && events.size() > 0){
					//for (JsonObject event : events.getJsonArray("eventSnapshots").getValuesAs(JsonObject.class)) {
					for (int i = 0; i < events.size(); i++) {
						JsonObject jo = events.getJsonObject(i);
						String joType = jo.getString(EventKey.TYPE.name());
						
						if(checkEvents(joType)){
							executeFacts = true;
						}
						
						String key = getEventKey(jo);
						if(key != null){
							if(key.toLowerCase().contains(createDailyPlanJobChain.toLowerCase())){
								createDailyPlanEvents.add(joType);
							}
						}
					}
				}
				
				if(executeFacts){
					tryDbConnect(getReportingConnection());
					tryDbConnect(getSchedulerConnection());
				
					executeFacts();
					
					if(createDailyPlanEvents.size() > 0 
						&&	!createDailyPlanEvents.contains(EventType.TaskEnded.name())
						&&	!createDailyPlanEvents.contains(EventType.TaskClosed.name())){
				
						LOGGER.debug(String.format("skip execute dailyPlan: found %s events",createDailyPlanJobChain));
					}
					else{
						executeDailyPlan();
					}
					
					if(waitInterval > 0){
						LOGGER.debug(String.format("waiting %s seconds ...", waitInterval));
						Thread.sleep(waitInterval*1000);
					}
				}
				else{
					LOGGER.debug(String.format("skip: not found observed events"));
				}
			}
			catch(Exception e){
				LOGGER.error(e.toString(),e);
			}
			super.onNonEmptyEvent(overview, eventTypes, eventId, type, events);
		} catch (Exception ex) {
			LOGGER.error(String.format("%s", ex.toString()));
		}
	}

	@Override
	public void onEmptyEvent(Overview overview, EventType[] eventTypes, Long eventId) {

		LOGGER.debug(String.format("onEmptyEvent: overview=%s, eventTypes=%s, eventId=%s", overview,
				joinEventTypes(eventTypes), eventId));
		try {
			try{
				tryDbDisconnect(getReportingConnection());
				tryDbDisconnect(getSchedulerConnection());
			}
			catch(Exception e){
				
			}
			super.onEmptyEvent(overview, eventTypes, eventId);
		} catch (Exception ex) {
			LOGGER.error(String.format("%s", ex.toString()));
		}
	}

	@Override
	public void onRestart(Overview overview, EventType[] eventTypes, Long eventId) {
		LOGGER.debug(String.format("onRestart: overview=%s, eventTypes=%s, eventId=%s", overview,
				joinEventTypes(eventTypes), eventId));

	}

	@Override
	public void close() {
		closeRestApiClient();
		getReportingConnection().disconnect();
		getSchedulerConnection().disconnect();
	}
	
	private void initObservedEvents(){
		observedEventTypes = new ArrayList<String>();
		observedEventTypes.add(EventType.TaskStarted.name());
		observedEventTypes.add(EventType.TaskEnded.name());
		observedEventTypes.add(EventType.OrderStepStarted.name());
		observedEventTypes.add(EventType.OrderStepEnded.name());
		observedEventTypes.add(EventType.OrderFinished.name());
	}
	
	private void initFactOptions(){
		factOptions = new FactJobOptions();
		factOptions.current_scheduler_id.setValue(getSchedulerAnswer().getSchedulerId());
		factOptions.current_scheduler_http_port.setValue(getSchedulerAnswer().getHttpPort());
		factOptions.max_history_age.setValue("30m");
		factOptions.force_max_history_age.value(false);
	}
	
	private void initDailyPlanOptions(){
		dailyPlanOptions = new CheckDailyPlanOptions();
		dailyPlanOptions.scheduler_id.setValue(getSchedulerAnswer().getSchedulerId());
		dailyPlanOptions.dayOffset.setValue("1");
		try{
			dailyPlanOptions.configuration_file.setValue(getSchedulerAnswer().getHibernateConfigPath().toFile().getCanonicalPath());
		}
		catch(Exception e){}
	}
	
	private boolean checkEvents(String type){
		if(type != null && observedEventTypes.contains(type)){
			return true;
		}
		return false;
	}
	
	
	private void executeFacts() throws Exception{
		FactModel model = new FactModel(getReportingConnection(),getSchedulerConnection(), factOptions);
		model.process();
	}
	
	private void executeDailyPlan() throws Exception{
		DailyPlanAdjustment dailyPlanAdjustment = new DailyPlanAdjustment(new File(dailyPlanOptions.configuration_file.getValue()));
        
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
        }
        finally{
        	try{
        		dailyPlanAdjustment.disconnect();
        	}
        	catch(Exception e){}
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
}
