package sos.scheduler.InstallationService;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;

public class JSBatchInstallerMain extends JSToolBox {

    protected JSBatchInstallerOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JSBatchInstallerMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "JSBatchInstallerMain::Main";
        LOGGER.info("JSBatchInstaller - Main");
        try {
            JSBatchInstaller objM = new JSBatchInstaller();
            JSBatchInstallerOptions objO = objM.Options();
            objO.CommandLineArgs(pstrArgs);
            objM.Execute();
        } catch (Exception e) {
            LOGGER.error(methodName + ": " + "Error occured ..." + e.getMessage());
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", methodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", methodName));
    }

}