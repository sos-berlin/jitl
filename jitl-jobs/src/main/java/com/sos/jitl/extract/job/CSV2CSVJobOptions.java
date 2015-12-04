package com.sos.jitl.extract.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "CSV2CSVJobOptions", description = "CSV2CSV")
public class CSV2CSVJobOptions extends CSV2CSVJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public CSV2CSVJobOptions() {
    }

    public CSV2CSVJobOptions(JSListener listener) {
        this();
        this.registerMessageListener(listener);
    }

    public CSV2CSVJobOptions(HashMap<String, String> settings) throws Exception {
        super(settings);
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
