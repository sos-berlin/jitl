package com.sos.jitl.eventing.checkevents;

import java.io.File;

import com.sos.jitl.eventing.checkevents.JobSchedulerCheckEvents;
import com.sos.jitl.eventing.checkevents.JobSchedulerCheckEventsOptions;
import sos.spooler.Spooler;
import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler
// Java-API-Jobs
import org.apache.log4j.Logger;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.localization.*;

/** \class JobSchedulerCheckEventsJSAdapterClass - JobScheduler Adapter for
 * "Check if events exist"
 *
 * \brief AdapterClass of JobSchedulerCheckEvents for the SOSJobScheduler
 *
 * This Class JobSchedulerCheckEventsJSAdapterClass works as an adapter-class
 * between the SOS JobScheduler and the worker-class JobSchedulerCheckEvents.
 *
 * 
 *
 *
 * \verbatim ; \endverbatim */
public class JobSchedulerCheckEventsJSAdapterClass extends JobSchedulerJobAdapter {

    private final String conClassName = "JobSchedulerCheckEventsJSAdapterClass";
    private static Logger logger = Logger.getLogger(JobSchedulerCheckEventsJSAdapterClass.class);
    private boolean success = false;

    public void init() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::init";
        doInitialize();
    }

    private void doInitialize() {
    } // doInitialize

    @Override
    public boolean spooler_init() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_init";
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_process";

        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        } finally {
        } // finally

        if (success) {
            return signalSuccess();
        } else {
            return false;
            // return signalFailure();
        }

    } // spooler_process

    @Override
    public void spooler_exit() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_exit";
        super.spooler_exit();
    }

    private void doProcessing() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::doProcessing";

        Object objSp = getSpoolerObject();

        JobSchedulerCheckEvents objR = new JobSchedulerCheckEvents();
        JobSchedulerCheckEventsOptions objO = objR.getOptions();

        String configuration_file = "";
        Spooler objSpooler = (Spooler) objSp;

        if (objO.configuration_file.IsEmpty() == false) {
            logger.debug("configuration_file from param");
            configuration_file = objO.configuration_file.Value();
        } else {
            logger.debug("configuration_file from scheduler");
            File f = new File(new File(objSpooler.configuration_directory()).getParent(), "hibernate.cfg.xml");
            if (!f.exists()) {
                f = new File(new File(objSpooler.directory()), "config/hibernate.cfg.xml");
            }
            configuration_file = f.getAbsolutePath();
        }
        objO.configuration_file.Value(configuration_file);

        objO.CurrentNodeName(this.getCurrentNodeName());
        objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
        objO.CheckMandatory();
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

        success = (objR.exist && objO.handle_existing_as.Value().equals("success") || !objR.exist
                && objO.handle_not_existing_as.Value().equals("success") || objR.exist && objO.handle_not_existing_as.Value().equals("error")
                || !objR.exist && objO.handle_existing_as.Value().equals("error") || objR.exist && !objO.handle_existing_as.isDirty());
        if (success) {
            logger.info("....Success:True");
        } else {
            logger.info("....Success:False");
        }

    } // doProcessing

}
