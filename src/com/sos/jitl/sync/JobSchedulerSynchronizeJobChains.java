

package com.sos.jitl.sync;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;

import com.sos.jitl.sync.JobSchedulerSynchronizeJobChains;
import com.sos.jitl.sync.JobSchedulerSynchronizeJobChainsOptions;
import org.apache.log4j.Logger;

import sos.spooler.Job_chain;
import sos.spooler.Job_chain_node;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Basics.JSJobUtilities;

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
	private final String					conClassName						= "JobSchedulerSynchronizeJobChains";  //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerSynchronizeJobChains.class);

	protected JobSchedulerSynchronizeJobChainsOptions	objOptions			= null;
    private JSJobUtilities      objJSJobUtilities   = this;
    protected SyncNodeContainer syncNodeContainer;
	protected HashMap<String, String>	SchedulerParameters	= new HashMap<String, String>();


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

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

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

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

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
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

		logger.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName ) );

		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
    
			
			 syncNodeContainer = new SyncNodeContainer();
		       
		     syncNodeContainer.setSyncId(Options().sync_session_id.Value());
			 syncNodeContainer.setJobpath(Options().jobpath.Value());
			 syncNodeContainer.getNodes(Options().jobchains_answer.Value());
			 syncNodeContainer.getOrders(Options().orders_answer.Value());
			 syncNodeContainer.setRequiredOrders(SchedulerParameters);
		
			if (syncNodeContainer.isReleased()){
		      	logger.debug("Release all orders");
		    }else{
		      	logger.debug("Suspending all order");
		    }
			
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

	public void init() throws RuntimeException, Exception {
		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::init";  //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() throws RuntimeException, Exception {
       
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
	public void setStateText(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setSchedulerParameters(HashMap<String, String> schedulerParameters) {
		SchedulerParameters = schedulerParameters;
	}



}  // class JobSchedulerSynchronizeJobChains