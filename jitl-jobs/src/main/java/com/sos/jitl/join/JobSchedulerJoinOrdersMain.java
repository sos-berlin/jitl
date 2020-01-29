

package com.sos.jitl.join;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;

public class JobSchedulerJoinOrdersMain extends JSToolBox {
	protected JobSchedulerJoinOrdersOptions	objOptions = null;
	private static final String CLASSNAME = "JobSchedulerJoinOrdersMain"; 
	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerJoinOrdersMain.class);
 
	public final static void main(String[] pstrArgs) {
		final String METHODNAME = CLASSNAME + "::Main"; 
		LOGGER.info("JobSchedulerJoinOrders - Main"); 
		try {
			JobSchedulerJoinOrders objM = new JobSchedulerJoinOrders();
			JobSchedulerJoinOrdersOptions objO = objM.getOptions();
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
