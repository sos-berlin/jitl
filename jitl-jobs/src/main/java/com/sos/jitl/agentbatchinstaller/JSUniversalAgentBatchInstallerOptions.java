package com.sos.jitl.agentbatchinstaller;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JSUniversalAgentBatchInstallerOptions", description = "Unattended Batch Installation on remote servers")
public class JSUniversalAgentBatchInstallerOptions extends JSUniversalAgentBatchInstallerOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public JSUniversalAgentBatchInstallerOptions() {
    }

    public JSUniversalAgentBatchInstallerOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JSUniversalAgentBatchInstallerOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    }

    @Override
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

}