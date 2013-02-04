package com.sos.jitl.mail.smtp;

import org.apache.log4j.Logger;

import sos.net.SOSMail;
import sos.net.mail.options.SOSSmtpMailOptions;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;

/**
 * \class 		JSMailClient - Workerclass for "Submit and Delete Events"
 *
 * \brief AdapterClass of JSMailClient for the SOSJobScheduler
 *
 * This Class JSMailClient is the worker-class.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20130109134235
 * \endverbatim
 */
@SuppressWarnings("deprecation")
public class JSSmtpMailClient extends JSJobUtilitiesClass<JSSmtpMailOptions> {
	private final String					conClassName	= "JSMailClient";
	@SuppressWarnings("unused")
	private static Logger					logger			= Logger.getLogger(JSSmtpMailClient.class);
	@SuppressWarnings("unused")
	private final String					conSVNVersion	= "$Id: JSMailClient.java 18220 2012-10-18 07:46:10Z kb $";


	/**
	 *
	 * \brief JSMailClient
	 *
	 * \details
	 *
	 */
	public JSSmtpMailClient() {
		super();
		this.Options();
	}

	/**
	 *
	 * \brief Options - returns the JSMailClientOptionClass
	 *
	 * \details
	 * The JSMailClientOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *
	 * \return JSMailClientOptions
	 *
	 */
	@Override
	public JSSmtpMailOptions Options() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new JSSmtpMailOptions();
		}
		return objOptions;
	}

	/**
	 *
	 * \brief Execute - Start the Execution of JSMailClient
	 *
	 * \details
	 *
	 * For more details see
	 *
	 * \see JobSchedulerAdapterClass
	 * \see JSMailClientMain
	 *
	 * \return JSMailClient
	 *
	 * @return
	 */
	public JSSmtpMailClient Execute() throws Exception {

		SOSSmtpMailOptions objO = objOptions;
		Execute (objO);

		return this;
	}

	public JSSmtpMailClient Execute(final SOSSmtpMailOptions pobjOptions) throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Execute";

		if (pobjOptions != null && pobjOptions.FileNotificationTo.isDirty() == true) {
			try {
//				String strA = "";
//				if (objOptions.log_filename.isDirty() == true) {
//					String strF = objOptions.log_filename.getHtmlLogFileName();
//					if (strF.length() > 0) {
//						strA += strF;
//					}
//
//					strF = objOptions.log_filename.Value();
//					if (strF.length() > 0) {
//						if (strA.length() > 0) {
//							strA += ";";
//						}
//						strA += strF;
//					}
//					if (strA.length() > 0) {
//						objOptions.attachment.Value(strA);
//					}
//				}

				if (pobjOptions.subject.isDirty() == false) {
					String strT = "JobScheduler: ${JobName} - ${JobTitle} - CC ${CC} ";
					pobjOptions.subject.Value(strT);
				}

				String strM = pobjOptions.subject.Value();
				pobjOptions.subject.Value(pobjOptions.replaceVars(strM));

				strM = pobjOptions.body.Value();
				pobjOptions.body.Value(pobjOptions.replaceVars(strM));

				if (pobjOptions.from.isDirty() == false) {
					pobjOptions.from.Value("JobScheduler@sos-berlin.com");
				}

				SOSMail objMail = new SOSMail(pobjOptions.host.Value());
				logger.debug(pobjOptions.dirtyString());
				objMail.sendMail(pobjOptions);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return this;
	}

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init";
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

} // class JSMailClient