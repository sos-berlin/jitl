package com.sos.jitl.dailyplan.job;

import java.util.Date;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.jitl.dailyplan.db.DailyPlanAdjustment;
import com.sos.jitl.reporting.db.DBLayer;

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

    private SOSHibernateSession getStatelessSession(String confFile) throws Exception {
        SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
        sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());
        sosHibernateFactory.build();
        return sosHibernateFactory.openStatelessSession();
    }

    public CheckDailyPlan Execute() throws Exception {
        getOptions().checkMandatory();
        LOGGER.debug(getOptions().dirtyString());
        SOSHibernateSession session = getStatelessSession(createDailyPlanOptions.configuration_file.getValue());
        DailyPlanAdjustment dailyPlanAdjustment = new DailyPlanAdjustment(getStatelessSession(createDailyPlanOptions.configuration_file.getValue()));
        dailyPlanAdjustment.setOptions(createDailyPlanOptions);
        dailyPlanAdjustment.setTo(new Date());
        try {
            dailyPlanAdjustment.adjustWithHistory();
            return this;
        } finally {
            if (session != null) {
                SOSHibernateFactory factory = session.getFactory();
                session.close();
                factory.close();
            }
        }
    }

}