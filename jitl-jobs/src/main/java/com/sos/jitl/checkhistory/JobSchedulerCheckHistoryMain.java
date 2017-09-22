package com.sos.jitl.checkhistory;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;
import org.apache.log4j.Logger;

import java.util.Locale;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerCheckHistoryMain extends JSToolBox {

    protected JobSchedulerCheckHistoryOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerCheckHistoryMain.class);
    private static Messages Messages = null;

    public final static void main(String[] pstrArgs) {
        final String methodName = "JobSchedulerCheckHistoryMain::Main";
        Messages = new Messages("com_sos_scheduler_messages", Locale.getDefault());
        LOGGER.info("JobSchedulerCheckHistory - Main");
        try {
            JobSchedulerCheckHistory objM = new JobSchedulerCheckHistory();
            JobSchedulerCheckHistoryOptions objO = objM.options();
            objO.commandLineArgs(pstrArgs);
            objM.Execute();
        } catch (Exception e) {
            LOGGER.error(methodName + ": " + "Error occured ..." + e.getMessage());
            int intExitCode = 99;
            LOGGER.error(Messages.getMsg("JSJ-E-105: %1$s - terminated with exit-code %2$d", methodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(Messages.getMsg("JSJ-I-106: %1$s - ended without errors", methodName));
    }

}