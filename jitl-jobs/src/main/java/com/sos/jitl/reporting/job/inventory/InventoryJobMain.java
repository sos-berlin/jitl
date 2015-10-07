package com.sos.jitl.reporting.job.inventory;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;

/**
 * \class InventoryJobMain - Main-Class for "Inventory"
 * 
 * \brief MainClass to launch CheckHistoryJob as an executable command-line
 * program
 * 
 * This Class InventoryJobMain is the worker-class.
 * 
 */
public class InventoryJobMain extends JSToolBox {
	private final static String conClassName = InventoryJobMain.class
			.getSimpleName();
	private static Logger logger = Logger.getLogger(InventoryJobMain.class);

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
			InventoryJob job = new InventoryJob();
			InventoryJobOptions options = job.getOptions();

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