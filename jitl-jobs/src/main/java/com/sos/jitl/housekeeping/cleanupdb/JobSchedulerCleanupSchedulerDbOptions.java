package com.sos.jitl.housekeeping.cleanupdb;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

/** \class JobSchedulerCleanupSchedulerDbOptions - Delete log entries in the Job
 * Scheduler history Databaser tables
 *
 * \brief An Options as a container for the Options super class. The Option
 * class will hold all the things, which would be otherwise overwritten at a
 * re-creation of the super-class.
 *
 *
 * 
 *
 * see \see C:\Dokumente und Einstellungen\Uwe Risse\Lokale
 * Einstellungen\Temp\scheduler_editor-7803311730891015050.html for (more)
 * details.
 * 
 * \verbatim ; mechanicaly created by JobDocu2OptionsClass.xslt from
 * http://www.sos-berlin.com at 20121211162230 \endverbatim */
@JSOptionClass(name = "JobSchedulerCleanupSchedulerDbOptions", description = "Delete log entries in the Job Scheduler history Databaser tables")
public class JobSchedulerCleanupSchedulerDbOptions extends JobSchedulerCleanupSchedulerDbOptionsSuperClass {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")//$NON-NLS-1$
    private final String conClassName = "JobSchedulerCleanupSchedulerDbOptions";  //$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerCleanupSchedulerDbOptions.class);

    /** constructors */

    public JobSchedulerCleanupSchedulerDbOptions() {
    } // public JobSchedulerCleanupSchedulerDbOptions

    public JobSchedulerCleanupSchedulerDbOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerCleanupSchedulerDbOptions

    //

    public JobSchedulerCleanupSchedulerDbOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public JobSchedulerCleanupSchedulerDbOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // JobSchedulerCleanupSchedulerDbOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
