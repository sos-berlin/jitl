package sos.scheduler.managed.db;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerManagedDBReportJobOptions", description = "Launch Database Report")
public class JobSchedulerManagedDBReportJobOptions extends JobSchedulerManagedDBReportJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public JobSchedulerManagedDBReportJobOptions() {
    }

    public JobSchedulerManagedDBReportJobOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerManagedDBReportJobOptions(HashMap<String, String> JSSettings) throws Exception {
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