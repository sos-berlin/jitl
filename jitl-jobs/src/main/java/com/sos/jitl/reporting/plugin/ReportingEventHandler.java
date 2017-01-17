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
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

import javassist.NotFoundException;

public class ReportingEventHandler implements IReportingEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportingEventHandler.class);

	public static enum EventType {
		FileBasedEvent, FileBasedAdded, FileBasedRemoved, FileBasedReplaced, FileBasedActivated, 
		TaskEvent, TaskStarted, TaskEnded, TaskClosed, 
		OrderEvent, OrderStarted, OrderFinished, OrderStepStarted, OrderStepEnded, OrderSetBack, OrderNodeChanged, OrderSuspended, OrderResumed, 
		JobChainEvent, JobChainStateChanged, JobChainNodeActionChanged
	};

	public static enum EventSeq {
		NonEmpty, Empty, Torn
	};

	public static enum EventUrl {
		event, fileBased, task, order, jobChain
	};

	public static enum EventKey {
		TYPE, key, eventId, eventSnapshots, jobPath
	};

	public static enum Overview {
		FileBasedOverview, FileBasedDetailed, TaskOverview, OrderOverview, JobChainOverview
	};

	private static final String WEBSERVICE_API_URL = "/jobscheduler/master/api/";
	private static final Integer HTTP_CLIENT_SOCKET_TIMEOUT = 65000;
	private static final String WEBSERVICE_PARAM_VALUE_TIMEOUT = "60";

	private SchedulerXmlCommandExecutor xmlCommandExecutor;
	private VariableSet variableSet;
	private SchedulerAnswer schedulerAnswer;
	private SOSHibernateConnection reportingConnection;
	private SOSHibernateConnection schedulerConnection;

	private String webserviceUrl = null;
	private JobSchedulerRestApiClient client;

	private int restartCounter = 0;
	private int maxRestarts = 5;
	private boolean closed = false;
	
	public ReportingEventHandler() {
	}

	@Override
	public void onPrepare(SchedulerXmlCommandExecutor sxce, VariableSet vs, SchedulerAnswer sa,
			SOSHibernateConnection reportingConn, SOSHibernateConnection schedulerConn) {
		this.xmlCommandExecutor = sxce;
		this.variableSet = vs;
		this.schedulerAnswer = sa;
		this.schedulerConnection = schedulerConn;
		this.reportingConnection = reportingConn;
		setWebServiceUrl();
	}

	@Override
	public void onActivate() {
		createRestApiClient();
		this.closed = false;
	}

	@Override
	public void close() {
		closeRestApiClient();
		this.closed = true;
	}

	public void createRestApiClient() {
		client = new JobSchedulerRestApiClient();
		client.setAutoCloseHttpClient(false);
		client.setSocketTimeout(HTTP_CLIENT_SOCKET_TIMEOUT);
		client.createHttpClient();
	}

	public void closeRestApiClient() {
		if (client != null) {
			client.closeHttpClient();
		}
		client = null;
	}

	public void start() throws Exception {
		start(null, null);
	}

	public void start(EventType[] eventTypes) throws Exception {
		start(null, eventTypes);
	}

	public void start(Overview overview, EventType[] eventTypes) throws Exception {
		Long eventId = null;
		try {
			if(closed){
				LOGGER.info(String.format("start: processing stopped."));
				return;
			}
			tryClientConnect();
			
			LOGGER.debug(String.format("start: overview=%s, eventTypes=%s", overview, joinEventTypes(eventTypes)));
			
			if (overview == null) {
				overview = getOverviewByEventTypes(eventTypes);
			}

			eventId = getEventIdFromOverview(overview);
			eventId = process(overview, eventTypes, eventId);
			restartCounter = 0;
		} catch (NotFoundException e) {
			LOGGER.warn(String.format("stop event processing. error message: %s. ", e.getMessage()), e);
			throw new Exception(e);
		} catch (Exception e) {
			restartCounter++;
			if (restartCounter > maxRestarts) {
				LOGGER.warn(String.format("max restarts (%s) exceeded. stop event processing. error message: %s. ",
						maxRestarts, e.getMessage()), e);
				throw new Exception(e);
			} else {
				LOGGER.warn(String.format("restart (%s of %s) event processing. error message: %s. ", restartCounter,
						maxRestarts, e.getMessage()), e);
				restart(overview, eventTypes, eventId);
			}
		}
	}

	public void onEmptyEvent(Overview overview, EventType[] eventTypes, Long eventId) throws Exception {
		LOGGER.debug("onEmptyEvent");
		process(overview, eventTypes, eventId);
	}

	public void onNonEmptyEvent(Overview overview, EventType[] eventTypes, Long eventId, String type, JsonArray events)
			throws Exception {
		LOGGER.debug("onNonEmptyEvent");
		process(overview, eventTypes, eventId);
	}

	public void onTornEvent(Overview overview, EventType[] eventTypes, Long eventId, String type, JsonArray events)
			throws Exception {
		LOGGER.debug("onTornEvent");
		restart(overview, eventTypes, eventId);
	}

	public void onRestart(Overview overview, EventType[] eventTypes, Long eventId) {

	}

	public String joinEventTypes(EventType[] eventTypes) {
		return eventTypes == null ? "" : Joiner.on(",").join(eventTypes);
	}

	public String getEventKey(JsonObject jo){
		String key = null;
		JsonValue joKey = jo.get(EventKey.key.name());
		if(joKey != null){
			if(joKey.getValueType().equals(ValueType.STRING)){
				key = joKey.toString();
			}
			else if(joKey.getValueType().equals(ValueType.OBJECT)){
				if(((JsonObject)joKey).containsKey(EventKey.jobPath.name())){
					key = ((JsonObject)joKey).getString(EventKey.jobPath.name());
				}
			}
		}
		return key;
	}
	
	private Long process(Overview overview, EventType[] eventTypes, Long eventId) throws Exception {
		
		if(closed){
			LOGGER.debug(String.format("process: processing stopped."));
			return null;
		}
		tryClientConnect();
		
		LOGGER.debug(String.format("process: overview=%s, eventTypes=%s, eventId=%s", overview,
				joinEventTypes(eventTypes), eventId));
				
		JsonObject result = getEvents(eventTypes, eventId);
		JsonArray events = result.getJsonArray(EventKey.eventSnapshots.name());
		String type = result.getString(EventKey.TYPE.name());
		eventId = result.getJsonNumber(EventKey.eventId.name()).longValue();

		if (type.equalsIgnoreCase(EventSeq.NonEmpty.name())) {
			onNonEmptyEvent(overview, eventTypes, eventId, type, events);
		} else if (type.equalsIgnoreCase(EventSeq.Empty.name())) {
			onEmptyEvent(overview, eventTypes, eventId);
		} else if (type.equalsIgnoreCase(EventSeq.Torn.name())) {
			onTornEvent(overview, eventTypes, eventId, type, events);
		}
		return eventId;
	}
	
	private void tryClientConnect(){
		if(client == null){
			createRestApiClient();
		}
	}

	private void restart(Overview overview, EventType[] eventTypes, Long eventId) throws Exception {
		LOGGER.debug(String.format("restart: overview=%s, eventTypes=%s, eventId=%s", overview,
				joinEventTypes(eventTypes), eventId));

		onRestart(overview, eventTypes, eventId);

		start(overview, eventTypes);
	}

	private Overview getOverviewByEventTypes(EventType[] eventTypes) {

		if (eventTypes != null) {
			String firstEventType = eventTypes[0].name();

			if (firstEventType.toLowerCase().startsWith(EventUrl.fileBased.name().toLowerCase())) {
				return Overview.FileBasedOverview;
			} else if (firstEventType.toLowerCase().startsWith(EventUrl.order.name().toLowerCase())) {
				return Overview.OrderOverview;
			} else if (firstEventType.toLowerCase().startsWith(EventUrl.task.name().toLowerCase())) {
				return Overview.TaskOverview;
			} else if (firstEventType.toLowerCase().startsWith(EventUrl.jobChain.name().toLowerCase())) {
				return Overview.JobChainOverview;
			}
		}
		return null;
	}

	private EventUrl getEventUrlByOverview(Overview overview) {
		if (overview != null) {
			if (overview.name().toLowerCase().startsWith(EventUrl.fileBased.name().toLowerCase())) {
				return EventUrl.fileBased;
			} else if (overview.name().toLowerCase().startsWith(EventUrl.order.name().toLowerCase())) {
				return EventUrl.order;
			} else if (overview.name().toLowerCase().startsWith(EventUrl.task.name().toLowerCase())) {
				return EventUrl.task;
			} else if (overview.name().toLowerCase().startsWith(EventUrl.jobChain.name().toLowerCase())) {
				return EventUrl.jobChain;
			}
			else if (overview.name().toLowerCase().startsWith(EventUrl.event.name().toLowerCase())) {
				return EventUrl.event;
			}
		}
		return null;
	}

	private Long getEventIdFromOverview(Overview overview) throws Exception {
		LOGGER.debug(String.format("getEventIdFromOverview: overview=%s", overview));

		if(overview == null){
			overview = Overview.FileBasedOverview;
		}
		
		Long rc = null;
		StringBuilder path = new StringBuilder();
		path.append(webserviceUrl);
		path.append(WEBSERVICE_API_URL);
		path.append(getEventUrlByOverview(overview));

		URIBuilder ub = new URIBuilder(path.toString());
		ub.addParameter("return", overview.name());
		JsonObject result = executeJsonPost(ub.build(), true);
		JsonNumber eventId = result.getJsonNumber(EventKey.eventId.name());

		LOGGER.debug(result.toString());

		if (eventId != null) {
			rc = eventId.longValue();
		}
		return rc;
	}

	private JsonObject executeJsonPost(URI uri, boolean withBody) throws Exception {
		LOGGER.debug(String.format("executeJsonPost: uri=%s, withBody=%s", uri, withBody));

		String headerKeyContentType = "Content-Type";
		String headerValueApplication = "application/json";

		client.addHeader(headerKeyContentType, headerValueApplication);
		client.addHeader("Accept", headerValueApplication);
		String response = null;
		if (withBody) {
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
			} else {
				throw new Exception(String.format("Unexpected content type '%s'. Response: %s", contentType, response));
			}
		case 400:
			// TO DO check Content-Type
			// for now the exception is plain/text instead of JSON
			// throw message item value
			if (json != null) {
				throw new Exception(json.getString("message"));
			} else {
				throw new Exception(String.format("Unexpected content type '%s'. Response: %s", contentType, response));
			}
		case 404:
			throw new NotFoundException(String.format("%s %s, uri=%s", statusCode,
					client.getHttpResponse().getStatusLine().getReasonPhrase(), uri.toString()));
		default:
			throw new Exception(
					String.format("%s %s", statusCode, client.getHttpResponse().getStatusLine().getReasonPhrase()));
		}
	}

	private JsonObject getEvents(EventType[] eventTypes, Long eventId) throws Exception {
		LOGGER.debug(String.format("getEvents: eventTypes=%s, eventId=%s", joinEventTypes(eventTypes), eventId));

		StringBuilder path = new StringBuilder();
		path.append(webserviceUrl);
		path.append(WEBSERVICE_API_URL);
		path.append(EventUrl.event.name());
		URIBuilder ub;
		try {
			ub = new URIBuilder(path.toString());
			if (eventTypes != null) {
				ub.addParameter("return", joinEventTypes(eventTypes));
			}
			ub.addParameter("timeout", WEBSERVICE_PARAM_VALUE_TIMEOUT);
			ub.addParameter("after", eventId.toString());
			JsonObject result = executeJsonPost(ub.build(), false);

			LOGGER.debug("result: " + result.toString());

			return result;
		} catch (URISyntaxException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
	}

	private void setWebServiceUrl() {
		this.webserviceUrl = "http://localhost:"+schedulerAnswer.getHttpPort();
	}

	public String getWebServiceUrl() {
		return this.webserviceUrl;
	}

	public JobSchedulerRestApiClient getRestApiClient() {
		return client;
	}

	public SOSHibernateConnection getReportingConnection() {
		return this.reportingConnection;
	}

	public SOSHibernateConnection getSchedulerConnection() {
		return this.schedulerConnection;
	}

	public SchedulerXmlCommandExecutor getXmlCommandExecutor() {
		return this.xmlCommandExecutor;
	}

	public VariableSet getVariableSet() {
		return this.variableSet;
	}

	public SchedulerAnswer getSchedulerAnswer() {
		return this.schedulerAnswer;
	}

}