package com.sos.jitl.reporting.job.report;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class AggregationJobJSAdapterClass extends JobSchedulerJobAdapter {
	
	@Override
	public boolean spooler_process() throws Exception {
		AggregationJob job = new AggregationJob();
		try {
			super.spooler_process();
			
			job = new AggregationJob();
			AggregationJobOptions options = job.Options();
			options.CurrentNodeName(this.getCurrentNodeName());
			options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
			job.setJSJobUtilites(this);
			job.setJSCommands(this);

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
