package com.sos.jitl.runonce.job;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class InsertOrUpdateInventoryInstanceEntriesJobJSAdapterClass extends JobSchedulerJobAdapter {
	private static final Logger LOGGER = Logger.getLogger(InsertOrUpdateInventoryInstanceEntriesJobJSAdapterClass.class);
    private static final String COMMAND = "<show_state subsystems=\"folder process_class\" what=\"folders cluster\" />";

	@Override
	public boolean spooler_init() {
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() throws Exception {
        InsertOrUpdateInventoryInstanceEntriesJob updateEntriesJob = new InsertOrUpdateInventoryInstanceEntriesJob();
		try {
			super.spooler_process();
	        updateEntriesJob.setAnswerXml(executeXML());
	        InsertOrUpdateInventoryInstanceEntriesOptions updateEntriesOptions = updateEntriesJob.getOptions();
	        updateEntriesOptions.setCurrentNodeName(this.getCurrentNodeName());
	        updateEntriesOptions.setAllOptions(getSchedulerParameterAsProperties());
	        updateEntriesOptions.checkMandatory();
	        updateEntriesJob.setJSJobUtilites(this);
	        updateEntriesJob.setLiveDirectory(getLiveDirectory());
	        updateEntriesJob.init();
	        updateEntriesJob.execute();
		} catch (Exception e) {
            throw new JobSchedulerException("Fatal Error in "+ InsertOrUpdateInventoryInstanceEntriesJobJSAdapterClass.class.getSimpleName() + " :"
                    + e.getCause() + ":" + e.getMessage(), e);
   		} finally {
   		    updateEntriesJob.exit();
		} 
        return signalSuccess();
	}  

    private String executeXML() {
        return spooler.execute_xml(COMMAND);
    }
    
	private String getLiveDirectory() {
	    return spooler.configuration_directory();
	}

}