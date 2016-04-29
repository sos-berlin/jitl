package com.sos.jitl.httppost;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerHttpPostJobOptions", description = "Post files via HTTP")
public class JobSchedulerHttpPostJobOptions extends JobSchedulerHttpPostJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public JobSchedulerHttpPostJobOptions() {
    }

    public JobSchedulerHttpPostJobOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerHttpPostJobOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    }

    @Override
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

}