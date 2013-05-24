package com.sos.jitl.mail.smtp;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.jitl.mail.smtp.JSSmtpMailOptions.enuMailClasses;

// Super-Class for JobScheduler Java-API-Jobs
/**
 * \class 		MailMonitorTaskAfter - JobScheduler Adapter for "Submit and Delete Events"
 *
 * \brief AdapterClass of MailMonitorTaskAfter for the SOSJobScheduler
 *
 * This Class MailMonitorTaskAfter works as an adapter-class between the SOS
 * JobScheduler and the worker-class JSMailClient.
 *
 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20130109134235
 * \endverbatim
 */
public class SmtpMailMonitor extends JSSmtpMailClientBaseClass {
	private final String	conClassName	= "MailMonitorTaskAfter";
	private static Logger	logger			= Logger.getLogger(SmtpMailMonitor.class);

	@Override
	public void spooler_task_after() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_task_after";

		try {
			super.spooler_process();
			CreateOptions("task_after");
			if (isOrderJob() == false) {
				if (spooler_task.exit_code() != 0) {
					if (objO.MailOnError() == true) {
						if (objO.MailOnError() == true) {
							objR.Execute(objO.getOptions(enuMailClasses.MailOnError));
						}
					}
				}
				else {
					if (objO.MailOnSuccess() == true) {
						if (objO.MailOnSuccess() == true) {
							objR.Execute(objO.getOptions(enuMailClasses.MailOnSuccess));
						}
					}
				}
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(e.getLocalizedMessage(), e);
		}
		finally {
		} // finally
	} // spooler_task_after

	@Override
	public boolean spooler_task_before() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_task_before";

		try {
			super.spooler_process();
			CreateOptions("task_before");
			if (isOrderJob() == false) {
				if (objO.MailOnJobStart() == true) {
					objR.Execute(objO.getOptions(enuMailClasses.MailOnJobStart));
				}
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(e.getLocalizedMessage(), e);
		}
		finally {
		} // finally
		return continue_with_task; // Task can start
	} // spooler_process

	@Override
	public boolean spooler_process_after(final boolean spooler_process_return_code) throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_process_after";

		try {
			super.spooler_process();
			CreateOptions("process_after");
			if (isOrderJob() == true) {
				/* weil der Task noch nicht beendet ist, ist der exit_code hier nicht auswertbar
				 *
				 */
				//				if (spooler_task.exit_code() != 0) {
				if (spooler_process_return_code == false) {
					if (objO.MailOnError() == true) {
						if (objO.MailOnError() == true) {
							objR.Execute(objO.getOptions(enuMailClasses.MailOnError));
						}
					}
				}
				else {
					if (objO.MailOnSuccess() == true) {
						if (objO.MailOnSuccess() == true) {
							objR.Execute(objO.getOptions(enuMailClasses.MailOnSuccess));
						}
					}
				}
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(e.getLocalizedMessage(), e);
		}
		finally {
		} // finally
		return spooler_process_return_code;
	} // spooler_process

	@Override
	public boolean spooler_process_before() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_process_before";

		try {
			super.spooler_process();
			CreateOptions("process_before");
			if (isOrderJob() == true) {
				if (objO.MailOnJobStart() == true) {
					objR.Execute(objO.getOptions(enuMailClasses.MailOnJobStart));
				}
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(e.getLocalizedMessage(), e);
		}
		finally {
		} // finally
		return continue_with_spooler_process;

	} // spooler_process_before
}
