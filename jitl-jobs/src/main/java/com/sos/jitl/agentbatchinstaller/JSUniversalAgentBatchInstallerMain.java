package com.sos.jitl.agentbatchinstaller;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;

public class JSUniversalAgentBatchInstallerMain extends JSToolBox {

    protected JSUniversalAgentBatchInstallerOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JSUniversalAgentBatchInstallerMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "JSUniversalAgentBatchInstallerMain::Main";
        LOGGER.info("JSUniversalAgentBatchInstallerMain - Main");
        try {
            JSUniversalAgentBatchInstaller objM = new JSUniversalAgentBatchInstaller();
            JSUniversalAgentBatchInstallerOptions objO = objM.options();
            objO.commandLineArgs(pstrArgs);
            objM.execute();
        } catch (Exception e) {
            LOGGER.error(methodName + ": " + "Error occured ..." + e.getMessage());
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", methodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", methodName));
    }

}