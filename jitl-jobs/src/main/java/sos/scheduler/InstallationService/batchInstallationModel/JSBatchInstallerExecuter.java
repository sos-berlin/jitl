package sos.scheduler.InstallationService.batchInstallationModel;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.scheduler.InstallationService.JSBatchInstaller;
import sos.scheduler.InstallationService.batchInstallationModel.installations.Installation;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Spooler;

public class JSBatchInstallerExecuter {

    private Order order = null;
    private JSBatchInstaller jsBatchInstaller = null;
    private File localDir;
    private File installationDefinitionFile;
    private String installationJobChain;
    private static final Logger LOGGER = LoggerFactory.getLogger(JSBatchInstallerExecuter.class);
    private boolean update;
    private String filterInstallHost = "";
    private String installationSetupFilename = "";
    private int filterInstallPort = 0;

    private void init() {
        localDir = new File(jsBatchInstaller.Options().getlocal_dir().getValue());
        installationDefinitionFile = new File(jsBatchInstaller.Options().getinstallation_definition_file().getValue());
        installationSetupFilename = jsBatchInstaller.Options().getinstallation_setup_filename().getValue();
        installationJobChain = jsBatchInstaller.Options().getinstallation_job_chain().getValue();
        update = jsBatchInstaller.Options().getupdate().isTrue();
        filterInstallHost = jsBatchInstaller.Options().getfilter_install_host().getValue();
        filterInstallPort = jsBatchInstaller.Options().getfilter_install_port().value();
    }

    private boolean filterNotSetOrFilterMatch(String value, String filter) {
        LOGGER.debug("Testing filter:" + value + "=" + filter);
        return value.equals(filter) || filter == null || "".equals(filter.trim());
    }

    private boolean filterNotSetOrFilterMatch(int value, int filter) {
        LOGGER.debug("Testing filter:" + value + "=" + filter);
        return value == filter || filter == 0;
    }

    private boolean checkFilter(JSinstallation jsInstallation) {
        boolean filterMatch =
                filterNotSetOrFilterMatch(jsInstallation.getHost(), filterInstallHost)
                        && filterNotSetOrFilterMatch(jsInstallation.getSchedulerPort(), filterInstallPort);
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

    private void updateLastRun(File installationsDefinitionFile) throws Exception {
        JSInstallations jsInstallationsUpdateFile = new JSInstallations(installationsDefinitionFile);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM");
        String now = dateFormat.format(new Date());
        while (!jsInstallationsUpdateFile.eof()) {
            Installation installationUpdate = jsInstallationsUpdateFile.nextInstallation();
            if (installationUpdate.getLastRun() == null || "".equals(installationUpdate.getLastRun()) || update) {
                installationUpdate.setLastRun(now);
            }
        }
        jsInstallationsUpdateFile.writeFile(installationsDefinitionFile);
    }

    public void performInstallation(JSBatchInstaller jsBatchInstaller) throws Exception {
        this.jsBatchInstaller = jsBatchInstaller;
        init();
        installationDefinitionFile = new File(jsBatchInstaller.Options().getinstallation_definition_file().getValue());
        JSInstallations jsInstallations = new JSInstallations(installationDefinitionFile);
        while (!jsInstallations.eof()) {
            JSinstallation installation = jsInstallations.next();
            if (checkFilter(installation)) {
                createOrder(installation);
            } else {
                installation.doReplacing();
                LOGGER.info(String.format("Skip creation of order for scheduler id %1$s", installation.getSchedulerId()));
            }
        }
        updateLastRun(installationDefinitionFile);
    }

    private void createOrder(JSinstallation installation) throws Exception {
        Spooler spooler = (Spooler) jsBatchInstaller.getJSCommands().getSpoolerObject();
        File installationFile = installation.getInstallationFile(localDir);
        installation.doReplacing();
        LOGGER.info(String.format("Start to create order for scheduler id %1$s", installation.getSchedulerId()));
        JSXMLInstallationFile jsXMLInstallationFile = new JSXMLInstallationFile();
        jsXMLInstallationFile.setValues(installation);
        jsXMLInstallationFile.writeFile(installationFile);
        LOGGER.info("scheduler_id:" + installation.getSchedulerId());
        LOGGER.info("host:" + installation.getHost());
        LOGGER.info("install_path:" + installation.getInstallPath());
        LOGGER.info("licence:" + installation.getLicence());
        LOGGER.info("allowed_host:" + installation.getSchedulerAllowedHost());
        LOGGER.info("scheduler_port:" + installation.getSchedulerPort());
        LOGGER.info("userPathPanelElement:" + installation.getUserPathPanelElement());
        LOGGER.info("----------------------------------------------");
        if (spooler == null) {
            LOGGER.info("Creation of order is skipped because spooler object is NULL");
            return;
        }
        order = spooler.create_order();
        Job_chain jobchain = spooler.job_chain(installationJobChain);
        order.set_id(installation.getHost() + ":" + installation.getSchedulerPort());
        setParam("installation_file", installationFile.getName());
        setParam("installation_setup_filename", installationSetupFilename);
        setParam("ftp_user", installation.getFtp().getUser());
        setParam("ftp_local_dir", installation.getFtp().getLocalDir());
        setParam("ftp_host", installation.getHost());
        setParam("ftp_password", installation.getFtp().getPassword());
        setParam("ftp_remote_dir", installation.getFtp().getRemoteDir());
        setParam("TransferInstallationSetup/ftp_file_path", installationSetupFilename);
        setParam("TransferInstallationFile/ftp_local_dir", installationFile.getParent());
        setParam("TransferInstallationFile/ftp_file_path", installationFile.getName());
        setParam("ShutdownScheduler/host", String.valueOf(installation.getHost()));
        setParam("ShutdownScheduler/port", String.valueOf(installation.getSsh().getPort()));
        setParam("ShutdownScheduler/user", installation.getSsh().getUser());
        setParam("ShutdownScheduler/auth_method", installation.getSsh().getAuthMethod());
        setParam("ShutdownScheduler/password", installation.getSsh().getPassword());
        setParam("ShutdownScheduler/sudo_password", installation.getSsh().getSudoPassword());
        setParam("ShutdownScheduler/command", installation.getInstallPath() + "/" + installation.getSchedulerId()
                + "/bin/jobscheduler_agent.sh  stop");
        setParam("PerformInstall/host", String.valueOf(installation.getHost()));
        setParam("PerformInstall/port", String.valueOf(installation.getSsh().getPort()));
        setParam("PerformInstall/user", installation.getSsh().getUser());
        setParam("PerformInstall/auth_method", installation.getSsh().getAuthMethod());
        setParam("PerformInstall/password", installation.getSsh().getPassword());
        setParam("PerformInstall/sudo_password", installation.getSsh().getSudoPassword());
        setParam("PerformInstall/command", installation.getSsh().getCommand());
        jobchain.add_order(order);
    }

    private void setParam(final String pstrParamName, final String pstrParamValue) {
        if (pstrParamName.contains("password")) {
            LOGGER.info("ParamName = " + pstrParamName + ", Value = *****");
        } else {
            LOGGER.info("ParamName = " + pstrParamName + ", Value = " + pstrParamValue);
        }
        order.params().set_var(pstrParamName, pstrParamValue);
    }

}