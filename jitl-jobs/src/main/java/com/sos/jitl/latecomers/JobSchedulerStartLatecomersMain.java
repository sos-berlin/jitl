

package com.sos.jitl.latecomers;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;

public class JobSchedulerStartLatecomersMain extends JSToolBox {
	protected JobSchedulerStartLatecomersOptions	objOptions = null;
	private static final String CLASSNAME = "JobSchedulerStartLatecomersMain"; 
	private static final Logger LOGGER = Logger.getLogger(JobSchedulerStartLatecomersMain.class);
 
	public final static void main(String[] pstrArgs) {
		final String METHODNAME = CLASSNAME + "::Main"; 
		LOGGER.info("JobSchedulerStartLatecomers - Main"); 
		try {
			JobSchedulerStartLatecomers objM = new JobSchedulerStartLatecomers();
			JobSchedulerStartLatecomersOptions objO = objM.getOptions();
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
