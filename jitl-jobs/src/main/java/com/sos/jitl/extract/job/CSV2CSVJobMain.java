package com.sos.jitl.extract.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class CSV2CSVJobMain extends JSToolBox {
	private final static String conClassName = CSV2CSVJobMain.class
			.getSimpleName();
	private static Logger logger = LoggerFactory.getLogger(CSV2CSVJobMain.class);

	
	/**
	 * 
	 * @param args
	 */
	public final static void main(String[] args) {
		final String method = conClassName + "::Main"; //$NON-NLS-1$

		logger.info(String.format("%s: Start",method));
		CSV2CSVJob job = null;
		try {
			job = new CSV2CSVJob();
			CSV2CSVJobOptions options = job.Options();
			
			options.CommandLineArgs(args);
			
			job.Execute();
		}
		
		catch (Exception e) {
			System.err.println(String.format("%s: %s",method,e.toString()));
			e.printStackTrace(System.err);
			int intExitCode = 99;
			logger.error(String.format("%s: exit-code %s",method, intExitCode), e);		
			System.exit(intExitCode);
		}
		logger.info(String.format("%s: End",method));
	}

}  // class CSV2CSVJobMain