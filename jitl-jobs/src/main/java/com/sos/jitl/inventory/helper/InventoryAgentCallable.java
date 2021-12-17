package com.sos.jitl.inventory.helper;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.Callable;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;

import com.google.common.base.Charsets;
import com.sos.exception.SOSBadRequestException;
import com.sos.exception.SOSNoResponseException;
import com.sos.jitl.reporting.db.DBItemInventoryAgentInstance;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;


public class InventoryAgentCallable implements Callable<CallableAgent> {
    
    @SuppressWarnings("unused")
    private static final String MASTER_WEBSERVICE_URL_APPEND = "/jobscheduler/master/api/agent/";
    @SuppressWarnings("unused")
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
    public CallableAgent call() {
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
    	HttpURLConnection connection = null;
        try {
            JobSchedulerRestApiClient client = new JobSchedulerRestApiClient();
			connection = client.getHttpURLConnection(uri.toURL(), 5000, 5000);
			int responseCode = connection.getResponseCode();
			String contentType = connection.getContentType();
			String response = null;
			JsonObject json = null;
			switch (responseCode) {
			case 200:
				response = IOUtils.toString(connection.getInputStream(), Charsets.UTF_8);
			    if (contentType.contains(APPLICATION_HEADER_VALUE)) {
			        JsonReader rdr = Json.createReader(new StringReader(response));
			        json = rdr.readObject();
			    }
			    if (json != null) {
			        return json;
			    } else {
			        throw new Exception("Unexpected content type '" + contentType + "'. Response: " + response);
			    }
			case 400:
				response = IOUtils.toString(connection.getErrorStream(), Charsets.UTF_8);
			    if (contentType.contains(APPLICATION_HEADER_VALUE)) {
			        JsonReader rdr = Json.createReader(new StringReader(response));
			        json = rdr.readObject();
			    }
			    if (json != null) {
			        throw new SOSBadRequestException(json.getString("message"));
			    } else {
			        throw new SOSBadRequestException("Unexpected content type '" + contentType + "'. Response: " + response);
			    }
			default:
			    throw new SOSBadRequestException(responseCode + " " + client.getHttpResponse().getStatusLine().getReasonPhrase());
			}
		} finally {
			if (connection != null) {
				connection.disconnect();
			};
		}
    }
}
