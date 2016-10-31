package com.sos.jitl.runonce.job;

import java.util.HashMap;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class InsertOrUpdateInventoryInstanceEntriesJobJSAdapterClass extends JobSchedulerJobAdapter {
	private static final Logger LOGGER = Logger.getLogger(InsertOrUpdateInventoryInstanceEntriesJobJSAdapterClass.class);
    private static final String COMMAND = "<show_state subsystems=\"folder\" what=\"folders cluster no_subfolders\" path=\"/any/path/that/does/not/exists\" />";

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
	        HashMap<String, String> schedulerParamsAsProps = getSchedulerParameterAsProperties();
	        updateEntriesOptions.setAllOptions(schedulerParamsAsProps);
	        updateEntriesOptions.checkMandatory();
	        updateEntriesJob.setJSJobUtilites(this);
	        updateEntriesJob.setLiveDirectory(getLiveDirectory());
	        updateEntriesJob.setSupervisorHost(schedulerParamsAsProps.get("SCHEDULER_SUPERVISOR_HOST"));
	        updateEntriesJob.setSupervisorPort(schedulerParamsAsProps.get("SCHEDULER_SUPERVISOR_PORT"));
	        if(updateEntriesOptions.getProxyUrl().getValue() != null && !updateEntriesOptions.getProxyUrl().getValue().isEmpty()) {
	            updateEntriesJob.setProxyUrl(updateEntriesOptions.getProxyUrl().getValue());
	        }
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