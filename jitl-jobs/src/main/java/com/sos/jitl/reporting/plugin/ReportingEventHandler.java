package com.sos.jitl.reporting.plugin;

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

public class ReportingEventHandler implements IReportingEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportingEventHandler.class);

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
	private static final Integer HTTP_CLIENT_SOCKET_TIMEOUT = 65000;
	private static final String WEBSERVICE_PARAM_VALUE_TIMEOUT = "60";

	private SchedulerXmlCommandExecutor xmlCommandExecutor;
	private VariableSet variableSet;
	private SchedulerAnswer schedulerAnswer;

	private Overview overview;
	private EventType[] eventTypes;
	private String webserviceUrl = null;
	private JobSchedulerRestApiClient client;
	private String pathParamForEventId = "/";

	private boolean closed = false;
	private int waitIntervalOnError = 5;

	public ReportingEventHandler() {
	}

	@Override
	public void onPrepare(SchedulerXmlCommandExecutor sxce, VariableSet vs, SchedulerAnswer sa) {
		this.xmlCommandExecutor = sxce;
		this.variableSet = vs;
		this.schedulerAnswer = sa;
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

	public void start() {
		start(null, null);
	}

	public void start(EventType[] et) {
		start(null, et);
	}

	public void start(Overview ov, EventType[] et) {
		String method = "start";
		if (closed) {
			LOGGER.info(String.format("%s: processing stopped.", method));
			return;
		}
		tryClientConnect();

		this.eventTypes = et;
		if (ov == null) {
			ov = getOverviewByEventTypes(eventTypes);
		}
		this.overview = ov;

		LOGGER.debug(String.format("%s: overview=%s, eventTypes=%s", method, overview, joinEventTypes(eventTypes)));

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
		String method = "onEmptyEvent";
		LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
		try {
			process(eventId);
		} catch (Exception e) {
			rerunProcess(method, e, eventId);
		}
	}

	public void onNonEmptyEvent(Long eventId, String type, JsonArray events) {
		String method = "onNonEmptyEvent";
		LOGGER.debug(String.format("%s: eventId=%s, type=%s", method, eventId, type));
		try {
			process(eventId);
		} catch (Exception e) {
			rerunProcess(method, e, eventId);
		}
	}

	public void onTornEvent(Long eventId, String type, JsonArray events) {
		String method = "onTornEvent";
		if (closed) {
			LOGGER.info(String.format("%s: processing stopped.", method));
		} else {
			LOGGER.debug(String.format("%s: eventId=%s, type=%s", method, eventId, type));
			onRestart(eventId, type, events);
			start(overview, eventTypes);
		}
	}

	public void onRestart(Long eventId, String type, JsonArray events) {
		LOGGER.debug(String.format("onRestart: eventId=%s, type=%s", eventId, type));
	}

	public String joinEventTypes(EventType[] et) {
		return et == null ? "" : Joiner.on(",").join(et);
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

	private void rerunProcess(String callerMethod, Exception ex, Long eventId) {
		String method = "rerunProcess";
		if (closed) {
			LOGGER.info(String.format("%s: processing stopped.", method));
			return;
		}
		if (ex != null) {
			LOGGER.error(String.format("error on %s: %s", callerMethod, ex.toString()), ex);
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
		String method = "rerunGetEventIdFromOverview";
		if (closed) {
			LOGGER.info(String.format("%s: processing stopped.", method));
			return null;
		}

		if (ex != null) {
			LOGGER.error(String.format("error on %s: %s", callerMethod, ex.toString()), ex);
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
		if (closed) {
			return null;
		}
		tryClientConnect();

		LOGGER.debug(String.format("process: eventId=%s", eventId));

		JsonObject result = getEvents(eventId);
		JsonArray events = result.getJsonArray(EventKey.eventSnapshots.name());
		String type = result.getString(EventKey.TYPE.name());
		eventId = result.getJsonNumber(EventKey.eventId.name()).longValue();

		if (type.equalsIgnoreCase(EventSeq.NonEmpty.name())) {
			onNonEmptyEvent(eventId, type, events);
		} else if (type.equalsIgnoreCase(EventSeq.Empty.name())) {
			onEmptyEvent(eventId);
		} else if (type.equalsIgnoreCase(EventSeq.Torn.name())) {
			onTornEvent(eventId, type, events);
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
			} else if (overview.name().toLowerCase().startsWith(EventUrl.event.name().toLowerCase())) {
				return EventUrl.event;
			}
		}
		return null;
	}

	private Long getEventIdFromOverview() throws Exception {
		LOGGER.debug(String.format("getEventIdFromOverview: overview=%s", overview));

		Long rc = null;
		StringBuilder path = new StringBuilder();
		path.append(webserviceUrl);
		path.append(WEBSERVICE_API_URL);
		path.append(getEventUrlByOverview(overview));

		URIBuilder ub = new URIBuilder(path.toString());
		ub.addParameter("return", overview.name());
		JsonObject result = executeJsonPost(ub.build(), pathParamForEventId);
		JsonNumber eventId = result.getJsonNumber(EventKey.eventId.name());

		LOGGER.debug(result.toString());

		if (eventId != null) {
			rc = eventId.longValue();
		}
		return rc;
	}

	private JsonObject executeJsonPost(URI uri) throws Exception {
		return executeJsonPost(uri, null);
	}

	private JsonObject executeJsonPost(URI uri, String path) throws Exception {
		LOGGER.debug(String.format("executeJsonPost: uri=%s, path=%s", uri, path));

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

	private JsonObject getEvents(Long eventId) throws Exception {
		LOGGER.debug(String.format("getEvents: eventId=%s", eventId));

		StringBuilder path = new StringBuilder();
		path.append(webserviceUrl);
		path.append(WEBSERVICE_API_URL);
		path.append(EventUrl.event.name());

		URIBuilder ub = new URIBuilder(path.toString());
		if (eventTypes != null) {
			ub.addParameter("return", joinEventTypes(eventTypes));
		}
		ub.addParameter("timeout", WEBSERVICE_PARAM_VALUE_TIMEOUT);
		ub.addParameter("after", eventId.toString());
		JsonObject result = executeJsonPost(ub.build());

		LOGGER.debug("result: " + result.toString());

		return result;
	}

	public void wait(int interval) {
		if (interval > 0) {
			LOGGER.debug(String.format("waiting %s seconds ...", interval));
			try {
				Thread.sleep(interval * 1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void setWebServiceUrl() {
		this.webserviceUrl = "http://localhost:" + schedulerAnswer.getHttpPort();
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

	public SchedulerAnswer getSchedulerAnswer() {
		return this.schedulerAnswer;
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

	public EventType[] getEventTypes() {
		return this.eventTypes;
	}
}