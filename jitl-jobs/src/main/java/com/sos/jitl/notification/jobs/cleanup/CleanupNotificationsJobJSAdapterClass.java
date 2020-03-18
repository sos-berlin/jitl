package com.sos.jitl.notification.jobs.cleanup;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.util.SOSString;

public class CleanupNotificationsJobJSAdapterClass extends JobSchedulerJobAdapter {

    private CleanupNotificationsJob job;

    @Override
    public boolean spooler_init() {
        try {
            super.spooler_init();

            job = new CleanupNotificationsJob();
            CleanupNotificationsJobOptions options = job.getOptions();
            if (SOSString.isEmpty(options.hibernate_configuration_file_reporting.getValue())) {
                options.hibernate_configuration_file_reporting.setValue(getHibernateConfigurationReporting().toString());
            }

            job.init();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.toString(), e);
        }
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {

        try {
            super.spooler_process();

            CleanupNotificationsJobOptions options = job.getOptions();
            options.setCurrentNodeName(getCurrentNodeName(getSpoolerProcess().getOrder(), false));
            options.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters(getSpoolerProcess().getOrder())));

            job.openSession();
            job.execute();
            return getSpoolerProcess().getSuccess();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.toString(), e);
        } finally {
            job.closeSession();
        }
    }

    @Override
    public void spooler_close() throws Exception {
        if (job != null) {
            job.exit();
        }
        super.spooler_close();
    }
}
