package sos.scheduler.db;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "SOSSQLPlusJobOptions", description = "Start SQL*Plus client and execute sql*plus programs")
public class SOSSQLPlusJobOptions extends SOSSQLPlusJobOptionsSuperClass {

    private static final long serialVersionUID = 7612674598767191212L;

    public SOSSQLPlusJobOptions() {
    }

    @Deprecated
    public SOSSQLPlusJobOptions(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public SOSSQLPlusJobOptions(final HashMap<String, String> JSSettings) throws Exception {
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

    public String getConnectionString() {
        String strT = "";
        if (db_user.isDirty()) {
            strT = db_user.Value() + "/" + db_password.Value() + "@" + db_url.Value();
        }
        return strT;
    }

}