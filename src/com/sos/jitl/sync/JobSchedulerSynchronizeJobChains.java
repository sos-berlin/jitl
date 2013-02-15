package com.sos.jitl.sync;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;

/**
 * \class 		JobSchedulerSynchronizeJobChains - Workerclass for "Synchronize Job Chains"
 *
 * \brief AdapterClass of JobSchedulerSynchronizeJobChains for the SOSJobScheduler
 *
 * This Class JobSchedulerSynchronizeJobChains is the worker-class.
 *

 *
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\scheduler_ur\config\JOETemplates\java\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20121217120436
 * \endverbatim
 */
public class JobSchedulerSynchronizeJobChains extends JSToolBox implements JSJobUtilities {
	private final String								conClassName		= "JobSchedulerSynchronizeJobChains";
	private static Logger								logger				= Logger.getLogger(JobSchedulerSynchronizeJobChains.class);

	protected JobSchedulerSynchronizeJobChainsOptions	objOptions			= null;
	private JSJobUtilities								objJSJobUtilities	= this;
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
		super("com_sos_scheduler_messages");
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
	 * \brief Options - set the JobSchedulerSynchronizeJobChainsOptionClass
	 *
	 * \details
	 * The JobSchedulerSynchronizeJobChainsOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *
	 * \return JobSchedulerSynchronizeJobChainsOptions
	 *
	 */
	public JobSchedulerSynchronizeJobChainsOptions Options(final JobSchedulerSynchronizeJobChainsOptions pobjOptions) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		objOptions = pobjOptions;
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
			//			logger.debug(Options().toString());

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

	@Override
	public String myReplaceAll(final String pstrSourceString, final String pstrReplaceWhat, final String pstrReplaceWith) {

		String newReplacement = pstrReplaceWith.replaceAll("\\$", "\\\\\\$");
		return pstrSourceString.replaceAll("(?m)" + pstrReplaceWhat, newReplacement);
	}

	/**
	 *
	 * \brief replaceSchedulerVars
	 *
	 * \details
	 * Dummy-Method to make sure, that there is always a valid Instance for the JSJobUtilities.
	 * \return
	 *
	 * @param isWindows
	 * @param pstrString2Modify
	 * @return
	 */
	@Override
	public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
		logger.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
		return pstrString2Modify;
	}

	/**
	 *
	 * \brief setJSParam
	 *
	 * \details
	 * Dummy-Method to make shure, that there is always a valid Instance for the JSJobUtilities.
	 * \return
	 *
	 * @param pstrKey
	 * @param pstrValue
	 */
	@Override
	public void setJSParam(final String pstrKey, final String pstrValue) {

	}

	@Override
	public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {

	}

	/**
	 *
	 * \brief setJSJobUtilites
	 *
	 * \details
	 * The JobUtilities are a set of methods used by the SSH-Job or can be used be other, similar, job-
	 * implementations.
	 *
	 * \return void
	 *
	 * @param pobjJSJobUtilities
	 */
	@Override
	public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {

		if (pobjJSJobUtilities == null) {
			objJSJobUtilities = this;
		}
		else {
			objJSJobUtilities = pobjJSJobUtilities;
		}
		logger.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
	}

	@Override
	public String getCurrentNodeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStateText(final String arg0) {
		// TODO Auto-generated method stub

	}

	public void setSchedulerParameters(final HashMap<String, String> schedulerParameters) {
		SchedulerParameters = schedulerParameters;
	}

} // class JobSchedulerSynchronizeJobChains