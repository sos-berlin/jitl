package com.sos.jitl.housekeeping.cleanupdb;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;

/** @author Uwe Risse */
public class JobSchedulerCleanupSchedulerDbMain extends JSToolBox {

    protected JobSchedulerCleanupSchedulerDbOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerCleanupSchedulerDbMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "JobSchedulerCleanupSchedulerDbMain::Main";
        LOGGER.info("JobSchedulerCleanupSchedulerDb - Main");
        try {
            JobSchedulerCleanupSchedulerDb objM = new JobSchedulerCleanupSchedulerDb();
            JobSchedulerCleanupSchedulerDbOptions objO = objM.Options();
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