
package com.sos.jitl.mailprocessor;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;

public class SOSMailProcessInboxMain extends JSToolBox {
	protected SOSMailProcessInboxOptions objOptions = null;
	private static final String CLASSNAME = "SOSMailProcessInboxMain";
	private static final Logger LOGGER = Logger.getLogger(SOSMailProcessInboxMain.class);

	public final static void main(String[] pstrArgs) {
		final String METHODNAME = CLASSNAME + "::Main";
		LOGGER.info("SOSMailProcessInbox - Main");
		try {
			SOSMailProcessInbox objM = new SOSMailProcessInbox();
			SOSMailProcessInboxOptions objO = objM.getOptions();
			objO.commandLineArgs(pstrArgs);
			objM.execute();
		} catch (Exception e) {
			System.err.println(METHODNAME + ": " + "Error occured ..." + e.getMessage());
			LOGGER.error(e.getMessage(), e);
			int intExitCode = 99;
			LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", METHODNAME, intExitCode), e);
			System.exit(intExitCode);
		}
		LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", METHODNAME));
	}

}
