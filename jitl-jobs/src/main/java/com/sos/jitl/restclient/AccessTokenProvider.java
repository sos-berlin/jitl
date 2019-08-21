package com.sos.jitl.restclient;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSException;
import com.typesafe.config.ConfigException;

import sos.spooler.Spooler;
import sos.util.SOSPrivateConf;

public class AccessTokenProvider {

    private static final String DEFAULT_PRIVATE_CONF_FILENAME = "config/private/private.conf";
    private static final String JOC_URL = "joc_url";
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenProvider.class);
    private static final String X_ACCESS_TOKEN = "X-Access-Token";
    private static final int MAX_WAIT_TIME_FOR_ACCESS_TOKEN = 3;
    private String jocUrl;
    private String profileFileName;

    public AccessTokenProvider() {
        super();
        profileFileName = DEFAULT_PRIVATE_CONF_FILENAME;
    }

    public AccessTokenProvider(String profileFileName) {
        super();
        this.profileFileName = profileFileName;
    }

    private void setSpoolerVariable(Spooler spooler, String name, String value) {
        if (spooler != null) {
            spooler.variables().set_value(name, value);
        }

    }

    private String getSpoolerVariable(Spooler spooler, String name) {
        if (spooler != null) {
            return spooler.variables().value(name);
        } else {
            return "";
        }

    }

    public WebserviceCredentials getAccessToken(Spooler spooler) throws InterruptedException, SOSException, URISyntaxException,
            UnsupportedEncodingException {

        String schedulerId = "";
        String xAccessToken = getSpoolerVariable(spooler, X_ACCESS_TOKEN);
        jocUrl = getSpoolerVariable(spooler, JOC_URL);

        if (jocUrl == null) {
            jocUrl = "";
        }

        ApiAccessToken apiAccessToken = new ApiAccessToken(jocUrl);

        LOGGER.debug("Check whether accessToken is valid");
        if (xAccessToken.isEmpty() || !apiAccessToken.isValidAccessToken(xAccessToken)) {
            xAccessToken = executeLogin();
            apiAccessToken.setJocUrl(jocUrl);

            if (xAccessToken != null && !xAccessToken.isEmpty()) {
                LOGGER.debug("... set accessToken:" + xAccessToken);
                setSpoolerVariable(spooler, X_ACCESS_TOKEN, xAccessToken);
                setSpoolerVariable(spooler, JOC_URL, jocUrl);
            } else {
                LOGGER.debug("AccessToken " + xAccessToken + " is not valid. Trying to renew it...");
                java.lang.Thread.sleep(1000);
            }

        }

        if (!apiAccessToken.isValidAccessToken(xAccessToken)) {
            return null;
        }

        if (spooler != null) {
            schedulerId = spooler.id();
        } else {
            schedulerId = "test";
        }

        WebserviceCredentials webserviceCredentials = new WebserviceCredentials();
        webserviceCredentials.setSchedulerId(schedulerId);
        webserviceCredentials.setAccessToken(xAccessToken);
        return webserviceCredentials;

    }

    public String getJocUrl() {
        return jocUrl;
    }

    private String executeLogin() throws UnsupportedEncodingException, SOSException, URISyntaxException {
        SOSPrivateConf sosPrivateConf;
        sosPrivateConf = new SOSPrivateConf(profileFileName);

        try {
            jocUrl = sosPrivateConf.getValue("joc.webservice.jitl", "joc.url");
        } catch (ConfigException e) {
            jocUrl = sosPrivateConf.getValue("joc.url");
        }
        jocUrl = jocUrl + "/joc/api";
        LOGGER.debug("jocUrl: " + jocUrl);

        String userEncodedAccount;
        try {
            userEncodedAccount = sosPrivateConf.getDecodedValue("joc.webservice.jitl", "joc.account");
        } catch (ConfigException e) {
            userEncodedAccount = sosPrivateConf.getDecodedValue("joc.account");
        }

        ApiAccessToken apiAccessToken = new ApiAccessToken(jocUrl);
        boolean sessionIsValid;
        String xAccessToken = "";

        int cnt = 0;
        while (cnt < MAX_WAIT_TIME_FOR_ACCESS_TOKEN && !apiAccessToken.isValidAccessToken(xAccessToken)) {
            LOGGER.debug("check session");
            cnt++;
            try {
                sessionIsValid = apiAccessToken.isValidUserAccount(userEncodedAccount);
            } catch (Exception e) {
                sessionIsValid = false;
            }
            if (!sessionIsValid || xAccessToken.isEmpty()) {
                LOGGER.debug("... execute login");
                try {
                    xAccessToken = apiAccessToken.login(userEncodedAccount);
                } catch (Exception e) {
                    LOGGER.warn("... login failed with " + sosPrivateConf.getValue("joc.webservice.jitl", "joc.account") + " at " + jocUrl);
                    try {
                        java.lang.Thread.sleep(1_000);
                    } catch (InterruptedException e1) {

                    }
                }

            }
        }
        if (cnt == MAX_WAIT_TIME_FOR_ACCESS_TOKEN) {
            LOGGER.warn("Could not get the access token from JOC Server:" + jocUrl);
        }
        return xAccessToken;
    }

}
