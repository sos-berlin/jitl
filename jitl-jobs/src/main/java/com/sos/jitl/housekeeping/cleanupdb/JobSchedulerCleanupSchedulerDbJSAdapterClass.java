package com.sos.jitl.housekeeping.cleanupdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.IJSCommands;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.util.SOSString;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerCleanupSchedulerDbJSAdapterClass.class);

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
        JobSchedulerCleanupSchedulerDbOptions objO = objR.getOptions();
        Object objSp = objJSCommands.getSpoolerObject();

        objO.setAllOptions(getSchedulerParameterAsProperties());

        if(SOSString.isEmpty(objO.hibernate_configuration_file_scheduler.getValue())){
        	objO.hibernate_configuration_file_scheduler.setValue(getHibernateConfigurationScheduler().toString());
		}
        if(SOSString.isEmpty(objO.hibernate_configuration_file_reporting.getValue())){
        	objO.hibernate_configuration_file_reporting.setValue(getHibernateConfigurationReporting().toString());
		}
        
        objO.checkMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    } // doProcessing

}
