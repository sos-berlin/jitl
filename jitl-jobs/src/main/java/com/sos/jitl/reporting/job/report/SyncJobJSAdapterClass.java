package com.sos.jitl.reporting.job.report;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.scheduler.job.JobSchedulerJobAdapter; // Super-Class for JobScheduler Java-API-Jobs
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/**
 * \class SyncJobJSAdapterClass - JobScheduler Adapter for "Sync"
 * 
 * \brief AdapterClass of SyncJob for the SOSJobScheduler
 * 
 * This Class SyncJobJSAdapterClass works as an adapter-class between the
 * SOS JobScheduler and the worker-class SyncJob.
 * 
 */
public class SyncJobJSAdapterClass extends JobSchedulerJobAdapter {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(SyncJobJSAdapterClass.class);

	SyncJob job = null;
	SyncJobOptions options = null;
	
	/**
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		job = new SyncJob();
		options = this.job.Options();
		options.CurrentNodeName(this.getCurrentNodeName());
		options
				.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));

		job.setJSJobUtilites(this);
		
		if(SOSString.isEmpty(options.hibernate_configuration_file_scheduler.Value())){
			File f = new File(new File(spooler.configuration_directory()).getParent(), "hibernate.cfg.xml");
			options.hibernate_configuration_file_scheduler.Value(f.getCanonicalPath());
		}
		job.init();
	}

	
	public void setVariable(String name,String value) throws Exception{
		Order order = spooler_task.order();
		Variable_set params = spooler.create_variable_set();
		params.merge(spooler_task.params());
		params.merge(order.params());
		order.params().set_var(name, value);
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
			
			if(job.getModel().getCounterSynchronizeNew().getTriggers() > 0 ||
					job.getModel().getCounterSynchronizeOld().getTriggers() > 0		
					){
						//see FactJobOptions
				setVariable("do_process","true");
						
			}
			
			
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
		try {
			exit();
			
			super.spooler_exit();
		} catch (Exception ex) {
			spooler_log.warn(ex.getMessage());
		}
	}

}
