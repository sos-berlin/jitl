package com.sos.jitl.md5;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerMD5FileOptions", description = "title")
public class JobSchedulerMD5FileOptions extends JobSchedulerMD5FileOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public JobSchedulerMD5FileOptions() {
    }

    public JobSchedulerMD5FileOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerMD5FileOptions(HashMap<String, String> JSSettings) throws Exception {
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