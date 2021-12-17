package com.sos.jitl.restclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sos.exception.SOSException;
import com.sos.exception.SOSMissingDataException;
import com.sos.exception.SOSSSLException;

public class ApiAccessToken {

    private static final String NOT_VALID = "not-valid";
    private JobSchedulerRestApiClient jobSchedulerRestApiClient;
    private String jocUrl;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAccessToken.class);

    public ApiAccessToken(String jocUrl) {
        super();
        this.jocUrl = jocUrl;
    }

    private void addSSLContext(WebserviceCredentials webserviceCredentials) throws SOSSSLException, SOSMissingDataException, KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException {

        LOGGER.debug("add SSLContext to REST api client");
        jobSchedulerRestApiClient.setKeyPass(webserviceCredentials.getKeyPassword());
        jobSchedulerRestApiClient.setKeystoreType(webserviceCredentials.getKeyStoreType());
        jobSchedulerRestApiClient.setKeystorePass(webserviceCredentials.getKeyStorePassword());
        jobSchedulerRestApiClient.setTruststorePass(webserviceCredentials.getTrustStorePassword());
        jobSchedulerRestApiClient.setTruststoreType(webserviceCredentials.getTrustStoreType());
        jobSchedulerRestApiClient.setTrustStore(webserviceCredentials.getTrustStorePath());
        jobSchedulerRestApiClient.setKeyStore(webserviceCredentials.getKeyStorePath());
        jobSchedulerRestApiClient.setSSLContext();

    }

    private void createRestApiClient(WebserviceCredentials webserviceCredentials) {
        if (jobSchedulerRestApiClient == null) {
            jobSchedulerRestApiClient = new JobSchedulerRestApiClient();

            jobSchedulerRestApiClient.addHeader("Content-Type", "application/json");
            jobSchedulerRestApiClient.addHeader("Accept", "application/json");
            try {
                addSSLContext(webserviceCredentials);
            } catch (SOSSSLException | SOSMissingDataException | KeyStoreException | NoSuchAlgorithmException | CertificateException
                    | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private JsonObject jsonFromString(String jsonObjectStr) {
        if ("".equals(jsonObjectStr) || jsonObjectStr == null) {
            jsonObjectStr = "{}";
        }
        JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;

    }

    private boolean isValid(JsonObject jsonAnswer) {
        return !(jsonAnswer.get("accessToken") == null || jsonAnswer.getString("accessToken").isEmpty() || NOT_VALID.equals(jsonAnswer.getString(
                "accessToken")));
    }

    public boolean isValidUserAccount(String userAccount, WebserviceCredentials webserviceCredentials) throws SOSException, URISyntaxException {
        createRestApiClient(webserviceCredentials);
        String user = jobSchedulerRestApiClient.addAuthorizationHeader(userAccount);

        String s = jocUrl + "/security/userbyname";
        LOGGER.debug("uri:" + s);
        LOGGER.debug("user:" + user);
        String answer = jobSchedulerRestApiClient.postRestService(new URI(s), user);

        LOGGER.debug("answer:" + answer);
        JsonObject userByNameAnswer = jsonFromString(answer);
        return isValid(userByNameAnswer);
    }

    public boolean isValidAccessToken(String xAccessToken, WebserviceCredentials webserviceCredentials) throws SOSException, URISyntaxException {

        boolean valid = false;
        if (xAccessToken == null || xAccessToken.isEmpty() || jocUrl == null || jocUrl.isEmpty()) {
            LOGGER.debug("Empty Access-Token or empty jocUrl");
            return false;
        }

        createRestApiClient(webserviceCredentials);
        jobSchedulerRestApiClient.addHeader("X-Access-Token", xAccessToken);

        String s = jocUrl + "/security/userbytoken";
        LOGGER.debug("uri:" + s);
        String answer = jobSchedulerRestApiClient.postRestService(new URI(s), "");
        LOGGER.debug("answer:" + answer);

        JsonObject userByTokenAnswer = jsonFromString(answer);
        valid = isValid(userByTokenAnswer);
        if (valid) {
            s = jocUrl + "/jobscheduler/ids";
            LOGGER.debug("uri:" + s);
            answer = jobSchedulerRestApiClient.postRestService(new URI(s), "");
            LOGGER.debug("answer:" + answer);

            JsonObject schedulerIds = jsonFromString(answer);
            valid = (schedulerIds.get("error") == null);
        }
        return valid;

    }

    public String login(WebserviceCredentials webserviceCredentials) throws SOSException, URISyntaxException {
        createRestApiClient(webserviceCredentials);
        jobSchedulerRestApiClient.addAuthorizationHeader(webserviceCredentials.getUserDecodedAccount());

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
