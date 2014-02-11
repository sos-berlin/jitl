package com.sos.jitl.eventing.checkevents;
import java.io.File;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;

/**
 * \class 		JobSchedulerCheckEvents - Workerclass for "Check if events exist"
 *
 * \brief AdapterClass of JobSchedulerCheckEvents for the SOSJobScheduler
 *
 * This Class JobSchedulerCheckEvents is the worker-class.
 *

 *
 *
 * \verbatim ;
 * \endverbatim
 */
public class JobSchedulerCheckEvents extends JSToolBox implements JSJobUtilities {
	private final String						conClassName		= "JobSchedulerCheckEvents";						//$NON-NLS-1$
	private static Logger						logger				= Logger.getLogger(JobSchedulerCheckEvents.class);
	protected JobSchedulerCheckEventsOptions	objOptions			= null;
	private JSJobUtilities						objJSJobUtilities	= this;
	protected boolean							exist				= false;

	/**
	 * 
	 * \brief JobSchedulerCheckEvents
	 *
	 * \details
	 *
	 */
	public JobSchedulerCheckEvents() {
		super();
	}

	/**
	 * 
	 * \brief Options - returns the JobSchedulerCheckEventsOptionClass
	 * 
	 * \details
	 * The JobSchedulerCheckEventsOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return JobSchedulerCheckEventsOptions
	 *
	 */
	public JobSchedulerCheckEventsOptions Options() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$
		if (objOptions == null) {
			objOptions = new JobSchedulerCheckEventsOptions();
		}
		return objOptions;
	}

	/**
	 * 
	 * \brief Options - set the JobSchedulerCheckEventsOptionClass
	 * 
	 * \details
	 * The JobSchedulerCheckEventsOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return JobSchedulerCheckEventsOptions
	 *
	 */
	public JobSchedulerCheckEventsOptions Options(final JobSchedulerCheckEventsOptions pobjOptions) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$
		objOptions = pobjOptions;
		return objOptions;
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of JobSchedulerCheckEvents
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see JobSchedulerCheckEventsMain
	 * 
	 * \return JobSchedulerCheckEvents
	 *
	 * @return
	 */
	public JobSchedulerCheckEvents Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$
		try {
			Options().CheckMandatory();
			logger.debug(Options().toString());
			exist = false;
			SchedulerEventDBLayer schedulerEventDBLayer = new SchedulerEventDBLayer(new File(objOptions.configuration_file.Value()));
			if (objOptions.event_condition.isDirty()) {
				if (objOptions.event_class.isDirty()) {
					exist = schedulerEventDBLayer.checkEventExists(objOptions.event_condition.Value(), objOptions.event_class.Value());
				}
				else {
					exist = schedulerEventDBLayer.checkEventExists(objOptions.event_condition.Value());
				}
			}
			else {
				SchedulerEventFilter schedulerEventFilter = new SchedulerEventFilter();
				schedulerEventFilter.setEventClass(objOptions.event_class.Value());
				schedulerEventFilter.setEventId(objOptions.event_id.Value());
				schedulerEventFilter.setExitCode(objOptions.event_exit_code.Value());
				schedulerEventFilter.setSchedulerId(objOptions.event_scheduler_id.Value());
				schedulerEventFilter.setSchedulerId(objOptions.remote_scheduler_host.Value());
				schedulerEventFilter.setSchedulerId(objOptions.remote_scheduler_port.Value());
				schedulerEventFilter.setSchedulerId(objOptions.event_job_chain.Value());
				schedulerEventFilter.setSchedulerId(objOptions.event_order_id.Value());
				schedulerEventFilter.setSchedulerId(objOptions.event_job.Value());
				exist = schedulerEventDBLayer.checkEventExists(schedulerEventFilter);
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			throw e;
		}
		finally {
		}
		return this;
	}

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() {
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
	@Override public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
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
	@Override public void setJSParam(final String pstrKey, final String pstrValue) {
	}

	@Override public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {
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

	@Override public String getCurrentNodeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public void setStateText(final String pstrStateText) {
		// TODO Auto-generated method stub
	}

	@Override public void setCC(final int pintCC) {
		// TODO Auto-generated method stub
	}
} // class JobSchedulerCheckEvents