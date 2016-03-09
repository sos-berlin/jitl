package com.sos.jitl.sync;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

@JSOptionClass(name = "JobSchedulerSynchronizeJobChainsOptions", description = "Synchronize Job Chains")
public class JobSchedulerSynchronizeJobChainsOptions extends JobSchedulerSynchronizeJobChainsOptionsSuperClass {

    @SuppressWarnings("unused")//$NON-NLS-1$
    private final String conClassName = "JobSchedulerSynchronizeJobChainsOptions";  //$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerSynchronizeJobChainsOptions.class);

    /** constructors */

    public JobSchedulerSynchronizeJobChainsOptions() {
    } // public JobSchedulerSynchronizeJobChainsOptions

    public JobSchedulerSynchronizeJobChainsOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerSynchronizeJobChainsOptions

    //

    public JobSchedulerSynchronizeJobChainsOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public JobSchedulerSynchronizeJobChainsOptions (HashMap JSSettings)

    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
