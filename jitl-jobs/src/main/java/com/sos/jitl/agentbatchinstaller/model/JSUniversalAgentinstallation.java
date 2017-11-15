package com.sos.jitl.agentbatchinstaller.model;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.jitl.agentbatchinstaller.model.installations.Globals;
import com.sos.jitl.agentbatchinstaller.model.installations.Installation;
import com.sos.jitl.agentbatchinstaller.model.installations.Source;
import com.sos.jitl.agentbatchinstaller.model.installations.Ssh;
import com.sos.jitl.agentbatchinstaller.model.installations.Target;
import com.sos.jitl.agentbatchinstaller.model.installations.Transfer;

class JSUniversalAgentinstallation extends Installation {

    protected Globals globals;
    private static final Logger LOGGER = Logger.getLogger(JSUniversalAgentBatchInstallerExecuter.class);
    private static final int SCHEDULER_AGENT_DEFAULT_PORT = 4445;
    private HashMap<String, String> listOfEntriesWithParameter;
    private File installationFile = null;

    private String getValue(String installationValue, String globalValue) {
        if (globalValue == null) {
            globalValue = "";
        }
        if (installationValue == null) {
            installationValue = "";
        }
        if (!installationValue.isEmpty() || "".equals(globalValue)) {
            return installationValue;
        } else {
            return globalValue;
        }
    }

    public File getInstallationFile(File configurationPath) {
        if (installationFile == null) {
            installationFile = new File(configurationPath, this.getSsh().getHost() + "_" + this.getAgentOptions().getSchedulerHttpPort() + ".xml");
        }
        return installationFile;
    }

    private String replace(String parameterValue, String parameterName, String newValue) {
        String s = parameterValue.replaceAll("\\$\\{" + parameterName + "\\}", newValue);
        if (parameterName.contains("password")) {
            String sp = parameterValue.replaceAll("\\$\\{" + parameterName + "\\}", "********");
            listOfEntriesWithParameter.put(s, sp);
        }
        return s;
    }

    private String replaceAll(String value) {
        if (value == null) {
            return value;
        }
        value = this.replace(value, "install_path", this.getInstallPath());
        value = this.replace(value, "installation_file", this.getInstallationFile());
        // Agent Options
        value = this.replace(value, "agent_options.java_home", this.getAgentOptions().getJavaHome());
        value = this.replace(value, "agent_options.java_options", this.getAgentOptions().getJavaOptions());
        value = this.replace(value, "agent_options.scheduler_ip_address", this.getAgentOptions().getJavaHome());
        value = this.replace(value, "agent_options.scheduler_http_port", String.valueOf(this.getAgentOptions().getSchedulerHttpPort()));
        value = this.replace(value, "agent_options.scheduler_user", this.getAgentOptions().getSchedulerUser());
        value = this.replace(value, "agent_options.scheduler_log_dir", this.getAgentOptions().getSchedulerLogDir());
        value = this.replace(value, "agent_options.scheduler_kill_script", this.getAgentOptions().getSchedulerKillScript());
        value = this.replace(value, "agent_options.scheduler_pid_file_dir", this.getAgentOptions().getSchedulerPidFileDir());
        // transfer
        value = this.replace(value, "transfer.settings", this.getTransfer().getSettings());
        value = this.replace(value, "transfer.profile", this.getTransfer().getProfile());
        value = this.replace(value, "transfer.source.host", this.getTransfer().getSource().getHost());
        value = this.replace(value, "transfer.source.port", this.getTransfer().getSource().getPort());
        value = this.replace(value, "transfer.source.protocol", this.getTransfer().getSource().getProtocol());
        value = this.replace(value, "transfer.source.user", this.getTransfer().getSource().getUser());
        value = this.replace(value, "transfer.source.ssh_auth_method", this.getTransfer().getSource().getSshAuthMethod());
        value = this.replace(value, "transfer.source.ssh_auth_file", this.getTransfer().getSource().getSshAuthFile());
        value = this.replace(value, "transfer.source.password", this.getTransfer().getSource().getPassword());
        value = this.replace(value, "transfer.target.host", this.getTransfer().getTarget().getHost());
        value = this.replace(value, "transfer.target.port", this.getTransfer().getTarget().getPort());
        value = this.replace(value, "transfer.target.protocol", this.getTransfer().getTarget().getProtocol());
        value = this.replace(value, "transfer.target.user", this.getTransfer().getTarget().getUser());
        value = this.replace(value, "transfer.target.ssh_auth_method", this.getTransfer().getTarget().getSshAuthMethod());
        value = this.replace(value, "transfer.target.ssh_auth_file", this.getTransfer().getTarget().getSshAuthFile());
        value = this.replace(value, "transfer.target.password", this.getTransfer().getTarget().getPassword());
        // SSH
        value = this.replace(value, "ssh.auth_file", this.getSsh().getAuthFile());
        value = this.replace(value, "ssh.auth_method", this.getSsh().getAuthMethod());
        value = this.replace(value, "ssh.port", String.valueOf(this.getSsh().getPort()));
        value = this.replace(value, "ssh.host", this.getSsh().getHost());
        value = this.replace(value, "ssh.user", this.getSsh().getUser());
        value = this.replace(value, "ssh.password", this.getSsh().getPassword());
        value = this.replace(value, "ssh.sudo_password", this.getSsh().getSudoPassword());
        if (this.installationFile == null) {
            LOGGER.debug("Installationfile is not set. Will not be replaces");
            this.installationFile = new File("");
        } else {
            if (!"".equals(this.installationFile.getName())) {
                value = this.replace(value, "installation_file", this.installationFile.getName());
            }
            value = this.replace(value, "installation_file", this.installationFile.getName());
        }
        return value;
    }

