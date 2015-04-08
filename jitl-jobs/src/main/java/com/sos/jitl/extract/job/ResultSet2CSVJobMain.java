package com.sos.jitl.extract.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class ResultSet2CSVJobMain extends JSToolBox {
	private final static String conClassName = ResultSet2CSVJobMain.class
			.getSimpleName();
	private static Logger logger = LoggerFactory.getLogger(ResultSet2CSVJobMain.class);

	
	/**
	 * 
	 * @param args
	 */
	public final static void main(String[] args) {
		final String method = conClassName + "::Main"; //$NON-NLS-1$

		logger.info(String.format("%s: Start",method));
		ResultSet2CSVJob job = null;
		try {
			job = new ResultSet2CSVJob();
			ResultSet2CSVJobOptions options = job.Options();
			
			options.CommandLineArgs(args);
			
			job.init();
			job.Execute();
		}
		
		catch (Exception e) {
			System.err.println(String.format("%s: %s",method,e.toString()));
			e.printStackTrace(System.err);
			int intExitCode = 99;
			logger.error(String.format("%s: exit-code %s",method, intExitCode), e);		
			System.exit(intExitCode);
		}
		finally{
			job.exit();
		}
		logger.info(String.format("%s: End",method));
	}

}  // class ResultSet2CSVJobMain