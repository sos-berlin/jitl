package com.sos.jitl.eventing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventMonitorProcessBefore extends JSEventsClientBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventMonitorProcessBefore.class);

    @Override
    public boolean spooler_process_before() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
        return continue_with_spooler_process;
    }

}
