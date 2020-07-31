package com.sos.jitl.dailyplan.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sos.JSHelper.Basics.IJSCommands;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.jitl.dailyplan.db.Calendar2DB;
import com.sos.jitl.reporting.db.DBLayer;

public class CreateDailyPlan extends JSJobUtilitiesClass<CreateDailyPlanOptions> implements IJSCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDailyPlan.class);
    protected CreateDailyPlanOptions createDailyPlanOptions;
    protected CheckDailyPlanOptions checkDailyPlanOptions;
    private sos.spooler.Spooler spooler;
    private String schedulerId;

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
        sosHibernateFactory.addClassMapping(DBLayer.getJobStreamClassMapping());
        sosHibernateFactory.build();
        return sosHibernateFactory.openStatelessSession();
    }

    public CreateDailyPlan Execute() throws Exception {
        getOptions().checkMandatory();
        LOGGER.debug(getOptions().dirtyString());
        SOSHibernateSession session = getSession(createDailyPlanOptions.getconfiguration_file().getValue());
        MDC.put("plugin", "dailyplan");
        Calendar2DB calendar2Db = new Calendar2DB(session,this.schedulerId);
        calendar2Db.setOptions(createDailyPlanOptions);
        calendar2Db.setSpooler(spooler);
        try {
            calendar2Db.store();
            return this;
        } finally {
            if (session != null) {
                SOSHibernateFactory factory = session.getFactory();
                session.close();
                factory.close();
            }
            MDC.remove("plugin");
        }
    }

    public void setSpooler(sos.spooler.Spooler spooler) {
        this.spooler = spooler;
    }

    
    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }
}
