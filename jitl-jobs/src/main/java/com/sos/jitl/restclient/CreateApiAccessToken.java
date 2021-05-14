package com.sos.jitl.restclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.IMonitor_impl;

public class CreateApiAccessToken extends JobSchedulerJobAdapter implements IMonitor_impl {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateApiAccessToken.class);

    @Override
    public boolean spooler_process_before() throws Exception {
       
        
        LOGGER.debug("Starting spooler_process_before");
        try {
        AccessTokenProvider accessTokenProvider = new AccessTokenProvider(null);
        WebserviceCredentials w = accessTokenProvider.getAccessToken(spooler);
        spooler.variables().set_value("joc_user", w.getUser());

        }catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(),e);
        }
        return true;
    }

}
