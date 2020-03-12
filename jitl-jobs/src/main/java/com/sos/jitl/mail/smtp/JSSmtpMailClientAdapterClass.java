package com.sos.jitl.mail.smtp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSSmtpMailClientAdapterClass extends JSSmtpMailClientBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSSmtpMailClientAdapterClass.class);

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
            return getSpoolerProcess().getSuccess();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

}