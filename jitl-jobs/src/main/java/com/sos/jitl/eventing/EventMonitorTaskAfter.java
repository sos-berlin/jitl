package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class EventMonitorTaskAfter extends JSEventsClientBaseClass {

    private static final Logger LOGGER = Logger.getLogger(EventMonitorTaskAfter.class);

    @Override
    public void spooler_task_after() throws Exception {
        try {
            super.spooler_init();
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobSchedulerException(e.getLocalizedMessage(), e);
        }
    }

}
