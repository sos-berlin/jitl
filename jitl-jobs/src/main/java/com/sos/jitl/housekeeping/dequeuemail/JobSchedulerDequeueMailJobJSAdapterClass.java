package com.sos.jitl.housekeeping.dequeuemail;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerDequeueMailJobJSAdapterClass extends JobSchedulerJobAdapter {

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
        JobSchedulerDequeueMailJob jobSchedulerDequeueMailJob = new JobSchedulerDequeueMailJob();
        JobSchedulerDequeueMailJobOptions jobSchedulerDequeueMailJobOptions = jobSchedulerDequeueMailJob.getOptions();
        if (jobSchedulerDequeueMailJobOptions.smtp_host.isNotDirty()) {
            if (!spooler_log.mail().smtp().equalsIgnoreCase("-queue")) {
                jobSchedulerDequeueMailJobOptions.smtp_host.Value(spooler_log.mail().smtp());
            } else {
                throw new Exception("no SMTP host was configured, global settings contain smtp=-queue");
            }
        }
        if (jobSchedulerDequeueMailJobOptions.queue_directory.isNotDirty()) {
            jobSchedulerDequeueMailJobOptions.queue_directory.Value(spooler_log.mail().queue_dir());
        }
        if (jobSchedulerDequeueMailJobOptions.ini_path.isNotDirty()) {
            jobSchedulerDequeueMailJobOptions.ini_path.Value(spooler.ini_path());
        }
        jobSchedulerDequeueMailJobOptions.CurrentNodeName(this.getCurrentNodeName());
        jobSchedulerDequeueMailJobOptions.setAllOptions(getSchedulerParameterAsProperties());
        jobSchedulerDequeueMailJobOptions.CheckMandatory();
        jobSchedulerDequeueMailJob.setJSJobUtilites(this);
        jobSchedulerDequeueMailJob.Execute();
    }

}