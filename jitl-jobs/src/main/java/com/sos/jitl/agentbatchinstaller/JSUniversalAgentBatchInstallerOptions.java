package com.sos.jitl.agentbatchinstaller;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

@JSOptionClass(name = "JSUniversalAgentBatchInstallerOptions", description = "Unattended Batch Installation on remote servers")
public class JSUniversalAgentBatchInstallerOptions extends JSUniversalAgentBatchInstallerOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")//$NON-NLS-1$
    private final String conClassName = "JSUniversalAgentBatchInstallerOptions";  //$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = null; /*
                                          * Logger.getLogger(JSBatchInstallerOptions
                                          * .class);
                                          */

    public JSUniversalAgentBatchInstallerOptions() {
    } // public JSUniversalAgentBatchInstallerOptions

    public JSUniversalAgentBatchInstallerOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JSUniversalAgentBatchInstallerOptions

    public JSUniversalAgentBatchInstallerOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
        logger = Logger.getLogger(JSUniversalAgentBatchInstallerOptions.class);

    } // public JSUniversalAgentBatchInstallerOptions (HashMap JSSettings)

    @Override
    // JSBatchInstallerOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
