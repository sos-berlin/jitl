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
    
	public static enum EventType { FileBasedEvent, TaskEvent};
    
	private static final String WEBSERVICE_URL_EVENT = "/jobscheduler/master/api/event";
	
	private static final Integer HTTP_CLIENT_SOCKET_TIMEOUT = 65000;
    
	
    private static final String WEBSERVICE_URL_FILE_BASED = "/jobscheduler/master/api/fileBased";
    //private static final String WEBSERVICE_URL_TASK_EVENT = "/jobscheduler/master/api/taskEvent";
    
	private static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";
	private static final String HEADER_KEY_ACCEPT = "Accept";
    
    private static final String HEADER_VALUE_APPLICATION_XML = "application/xml";
    private static final String HEADER_VALUE_APPLICATION_JSON = "application/json";
    
    private static final String WEBSERVICE_PARAM_KEY_RETURN = "return";
    private static final String WEBSERVICE_PARAM_VALUE_FILEBASED_DETAILED = "FileBasedDetailed";
    private static final String WEBSERVICE_PARAM_VALUE_FILEBASED_OVERVIEW = "FileBasedOverview";
    //private static final String WEBSERVICE_PARAM_VALUE_FILEBASED_EVENT = "FileBasedEvent";
    //private static final String WEBSERVICE_PARAM_VALUE_TASK_EVENT = "TaskEvent";
    
    private static final String WEBSERVICE_PARAM_KEY_AFTER = "after";
    private static final String WEBSERVICE_PARAM_KEY_TIMEOUT = "timeout";
    private static final String WEBSERVICE_PARAM_VALUE_TIMEOUT = "60s";
    
    private static final String POST_BODY_JSON_KEY = "path";
    private static final String POST_BODY_JSON_VALUE = "/";
    
    private static final String EVENT_TYPE = "TYPE";
    private static final String EVENT_TYPE_NON_EMPTY = "NonEmpty";
    private static final String EVENT_TYPE_EMPTY = "Empty";
    private static final String EVENT_TYPE_TORN = "Torn";
    private static final String EVENT_KEY = "key";
    private static final String EVENT_ID = "eventId";
    private static final String EVENT_SNAPSHOT = "eventSnapshots";
    
    private static final String JS_OBJECT_TYPE_JOB = "Job";
    private static final String JS_OBJECT_TYPE_JOBCHAIN = "JobChain";
    private static final String JS_OBJECT_TYPE_ORDER = "Order";
    private static final String JS_OBJECT_TYPE_PROCESS_CLASS = "ProcessClass";
    private static final String JS_OBJECT_TYPE_SCHEDULE = "Schedule";
    private static final String JS_OBJECT_TYPE_LOCK = "Lock";
    private static final String JS_OBJECT_TYPE_FOLDER = "Folder";
        
    private SchedulerXmlCommandExecutor xmlCommandExecutor;
	private VariableSet variableSet;
	private SOSHibernateConnection connection;
	private SchedulerAnswer schedulerAnswer;
	
    private String webserviceUrl = null;
    //private Long eventId = null;
    private JobSchedulerRestApiClient restClient;
        
    
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
    	createHttpClient();
    }
	
	@Override
	public void close() throws Exception {
		closeHttpClient();
	}
    
	public void createHttpClient(){
		restClient = new JobSchedulerRestApiClient();
        restClient.setAutoCloseHttpClient(false);
        restClient.setSocketTimeout(HTTP_CLIENT_SOCKET_TIMEOUT);
        restClient.createHttpClient();
	}
	
	public void closeHttpClient(){
		if(restClient != null){
			restClient.closeHttpClient();
		}
		restClient = null;
	}
	
	public void start(EventType eventType) {
		Long eventId = null;
		try {
            LOGGER.debug(String.format("start: eventType=%s",eventType));
            
            eventId = getEventIdFromOverview(eventType);
            eventId = process(eventType,eventId);
        } catch (Exception e) {
            LOGGER.warn(String.format("restart event processing. error message: %s. ", e.getMessage()), e);
            restart(eventType,eventId);
        }
    }
	
	 private Long process(EventType eventType,Long eventId) throws Exception {
	    	LOGGER.debug(String.format("process: eventType=%s, eventId=%s",eventType,eventId));
	        
	    	JsonObject result = getEvents(eventType,eventId);
	    	JsonArray events = result.getJsonArray(EVENT_SNAPSHOT);
	     	String type = result.getString(EVENT_TYPE);
	        eventId = result.getJsonNumber(EVENT_ID).longValue();
            if(events != null && !events.isEmpty()) {
	            processEvents(eventType,eventId,type, events);
	        } else if(EVENT_TYPE_EMPTY.equalsIgnoreCase(type)) {
	        	onEmptyEvent(eventType,eventId);
	        }
            return eventId;
	    }
    
    private JsonObject executeJsonPost(URI uri, boolean withBody) throws Exception {
    	LOGGER.debug(String.format("executeJsonPost: uri=%s, withBody=%s",uri,withBody));
        
    	restClient.addHeader(HEADER_KEY_CONTENT_TYPE, HEADER_VALUE_APPLICATION_JSON);
        restClient.addHeader(HEADER_KEY_ACCEPT, HEADER_VALUE_APPLICATION_JSON);
        String response = null;
        if(withBody) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add(POST_BODY_JSON_KEY, POST_BODY_JSON_VALUE);
            response = restClient.postRestService(uri, builder.build().toString());
        } else {
            response = restClient.postRestService(uri, null);
        }
        int statusCode = restClient.statusCode();
        String contentType = restClient.getResponseHeader(HEADER_KEY_CONTENT_TYPE);
        JsonObject json = null;
        if (contentType.contains(HEADER_VALUE_APPLICATION_JSON)) {
            JsonReader jr = Json.createReader(new StringReader(response));
            json = jr.readObject();
        }
        switch (statusCode) {
        case 200:
            if (json != null) {
                return json;
            } else {
                throw new Exception(String.format("Unexpected content type '%s'. Response: %s", contentType,response));
            }
        case 400:
            // TO DO check Content-Type
            // for now the exception is plain/text instead of JSON
            // throw message item value
            if (json != null) {
                throw new Exception(json.getString("message"));
            } else {
                throw new Exception(String.format("Unexpected content type '%s'. Response: %s", contentType,response));
            }
        default:
            throw new Exception(statusCode + " " + restClient.getHttpResponse().getStatusLine().getReasonPhrase());
        }
    }
    
    private void processEvents(EventType eventType, Long eventId, String type, JsonArray events) throws Exception {
    	LOGGER.debug(String.format("processEvents: eventType=%s, eventId=%s, type=%s, events=%s",eventType,eventId,type,events));
            	
    	switch(type) {
    		case EVENT_TYPE_NON_EMPTY :
    				onNonEmptyEvent(eventType,eventId,type,events);
    				break;
    		case EVENT_TYPE_TORN :
    				onTornEvent(eventType,eventId,type,events);
        			break;
        }
    }
    
    public void onEmptyEvent(EventType eventType, Long eventId) throws Exception{
    	LOGGER.info("onEmptyEvent -PARENT"); 
    	process(eventType,eventId);
    }
  
    public void onNonEmptyEvent(EventType eventType, Long eventId, String type, JsonArray events) throws Exception{
    	LOGGER.info("onNonEmptyEvent -PARENT"); 
    	//start(eventType);
    	process(eventType,eventId);
    }
    
    public void onTornEvent(EventType eventType, Long eventId,String type, JsonArray events){
    	LOGGER.info("onTornEvent -PARENT"); 
    	restart(eventType,eventId);
    }
    
    public void onRestart(EventType eventType, Long eventId){
    	
    }

    private void restart(EventType eventType, Long eventId) {
    	LOGGER.debug(String.format("restart: eventType=%s, eventId=%s",eventType,eventId));
        
    	onRestart(eventType,eventId);
        
    	start(eventType);
    }
    
    private String getParamReturnOverview(EventType eventType){
    	switch(eventType){
    		case FileBasedEvent :
    			return WEBSERVICE_PARAM_VALUE_FILEBASED_OVERVIEW;
    		default:
    			return WEBSERVICE_PARAM_VALUE_FILEBASED_OVERVIEW;
    			
    	}
    }
    
    private String getWebserviceUrl(EventType eventType){
    	switch(eventType){
    		case FileBasedEvent :
    			return WEBSERVICE_URL_FILE_BASED;
    		case TaskEvent :
    			return WEBSERVICE_URL_FILE_BASED;
    		default:
    			return "";
    			
    	}
    }
    
    
    private Long getEventIdFromOverview(EventType eventType) {
    	LOGGER.debug(String.format("getEventIdFromOverview: eventType=%s",eventType));
            	
    	StringBuilder connectTo = new StringBuilder();
        connectTo.append(webserviceUrl);
        connectTo.append(getWebserviceUrl(eventType));
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder();
            uriBuilder.setPath(connectTo.toString());
            uriBuilder.addParameter(WEBSERVICE_PARAM_KEY_RETURN, getParamReturnOverview(eventType));
            JsonObject result = executeJsonPost(uriBuilder.build(), true);
            LOGGER.debug(result.toString());
            JsonNumber jsonEventId = result.getJsonNumber(EVENT_ID);
            if (jsonEventId != null) {
                return jsonEventId.longValue();
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
        
    	StringBuilder connectTo = new StringBuilder();
        connectTo.append(webserviceUrl);
        connectTo.append(WEBSERVICE_URL_EVENT);
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(connectTo.toString());
            uriBuilder.addParameter(WEBSERVICE_PARAM_KEY_RETURN, eventType.name());
            uriBuilder.addParameter(WEBSERVICE_PARAM_KEY_TIMEOUT, WEBSERVICE_PARAM_VALUE_TIMEOUT);
            uriBuilder.addParameter(WEBSERVICE_PARAM_KEY_AFTER, eventId.toString());
            JsonObject result = executeJsonPost(uriBuilder.build(), false);
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
    
    public JobSchedulerRestApiClient getRestClient() {
        return restClient;
    }
    
    public SOSHibernateConnection getConnection(){
    	return this.connection;
    }
    
    public SchedulerXmlCommandExecutor getXmlCommandExecutor(){
    	return this.xmlCommandExecutor;
    }

	
}