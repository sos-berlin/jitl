package com.sos.jitl.eventhandler.handler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.sos.jitl.eventhandler.EventMeta;
import com.sos.jitl.eventhandler.EventMeta.EventKey;
import com.sos.jitl.eventhandler.EventMeta.EventOverview;
import com.sos.jitl.eventhandler.EventMeta.EventPath;
import com.sos.jitl.eventhandler.EventMeta.EventType;
import com.sos.jitl.eventhandler.http.HttpClient;

import sos.util.SOSString;

public class EventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private final HttpClient httpClient;

    private int webserviceLimit = 1_000;
    // is seconds
    private int webserviceTimeout = 60;
    private int webserviceDelay = 0;

    private String baseUrl;
    private String identifier;

    public EventHandler() {
        httpClient = new HttpClient();
    }

    public JsonObject getOverview(EventPath path) throws Exception {
        return getOverview(path, getEventOverviewByEventPath(path), null);
    }

    public JsonObject getOverview(EventPath path, String bodyParamPath) throws Exception {
        return getOverview(path, getEventOverviewByEventPath(path), bodyParamPath);
    }

    public JsonObject getOverview(EventPath path, EventOverview overview) throws Exception {
        return getOverview(path, overview, null);
    }

    public JsonObject getOverview(EventPath path, EventOverview overview, String bodyParamPath) throws Exception {
        String method = getMethodName("getOverview");
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[eventPath=%s][eventOverview=%s][bodyParamPath=%s]", method, path, overview, bodyParamPath));
        }
        URIBuilder ub = new URIBuilder(getUri(path));
        ub.addParameter("return", overview.name());
        Map<String, String> bodyParams = null;
        if (bodyParamPath != null) {
            bodyParams = Collections.singletonMap("path", bodyParamPath);
        }
        return httpClient.executeJsonPost(ub.build(), bodyParams);
    }

    public JsonObject getEvents(Long eventId, EventType[] eventTypes) throws Exception {
        return getEvents(eventId, joinEventTypes(eventTypes), null);
    }

    public JsonObject getEvents(Long eventId, EventType[] eventTypes, String bodyParamPath) throws Exception {
        return getEvents(eventId, joinEventTypes(eventTypes), bodyParamPath);
    }

    public JsonObject getEvents(Long eventId, String eventTypes) throws Exception {
        return getEvents(eventId, eventTypes, null);
    }

    public JsonObject getEvents(Long eventId, String eventTypes, String bodyParamPath) throws Exception {
        String method = getMethodName("getEvents");

        if (isDebugEnabled) {
            String bodyParamMsg = SOSString.isEmpty(bodyParamPath) ? "" : String.format("[bodyParamPath=%s]", bodyParamPath);
            LOGGER.debug(String.format("%s[%s][%s]%s", method, eventId, eventTypes, bodyParamMsg));
        }

        URIBuilder ub = new URIBuilder(getUri(EventPath.event));
        if (!SOSString.isEmpty(eventTypes)) {
            ub.addParameter("return", eventTypes);
        }
        ub.addParameter("after", eventId.toString());
        if (webserviceTimeout > 0) {
            ub.addParameter("timeout", String.valueOf(webserviceTimeout));
        }
        if (webserviceDelay > 0) {
            ub.addParameter("delay", String.valueOf(webserviceDelay));
        }
        if (webserviceLimit > 0) {
            ub.addParameter("limit", String.valueOf(webserviceLimit));
        }
        if (SOSString.isEmpty(bodyParamPath)) {
            return httpClient.executeJsonGet(ub.build());
        }
        Map<String, String> bodyParams = null;
        if (bodyParamPath != null) {
            bodyParams = Collections.singletonMap("path", bodyParamPath);
        }
        return httpClient.executeJsonPost(ub.build(), bodyParams);
    }

    public Long getEventId(JsonObject json) {
        Long eventId = null;
        if (json != null) {
            JsonNumber r = json.getJsonNumber(EventKey.eventId.name());
            if (r != null) {
                eventId = r.longValue();
            }
        }
        return eventId;
    }

    public String getEventType(JsonObject json) {
        return json == null ? null : json.getString(EventKey.TYPE.name());
    }

    public JsonArray getEventSnapshots(JsonObject json) {
        return json == null ? null : json.getJsonArray(EventKey.eventSnapshots.name());
    }

    public String getEventKey(JsonObject json) {
        String eventKey = null;
        JsonValue key = json.get(EventKey.key.name());
        if (key != null) {
            if (key.getValueType().equals(ValueType.STRING)) {
                eventKey = key.toString();
            } else if (key.getValueType().equals(ValueType.OBJECT)) {
                if (((JsonObject) key).containsKey(EventKey.jobPath.name())) {
                    eventKey = ((JsonObject) key).getString(EventKey.jobPath.name());
                }
            }
        }
        return eventKey;
    }

    public String joinEventTypes(EventType[] type) {
        return type == null ? "" : Joiner.on(",").join(type);
    }

    public EventOverview getEventOverviewByEventTypes(EventType[] type) {
        if (type != null && type.length > 0) {
            String first = type[0].name();
            if (first.toLowerCase().startsWith(EventPath.fileBased.name().toLowerCase())) {
                return EventOverview.FileBasedOverview;
            } else if (first.toLowerCase().startsWith(EventPath.order.name().toLowerCase())) {
                return EventOverview.OrderOverview;
            } else if (first.toLowerCase().startsWith(EventPath.task.name().toLowerCase())) {
                return EventOverview.TaskOverview;
            } else if (first.toLowerCase().startsWith(EventPath.jobChain.name().toLowerCase())) {
                return EventOverview.JobChainOverview;
            }
        }
        return null;
    }

    public EventOverview getEventOverviewByEventPath(EventPath path) {
        if (path != null) {
            if (path.equals(EventPath.fileBased)) {
                return EventOverview.FileBasedOverview;
            } else if (path.equals(EventPath.order)) {
                return EventOverview.OrderOverview;
            } else if (path.equals(EventPath.task)) {
                return EventOverview.TaskOverview;
            } else if (path.equals(EventPath.jobChain)) {
                return EventOverview.JobChainOverview;
            }
        }
        return null;
    }

    public EventPath getEventPathByEventOverview(EventOverview overview) {
        if (overview != null) {
            if (overview.name().toLowerCase().startsWith(EventPath.fileBased.name().toLowerCase())) {
                return EventPath.fileBased;
            } else if (overview.name().toLowerCase().startsWith(EventPath.order.name().toLowerCase())) {
                return EventPath.order;
            } else if (overview.name().toLowerCase().startsWith(EventPath.task.name().toLowerCase())) {
                return EventPath.task;
            } else if (overview.name().toLowerCase().startsWith(EventPath.jobChain.name().toLowerCase())) {
                return EventPath.jobChain;
            } else if (overview.name().toLowerCase().startsWith(EventPath.event.name().toLowerCase())) {
                return EventPath.event;
            }
        }
        return null;
    }

    public void setBaseUrl(String host, String port) {
        baseUrl = String.format("http://%s:%s", host, port);
    }

    public URI getUri(EventPath path) throws URISyntaxException {
        if (baseUrl == null) {
            throw new URISyntaxException("null", "baseUrl is NULL");
        }
        if (path == null) {
            throw new URISyntaxException("null", "path is NULL");
        }
        StringBuilder uri = new StringBuilder();
        uri.append(baseUrl);
        uri.append(EventMeta.MASTER_API_PATH);
        uri.append(path.name());
        return new URI(uri.toString());
    }

    public String getMethodName(String name) {
        String prefix = identifier == null ? "" : String.format("[%s]", identifier);
        return String.format("%s[%s]", prefix, name);
    }

    public void setIdentifier(String val) {
        identifier = val;
        httpClient.setIdentifier(identifier);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getWebserviceTimeout() {
        return webserviceTimeout;
    }

    public void setWebserviceTimeout(int val) {
        webserviceTimeout = val;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

}