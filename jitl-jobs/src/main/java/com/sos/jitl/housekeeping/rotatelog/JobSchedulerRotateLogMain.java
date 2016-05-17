package com.sos.jitl.housekeeping.rotatelog;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.scheduler.messages.JSMsg;

public class JobSchedulerRotateLogMain extends JSToolBox {

    protected JobSchedulerRotateLogOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerRotateLogMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "JobSchedulerRotateLogMain::Main";
        LOGGER.info("JobSchedulerRotateLog - Main");
        try {
            JobSchedulerRotateLog objM = new JobSchedulerRotateLog();
            JobSchedulerRotateLogOptions objO = objM.getOptions();
            objO.AllowEmptyParameterList.setFalse();
            objO.ApplicationName.Value("JITL");
            objO.ApplicationDocuUrl.Value("http://www.sos-berlin.com/jitl/JobSchedulerRotateLog.xml");
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