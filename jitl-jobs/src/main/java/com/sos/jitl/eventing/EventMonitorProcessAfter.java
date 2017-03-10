package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

public class EventMonitorProcessAfter extends JSEventsClientBaseClass {

    private static final Logger LOGGER = Logger.getLogger(EventMonitorProcessAfter.class);

    @Override
    public boolean spooler_process_after(final boolean spooler_process_return_code) throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
        return spooler_process_return_code;
    }

}
