package com.sos.jitl.checkhistory;

public class JobSchedulerCheckHistoryProcessBefore extends JobSchedulerCheckHistoryJSAdapterClass {

    @Override
    public boolean spooler_process_before() throws Exception {
        try {
            super.spooler_process_before();
            doProcessing();
            return continueWithProcess;
        } catch (Exception e) {
            return false;
        }
    }

}
