package com.sos.jitl.eventing.checkevents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class JobSchedulerCheckEventsJSAdapterClass extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerCheckEventsJSAdapterClass.class);
    private boolean success = false;

    @Override
    public boolean spooler_process() throws Exception {
        super.spooler_process();
        doProcessing();

        if (success) {
            return getSpoolerProcess().getSuccess();
        } else {
            return false;
        }
    }

    private void doProcessing() throws Exception {
        JobSchedulerCheckEvents jobSchedulerCheckEvents = new JobSchedulerCheckEvents();
        JobSchedulerCheckEventsOptions jobSchedulerCheckEventsOptions = jobSchedulerCheckEvents.getOptions();
        String configurationFile = this.getHibernateConfigurationReporting().toFile().getAbsolutePath();

        jobSchedulerCheckEventsOptions.configuration_file.setValue(configurationFile);
        jobSchedulerCheckEventsOptions.setCurrentNodeName(this.getCurrentNodeName(getSpoolerProcess().getOrder(), true));
        jobSchedulerCheckEventsOptions.setAllOptions(getSchedulerParameterAsProperties());
        jobSchedulerCheckEventsOptions.checkMandatory();
        jobSchedulerCheckEvents.setJSJobUtilites(this);
        jobSchedulerCheckEvents.Execute();
        if (getSpoolerProcess().getOrder() != null) {
            if (jobSchedulerCheckEvents.exist) {
                spooler_log.debug3("EventExistResult=true");
                getSpoolerProcess().getOrder().params().set_var("event_exist_result", "true");
            } else {
                spooler_log.debug3("EventExistResult=false");
                getSpoolerProcess().getOrder().params().set_var("event_exist_result", "false");
            }
        }
        success = jobSchedulerCheckEvents.exist && "success".equals(jobSchedulerCheckEventsOptions.handle_existing_as.getValue())
                || !jobSchedulerCheckEvents.exist && "success".equals(jobSchedulerCheckEventsOptions.handle_not_existing_as.getValue())
                || jobSchedulerCheckEvents.exist && "error".equals(jobSchedulerCheckEventsOptions.handle_not_existing_as.getValue())
                || !jobSchedulerCheckEvents.exist && "error".equals(jobSchedulerCheckEventsOptions.handle_existing_as.getValue())
                || jobSchedulerCheckEvents.exist && !jobSchedulerCheckEventsOptions.handle_existing_as.isDirty();
        if (success) {
            LOGGER.info("....Success:True");
        } else {
            LOGGER.info("....Success:False");
        }
    }

}