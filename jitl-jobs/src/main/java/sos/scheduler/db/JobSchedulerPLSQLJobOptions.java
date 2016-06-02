package sos.scheduler.db;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerPLSQLJobOptions", description = "Launch Database Statement")
public class JobSchedulerPLSQLJobOptions extends JobSchedulerPLSQLJobOptionsSuperClass {

    private static final long serialVersionUID = -2492091654517629849L;

    public JobSchedulerPLSQLJobOptions() {
    }

    @Deprecated
    public JobSchedulerPLSQLJobOptions(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerPLSQLJobOptions(final HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
        super.setChildClasses(JSSettings, EMPTY_STRING);
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