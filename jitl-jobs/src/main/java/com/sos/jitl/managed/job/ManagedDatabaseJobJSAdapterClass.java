package com.sos.jitl.managed.job;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSString;

public class ManagedDatabaseJobJSAdapterClass extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedDatabaseJobJSAdapterClass.class);
    private ManagedDatabaseJob job;

    @Override
    public boolean spooler_init() {
        try {
            job = new ManagedDatabaseJob();
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
            options.setCurrentNodeName(getCurrentNodeName(getSpoolerProcess().getOrder(), true));
            options.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));

            if (SOSString.isEmpty(options.command.getValue())) {
                LOGGER.debug("\"command\" parameter is empty. set command from job script...");

                String jobScript = getJobScript();
                if (SOSString.isEmpty(jobScript)) {
                    throw new Exception("command is empty. please check the job/order \"command\" parameter or the job script element.");
                } else {
                    // replace \${ to ${
                    jobScript = jobScript.replaceAll(Pattern.quote("\\${"), "\\${");
                    options.command.setValue(replaceSchedulerVars(jobScript));
                }
            }

            Variable_set orderParams = null;
            Order order = spooler_task.order();
            if (order != null) {
                orderParams = order.params();
            }

            job.execute(spooler_task.job().order_queue() != null, orderParams);
            if (job.getModel().getWarning() != null) {
                spooler_log.warn(job.getModel().getWarning());
            }
            return getSpoolerProcess().getSuccess();
        } catch (Exception e) {
            throw new JobSchedulerException(String.format("Exception: %s", e.toString()), e);
        }
    }
}
