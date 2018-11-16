package com.sos.jitl.restclient;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSException;
import sos.scheduler.job.JobSchedulerJobAdapter;

public class CreateApiAccessToken extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateApiAccessToken.class);

    @Override
    public boolean spooler_process_before() throws SOSException, URISyntaxException, InterruptedException, UnsupportedEncodingException {
        LOGGER.debug("Starting spooler_process_before");
        AccessTokenProvider accessTokenProvider = new AccessTokenProvider();
        accessTokenProvider.getAccessToken(spooler);
        return continue_with_spooler_process;
    }

}
