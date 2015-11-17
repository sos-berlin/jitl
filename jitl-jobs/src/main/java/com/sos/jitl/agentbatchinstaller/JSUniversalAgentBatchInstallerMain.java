

package com.sos.jitl.agentbatchinstaller;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;

 
public class JSUniversalAgentBatchInstallerMain extends JSToolBox {
	private final static String					conClassName						= "JSUniversalAgentBatchInstallerMain"; //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JSUniversalAgentBatchInstallerMain.class);
	protected JSUniversalAgentBatchInstallerOptions	objOptions			= null;

	 
	public final static void main(String[] pstrArgs) {

		final String conMethodName = conClassName + "::Main"; //$NON-NLS-1$
		logger.info("JSUniversalAgentBatchInstallerMain - Main"); //$NON-NLS-1$

		try {
			JSUniversalAgentBatchInstaller objM = new JSUniversalAgentBatchInstaller();
			JSUniversalAgentBatchInstallerOptions objO = objM.options();
			
			objO.CommandLineArgs(pstrArgs);
			objM.execute();
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

}  // class JSUniversalAgentBatchInstallerMain