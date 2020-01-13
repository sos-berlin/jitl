package com.sos.jitl.notification.jobs.result;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

/**
 * 
 * @author Robert Ehrlich
 * 
 */
@JSOptionClass(name = "StoreResultsJobOptions", description = "NotificationMonitor")
public class StoreResultsJobOptions extends StoreResultsJobOptionsSuperClass {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private final String conClassName = StoreResultsJobOptions.class.getSimpleName();

	/**
	 * constructors
	 */

	public StoreResultsJobOptions() {
	} // public StoreResultsJobOptions

	public StoreResultsJobOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public StoreResultsJobOptions

	//

	public StoreResultsJobOptions(HashMap<String, String> JSSettings)
			throws Exception {
		super(JSSettings);
	} // public StoreResultsJobOptions (HashMap JSSettings)

	/**
	 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
	 * 
	 * \details
	 * 
	 * @throws Exception
	 * 
	 * @throws Exception
	 *             - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
	 */
	@Override
	// StoreResultsJobOptionsSuperClass
	public void checkMandatory() {
		try {
			super.checkMandatory();
		} catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}
