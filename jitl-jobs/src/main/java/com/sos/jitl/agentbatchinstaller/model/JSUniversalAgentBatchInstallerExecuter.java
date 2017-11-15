/*
 * Created on 28.02.2011 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.sos.jitl.agentbatchinstaller.model;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.agentbatchinstaller.JSUniversalAgentBatchInstaller;
import com.sos.jitl.agentbatchinstaller.model.installations.Installation;

import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Spooler;
import sos.xml.SOSXMLXPath;

public class JSUniversalAgentBatchInstallerExecuter {

    private static final String XPATH_TARGET_DIRECTORY = "//Profiles/Profile[@profile_id='%s']//CopyTarget//Directory";
    private static final String XPATH_SOURCE_DIRECTORY = "//Profiles/Profile[@profile_id='%s']//CopySource//Directory";
    private Order order = null;
    private JSUniversalAgentBatchInstaller jsUniversalAgentBatchInstaller = null;
    private File installationDefinitionFile;
    private String installationJobChain;
    private static final Logger LOGGER = LoggerFactory.getLogger(JSUniversalAgentBatchInstallerExecuter.class);
    private boolean update;
    private String filterInstallHost = "";
    private int filterInstallPort = 0;
    private int installationCounter = 0;
    private JSUniversalAgentinstallation jsInstallation;
    private HashMap<String, String> createdOrders;
    private HashMap<String, Integer> counterInstallation;

    private void init() {
        installationDefinitionFile = new File(jsUniversalAgentBatchInstaller.options().getinstallation_definition_file().getValue());
        installationJobChain = jsUniversalAgentBatchInstaller.options().getinstallation_job_chain().getValue();
        update = jsUniversalAgentBatchInstaller.options().getupdate().isTrue();
        filterInstallHost = jsUniversalAgentBatchInstaller.options().getfilter_install_host().getValue();
        filterInstallPort = jsUniversalAgentBatchInstaller.options().getfilter_install_port().value();
        createdOrders = new HashMap<String, String>();
    }

    private boolean filterNotSetOrFilterMatch(String value, String filter) {
        LOGGER.debug("Testing filter:" + value + "=" + filter);
        return value.equals(filter) || filter == null || "".equals(filter.trim());
    }

    private boolean filterNotSetOrFilterMatch(int value, int filter) {
        LOGGER.debug("Testing filter:" + value + "=" + filter);
        return value == filter || filter == 0;
    }

    private boolean checkFilter() {
        boolean filterMatch =
                filterNotSetOrFilterMatch(jsInstallation.getAgentOptions().getSchedulerIpAddress(), filterInstallHost)
                        && filterNotSetOrFilterMatch(jsInstallation.getAgentOptions().getSchedulerHttpPort(), filterInstallPort);
        LOGGER.debug("FilterMatch: " + filterMatch);
        boolean installationNotExecuted = jsInstallation.getLastRun() == null || "".equals(jsInstallation.getLastRun());
        LOGGER.debug("installationNotExecuted: " + installationNotExecuted + "(lastRun=" + jsInstallation.getLastRun() + ")");
        if (filterMatch && (installationNotExecuted || update)) {
            return true;
        } else {
            if (!filterMatch) {
                LOGGER.info("Installation will not execute because filter does not match");
            }
            if (!installationNotExecuted) {
                LOGGER.info("Installation will not execute because already was executed");
            }
            return false;
        }
    }

    private String getKey(Installation installation) {
        String schedulerIpAddress;
        String installPath;
        if (installation.getAgentOptions().getSchedulerIpAddress() == null || installation.getAgentOptions().getSchedulerIpAddress().isEmpty()) {
            schedulerIpAddress = installation.getSsh().getHost(); 
        }else {
            schedulerIpAddress = installation.getAgentOptions().getSchedulerIpAddress();
        }
        if (installation.getInstallPath() == null || installation.getInstallPath().isEmpty()) {
            installPath = installation.getTransfer().getTarget().getDir();
        }else {
            installPath = installation.getInstallPath();
        }
        return schedulerIpAddress + ":" + installPath;
    }

    private void setInstallationCounter(File installationsDefinitionFile) throws Exception {
        counterInstallation = new HashMap<String, Integer>();
        JSUniversalAgentInstallations jsInstallationsUpdateFile = new JSUniversalAgentInstallations(installationsDefinitionFile);
        while (!jsInstallationsUpdateFile.eof()) {
            Installation installation = jsInstallationsUpdateFile.nextInstallation();
            String key = getKey(installation);
            int i = 0;
            if (counterInstallation.get(key) != null) {
                i = counterInstallation.get(key);
            }
            i = i + 1;
            counterInstallation.put(key, i);
            LOGGER.debug(String.format("%s installation on host %s", i, key));
        }
        jsInstallationsUpdateFile.writeFile(installationsDefinitionFile);
    }

    private void updateLastRun(File installationsDefinitionFile) throws Exception {
        JSUniversalAgentInstallations jsInstallationsUpdateFile = new JSUniversalAgentInstallations(installationsDefinitionFile);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM");
        String now = dateFormat.format(new Date());
        jsInstallationsUpdateFile.getInstallations().setLastRun(now);
        while (!jsInstallationsUpdateFile.eof()) {
            Installation installationUpdate = jsInstallationsUpdateFile.nextInstallation();
            if (installationUpdate.getLastRun() == null || "".equals(installationUpdate.getLastRun()) || update) {
                installationUpdate.setLastRun(now);
            }
        }
        jsInstallationsUpdateFile.writeFile(installationsDefinitionFile);
    }

    public void performInstallation(JSUniversalAgentBatchInstaller jsUniversalAgentatchInstaller) throws Exception {
        this.jsUniversalAgentBatchInstaller = jsUniversalAgentatchInstaller;
        init();
        installationDefinitionFile = new File(jsUniversalAgentatchInstaller.options().getinstallation_definition_file().getValue());
        setInstallationCounter(installationDefinitionFile);
        JSUniversalAgentInstallations jsInstallations = new JSUniversalAgentInstallations(installationDefinitionFile);
        installationCounter = jsInstallations.getInstallations().getInstallation().size();
        LOGGER.info(String.format("installing %s JobScheduler Universal Agents", installationCounter));
        while (!jsInstallations.eof()) {
            jsInstallation = jsInstallations.next();
            if (checkFilter()) {
                createOrder();
            } else {
                LOGGER.info(String.format("Skip creation of order for JobScheduler Universal Agent %1$s",
                        jsInstallation.getAgentOptions().getSchedulerIpAddress() + ":" + jsInstallation.getAgentOptions().getSchedulerHttpPort()));
            }
        }
        updateLastRun(installationDefinitionFile);
    }

    private String getValueFromXml(String file, String xpathExpression) throws Exception {
        file = file.replace("\\", "/");
        file = file.replace(".ini", ".xml");
        SOSXMLXPath xpath = new SOSXMLXPath(new FileInputStream(file), true);
        return xpath.selectSingleNodeValue(xpathExpression);
    }

    private int getInstallationCounter(JSUniversalAgentinstallation jsInstallation) {
        String key = getKey(jsInstallation);
        if (counterInstallation.get(key) != null) {
            return counterInstallation.get(key);
        } else {
            return 0;
        }
    }

    private void createOrder() throws Exception {
        Spooler spooler = (Spooler) jsUniversalAgentBatchInstaller.getJSCommands().getSpoolerObject();
        LOGGER.info(String.format("Start to create order for scheduler %1$s", jsInstallation.getAgentOptions().getSchedulerIpAddress() + ":"
                + jsInstallation.getAgentOptions().getSchedulerHttpPort()));
        LOGGER.info("scheduler_host:" + jsInstallation.getAgentOptions().getSchedulerIpAddress());
        LOGGER.info("install_path:" + jsInstallation.getInstallPath());
        LOGGER.info("scheduler_port:" + jsInstallation.getAgentOptions().getSchedulerHttpPort());
        LOGGER.info("----------------------------------------------");
        if (spooler == null) {
            LOGGER.info("Creation of order is skipped because spooler object is NULL");
            return;
        }
        order = spooler.create_order();
        Job_chain jobchain = spooler.job_chain(installationJobChain);
        String installationJobChainName = new File(installationJobChain).getName();
        String syncParam = installationJobChainName + "_required_orders";
        setParam(syncParam, getInstallationCounter(jsInstallation));
        String schedulerIpAddress = jsInstallation.getAgentOptions().getSchedulerIpAddress();
        int SchedulerHttpPort = jsInstallation.getAgentOptions().getSchedulerHttpPort();
        String orderId = schedulerIpAddress + ":" + SchedulerHttpPort;
        order.set_id(orderId);
        setParam("sync_session_id", jsInstallation.getAgentOptions().getSchedulerIpAddress() + ":" + jsInstallation.getInstallPath());
        setParam("agent_options.scheduler_ip_address", jsInstallation.getAgentOptions().getSchedulerIpAddress());
        setParam("agent_options.scheduler_http_port", jsInstallation.getAgentOptions().getSchedulerHttpPort());
        setParam("agent_options.java_home", jsInstallation.getAgentOptions().getJavaHome());
        setParam("agent_options.java_options", jsInstallation.getAgentOptions().getJavaOptions());
        setParam("agent_options.scheduler_home", jsInstallation.getInstallPath() + "/jobscheduler_agent");
        setParam("agent_options.scheduler_user", jsInstallation.getAgentOptions().getSchedulerUser());
        setParam("agent_options.scheduler_log_dir", jsInstallation.getAgentOptions().getSchedulerLogDir());
        setParam("agent_options.scheduler_kill_script", jsInstallation.getAgentOptions().getSchedulerKillScript());
        setParam("agent_options.scheduler_pid_file_dir", jsInstallation.getAgentOptions().getSchedulerPidFileDir());
        setParam("TransferInstallationSetup/operation", "copy");
        setParam("TransferInstallationSetup/file_spec", jsInstallation.getTransfer().getFileSpec());
        setParam("TransferInstallationSetup/target_host", jsInstallation.getTransfer().getTarget().getHost());
        String sourceDir = "";
        String targetDir = "";
        if (jsInstallation.getTransfer().getSettings() == null || jsInstallation.getTransfer().getSettings().isEmpty()) {
            setParam("TransferInstallationSetup/target_port", jsInstallation.getTransfer().getTarget().getPort());
            setParam("TransferInstallationSetup/target_port", jsInstallation.getTransfer().getTarget().getPort());
            setParam("TransferInstallationSetup/target_protocol", jsInstallation.getTransfer().getTarget().getProtocol());
            setParam("TransferInstallationSetup/target_user", jsInstallation.getTransfer().getTarget().getUser());
            setParam("TransferInstallationSetup/target_password", jsInstallation.getTransfer().getTarget().getPassword());
            setParam("TransferInstallationSetup/target_dir", jsInstallation.getTransfer().getTarget().getDir());
            setParam("PerformInstall/target_dir", jsInstallation.getTransfer().getTarget().getDir());
            setParam("TransferInstallationSetup/target_ssh_auth_method", jsInstallation.getTransfer().getTarget().getSshAuthMethod());
            setParam("TransferInstallationSetup/target_ssh_auth_file", jsInstallation.getTransfer().getTarget().getSshAuthFile());
            setParam("TransferInstallationSetup/source_host", jsInstallation.getTransfer().getSource().getHost());
            setParam("TransferInstallationSetup/source_port", jsInstallation.getTransfer().getSource().getPort());
            setParam("TransferInstallationSetup/source_protocol", jsInstallation.getTransfer().getSource().getProtocol());
            setParam("TransferInstallationSetup/source_user", jsInstallation.getTransfer().getSource().getUser());
            setParam("TransferInstallationSetup/source_password", jsInstallation.getTransfer().getSource().getPassword());
            setParam("TransferInstallationSetup/source_dir", jsInstallation.getTransfer().getSource().getDir());
            setParam("PerformInstall/source_dir", jsInstallation.getTransfer().getSource().getDir());
            setParam("TransferInstallationSetup/source_ssh_auth_method", jsInstallation.getTransfer().getSource().getSshAuthMethod());
            setParam("TransferInstallationSetup/source_ssh_auth_file", jsInstallation.getTransfer().getSource().getSshAuthFile());
            sourceDir = jsInstallation.getTransfer().getSource().getDir();
            targetDir = jsInstallation.getTransfer().getTarget().getDir();
        } else {        
            LOGGER.debug("...getting values from ini file: " + jsInstallation.getTransfer().getSettings());
            String profile = "";
            setParam("TransferInstallationSetup/settings", jsInstallation.getTransfer().getSettings());
            if (jsInstallation.getTransfer().getProfile() == null || jsInstallation.getTransfer().getProfile().isEmpty()) {
                if (jsInstallation.getAgentOptions().getSchedulerIpAddress() == null || jsInstallation.getAgentOptions().getSchedulerIpAddress().isEmpty()) {
                    LOGGER.warn("Please specify the value for the SchedulerIpAddress in the batchinstall configuration file");
                }
                profile = jsInstallation.getAgentOptions().getSchedulerIpAddress() + ":" + jsInstallation.getAgentOptions().getSchedulerHttpPort();
            } else {
                profile = jsInstallation.getTransfer().getProfile();
            }
            LOGGER.debug("...reading from profile: " + profile);
            setParam("TransferInstallationSetup/profile", profile);
            sourceDir = getValueFromXml(jsInstallation.getTransfer().getSettings(), String.format(XPATH_SOURCE_DIRECTORY, profile));
            targetDir = getValueFromXml(jsInstallation.getTransfer().getSettings(), String.format(XPATH_TARGET_DIRECTORY, profile));
        }
        setParam("TransferInstallationSetup/source_dir", sourceDir);
        setParam("TransferInstallationSetup/target_dir", targetDir);
        setParam("PerformInstall/source_dir", sourceDir);
        setParam("PerformInstall/target_dir", targetDir);
        setParam("PerformInstall/installation_file", jsInstallation.getInstallationFile());
        setParam("PerformInstall/simulate_shell", "true");
        setParam("host", String.valueOf(jsInstallation.getSsh().getHost()));
        setParam("port", String.valueOf(jsInstallation.getSsh().getPort()));
        setParam("user", jsInstallation.getSsh().getUser());
        setParam("auth_method", jsInstallation.getSsh().getAuthMethod());
        setParam("auth_file", jsInstallation.getSsh().getAuthFile());
        setParam("password", jsInstallation.getSsh().getPassword());
        setParam("sudo_password", jsInstallation.getSsh().getSudoPassword());
        setParam("install_path", jsInstallation.getInstallPath());
        if (jsInstallation.getPostprocessing() != null && jsInstallation.getPostprocessing().getCommand() != null) {
            setParam("PerformInstall/command_counter", String.valueOf(jsInstallation.getPostprocessing().getCommand().size()));
            for (int i = 0; i < jsInstallation.getPostprocessing().getCommand().size(); i++) {
                String command = jsInstallation.getPostprocessing().getCommand().get(i);
                setParam("PerformInstall/command_" + i, command);
            }
        }
        if (createdOrders.get(targetDir) != null && createdOrders.get(targetDir).equals(schedulerIpAddress)) {
            setParam("skipFileTransfer", "true");
        } else {
            setParam("skipFileTransfer", "false");
        }
        jobchain.add_order(order);
        createdOrders.put(targetDir, schedulerIpAddress);
    }

    private void setParam(final String pstrParamName, final int pstrParamValue) {
        setParam(pstrParamName, String.valueOf(pstrParamValue));
    }

    private void setParam(final String pstrParamName, final String pstrParamValue) {
        if (pstrParamValue != null && !pstrParamValue.isEmpty()) {
            if (pstrParamName.contains("password")) {
                LOGGER.info("ParamName = " + pstrParamName + ", Value = ********");
            } else {
                if (jsInstallation.getListOfEntriesWithParameter().get(pstrParamValue) != null) {
                    LOGGER.info("ParamName = " + pstrParamName + ", Value = " + jsInstallation.getListOfEntriesWithParameter().get(pstrParamValue));
                } else {
                    LOGGER.info("ParamName = " + pstrParamName + ", Value = " + pstrParamValue);
                }
            }
            order.params().set_var(pstrParamName, pstrParamValue);
        } else {
            LOGGER.debug("ParamName = " + pstrParamName + ", Value is empty --> not set");
        }
    }

}