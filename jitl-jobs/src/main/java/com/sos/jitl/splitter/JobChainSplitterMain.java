

package com.sos.jitl.splitter;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;
 
public class JobChainSplitterMain extends JSToolBox {
	private final static String					conClassName						= "JobChainSplitterMain"; //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobChainSplitterMain.class);

	protected JobChainSplitterOptions	objOptions			= null;

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

		logger.info("JobChainSplitter - Main"); //$NON-NLS-1$

		try {
			JobChainSplitter objM = new JobChainSplitter();
			JobChainSplitterOptions objO = objM.getOptions();
			
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

}  // class JobChainSplitterMain