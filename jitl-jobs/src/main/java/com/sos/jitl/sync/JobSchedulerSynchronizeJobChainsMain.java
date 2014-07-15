

package com.sos.jitl.sync;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;


/**
 * \class 		JobSchedulerSynchronizeJobChainsMain - Main-Class for "Synchronize Job Chains"
 *
 * \brief MainClass to launch JobSchedulerSynchronizeJobChains as an executable command-line program
 *
 * This Class JobSchedulerSynchronizeJobChainsMain is the worker-class.
 *

 *
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\scheduler_ur\config\JOETemplates\java\xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20121217120436 
 * \endverbatim
 */
public class JobSchedulerSynchronizeJobChainsMain extends JSToolBox {
	private final static String					conClassName						= "JobSchedulerSynchronizeJobChainsMain"; //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerSynchronizeJobChainsMain.class);

	protected JobSchedulerSynchronizeJobChainsOptions	objOptions			= null;

	/**
	 * 
	 * \brief main
	 * 
	 * \details
	 *
	 * \return void
	 *
	 * @param pstrArgs
	 * @throws Exception
	 */
	public final static void main(String[] pstrArgs) {

		final String conMethodName = conClassName + "::Main"; //$NON-NLS-1$

		logger.info("JobSchedulerSynchronizeJobChains - Main"); //$NON-NLS-1$

		try {
			JobSchedulerSynchronizeJobChains objM = new JobSchedulerSynchronizeJobChains();
			JobSchedulerSynchronizeJobChainsOptions objO = objM.Options();
			
			objO.CommandLineArgs(pstrArgs);
			objM.Execute();
		}
		
		catch (Exception e) {
			System.err.println(conMethodName + ": " + "Error occured ..." + e.getMessage()); 
			e.printStackTrace(System.err);
			int intExitCode = 99;
			logger.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", conMethodName, intExitCode), e);		
			System.exit(intExitCode);
		}
		
		logger.info(String.format("JSJ-I-106: %1$s - ended without errors", conMethodName));		
	}

}  // class JobSchedulerSynchronizeJobChainsMain