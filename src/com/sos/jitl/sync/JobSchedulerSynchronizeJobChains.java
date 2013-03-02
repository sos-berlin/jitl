package com.sos.jitl.sync;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;

/**
 * \class 		JobSchedulerSynchronizeJobChains - Workerclass for "Synchronize Job Chains"
 *
 * \brief AdapterClass of JobSchedulerSynchronizeJobChains for the SOSJobScheduler
 *
 * This Class JobSchedulerSynchronizeJobChains is the worker-class.
 *
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\scheduler_ur\config\JOETemplates\java\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20121217120436
 * \endverbatim
 */
public class JobSchedulerSynchronizeJobChains extends JSJobUtilitiesClass <JobSchedulerSynchronizeJobChainsOptions> {
	private final String								conClassName		= "JobSchedulerSynchronizeJobChains";
	private static Logger								logger				= Logger.getLogger(JobSchedulerSynchronizeJobChains.class);

	protected JobSchedulerSynchronizeJobChainsOptions	objOptions			= null;
	private final JSJobUtilities								objJSJobUtilities	= this;
	protected SyncNodeContainer							syncNodeContainer;
	protected HashMap<String, String>					SchedulerParameters	= new HashMap<String, String>();

	/**
	 *
	 * \brief JobSchedulerSynchronizeJobChains
	 *
	 * \details
	 *
	 */
	public JobSchedulerSynchronizeJobChains() {
		super(new JobSchedulerSynchronizeJobChainsOptions());
	}

	/**
	 *
	 * \brief Options - returns the JobSchedulerSynchronizeJobChainsOptionClass
	 *
	 * \details
	 * The JobSchedulerSynchronizeJobChainsOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *
	 * \return JobSchedulerSynchronizeJobChainsOptions
	 *
	 */
	@Override
	public JobSchedulerSynchronizeJobChainsOptions Options() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new JobSchedulerSynchronizeJobChainsOptions();
		}
		return objOptions;
	}

	/**
	 *
	 * \brief Execute - Start the Execution of JobSchedulerSynchronizeJobChains
	 *
	 * \details
	 *
	 * For more details see
	 *
	 * \see JobSchedulerAdapterClass
	 * \see JobSchedulerSynchronizeJobChainsMain
	 *
	 * \return JobSchedulerSynchronizeJobChains
	 *
	 * @return
	 */
	public JobSchedulerSynchronizeJobChains Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

		//		logger.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName ) );

		try {
			Options().CheckMandatory();
			logger.debug(Options().dirtyString());

			syncNodeContainer = new SyncNodeContainer();

			syncNodeContainer.setSyncId(Options().sync_session_id.Value());
			syncNodeContainer.setJobpath(Options().jobpath.Value());
			syncNodeContainer.getNodes(Options().jobchains_answer.Value());
			syncNodeContainer.getOrders(Options().orders_answer.Value());
			syncNodeContainer.setRequiredOrders(SchedulerParameters);

			if (syncNodeContainer.isReleased()) {
				logger.debug("Release all orders");
			}
			else {
				logger.debug("Suspending all order");
			}

		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format(Messages.getMsg("JSJ-I-107"), conMethodName), e);
			throw e;
		}
		finally {
			logger.debug(String.format(Messages.getMsg("JSJ-I-111"), conMethodName));
		}

		return this;
	}

	public void init() throws RuntimeException, Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() throws RuntimeException, Exception {

	} // doInitialize

	public void setSchedulerParameters(final HashMap<String, String> schedulerParameters) {
		SchedulerParameters = schedulerParameters;
	}

} // class JobSchedulerSynchronizeJobChains