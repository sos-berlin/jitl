package com.sos.jitl.latecomers;

import com.sos.jitl.latecomers.JobSchedulerStartLatecomers;
import com.sos.jitl.latecomers.JobSchedulerStartLatecomersOptions;
import sos.scheduler.job.JobSchedulerJobAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSchedulerStartLatecomersJSAdapterClass extends JobSchedulerJobAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerStartLatecomersJSAdapterClass.class);

	@Override
	public boolean spooler_process() throws Exception {
		try {
			super.spooler_process();
			doProcessing();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw(e);
		}
		return signalSuccess();
	}

	
	private void doProcessing() throws Exception {
		JobSchedulerStartLatecomers jobSchedulerStartLatecomers = new JobSchedulerStartLatecomers();
		JobSchedulerStartLatecomersOptions jobSchedulerStartLatecomersOptions = jobSchedulerStartLatecomers
				.getOptions();
		jobSchedulerStartLatecomersOptions.setCurrentNodeName(this.getCurrentNodeName());
		jobSchedulerStartLatecomersOptions.setAllOptions(getSchedulerParameterAsProperties());
		jobSchedulerStartLatecomersOptions.checkMandatory();
		jobSchedulerStartLatecomers.setJSJobUtilites(this);
        jobSchedulerStartLatecomers.setJSCommands(this);
		jobSchedulerStartLatecomers.execute();
	}
}
