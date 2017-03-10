/**
 * 
 */
package com.sos.jitl.eventing.checkevents;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;


public class JobSchedulerCheckEventsMonitor extends JobSchedulerCheckEventsJSAdapterClass {

    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void spooler_task_after() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_task_after";
        try {
            super.spooler_process();
        } catch (Exception e) {
            throw new JobSchedulerException(e.getLocalizedMessage(), e);
        }
    } // spooler_task_after

    @Override
    public boolean spooler_task_before() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_task_before";
        try {
            super.spooler_process();
        } catch (Exception e) {
            throw new JobSchedulerException(e.getLocalizedMessage(), e);
        }
        return continue_with_task; // Task can start
    } // spooler_process

    @Override
    public boolean spooler_process_after(final boolean spooler_process_return_code) throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_process_after";
        try {
            super.spooler_process();
        } catch (Exception e) {
            throw new JobSchedulerException(e.getLocalizedMessage(), e);
        }
        return spooler_process_return_code;
    } // spooler_process

    @Override
    public boolean spooler_process_before() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_process_before";
        try {
            super.spooler_process();
        } catch (Exception e) {
            throw new JobSchedulerException(e.getLocalizedMessage(), e);
        }
        return continue_with_spooler_process;
    } // spooler_process_before
}
