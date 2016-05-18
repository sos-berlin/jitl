package com.sos.jitl.housekeeping.rotatelog;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerRotateLogJSAdapterClass extends JobSchedulerJobAdapter {

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
        JobSchedulerRotateLog rotateLogExecuter = new JobSchedulerRotateLog();
        JobSchedulerRotateLogOptions rotateLogOptions = rotateLogExecuter.getOptions();
        rotateLogOptions.CurrentNodeName(this.getCurrentNodeName());
        rotateLogOptions.jobSchedulerID.Value(spooler.id());
        rotateLogOptions.jobSchedulerLogFilesPath.Value(spooler.log_dir());
        rotateLogOptions.setAllOptions(getSchedulerParameterAsProperties());
        rotateLogOptions.checkMandatory();
        rotateLogExecuter.setJSJobUtilites(this);
        try {
            rotateLogExecuter.executeDebugLog();
            spooler.log().start_new_file();
            rotateLogExecuter.executeMainLog();
        } catch (Exception e) {
            throw new JobSchedulerException("an error occurred rotating log file: " + e.getMessage(), e);
        }
    }

}