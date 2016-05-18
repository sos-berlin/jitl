package com.sos.jitl.eventing.checkevents;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

/** \class JobSchedulerCheckEventsOptions - Check if events exist
 *
 * \brief An Options as a container for the Options super class. The Option
 * class will hold all the things, which would be otherwise overwritten at a
 * re-creation of the super-class.
 *
 *
 * 
 *
 *
 * \verbatim ; mechanicaly created by JobDocu2OptionsClass.xslt from
 * http://www.sos-berlin.com at 20130527114318 \endverbatim */
@JSOptionClass(name = "JobSchedulerCheckEventsOptions", description = "Check if events exist")
public class JobSchedulerCheckEventsOptions extends JobSchedulerCheckEventsOptionsSuperClass {

    @SuppressWarnings("unused")
    private final String conClassName = "JobSchedulerCheckEventsOptions";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerCheckEventsOptions.class);

    /** constructors */

    public JobSchedulerCheckEventsOptions() {
    } // public JobSchedulerCheckEventsOptions

    public JobSchedulerCheckEventsOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerCheckEventsOptions

    //

    public JobSchedulerCheckEventsOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public JobSchedulerCheckEventsOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // JobSchedulerCheckEventsOptionsSuperClass
    public void checkMandatory() {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
