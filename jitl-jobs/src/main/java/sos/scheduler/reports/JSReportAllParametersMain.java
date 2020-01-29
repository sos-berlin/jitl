package sos.scheduler.reports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;

/** @author KB */
public class JSReportAllParametersMain extends JSToolBox {

	protected JSReportAllParametersOptions objOptions = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(JSReportAllParametersMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "JSReportAllParametersMain::Main";
        LOGGER.info("JSReportAllParameters - Main");
        try {
            JSReportAllParameters objM = new JSReportAllParameters();
            JSReportAllParametersOptions objO = objM.Options();
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