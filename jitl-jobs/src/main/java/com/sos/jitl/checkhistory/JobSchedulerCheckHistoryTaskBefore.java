package com.sos.jitl.checkhistory;

public class JobSchedulerCheckHistoryTaskBefore extends JobSchedulerCheckHistoryJSAdapterClass {

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
