package com.sos.jitl.dailyplan.job;

import java.io.File;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Spooler;

public class CheckDailyPlanJSAdapterClass extends JobSchedulerJobAdapter {

    private final String conClassName = "CheckDailyScheduleJSAdapterClass";
    private static final Logger LOGGER = Logger.getLogger(CheckDailyPlanJSAdapterClass.class);

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            return false;
        }
        return spooler_task.job().order_queue() != null;
    }

    private void doProcessing() throws Exception {
        final String conMethodName = conClassName + "::doProcessing";
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
        CheckDailyPlan checkDailyPlan = new CheckDailyPlan();
        CheckDailyPlanOptions checkDailyPlanOptions = checkDailyPlan.getOptions();
        checkDailyPlanOptions.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
        Spooler spooler = (Spooler) getSpoolerObject();
        String schedulerId = "";
        String configuration_file = "";
        if (checkDailyPlanOptions.getItem("scheduler_id") != null) {
            LOGGER.debug("scheduler_id from param");
            schedulerId = checkDailyPlanOptions.scheduler_id.getValue();
        } else {
            LOGGER.debug("scheduler_id from scheduler");
            schedulerId = spooler.id();
        }
        if (checkDailyPlanOptions.configuration_file.IsEmpty() == false) {
            LOGGER.debug("configuration_file from param");
            configuration_file = checkDailyPlanOptions.configuration_file.getValue();
        } else {
            LOGGER.debug("configuration_file from scheduler");
            File f = new File(new File(spooler.configuration_directory()).getParent(), "hibernate.cfg.xml");
            if (!f.exists()) {
                f = new File(new File(spooler.directory()), "config/hibernate.cfg.xml");
            }
            configuration_file = f.getAbsolutePath();
        }
        if (checkDailyPlanOptions.check_all_jobscheduler_instances.value()) {
            schedulerId = "";
            spooler_log.debug3("Checking all JobScheduler instance");
        }
        checkDailyPlanOptions.configuration_file.setValue(configuration_file);
        checkDailyPlanOptions.scheduler_id.setValue(schedulerId);
        checkDailyPlanOptions.checkMandatory();
        checkDailyPlan.setJSJobUtilites(this);
        checkDailyPlan.Execute();
    }

}