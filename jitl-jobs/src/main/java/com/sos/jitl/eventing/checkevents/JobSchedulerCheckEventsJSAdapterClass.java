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
        Object objSp = getSpoolerObject();
        JobSchedulerCheckEvents objR = new JobSchedulerCheckEvents();
        JobSchedulerCheckEventsOptions objO = objR.getOptions();
        String configuration_file = "";
        Spooler objSpooler = (Spooler) objSp;
        if (!objO.configuration_file.IsEmpty()) {
            LOGGER.debug("configuration_file from param");
            configuration_file = objO.configuration_file.getValue();
        } else {
            LOGGER.debug("configuration_file from scheduler");
            File f = new File(new File(objSpooler.configuration_directory()).getParent(), "hibernate.cfg.xml");
            if (!f.exists()) {
                f = new File(new File(objSpooler.directory()), "config/hibernate.cfg.xml");
            }
            configuration_file = f.getAbsolutePath();
        }
        objO.configuration_file.setValue(configuration_file);
        objO.setCurrentNodeName(this.getCurrentNodeName());
        objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
        objO.checkMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
        if (isJobchain()) {
            if (objR.exist) {
                spooler_log.debug3("EventExistResult=true");
                spooler_task.order().params().set_var("event_exist_result", "true");
            } else {
                spooler_log.debug3("EventExistResult=false");
                spooler_task.order().params().set_var("event_exist_result", "false");
            }
        }
        success =
                objR.exist && "success".equals(objO.handle_existing_as.getValue()) || !objR.exist
                        && "success".equals(objO.handle_not_existing_as.getValue()) || objR.exist && "error".equals(objO.handle_not_existing_as.getValue())
                        || !objR.exist && "error".equals(objO.handle_existing_as.getValue()) || objR.exist && !objO.handle_existing_as.isDirty();
        if (success) {
            LOGGER.info("....Success:True");
        } else {
            LOGGER.info("....Success:False");
        }
    }

}