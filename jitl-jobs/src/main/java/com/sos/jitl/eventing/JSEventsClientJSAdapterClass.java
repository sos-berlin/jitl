package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

public class JSEventsClientJSAdapterClass extends JSEventsClientBaseClass {

    private static final Logger LOGGER = Logger.getLogger(JSEventsClientJSAdapterClass.class);

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