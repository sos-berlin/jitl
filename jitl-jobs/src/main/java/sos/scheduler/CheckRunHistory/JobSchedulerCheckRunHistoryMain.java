package sos.scheduler.CheckRunHistory;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;
import org.apache.log4j.Logger;

import java.util.Locale;
 
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerCheckRunHistoryMain extends JSToolBox {
	private final static String						conClassName	= "JobSchedulerCheckRunHistoryMain";						//$NON-NLS-1$
	private static Logger							logger			= Logger.getLogger(JobSchedulerCheckRunHistoryMain.class);
	private static Messages Messages = null;

	protected JobSchedulerCheckRunHistoryOptions	objOptions		= null;

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
		Messages = new Messages("com_sos_scheduler_messages", Locale.getDefault());

		logger.info("JobSchedulerCheckRunHistory - Main"); //$NON-NLS-1$

		try {
			JobSchedulerCheckRunHistory objM = new JobSchedulerCheckRunHistory();
			JobSchedulerCheckRunHistoryOptions objO = objM.Options();

			objO.CommandLineArgs(pstrArgs);
			objM.Execute();
		}

		catch (Exception e) {
			System.err.println(conMethodName + ": " + "Error occured ..." + e.getMessage());
			e.printStackTrace(System.err);
			int intExitCode = 99;
			logger.error(Messages.getMsg("JSJ-E-105: %1$s - terminated with exit-code %2$d", conMethodName, intExitCode), e);
			System.exit(intExitCode);
		}

		logger.info(Messages.getMsg("JSJ-I-106: %1$s - ended without errors", conMethodName));
	}

} // class JobSchedulerCheckRunHistoryMain