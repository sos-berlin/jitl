

package com.sos.jitl.textprocessor;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

 
@JSOptionClass(name = "JobSchedulerTextProcessorOptions", description = "Diverse Funktionen auf Textdateien")
public class JobSchedulerTextProcessorOptions extends JobSchedulerTextProcessorOptionsSuperClass {
	@SuppressWarnings("unused")
	private final String					conClassName						= "JobSchedulerTextProcessorOptions";
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JobSchedulerTextProcessorOptions.class);

    /**
    * constructors
    */

	public JobSchedulerTextProcessorOptions() {
	} // public JobSchedulerTextProcessorOptions

	public JobSchedulerTextProcessorOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerTextProcessorOptions

		//

	public JobSchedulerTextProcessorOptions (HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public JobSchedulerTextProcessorOptions (HashMap JSSettings)
/**
 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // JobSchedulerTextProcessorOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

