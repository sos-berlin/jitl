package com.sos.jitl.sync;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;

public class JobSchedulerSynchronizeJobChainsMain extends JSToolBox {

    protected JobSchedulerSynchronizeJobChainsOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerSynchronizeJobChainsMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "JobSchedulerSynchronizeJobChainsMain::Main";
        LOGGER.info("JobSchedulerSynchronizeJobChains - Main");
        try {
            JobSchedulerSynchronizeJobChains objM = new JobSchedulerSynchronizeJobChains();
			JobSchedulerSynchronizeJobChainsOptions objO = objM.getOptions();
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