package com.sos.jitl.eventhandler.http;

import java.io.StringReader;
import java.net.URI;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.restclient.JobSchedulerRestApiClient;

import javassist.NotFoundException;
import sos.util.SOSString;

public class HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_APPLICATION_JSON = "application/json";

    /* all intervals in seconds */
    private int connectTimeout = 30;
    private int connectionRequestTimeout = 30;
    private int socketTimeout = 75;

    private JobSchedulerRestApiClient client;
    private String identifier;

    public void create() {
        String method = getMethodName("create");

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[connectTimeout=%ss][socketTimeout=%ss][connectionRequestTimeout=%ss]", method, connectTimeout,
                    socketTimeout, connectionRequestTimeout));
        }
        client = new JobSchedulerRestApiClient();
        client.setAutoCloseHttpClient(false);
        client.setConnectionTimeout(connectTimeout * 1000);
        client.setConnectionRequestTimeout(connectionRequestTimeout * 1000);
        client.setSocketTimeout(socketTimeout * 1000);
        client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        client.createHttpClient();
    }

    public void tryCreate() {
        if (client == null) {
            create();
        }
    }

    public void close() {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", getMethodName("close"), client == null ? "[skip]client is NULL" : ""));
        }

        if (client != null) {
            client.closeHttpClient();
            client = null;
        }
    }

    public boolean isClosed() {
        return client == null;
    }

    public String executeGet(URI uri, String contentType, String accept) throws Exception {
        String method = getMethodName("executeGet");
        if (client == null) {
            throw new Exception(String.format("%s[%s]client is NULL", method, uri));
        }
        client.addHeader(HEADER_CONTENT_TYPE, contentType);
        client.addHeader(HEADER_ACCEPT, accept);

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[call]%s", method, uri));
        }
        String response = client.getRestService(uri);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[response]%s", method, response));
        }
        checkResponse(uri, response);
        return response;
    }

    public JsonObject executeJsonGet(URI uri) throws Exception {
        return readJsonObject(uri, executeGet(uri, HEADER_APPLICATION_JSON, HEADER_APPLICATION_JSON));
    }

    public JsonObject executeJsonPost(URI uri) throws Exception {
        return executeJsonPost(uri, null);
    }

    public JsonObject executeJsonPost(URI uri, Map<String, String> bodyParams) throws Exception {
        String body = null;
        if (bodyParams != null) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            for (Map.Entry<String, String> pair : bodyParams.entrySet()) {
                builder.add(pair.getKey(), pair.getValue());
            }
            body = builder.build().toString();
        }
        return readJsonObject(uri, executePost(uri, HEADER_APPLICATION_JSON, HEADER_APPLICATION_JSON, body));
    }

    public String executePost(URI uri, String contentType, String accept) throws Exception {
        return executePost(uri, contentType, accept, null);
    }

    public String executePost(URI uri, String contentType, String accept, String body) throws Exception {
        String method = getMethodName("executePost");
        if (client == null) {
            throw new Exception(String.format("%s[%s]client is NULL", method, uri));
        }
        client.addHeader(HEADER_CONTENT_TYPE, contentType);
        client.addHeader(HEADER_ACCEPT, accept);

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[call][%s][body=%s]", method, uri, body));
        }
        String response = client.postRestService(uri, body);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[response]%s", method, response));
        }
        checkResponse(uri, response);
        return response;
    }

    private void checkResponse(URI uri, String response) throws Exception {
        String method = getMethodName("checkResponse");
        int statusCode = client.statusCode();
        String contentType = client.getResponseHeader(HEADER_CONTENT_TYPE);
        if (isTraceEnabled) {
            LOGGER.trace(String.format("%s[%s][%s]", method, statusCode, contentType));
        }
        switch (statusCode) {
        case 200:
            if (SOSString.isEmpty(response)) {
                throw new Exception(String.format("%s[%s][%s][%s]response is empty", method, uri, statusCode, contentType));
            }
            break;
        case 404:
            throw new NotFoundException(String.format("%s[%s][%s][%s]%s", method, uri, statusCode, contentType, getResponseReason()));
        default:
            throw new Exception(String.format("%s[%s][%s][%s]%s", method, uri, statusCode, contentType, getResponseReason()));
        }
    }

    private String getResponseReason() {
        try {
            return client.getHttpResponse().getStatusLine().getReasonPhrase();
        } catch (Throwable t) {
        }
        return "";
    }

    private JsonObject readJsonObject(URI uri, String response) throws Exception {
        JsonObject json = null;
        StringReader sr = new StringReader(response);
        JsonReader jr = Json.createReader(sr);
        try {
            json = jr.readObject();
        } catch (Throwable e) {
            LOGGER.error(String.format("%s[%s][readJsonObject]%s", getMethodName("readJsonObject"), uri.toString(), e.toString()), e);
            throw e;
        } finally {
            jr.close();
            sr.close();
        }

        return json;
    }

    public String getMethodName(String name) {
        String prefix = identifier == null ? "" : String.format("[%s]", identifier);
        return String.format("%s[%s]", prefix, name);
    }

    public void setIdentifier(String val) {
        identifier = val;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setConnectionRequestTimeout(int val) {
        connectionRequestTimeout = val;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectTimeout(int val) {
        connectTimeout = val;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setSocketTimeout(int val) {
        socketTimeout = val;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }
}
