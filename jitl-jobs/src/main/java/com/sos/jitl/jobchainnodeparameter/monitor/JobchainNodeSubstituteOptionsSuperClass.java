package com.sos.jitl.jobchainnodeparameter.monitor;

import java.util.HashMap;

import com.sos.JSHelper.Options.*;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

@JSOptionClass(name = "ConfigurationMonitorOptionsSuperClass", description = "ConfigurationMonitorOptionsSuperClass")
public class JobchainNodeSubstituteOptionsSuperClass extends JSOptionsClass {
    private final String CLASSNAME = "ConfigurationMonitorOptionsSuperClass";
    @SuppressWarnings("unused")
    private final static Logger LOGGER = Logger.getLogger(JobchainNodeSubstituteOptionsSuperClass.class);

    @JSOptionDefinition(name = "configurationMonitor_configuration_file", description = "The default value is the name of the job chain of the actual running o", key = "configurationMonitor_configuration_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString configurationMonitor_configuration_file = new SOSOptionString(this, CLASSNAME + ".configurationMonitor_configuration_file", // HashMap-Key
            "The default value is the name of the job chain of the actual running o", // Titel
            " ", // InitValue
            " ", // DefaultValue
            false // isMandatory
    );

    public SOSOptionString getconfigurationMonitor_configuration_file() {
        return configurationMonitor_configuration_file;
    }

    public void setconfigurationMonitor_configuration_file(SOSOptionString p_configurationMonitor_configuration_file) {
        this.configurationMonitor_configuration_file = p_configurationMonitor_configuration_file;
    }

    @JSOptionDefinition(name = "configurationMonitor_configuration_path", description = "The default value is the directory that contains the job chain definit", key = "configurationMonitor_configuration_path", type = "SOSOptionString", mandatory = false)
    public SOSOptionString configurationMonitor_configuration_path = new SOSOptionString(this, CLASSNAME + ".configurationMonitor_configuration_path",
            "The default value is the directory that contains the job chain definit", " ", // InitValue
            " ", // DefaultValue
            false // isMandatory
    );

    public SOSOptionString getconfigurationMonitor_configuration_path() {
        return configurationMonitor_configuration_path;
    }

    public void setconfigurationMonitor_configuration_path(SOSOptionString p_configurationMonitor_configuration_path) {
        this.configurationMonitor_configuration_path = p_configurationMonitor_configuration_path;
    }

    public JobchainNodeSubstituteOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobchainNodeSubstituteOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobchainNodeSubstituteOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        @SuppressWarnings("unused")
        final String conMethodName = CLASSNAME + "::setAllOptions";
        objSettings = pobjJSSettings;
        super.setAllOptions(pobjJSSettings);
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
    public void CommandLineArgs(String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }
}