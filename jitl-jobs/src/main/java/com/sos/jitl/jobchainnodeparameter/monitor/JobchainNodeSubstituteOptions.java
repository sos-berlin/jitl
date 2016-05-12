package com.sos.jitl.jobchainnodeparameter.monitor;

import java.util.HashMap;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "ConfigurationMonitorOptions", description = "Configuration Monitor")
public class JobchainNodeSubstituteOptions extends JobchainNodeSubstituteOptionsSuperClass {

    public JobchainNodeSubstituteOptions() {
        //
    }

    public JobchainNodeSubstituteOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobchainNodeSubstituteOptions(HashMap<String, String> JSSettings) throws Exception {
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
