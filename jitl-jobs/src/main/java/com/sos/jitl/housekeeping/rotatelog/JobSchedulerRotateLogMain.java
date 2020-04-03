package com.sos.jitl.housekeeping.rotatelog;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.scheduler.messages.JSMsg;

public class JobSchedulerRotateLogMain extends JSToolBox {

    protected JobSchedulerRotateLogOptions objOptions = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerRotateLogMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "JobSchedulerRotateLogMain::Main";
        LOGGER.info("JobSchedulerRotateLog - Main");
        try {
            JobSchedulerRotateLog objM = new JobSchedulerRotateLog();
            JobSchedulerRotateLogOptions objO = objM.getOptions();
            objO.commandLineArgs(pstrArgs);
            objM.executeDebugLog();
            objM.executeMainLog();
        } catch (Exception e) {
            LOGGER.error(methodName + ": " + "Error occured ..." + e.getMessage());
            int intExitCode = 99;
            LOGGER.error(String.format(new JSMsg("JSJ-E-105").get(), methodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(String.format(new JSMsg("JSJ-I-106").get(), methodName));
    }

}