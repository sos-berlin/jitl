package com.sos.jitl.reporting.job.inventory;

import com.sos.exception.SOSException;

import sos.scheduler.command.SOSSchedulerCommand;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.util.SOSString;

public class InventoryJobJSAdapterClass extends JobSchedulerJobAdapter {

    private static final String FULL_COMMAND = "<show_state what=\"cluster source job_chains job_chain_orders schedules\" />";

    @Override
    public boolean spooler_process() throws Exception {

        InventoryJob job = new InventoryJob();
        try {
            super.spooler_process();

            InventoryJobOptions options = job.getOptions();
            options.setCurrentNodeName(getCurrentNodeName(getSpoolerProcess().getOrder(), false));
            options.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));
            job.setJSJobUtilites(this);
            job.setJSCommands(this);

            if (SOSString.isEmpty(options.current_scheduler_configuration_directory.getValue())) {
                options.current_scheduler_configuration_directory.setValue(spooler.configuration_directory());
            }
            if (SOSString.isEmpty(options.current_scheduler_id.getValue())) {
                options.current_scheduler_id.setValue(spooler.id());
            }
            if (SOSString.isEmpty(options.current_scheduler_hostname.getValue())) {
                options.current_scheduler_hostname.setValue(spooler.hostname());
            }

            int httpPort = SOSSchedulerCommand.getHTTPPortFromScheduler(spooler);
            if (SOSString.isEmpty(options.current_scheduler_port.getValue())) {
                if (httpPort > 0) {
                    options.current_scheduler_port.value(httpPort);
                }
            }
            job.getOptions(options);
            job.setAnswerXml(executeXml());
            job.init();
            job.execute();
            return getSpoolerProcess().isOrderJob();
        } catch (Exception e) {
            throw new SOSException("Fatal Error:" + e.getMessage(), e);
        } finally {
            job.exit();
        }
    }

    private String executeXml() {
        return spooler.execute_xml(FULL_COMMAND);
    }

}