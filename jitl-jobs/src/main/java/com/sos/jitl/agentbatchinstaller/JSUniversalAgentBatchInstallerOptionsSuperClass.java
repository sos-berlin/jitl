

package com.sos.jitl.agentbatchinstaller;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSJobChainName;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionString;
 
@JSOptionClass(name = "JSUniversalAgentBatchInstallerOptionsSuperClass", description = "JSUniversalAgentBatchInstallerOptionsSuperClass")
public class JSUniversalAgentBatchInstallerOptionsSuperClass extends JSOptionsClass {
	 
	private static final long	serialVersionUID	= 9068599714451980002L;
	private final String					conClassName						= "JSUniversalAgentBatchInstallerOptionsSuperClass";
		@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JSUniversalAgentBatchInstallerOptionsSuperClass.class);

	 
  
    @JSOptionDefinition(name = "update", 
    description = "False: Ignore value of 'LastRun'", 
    key = "update", 
    type = "SOSOptionBoolean", 
    mandatory = false)
    
    public SOSOptionBoolean update = new SOSOptionBoolean(this, conClassName + ".update", // HashMap-Key
                                                                "False: Ignore value of LastRun", // Titel
                                                                "false", // InitValue
                                                                "false", // DefaultValue
                                                                false // isMandatory
                    );
 
    public SOSOptionBoolean  getupdate() {
        return update;
    }
 
    public void setupdate (SOSOptionBoolean p_update) { 
        this.update = p_update;
    }

  
    @JSOptionDefinition(name = "filter_install_host", 
    description = "Only installations are executed which belongs to this host.", 
    key = "filter_install_host", 
    type = "SOSOptionHostName", 
    mandatory = false)
    
    public SOSOptionHostName filter_install_host = new SOSOptionHostName(this, conClassName + ".filter_install_host", // HashMap-Key
                                                                "Only installations are executed which belongs to this host.", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );
 
    public SOSOptionHostName  getfilter_install_host() {
        return filter_install_host;
    }
 
    public void setfilter_install_host (SOSOptionHostName p_filter_install_host) { 
        this.filter_install_host = p_filter_install_host;
    }

                        
 
    @JSOptionDefinition(name = "filter_install_port", 
    description = "Only installations are executed which belongs to this port.", 
    key = "filter_install_port", 
    type = "SOSOptionPortNumber", 
    mandatory = false)
    
    public SOSOptionPortNumber filter_install_port = new SOSOptionPortNumber(this, conClassName + ".filter_install_port", // HashMap-Key
                                                                "Only installations are executed which belongs to this port.", // Titel
                                                                "0", // InitValue
                                                                "0", // DefaultValue
                                                                false // isMandatory
                    );
 
    public SOSOptionPortNumber  getfilter_install_port() {
        return filter_install_port;
    }
 
    public void setfilter_install_port (SOSOptionPortNumber p_filter_install_port) { 
        this.filter_install_port = p_filter_install_port;
    }

                        
 
    @JSOptionDefinition(name = "installation_definition_file", 
    description = "XML file with installation elements. One element per installation.", 
    key = "installation_definition_file", 
    type = "SOSOptionInFileName", 
    mandatory = true)
    
    public SOSOptionInFileName installation_definition_file = new SOSOptionInFileName(this, conClassName + ".installation_definition_file", // HashMap-Key
                                                                "XML file with installation elements. One element per installation.", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                true // isMandatory
                    );
 
    public SOSOptionInFileName  getinstallation_definition_file() {
        return installation_definition_file;
    }

 
    public void setinstallation_definition_file (SOSOptionInFileName p_installation_definition_file) { 
        this.installation_definition_file = p_installation_definition_file;
    }

 
   
 
    @JSOptionDefinition(name = "installation_job_chain", 
    description = "Job chain with the steps for transfer the installation files and perfo", 
    key = "installation_job_chain", 
    type = "JSOptionJobChainName", 
    mandatory = true)
    
    public JSJobChainName installation_job_chain = new JSJobChainName(this, conClassName + ".installation_job_chain", // HashMap-Key
                                                                "Job chain with the steps for transfer the installation files and perfo", // Titel
                                                                "automatic_installation", // InitValue
                                                                "automatic_installation", // DefaultValue
                                                                true // isMandatory
                    );

 
    public JSJobChainName  getinstallation_job_chain() {
        return installation_job_chain;
    }

 
    public void setinstallation_job_chain (JSJobChainName p_installation_job_chain) { 
        this.installation_job_chain = p_installation_job_chain;
    }

                        
        
        
	public JSUniversalAgentBatchInstallerOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public JSBatchInstallerOptionsSuperClass

	public JSUniversalAgentBatchInstallerOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JSBatchInstallerOptionsSuperClass

		//

	public JSUniversalAgentBatchInstallerOptionsSuperClass (HashMap <String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public JSBatchInstallerOptionsSuperClass (HashMap JSSettings)
 
 
	public void setAllOptions(HashMap <String, String> pobjJSSettings) {
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
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
		} // public void CheckMandatory ()

 
	@Override
	public void CommandLineArgs(String[] pstrArgs) {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}
} // public class JSUniversalAgentBatchInstallerOptionsSuperClass