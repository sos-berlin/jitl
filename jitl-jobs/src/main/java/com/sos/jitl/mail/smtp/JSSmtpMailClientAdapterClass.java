package com.sos.jitl.mail.smtp;

import org.apache.log4j.Logger;

public class JSSmtpMailClientAdapterClass extends JSSmtpMailClientBaseClass {

    private static final Logger LOGGER = Logger.getLogger(JSSmtpMailClientAdapterClass.class);

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
        return signalSuccess();
    }

}