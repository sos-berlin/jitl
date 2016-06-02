package com.sos.jitl.agentbatchinstaller;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSJobChainName;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionPortNumber;

@JSOptionClass(name = "JSUniversalAgentBatchInstallerOptionsSuperClass", description = "JSUniversalAgentBatchInstallerOptionsSuperClass")
public class JSUniversalAgentBatchInstallerOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 9068599714451980002L;
    private final String conClassName = "JSUniversalAgentBatchInstallerOptionsSuperClass";

    @JSOptionDefinition(name = "update", description = "False: Ignore value of 'LastRun'", key = "update", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean update = new SOSOptionBoolean(this, conClassName + ".update", "False: Ignore value of LastRun", "false", "false", false);

    public SOSOptionBoolean getupdate() {
        return update;
    }

    public void setupdate(SOSOptionBoolean p_update) {
        this.update = p_update;
    }

    @JSOptionDefinition(name = "filter_install_host", description = "Only installations are executed which belongs to this host.",
            key = "filter_install_host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName filter_install_host = new SOSOptionHostName(this, conClassName + ".filter_install_host",
            "Only installations are executed which belongs to this host.", " ", " ", false);

    public SOSOptionHostName getfilter_install_host() {
        return filter_install_host;
    }

    public void setfilter_install_host(SOSOptionHostName p_filter_install_host) {
        this.filter_install_host = p_filter_install_host;
    }

    @JSOptionDefinition(name = "filter_install_port", description = "Only installations are executed which belongs to this port.",
            key = "filter_install_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber filter_install_port = new SOSOptionPortNumber(this, conClassName + ".filter_install_port",
            "Only installations are executed which belongs to this port.", "0", "0", false);

    public SOSOptionPortNumber getfilter_install_port() {
        return filter_install_port;
    }

    public void setfilter_install_port(SOSOptionPortNumber p_filter_install_port) {
        this.filter_install_port = p_filter_install_port;
    }

    @JSOptionDefinition(name = "installation_definition_file", description = "XML file with installation elements. One element per installation.",
            key = "installation_definition_file", type = "SOSOptionInFileName", mandatory = true)
    public SOSOptionInFileName installation_definition_file = new SOSOptionInFileName(this, conClassName + ".installation_definition_file",
            "XML file with installation elements. One element per installation.", " ", " ", true);

    public SOSOptionInFileName getinstallation_definition_file() {
        return installation_definition_file;
    }

    public void setinstallation_definition_file(SOSOptionInFileName p_installation_definition_file) {
        this.installation_definition_file = p_installation_definition_file;
    }

    @JSOptionDefinition(name = "installation_job_chain", description = "Job chain with the steps for transfer the installation files and perfo",
            key = "installation_job_chain", type = "JSOptionJobChainName", mandatory = true)
    public JSJobChainName installation_job_chain = new JSJobChainName(this, conClassName + ".installation_job_chain",
            "Job chain with the steps for transfer the installation files and perfo", "automatic_installation", "automatic_installation", true);

    public JSJobChainName getinstallation_job_chain() {
        return installation_job_chain;
    }

    public void setinstallation_job_chain(JSJobChainName p_installation_job_chain) {
        this.installation_job_chain = p_installation_job_chain;
    }

    public JSUniversalAgentBatchInstallerOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JSUniversalAgentBatchInstallerOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JSUniversalAgentBatchInstallerOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        objSettings = pobjJSSettings;
        super.setAllOptions(pobjJSSettings);
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