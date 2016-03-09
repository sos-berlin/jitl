package com.sos.jitl.housekeeping.rotatelog;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler
                                                 // Java-API-Jobs

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerRotateLogJSAdapterClass extends JobSchedulerJobAdapter {

    private final String conClassName = "JobSchedulerRotateLogJSAdapterClass";
    private static Logger logger = Logger.getLogger(JobSchedulerRotateLogJSAdapterClass.class);

    public void init() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::init";
        doInitialize();
    }

    private void doInitialize() {
    } // doInitialize

    @Override
    public boolean spooler_init() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_init";
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_process";

        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        } finally {
        } // finally
        return signalSuccess();

    } // spooler_process

    private void doProcessing() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::doProcessing";

        JobSchedulerRotateLog rotateLogExecuter = new JobSchedulerRotateLog();
        JobSchedulerRotateLogOptions rotateLogOptions = rotateLogExecuter.getOptions();

        rotateLogOptions.CurrentNodeName(this.getCurrentNodeName());
        rotateLogOptions.jobSchedulerID.Value(spooler.id());
        rotateLogOptions.jobSchedulerLogFilesPath.Value(spooler.log_dir());
        rotateLogOptions.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));

        rotateLogOptions.CheckMandatory();
        rotateLogExecuter.setJSJobUtilites(this);

        try {
            rotateLogExecuter.executeDebugLog();
            spooler.log().start_new_file(); // this will start with a fresh log
                                            // file
            rotateLogExecuter.executeMainLog();
        } catch (Exception e) {
            throw new JobSchedulerException("an error occurred rotating log file: " + e.getMessage(), e);
        }
    } // doProcessing
}
