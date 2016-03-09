package com.sos.jitl.httppost;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

@JSOptionClass(name = "JobSchedulerHttpPostJobOptions", description = "Post files via HTTP")
public class JobSchedulerHttpPostJobOptions extends JobSchedulerHttpPostJobOptionsSuperClass {

    @SuppressWarnings("unused")
    private final String conClassName = "JobSchedulerHttpPostJobOptions";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerHttpPostJobOptions.class);

    /** constructors */

    public JobSchedulerHttpPostJobOptions() {
    } // public JobSchedulerHttpPostJobOptions

    public JobSchedulerHttpPostJobOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerHttpPostJobOptions

    //

    public JobSchedulerHttpPostJobOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public JobSchedulerHttpPostJobOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // JobSchedulerHttpPostJobOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
