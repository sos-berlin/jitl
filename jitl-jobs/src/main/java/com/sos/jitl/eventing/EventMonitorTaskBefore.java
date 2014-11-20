package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

// Super-Class for JobScheduler Java-API-Jobs
/**
 * \class 		EventMonitorTaskBefore - JobScheduler Adapter for "Submit and Delete Events"
 *
 * \brief AdapterClass of JSMailClient for the SOSJobScheduler
 *
 * This Class EventMonitorTaskBefore works as an adapter-class between the SOS
 * JobScheduler and the worker-class JSEventsClient.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20130109134235
 * \endverbatim
 */
public class EventMonitorTaskBefore extends JSEventsClientBaseClass {
	private final String	conClassName	= "EventMonitorTaskBefore";						//$NON-NLS-1$
	private static Logger	logger			= Logger.getLogger(EventMonitorTaskBefore.class);

	@Override
	public boolean spooler_task_before() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_task_before"; //$NON-NLS-1$

		try {
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(e.getLocalizedMessage());
			throw e;
		}
		finally {
		} // finally
		return continue_with_task;   // Task can start

	} // spooler_task_before


}
