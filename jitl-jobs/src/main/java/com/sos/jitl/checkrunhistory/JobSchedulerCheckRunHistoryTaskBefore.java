package com.sos.jitl.checkrunhistory;

public class JobSchedulerCheckRunHistoryTaskBefore extends JobSchedulerCheckRunHistoryJSAdapterClass {

    @Override
    public boolean spooler_task_before() throws Exception {
        try {
            doProcessing();
            return continueWithProcessBefore;
        } catch (Exception e) {
            return false;
        }
    }

}
