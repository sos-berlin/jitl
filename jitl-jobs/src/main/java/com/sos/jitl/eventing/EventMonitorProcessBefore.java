package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

public class EventMonitorProcessBefore extends JSEventsClientBaseClass {

    private final String conClassName = "EventMonitorProcessBefore";
    private static Logger logger = Logger.getLogger(EventMonitorProcessBefore.class);

    @Override
    public boolean spooler_process_before() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_process";

        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            throw e;
        } finally {
        } // finally
        return continue_with_spooler_process;

    } // spooler_process_before
}
