package com.sos.jitl.dailyplan.job;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;

public class CreateDailyPlanMain extends JSToolBox {

    private static final Logger LOGGER = Logger.getLogger(CreateDailyPlanMain.class);
    protected CreateDailyPlanOptions objOptions = null;

    public final static void main(String[] pstrArgs) {

        final String conMethodName = "CreateDailyPlanMain::Main";
        LOGGER.info("CreateDaysSchedule - Main");
        try {
            CreateDailyPlan createDailyPlan = new CreateDailyPlan();
            CreateDailyPlanOptions createDailyPlanOptions = createDailyPlan.getOptions();
            createDailyPlanOptions.commandLineArgs(pstrArgs);
            createDailyPlan.Execute();
        } catch (Exception e) {
            System.err.println(conMethodName + ": " + "Error occured ..." + e.getMessage());
            LOGGER.error(e.getMessage(), e);
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", conMethodName, intExitCode), e);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", conMethodName));
    }

}