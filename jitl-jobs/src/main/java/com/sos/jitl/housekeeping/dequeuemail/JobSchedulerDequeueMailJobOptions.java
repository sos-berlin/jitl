package com.sos.jitl.housekeeping.dequeuemail;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

@JSOptionClass(name = "JobSchedulerDequeueMailJobOptions", description = "Dequeues Mails")
public class JobSchedulerDequeueMailJobOptions extends JobSchedulerDequeueMailJobOptionsSuperClass {

    @SuppressWarnings("unused")
    private final String conClassName = "JobSchedulerDequeueMailJobOptions";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerDequeueMailJobOptions.class);

    /** constructors */

    public JobSchedulerDequeueMailJobOptions() {
    } // public JobSchedulerDequeueMailJobOptions

    public JobSchedulerDequeueMailJobOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerDequeueMailJobOptions

    //

    public JobSchedulerDequeueMailJobOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public JobSchedulerDequeueMailJobOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // JobSchedulerDequeueMailJobOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
