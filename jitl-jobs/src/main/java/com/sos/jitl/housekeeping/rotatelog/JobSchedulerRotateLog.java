package com.sos.jitl.housekeeping.rotatelog;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.messages.JSMsg;

/**
 * \class 		JobSchedulerRotateLog - Workerclass for "Rotate compress and delete log files"
 *
 * \brief AdapterClass of JobSchedulerRotateLog for the SOSJobScheduler
 *
 * This Class JobSchedulerRotateLog is the worker-class.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-1724231827372138737html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by com/sos/resources/xsl/JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20140906131052 
 * \endverbatim
 */
public class JobSchedulerRotateLog extends JSJobUtilitiesClass<JobSchedulerRotateLogOptions> {
	@SuppressWarnings("unused")
	private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused")
	private final Logger logger = Logger.getLogger(this.getClass());


	/**
	 * 
	 * \brief JobSchedulerRotateLog
	 *
	 * \details
	 *
	 */
	public JobSchedulerRotateLog() {
		super(new JobSchedulerRotateLogOptions());
	}

	/**
	 * 
	 * \brief Options - returns the JobSchedulerRotateLogOptionClass
	 * 
	 * \details
	 * The JobSchedulerRotateLogOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return JobSchedulerRotateLogOptions
	 *
	 */
	@Override
	public JobSchedulerRotateLogOptions Options() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options";

		if (objOptions == null) {
			objOptions = new JobSchedulerRotateLogOptions();
		}
		return objOptions;
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of JobSchedulerRotateLog
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see JobSchedulerRotateLogMain
	 * 
	 * \return JobSchedulerRotateLog
	 *
	 * @return
	 */
	public JobSchedulerRotateLog Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";

		logger.debug(String.format(new JSMsg("JSJ-I-110").get(), conMethodName));

		try {
			Options().CheckMandatory();
			logger.debug(Options().dirtyString());
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format(new JSMsg("JSJ-I-107").get(), conMethodName), e);
			throw e;
		}
		finally {
			logger.debug(String.format(new JSMsg("JSJ-I-111").get(), conMethodName));
		}

		return this;
	}

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init";
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

} // class JobSchedulerRotateLog