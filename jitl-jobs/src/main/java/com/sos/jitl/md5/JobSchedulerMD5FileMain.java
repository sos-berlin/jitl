package com.sos.jitl.md5;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;

public class JobSchedulerMD5FileMain extends JSToolBox {

    protected JobSchedulerMD5FileOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerMD5FileMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "JobSchedulerMD5FileMain::Main";
        LOGGER.info("JobSchedulerMD5FileJSAdapterClass - Main"); //$NON-NLS-1$
        try {
            JobSchedulerMD5File objM = new JobSchedulerMD5File();
            JobSchedulerMD5FileOptions objO = objM.getOptions();
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