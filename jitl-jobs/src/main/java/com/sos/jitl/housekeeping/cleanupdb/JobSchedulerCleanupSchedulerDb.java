package com.sos.jitl.housekeeping.cleanupdb;

import java.io.File;

import org.apache.log4j.Logger;

import sos.jadehistory.db.JadeFilesDBLayer;
import sos.jadehistory.db.JadeFilesHistoryDBLayer;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.jitl.dailyplan.db.DailyPlanDBLayer;
import com.sos.jitl.schedulerhistory.db.SchedulerOrderHistoryDBLayer;
import com.sos.jitl.schedulerhistory.db.SchedulerTaskHistoryDBLayer;
import com.sos.scheduler.messages.JSMessages;

public class JobSchedulerCleanupSchedulerDb extends JSJobUtilitiesClass<JobSchedulerCleanupSchedulerDbOptions> {

    protected JobSchedulerCleanupSchedulerDbOptions objOptions = null;
    private final String conClassName = "JobSchedulerCleanupSchedulerDb";
    private static Logger logger = Logger.getLogger(JobSchedulerCleanupSchedulerDb.class);

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

    public JobSchedulerCleanupSchedulerDb Execute() throws Exception {
        final String conMethodName = conClassName + "::Execute";
        logger.debug(String.format(JSMessages.JSJ_I_110.get(), conMethodName));
        try {
            getOptions().checkMandatory();
            logger.debug(getOptions().dirtyString());
            if (getOptions().cleanup_job_scheduler_history_execute.isTrue()) {
                SchedulerOrderHistoryDBLayer schedulerOrderHistoryDBLayer = new SchedulerOrderHistoryDBLayer(new File(getOptions().hibernate_configuration_file.getValue()));
                if (!getOptions().delete_history_interval.isDirty()) {
                    getOptions().delete_history_interval.setValue(getOptions().delete_interval.getValue());
                }
                long i = schedulerOrderHistoryDBLayer.deleteInterval(getOptions().delete_history_interval.value(), getOptions().cleanup_jobscheduler_history_limit.value());
                logger.info(String.format("%s records deleted from SCHEDULER_ORDER_HISTORY that are older than %s days", i, getOptions().delete_history_interval.getValue()));
                SchedulerTaskHistoryDBLayer schedulerTaskHistoryDBLayer = new SchedulerTaskHistoryDBLayer(new File(getOptions().hibernate_configuration_file.getValue()));
                i = schedulerTaskHistoryDBLayer.deleteInterval(getOptions().delete_history_interval.value(), getOptions().cleanup_jobscheduler_history_limit.value());
                logger.info(String.format("%s records deleted from SCHEDULER_HISTORY that are older than %s days", i, getOptions().delete_history_interval.getValue()));
            } else {
                logger.info("Records in SCHEDULER_ORDER_HISTORY and SCHEDULER_HISTORY will not be deleted");
            }
          
            if (getOptions().cleanup_daily_plan_execute.isTrue()) {
            /*    DailyScheduleDBLayer dailyScheduleDBLayer = new DailyScheduleDBLayer(getOptions().hibernate_configuration_file.getValue());
                if (!getOptions().delete_daily_plan_interval.isDirty()) {
                    getOptions().delete_daily_plan_interval.setValue(getOptions().delete_interval.getValue());
                }
                long i = dailyScheduleDBLayer.deleteInterval(getOptions().delete_daily_plan_interval.value(), getOptions().cleanup_daily_plan_limit.value());
                logger.info(String.format("%s records deleted from DAYS_SCHEDULE that are older than %s days", i, getOptions().delete_history_interval.getValue()));
                */

                DailyPlanDBLayer dailyPlanDBLayer = new DailyPlanDBLayer(getOptions().hibernate_configuration_file.getValue());
                if (!getOptions().delete_daily_plan_interval.isDirty()) {
                    getOptions().delete_daily_plan_interval.setValue(getOptions().delete_interval.getValue());
                }
                long i = dailyPlanDBLayer.deleteInterval(getOptions().delete_daily_plan_interval.value(), getOptions().cleanup_daily_plan_limit.value());
                logger.info(String.format("%s records deleted from DAILY_PLAN that are older than %s days", i, getOptions().delete_history_interval.getValue()));

            } else {
                logger.info("Records in DAYS_SCHEDULE will not be deleted");
            }

            if (getOptions().cleanup_jade_history_execute.isTrue()) {
                JadeFilesDBLayer jadeFilesDBLayer = new JadeFilesDBLayer(getOptions().hibernate_configuration_file.getValue());
                if (!getOptions().delete_jade_history_interval.isDirty()) {
                    getOptions().delete_jade_history_interval.setValue(getOptions().delete_interval.getValue());
                }
                long i = jadeFilesDBLayer.deleteInterval(getOptions().delete_jade_history_interval.value(), getOptions().cleanup_jade_history_limit.value());
                logger.info(String.format("%s records deleted from JADE_FILES that are older than %s days", i, getOptions().delete_jade_history_interval.getValue()));
                JadeFilesHistoryDBLayer jadeFilesHistoryDBLayer = new JadeFilesHistoryDBLayer(getOptions().hibernate_configuration_file.getValue());
                if (!getOptions().delete_jade_history_interval.isDirty()) {
                    getOptions().delete_jade_history_interval.setValue(getOptions().delete_interval.getValue());
                }
                i = jadeFilesHistoryDBLayer.deleteInterval(getOptions().delete_jade_history_interval.value(), getOptions().cleanup_jade_history_limit.value());
                logger.info(String.format("%s records deleted from JADE_FILES_HISTORY that are older than %s days", i, getOptions().delete_jade_history_interval.getValue()));
            } else {
                logger.info("Records in JADE_FILES will not be deleted");
            }
        } catch (Exception e) {
            String strM = String.format(JSMessages.JSJ_F_107.get(), conMethodName);
            throw new JobSchedulerException(strM, e);
        } finally {
            logger.debug(String.format(JSMessages.JSJ_I_111.get(), conMethodName));
        }
        return this;
    }

}