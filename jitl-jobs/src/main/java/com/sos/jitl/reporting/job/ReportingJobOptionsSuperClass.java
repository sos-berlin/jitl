package com.sos.jitl.reporting.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "ReportingJobOptionsSuperClass", description = "ReportingJobOptionsSuperClass")
public class ReportingJobOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = ReportingJobOptionsSuperClass.class.getSimpleName();

    @JSOptionDefinition(name = "hibernate_configuration_file", description = "", key = "hibernate_configuration_file", type = "SOSOptionString",
            mandatory = true)
    public SOSOptionString hibernate_configuration_file = new SOSOptionString(this, conClassName + ".hibernate_configuration_file", "",
            "config/hibernate.cfg.xml", "config/hibernate.cfg.xml", true);

    public SOSOptionString gethibernate_configuration_file() {
        return hibernate_configuration_file;
    }

    public void sethibernate_configuration_file(SOSOptionString val) {
        this.hibernate_configuration_file = val;
    }

    @JSOptionDefinition(name = "connection_transaction_isolation", description = "", key = "connection_transaction_isolation",
            type = "SOSOptionInterval", mandatory = false)
    public SOSOptionInteger connection_transaction_isolation = new SOSOptionInteger(this, conClassName + ".connection_transaction_isolation", "",
            "2", "2", false);

    public SOSOptionInteger getconnection_transaction_isolation() {
        return connection_transaction_isolation;
    }

    public void setconnection_transaction_isolation(SOSOptionInteger p_connection_transaction_isolation) {
        this.connection_transaction_isolation = p_connection_transaction_isolation;
    }

    @JSOptionDefinition(name = "connection_autocommit", description = "", key = "connection_autocommit", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean connection_autocommit = new SOSOptionBoolean(this, conClassName + ".connection_autocommit", "", "false", "false", false);

    public SOSOptionBoolean getconnection_autocommit() {
        return connection_autocommit;
    }

    public void setconnection_autocommit(SOSOptionBoolean val) {
        this.connection_autocommit = val;
    }

    @JSOptionDefinition(name = "large_result_fetch_size", description = "", key = "large_result_fetch_size", type = "SOSOptionInteger",
            mandatory = false)
    public SOSOptionInteger large_result_fetch_size = new SOSOptionInteger(this, conClassName + ".large_result_fetch_size", "", "-1", "-1", false);

    public SOSOptionInteger getlarge_result_fetch_size() {
        return large_result_fetch_size;
    }

    public void setlarge_result_fetch_size(SOSOptionInteger val) {
        this.large_result_fetch_size = val;
    }

    public ReportingJobOptionsSuperClass() {
        this.objParentClass = this.getClass();
    }

    public ReportingJobOptionsSuperClass(JSListener listener) {
        this();
        this.registerMessageListener(listener);
    }

    public ReportingJobOptionsSuperClass(HashMap<String, String> settings) throws Exception {
        this();
        this.setAllOptions(settings);
    }

    public void setAllOptions(HashMap<String, String> settings) {
        objSettings = settings;
        super.setAllOptions(settings);
    }

    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void CommandLineArgs(String[] args) {
        super.CommandLineArgs(args);
        this.setAllOptions(super.objSettings);
    }

}