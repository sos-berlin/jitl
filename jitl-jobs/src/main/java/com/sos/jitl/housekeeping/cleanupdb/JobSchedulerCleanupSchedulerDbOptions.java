package com.sos.jitl.housekeeping.cleanupdb;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerCleanupSchedulerDbOptions", description = "Delete log entries in the Job Scheduler history Databaser tables")
public class JobSchedulerCleanupSchedulerDbOptions extends JobSchedulerCleanupSchedulerDbOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public JobSchedulerCleanupSchedulerDbOptions() {
    }

    public JobSchedulerCleanupSchedulerDbOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerCleanupSchedulerDbOptions(HashMap<String, String> JSSettings) throws Exception {
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