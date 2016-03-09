package com.sos.jitl.housekeeping.cleanupdb;

import java.io.File;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Spooler;

import com.sos.JSHelper.Basics.IJSCommands;

// Super-Class for JobScheduler Java-API-Jobs

/** \class JobSchedulerCleanupSchedulerDbJSAdapterClass - JobScheduler Adapter
 * for "Delete log entries in the Job Scheduler history Databaser tables"
 *
 * \brief AdapterClass of JobSchedulerCleanupSchedulerDb for the SOSJobScheduler
 *
 * This Class JobSchedulerCleanupSchedulerDbJSAdapterClass works as an
 * adapter-class between the SOS JobScheduler and the worker-class
 * JobSchedulerCleanupSchedulerDb.
 *
 * 
 *
 * see \see C:\Dokumente und Einstellungen\Uwe Risse\Lokale
 * Einstellungen\Temp\scheduler_editor-3271913404894833399.html for more
 * details.
 *
 * \verbatim ; mechanicaly created by C:\Dokumente und Einstellungen\Uwe
 * Risse\Eigene Dateien\sos-berlin.com\jobscheduler\scheduler_ur_current\config\
 * JOETemplates\java\xsl\JSJobDoc2JSAdapterClass.xsl from
 * http://www.sos-berlin.com at 20121211160841 \endverbatim */
public class JobSchedulerCleanupSchedulerDbJSAdapterClass extends JobSchedulerJobAdapter {

    private final String conClassName = "JobSchedulerCleanupSchedulerDbJSAdapterClass";  //$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JobSchedulerCleanupSchedulerDbJSAdapterClass.class);

    public void init() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
        doInitialize();
    }

    private void doInitialize() {
    } // doInitialize

    @Override
    public boolean spooler_init() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_init"; //$NON-NLS-1$
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_process"; //$NON-NLS-1$

        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            throw e;
        } finally {
        } // finally

        return spooler_task.job().order_queue() != null;

    } // spooler_process

    @Override
    public void spooler_exit() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_exit"; //$NON-NLS-1$
        super.spooler_exit();
    }

    private void doProcessing() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::doProcessing"; //$NON-NLS-1$
        IJSCommands objJSCommands = this;

        JobSchedulerCleanupSchedulerDb objR = new JobSchedulerCleanupSchedulerDb();
        JobSchedulerCleanupSchedulerDbOptions objO = objR.Options();
        Object objSp = objJSCommands.getSpoolerObject();

        objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));

        String hibernate_configuration_file = "";
        Spooler objSpooler = (Spooler) objSp;

        if (objO.getItem("hibernate_configuration_file") != null) {
            logger.debug("hibernate_configuration_file from param");
            hibernate_configuration_file = objO.hibernate_configuration_file.Value();
        } else {
            logger.debug("hibernate_configuration_file from scheduler");
            File f = new File(new File(objSpooler.configuration_directory()).getParent(), "hibernate.cfg.xml");
            hibernate_configuration_file = f.getAbsolutePath();
        }
        objO.hibernate_configuration_file.Value(hibernate_configuration_file);

        objO.CheckMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    } // doProcessing

}
