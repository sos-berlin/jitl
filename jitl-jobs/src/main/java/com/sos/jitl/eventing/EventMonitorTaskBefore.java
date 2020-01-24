package com.sos.jitl.eventing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventMonitorTaskBefore extends JSEventsClientBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventMonitorTaskBefore.class);

    @Override
    public boolean spooler_task_before() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
        return continue_with_task;
    }

}
