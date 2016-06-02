package com.sos.jitl.sync;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerSynchronizeJobChainsOptions", description = "Synchronize Job Chains")
public class JobSchedulerSynchronizeJobChainsOptions extends JobSchedulerSynchronizeJobChainsOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public JobSchedulerSynchronizeJobChainsOptions() {
    }

    public JobSchedulerSynchronizeJobChainsOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerSynchronizeJobChainsOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    }

    public void checkMandatory() {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

}