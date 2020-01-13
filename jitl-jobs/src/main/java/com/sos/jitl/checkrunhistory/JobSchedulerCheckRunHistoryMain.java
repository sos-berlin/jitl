package com.sos.jitl.checkrunhistory;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerCheckRunHistoryMain extends JSToolBox {

    protected JobSchedulerCheckRunHistoryOptions objOptions = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerCheckRunHistoryMain.class);
    private static Messages Messages = null;

    public final static void main(String[] pstrArgs) {
        final String methodName = "JobSchedulerCheckRunHistoryMain::Main";
        Messages = new Messages("com_sos_scheduler_messages", Locale.getDefault());
        LOGGER.info("JobSchedulerCheckRunHistory - Main");
        try {
            JobSchedulerCheckRunHistory objM = new JobSchedulerCheckRunHistory();
            JobSchedulerCheckRunHistoryOptions objO = objM.options();
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