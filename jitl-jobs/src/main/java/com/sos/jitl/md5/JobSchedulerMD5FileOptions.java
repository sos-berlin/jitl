package com.sos.jitl.md5;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

@JSOptionClass(name = "JobSchedulerMD5FileOptions", description = "title")
public class JobSchedulerMD5FileOptions extends JobSchedulerMD5FileOptionsSuperClass {

    @SuppressWarnings("unused")
    private final String conClassName = "JobSchedulerMD5FileOptions";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerMD5FileOptions.class);

    /** constructors */

    public JobSchedulerMD5FileOptions() {
    } // public JobSchedulerMD5FileOptions

    public JobSchedulerMD5FileOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerMD5FileOptions

    //

    public JobSchedulerMD5FileOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public JobSchedulerMD5FileOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // JobSchedulerMD5FileOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
