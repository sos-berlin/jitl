package com.sos.jitl.restclient;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSPrivateConf;

import java.net.URISyntaxException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSException;
import com.typesafe.config.ConfigException;

public class CreateApiAccessToken extends JobSchedulerJobAdapter {

    private static final int MAX_WAIT_TIME_FOR_ACCESS_TOKEN = 30;
    private static final String SOS_REST_CREATE_API_ACCESS_TOKEN = "/sos/rest/createApiAccessToken";

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateApiAccessToken.class);

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            return false;
        }
        return this.signalSuccess();
    }

    @Override
    public boolean spooler_process_before() throws SOSException, URISyntaxException, InterruptedException {
        LOGGER.debug("Starting spooler_process_before");
        String xAccessToken = spooler.variables().value("X-Access-Token");
        String jocUrl = spooler.variables().value("joc_url");

        if (jocUrl == null) {
            jocUrl = "";
        }

        ApiAccessToken apiAccessToken = new ApiAccessToken(jocUrl);

        int cnt = 0;
        Job_chain j = spooler.job_chain(SOS_REST_CREATE_API_ACCESS_TOKEN);

        LOGGER.debug("Check whether accessToken is valid");
        while (cnt < MAX_WAIT_TIME_FOR_ACCESS_TOKEN && !apiAccessToken.isValidAccessToken(xAccessToken)) {
            Order o = spooler.create_order();
            LOGGER.debug("AccessToken " + xAccessToken + " is not valid. Renew it");
            j.add_or_replace_order(o);
            java.lang.Thread.sleep(1000);
            jocUrl = spooler.variables().value("joc_url");
            apiAccessToken.setJocUrl(jocUrl);
            xAccessToken = spooler.variables().value("X-Access-Token");
            cnt = cnt + 1;
        }

        if (cnt == MAX_WAIT_TIME_FOR_ACCESS_TOKEN) {
            LOGGER.warn("Could not renew the access token for JOC Server:" + jocUrl);
            return !continue_with_spooler_process;
        }

        return continue_with_spooler_process;

    }

    public void doProcessing() throws Exception {
        Variable_set params = spooler.create_variable_set();
        params.merge(spooler_task.params());

        if (this.isJobchain()) {
            params.merge(spooler_task.order().params());
        }

        SOSPrivateConf sosPrivateConf = new SOSPrivateConf("config\\private\\private.conf");

        String jocUrl;
        try {
            jocUrl = sosPrivateConf.getValue("joc.webservice.jitl", "joc.url");
        } catch (ConfigException e) {
            jocUrl = sosPrivateConf.getValue("joc.url");
        }
        jocUrl = jocUrl + "/joc/api";

        String userAccount;
        try {
            userAccount = sosPrivateConf.getEncodedValue("joc.webservice.jitl", "joc.account");
        } catch (ConfigException e) {
            userAccount = sosPrivateConf.getEncodedValue("joc.account");
        }

        ApiAccessToken apiAccessToken = new ApiAccessToken(jocUrl);
        boolean sessionIsValid = apiAccessToken.isValidUserAccount(userAccount);

        if (!sessionIsValid || spooler.variables().value("X-Access-Token").isEmpty()) {
            String accessToken = apiAccessToken.login(userAccount);
            if (accessToken != null && !accessToken.isEmpty()) {
                spooler.variables().set_value("JOC_Url", jocUrl);
                spooler.variables().set_value("X-Access-Token", accessToken);
            }
        }
    }
}
