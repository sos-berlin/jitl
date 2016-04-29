package com.sos.jitl.eventing.checkevents;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerCheckEventsMonitor extends JobSchedulerCheckEventsJSAdapterClass {

    @Override
    public void spooler_task_after() throws Exception {
        try {
            super.spooler_process();
        } catch (Exception e) {
            throw new JobSchedulerException(e.getMessage(), e);
        }
    }

    @Override
    public boolean spooler_task_before() throws Exception {
        try {
            super.spooler_process();
        } catch (Exception e) {
            throw new JobSchedulerException(e.getMessage(), e);
        }
        return continue_with_task;
    }

    @Override
    public boolean spooler_process_after(final boolean spooler_process_return_code) throws Exception {
        try {
            super.spooler_process();
        } catch (Exception e) {
            throw new JobSchedulerException(e.getMessage(), e);
        }
        return spooler_process_return_code;
    }

    @Override
    public boolean spooler_process_before() throws Exception {
        try {
            super.spooler_process();
        } catch (Exception e) {
            throw new JobSchedulerException(e.getMessage(), e);
        }
        return continue_with_spooler_process;
    }

}