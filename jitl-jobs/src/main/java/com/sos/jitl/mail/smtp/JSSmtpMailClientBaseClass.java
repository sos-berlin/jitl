package com.sos.jitl.mail.smtp;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
 
public class JSSmtpMailClientBaseClass extends JobSchedulerJobAdapter {
	private final String		conClassName					= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion					= "$Id$";
	@SuppressWarnings("unused")
	private final Logger		logger							= Logger.getLogger(this.getClass());
	protected final boolean		continue_with_spooler_process	= true;
	protected final boolean		continue_with_task				= true;

	protected JSSmtpMailClient	objR							= null;
	protected JSSmtpMailOptions	objO							= null;

	protected void CreateOptions(final String pstrEntryPointName) throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CreateOptions";

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
		objO.CurrentNodeName(strStepName)
		.CurrentJobName(this.getJobName())
		.CurrentJobId(this.getJobId())
		.CurrentJobFolder(this.getJobFolder());

		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
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
