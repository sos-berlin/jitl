package com.sos.jitl.managed.job;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;
import sos.spooler.Variable_set;

public class ManagedDatabaseJobJSAdapterClass extends JobSchedulerJobAdapter {

    private ManagedDatabaseJob job;

    @Override
    public boolean spooler_init() {
        try {
            job = new ManagedDatabaseJob();
            ManagedDatabaseJobOptions options = job.getOptions();
            options.setCurrentNodeName(getCurrentNodeName());
            options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
            job.setJSJobUtilites(this);
            job.setJSCommands(this);
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {

        try {
            super.spooler_process();

            ManagedDatabaseJobOptions options = job.getOptions();
            options.setCurrentNodeName(getCurrentNodeName());
            options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));

            Variable_set orderParams = null;
            Order order = spooler_task.order();
            if (order != null) {
                orderParams = order.params();
            }
            job.execute(spooler_task.job().order_queue() != null, orderParams);
            if (job.getModel().getWarning() != null) {
                spooler_log.warn(job.getModel().getWarning());
            }
        } catch (Exception e) {
            throw new JobSchedulerException(String.format("Exception: %s", e.toString()), e);
        }
        return signalSuccess();
    }
}
