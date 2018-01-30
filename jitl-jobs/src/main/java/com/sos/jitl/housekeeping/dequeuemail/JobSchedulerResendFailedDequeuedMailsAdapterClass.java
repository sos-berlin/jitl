package com.sos.jitl.housekeeping.dequeuemail;

import sos.scheduler.job.JobSchedulerJobAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSchedulerResendFailedDequeuedMailsAdapterClass extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerResendFailedDequeuedMailsAdapterClass.class);

    @Override
    public boolean spooler_process() throws Exception {
        super.spooler_process();
        doProcessing();
        return signalSuccess();
    }

    private void doProcessing() throws Exception {
        JobSchedulerResendFailedDequeuedMails jobSchedulerResendFailedDequeuedMails = new JobSchedulerResendFailedDequeuedMails();
        JobSchedulerDequeueMailJobOptions jobSchedulerDequeueMailJobOptions = jobSchedulerResendFailedDequeuedMails.getOptions();
        if (jobSchedulerDequeueMailJobOptions.iniPath.isNotDirty()) {
            jobSchedulerDequeueMailJobOptions.iniPath.setValue(spooler.ini_path());
        }

        jobSchedulerDequeueMailJobOptions.setCurrentNodeName(this.getCurrentNodeName());
        jobSchedulerDequeueMailJobOptions.setAllOptions(getSchedulerParameterAsProperties());
        jobSchedulerDequeueMailJobOptions.checkMandatory();
        jobSchedulerResendFailedDequeuedMails.setJSJobUtilites(this);
        jobSchedulerResendFailedDequeuedMails.Execute();
    }

}