    private void doReplacing() {
        this.setInstallPath(replaceAll(this.getInstallPath()));
        this.setInstallationFile(replaceAll(this.getInstallationFile()));
        this.getAgentOptions().setJavaHome(replaceAll(this.getAgentOptions().getJavaHome()));
        this.getAgentOptions().setJavaOptions(replaceAll(this.getAgentOptions().getJavaOptions()));
        this.getAgentOptions().setSchedulerIpAddress(replaceAll(this.getAgentOptions().getSchedulerIpAddress()));
        this.getAgentOptions().setSchedulerUser(replaceAll(this.getAgentOptions().getSchedulerUser()));
        this.getAgentOptions().setSchedulerLogDir(replaceAll(this.getAgentOptions().getSchedulerLogDir()));
        this.getAgentOptions().setSchedulerKillScript(replaceAll(this.getAgentOptions().getSchedulerKillScript()));
        this.getAgentOptions().setSchedulerPidFileDir(replaceAll(this.getAgentOptions().getSchedulerPidFileDir()));
        this.getTransfer().setOperation(replaceAll(this.getTransfer().getOperation()));
        this.getTransfer().setFileSpec(replaceAll(this.getTransfer().getFileSpec()));
        this.getTransfer().setSettings(replaceAll(this.getTransfer().getSettings()));
        this.getTransfer().setProfile(replaceAll(this.getTransfer().getProfile()));
        this.getTransfer().getSource().setHost(replaceAll(this.getTransfer().getSource().getHost()));
        this.getTransfer().getSource().setPort(replaceAll(this.getTransfer().getSource().getPort()));
        this.getTransfer().getSource().setProtocol(replaceAll(this.getTransfer().getSource().getProtocol()));
        this.getTransfer().getSource().setUser(replaceAll(this.getTransfer().getSource().getUser()));
        this.getTransfer().getSource().setPassword(replaceAll(this.getTransfer().getSource().getPassword()));
        this.getTransfer().getSource().setDir(replaceAll(this.getTransfer().getSource().getDir()));
        this.getTransfer().getSource().setSshAuthMethod(replaceAll(this.getTransfer().getSource().getSshAuthMethod()));
        this.getTransfer().getSource().setSshAuthFile(replaceAll(this.getTransfer().getSource().getSshAuthFile()));
        this.getTransfer().getTarget().setHost(replaceAll(this.getTransfer().getTarget().getHost()));
        this.getTransfer().getTarget().setPort(replaceAll(this.getTransfer().getTarget().getPort()));
        this.getTransfer().getTarget().setProtocol(replaceAll(this.getTransfer().getTarget().getProtocol()));
        this.getTransfer().getTarget().setUser(replaceAll(this.getTransfer().getTarget().getUser()));
        this.getTransfer().getTarget().setPassword(replaceAll(this.getTransfer().getTarget().getPassword()));
        this.getTransfer().getTarget().setDir(replaceAll(this.getTransfer().getTarget().getDir()));
        this.getTransfer().getTarget().setSshAuthMethod(replaceAll(this.getTransfer().getTarget().getSshAuthMethod()));
        this.getTransfer().getTarget().setSshAuthFile(replaceAll(this.getTransfer().getTarget().getSshAuthFile()));
        this.getSsh().setAuthMethod(replaceAll(this.getSsh().getAuthMethod()));
        this.getSsh().setAuthFile(replaceAll(this.getSsh().getAuthFile()));
        this.getSsh().setSudoPassword(replaceAll(this.getSsh().getSudoPassword()));
        this.getSsh().setPassword(replaceAll(this.getSsh().getPassword()));
        this.getSsh().setUser(replaceAll(this.getSsh().getUser()));
        this.getSsh().setPort(replaceAll(this.getSsh().getPort()));
        this.getSsh().setHost(replaceAll(this.getSsh().getHost()));
        if (this.getPostprocessing() != null && this.getPostprocessing().getCommand() != null) {
            for (int i = 0; i < this.getPostprocessing().getCommand().size(); i++) {
                String command = this.getPostprocessing().getCommand().get(i);
                String replacedCommand = replaceAll(command);
                this.getPostprocessing().getCommand().set(i, replacedCommand);
            }
        }
    }

