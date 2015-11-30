package com.sos.jitl.reporting.job.inventory;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class InventoryJobJSAdapterClass extends JobSchedulerJobAdapter {

	@Override
	public boolean spooler_process() throws Exception {
		
		InventoryJob job = new InventoryJob();
		try {
			super.spooler_process();
						
			InventoryJobOptions options = job.getOptions();
			options.CurrentNodeName(this.getCurrentNodeName());
			options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
			job.setJSJobUtilites(this);
			job.setJSCommands(this);
			
			if(SOSString.isEmpty(options.current_scheduler_configuration_directory.Value())){
				options.current_scheduler_configuration_directory.Value(spooler.configuration_directory());
			}
			if(SOSString.isEmpty(options.current_scheduler_id.Value())){
				options.current_scheduler_id.Value(spooler.id());
			}
			if(SOSString.isEmpty(options.current_scheduler_hostname.Value())){
				options.current_scheduler_hostname.Value(spooler.hostname());
			}
			if(SOSString.isEmpty(options.current_scheduler_port.Value())){
				if(spooler.tcp_port() > 0){
					options.current_scheduler_port.value(spooler.tcp_port());
				}
				else if(spooler.udp_port() > 0){
					options.current_scheduler_port.value(spooler.udp_port());
				}
			}
			job.Options(options);
			
			job.init();
			job.execute();
		} catch (Exception e) {
			throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
		}
		finally{
			job.exit();
		}
		return signalSuccess();

	}
}
