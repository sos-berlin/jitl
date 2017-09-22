package com.sos.jitl.restclient;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sos.exception.SOSException;

public class ApiAccessToken {

    private static final String NOT_VALID = "not-valid";
    private JobSchedulerRestApiClient jobSchedulerRestApiClient;
    private String jocUrl;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAccessToken.class);

    public ApiAccessToken(String jocUrl) {
        super();
        this.jocUrl = jocUrl;
    }

    private void createRestApiClient() {
        if (jobSchedulerRestApiClient == null) {
            jobSchedulerRestApiClient = new JobSchedulerRestApiClient();

            JobSchedulerRestClient.accept = "application/json";

            jobSchedulerRestApiClient.addHeader("Content-Type", "application/json");
            jobSchedulerRestApiClient.addHeader("Accept", "application/json");
        }

    }

    private JsonObject jsonFromString(String jsonObjectStr) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }

    private boolean isValid(JsonObject jsonAnswer) {
        return !(jsonAnswer.get("accessToken") == null || jsonAnswer.getString("accessToken").isEmpty() || NOT_VALID.equals(jsonAnswer.getString(
                "accessToken")));
    }

    public boolean isValidUserAccount(String userAccount) throws SOSException, URISyntaxException {
        createRestApiClient();
        String user = jobSchedulerRestApiClient.addAuthorizationHeader(userAccount);

        String s = jocUrl + "/security/userbyname";
        LOGGER.debug("uri:" + s);
        String answer = jobSchedulerRestApiClient.postRestService(new URI(s), user);

        LOGGER.debug("answer:" + answer);
        JsonObject userByNameAnswer = jsonFromString(answer);
        return isValid(userByNameAnswer);
    }

    public boolean isValidAccessToken(String xAccessToken) throws SOSException, URISyntaxException {

        if (xAccessToken == null || xAccessToken.isEmpty() || jocUrl == null || jocUrl.isEmpty()) {
            return false;
        }

        createRestApiClient();
        jobSchedulerRestApiClient.addHeader("X-Access-Token", xAccessToken);

        String s = jocUrl + "/security/userbytoken";
        LOGGER.debug("uri:" + s);
        String answer = jobSchedulerRestApiClient.postRestService(new URI(s), "");
        LOGGER.debug("answer:" + answer);

        JsonObject userByTokenAnswer = jsonFromString(answer);
        return isValid(userByTokenAnswer);

    }

    public String login(String userAccount) throws SOSException, URISyntaxException {
        createRestApiClient();
        
        String s = jocUrl + "/security/login";
        LOGGER.debug("uri:" + s);
        String answer = jobSchedulerRestApiClient.postRestService(new URI(s), "");

        LOGGER.debug("answer:" + answer);
        JsonObject login = jsonFromString(answer);
        if (login.get("accessToken") != null) {
            return login.getString("accessToken");
        } else {
            LOGGER.error(login.toString());
            return "";
        }
    }

    
    public void setJocUrl(String jocUrl) {
        this.jocUrl = jocUrl;
    }

}
