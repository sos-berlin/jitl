package com.sos.jitl.eventing.checkevents;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;

public class JobSchedulerCheckEventsMain extends JSToolBox {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerCheckEventsMain.class);
    protected JobSchedulerCheckEventsOptions objOptions = null;

    public final static void main(String[] pstrArgs) {
        final String methodName = "JobSchedulerCheckEventsMain::Main";
        LOGGER.info("JobSchedulerCheckEvents - Main");
        try {
            JobSchedulerCheckEvents objM = new JobSchedulerCheckEvents();
            JobSchedulerCheckEventsOptions objO = objM.getOptions();
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