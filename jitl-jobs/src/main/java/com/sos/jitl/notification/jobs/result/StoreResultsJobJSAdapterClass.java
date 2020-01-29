package com.sos.jitl.notification.jobs.result;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.notification.db.DBLayer;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;
import sos.util.SOSString;

public class StoreResultsJobJSAdapterClass extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreResultsJobJSAdapterClass.class);

    StoreResultsJob job = null;
    StoreResultsJobOptions options = null;

    public void init() throws Exception {

        job = new StoreResultsJob();
        options = job.getOptions();
        options.setCurrentNodeName(this.getCurrentNodeName());
        options.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
        job.setJSJobUtilites(this);

        if (SOSString.isEmpty(options.scheduler_notification_hibernate_configuration_file.getValue())) {
            options.scheduler_notification_hibernate_configuration_file.setValue(getHibernateConfigurationReporting().toString());
        }
        job.init();
        job.openSession();
    }

    public void exit() throws Exception {
        if (job != null) {
            job.closeSession();
            job.exit();
        }
    }

    private void doProcessing(boolean isStandalone) throws Exception {

        Order order = spooler_task.order();
        if (!isStandalone) {
            if (order == null || order.job_chain() == null || order.job_chain_node() == null) {
                LOGGER.info(String.format("exit processing. object is null: order = %s, order.job_chain = %s, order.job_chain_node = %s", order, order
                        .job_chain(), order.job_chain_node()));
                return;
            }
        }

        HashMap<String, String> params = this.getParameters();
        if (params != null && params.size() > 0) {
            init();

            options.mon_results_standalone.value(isStandalone);
            options.mon_results_scheduler_id.setValue(spooler.id());
            options.mon_results_task_id.value(spooler_task.id());

            if (isStandalone) {
                options.mon_results_order_history_id.setValue("0");
                options.mon_results_order_id.setValue(DBLayer.DEFAULT_EMPTY_NAME);
                options.mon_results_job_chain_name.setValue(DBLayer.DEFAULT_EMPTY_NAME);
                options.mon_results_order_step_state.setValue(DBLayer.DEFAULT_EMPTY_NAME);

            } else {
                options.mon_results_order_history_id.setValue(order.history_id());
                options.mon_results_order_id.setValue(order.id());
                options.mon_results_job_chain_name.setValue(order.job_chain().path());
                options.mon_results_order_step_state.setValue(order.job_chain_node().state());
            }
            job.execute();
            this.exit();
        }
    }

    /** standalone jobs */
    @Override
    public void spooler_task_after() throws Exception {
        try {
            super.spooler_task_after();
        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                if (spooler_task.job().order_queue() == null) {
                    doProcessing(true);
                }
            } catch (Exception ex) {
                spooler_log.warn(ex.getMessage());
            }
        }
    }

    /** order jobs */
    @Override
    public boolean spooler_process_after(boolean processResult) throws Exception {
        boolean result = false;
        try {
            result = super.spooler_process_after(processResult);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                if (spooler_task.job().order_queue() != null) {
                    doProcessing(false);
                }
            } catch (Exception ex) {
                spooler_log.warn(ex.getMessage());
            }
        }
        return result;
    }
}
