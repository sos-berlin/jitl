

package com.sos.jitl.checkblacklist;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

 
@JSOptionClass(name = "JobSchedulerCheckBlacklistOptions", description = "Checks wether orders are in a blacklist")
public class JobSchedulerCheckBlacklistOptions extends JobSchedulerCheckBlacklistOptionsSuperClass {
	@SuppressWarnings("unused")
	private final String					conClassName						= "JobSchedulerCheckBlacklistOptions";
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JobSchedulerCheckBlacklistOptions.class);

    /**
    * constructors
    */

	public JobSchedulerCheckBlacklistOptions() {
	} // public JobSchedulerCheckBlacklistOptions

	public JobSchedulerCheckBlacklistOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerCheckBlacklistOptions

		//

	public JobSchedulerCheckBlacklistOptions (HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public JobSchedulerCheckBlacklistOptions (HashMap JSSettings)
/**
 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // JobSchedulerCheckBlacklistOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

