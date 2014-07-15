package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;

/**
 * \class 		JSEventsClientMain - Main-Class for "Submit and Delete Events"
 *
 * \brief MainClass to launch JSEventsClient as an executable command-line program
 *
 * This Class JSEventsClientMain is the worker-class.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20130109134235
 * \endverbatim
 */
public class JSEventsClientMain extends JSToolBox {
	private final static String		conClassName	= "JSEventsClientMain";						//$NON-NLS-1$
	private static Logger			logger			= Logger.getLogger(JSEventsClientMain.class);
	protected JSEventsClientOptions	objOptions		= null;

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
	public final static void main(final String[] pstrArgs) {

		final String conMethodName = conClassName + "::Main"; //$NON-NLS-1$
		logger.info("JSEventsClient - Main"); //$NON-NLS-1$

		try {
			JSEventsClient objM = new JSEventsClient();
			JSEventsClientOptions objO = objM.Options();

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

} // class JSEventsClientMain