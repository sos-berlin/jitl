package com.sos.jitl.httppost;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "JobSchedulerHttpPostJobOptionsSuperClass", description = "JobSchedulerHttpPostJobOptionsSuperClass")
public class JobSchedulerHttpPostJobOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = "JobSchedulerHttpPostJobOptionsSuperClass";

    @JSOptionDefinition(name = "content_type", description = "", key = "content_type", type = "SOSOptionString", mandatory = false)
    public SOSOptionString content_type = new SOSOptionString(this, conClassName + ".content_type", "", " ", " ", false);

    public SOSOptionString getcontent_type() {
        return content_type;
    }

    public void setcontent_type(SOSOptionString p_content_type) {
        this.content_type = p_content_type;
    }

    @JSOptionDefinition(name = "encoding", description = "", key = "encoding", type = "SOSOptionString", mandatory = false)
    public SOSOptionString encoding = new SOSOptionString(this, conClassName + ".encoding", "", " ", " ", false);

    public SOSOptionString getencoding() {
        return encoding;
    }

    public void setencoding(SOSOptionString p_encoding) {
        this.encoding = p_encoding;
    }

    @JSOptionDefinition(name = "input", description = "", key = "input", type = "SOSOptionString", mandatory = true)
    public SOSOptionString input = new SOSOptionString(this, conClassName + ".input", "", " ", " ", true);
    public SOSOptionString input_directory = (SOSOptionString) input.setAlias(conClassName + ".input");

    public SOSOptionString getinput() {
        return input;
    }

    public void setinput(SOSOptionString p_input) {
        this.input = p_input;
    }

    @JSOptionDefinition(name = "input_filespec", description = "", key = "input_filespec", type = "SOSOptionString", mandatory = false)
    public SOSOptionString input_filespec = new SOSOptionString(this, conClassName + ".input_filespec", "", "^(.*)$", "^(.*)$", false);

    public SOSOptionString getinput_filespec() {
        return input_filespec;
    }

    public void setinput_filespec(SOSOptionString p_input_filespec) {
        this.input_filespec = p_input_filespec;
    }

    @JSOptionDefinition(name = "output", description = "", key = "output", type = "SOSOptionString", mandatory = false)
    public SOSOptionString output = new SOSOptionString(this, conClassName + ".output", "", " ", " ", false);
    public SOSOptionString output_directory = (SOSOptionString) output.setAlias(conClassName + ".output");

    public SOSOptionString getoutput() {
        return output;
    }

    public void setoutput(SOSOptionString p_output) {
        this.output = p_output;
    }

    @JSOptionDefinition(name = "timeout", description = "", key = "timeout", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger timeout = new SOSOptionInteger(this, conClassName + ".timeout", "", "0", "0", false);

    public SOSOptionInteger gettimeout() {
        return timeout;
    }

    public void settimeout(SOSOptionInteger p_timeout) {
        this.timeout = p_timeout;
    }

    @JSOptionDefinition(name = "url", description = "", key = "url", type = "SOSOptionString", mandatory = true)
    public SOSOptionString url = new SOSOptionString(this, conClassName + ".url", "", " ", " ", true);

    public SOSOptionString geturl() {
        return url;
    }

    public void seturl(SOSOptionString p_url) {
        this.url = p_url;
    }

    public JobSchedulerHttpPostJobOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerHttpPostJobOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerHttpPostJobOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
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