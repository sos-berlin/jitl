package com.sos.jitl.reporting.plugin;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;


public class ReportingEventHandler implements IReportingEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportingEventHandler.class);
    
	public static enum EventType { FileBasedEvent, TaskEvent, TaskStarted, TaskEnded, TaskClosed};
	public static enum EventRetunType { NonEmpty, Empty,Torn};
	public static enum EventUrl { event, fileBased};
	public static enum EventKey { key, eventId,eventSnapshots,TYPE};	
	public static enum Overview { FileBasedOverview, FileBasedDetailed};
	
	private static final String WEBSERVICE_API_URL = "/jobscheduler/master/api/";
	private static final Integer HTTP_CLIENT_SOCKET_TIMEOUT = 65000;
	private static final String WEBSERVICE_PARAM_VALUE_TIMEOUT = "60";
	    
    private SchedulerXmlCommandExecutor xmlCommandExecutor;
	private VariableSet variableSet;
	private SOSHibernateConnection connection;
	private SchedulerAnswer schedulerAnswer;
    private String webserviceUrl = null;
    private JobSchedulerRestApiClient client;
        
    
    public ReportingEventHandler(){}
   
    @Override
	public void onPrepare(SchedulerXmlCommandExecutor sxce, VariableSet vs, SOSHibernateConnection conn, SchedulerAnswer sa) throws Exception {
    	this.xmlCommandExecutor = sxce;
    	this.variableSet = vs;
    	this.connection = conn;
    	this.schedulerAnswer = sa;    	
    	setWebServiceUrl();
	}
    
    
    @Override
	public void onActivate() throws Exception {
    	createRestApiClient();
    }
	
	@Override
	public void close() throws Exception {
		closeRestApiClient();
	}
    
	public void createRestApiClient(){
		client = new JobSchedulerRestApiClient();
        client.setAutoCloseHttpClient(false);
        client.setSocketTimeout(HTTP_CLIENT_SOCKET_TIMEOUT);
        client.createHttpClient();
	}
	
	public void closeRestApiClient(){
		if(client != null){
			client.closeHttpClient();
		}
		client = null;
	}
	
	public void start(EventType eventType) {
		Long eventId = null;
		try {
            LOGGER.debug(String.format("start: eventType=%s",eventType));
            
            eventId = getEventIdFromOverview();
            eventId = process(eventType,eventId);
        } catch (Exception e) {
            LOGGER.warn(String.format("restart event processing. error message: %s. ", e.getMessage()), e);
            restart(eventType,eventId);
        }
    }
	
	 private Long process(EventType eventType,Long eventId) throws Exception {
	    	LOGGER.debug(String.format("process: eventType=%s, eventId=%s",eventType,eventId));
	        
	    	JsonObject result = getEvents(eventType,eventId);
	    	JsonArray events = result.getJsonArray(EventKey.eventSnapshots.name());
	     	String type = result.getString(EventKey.TYPE.name());
	        eventId = result.getJsonNumber(EventKey.eventId.name()).longValue();
            if(events != null && !events.isEmpty()) {
	            processEvents(eventType,eventId,type, events);
	        } else if(EventRetunType.Empty.name().equalsIgnoreCase(type)) {
	        	onEmptyEvent(eventType,eventId);
	        }
            return eventId;
	    }
    
    private JsonObject executeJsonPost(URI uri, boolean withBody) throws Exception {
    	LOGGER.debug(String.format("executeJsonPost: uri=%s, withBody=%s",uri,withBody));
        
    	String headerKeyContentType = "Content-Type";
        String headerValueApplication = "application/json";
    	
    	client.addHeader(headerKeyContentType, headerValueApplication);
        client.addHeader("Accept", headerValueApplication);
        String response = null;
        if(withBody) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("path", "/");
            response = client.postRestService(uri, builder.build().toString());
        } else {
            response = client.postRestService(uri, null);
        }
        int statusCode = client.statusCode();
        String contentType = client.getResponseHeader(headerKeyContentType);
        JsonObject json = null;
        if (contentType.contains(headerValueApplication)) {
            JsonReader jr = Json.createReader(new StringReader(response));
            json = jr.readObject();
        }
        switch (statusCode) {
        	case 200:
        				if (json != null) {
        					return json;
        				} 
        				else {
        					throw new Exception(String.format("Unexpected content type '%s'. Response: %s", contentType,response));
        				}
        	case 400:
        				// TO DO check Content-Type
        				// for now the exception is plain/text instead of JSON
        				// throw message item value
        				if (json != null) {
        					throw new Exception(json.getString("message"));
        				} 
        				else {
        					throw new Exception(String.format("Unexpected content type '%s'. Response: %s", contentType,response));
        				}
        	default:
        				throw new Exception(statusCode + " " + client.getHttpResponse().getStatusLine().getReasonPhrase());
        }
    }
    
    private void processEvents(EventType eventType, Long eventId, String type, JsonArray events) throws Exception {
    	LOGGER.debug(String.format("processEvents: eventType=%s, eventId=%s, type=%s, events=%s",eventType,eventId,type,events));
        
    	if(type.equals(EventRetunType.NonEmpty.name())){
    		onNonEmptyEvent(eventType,eventId,type,events);
    	}
    	else if(type.equals(EventRetunType.Torn.name())){
    		onTornEvent(eventType,eventId,type,events);
    	}
    }
    
    public void onEmptyEvent(EventType eventType, Long eventId) throws Exception{
    	LOGGER.info("onEmptyEvent"); 
    	process(eventType,eventId);
    }
  
    public void onNonEmptyEvent(EventType eventType, Long eventId, String type, JsonArray events) throws Exception{
    	LOGGER.info("onNonEmptyEvent"); 
    	process(eventType,eventId);
    }
    
    public void onTornEvent(EventType eventType, Long eventId,String type, JsonArray events){
    	LOGGER.info("onTornEvent"); 
    	restart(eventType,eventId);
    }
    
    public void onRestart(EventType eventType, Long eventId){
    	
    }

    private void restart(EventType eventType, Long eventId) {
    	LOGGER.debug(String.format("restart: eventType=%s, eventId=%s",eventType,eventId));
        
    	onRestart(eventType,eventId);
        
    	start(eventType);
    }
    
    private Long getEventIdFromOverview() {
    	LOGGER.debug(String.format("getEventIdFromOverview"));
            	
    	StringBuilder path = new StringBuilder();
        path.append(webserviceUrl);
        path.append(WEBSERVICE_API_URL);
        path.append(EventUrl.fileBased.name());
        URIBuilder ub;
        try {
        	ub = new URIBuilder(path.toString());
        	ub.addParameter("return",Overview.FileBasedOverview.name());
            JsonObject result = executeJsonPost(ub.build(), true);
            JsonNumber eventId = result.getJsonNumber(EventKey.eventId.name());
            
            LOGGER.debug(result.toString());
            
            if (eventId != null) {
                return eventId.longValue();
            }
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
    
    
    
    private JsonObject getEvents(EventType eventType, Long eventId) throws Exception {
    	LOGGER.debug(String.format("getEvents: eventType=%s, eventId=%s",eventType,eventId));
        
    	StringBuilder path = new StringBuilder();
        path.append(webserviceUrl);
        path.append(WEBSERVICE_API_URL);
        path.append(EventUrl.event.name());
        URIBuilder ub;
        try {
            ub = new URIBuilder(path.toString());
            ub.addParameter("return", eventType.name());
            ub.addParameter("timeout", WEBSERVICE_PARAM_VALUE_TIMEOUT);
            ub.addParameter("after", eventId.toString());
            JsonObject result = executeJsonPost(ub.build(), false);
            
            LOGGER.debug(result.toString());
            
            return result;
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
    
    private void setWebServiceUrl(){
    	String hostname = schedulerAnswer.getMasterUrl().substring(schedulerAnswer.getMasterUrl().lastIndexOf("/") + 1, schedulerAnswer.getMasterUrl().lastIndexOf(":"));
        this.webserviceUrl = schedulerAnswer.getMasterUrl().replace(hostname, "localhost");
    }
    
    public String getWebServiceUrl() {
        return this.webserviceUrl;
    }
    
    public JobSchedulerRestApiClient getRestApiClient() {
        return client;
    }
    
    public SOSHibernateConnection getConnection(){
    	return this.connection;
    }
    
    public SchedulerXmlCommandExecutor getXmlCommandExecutor(){
    	return this.xmlCommandExecutor;
    }

    public VariableSet getVariableSet(){
    	return this.variableSet;
    }
	
}