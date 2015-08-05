package com.sos.jitl.md5;

import java.util.HashMap;

import com.sos.JSHelper.Options.*;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Options.*;

@JSOptionClass(name = "JobSchedulerMD5FileOptionsSuperClass", description = "JobSchedulerMD5FileOptionsSuperClass")
public class JobSchedulerMD5FileOptionsSuperClass extends JSOptionsClass {
    private final String conClassName = "JobSchedulerMD5FileOptionsSuperClass";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerMD5FileOptionsSuperClass.class);

    @JSOptionDefinition(name = "file", description = "", key = "file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString file = new SOSOptionString(this, conClassName + ".file", // HashMap-Key
            "", // Titel
            " ", // InitValue
            " ", // DefaultValue
            false // isMandatory
    );

    public SOSOptionString getfile() {
        return file;
    }

    public void setfile(SOSOptionString p_file) {
        this.file = p_file;
    }

    @JSOptionDefinition(name = "md5_suffix", description = "", key = "md5_suffix", type = "SOSOptionString", mandatory = false)
    public SOSOptionString md5_suffix = new SOSOptionString(this, conClassName + ".md5_suffix", // HashMap-Key
            "", // Titel
            ".md5", // InitValue
            ".md5", // DefaultValue
            false // isMandatory
    );

    public SOSOptionString getmd5_suffix() {
        return md5_suffix;
    }

    public void setmd5_suffix(SOSOptionString p_md5_suffix) {
        this.md5_suffix = p_md5_suffix;
    }

    @JSOptionDefinition(name = "mode", description = "", key = "mode", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mode = new SOSOptionString(this, conClassName + ".mode", // HashMap-Key
            "", // Titel
            " ", // InitValue
            " ", // DefaultValue
            false // isMandatory
    );

    public SOSOptionString getmode() {
        return mode;
    }

    public void setmode(SOSOptionString p_mode) {
        this.mode = p_mode;
    }

    @JSOptionDefinition(name = "result", description = "", key = "result", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean result = new SOSOptionBoolean(this, conClassName + ".result", // HashMap-Key
            "", // Titel
            "true", // InitValue
            "true", // DefaultValue
            false // isMandatory
    );

    public SOSOptionBoolean getresult() {
        return result;
    }

    public void setresult(SOSOptionBoolean p_result) {
        this.result = p_result;
    }

    public JobSchedulerMD5FileOptionsSuperClass() {
        objParentClass = this.getClass();
    } // public JobSchedulerMD5FileOptionsSuperClass

    public JobSchedulerMD5FileOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerMD5FileOptionsSuperClass

    //

    public JobSchedulerMD5FileOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    } // public JobSchedulerMD5FileOptionsSuperClass (HashMap JSSettings)

    private String getAllOptionsAsString() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getAllOptionsAsString";
        String strT = conClassName + "\n";
        final StringBuffer strBuffer = new StringBuffer();

        strT += this.toString(); // fix

        return strT;
    } // private String getAllOptionsAsString ()

    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setAllOptions";
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    } // public void setAllOptions (HashMap <String, String> JSSettings)

    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
            , Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()

    @Override
    public void CommandLineArgs(String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }
} // public class JobSchedulerMD5FileOptionsSuperClass