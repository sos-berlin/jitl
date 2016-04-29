package sos.scheduler.managed.configuration;

import sos.spooler.Order;

/** @author andreas pueschel */
public class JobSchedulerConfigurationPreparationJob extends ConfigurationOrderJob {

    public boolean spooler_process() {
        Order order = null;
        String orderId = "(none)";
        try {
            try {
                if (spooler_job.order_queue() != null) {
                    order = spooler_task.order();
                    orderId = order.id();
                    if (order.params().value("configuration_path") != null && !order.params().value("configuration_path").isEmpty()) {
                        this.setConfigurationPath(order.params().value("configuration_path"));
                    } else if (spooler_task.params().value("configuration_path") != null
                            && !spooler_task.params().value("configuration_path").isEmpty()) {
                        this.setConfigurationPath(spooler_task.params().value("configuration_path"));
                    }
                    if (order.params().value("configuration_file") != null && !order.params().value("configuration_file").isEmpty()) {
                        this.setConfigurationFilename(order.params().value("configuration_file"));
                    } else if (spooler_task.params().value("configuration_file") != null
                            && !spooler_task.params().value("configuration_file").isEmpty()) {
                        this.setConfigurationFilename(spooler_task.params().value("configuration_file"));
                    }
                }
                this.initConfiguration();
            } catch (Exception e) {
                throw new Exception("error occurred preparing order: " + e.getMessage());
            }
            return (spooler_task.job().order_queue() != null) ? true : false;
        } catch (Exception e) {
            spooler_log.warn("error occurred processing order [" + orderId + "]: " + e.getMessage());
            return false;
        }
    }

}