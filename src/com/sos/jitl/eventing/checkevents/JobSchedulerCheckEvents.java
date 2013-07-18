

package com.sos.jitl.eventing.checkevents;

import com.sos.jitl.eventing.checkevents.JobSchedulerCheckEvents;
import com.sos.jitl.eventing.checkevents.JobSchedulerCheckEventsOptions;
import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.localization.*;
import com.sos.JSHelper.Basics.JSJobUtilities;

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
	private final String					conClassName						= "JobSchedulerCheckEvents";  //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerCheckEvents.class);

	protected JobSchedulerCheckEventsOptions	objOptions			= null;
    private JSJobUtilities      objJSJobUtilities   = this;


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

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

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

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

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
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

		logger.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName ) );

		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format(Messages.getMsg("JSJ-I-107"), conMethodName ), e);
            throw e;			
		}
		finally {
			logger.debug(String.format(Messages.getMsg("JSJ-I-111"), conMethodName ) );
		}
		
		return this;
	}

	public void init() {
		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::init";  //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

    public String myReplaceAll(String pstrSourceString, String pstrReplaceWhat, String pstrReplaceWith) {

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
    public String replaceSchedulerVars(boolean isWindows, String pstrString2Modify) {
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
    public void setJSParam(String pstrKey, String pstrValue) {

    }

    @Override
    public void setJSParam(String pstrKey, StringBuffer pstrValue) {

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
    public void setJSJobUtilites(JSJobUtilities pobjJSJobUtilities) {

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
	public void setStateText(String pstrStateText) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCC(int pintCC) {
		// TODO Auto-generated method stub
		
	}



}  // class JobSchedulerCheckEvents