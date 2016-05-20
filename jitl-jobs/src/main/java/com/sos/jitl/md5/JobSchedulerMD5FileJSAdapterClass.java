package com.sos.jitl.md5;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerMD5FileJSAdapterClass extends JobSchedulerJobAdapter {

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
        JobSchedulerMD5File objR = new JobSchedulerMD5File();
        JobSchedulerMD5FileOptions objO = objR.getOptions();
        objO.setCurrentNodeName(this.getCurrentNodeName());
        objO.setAllOptions(getSchedulerParameterAsProperties());
        objO.checkMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    }

}