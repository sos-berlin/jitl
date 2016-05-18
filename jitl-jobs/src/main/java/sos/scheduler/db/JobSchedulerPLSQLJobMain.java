package sos.scheduler.db;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;

public class JobSchedulerPLSQLJobMain extends JSToolBox {

    protected JobSchedulerPLSQLJobOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerPLSQLJobMain.class);

    public final static void main(final String[] pstrArgs) {
        final String methodName = "JobSchedulerPLSQLJobMain::Main";
        LOGGER.info("JobSchedulerPLSQLJob - Main");
        try {
            JobSchedulerPLSQLJob objM = new JobSchedulerPLSQLJob();
            JobSchedulerPLSQLJobOptions objO = objM.getOptions();
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