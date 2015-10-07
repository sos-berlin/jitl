package com.sos.jitl.extract.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler Java-API-Jobs

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class CSV2CSVJobJSAdapterClass extends JobSchedulerJobAdapter  {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(CSV2CSVJobJSAdapterClass.class);

	CSV2CSVJob job = null;
	CSV2CSVJobOptions options = null;
	
	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		job = new CSV2CSVJob();
		options = job.getOptions();
		options.CurrentNodeName(this.getCurrentNodeName());
		options.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
	    job.setJSJobUtilites(this);
	}
	
	/**
	 * 
	 */
	@Override
	public boolean spooler_init() {
		try{
			this.init();
		}
		catch(Exception ex){
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
		}
		catch (Exception e) {
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
	}
}

