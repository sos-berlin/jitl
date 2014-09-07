

package com.sos.jitl.housekeeping.rotatelog;

import java.util.HashMap;

import com.sos.jitl.housekeeping.rotatelog.JobSchedulerRotateLog;
import com.sos.jitl.housekeeping.rotatelog.JobSchedulerRotateLogOptions;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler Java-API-Jobs
import org.apache.log4j.Logger;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.localization.*;
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

	@Override
	public void spooler_exit() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_exit";
		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcessing";

		JobSchedulerRotateLog objR = new JobSchedulerRotateLog();
		JobSchedulerRotateLogOptions objO = objR.Options();

        objO.CurrentNodeName(this.getCurrentNodeName());
		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		objO.CheckMandatory();
        objR.setJSJobUtilites(this);
		objR.Execute();
	} // doProcessing

}

