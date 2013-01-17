package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

// Super-Class for JobScheduler Java-API-Jobs
/**
 * \class 		EventMonitorTaskAfter - JobScheduler Adapter for "Submit and Delete Events"
 *
 * \brief AdapterClass of EventMonitorTaskAfter for the SOSJobScheduler
 *
 * This Class EventMonitorTaskAfter works as an adapter-class between the SOS
 * JobScheduler and the worker-class JSEventsClient.
 *
 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20130109134235
 * \endverbatim
 */
public class EventMonitorTaskAfter extends JSEventsClientBaseClass {
	private final String	conClassName	= "EventMonitorTaskAfter";
	private static Logger	logger			= Logger.getLogger(EventMonitorTaskAfter.class);

	@Override
	public void spooler_task_after() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_process";

		try {
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			throw e;
		}
		finally {
		} // finally
	} // spooler_process
}
