

package com.sos.jitl.sync;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener; 
import org.apache.log4j.Logger;

/**
 * \class 		JobSchedulerSynchronizeJobChainsOptions - Synchronize Job Chains
 *
 * \brief 
 * An Options as a container for the Options super class. 
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *

 *
 * 
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20121218120331 
 * \endverbatim
 */
@JSOptionClass(name = "JobSchedulerSynchronizeJobChainsOptions", description = "Synchronize Job Chains")
public class JobSchedulerSynchronizeJobChainsOptions extends JobSchedulerSynchronizeJobChainsOptionsSuperClass {
	@SuppressWarnings("unused")  //$NON-NLS-1$
	private final String					conClassName						= "JobSchedulerSynchronizeJobChainsOptions";  //$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JobSchedulerSynchronizeJobChainsOptions.class);

    /**
    * constructors
    */
    
	public JobSchedulerSynchronizeJobChainsOptions() {
	} // public JobSchedulerSynchronizeJobChainsOptions

	public JobSchedulerSynchronizeJobChainsOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerSynchronizeJobChainsOptions

		//

	public JobSchedulerSynchronizeJobChainsOptions (HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public JobSchedulerSynchronizeJobChainsOptions (HashMap JSSettings)
/**
 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // JobSchedulerSynchronizeJobChainsOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

