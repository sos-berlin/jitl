

package com.sos.jitl.textprocessor;

import java.io.File;

import com.sos.jitl.textprocessor.JobSchedulerTextProcessor;
import com.sos.jitl.textprocessor.JobSchedulerTextProcessorOptions;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.localization.*;
import com.sos.scheduler.messages.JSMessages;
import com.sos.JSHelper.Basics.JSJobUtilities;


public class JobSchedulerTextProcessor extends JSJobUtilitiesClass <JobSchedulerTextProcessorOptions>{  
	private final String					conClassName						= "JobSchedulerTextProcessor";  //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerTextProcessor.class);

	protected JobSchedulerTextProcessorOptions	objOptions			= null;
    private JSJobUtilities      objJSJobUtilities   = this;


	 
	public JobSchedulerTextProcessor() {
		super(new JobSchedulerTextProcessorOptions());
	}

	 
	public JobSchedulerTextProcessorOptions Options() {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new JobSchedulerTextProcessorOptions();
		}
		return objOptions;
	}

	 
	public JobSchedulerTextProcessorOptions Options(final JobSchedulerTextProcessorOptions pobjOptions) {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		objOptions = pobjOptions;
		return objOptions;
	}

	 
	public JobSchedulerTextProcessor Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

        logger.debug(String.format(JSMessages.JSJ_I_110.get(), conMethodName ));
        File inputFile = new File(Options().filename.Value());
        String command = Options().command.Value();
        String param = Options().param.Value();
  
        JobSchedulerTextProcessorExecuter jobSchedulerTextProcessorExecuter = new JobSchedulerTextProcessorExecuter(inputFile, command + " " + param);
        String result = jobSchedulerTextProcessorExecuter.execute();
        Options().result.Value(result);

		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
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

     
    @Override
    public String replaceSchedulerVars(boolean isWindows, String pstrString2Modify) {
        logger.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
        return pstrString2Modify;
    }

    
    @Override
    public void setJSParam(String pstrKey, String pstrValue) {

    }

    @Override
    public void setJSParam(String pstrKey, StringBuffer pstrValue) {

    }

   
    public void setJSJobUtilites(JSJobUtilities pobjJSJobUtilities) {

        if (pobjJSJobUtilities == null) {
            objJSJobUtilities = this;
        }
        else {
            objJSJobUtilities = pobjJSJobUtilities;
        }
        logger.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }



}  // class JobSchedulerTextProcessor