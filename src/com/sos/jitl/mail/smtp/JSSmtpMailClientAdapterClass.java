package com.sos.jitl.mail.smtp;

import org.apache.log4j.Logger;

// Super-Class for JobScheduler Java-API-Jobs
/**
 * \class 		JSJSSmtpMailClientAdapterClass - JobScheduler Adapter for "Submit and Delete Events"
 *
 * \brief AdapterClass of JSJSSmtpMailClient for the SOSJobScheduler
 *
 * This Class JSJSSmtpMailClientAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JSJSSmtpMailClient.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20130109134235
 * \endverbatim
 */
public class JSSmtpMailClientAdapterClass extends JSSmtpMailClientBaseClass {
	private final String	conClassName	= "JSSmtpMailClientAdapterClass";						//$NON-NLS-1$
	private static Logger	logger			= Logger.getLogger(JSSmtpMailClientAdapterClass.class);

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
