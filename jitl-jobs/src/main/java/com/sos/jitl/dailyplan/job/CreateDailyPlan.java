package com.sos.jitl.dailyplan.job;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.IJSCommands;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.jitl.dailyplan.db.Calendar2DB;
import com.sos.jitl.dailyplan.db.DailyPlanAdjustment;
import com.sos.jitl.reporting.db.DBLayer;

public class CreateDailyPlan extends JSJobUtilitiesClass<CreateDailyPlanOptions> implements IJSCommands {

    private static final Logger LOGGER = Logger.getLogger(CreateDailyPlan.class);
    protected CreateDailyPlanOptions createDailyPlanOptions;
    protected CheckDailyPlanOptions checkDailyPlanOptions;
    private String schedulerId;
    private sos.spooler.Spooler spooler;

    public CreateDailyPlan() {
        super(new CreateDailyPlanOptions());
    }

    @Override
    public CreateDailyPlanOptions getOptions() {
        if (createDailyPlanOptions == null) {
            createDailyPlanOptions = new CreateDailyPlanOptions();
            checkDailyPlanOptions = new CheckDailyPlanOptions();
        }
        return createDailyPlanOptions;
    }

    private SOSHibernateSession getSession(String confFile) throws Exception {
        SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
        sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());
        sosHibernateFactory.build();
        return sosHibernateFactory.openStatelessSession();
    }

    public CreateDailyPlan Execute() throws Exception {
        getOptions().checkMandatory();
        LOGGER.debug(getOptions().dirtyString());
        SOSHibernateSession session = getSession(createDailyPlanOptions.getconfiguration_file().getValue());
        Calendar2DB calendar2Db = new Calendar2DB(session);
        try {
            calendar2Db.setOptions(createDailyPlanOptions);
            calendar2Db.setSpooler(spooler);
            calendar2Db.beginTransaction();
            calendar2Db.store();
            calendar2Db.commit();

            DailyPlanAdjustment dailyPlanAdjustment = new DailyPlanAdjustment(session);
            try {
                checkDailyPlanOptions.dayOffset.value(createDailyPlanOptions.dayOffset.value());
                checkDailyPlanOptions.configuration_file.setValue(createDailyPlanOptions.configuration_file.getValue());
                if (schedulerId != null) {
                    checkDailyPlanOptions.scheduler_id.setValue(schedulerId);
                }
                dailyPlanAdjustment.setOptions(checkDailyPlanOptions);
                dailyPlanAdjustment.setTo(new Date());
                dailyPlanAdjustment.beginTransaction();
                dailyPlanAdjustment.adjustWithHistory();
                dailyPlanAdjustment.commit();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                dailyPlanAdjustment.rollback();
                throw new Exception(e);
            } finally {
                if (session != null) {
                    SOSHibernateFactory factory = session.getFactory();
                    session.close();
                    factory.close();
                }
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            calendar2Db.rollback();
            throw new Exception(e);

        }
        return this;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public void setSpooler(sos.spooler.Spooler spooler) {
        this.spooler = spooler;
    }

}
