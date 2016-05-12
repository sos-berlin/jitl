package com.sos.jitl.jobchainnodeparameter.monitor;

import java.util.HashMap;
import com.sos.JSHelper.Options.*;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "ConfigurationMonitorOptionsSuperClass", description = "ConfigurationMonitorOptionsSuperClass")
public class JobchainNodeSubstituteOptionsSuperClass extends JSOptionsClass {
    private static final String CLASSNAME = "ConfigurationMonitorOptionsSuperClass";


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
    
    
    @JSOptionDefinition(name = "configurationMonitor_configuration_file", description = "The default value is the name of the job chain of the actual running o", key = "configurationMonitor_configuration_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString configurationMonitorConfigurationFile = new SOSOptionString(this, CLASSNAME + ".configurationMonitor_configuration_file",
            "The default value is the name of the job chain of the actual running o",
            // InitValue, DefaultValue, isMandatory
            " ", " ", false);

    public SOSOptionString getConfigurationMonitorCconfigurationFile() {
        return configurationMonitorConfigurationFile;
    }

    public void setconfigurationMonitor_configuration_file(SOSOptionString p_configurationMonitor_configuration_file) {
        this.configurationMonitorConfigurationFile = p_configurationMonitor_configuration_file;
    }

    @JSOptionDefinition(name = "configurationMonitor_configuration_path", description = "The default value is the directory that contains the job chain definit", key = "configurationMonitor_configuration_path", type = "SOSOptionString", mandatory = false)
    public SOSOptionString configurationMonitorConfigurationPath = new SOSOptionString(this, CLASSNAME + ".configurationMonitor_configuration_path",
            "The default value is the directory that contains the job chain definit",
            // InitValue, DefaultValue, isMandatory
            " ", " ", false);

    public SOSOptionString getConfigurationMonitorConfigurationPath() {
        return configurationMonitorConfigurationPath;
    }

    public void setConfigurationMonitorConfigurationPath(SOSOptionString p_configurationMonitor_configuration_path) {
        this.configurationMonitorConfigurationPath = p_configurationMonitor_configuration_path;
    }

 
    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
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