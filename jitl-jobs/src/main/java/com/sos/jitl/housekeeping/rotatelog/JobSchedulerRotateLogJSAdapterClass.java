

package com.sos.jitl.housekeeping.rotatelog;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler Java-API-Jobs

import com.sos.JSHelper.Exceptions.JobSchedulerException;
/**
 * \class 		JobSchedulerRotateLogJSAdapterClass - JobScheduler Adapter for "Rotate compress and delete log files"
 *
 * \brief AdapterClass of JobSchedulerRotateLog for the SOSJobScheduler
 *
 * This Class JobSchedulerRotateLogJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JobSchedulerRotateLog.
 *
 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-1724231827372138737html for more details.
 *
 * \verbatim ;
 * mechanicaly created by com/sos/resources/xsl/JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20140906131052
 * \endverbatim
 */
public class JobSchedulerRotateLogJSAdapterClass extends JobSchedulerJobAdapter  {
	private final String					conClassName						= "JobSchedulerRotateLogJSAdapterClass";
	private static Logger		logger			= Logger.getLogger(JobSchedulerRotateLogJSAdapterClass.class);

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

	private void doProcessing() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcessing";

		JobSchedulerRotateLog objR = new JobSchedulerRotateLog();
		JobSchedulerRotateLogOptions objO = objR.Options();

        objO.CurrentNodeName(this.getCurrentNodeName());
        objO.JobSchedulerID.Value(spooler.id());
        objO.JobSchedulerLogFilesPath.Value(spooler.log_dir());
		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		
		objO.CheckMandatory();
        objR.setJSJobUtilites(this);
		objR.Execute();
		
		try {
			spooler.log().start_new_file(); // this will start with a fresh log file
		}
		catch (Exception e) {
            throw new JobSchedulerException("an error occurred rotating log file: " + e.getMessage(), e);
		}
	} // doProcessing
}

