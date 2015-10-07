package com.sos.jitl.reporting.job.inventory;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter; // Super-Class for JobScheduler Java-API-Jobs
import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/**
 * \class InventoryJobJSAdapterClass - JobScheduler Adapter for "Inventory"
 * 
 * \brief AdapterClass of InventoryJob for the SOSJobScheduler
 * 
 * This Class InventoryJobJSAdapterClass works as an adapter-class between the
 * SOS JobScheduler and the worker-class InventoryJob.
 * 
 */
public class InventoryJobJSAdapterClass extends JobSchedulerJobAdapter {
	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(InventoryJobJSAdapterClass.class);

	InventoryJob job = null;
	InventoryJobOptions options = null;
	
	/**
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		job = new InventoryJob();
		options = this.job.getOptions();
		options.CurrentNodeName(this.getCurrentNodeName());
		options
				.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));

		job.setJSJobUtilites(this);
		
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
		
		job.init();
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void exit() throws Exception {
		if (job != null) {
			job.exit();
		}
	}

	/**
	 * 
	 */
	@Override
	public boolean spooler_init() {
		try {
			init();
		} catch (Exception ex) {
			spooler_log.error(ex.getMessage());
			return false;
		}

		return super.spooler_init();
	}

	/**
	 * 
	 */
	@Override
	public boolean spooler_process() throws Exception {
		try {
			super.spooler_process();
			job.Execute();
		} catch (Exception e) {
			throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
		}
		return signalSuccess();

	}

	/**
	 * 
	 */
	@Override
	public void spooler_exit() {
		super.spooler_exit();

		try {
			exit();
		} catch (Exception ex) {
			spooler_log.warn(ex.getMessage());
		}
	}

}
