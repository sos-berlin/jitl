

package com.sos.jitl.housekeeping.dequeuemail;

import com.sos.jitl.housekeeping.dequeuemail.JobSchedulerDequeueMailJob;
import com.sos.jitl.housekeeping.dequeuemail.JobSchedulerDequeueMailJobOptions;

import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler Java-API-Jobs

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.localization.*;

public class JobSchedulerDequeueMailJobJSAdapterClass extends JobSchedulerJobAdapter  {
	private final String					conClassName						= "JobSchedulerDequeueMailJobJSAdapterClass";
	private static Logger		logger			= Logger.getLogger(JobSchedulerDequeueMailJobJSAdapterClass.class);

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
		}
		catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
   		}
		finally {
		} // finally
        return signalSuccess();

	} // spooler_process

	@Override
	public void spooler_exit() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_exit";
		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcessing";

		JobSchedulerDequeueMailJob jobSchedulerDequeueMailJob = new JobSchedulerDequeueMailJob();
		JobSchedulerDequeueMailJobOptions jobSchedulerDequeueMailJobOptions = jobSchedulerDequeueMailJob.Options();

	      
		
        if (jobSchedulerDequeueMailJobOptions.smtp_host.isNotDirty()){
            if (!spooler_log.mail().smtp().equalsIgnoreCase("-queue")) {
                jobSchedulerDequeueMailJobOptions.smtp_host.Value(spooler_log.mail().smtp());
            } else {
                throw new Exception("no SMTP host was configured, global settings contain smtp=-queue");
            }
        }

        if (jobSchedulerDequeueMailJobOptions.queue_directory.isNotDirty()){
            jobSchedulerDequeueMailJobOptions.queue_directory.Value(spooler_log.mail().queue_dir());
        }
        
        if (jobSchedulerDequeueMailJobOptions.ini_path.isNotDirty()){
            jobSchedulerDequeueMailJobOptions.ini_path.Value(spooler.ini_path());
        }		
		
        jobSchedulerDequeueMailJobOptions.CurrentNodeName(this.getCurrentNodeName());
		jobSchedulerDequeueMailJobOptions.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		jobSchedulerDequeueMailJobOptions.CheckMandatory();
        jobSchedulerDequeueMailJob.setJSJobUtilites(this);
		jobSchedulerDequeueMailJob.Execute();
	} // doProcessing

}

