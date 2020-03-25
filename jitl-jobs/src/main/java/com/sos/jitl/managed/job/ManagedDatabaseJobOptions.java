package com.sos.jitl.managed.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "ManagedDatabaseJobOptions", description = "ManagedDatabaseJobOptions")
public class ManagedDatabaseJobOptions extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = ManagedDatabaseJobOptions.class.getSimpleName();

    @JSOptionDefinition(name = "hibernate_configuration_file", description = "", key = "hibernate_configuration_file", type = "SOSOptionString", mandatory = true)
    public SOSOptionString hibernate_configuration_file = new SOSOptionString(this, conClassName + ".hibernate_configuration_file", // HashMap-Key
            "", // Titel
            "", // InitValue
            "", // DefaultValue
            true // isMandatory
    );

    public SOSOptionString gethibernate_configuration_file() {
        return hibernate_configuration_file;
    }

    public void sethibernate_configuration_file(SOSOptionString val) {
        hibernate_configuration_file = val;
    }

    @JSOptionDefinition(name = "command", description = "", key = "command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString command = new SOSOptionString(this, conClassName + ".command", // HashMap-Key
            "", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );

    public SOSOptionString getcommand() {
        return command;
    }

    public void setcommand(SOSOptionString val) {
        command = val;
    }

    @JSOptionDefinition(name = "exec_returns_resultset", description = "", key = "exec_returns_resultset", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean exec_returns_resultset = new SOSOptionBoolean(this, conClassName + ".exec_returns_resultset", // HashMap-Key
            "", // Titel
            "false", // InitValue
            "false", //
            false // isMandatory
    );

    public SOSOptionBoolean getexec_returns_resultset() {
        return exec_returns_resultset;
    }

    public void setexec_returns_resultset(SOSOptionBoolean val) {
        exec_returns_resultset = val;
    }

    @JSOptionDefinition(name = "resultset_as_parameters", description = "", key = "resultset_as_parameters", type = "SOSOptionString", mandatory = false)
    public SOSOptionString resultset_as_parameters = new SOSOptionString(this, conClassName + ".resultset_as_parameters", // HashMap-Key
            "", // Titel
            "false", // InitValue
            "false", //
            false // isMandatory
    );

    public SOSOptionString getresultset_as_parameters() {
        return resultset_as_parameters;
    }

    public void setresultset_as_parameters(SOSOptionString val) {
        resultset_as_parameters = val;
    }

    @JSOptionDefinition(name = "resultset_as_warning", description = "", key = "resultset_as_warning", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean resultset_as_warning = new SOSOptionBoolean(this, conClassName + ".resultset_as_warning", // HashMap-Key
            "", // Titel
            "false", // InitValue
            "false", //
            false // isMandatory
    );

    public SOSOptionBoolean getresultset_as_warning() {
        return resultset_as_warning;
    }

    public void setresultset_as_warning(SOSOptionBoolean val) {
        resultset_as_warning = val;
    }

    public ManagedDatabaseJobOptions() {
        objParentClass = getClass();
    }

    public ManagedDatabaseJobOptions(JSListener listener) {
        this();
        registerMessageListener(listener);
    }

    public ManagedDatabaseJobOptions(HashMap<String, String> settings) throws Exception {
        this();
        setAllOptions(settings);
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
    public void commandLineArgs(String[] args) {
        super.commandLineArgs(args);
        setAllOptions(super.getSettings());
    }
}