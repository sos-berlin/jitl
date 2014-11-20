package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

// Super-Class for JobScheduler Java-API-Jobs
/**
 * \class 		JSEventsClientJSAdapterClass - JobScheduler Adapter for "Submit and Delete Events"
 *
 * \brief AdapterClass of JSEventsClient for the SOSJobScheduler
 *
 * This Class JSEventsClientJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JSEventsClient.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20130109134235
 * \endverbatim
 */
public class JSEventsClientJSAdapterClass extends JSEventsClientBaseClass {
	private final String	conClassName	= "JSEventsClientJSAdapterClass";						//$NON-NLS-1$
	private static Logger	logger			= Logger.getLogger(JSEventsClientJSAdapterClass.class);

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

	@Override
	public boolean spooler_process() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_process"; //$NON-NLS-1$

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
		return signalSuccess();

	} // spooler_process


}
