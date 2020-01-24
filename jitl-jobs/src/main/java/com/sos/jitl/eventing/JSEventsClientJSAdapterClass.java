package com.sos.jitl.eventing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSEventsClientJSAdapterClass extends JSEventsClientBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSEventsClientJSAdapterClass.class);

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
