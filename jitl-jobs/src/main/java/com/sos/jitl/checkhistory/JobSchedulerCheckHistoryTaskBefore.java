package com.sos.jitl.checkhistory;

public class JobSchedulerCheckHistoryTaskBefore extends JobSchedulerCheckHistoryJSAdapterClass {

    @Override
    public boolean spooler_task_before() throws Exception {
        try {
            doProcessing();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
