package com.sos.jitl.notification.jobs.reset;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;

public class ResetNotificationsJobMain extends JSToolBox {
	private final static String	className = ResetNotificationsJobMain.class.getSimpleName();
	private static final Logger LOGGER = LoggerFactory.getLogger(ResetNotificationsJobMain.class);

	public final static void main(String[] args) {
		final String methodName = className + "::main";

		LOGGER.info(String.format(methodName));
		int exitCode = 0;
		ResetNotificationsJob job = new ResetNotificationsJob();
		try {
			ResetNotificationsJobOptions options = job.getOptions();
			options.commandLineArgs(args);
			
			job.init();
			job.execute();
			
			LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", methodName));
		}
		catch (Exception e) {
			exitCode = 99;
			LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", methodName, exitCode), e);		
		}
		finally{
			job.exit();
		}
		System.exit(exitCode);
	}

}