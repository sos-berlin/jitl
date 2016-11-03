package com.sos.jitl.dailyplan.job;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;

public class CheckDailyPlanMain extends JSToolBox {

    private static final Logger LOGGER = Logger.getLogger(CheckDailyPlanMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "CheckDailyPlanMain::Main";
        LOGGER.info("CheckDailySchedule - Main");
        try {
            CheckDailyPlan checkDailyPlan = new CheckDailyPlan();
            CheckDailyPlanOptions checkDailyPlanOptions = checkDailyPlan.getOptions();
            checkDailyPlanOptions.commandLineArgs(pstrArgs);
            checkDailyPlan.Execute();
        } catch (Exception e) {
            System.err.println(methodName + ": " + "Error occured ..." + e.getMessage());
            LOGGER.error(e.getMessage(), e);
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", methodName, intExitCode), e);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", methodName));
    }

}