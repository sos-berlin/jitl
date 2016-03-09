package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

public class EventMonitorProcessAfter extends JSEventsClientBaseClass {

    private final String conClassName = "EventMonitorProcessAfter";
    private static Logger logger = Logger.getLogger(EventMonitorProcessAfter.class);

    @Override
    public boolean spooler_process_after(final boolean spooler_process_return_code) throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_process_after";

        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            throw e;
        } finally {
        } // finally
        return spooler_process_return_code;
    } // spooler_process_after
}
