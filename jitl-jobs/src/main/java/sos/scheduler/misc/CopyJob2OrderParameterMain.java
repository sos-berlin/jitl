package sos.scheduler.misc;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;

public class CopyJob2OrderParameterMain extends JSToolBox {

    protected CopyJob2OrderParameterOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(CopyJob2OrderParameterMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "CopyJob2OrderParameterMain::Main";
        LOGGER.info("CopyJob2OrderParameter - Main"); //$NON-NLS-1$
        try {
            CopyJob2OrderParameter objM = new CopyJob2OrderParameter();
            CopyJob2OrderParameterOptions objO = objM.Options();
            objO.commandLineArgs(pstrArgs);
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