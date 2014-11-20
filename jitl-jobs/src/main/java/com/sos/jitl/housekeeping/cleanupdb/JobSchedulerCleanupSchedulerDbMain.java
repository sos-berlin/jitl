

package com.sos.jitl.housekeeping.cleanupdb;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Logging.Log4JHelper;


/**
 * \class 		JobSchedulerCleanupSchedulerDbMain - Main-Class for "Delete log entries in the Job Scheduler history Databaser tables"
 *
 * \brief MainClass to launch JobSchedulerCleanupSchedulerDb as an executable command-line program
 *
 * This Class JobSchedulerCleanupSchedulerDbMain is the worker-class.
 *

 *
 * see \see C:\Dokumente und Einstellungen\Uwe Risse\Lokale Einstellungen\Temp\scheduler_editor-3271913404894833399.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Dokumente und Einstellungen\Uwe Risse\Eigene Dateien\sos-berlin.com\jobscheduler\scheduler_ur_current\config\JOETemplates\java\xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20121211160841 
 * \endverbatim
 */
public class JobSchedulerCleanupSchedulerDbMain extends JSToolBox {
	private final static String					conClassName						= "JobSchedulerCleanupSchedulerDbMain"; //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerCleanupSchedulerDbMain.class);
	@SuppressWarnings("unused")	
	private static Log4JHelper	objLogger		= null;

	protected JobSchedulerCleanupSchedulerDbOptions	objOptions			= null;

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

		objLogger = new Log4JHelper("./log4j.properties"); //$NON-NLS-1$

		logger = Logger.getRootLogger();
		logger.info("JobSchedulerCleanupSchedulerDb - Main"); //$NON-NLS-1$

		try {
			JobSchedulerCleanupSchedulerDb objM = new JobSchedulerCleanupSchedulerDb();
			JobSchedulerCleanupSchedulerDbOptions objO = objM.Options();
			
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

}  // class JobSchedulerCleanupSchedulerDbMain