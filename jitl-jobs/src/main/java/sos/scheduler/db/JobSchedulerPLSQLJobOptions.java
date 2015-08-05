

package sos.scheduler.db;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerPLSQLJobOptions", description = "Launch Database Statement")
public class JobSchedulerPLSQLJobOptions extends JobSchedulerPLSQLJobOptionsSuperClass {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -2492091654517629849L;
	@SuppressWarnings("unused")  
	private final String					conClassName						= "JobSchedulerPLSQLJobOptions";  //$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JobSchedulerPLSQLJobOptions.class);

    /**
    * constructors
    */
    
	public JobSchedulerPLSQLJobOptions() {
	} // public JobSchedulerPLSQLJobOptions

	@Deprecated
	public JobSchedulerPLSQLJobOptions(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerPLSQLJobOptions

		//

	public JobSchedulerPLSQLJobOptions (final HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
		super.setChildClasses(JSSettings, EMPTY_STRING);
	} // public JobSchedulerPLSQLJobOptions (HashMap JSSettings)
/**
 * \brief CheckMandatory - pr�ft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgel�st, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // JobSchedulerPLSQLJobOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

