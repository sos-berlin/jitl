package com.sos.jitl.dailyplan.job;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.jitl.dailyplan.db.DailyPlanAdjustment;

public class CheckDailyPlan extends JSJobUtilitiesClass<CheckDailyPlanOptions> {

    private static final Logger LOGGER = Logger.getLogger(CheckDailyPlan.class);
    protected CheckDailyPlanOptions createDailyPlanOptions;

    public CheckDailyPlan() {
        super(new CheckDailyPlanOptions());
    }

    @Override
    public CheckDailyPlanOptions getOptions() {
        if (createDailyPlanOptions == null) {
            createDailyPlanOptions = new CheckDailyPlanOptions();
        }
        return createDailyPlanOptions;
    }
 
    public CheckDailyPlan Execute() throws Exception {
        getOptions().checkMandatory();
        LOGGER.debug(getOptions().dirtyString());
        DailyPlanAdjustment dailyPlanAdjustment = new DailyPlanAdjustment(new File(createDailyPlanOptions.configuration_file.getValue()));
        try {
            dailyPlanAdjustment.setOptions(createDailyPlanOptions);
            dailyPlanAdjustment.setTo(new Date());
            dailyPlanAdjustment.beginTransaction();
            dailyPlanAdjustment.adjustWithHistory();
            dailyPlanAdjustment.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            dailyPlanAdjustment.rollback();
            throw new Exception(e);
        }
        return this;
    }

}