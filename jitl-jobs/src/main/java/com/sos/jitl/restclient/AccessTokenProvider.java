package com.sos.jitl.restclient;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSException;
import com.sos.keepass.SOSKeePassResolver;
import com.typesafe.config.ConfigException;

import sos.spooler.Spooler;
import sos.util.SOSPrivateConf;

public class AccessTokenProvider {

    private static final String DEFAULT_PRIVATE_CONF_FILENAME = "config/private/private.conf";
    private static final String JOC_URL = "joc_url";
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenProvider.class);
    private static final String X_ACCESS_TOKEN = "X-Access-Token";
    private static final int MAX_WAIT_TIME_FOR_ACCESS_TOKEN = 30;
    private WebserviceCredentials webserviceCredentials;
    private String profileFileName;
    private JobSchedulerCredentialStoreJOCParameters jobSchedulerCredentialStoreParameters;

    public AccessTokenProvider(JobSchedulerCredentialStoreJOCParameters options) {
        super();
        profileFileName = DEFAULT_PRIVATE_CONF_FILENAME;
        this.jobSchedulerCredentialStoreParameters = options;
    }

    public AccessTokenProvider(JobSchedulerCredentialStoreJOCParameters options, String profileName) {
        super();
        this.profileFileName = profileName;
        this.jobSchedulerCredentialStoreParameters = options;
    }

    private void setSpoolerVariable(Spooler spooler, String name, String value) {
        LOGGER.debug("Setting" + name + "=" + value);
        if (spooler != null) {
            spooler.variables().set_value(name, value);
        }else {
            LOGGER.debug("spooler is null "  + name + "=" + value + " not set");
        }

    }

    private String getSpoolerVariable(Spooler spooler, String name) {
        if (spooler != null) {
            return spooler.variables().value(name);
        } else {
            return "";
        }

    }

    public WebserviceCredentials getAccessToken(Spooler spooler) throws SOSException, URISyntaxException, InterruptedException,
            UnsupportedEncodingException {

        String schedulerId = "";
        webserviceCredentials = this.getWebServiceVCredentials();

        String xAccessToken;
        if (webserviceCredentials.getUser() != null && !webserviceCredentials.getUser().isEmpty()) {
            xAccessToken = getSpoolerVariable(spooler, webserviceCredentials.getUser() + "_" + X_ACCESS_TOKEN);
        } else {
            xAccessToken = "";
        }

        if (webserviceCredentials.getJocUrl() == null) {
            webserviceCredentials.setJocUrl("");
        }
        if (spooler != null && webserviceCredentials.getJocUrl().isEmpty()) {
            webserviceCredentials.setJocUrl(getSpoolerVariable(spooler, JOC_URL));
        }

        ApiAccessToken apiAccessToken = new ApiAccessToken(webserviceCredentials.getJocUrl());

        LOGGER.debug("Check whether accessToken is valid");
        if (xAccessToken.isEmpty() || !apiAccessToken.isValidAccessToken(xAccessToken)) {
            xAccessToken = executeLogin();
            apiAccessToken.setJocUrl(webserviceCredentials.getJocUrl());

            if (xAccessToken != null && !xAccessToken.isEmpty()) {
                LOGGER.debug("... set accessToken:" + xAccessToken);
                setSpoolerVariable(spooler, webserviceCredentials.getUser() + "_" + X_ACCESS_TOKEN, xAccessToken);
                setSpoolerVariable(spooler, JOC_URL, webserviceCredentials.getJocUrl());
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

        webserviceCredentials.setSchedulerId(schedulerId);
        webserviceCredentials.setAccessToken(xAccessToken);
        return webserviceCredentials;

    }

    public String getJocUrl() {
        return webserviceCredentials.getJocUrl();
    }

    private WebserviceCredentials getWebServiceVCredentials() throws SOSException, UnsupportedEncodingException {
        String userDecodedAccount = "";
        String jocApiUser = "";
        String jocApiPassword = "";
        String jocUrl="";

        WebserviceCredentials webserviceCredentials = new WebserviceCredentials();

        if (jobSchedulerCredentialStoreParameters != null && jobSchedulerCredentialStoreParameters.getCredentialStoreFile() != null
                && !jobSchedulerCredentialStoreParameters.getCredentialStoreFile().isEmpty()) {
            SOSKeePassResolver r = new SOSKeePassResolver(jobSchedulerCredentialStoreParameters.getCredentialStoreFile(),
                    jobSchedulerCredentialStoreParameters.getCredentialStoreKeyFile(), jobSchedulerCredentialStoreParameters
                            .getCredentialStorePassword());
            r.setEntryPath(jobSchedulerCredentialStoreParameters.getCredentialStoreEntryPath());

            try {
                jocUrl = r.resolve(jobSchedulerCredentialStoreParameters.getJocUrl());

                jocApiUser = r.resolve(jobSchedulerCredentialStoreParameters.getUser());
                jocApiPassword = r.resolve(jobSchedulerCredentialStoreParameters.getPassword());
            } catch (Exception e) {
                throw new SOSException(e);
            }

            LOGGER.debug(jobSchedulerCredentialStoreParameters.getCredentialStoreFile());
            LOGGER.debug(jobSchedulerCredentialStoreParameters.getCredentialStoreKeyFile());
            LOGGER.debug(jobSchedulerCredentialStoreParameters.getCredentialStoreEntryPath());

            LOGGER.debug("JOCUrl: " + jocUrl);
            LOGGER.debug("User: " + jocApiUser);
            LOGGER.debug("Password: " + "********");

        } else {
            if (jobSchedulerCredentialStoreParameters != null) {
                jocUrl = jobSchedulerCredentialStoreParameters.getJocUrl();
                jocApiUser = jobSchedulerCredentialStoreParameters.getUser();
                jocApiPassword = jobSchedulerCredentialStoreParameters.getPassword();
            }
        }

        if (!jocApiUser.isEmpty() && !jocApiPassword.isEmpty()) {
            userDecodedAccount = jocApiUser + ":" + jocApiPassword;
        }

        if (jocUrl.isEmpty() || (jocApiUser.isEmpty() && jocApiPassword.isEmpty())) {
            SOSPrivateConf sosPrivateConf;
            sosPrivateConf = new SOSPrivateConf(profileFileName);

            if (jocUrl.isEmpty()) {
                try {
                    jocUrl = sosPrivateConf.getValue("joc.webservice.jitl", "joc.url");
                } catch (ConfigException e) {
                    jocUrl = sosPrivateConf.getValue("joc.url");
                }
            }

            if (userDecodedAccount.isEmpty()) {
                try {
                    userDecodedAccount = sosPrivateConf.getDecodedValue("joc.webservice.jitl", "joc.account");
                } catch (ConfigException e) {
                    userDecodedAccount = sosPrivateConf.getDecodedValue("joc.account");
                }
            }
            String[] account = userDecodedAccount.split(":");
            jocApiUser = account[0];
            if (account.length > 1) {
                jocApiPassword = userDecodedAccount.split(":")[1];
            }
        }

        webserviceCredentials.setJocUrl(jocUrl + "/joc/api");
        webserviceCredentials.setPassword(jocApiPassword);
        webserviceCredentials.setUser(jocApiUser);
        webserviceCredentials.setUserDecodedAccount(userDecodedAccount);
        return webserviceCredentials;

    }

    private String executeLogin() throws SOSException, URISyntaxException, UnsupportedEncodingException {

        String userDecodedAccount = webserviceCredentials.getUserDecodedAccount();
        String jocUrl = webserviceCredentials.getJocUrl();

        // jocUrl = jocUrl + "/joc/api";
        LOGGER.debug("jocUrl: " + jocUrl);

        ApiAccessToken apiAccessToken = new ApiAccessToken(jocUrl);
        boolean sessionIsValid;
        String xAccessToken = "";

        int cnt = 0;
        while (cnt < MAX_WAIT_TIME_FOR_ACCESS_TOKEN && !apiAccessToken.isValidAccessToken(xAccessToken)) {
            LOGGER.debug("check session");

            try {
                sessionIsValid = apiAccessToken.isValidUserAccount(userDecodedAccount);
            } catch (Exception e) {
                sessionIsValid = false;
            }
            if (!sessionIsValid || xAccessToken.isEmpty()) {
                LOGGER.debug("... execute login");
                try {
                    xAccessToken = apiAccessToken.login(userDecodedAccount);
                } catch (Exception e) {
                    LOGGER.warn("... login failed with " + webserviceCredentials.getUserEncodedAccount() + " at " + jocUrl);
                }

            }
        }
        if (cnt == MAX_WAIT_TIME_FOR_ACCESS_TOKEN) {
            LOGGER.warn("Could not get the access token from JOC Server:" + jocUrl);
        }
        return xAccessToken;
    }

}
