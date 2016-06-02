package com.sos.jitl.httppost;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;

public class JobSchedulerHttpPostJobMain extends JSToolBox {

    protected JobSchedulerHttpPostJobOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerHttpPostJobMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "JobSchedulerHttpPostJobMain::Main";
        LOGGER.info("JobSchedulerHttpPostJob - Main");
        try {
            JobSchedulerHttpPostJob objM = new JobSchedulerHttpPostJob();
            JobSchedulerHttpPostJobOptions objO = objM.getOptions();
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