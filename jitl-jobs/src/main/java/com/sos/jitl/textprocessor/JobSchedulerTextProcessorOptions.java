package com.sos.jitl.textprocessor;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerTextProcessorOptions", description = "Diverse Funktionen auf Textdateien")
public class JobSchedulerTextProcessorOptions extends JobSchedulerTextProcessorOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public JobSchedulerTextProcessorOptions() {
    }

    public JobSchedulerTextProcessorOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerTextProcessorOptions(HashMap<String, String> JSSettings) throws Exception {
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