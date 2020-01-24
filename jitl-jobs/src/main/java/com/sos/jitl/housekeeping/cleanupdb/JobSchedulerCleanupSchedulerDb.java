package com.sos.jitl.housekeeping.cleanupdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.dailyplan.db.DailyPlanDBLayer;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.schedulerhistory.db.SchedulerOrderHistoryDBLayer;
import com.sos.jitl.schedulerhistory.db.SchedulerTaskHistoryDBLayer;
import com.sos.scheduler.messages.JSMessages;

public class JobSchedulerCleanupSchedulerDb extends JSJobUtilitiesClass<JobSchedulerCleanupSchedulerDbOptions> {

    protected JobSchedulerCleanupSchedulerDbOptions objOptions = null;
    private final String conClassName = "JobSchedulerCleanupSchedulerDb";
    private static Logger logger = LoggerFactory.getLogger(JobSchedulerCleanupSchedulerDb.class);

    public JobSchedulerCleanupSchedulerDb() {
        super(new JobSchedulerCleanupSchedulerDbOptions());
    }

    @Override
    public JobSchedulerCleanupSchedulerDbOptions getOptions() {
        if (objOptions == null) {
            objOptions = new JobSchedulerCleanupSchedulerDbOptions();
        }
        return objOptions;
    }

    private SOSHibernateSession getSession(String confFile) throws Exception {
        SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
        sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());
        sosHibernateFactory.addClassMapping(DBLayer.getSchedulerClassMapping());
        sosHibernateFactory.addClassMapping(DBLayer.getYadeClassMapping());
        
        sosHibernateFactory.build();
        return sosHibernateFactory.openStatelessSession();
    }
    

    public JobSchedulerCleanupSchedulerDb Execute() throws Exception {
        final String conMethodName = conClassName + "::Execute";
        logger.debug(String.format(JSMessages.JSJ_I_110.get(), conMethodName));
        SOSHibernateSession session = null;
        try {
            getOptions().checkMandatory();
            logger.debug(getOptions().dirtyString());
            session = getSession(getOptions().hibernate_configuration_file_scheduler.getValue());
            
            if (getOptions().cleanup_job_scheduler_history_execute.isTrue()) {
                SchedulerOrderHistoryDBLayer schedulerOrderHistoryDBLayer = new SchedulerOrderHistoryDBLayer(session);
                if (!getOptions().delete_history_interval.isDirty()) {
                    getOptions().delete_history_interval.setValue(getOptions().delete_interval.getValue());
                }
                long i = schedulerOrderHistoryDBLayer.deleteInterval(getOptions().delete_history_interval.value(), getOptions().cleanup_jobscheduler_history_limit.value());
                logger.info(String.format("%s records deleted from SCHEDULER_ORDER_HISTORY that are older than %s days", i, getOptions().delete_history_interval.getValue()));
                SchedulerTaskHistoryDBLayer schedulerTaskHistoryDBLayer = new SchedulerTaskHistoryDBLayer(session);
                i = schedulerTaskHistoryDBLayer.deleteInterval(getOptions().delete_history_interval.value(), getOptions().cleanup_jobscheduler_history_limit.value());
                logger.info(String.format("%s records deleted from SCHEDULER_HISTORY that are older than %s days", i, getOptions().delete_history_interval.getValue()));
            } else {
                logger.info("Records in SCHEDULER_ORDER_HISTORY and SCHEDULER_HISTORY will not be deleted");
            }
          
            if (getOptions().cleanup_daily_plan_execute.isTrue()) {

                DailyPlanDBLayer dailyPlanDBLayer = new DailyPlanDBLayer(session);
                if (!getOptions().delete_daily_plan_interval.isDirty()) {
                    getOptions().delete_daily_plan_interval.setValue(getOptions().delete_interval.getValue());
                }
                long i = dailyPlanDBLayer.deleteInterval(getOptions().delete_daily_plan_interval.value(), getOptions().cleanup_daily_plan_limit.value());
                logger.info(String.format("%s records deleted from DAILY_PLAN that are older than %s days", i, getOptions().delete_history_interval.getValue()));

            } else {
                logger.info("Records in DAILY_PLAN will not be deleted");
            }

//            if (getOptions().cleanup_jade_history_execute.isTrue()) {
//                JadeFilesDBLayer jadeFilesDBLayer = new JadeFilesDBLayer(session);
//                if (!getOptions().delete_jade_history_interval.isDirty()) {
//                    getOptions().delete_jade_history_interval.setValue(getOptions().delete_interval.getValue());
//                }
//                long i = jadeFilesDBLayer.deleteInterval(getOptions().delete_jade_history_interval.value(), getOptions().cleanup_jade_history_limit.value());
//                logger.info(String.format("%s records deleted from JADE_FILES that are older than %s days", i, getOptions().delete_jade_history_interval.getValue()));
//                JadeFilesHistoryDBLayer jadeFilesHistoryDBLayer = new JadeFilesHistoryDBLayer(session);
//                if (!getOptions().delete_jade_history_interval.isDirty()) {
//                    getOptions().delete_jade_history_interval.setValue(getOptions().delete_interval.getValue());
//                }
//                i = jadeFilesHistoryDBLayer.deleteInterval(getOptions().delete_jade_history_interval.value(), getOptions().cleanup_jade_history_limit.value());
//                logger.info(String.format("%s records deleted from JADE_FILES_HISTORY that are older than %s days", i, getOptions().delete_jade_history_interval.getValue()));
//            } else {
//                logger.info("Records in JADE_FILES will not be deleted");
//            }
        } catch (Exception e) {
            String strM = String.format(JSMessages.JSJ_F_107.get(), conMethodName);
            throw new JobSchedulerException(strM, e);
        } finally {
            logger.debug(String.format(JSMessages.JSJ_I_111.get(), conMethodName));
            if (session != null) {
                SOSHibernateFactory factory = session.getFactory();
                session.close();
                factory.close();
            }
        }
        return this;
    }

}