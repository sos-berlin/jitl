package com.sos.jitl.restclient;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import com.sos.exception.SOSException;
import com.sos.exception.SOSMissingDataException;
import com.sos.exception.SOSSSLException;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebserviceExecuter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebserviceExecuter.class);
    protected String accessToken = "";
    protected JobSchedulerRestApiClient jobSchedulerRestApiClient;
    protected String schedulerId;
    protected WebserviceCredentials webserviceCredentials;

    public WebserviceExecuter(WebserviceCredentials webserviceCredentials) {
        super();
        jobSchedulerRestApiClient = new JobSchedulerRestApiClient();
        this.webserviceCredentials = webserviceCredentials;

        try {
            addSSLContext();
        } catch (SOSSSLException | SOSMissingDataException | KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            e.printStackTrace();
        }
    }

    private void addSSLContext() throws SOSSSLException, SOSMissingDataException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException {

        if (webserviceCredentials != null) {
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
    }

    protected BigInteger string2BigInteger(String s) {
        try {
            return new BigInteger(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Integer string2Integer(String s) {
        try {
            return new Integer(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Date string2Date(String s) {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date;
        try {
            date = format.parse(s);
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    protected JsonObject jsonFromString(String jsonObjectStr) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }

    public void login() throws SOSException, URISyntaxException {
        jobSchedulerRestApiClient.addHeader("Content-Type", "application/json");
        jobSchedulerRestApiClient.addHeader("Accept", "application/json");
        jobSchedulerRestApiClient.addAuthorizationHeader(webserviceCredentials.getUserDecodedAccount());

        String answer = jobSchedulerRestApiClient.postRestService(new URI(webserviceCredentials.getJocUrl() + "/security/login"), "");
        JsonObject login = jsonFromString(answer);
        if (login.get("accessToken") != null) {
            accessToken = login.getString("accessToken");
            jobSchedulerRestApiClient.addHeader("X-Access-Token", accessToken);
        }
    }

    public void login(String xAccessToken) throws SOSException, URISyntaxException {
        if (xAccessToken != null && !xAccessToken.isEmpty()) {
            jobSchedulerRestApiClient.addHeader("Content-Type", "application/json");
            jobSchedulerRestApiClient.addHeader("Accept", "application/json");

            accessToken = xAccessToken;
            jobSchedulerRestApiClient.addHeader("X-Access-Token", xAccessToken);
        } else {
            login();
        }
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public String getAccessToken() {
        return accessToken;
    }

}
