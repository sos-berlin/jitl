package com.sos.jitl.md5;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "JobSchedulerMD5FileOptionsSuperClass", description = "JobSchedulerMD5FileOptionsSuperClass")
public class JobSchedulerMD5FileOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = "JobSchedulerMD5FileOptionsSuperClass";

    @JSOptionDefinition(name = "file", description = "", key = "file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString file = new SOSOptionString(this, conClassName + ".file", "", " ", " ", false);

    public SOSOptionString getfile() {
        return file;
    }

    public void setfile(SOSOptionString p_file) {
        this.file = p_file;
    }

    @JSOptionDefinition(name = "md5_suffix", description = "", key = "md5_suffix", type = "SOSOptionString", mandatory = false)
    public SOSOptionString md5_suffix = new SOSOptionString(this, conClassName + ".md5_suffix", "", ".md5", ".md5", false);

    public SOSOptionString getmd5_suffix() {
        return md5_suffix;
    }

    public void setmd5_suffix(SOSOptionString p_md5_suffix) {
        this.md5_suffix = p_md5_suffix;
    }

    @JSOptionDefinition(name = "mode", description = "", key = "mode", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mode = new SOSOptionString(this, conClassName + ".mode", "", " ", " ", false);

    public SOSOptionString getmode() {
        return mode;
    }

    public void setmode(SOSOptionString p_mode) {
        this.mode = p_mode;
    }

    @JSOptionDefinition(name = "result", description = "", key = "result", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean result = new SOSOptionBoolean(this, conClassName + ".result", "", "true", "true", false);

    public SOSOptionBoolean getresult() {
        return result;
    }

    public void setresult(SOSOptionBoolean p_result) {
        this.result = p_result;
    }

    public JobSchedulerMD5FileOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerMD5FileOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerMD5FileOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> settings) {
        super.setAllOptions(settings);
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
        this.setAllOptions(super.getSettings());
    }

}