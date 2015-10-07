package com.sos.jitl.reporting.job.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;

/**
 * \class FactJobMain - Main-Class for "Fact"
 * 
 * \brief MainClass to launch CheckHistoryJob as an executable command-line
 * program
 * 
 * This Class FactJobMain is the worker-class.
 * 
 */
public class FactJobMain extends JSToolBox {
	private final static String conClassName = FactJobMain.class
			.getSimpleName();
	private static Logger logger = LoggerFactory.getLogger(FactJobMain.class);


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
	public final static void main(String[] args) {

		final String conMethodName = conClassName + "::Main"; //$NON-NLS-1$

		logger.info(conMethodName); //$NON-NLS-1$

		try {
			FactJob job = new FactJob();
			FactJobOptions options = job.getOptions();

			options.CommandLineArgs(args);
			job.Execute();
		}

		catch (Exception e) {
			System.err.println(conMethodName + ": " + "Error occured ..."
					+ e.getMessage());
			e.printStackTrace(System.err);
			int intExitCode = 99;
			logger.error(String.format(
					"JSJ-E-105: %1$s - terminated with exit-code %2$d",
					conMethodName, intExitCode), e);
			System.exit(intExitCode);
		}

		logger.info(String.format("JSJ-I-106: %1$s - ended without errors",
				conMethodName));
	}

}