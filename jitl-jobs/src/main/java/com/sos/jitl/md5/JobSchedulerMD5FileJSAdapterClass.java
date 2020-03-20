package com.sos.jitl.md5;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerMD5FileJSAdapterClass extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
            return getSpoolerProcess().isOrderJob();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
    }

    private void doProcessing() throws Exception {
        JobSchedulerMD5File objR = new JobSchedulerMD5File();
        JobSchedulerMD5FileOptions objO = objR.getOptions();
        objO.setCurrentNodeName(this.getCurrentNodeName(getSpoolerProcess().getOrder(), true));
        objO.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));
        objO.checkMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    }

}