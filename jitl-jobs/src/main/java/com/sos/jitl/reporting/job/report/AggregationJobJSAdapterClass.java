package com.sos.jitl.reporting.job.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.scheduler.job.JobSchedulerJobAdapter; // Super-Class for JobScheduler Java-API-Jobs

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/**
 * \class AggregationJobJSAdapterClass - JobScheduler Adapter for "Aggregation"
 * 
 * \brief AdapterClass of AggregationJob for the SOSJobScheduler
 * 
 * This Class AggregationJobJSAdapterClass works as an adapter-class between the
 * SOS JobScheduler and the worker-class AggregationJob.
 * 
 */
public class AggregationJobJSAdapterClass extends JobSchedulerJobAdapter {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(AggregationJobJSAdapterClass.class);

	AggregationJob job = null;
	AggregationJobOptions options = null;
	
	/**
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		job = new AggregationJob();
		options = this.job.Options();
		options.CurrentNodeName(this.getCurrentNodeName());
		options
				.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));

		job.setJSJobUtilites(this);
		
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
		try {
			exit();
			
			super.spooler_exit();
		} catch (Exception ex) {
			spooler_log.warn(ex.getMessage());
		}
	}

}
