package com.sos.jitl.checkrunhistory;


public class JobSchedulerCheckRunHistoryProcessBefore extends JobSchedulerCheckRunHistoryJSAdapterClass{
    
 

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
