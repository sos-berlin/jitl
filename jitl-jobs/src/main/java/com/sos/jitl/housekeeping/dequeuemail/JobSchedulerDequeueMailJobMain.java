package com.sos.jitl.housekeeping.dequeuemail;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;

public class JobSchedulerDequeueMailJobMain extends JSToolBox {

    protected JobSchedulerDequeueMailJobOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerDequeueMailJobMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "JobSchedulerDequeueMailJobMain::Main";
        LOGGER.info("JobSchedulerDequeueMailJob - Main");
        try {
            JobSchedulerDequeueMailJob objM = new JobSchedulerDequeueMailJob();
            JobSchedulerDequeueMailJobOptions objO = objM.getOptions();
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