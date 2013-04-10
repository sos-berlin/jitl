package com.sos.jitl.mail.smtp;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;

// Super-Class for JobScheduler Java-API-Jobs
/**
 * \class 		JSMailClientJSAdapterClass - JobScheduler Adapter for "sending eMails via SMTP"
 *
 * \brief AdapterClass of JSMailClient for the SOSJobScheduler
 *
 * This Class JSMailClientJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JSMailClient.
 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20130109134235
 * \endverbatim
 */
public class JSSmtpMailClientBaseClass extends JobSchedulerJobAdapter {
	private final String	conClassName					= "JSMailClientJSAdapterClass";
	@SuppressWarnings("unused")
	private static Logger	logger							= Logger.getLogger(JSSmtpMailClientBaseClass.class);
	protected final boolean	continue_with_spooler_process	= true;
	protected final boolean	continue_with_task				= true;

	protected JSSmtpMailClient objR = null;
	protected JSSmtpMailOptions objO = null;

	protected void CreateOptions(final String pstrEntryPointName) throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcessing";

		initializeLog4jAppenderClass();

		objR = new JSSmtpMailClient();
		objO = objR.Options();

		objR.setJSJobUtilites(this);
		objR.setJSCommands(this);
		String strStepName = this.getCurrentNodeName();
//		if (pstrEntryPointName.length() > 0) {
//			strStepName = pstrEntryPointName + "@" + strStepName;
//			logger.debug("Options-Prefix is " + strStepName);
//		}
		objO.CurrentNodeName(strStepName);

		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		objO.CheckMandatory();
	} // doProcessing


	protected void doProcessing() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcessing";
		CreateOptions("");
		objR.Execute();
	} // doProcessing

	@Override
	public boolean spooler_init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_init"; //$NON-NLS-1$
		return super.spooler_init();
	}

	@Override
	public void spooler_exit() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_exit"; //$NON-NLS-1$
		super.spooler_exit();
	}

}
