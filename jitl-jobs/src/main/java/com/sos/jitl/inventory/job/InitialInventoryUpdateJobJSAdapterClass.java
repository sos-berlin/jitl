package com.sos.jitl.inventory.job;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Supervisor_client;
import sos.spooler.Variable_set;

public class InitialInventoryUpdateJobJSAdapterClass extends JobSchedulerJobAdapter {
	private static final Logger LOGGER = Logger.getLogger(InitialInventoryUpdateJobJSAdapterClass.class);
    private static final String COMMAND = 
            "<show_state subsystems=\"folder\" what=\"folders cluster no_subfolders\" path=\"/any/path/that/does/not/exists\" />";
    private static final String HIBERNATE_CONFIG_PATH_APPENDER = "config/hibernate.cfg.xml";

	@Override
	public boolean spooler_init() {
        InitialInventoryUpdateJob updateEntriesJob = new InitialInventoryUpdateJob();
        updateEntriesJob.setAnswerXml(executeXML());
        updateEntriesJob.setLiveDirectory(getLiveDirectory());
        InitialInventoryUpdateJobOptions updateEntriesOptions = updateEntriesJob.getOptions();
        String schedulerDataPath = spooler.directory();
        if(!schedulerDataPath.endsWith("/")) {
            schedulerDataPath = schedulerDataPath + "/";
        }
        updateEntriesOptions.schedulerHibernateConfigurationFile.setValue(schedulerDataPath + HIBERNATE_CONFIG_PATH_APPENDER);
        Supervisor_client supervisor = spooler.supervisor_client();
        if(supervisor != null) {
            updateEntriesJob.setSupervisorHost(supervisor.hostname());
            updateEntriesJob.setSupervisorPort(String.valueOf(supervisor.tcp_port()));
        }
        Variable_set spoolerVariables = spooler.variables();
        String proxyUrl = spoolerVariables.value("proxy_url");
        if(proxyUrl != null && !proxyUrl.isEmpty()) {
            updateEntriesJob.setProxyUrl(proxyUrl);
        }
        Thread startProcessing = new Thread() {
            @Override
            public void run() {
                try {
                    updateEntriesJob.init();
                    updateEntriesJob.execute();
                } catch (Exception e) {
                    LOGGER.error(e);
                } finally {
                    updateEntriesJob.exit();
                }
                super.run();
            }                
        };
        startProcessing.start();
		return true;
	}

    private String executeXML() {
        return spooler.execute_xml(COMMAND);
    }
    
	private String getLiveDirectory() {
	    return spooler.configuration_directory();
	}
	
}