package com.sos.jitl.textprocessor;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "JobSchedulerTextProcessorOptionsSuperClass", description = "JobSchedulerTextProcessorOptionsSuperClass")
public class JobSchedulerTextProcessorOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = "JobSchedulerTextProcessorOptionsSuperClass";

    @JSOptionDefinition(name = "command", description = "", key = "command", type = "SOSOptionString", mandatory = true)
    public SOSOptionString command = new SOSOptionString(this, conClassName + ".command", "", " ", " ", true);

    public SOSOptionString getcommand() {
        return command;
    }

    public void setcommand(SOSOptionString p_command) {
        this.command = p_command;
    }

    @JSOptionDefinition(name = "filename", description = "", key = "filename", type = "SOSOptionString", mandatory = true)
    public SOSOptionString filename = new SOSOptionString(this, conClassName + ".filename", "", " ", " ", true);

    public SOSOptionString getfilename() {
        return filename;
    }

    public void setfilename(SOSOptionString p_filename) {
        this.filename = p_filename;
    }

    @JSOptionDefinition(name = "param", description = "", key = "param", type = "SOSOptionString", mandatory = false)
    public SOSOptionString param = new SOSOptionString(this, conClassName + ".param", "", " ", " ", false);

    public SOSOptionString getparam() {
        return param;
    }

    public void setparam(SOSOptionString p_param) {
        this.param = p_param;
    }

    @JSOptionDefinition(name = "result", description = "", key = "result", type = "SOSOptionString", mandatory = false)
    public SOSOptionString result = new SOSOptionString(this, conClassName + ".result", "", " ", " ", false);

    public SOSOptionString getresult() {
        return result;
    }

    public void setresult(SOSOptionString p_result) {
        this.result = p_result;
    }

    @JSOptionDefinition(name = "scheduler_textprocessor_result", description = "", key = "scheduler_textprocessor_result", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString scheduler_textprocessor_result = new SOSOptionString(this, conClassName + ".scheduler_textprocessor_result", "", " ", " ",
            false);

    public SOSOptionString getscheduler_textprocessor_result() {
        return scheduler_textprocessor_result;
    }

    public void setscheduler_textprocessor_result(SOSOptionString p_scheduler_textprocessor_result) {
        this.scheduler_textprocessor_result = p_scheduler_textprocessor_result;
    }

    public JobSchedulerTextProcessorOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerTextProcessorOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerTextProcessorOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.setSettings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    }

    @Override
    public void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void commandLineArgs(String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

}