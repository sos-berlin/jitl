package com.sos.jitl.dailyplan.job;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.IJSCommands;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.jitl.dailyplan.db.Calendar2DB;

public class CreateDailyPlan extends JSJobUtilitiesClass<CreateDailyPlanOptions> implements IJSCommands {

    private static final Logger LOGGER = Logger.getLogger(CreateDailyPlan.class);
    protected CreateDailyPlanOptions createDailyPlanOptions;

    public CreateDailyPlan() {
        super(new CreateDailyPlanOptions());
    }

    @Override
    public CreateDailyPlanOptions getOptions() {
        if (createDailyPlanOptions == null) {
            createDailyPlanOptions = new CreateDailyPlanOptions();
        }
        return createDailyPlanOptions;
    }

    public CreateDailyPlan Execute() throws Exception {
        getOptions().checkMandatory();
        LOGGER.debug(getOptions().dirtyString());
        Calendar2DB calendar2Db = new Calendar2DB(createDailyPlanOptions.getconfiguration_file().getValue());
        try {
            calendar2Db.setOptions(createDailyPlanOptions);
            calendar2Db.beginTransaction();
            calendar2Db.store();
            calendar2Db.commit();

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            calendar2Db.rollback();
            throw new Exception(e);

        }
        return this;
    }

}
