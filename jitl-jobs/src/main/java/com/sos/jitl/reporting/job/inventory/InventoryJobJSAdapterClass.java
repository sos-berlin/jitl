package com.sos.jitl.reporting.job.inventory;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class InventoryJobJSAdapterClass extends JobSchedulerJobAdapter {

    private static final String GET_STATE = 
            "<show_state subsystems=\"folder\" what=\"folders cluster no_subfolders\" path=\"/any/path/that/does/not/exists\" />";
    private static final String FULL_COMMAND = "<show_state what=\"cluster source job_chains job_chain_orders schedules\" />";

    @Override
    public boolean spooler_process() throws Exception {

        InventoryJob job = new InventoryJob();
        try {
            super.spooler_process();

            InventoryJobOptions options = job.getOptions();
            options.setCurrentNodeName(this.getCurrentNodeName());
            options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
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
            if (SOSString.isEmpty(options.current_scheduler_port.getValue())) {
                if (spooler.tcp_port() > 0) {
                    options.current_scheduler_port.value(spooler.tcp_port());
                } else if (spooler.udp_port() > 0) {
                    options.current_scheduler_port.value(spooler.udp_port());
                }
            }
            job.getOptions(options);
            job.setAnswerXml(executeXml());
            job.init();
            job.execute();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        } finally {
            job.exit();
        }
        return signalSuccess();
    }

    private String executeXml () {
        return spooler.execute_xml(FULL_COMMAND);
    }
    
}