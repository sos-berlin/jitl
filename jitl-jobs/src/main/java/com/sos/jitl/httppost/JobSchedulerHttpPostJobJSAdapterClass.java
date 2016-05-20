package com.sos.jitl.httppost;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerHttpPostJobJSAdapterClass extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
        return signalSuccess();
    }

    private void doProcessing() throws Exception {
        JobSchedulerHttpPostJob objR = new JobSchedulerHttpPostJob();
        JobSchedulerHttpPostJobOptions objO = objR.getOptions();
        objO.setCurrentNodeName(this.getCurrentNodeName());
        objO.setAllOptions(getSchedulerParameterAsProperties());
        objO.checkMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    }

}