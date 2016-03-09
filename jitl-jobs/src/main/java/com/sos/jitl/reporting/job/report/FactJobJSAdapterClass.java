package com.sos.jitl.reporting.job.report;

import sos.scheduler.job.JobSchedulerJobAdapter; // Super-Class for JobScheduler
                                                 // Java-API-Jobs
import sos.spooler.Order;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class FactJobJSAdapterClass extends JobSchedulerJobAdapter {

    public void setVariable(String name, String value) throws Exception {
        Order order = spooler_task.order();
        Variable_set params = spooler.create_variable_set();
        params.merge(spooler_task.params());
        params.merge(order.params());
        order.params().set_var(name, value);
    }

    @Override
    public boolean spooler_process() throws Exception {

        FactJob job = new FactJob();
        try {
            super.spooler_process();

            FactJobOptions options = job.getOptions();
            options.CurrentNodeName(this.getCurrentNodeName());
            options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
            job.setJSJobUtilites(this);
            job.setJSCommands(this);

            job.init();
            job.execute();

            if (job.getModel().getCounterSynchronizeNew().getTriggers() > 0 || job.getModel().getCounterSynchronizeOld().getTriggers() > 0) {
                setVariable(AggregationJobOptions.VARIABLE_EXECUTE_AGGREGATION, "true");
            } else {
                setVariable(AggregationJobOptions.VARIABLE_EXECUTE_AGGREGATION, "false");
            }
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        } finally {
            job.exit();
        }
        return signalSuccess();

    }
}
