package com.sos.jitl.classes.event;

import java.io.StringReader;
import java.net.URI;

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
import com.sos.jitl.restclient.JobSchedulerRestApiClient;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

import javassist.NotFoundException;
import sos.util.SOSString;

public class JobSchedulerEventHandler implements IJobSchedulerEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerEventHandler.class);

	public static enum EventType {
		FileBasedEvent, FileBasedAdded, FileBasedRemoved, FileBasedReplaced, FileBasedActivated, TaskEvent, TaskStarted, TaskEnded, TaskClosed, OrderEvent, OrderStarted, OrderFinished, OrderStepStarted, OrderStepEnded, OrderSetBack, OrderNodeChanged, OrderSuspended, OrderResumed, JobChainEvent, JobChainStateChanged, JobChainNodeActionChanged, SchedulerClosed
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

	private SchedulerXmlCommandExecutor xmlCommandExecutor;
	private VariableSet variableSet;
	private EventHandlerSettings settings;
	private String identifier;

	private String webserviceUrl = null;
	private JobSchedulerRestApiClient client;
	private boolean closed = false;
	private Overview overview;
	private EventType[] eventTypes;
	private String eventTypesJoined;

	private String pathParamForEventId = "/not_exists/";
	private EventUrl eventUrlForEventId;
	private int waitIntervalOnError = 5;
	private int httpClientSocketTimeout = 65000;
	private int webserviceTimeout = 60;

	public JobSchedulerEventHandler() {
	}

	@Override
	public void onPrepare(SchedulerXmlCommandExecutor sxce, VariableSet vs, EventHandlerSettings st) {
		this.xmlCommandExecutor = sxce;
		this.variableSet = vs;
		this.settings = st;
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
		client.setSocketTimeout(this.httpClientSocketTimeout);
		client.createHttpClient();
	}

	public void closeRestApiClient() {
		if (client != null) {
			client.closeHttpClient();
		}
		client = null;
	}

	public void start() {
		start(null, null);
	}

	public void start(EventType[] et) {
		start(null, et);
	}

	public void start(Overview ov, EventType[] et) {
		String method = getMethodName("start");

		if (closed) {
			LOGGER.info(String.format("%s: processing stopped.", method));
			return;
		}
		tryClientConnect();

		if (ov == null) {
			ov = getOverviewByEventTypes(et);
		}
		this.overview = ov;
		this.eventTypes = et;
		this.eventTypesJoined = joinEventTypes(this.eventTypes);
		this.eventUrlForEventId = getEventUrlByOverview(this.overview);
		
		LOGGER.debug(String.format("%s: overview=%s, eventTypes=%s", method, overview, eventTypesJoined));

		Long eventId = null;
		try {
			eventId = getEventIdFromOverview();
		} catch (Exception e) {
			eventId = rerunGetEventIdFromOverview(method, e);
		}
		try {
			process(eventId);
		} catch (Exception e) {
			rerunProcess(method, e, eventId);
		}
	}

	public void onEmptyEvent(Long eventId) {
		String method = getMethodName("onEmptyEvent");

		LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
		try {
			process(eventId);
		} catch (Exception e) {
			rerunProcess(method, e, eventId);
		}
	}

	public void onNonEmptyEvent(Long eventId, JsonArray events) {
		String method = getMethodName("onNonEmptyEvent");

		LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
		try {
			process(eventId);
		} catch (Exception e) {
			rerunProcess(method, e, eventId);
		}
	}

	public void onTornEvent(Long eventId, JsonArray events) {
		String method = getMethodName("onTornEvent");

		if (closed) {
			LOGGER.info(String.format("%s: processing stopped.", method));
		} else {
			LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
			onRestart(eventId, events);
			start(overview, eventTypes);
		}
	}

	public void onRestart(Long eventId, JsonArray events) {
		String method = getMethodName("onRestart");
		LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
	}

	public String getEventKey(JsonObject jo) {
		String key = null;
		JsonValue joKey = jo.get(EventKey.key.name());
		if (joKey != null) {
			if (joKey.getValueType().equals(ValueType.STRING)) {
				key = joKey.toString();
			} else if (joKey.getValueType().equals(ValueType.OBJECT)) {
				if (((JsonObject) joKey).containsKey(EventKey.jobPath.name())) {
					key = ((JsonObject) joKey).getString(EventKey.jobPath.name());
				}
			}
		}
		return key;
	}

	private String joinEventTypes(EventType[] et) {
		return et == null ? "" : Joiner.on(",").join(et);
	}

	private void rerunProcess(String callerMethod, Exception ex, Long eventId) {
		String method = getMethodName("rerunProcess");

		if (closed) {
			LOGGER.info(String.format("%s: processing stopped.", method));
			return;
		}
		if (ex != null) {
			LOGGER.error(String.format("%s: error on %s: %s", method, callerMethod, ex.toString()), ex);
		}
		LOGGER.debug(String.format("%s: eventId=%s", method, eventId));

		wait(waitIntervalOnError);
		try {
			process(eventId);
		} catch (Exception e) {
			rerunProcess(method, e, eventId);
		}
	}

	private Long rerunGetEventIdFromOverview(String callerMethod, Exception ex) {
		String method = getMethodName("rerunGetEventIdFromOverview");

		if (closed) {
			LOGGER.info(String.format("%s: processing stopped.", method));
			return null;
		}

		if (ex != null) {
			LOGGER.error(String.format("%s: error on %s: %s", method, callerMethod, ex.toString()), ex);
		}
		LOGGER.debug(String.format("%s", method));

		wait(waitIntervalOnError);
		Long eventId = null;
		try {
			eventId = getEventIdFromOverview();
		} catch (Exception e) {
			eventId = rerunGetEventIdFromOverview(method, e);
		}
		return eventId;
	}

	private Long process(Long eventId) throws Exception {
		String method = getMethodName("process");

		if (closed) {
			return null;
		}
		tryClientConnect();

		LOGGER.debug(String.format("%s: eventId=%s", method, eventId));

		JsonObject result = getEvents(eventId);
		JsonArray events = result.getJsonArray(EventKey.eventSnapshots.name());
		String type = result.getString(EventKey.TYPE.name());
		eventId = result.getJsonNumber(EventKey.eventId.name()).longValue();

		if (type.equalsIgnoreCase(EventSeq.NonEmpty.name())) {
			onNonEmptyEvent(eventId, events);
		} else if (type.equalsIgnoreCase(EventSeq.Empty.name())) {
			onEmptyEvent(eventId);
		} else if (type.equalsIgnoreCase(EventSeq.Torn.name())) {
			onTornEvent(eventId, events);
		}
		return eventId;
	}

	private void tryClientConnect() {
		if (client == null) {
			createRestApiClient();
		}
	}

	private Overview getOverviewByEventTypes(EventType[] et) {
		if (et != null) {
			String firstEventType = et[0].name();
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
		return Overview.FileBasedOverview;
	}

	private EventUrl getEventUrlByOverview(Overview ov) {
		if (ov != null) {
			if (ov.name().toLowerCase().startsWith(EventUrl.fileBased.name().toLowerCase())) {
				return EventUrl.fileBased;
			} else if (ov.name().toLowerCase().startsWith(EventUrl.order.name().toLowerCase())) {
				return EventUrl.order;
			} else if (ov.name().toLowerCase().startsWith(EventUrl.task.name().toLowerCase())) {
				return EventUrl.task;
			} else if (ov.name().toLowerCase().startsWith(EventUrl.jobChain.name().toLowerCase())) {
				return EventUrl.jobChain;
			} else if (ov.name().toLowerCase().startsWith(EventUrl.event.name().toLowerCase())) {
				return EventUrl.event;
			}
		}
		return EventUrl.event;
	}

	private Long getEventIdFromOverview() throws Exception {
		String method = getMethodName("getEventIdFromOverview");

		LOGGER.debug(String.format("%s: overview=%s", method, overview));

		Long rc = null;
		StringBuilder path = new StringBuilder();
		path.append(webserviceUrl);
		path.append(WEBSERVICE_API_URL);
		path.append(this.eventUrlForEventId.name());

		URIBuilder ub = new URIBuilder(path.toString());
		ub.addParameter("return", overview.name());
		JsonObject result = executeJsonPost(ub.build(), pathParamForEventId);
		JsonNumber eventId = result.getJsonNumber(EventKey.eventId.name());

		LOGGER.debug(String.format("%s: %s", method, result.toString()));

		if (eventId != null) {
			rc = eventId.longValue();
		}
		return rc;
	}

	private JsonObject executeJsonPost(URI uri) throws Exception {
		return executeJsonPost(uri, null);
	}

	private JsonObject executeJsonPost(URI uri, String path) throws Exception {
		String method = getMethodName("executeJsonPost");

		LOGGER.debug(String.format("%s: uri=%s, path=%s", method, uri, path));

		String headerKeyContentType = "Content-Type";
		String headerValueApplication = "application/json";

		client.addHeader(headerKeyContentType, headerValueApplication);
		client.addHeader("Accept", headerValueApplication);
		String response = null;
		if (!SOSString.isEmpty(path)) {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("path", path);
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
				throw new Exception(
						String.format("%s: unexpected content type '%s'. response: %s", method, contentType, response));
			}
		case 400:
			// TO DO check Content-Type
			// for now the exception is plain/text instead of JSON
			// throw message item value
			if (json != null) {
				throw new Exception(json.getString("message"));
			} else {
				throw new Exception(
						String.format("%s: unexpected content type '%s'. response: %s", method, contentType, response));
			}
		case 404:
			throw new NotFoundException(String.format("%s: %s %s, uri=%s", method, statusCode,
					client.getHttpResponse().getStatusLine().getReasonPhrase(), uri.toString()));
		default:
			throw new Exception(String.format("%s: %s %s", method, statusCode,
					client.getHttpResponse().getStatusLine().getReasonPhrase()));
		}
	}

	private JsonObject getEvents(Long eventId) throws Exception {
		String method = getMethodName("getEvents");

		LOGGER.debug(String.format("%s: eventId=%s", method, eventId));

		StringBuilder path = new StringBuilder();
		path.append(webserviceUrl);
		path.append(WEBSERVICE_API_URL);
		path.append(EventUrl.event.name());

		URIBuilder ub = new URIBuilder(path.toString());
		if (!SOSString.isEmpty(eventTypesJoined)) {
			ub.addParameter("return", eventTypesJoined);
		}
		ub.addParameter("timeout", String.valueOf(webserviceTimeout));
		ub.addParameter("after", eventId.toString());
		JsonObject result = executeJsonPost(ub.build());

		LOGGER.debug(String.format("%s: result: %s", method, result.toString()));

		return result;
	}

	public void wait(int interval) {
		if (interval > 0) {
			String method = getMethodName("wait");
			LOGGER.debug(String.format("%s: waiting %s seconds ...", method, interval));
			try {
				Thread.sleep(interval * 1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void setWebServiceUrl() {
		this.webserviceUrl = "http://localhost:" + settings.getHttpPort();
	}

	private String getMethodName(String name) {
		String prefix = this.identifier == null ? "" : String.format("[%s] ", this.identifier);
		return String.format("%s%s", prefix, name);
	}

	public void setIdentifier(String val) {
		this.identifier = val;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public String getWebServiceUrl() {
		return this.webserviceUrl;
	}

	public JobSchedulerRestApiClient getRestApiClient() {
		return client;
	}

	public SchedulerXmlCommandExecutor getXmlCommandExecutor() {
		return this.xmlCommandExecutor;
	}

	public VariableSet getVariableSet() {
		return this.variableSet;
	}

	public EventHandlerSettings getSettings() {
		return this.settings;
	}

	public String getPathParamForEventId() {
		return this.pathParamForEventId;
	}

	public void setPathParamForEventId(String val) {
		this.pathParamForEventId = val;
	}

	public Overview getOverview() {
		return this.overview;
	}

	public int getWaitIntervalOnError() {
		return this.waitIntervalOnError;
	}

	public void setWaitIntervalOnError(int val) {
		this.waitIntervalOnError = val;
	}

	public int getHttpClientSocketTimeout() {
		return this.httpClientSocketTimeout;
	}

	public void setHttpClientSocketTimeout(int val) {
		this.httpClientSocketTimeout = val;
	}

	public int getWebserviceTimeout() {
		return this.webserviceTimeout;
	}

	public void setWebserviceTimeout(int val) {
		this.webserviceTimeout = val;
	}

	public String getEventTypesJoined() {
		return this.eventTypesJoined;
	}

	public EventType[] getEventTypes() {
		return this.eventTypes;
	}
}