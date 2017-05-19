package com.sos.jitl.inventory.helper;

import java.io.StringReader;
import java.net.URI;
import java.util.concurrent.Callable;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.utils.URIBuilder;

import com.sos.exception.SOSBadRequestException;
import com.sos.exception.SOSNoResponseException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBItemInventoryAgentInstance;
import com.sos.jitl.reporting.db.DBItemInventoryOperatingSystem;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;


public class InventoryAgentCallable implements Callable<CallableAgent> {
    
    private static final String MASTER_WEBSERVICE_URL_APPEND = "/jobscheduler/master/api/agent/";
    private static final String AGENT_WEBSERVICE_URL_APPEND = "/jobscheduler/agent/api";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_HEADER_VALUE = "application/json";
    private final URIBuilder uriBuilder;
    private final DBItemInventoryAgentInstance agentInstance;
    private final String agentUrl;
    
    public InventoryAgentCallable(URIBuilder uriBuilder, DBItemInventoryAgentInstance agentInstance, String agentUrl) {
        this.uriBuilder = uriBuilder;
        this.agentInstance = agentInstance;
        this.agentUrl = agentUrl;
    }

    @Override
    public CallableAgent call() throws Exception {
        CallableAgent ca = new CallableAgent();
        agentInstance.setUrl(agentUrl);
        try {
            JsonObject result = getJsonObjectFromResponse(uriBuilder.build());
            ca.setAgent(agentInstance);
            ca.setResult(result);
            return ca;
        } catch (SOSNoResponseException|SOSBadRequestException e) {
            agentInstance.setHostname(null);
            agentInstance.setOsId(0L);
            agentInstance.setStartedAt(null);
            agentInstance.setState(1);
            agentInstance.setVersion(null);
            ca.setAgent(agentInstance);
            ca.setResult(null);
            return ca;
        } catch (Exception e) {
            return null;
        }
    }

    private JsonObject getJsonObjectFromResponse(URI uri) throws Exception {
        JobSchedulerRestApiClient client = new JobSchedulerRestApiClient();
        client.addHeader(CONTENT_TYPE_HEADER, APPLICATION_HEADER_VALUE);
        client.addHeader(ACCEPT_HEADER, APPLICATION_HEADER_VALUE);
        client.setSocketTimeout(5000);
        String response = client.getRestService(uri);
        int httpReplyCode = client.statusCode();
        String contentType = client.getResponseHeader(CONTENT_TYPE_HEADER);
        JsonObject json = null;
        if (contentType.contains(APPLICATION_HEADER_VALUE)) {
            JsonReader rdr = Json.createReader(new StringReader(response));
            json = rdr.readObject();
        }
        switch (httpReplyCode) {
        case 200:
            if (json != null) {
                client.closeHttpClient();
                return json;
            } else {
                client.closeHttpClient();
                throw new Exception("Unexpected content type '" + contentType + "'. Response: " + response);
            }
        case 400:
            if (json != null) {
                client.closeHttpClient();
                throw new SOSBadRequestException(json.getString("message"));
            } else {
                client.closeHttpClient();
                throw new SOSBadRequestException("Unexpected content type '" + contentType + "'. Response: " + response);
            }
        default:
            client.closeHttpClient();
            throw new Exception(httpReplyCode + " " + client.getHttpResponse().getStatusLine().getReasonPhrase());
        }
    }
}
