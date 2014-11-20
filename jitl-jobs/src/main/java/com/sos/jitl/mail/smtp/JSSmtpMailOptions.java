package com.sos.jitl.mail.smtp;

import java.util.HashMap;

import org.apache.log4j.Logger;

import sos.net.mail.options.SOSSmtpMailOptions;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSJobId;
import com.sos.JSHelper.Options.JSJobName;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionPortNumber;

/**
 * \class 		JSSmtpMailOptions - SMTP Mail Options
 *
 * \brief
 * An Options as a container for the Options super class.
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerSmtpMail.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20111124184709
 * \endverbatim
 */
@JSOptionClass(name = "JSSmtpMailOptions", description = "Launch and observe any given job or job chain")
public class JSSmtpMailOptions extends SOSSmtpMailOptions {

	private static final long	serialVersionUID		= 6441074884525254517L;
	private final String		conClassName			= "JSSmtpMailOptions";							//$NON-NLS-1$
	private static Logger		logger					= Logger.getLogger(JSSmtpMailOptions.class);
	@SuppressWarnings("unused")
	private static final String	conSVNVersion			= "$Id$";

	// TODO über Prefix OnError_, OnSuccess_, OnJobStart_ adressieren

	public JSSmtpMailOptions	objMailOnError			= null;
	public JSSmtpMailOptions	objMailOnSuccess		= null;
	public JSSmtpMailOptions	objMailOnJobStart		= null;

	private String				strAlternativePrefix	= "";

	public enum enuMailClasses {
		MailDefault, MailOnError, MailOnSuccess, MailOnJobStart;
	}

	/**
	* constructors
	*/

	public JSSmtpMailOptions() {
	} // public JSSmtpMailOptions

	public JSSmtpMailOptions(final JSListener pobjListener) {
		this();
	} // public JSSmtpMailOptions

	public JSSmtpMailOptions getOptions(final enuMailClasses penuMailClass) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getOptions";

		JSSmtpMailOptions objO = objMailOnError;
		switch (penuMailClass) {
			case MailOnError:
				break;

			case MailOnJobStart:
				objO = objMailOnJobStart;
				break;

			case MailOnSuccess:
				objO = objMailOnSuccess;
				break;

			default:
				objO = this;
				break;
		}

		return objO;
	} // private JSSmtpMailOptions getOptions

	public JSSmtpMailOptions(final HashMap<String, String> JSSettings) throws Exception {
		super(JSSettings);
		objMailOnError = new JSSmtpMailOptions(JSSettings, "MailOnError_");
		objMailOnSuccess = new JSSmtpMailOptions(JSSettings, "MailOnSuccess_");
		// ....
		objMailOnJobStart = new JSSmtpMailOptions(JSSettings, "MailOnJobStart_");

	} // public JSSmtpMailOptions (HashMap JSSettings)

	public boolean MailOnJobStart() {
		boolean flgR = false;
		if (objMailOnJobStart == null) {
			objMailOnJobStart = new JSSmtpMailOptions(Settings(), "MailOnJobStart_");
			mergeDefaultSettings(objMailOnJobStart);
		}
		flgR = objMailOnJobStart.to.isDirty();
		return flgR;
	}

	public boolean MailOnError() {
		boolean flgR = false;
		if (objMailOnError == null) {
			objMailOnError = new JSSmtpMailOptions(Settings(), "MailOnError_");
			mergeDefaultSettings(objMailOnError);
		}

		flgR = objMailOnError.to.isDirty();
		return flgR;
	}

	public boolean MailOnSuccess() {
		boolean flgR = false;
		if (objMailOnSuccess == null) {
			objMailOnSuccess = new JSSmtpMailOptions(Settings(), "MailOnSuccess_");
			mergeDefaultSettings(objMailOnSuccess);
		}
		flgR = objMailOnSuccess.to.isDirty();
		return flgR;
	}

	private void mergeDefaultSettings (final JSSmtpMailOptions pobjOpt) {
		// TODO mark Options as default and assign by reflection (extend baseclass) 
		if (pobjOpt.host.isNotDirty()) {
			pobjOpt.host.Value(host.Value());
		}
		if (pobjOpt.port.isNotDirty()) {
			pobjOpt.port.Value(port.Value());
		}
		if (pobjOpt.smtp_user.isNotDirty()) {
			pobjOpt.smtp_user.Value(smtp_user.Value());
		}
		if (pobjOpt.smtp_password.isNotDirty()) {
			pobjOpt.smtp_password.Value(smtp_password.Value());
		}
	}
