package com.sos.jitl.housekeeping.dequeuemail;

import sos.scheduler.job.JobSchedulerJobAdapter;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerDequeueMailJobJSAdapterClass extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerDequeueMailJobJSAdapterClass.class);

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
        JobSchedulerDequeueMailJob jobSchedulerDequeueMailJob = new JobSchedulerDequeueMailJob();
        JobSchedulerDequeueMailJobOptions jobSchedulerDequeueMailJobOptions = jobSchedulerDequeueMailJob.getOptions();

        jobSchedulerDequeueMailJobOptions.setCurrentNodeName(this.getCurrentNodeName(getSpoolerProcess().getOrder(),true));
        jobSchedulerDequeueMailJobOptions.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));

        if (jobSchedulerDequeueMailJobOptions.smtpHost.isNotDirty()) {
            if (!"-queue".equalsIgnoreCase(spooler_log.mail().smtp())) {
                jobSchedulerDequeueMailJobOptions.smtpHost.setValue(spooler_log.mail().smtp());
            } else {
                throw new Exception("no SMTP host was configured, global settings contain smtp=-queue");
            }
        }

        String schedulerFilePathName = "";
        boolean isJobChain = spooler_task.order() != null;
        if (isJobChain) {
            schedulerFilePathName = spooler_task.order().params().value("scheduler_file_path");
        }

        if (!schedulerFilePathName.isEmpty()) {
            File schedulerFilePath = new File(schedulerFilePathName);
            jobSchedulerDequeueMailJobOptions.fileWatching.value(isJobChain);
            jobSchedulerDequeueMailJobOptions.queueDirectory.setValue(schedulerFilePath.getParent());
            jobSchedulerDequeueMailJobOptions.emailFileName.setValue(schedulerFilePathName);
            LOGGER.debug("Running in a job chain with a file order source.");
        } else {
            if (jobSchedulerDequeueMailJobOptions.queueDirectory.isNotDirty()) {
                jobSchedulerDequeueMailJobOptions.queueDirectory.setValue(spooler_log.mail().queue_dir());
            }
        }

        if (jobSchedulerDequeueMailJobOptions.iniPath.isNotDirty()) {
            jobSchedulerDequeueMailJobOptions.iniPath.setValue(spooler.ini_path());
        }

        jobSchedulerDequeueMailJobOptions.checkMandatory();
        jobSchedulerDequeueMailJob.setOptions(jobSchedulerDequeueMailJobOptions);
        jobSchedulerDequeueMailJob.setJSJobUtilites(this);
        jobSchedulerDequeueMailJob.setHibernateConfigurationFile(this.getHibernateConfigurationReporting().toFile().getAbsolutePath());
        jobSchedulerDequeueMailJob.setConfigDir(spooler.directory() + "/config");
        jobSchedulerDequeueMailJob.setNotification ("true".equalsIgnoreCase(spooler.variables().value("sos.use_notification")));
        LOGGER.debug("sos.use_notification:" + spooler.variables().value("sos.use_notification"));
        jobSchedulerDequeueMailJob.setSchedulerId(spooler.id());
        jobSchedulerDequeueMailJob.execute();
    }

}