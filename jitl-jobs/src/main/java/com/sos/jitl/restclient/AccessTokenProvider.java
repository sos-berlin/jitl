package com.sos.jitl.restclient;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

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
        LOGGER.debug("Setting:" + name + "=" + value);
        if (spooler != null) {
            spooler.variables().set_value(name, value);
        } else {
            LOGGER.debug("spooler is null " + name + "=" + value + " not set");
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

        LOGGER.debug("User:" + webserviceCredentials.getUser());
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

        LOGGER.debug("Check whether accessToken " + xAccessToken + " is valid");
        if (xAccessToken.isEmpty() || !apiAccessToken.isValidAccessToken(xAccessToken, webserviceCredentials)) {
            LOGGER.debug("---> not valid. Execute login");
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

        if (!apiAccessToken.isValidAccessToken(xAccessToken, webserviceCredentials)) {
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
        String jocUrl = "";

        String keyStorePath = "";
        String keyStorePassword = "";
        String keyPassword = "";

        String keyStoreType = "";

        String trustStorePath = "";
        String trustStorePassword = "";
        String trustStoreType = "";

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

                keyStorePath = r.resolve(jobSchedulerCredentialStoreParameters.getKeyStorePath());
                keyPassword = r.resolve(jobSchedulerCredentialStoreParameters.getKeyPassword());
                keyStorePassword = r.resolve(jobSchedulerCredentialStoreParameters.getKeyStorePassword());
                keyStoreType = r.resolve(jobSchedulerCredentialStoreParameters.getKeyStoreType());

                trustStorePath = r.resolve(jobSchedulerCredentialStoreParameters.getTrustStorePath());
                trustStorePassword = r.resolve(jobSchedulerCredentialStoreParameters.getTrustStorePassword());
                trustStoreType = r.resolve(jobSchedulerCredentialStoreParameters.getKeyStoreType());

            } catch (Exception e) {
                throw new SOSException(e);
            }

            LOGGER.debug(jobSchedulerCredentialStoreParameters.getCredentialStoreFile());
            LOGGER.debug(jobSchedulerCredentialStoreParameters.getCredentialStoreKeyFile());
            LOGGER.debug(jobSchedulerCredentialStoreParameters.getCredentialStoreEntryPath());

            LOGGER.debug("JOCUrl: " + jocUrl);
            LOGGER.debug("KeyStorePath: " + keyStorePath);
            LOGGER.debug("KeyStoreType: " + keyStoreType);
            LOGGER.debug("KeyStorePasswort: " + "********");
            LOGGER.debug("KeyPassword: " + "********");
            LOGGER.debug("TrustStorePath: " + trustStorePath);
            LOGGER.debug("TrustStoreType: " + trustStoreType);
            LOGGER.debug("TrustStorePasswort: " + "********");
            LOGGER.debug("User: " + jocApiUser);
            LOGGER.debug("Password: " + "********");

        } else {
            if (jobSchedulerCredentialStoreParameters != null) {
                jocUrl = jobSchedulerCredentialStoreParameters.getJocUrl();
                jocApiUser = jobSchedulerCredentialStoreParameters.getUser();
                jocApiPassword = jobSchedulerCredentialStoreParameters.getPassword();

                keyStorePath = jobSchedulerCredentialStoreParameters.getKeyStorePath();
                keyStorePassword = jobSchedulerCredentialStoreParameters.getKeyStorePassword();
                keyPassword = jobSchedulerCredentialStoreParameters.getKeyStorePassword();
                keyStoreType = jobSchedulerCredentialStoreParameters.getKeyStoreType();

                trustStorePath = jobSchedulerCredentialStoreParameters.getTrustStorePath();
                trustStorePassword = jobSchedulerCredentialStoreParameters.getTrustStorePassword();
                trustStoreType = jobSchedulerCredentialStoreParameters.getKeyStoreType();

            }
        }

        if (jocApiUser != null && jocApiPassword != null && !jocApiUser.isEmpty() && !jocApiPassword.isEmpty()) {
            userDecodedAccount = jocApiUser + ":" + jocApiPassword;
        }

        SOSPrivateConf sosPrivateConf;
        sosPrivateConf = new SOSPrivateConf(profileFileName);

        if (jocUrl == null || jocUrl.isEmpty()) {
            try {
                jocUrl = sosPrivateConf.getValue("joc.webservice.jitl", "joc.url");
            } catch (ConfigException.Missing e) {
                jocUrl = sosPrivateConf.getValue("joc.url");
            }
        }

        if (userDecodedAccount.isEmpty()) {
            userDecodedAccount = sosPrivateConf.getDecodedValue("joc.webservice.jitl", "joc.account");
            if (userDecodedAccount == null) {
                userDecodedAccount = sosPrivateConf.getDecodedValue("joc.account");
            }
        }

        if (userDecodedAccount != null) {
            String[] account = userDecodedAccount.split(":");
            if (account.length > 0) {

                if (jocApiUser.isEmpty()) {
                    jocApiUser = account[0];
                }
                if (jocApiPassword.isEmpty()) {
                    if (account.length > 1) {
                        jocApiPassword = userDecodedAccount.split(":")[1];
                    }
                }
            }
        }

        if (keyStorePath.isEmpty()) {
            try {
                keyStorePath = sosPrivateConf.getValue("joc.webservice.jitl", "joc.keystorepath");
            } catch (ConfigException.Missing e) {
                keyStorePath = sosPrivateConf.getValueDefaultEmpty("joc.keystorepath");
            }
        }

        if (keyStorePassword.isEmpty()) {
            try {
                keyStorePassword = sosPrivateConf.getValue("joc.webservice.jitl", "joc.keystorepassword");
            } catch (ConfigException.Missing e) {
                keyStorePassword = sosPrivateConf.getValueDefaultEmpty("joc.keystorepassword");
            }
        }

        if (keyPassword.isEmpty()) {
            try {
                keyPassword = sosPrivateConf.getValue("joc.webservice.jitl", "joc.keypassword");
            } catch (ConfigException.Missing e) {
                keyPassword = sosPrivateConf.getValueDefaultEmpty("joc.keyPassword");
            }
        }

        if (keyStoreType.isEmpty()) {
            try {
                keyStoreType = sosPrivateConf.getValue("joc.webservice.jitl", "joc.keystoretype");
            } catch (ConfigException.Missing e) {
                keyStoreType = sosPrivateConf.getValueDefaultEmpty("joc.keystoretype");
            }
        }

        if (trustStorePath.isEmpty()) {
            try {
                trustStorePath = sosPrivateConf.getValue("joc.webservice.jitl", "joc.truststorepath");
            } catch (ConfigException.Missing e) {
                trustStorePath = sosPrivateConf.getValueDefaultEmpty("joc.truststorepath");
            }
        }

        if (trustStorePassword.isEmpty()) {
            try {
                trustStorePassword = sosPrivateConf.getValue("joc.webservice.jitl", "joc.truststorepassword");
            } catch (ConfigException.Missing e) {
                trustStorePassword = sosPrivateConf.getValueDefaultEmpty("joc.truststorepassword");
            }
        }

        if (trustStoreType.isEmpty()) {
            try {
                trustStoreType = sosPrivateConf.getValue("joc.webservice.jitl", "joc.truststoretype");
            } catch (ConfigException.Missing e) {
                trustStoreType = sosPrivateConf.getValueDefaultEmpty("joc.truststoretype");
            }
        }

        webserviceCredentials.setJocUrl(jocUrl + "/joc/api");
        webserviceCredentials.setPassword(jocApiPassword);
        webserviceCredentials.setUser(jocApiUser);
        webserviceCredentials.setUserDecodedAccount(userDecodedAccount);

        webserviceCredentials.setKeyStorePassword(keyStorePassword);
        webserviceCredentials.setKeyPassword(keyPassword);
        webserviceCredentials.setKeyStorePath(keyStorePath);
        webserviceCredentials.setKeyStoreType(keyStoreType);
        webserviceCredentials.setTrustStorePassword(trustStorePassword);
        webserviceCredentials.setTrustStorePath(trustStorePath);
        webserviceCredentials.setTrustStoreType(trustStoreType);

        return webserviceCredentials;

    }

    private String executeLogin() throws SOSException, URISyntaxException, UnsupportedEncodingException {

        String userDecodedAccount = webserviceCredentials.getUserDecodedAccount();
        String jocUrl = webserviceCredentials.getJocUrl();

        // jocUrl = jocUrl + "/joc/api";
        LOGGER.debug("jocUrl: " + jocUrl);

        ApiAccessToken apiAccessToken = new ApiAccessToken(jocUrl);
        boolean sessionIsValid = false;
        String xAccessToken = "";

        int cnt = 0;
        while (cnt < MAX_WAIT_TIME_FOR_ACCESS_TOKEN && !sessionIsValid) {
            LOGGER.debug("check session");

            try {
                sessionIsValid = apiAccessToken.isValidAccessToken(xAccessToken, webserviceCredentials);
            } catch (Exception e) {
                sessionIsValid = false;
            }
            if (!sessionIsValid || xAccessToken.isEmpty()) {
                LOGGER.debug("... execute login");
                try {
                    xAccessToken = apiAccessToken.login(webserviceCredentials);
                } catch (Exception e) {
                    LOGGER.warn("... login failed with " + webserviceCredentials.getUserEncodedAccount() + " at " + jocUrl);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ei) {
                    }
                }
                cnt = cnt + 1;
            }
        }
        if (cnt == MAX_WAIT_TIME_FOR_ACCESS_TOKEN) {
            LOGGER.warn("Could not get the access token from JOC Server:" + jocUrl);
        }
        return xAccessToken;
    }

}
