package sos.scheduler.xsl;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;

/** @author KB */
public class JobSchedulerXslTransformMain extends JSToolBox {

    protected JobSchedulerXslTransformOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerXslTransformMain.class);

    public final static void main(final String[] pstrArgs) {
        final String methodName = "JobSchedulerXslTransformMain::Main";
        LOGGER.info("JobSchedulerXslTransform - Main");
        try {
            JobSchedulerXslTransform objM = new JobSchedulerXslTransform();
            JobSchedulerXslTransformOptions objO = objM.getOptions();
            objO.commandLineArgs(pstrArgs);
            objM.Execute();
        } catch (Exception e) {
            LOGGER.error(methodName + ": " + "Error occured ..." + e.getMessage());
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", methodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", methodName));
        System.exit(0);
    }

}