package com.sos.jitl.housekeeping.dequeuemail;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerDequeueMailJobOptions", description = "Dequeues Mails")
public class JobSchedulerDequeueMailJobOptions extends JobSchedulerDequeueMailJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public JobSchedulerDequeueMailJobOptions() {
    }

    public JobSchedulerDequeueMailJobOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerDequeueMailJobOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    }

    @Override
    public void checkMandatory() {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

}