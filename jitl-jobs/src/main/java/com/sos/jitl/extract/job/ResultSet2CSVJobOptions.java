package com.sos.jitl.extract.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "ResultSet2CSVJobOptions", description = "ResultSet2CSV")
public class ResultSet2CSVJobOptions extends ResultSet2CSVJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public ResultSet2CSVJobOptions() {
    }

    public ResultSet2CSVJobOptions(JSListener listener) {
        this();
        this.registerMessageListener(listener);
    }

    public ResultSet2CSVJobOptions(HashMap<String, String> settings) throws Exception {
        super(settings);
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
