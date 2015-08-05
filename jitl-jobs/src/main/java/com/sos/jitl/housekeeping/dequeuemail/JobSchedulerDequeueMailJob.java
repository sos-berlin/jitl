

package com.sos.jitl.housekeeping.dequeuemail;

import com.sos.jitl.housekeeping.dequeuemail.JobSchedulerDequeueMailJob;
import com.sos.jitl.housekeeping.dequeuemail.JobSchedulerDequeueMailJobOptions;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.localization.*;
import com.sos.scheduler.messages.JSMessages;
import com.sos.JSHelper.Basics.JSJobUtilities;



public class JobSchedulerDequeueMailJob extends JSJobUtilitiesClass <JobSchedulerDequeueMailJobOptions>{  
	private final String					conClassName						= "JobSchedulerDequeueMailJob";  //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerDequeueMailJob.class);

	protected JobSchedulerDequeueMailJobOptions	objOptions			= null;
    private JSJobUtilities      objJSJobUtilities   = this;


	/**
	 * 
	 * \brief JobSchedulerDequeueMailJob
	 *
	 * \details
	 *
	 */
	public JobSchedulerDequeueMailJob() {
		super(new JobSchedulerDequeueMailJobOptions());
	}

	/**
	 * 
	 * \brief Options - returns the JobSchedulerDequeueMailJobOptionClass
	 * 
	 * \details
	 * The JobSchedulerDequeueMailJobOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return JobSchedulerDequeueMailJobOptions
	 *
	 */
	public JobSchedulerDequeueMailJobOptions Options() {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new JobSchedulerDequeueMailJobOptions();
		}
		return objOptions;
	}

	/**
	 * 
	 * \brief Options - set the JobSchedulerDequeueMailJobOptionClass
	 * 
	 * \details
	 * The JobSchedulerDequeueMailJobOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return JobSchedulerDequeueMailJobOptions
	 *
	 */
	public JobSchedulerDequeueMailJobOptions Options(final JobSchedulerDequeueMailJobOptions pobjOptions) {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		objOptions = pobjOptions;
		return objOptions;
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of JobSchedulerDequeueMailJob
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see JobSchedulerDequeueMailJobMain
	 * 
	 * \return JobSchedulerDequeueMailJob
	 *
	 * @return
	 */
	public JobSchedulerDequeueMailJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

		logger.debug(String.format(JSMessages.JSJ_I_110.get(), conMethodName ));

		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
			DequeueMailExecuter dequeueMailExecuter = new DequeueMailExecuter(Options());
			dequeueMailExecuter.execute();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
	        logger.error(String.format(JSMessages.JSJ_F_107.get(), conMethodName ),e);
            throw e;			
		}
		finally {
	        logger.debug(String.format(JSMessages.JSJ_I_111.get(), conMethodName ));
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



}  // class JobSchedulerDequeueMailJob