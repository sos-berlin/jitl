package com.sos.jitl.jobchainnodeparameter.monitor;

import java.util.HashMap;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "ConfigurationMonitorOptions", description = "Configuration Monitor")
public class JobchainNodeSubstituteOptions extends JobchainNodeSubstituteOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public JobchainNodeSubstituteOptions() {
        //
    }

    public JobchainNodeSubstituteOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobchainNodeSubstituteOptions(HashMap<String, String> jsSettings) throws Exception {
        super(jsSettings);
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