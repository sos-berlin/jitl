package com.sos.jitl.eventing.checkevents;

import java.io.File;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Spooler;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerCheckEventsJSAdapterClass extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerCheckEventsJSAdapterClass.class);
    private boolean success = false;

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
        if (success) {
            return signalSuccess();
        } else {
            return false;
        }
    }

    private void doProcessing() throws Exception {
        JobSchedulerCheckEvents jobSchedulerCheckEvents = new JobSchedulerCheckEvents();
        JobSchedulerCheckEventsOptions jobSchedulerCheckEventsOptions = jobSchedulerCheckEvents.getOptions();
        String configuration_file = "";

        if (jobSchedulerCheckEventsOptions.getItem("configuration_file") != null) {
            LOGGER.debug("configuration_file from param");
            configuration_file = jobSchedulerCheckEventsOptions.configuration_file.getValue();
        } else {
            LOGGER.debug("configuration_file from scheduler");
            configuration_file = getHibernateConfigurationReporting().toFile().getAbsolutePath();
        }

        jobSchedulerCheckEventsOptions.configuration_file.setValue(configuration_file);
        jobSchedulerCheckEventsOptions.setCurrentNodeName(this.getCurrentNodeName());
        jobSchedulerCheckEventsOptions.setAllOptions(getSchedulerParameterAsProperties());
        jobSchedulerCheckEventsOptions.checkMandatory();
        jobSchedulerCheckEvents.setJSJobUtilites(this);
        jobSchedulerCheckEvents.Execute();
        if (isJobchain()) {
            if (jobSchedulerCheckEvents.exist) {
                spooler_log.debug3("EventExistResult=true");
                spooler_task.order().params().set_var("event_exist_result", "true");
            } else {
                spooler_log.debug3("EventExistResult=false");
                spooler_task.order().params().set_var("event_exist_result", "false");
            }
        }
        success = jobSchedulerCheckEvents.exist && "success".equals(jobSchedulerCheckEventsOptions.handle_existing_as.getValue()) || !jobSchedulerCheckEvents.exist && "success".equals(jobSchedulerCheckEventsOptions.handle_not_existing_as.getValue()) || jobSchedulerCheckEvents.exist
                && "error".equals(jobSchedulerCheckEventsOptions.handle_not_existing_as.getValue()) || !jobSchedulerCheckEvents.exist && "error".equals(jobSchedulerCheckEventsOptions.handle_existing_as.getValue()) || jobSchedulerCheckEvents.exist
                        && !jobSchedulerCheckEventsOptions.handle_existing_as.isDirty();
        if (success) {
            LOGGER.info("....Success:True");
        } else {
            LOGGER.info("....Success:False");
        }
    }

}