//	@Override
//	public void setAllOptions(final HashMap<String, String> JSSettings) throws Exception {
//		@SuppressWarnings("unused")
//		final String conMethodName = conClassName + "::setAllOptions";
//		setAllCommonOptions(JSSettings);
//		super.setAllOptions(JSSettings);
//		objMailOnError = new JSSmtpMailOptions(JSSettings, "MailOnError_");
//		objMailOnSuccess = new JSSmtpMailOptions(JSSettings, "MailOnSuccess_");
//		// ....
//		objMailOnJobStart = new JSSmtpMailOptions(JSSettings, "MailOnJobStart_");
//
//	} // public void setAllOptions}

	public JSSmtpMailOptions(final HashMap<String, String> JSSettings, final String pstrPrefix)  {
		strAlternativePrefix = pstrPrefix;
		try {
			super.setAllOptions(JSSettings, strAlternativePrefix);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.trace(this.dirtyString());
	} // public JSSmtpMailOptions (HashMap JSSettings)

	/**
	 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
	 *
	 * \details
	 * @throws Exception
	 *
	 * @throws Exception
	 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
	 */
	@Override
	// JSSmtpMailOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
	
	
	/**
	 * \var tasklog_to_body : add task log to body
	 *
	 */
	@JSOptionDefinition(name = "tasklog_to_body", description = "add task log to body", key = "tasklog_to_body", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	tasklog_to_body	= new SOSOptionBoolean(this, conClassName + ".tasklog_to_body", // HashMap-Key
											"add task log to body", //title
											"false", // InitValue
											"false", // DefaultValue
											false // isMandatory
									);
	
	/**
	 * \brief gettasklog_to_body
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	public SOSOptionBoolean gettasklog_to_body() {
		return tasklog_to_body;
	}

	/**
	 * \brief settasklog_to_body
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_to
	 */
	public void settasklog_to_body(final SOSOptionBoolean p_tasklog_to_body) {
		tasklog_to_body = p_tasklog_to_body;
	}

	
	/**
	 * \var attachment : job name
	 * Name and path of a job (path starts at live folder)
	 * Only used if tasklog_to_body = true
	 * If it is unset then current job is used
	 *
	 */
	@JSOptionDefinition(name = "job_name", description = "job name", key = "job_name", type = "JSJobName", mandatory = false)
	public JSJobName	job_name		= new JSJobName(this, conClassName + ".job_name", // HashMap-Key
													"job name", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getjob_name
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	public JSJobName getjob_name() {
		return job_name;
	}

	/**
	 * \brief setjob_path
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_job_path
	 */
	public void setjob_name(final JSJobName p_job_name) {
		job_name = p_job_name;
	}
	
	/**
	 * \var job_id : task id of a job
	 * Only used if tasklog_to_body = true
	 * If it is unset then current task id is used
	 *
	 */
	@JSOptionDefinition(name = "job_id", description = "task id of a job", key = "job_id", type = "JSJobId", mandatory = false)
	public JSJobId	job_id		= new JSJobId(this, conClassName + ".job_id", // HashMap-Key
													"task id of a job", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	public JSJobId task_id = (JSJobId) job_id.SetAlias("task_id");
	
	/**
	 * \brief getjob_id
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	public JSJobId getjob_id() {
		return job_id;
	}

	/**
	 * \brief setjob_id
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_job_id
	 */
	public void setjob_id(final JSJobId p_job_id) {
		job_id = p_job_id;
	}
	
	
	/**
	 * \var scheduler_host : jobscheduler hostname
	 * Only used if tasklog_to_body = true
	 * If it is unset then current jobscheduler is used
	 *
	 */
	@JSOptionDefinition(name = "scheduler_host", description = "jobscheduler hostname", key = "scheduler_host", type = "SOSOptionHostName", mandatory = false)
	public SOSOptionHostName	scheduler_host		= new SOSOptionHostName(this, conClassName + ".scheduler_host", // HashMap-Key
													"jobscheduler hostname", // Titel
													"localhost", // InitValue
													"localhost", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getscheduler_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	public SOSOptionHostName getscheduler_host() {
		return scheduler_host;
	}

	/**
	 * \brief setscheduler_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_scheduler_host
	 */
	public void setscheduler_host(final SOSOptionHostName p_scheduler_host) {
		scheduler_host = p_scheduler_host;
	}
	
	
	/**
	 * \var scheduler_port : jobscheduler port
	 * Only used if tasklog_to_body = true
	 * If it is unset then current jobscheduler is used
	 *
	 */
	@JSOptionDefinition(name = "scheduler_port", description = "jobscheduler port", key = "scheduler_port", type = "SOSOptionPortNumber", mandatory = false)
	public SOSOptionPortNumber	scheduler_port		= new SOSOptionPortNumber(this, conClassName + ".scheduler_port", // HashMap-Key
													"jobscheduler port", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getscheduler_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	public SOSOptionPortNumber getscheduler_port() {
		return scheduler_port;
	}

	/**
	 * \brief setscheduler_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_scheduler_port
	 */
	public void setscheduler_port(final SOSOptionPortNumber p_scheduler_port) {
		scheduler_port = p_scheduler_port;
	}
	

	
	
}