    public void setValues(Installation installation) {
        listOfEntriesWithParameter = new HashMap<String, String>();
        this.setInstallPath(getValue(installation.getInstallPath(), globals.getInstallPath()));
        this.setInstallationFile(getValue(installation.getInstallationFile(), globals.getInstallationFile()));
        this.setLastRun(installation.getLastRun());
        this.setAgentOptions(installation.getAgentOptions());
        if (installation.getTransfer() == null) {
            Transfer transfer = new Transfer();
            transfer.setSource(new Source());
            transfer.setTarget(new Target());
            installation.setTransfer(transfer);
        }
        installation.getTransfer().setSettings(getValue(installation.getTransfer().getSettings(), globals.getTransfer().getSettings()));
        installation.getTransfer().setProfile(getValue(installation.getTransfer().getProfile(), globals.getTransfer().getProfile()));
        if (installation.getTransfer().getTarget() != null && globals.getTransfer() != null && globals.getTransfer().getTarget() != null) {
            installation.getTransfer().getTarget().setHost(
                    getValue(installation.getTransfer().getTarget().getHost(), globals.getTransfer().getTarget().getHost()));
            installation.getTransfer().getTarget().setPort(
                    getValue(installation.getTransfer().getTarget().getPort(), globals.getTransfer().getTarget().getPort()));
            installation.getTransfer().getTarget().setProtocol(
                    getValue(installation.getTransfer().getTarget().getProtocol(), globals.getTransfer().getTarget().getProtocol()));
            installation.getTransfer().getTarget().setUser(
                    getValue(installation.getTransfer().getTarget().getUser(), globals.getTransfer().getTarget().getUser()));
            installation.getTransfer().getTarget().setPassword(
                    getValue(installation.getTransfer().getTarget().getPassword(), globals.getTransfer().getTarget().getPassword()));
            installation.getTransfer().getTarget().setDir(
                    getValue(installation.getTransfer().getTarget().getDir(), globals.getTransfer().getTarget().getDir()));
            installation.getTransfer().getTarget().setSshAuthMethod(
                    getValue(installation.getTransfer().getTarget().getSshAuthMethod(), globals.getTransfer().getTarget().getSshAuthMethod()));
            installation.getTransfer().getTarget().setSshAuthFile(
                    getValue(installation.getTransfer().getTarget().getSshAuthFile(), globals.getTransfer().getTarget().getSshAuthFile()));
        }
        if (installation.getTransfer().getSource() != null && globals.getTransfer() != null && globals.getTransfer().getSource() != null) {
            installation.getTransfer().getSource().setHost(
                    getValue(installation.getTransfer().getSource().getHost(), globals.getTransfer().getSource().getHost()));
            installation.getTransfer().getSource().setPort(
                    getValue(installation.getTransfer().getSource().getPort(), globals.getTransfer().getSource().getPort()));
            installation.getTransfer().getSource().setProtocol(
                    getValue(installation.getTransfer().getSource().getProtocol(), globals.getTransfer().getSource().getProtocol()));
            installation.getTransfer().getSource().setUser(
                    getValue(installation.getTransfer().getSource().getUser(), globals.getTransfer().getSource().getUser()));
            installation.getTransfer().getSource().setPassword(
                    getValue(installation.getTransfer().getSource().getPassword(), globals.getTransfer().getSource().getPassword()));
            installation.getTransfer().getSource().setDir(
                    getValue(installation.getTransfer().getSource().getDir(), globals.getTransfer().getSource().getDir()));
            installation.getTransfer().getSource().setSshAuthMethod(
                    getValue(installation.getTransfer().getSource().getSshAuthMethod(), globals.getTransfer().getSource().getSshAuthMethod()));
            installation.getTransfer().getSource().setSshAuthFile(
                    getValue(installation.getTransfer().getSource().getSshAuthFile(), globals.getTransfer().getSource().getSshAuthFile()));
        }
        this.setTransfer(installation.getTransfer());
        if (installation.getSsh() == null) {
            installation.setSsh(new Ssh());
        }
        if (globals.getSsh() != null) {
            installation.getSsh().setAuthMethod(getValue(installation.getSsh().getAuthMethod(), globals.getSsh().getAuthMethod()));
            installation.getSsh().setPassword(getValue(installation.getSsh().getPassword(), globals.getSsh().getPassword()));
            installation.getSsh().setSudoPassword(getValue(installation.getSsh().getSudoPassword(), globals.getSsh().getSudoPassword()));
            installation.getSsh().setPort(getValue(installation.getSsh().getPort(), globals.getSsh().getPort()));
            installation.getSsh().setHost(getValue(installation.getSsh().getHost(), globals.getSsh().getHost()));
            installation.getSsh().setUser(getValue(installation.getSsh().getUser(), globals.getSsh().getUser()));
            installation.getSsh().setAuthMethod(getValue(installation.getSsh().getAuthMethod(), globals.getSsh().getAuthMethod()));
        }
        this.setSsh(installation.getSsh());
        if ((installation.getPostprocessing() == null) || (installation.getPostprocessing().getCommand() == null)
                || installation.getPostprocessing().getCommand().isEmpty()) {
            installation.setPostprocessing(globals.getPostprocessing());
        }
        if (installation.getPostprocessing() == null) {
            LOGGER.debug("no Postprocessing given");
        }
        this.setPostprocessing(installation.getPostprocessing());
        if (installation.getAgentOptions().getSchedulerIpAddress() == null || installation.getAgentOptions().getSchedulerIpAddress().isEmpty()) {
            installation.getAgentOptions().setSchedulerIpAddress(installation.getSsh().getHost());
        }
        if (installation.getAgentOptions().getSchedulerHttpPort() == null) {
            installation.getAgentOptions().setSchedulerHttpPort(SCHEDULER_AGENT_DEFAULT_PORT);
        }
        doReplacing();
    }

    public HashMap<String, String> getListOfEntriesWithParameter() {
        return listOfEntriesWithParameter;
    }

}