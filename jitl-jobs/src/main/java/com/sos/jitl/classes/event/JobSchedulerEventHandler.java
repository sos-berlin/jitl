package com.sos.jitl.classes.event;

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
import com.sos.jitl.restclient.JobSchedulerRestApiClient;

import javassist.NotFoundException;
import sos.util.SOSString;

public class JobSchedulerEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerEventHandler.class);

	public static enum EventType {
		FileBasedEvent, FileBasedAdded, FileBasedRemoved, FileBasedReplaced, FileBasedActivated, TaskEvent, TaskStarted, TaskEnded, TaskClosed, OrderEvent, OrderStarted, OrderFinished, OrderStepStarted, OrderStepEnded, OrderSetBack, OrderNodeChanged, OrderSuspended, OrderResumed, JobChainEvent, JobChainStateChanged, JobChainNodeActionChanged, SchedulerClosed
	};

	public static enum EventSeq {
		NonEmpty, Empty, Torn
	};

	public static enum EventPath {
		event, fileBased, task, order, jobChain
	};

	public static enum EventKey {
		TYPE, key, eventId, eventSnapshots, jobPath
	};

	public static enum EventOverview {
		FileBasedOverview, FileBasedDetailed, TaskOverview, OrderOverview, JobChainOverview
	};

	public static final String MASTER_API_PATH = "/jobscheduler/master/api/";

	private String identifier;

	private String baseUrl = null;
	private JobSchedulerRestApiClient client;

	private int httpClientSocketTimeout = 65000;
	private int webserviceTimeout = 60;

	public JobSchedulerEventHandler() {
	}

	public void createRestApiClient() {
		String method = getMethodName("createRestApiClient");

		LOGGER.info(String.format("%s: socketTimeout=%s", method, this.httpClientSocketTimeout));
		client = new JobSchedulerRestApiClient();
		client.setAutoCloseHttpClient(false);
		client.setSocketTimeout(this.httpClientSocketTimeout);
		client.createHttpClient();
	}

	public void closeRestApiClient() {
		String method = getMethodName("closeRestApiClient");

		if (client != null) {
			LOGGER.info(String.format("%s", method));
			client.closeHttpClient();
		} else {
			LOGGER.info(String.format("%s: skip. client is NULL", method));
		}
		client = null;
	}

	public void setBaseUrl(String httpPort) {
		this.baseUrl = "http://localhost:" + httpPort;
	}

	public JsonObject getOverview(EventPath ep) throws Exception {
		return getOverview(ep, getEventOverviewByEventPath(ep), null);
	}

	public JsonObject getOverview(EventPath ep, String path) throws Exception {
		return getOverview(ep, getEventOverviewByEventPath(ep), path);
	}

	public JsonObject getOverview(EventPath ep, EventOverview eo) throws Exception {
		return getOverview(ep, eo, null);
	}

	public JsonObject getOverview(EventPath ep, EventOverview eo, String path) throws Exception {
		String method = getMethodName("getOverview");

		LOGGER.debug(String.format("%s: eventPath=%s, eventOverview=%s, path=%s", method, ep, eo, path));
		URIBuilder ub = new URIBuilder(getUri(ep));
		ub.addParameter("return", eo.name());
		return executeJsonPost(ub.build(), path);
	}

	public JsonObject getEvents(Long eventId, EventType[] eventTypes) throws Exception {
		return getEvents(eventId, joinEventTypes(eventTypes), null);
	}

	public JsonObject getEvents(Long eventId, EventType[] eventTypes, String path) throws Exception {
		return getEvents(eventId, joinEventTypes(eventTypes), path);
	}

	public JsonObject getEvents(Long eventId, String eventTypes) throws Exception {
		return getEvents(eventId, eventTypes, null);
	}

	public JsonObject getEvents(Long eventId, String eventTypes, String path) throws Exception {
		String method = getMethodName("getEvents");

		LOGGER.debug(String.format("%s: eventId=%s, eventTypes=%s, path=%s", method, eventId, eventTypes, path));

		URIBuilder ub = new URIBuilder(getUri(EventPath.event));
		if (!SOSString.isEmpty(eventTypes)) {
			ub.addParameter("return", eventTypes);
		}
		ub.addParameter("timeout", String.valueOf(webserviceTimeout));
		ub.addParameter("after", eventId.toString());
		JsonObject result = executeJsonPost(ub.build(), path);

		LOGGER.debug(String.format("%s: result: %s", method, result.toString()));

		return result;
	}

	public JsonObject executeJsonPost(URI uri) throws Exception {
		return executeJsonPost(uri, null);
	}

	public JsonObject executeJsonPost(URI uri, String path) throws Exception {
		String method = getMethodName("executeJsonPost");

		LOGGER.debug(String.format("%s: uri=%s, path=%s", method, uri, path));

		String headerKeyContentType = "Content-Type";
		String headerValueApplication = "application/json";

		client.addHeader(headerKeyContentType, headerValueApplication);
		client.addHeader("Accept", headerValueApplication);
		String body = null;
		if (!SOSString.isEmpty(path)) {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("path", path);
			body = builder.build().toString();
		}
		String response = client.postRestService(uri, body);
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

	public Long getEventId(JsonObject result) {
		Long rc = null;
		if (result != null) {
			JsonNumber eventId = result.getJsonNumber(EventKey.eventId.name());
			if (eventId != null) {
				rc = eventId.longValue();
			}
		}
		return rc;
	}

	public String getEventType(JsonObject result) {
		return result == null ? null : result.getString(EventKey.TYPE.name());
	}

	public JsonArray getEventSnapshots(JsonObject result) {
		return result == null ? null : result.getJsonArray(EventKey.eventSnapshots.name());
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

	public String joinEventTypes(EventType[] et) {
		return et == null ? "" : Joiner.on(",").join(et);
	}

	public EventOverview getEventOverviewByEventTypes(EventType[] et) {
		if (et != null) {
			String firstEventType = et[0].name();
			if (firstEventType.toLowerCase().startsWith(EventPath.fileBased.name().toLowerCase())) {
				return EventOverview.FileBasedOverview;
			} else if (firstEventType.toLowerCase().startsWith(EventPath.order.name().toLowerCase())) {
				return EventOverview.OrderOverview;
			} else if (firstEventType.toLowerCase().startsWith(EventPath.task.name().toLowerCase())) {
				return EventOverview.TaskOverview;
			} else if (firstEventType.toLowerCase().startsWith(EventPath.jobChain.name().toLowerCase())) {
				return EventOverview.JobChainOverview;
			}
		}
		return null;
	}

	public EventOverview getEventOverviewByEventPath(EventPath ep) {
		if (ep != null) {
			if (ep.equals(EventPath.fileBased)) {
				return EventOverview.FileBasedOverview;
			} else if (ep.equals(EventPath.order)) {
				return EventOverview.OrderOverview;
			} else if (ep.equals(EventPath.task)) {
				return EventOverview.TaskOverview;
			} else if (ep.equals(EventPath.jobChain)) {
				return EventOverview.JobChainOverview;
			}
		}
		return null;
	}

	public EventPath getEventPathByEventOverview(EventOverview ov) {
		if (ov != null) {
			if (ov.name().toLowerCase().startsWith(EventPath.fileBased.name().toLowerCase())) {
				return EventPath.fileBased;
			} else if (ov.name().toLowerCase().startsWith(EventPath.order.name().toLowerCase())) {
				return EventPath.order;
			} else if (ov.name().toLowerCase().startsWith(EventPath.task.name().toLowerCase())) {
				return EventPath.task;
			} else if (ov.name().toLowerCase().startsWith(EventPath.jobChain.name().toLowerCase())) {
				return EventPath.jobChain;
			} else if (ov.name().toLowerCase().startsWith(EventPath.event.name().toLowerCase())) {
				return EventPath.event;
			}
		}
		return null;
	}

	public String getMethodName(String name) {
		String prefix = this.identifier == null ? "" : String.format("[%s] ", this.identifier);
		return String.format("%s%s", prefix, name);
	}

	public URI getUri(EventPath eventPath) throws URISyntaxException {
		if (this.baseUrl == null) {
			throw new URISyntaxException("null", "baseUrl is NULL");
		}
		if (eventPath == null) {
			throw new URISyntaxException("null", "eventPath is NULL");
		}
		StringBuilder uri = new StringBuilder();
		uri.append(baseUrl);
		uri.append(MASTER_API_PATH);
		uri.append(eventPath.name());
		return new URI(uri.toString());
	}

	public void setIdentifier(String val) {
		this.identifier = val;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public String getBaseUrl() {
		return this.baseUrl;
	}

	public JobSchedulerRestApiClient getRestApiClient() {
		return client;
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

}