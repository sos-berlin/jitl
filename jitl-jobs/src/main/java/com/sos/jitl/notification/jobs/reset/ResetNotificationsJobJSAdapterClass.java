package com.sos.jitl.notification.jobs.reset;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.util.SOSString;

public class ResetNotificationsJobJSAdapterClass extends JobSchedulerJobAdapter {

    private ResetNotificationsJob job;

    @Override
    public boolean spooler_init() {
        try {
            job = new ResetNotificationsJob();
            ResetNotificationsJobOptions options = job.getOptions();
            job.setJSJobUtilites(this);
            job.setJSCommands(this);

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

            ResetNotificationsJobOptions options = job.getOptions();
            options.setCurrentNodeName(getCurrentNodeName(getSpoolerProcess().getOrder(), true));
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
