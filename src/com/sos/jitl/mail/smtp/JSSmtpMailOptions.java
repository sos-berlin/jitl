package com.sos.jitl.mail.smtp;

import java.util.HashMap;

import org.apache.log4j.Logger;

import sos.net.mail.options.SOSSmtpMailOptions;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

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
public class JSSmtpMailOptions extends SOSSmtpMailOptions  {

	private static final long	serialVersionUID		= 6441074884525254517L;
	@SuppressWarnings("unused")
	private final String		conClassName			= "JSSmtpMailOptions";						//$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger		logger					= Logger.getLogger(JSSmtpMailOptions.class);
	@SuppressWarnings("unused")
	private static final String	conSVNVersion			= "$Id$";

	// TODO über Prefix OnError_, OnSuccess_, OnJobStart_ adressieren

	public SOSSmtpMailOptions	objMailOnError			= null;
	public SOSSmtpMailOptions	objMailOnSuccess		= null;
	public SOSSmtpMailOptions	objMailOnJobStart		= null;

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
		this.registerMessageListener(pobjListener);
	} // public JSSmtpMailOptions

	public SOSSmtpMailOptions getOptions(final enuMailClasses penuMailClass) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getOptions";

		SOSSmtpMailOptions objO = objMailOnError;
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

	public boolean MailOnJobStart () {
		boolean flgR = false;
		flgR = objMailOnJobStart.to.isDirty();
		return flgR;
	}
	public boolean MailOnError () {
		boolean flgR = false;
		flgR = objMailOnError.to.isDirty();
		return flgR;
	}
	public boolean MailOnSuccess () {
		boolean flgR = false;
		flgR = objMailOnSuccess.to.isDirty();
		return flgR;
	}

	public JSSmtpMailOptions(final HashMap<String, String> JSSettings, final String pstrPrefix) throws Exception {
		strAlternativePrefix = pstrPrefix;
		setAllOptions(JSSettings, strAlternativePrefix);
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
}
