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
        AccessTokenProvider accessTokenProvider = new AccessTokenProvider(null);
        accessTokenProvider.getAccessToken(spooler);
        return continue_with_spooler_process;
    }

}
