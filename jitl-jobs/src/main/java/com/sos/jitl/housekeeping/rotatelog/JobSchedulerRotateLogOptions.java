package com.sos.jitl.housekeeping.rotatelog;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerRotateLogOptions", description = "Rotate compress and delete log files")
public class JobSchedulerRotateLogOptions extends JobSchedulerRotateLogOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public JobSchedulerRotateLogOptions() {
    }

    public JobSchedulerRotateLogOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerRotateLogOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    }

    @Override
    public void checkMandatory() {
        try {
            super.checkMandatory();
            compress_file_age.adjust2TimeFormat();
            delete_file_age.adjust2TimeFormat();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

}