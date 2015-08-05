

package com.sos.jitl.httppost;

import com.sos.jitl.httppost.JobSchedulerHttpPostJob;
import com.sos.jitl.httppost.JobSchedulerHttpPostJobOptions;
import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler Java-API-Jobs
import org.apache.log4j.Logger;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.localization.*;

public class JobSchedulerHttpPostJobJSAdapterClass extends JobSchedulerJobAdapter  {
	private final String					conClassName						= "JobSchedulerHttpPostJobJSAdapterClass";
	private static Logger		logger			= Logger.getLogger(JobSchedulerHttpPostJobJSAdapterClass.class);

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

		JobSchedulerHttpPostJob objR = new JobSchedulerHttpPostJob();
		JobSchedulerHttpPostJobOptions objO = objR.Options();

        objO.CurrentNodeName(this.getCurrentNodeName());
		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		objO.CheckMandatory();
        objR.setJSJobUtilites(this);
		objR.Execute();
	} // doProcessing

}

