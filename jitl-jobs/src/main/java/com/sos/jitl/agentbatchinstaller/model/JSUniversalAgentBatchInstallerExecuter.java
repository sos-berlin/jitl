/*
 * Created on 28.02.2011
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.sos.jitl.agentbatchinstaller.model;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.sos.jitl.agentbatchinstaller.JSUniversalAgentBatchInstaller;
import com.sos.jitl.agentbatchinstaller.model.installations.Installation;

import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Spooler;
import sos.xml.SOSXMLXPath;

public class JSUniversalAgentBatchInstallerExecuter {

	private static final String XPATH_TARGET_DIRECTORY = "//Profiles/Profile[@profile_id='192.11.0.116:4445']//CopyTarget//Directory";
	private static final String XPATH_SOURCE_DIRECTORY = "//Profiles/Profile[@profile_id='192.11.0.116:4445']//CopySource//Directory";
	private Order				order				          = null;
	private JSUniversalAgentBatchInstaller	jsUniversalAgentBatchInstaller  = null;
	private File				installationDefinitionFile;
	private String				installationJobChain;
	private static Logger		logger				           = Logger.getLogger(JSUniversalAgentBatchInstallerExecuter.class);

	private boolean				update;						   //Alle ausführen auf filterInstallHost:filterInstallPort 
    private String              filterInstallHost              = "";
	private int					filterInstallPort	           = 0;
	private JSUniversalAgentinstallation jsInstallation;

	private void init() {
		installationDefinitionFile = new File(jsUniversalAgentBatchInstaller.options().getinstallation_definition_file().Value());
		installationJobChain = jsUniversalAgentBatchInstaller.options().getinstallation_job_chain().Value();
		update = jsUniversalAgentBatchInstaller.options().getupdate().isTrue(); 
		filterInstallHost = jsUniversalAgentBatchInstaller.options().getfilter_install_host().Value();
		filterInstallPort = jsUniversalAgentBatchInstaller.options().getfilter_install_port().value();
	}

	private boolean filterNotSetOrFilterMatch(String value, String filter) { 
		logger.debug("Testing filter:" + value + "=" + filter);
		return (value.equals(filter) || filter == null || filter.trim().equals(""));
	}
	
	private boolean filterNotSetOrFilterMatch(int value, int filter) { 
		logger.debug("Testing filter:" + value + "=" + filter);
		return (value == filter || filter == 0);
	}	
	
	private boolean checkFilter() {
		boolean filterMatch = filterNotSetOrFilterMatch(jsInstallation.getAgentOptions().getSchedulerIpAddress(),filterInstallHost) &&
				              filterNotSetOrFilterMatch(jsInstallation.getAgentOptions().getSchedulerHttpPort(),filterInstallPort); 
		logger.debug("FilterMatch: " + filterMatch);

		boolean installationNotExecuted = jsInstallation.getLastRun() == null || jsInstallation.getLastRun().equals("");
		logger.debug("installationNotExecuted: " + installationNotExecuted + "(lastRun=" + jsInstallation.getLastRun() +")");

		if (filterMatch && (installationNotExecuted || update)){
			return true;
		}else {
			if (!filterMatch) {
				logger.info("Installation will not execute because filter does not match");
			}
			if (!installationNotExecuted) {
				logger.info("Installation will not execute because already was executed");
			}
			return false;
		}
	}

	private void updateLastRun(File installationsDefinitionFile) throws Exception {
		JSUniversalAgentInstallations jsInstallationsUpdateFile = new JSUniversalAgentInstallations(installationsDefinitionFile);

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM");
		String now = dateFormat.format(new Date());
		jsInstallationsUpdateFile.getInstallations().setLastRun(now);
		while (!jsInstallationsUpdateFile.eof()) {
			Installation installationUpdate = jsInstallationsUpdateFile.nextInstallation();
			if (installationUpdate.getLastRun() == null || installationUpdate.getLastRun().equals("") || update) {
			   installationUpdate.setLastRun(now);
			}
		}
		jsInstallationsUpdateFile.writeFile(installationsDefinitionFile);

	}

	public void performInstallation(JSUniversalAgentBatchInstaller jsUniversalAgentatchInstaller) throws Exception {
		this.jsUniversalAgentBatchInstaller = jsUniversalAgentatchInstaller;
		init();
		installationDefinitionFile = new File(jsUniversalAgentatchInstaller.options().getinstallation_definition_file().Value());
		JSUniversalAgentInstallations jsInstallations = new JSUniversalAgentInstallations(installationDefinitionFile);

		while (!jsInstallations.eof()) {
			jsInstallation = jsInstallations.next();
			if (checkFilter()) {
				createOrder();
			} else {
				logger.info(String.format("Skip creation of order for JobScheduler Universal Agent %1$s", jsInstallation.getAgentOptions().getSchedulerIpAddress() + ":" +  jsInstallation.getAgentOptions().getSchedulerHttpPort()));
			}
		}

		updateLastRun(installationDefinitionFile);
	}

	private String getValueFromXml(String file, String xpathExpression) throws Exception{
		file = file.replace("\\", "/");
		file = file.replace(".ini",".xml");
	
		SOSXMLXPath xpath = new SOSXMLXPath(new FileInputStream(file),true);
		return xpath.selectSingleNodeValue(xpathExpression);
		
	}
	private void createOrder() throws Exception {
		Spooler spooler = (Spooler) jsUniversalAgentBatchInstaller.getJSCommands().getSpoolerObject();

 		logger.info(String.format("Start to create order for scheduler id %1$s", jsInstallation.getAgentOptions().getSchedulerIpAddress() + ":" +  jsInstallation.getAgentOptions().getSchedulerHttpPort()));

		logger.info("scheduler_host:" + jsInstallation.getAgentOptions().getSchedulerIpAddress());
		logger.info("install_path:" + jsInstallation.getInstallPath());
		logger.info("scheduler_port:" + jsInstallation.getAgentOptions().getSchedulerHttpPort());
		logger.info("----------------------------------------------");

		if (spooler == null) {
			logger.info("Creation of order is skipped because spooler object is NULL");
			return;
		}
 
		order = spooler.create_order();
		Job_chain jobchain = spooler.job_chain(installationJobChain);
		order.set_id(jsInstallation.getAgentOptions().getSchedulerIpAddress() + ":" + jsInstallation.getAgentOptions().getSchedulerHttpPort());
	
		setParam("agent_options.scheduler_ip_address", jsInstallation.getAgentOptions().getSchedulerIpAddress());
		setParam("agent_options.scheduler_http_port", jsInstallation.getAgentOptions().getSchedulerHttpPort());
		setParam("agent_options.java_home", jsInstallation.getAgentOptions().getJavaHome());
		setParam("agent_options.java_options", jsInstallation.getAgentOptions().getJavaOptions());
		setParam("agent_options.scheduler_home", jsInstallation.getAgentOptions().getSchedulerHome());
		setParam("agent_options.scheduler_user", jsInstallation.getAgentOptions().getSchedulerUser());
		setParam("agent_options.scheduler_log_dir", jsInstallation.getAgentOptions().getSchedulerLogDir());
		setParam("agent_options.scheduler_kill_script", jsInstallation.getAgentOptions().getSchedulerKillScript());
		setParam("agent_options.scheduler_pid_file_dir", jsInstallation.getAgentOptions().getSchedulerPidFileDir());

		setParam("TransferInstallationSetup/operation", "copy");

		setParam("TransferInstallationSetup/file_spec", jsInstallation.getTransfer().getFileSpec());
		setParam("TransferInstallationSetup/target_host", jsInstallation.getTransfer().getTarget().getHost());

		if (jsInstallation.getTransfer().getSettings() == null || jsInstallation.getTransfer().getSettings().length() == 0){
			setParam("TransferInstallationSetup/target_port", jsInstallation.getTransfer().getTarget().getPort());
			setParam("TransferInstallationSetup/target_port", jsInstallation.getTransfer().getTarget().getPort());
			setParam("TransferInstallationSetup/target_protocol",  jsInstallation.getTransfer().getTarget().getProtocol());
			setParam("TransferInstallationSetup/target_user", jsInstallation.getTransfer().getTarget().getUser());
			setParam("TransferInstallationSetup/target_password",  jsInstallation.getTransfer().getTarget().getPassword());
			setParam("TransferInstallationSetup/target_dir", jsInstallation.getTransfer().getTarget().getDir());
			setParam("PerformInstall/target_dir", jsInstallation.getTransfer().getTarget().getDir());
			setParam("TransferInstallationSetup/target_ssh_auth_method", jsInstallation.getTransfer().getTarget().getSshAuthMethod());
			setParam("TransferInstallationSetup/target_ssh_auth_file", jsInstallation.getTransfer().getTarget().getSshAuthFile());

			setParam("TransferInstallationSetup/source_host", jsInstallation.getTransfer().getSource().getHost());
			setParam("TransferInstallationSetup/source_port", jsInstallation.getTransfer().getSource().getPort());
			setParam("TransferInstallationSetup/source_protocol", jsInstallation.getTransfer().getSource().getProtocol());
			setParam("TransferInstallationSetup/source_user", jsInstallation.getTransfer().getSource().getUser());
			setParam("TransferInstallationSetup/source_password", jsInstallation.getTransfer().getSource().getPassword());
			setParam("TransferInstallationSetup/source_dir",  jsInstallation.getTransfer().getSource().getDir());
			setParam("PerformInstall/source_dir",  jsInstallation.getTransfer().getSource().getDir());
			setParam("TransferInstallationSetup/source_ssh_auth_method", jsInstallation.getTransfer().getSource().getSshAuthMethod());
			setParam("TransferInstallationSetup/source_ssh_auth_file", jsInstallation.getTransfer().getSource().getSshAuthFile());
			
			setParam("PerformInstall/source_dir", jsInstallation.getTransfer().getSource().getDir());
			setParam("PerformInstall/target_dir", jsInstallation.getTransfer().getTarget().getDir());			
		}else{
			setParam("TransferInstallationSetup/settings", jsInstallation.getTransfer().getSettings());
			if (jsInstallation.getTransfer().getProfile() == null || jsInstallation.getTransfer().getProfile().length() == 0){
				setParam("TransferInstallationSetup/profile", jsInstallation.getAgentOptions().getSchedulerIpAddress() + ":" + jsInstallation.getAgentOptions().getSchedulerHttpPort());
			}else{
				setParam("TransferInstallationSetup/profile", jsInstallation.getTransfer().getProfile());
			}
			
			setParam("TransferInstallationSetup/source_dir", getValueFromXml(jsInstallation.getTransfer().getSettings(),XPATH_SOURCE_DIRECTORY));
			setParam("TransferInstallationSetup/target_dir", getValueFromXml(jsInstallation.getTransfer().getSettings(),XPATH_TARGET_DIRECTORY));
			setParam("PerformInstall/source_dir", getValueFromXml(jsInstallation.getTransfer().getSettings(),XPATH_SOURCE_DIRECTORY));
			setParam("PerformInstall/target_dir", getValueFromXml(jsInstallation.getTransfer().getSettings(),XPATH_TARGET_DIRECTORY));
		}

		setParam("PerformInstall/simulate_shell", "true");

		setParam("host", String.valueOf(jsInstallation.getSsh().getHost()));
		setParam("port", String.valueOf(jsInstallation.getSsh().getPort()));
		setParam("user", jsInstallation.getSsh().getUser());
		setParam("auth_method", jsInstallation.getSsh().getAuthMethod());
		setParam("auth_file", jsInstallation.getSsh().getAuthMethod());
		setParam("password", jsInstallation.getSsh().getPassword());
		setParam("sudo_password", jsInstallation.getSsh().getSudoPassword());
		setParam("install_path", jsInstallation.getInstallPath());

	    if  (jsInstallation.getPostprocessing() != null && jsInstallation.getPostprocessing().getCommand() != null){
		   setParam("PerformInstall/command_counter", String.valueOf(jsInstallation.getPostprocessing().getCommand().size()));
		   for ( int i=0;i< jsInstallation.getPostprocessing().getCommand().size();i++) {
				String command =  jsInstallation.getPostprocessing().getCommand().get(i);
				setParam("PerformInstall/command_" + i, command);
			} 	  
	 	 }		
		
		jobchain.add_order(order);
	}

	private void setParam(final String pstrParamName, final int pstrParamValue) {
	  setParam(pstrParamName,String.valueOf(pstrParamValue));
	}
	
	private void setParam(final String pstrParamName, final String pstrParamValue) {

		if (pstrParamValue != null && pstrParamValue.length() > 0){
			if(pstrParamName.contains("password")){
				logger.info("ParamName = " + pstrParamName + ", Value = ********");
			}else{
				if (jsInstallation.getListOfEntriesWithParameter().get(pstrParamValue) != null){
					logger.info("ParamName = " + pstrParamName + ", Value = " + jsInstallation.getListOfEntriesWithParameter().get(pstrParamValue));
				}else{
					logger.info("ParamName = " + pstrParamName + ", Value = " + pstrParamValue);
				}
			}
    		order.params().set_var(pstrParamName, pstrParamValue);
		}else{
			logger.debug("ParamName = " + pstrParamName + ", Value is empty --> not set");
		}
	}

	

}
