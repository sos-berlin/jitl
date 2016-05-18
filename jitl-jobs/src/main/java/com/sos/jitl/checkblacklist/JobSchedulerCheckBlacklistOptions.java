package com.sos.jitl.checkblacklist;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerCheckBlacklistOptions", description = "Checks wether orders are in a blacklist")
public class JobSchedulerCheckBlacklistOptions extends JobSchedulerCheckBlacklistOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public JobSchedulerCheckBlacklistOptions() {
    }

    public JobSchedulerCheckBlacklistOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerCheckBlacklistOptions(HashMap<String, String> JSSettings) throws Exception {
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