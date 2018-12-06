package sos.scheduler.managed.configuration;

import java.io.File;

import sos.spooler.Order;
import sos.util.SOSSchedulerLogger;

/** @author andreas pueschel */
public class ConfigurationOrderMonitor extends ConfigurationBaseMonitor {

    @Override
    public boolean spooler_process_before() {
        try {
            this.setLogger(new SOSSchedulerLogger(spooler_log));
            Order order = spooler_task.order();
            if (order == null) {
                return true;
            }
            String liveFolder = "";
            String jobChainPath = order.job_chain().path();
            if (order.params().value(conParamNameCONFIGURATION_PATH) != null && !order.params().value(conParamNameCONFIGURATION_PATH).isEmpty()) {
                spooler_log.debug3(conParamNameCONFIGURATION_PATH + " found in order parameters.");
                this.setConfigurationPath(order.params().value(conParamNameCONFIGURATION_PATH));
            } else if (spooler_task.params().value(conParamNameCONFIGURATION_PATH) != null
                    && !spooler_task.params().value(conParamNameCONFIGURATION_PATH).isEmpty()) {
                spooler_log.debug3(conParamNameCONFIGURATION_PATH + " found in task parameters.");
                this.setConfigurationPath(spooler_task.params().value(conParamNameCONFIGURATION_PATH));
            } else {
                if (!spooler_job.configuration_directory().isEmpty()) {
                    File fLiveBaseFolder = new File(spooler.configuration_directory());
                    File sJobChainPath = new File(fLiveBaseFolder, jobChainPath + ".job_chain.xml");
                    this.getLogger().debug7("Looking for job chain configuration path: " + sJobChainPath.getAbsolutePath());
                    if (!sJobChainPath.exists()) {
                        this.getLogger().debug2("Job Chain is probably configured in cache folder and not in live folder...");
                        File fCacheBaseFolder = new File(fLiveBaseFolder.getParentFile(), conDefaultFileName4CACHE);
                        sJobChainPath = new File(fCacheBaseFolder, jobChainPath);
                    }
                    liveFolder = sJobChainPath.getParent();
                    this.setConfigurationPath(liveFolder);
                } else {
                    this.setConfigurationPath(new File(spooler.ini_path()).getParent());
                }
                this.getLogger().debug2(".. parameter [" + conParamNameCONFIGURATION_PATH + "]: " + this.getConfigurationPath());
            }
            if (order.params().value(conParamNameCONFIGURATION_FILE) != null && !order.params().value(conParamNameCONFIGURATION_FILE).isEmpty()) {
                spooler_log.debug3(conParamNameCONFIGURATION_FILE + " found in order parameters.");
                this.setConfigurationFilename(order.params().value(conParamNameCONFIGURATION_FILE));
            } else if (spooler_task.params().value(conParamNameCONFIGURATION_FILE) != null
                    && !spooler_task.params().value(conParamNameCONFIGURATION_FILE).isEmpty()) {
                spooler_log.debug3(conParamNameCONFIGURATION_FILE + " found in task parameters.");
                this.setConfigurationFilename(spooler_task.params().value(conParamNameCONFIGURATION_FILE));
            } else {
                if (spooler_job.order_queue() != null) {
                    if (!spooler_job.configuration_directory().isEmpty()) {
                        File confFile = new File(getConfigurationPath(), order.job_chain().name() + conFileNameExtensionCONFIG_XML);
                        File confOrderFile =
                                new File(getConfigurationPath(), order.job_chain().name() + "," + order.id() + conFileNameExtensionCONFIG_XML);
                        if (confOrderFile.exists()) {
                            this.setConfigurationFilename(confOrderFile.getAbsolutePath());
                            this.getLogger().debug2(
                                    ".. configuration file for this order exists. order_id:" + order.job_chain().name() + "/" + order.id());
                        } else {
                            this.setConfigurationFilename(confFile.getAbsolutePath());
                            this.getLogger().debug2(
                                    ".. configuration file for job chain:" + order.job_chain().name() + "=" + this.getConfigurationFilename());
                        }
                    } else {
                        this.setConfigurationFilename("scheduler_" + spooler_task.order().job_chain().name() + conFileNameExtensionCONFIG_XML);
                    }
                    this.getLogger().debug2(".. parameter [" + conParamNameCONFIGURATION_FILE + "]: " + this.getConfigurationFilename());
                }
            }
            File confFile = null;
            if (this.getConfigurationFilename().startsWith(".") || this.getConfigurationFilename().startsWith("/")
                    || this.getConfigurationFilename().startsWith("\\") || this.getConfigurationFilename().indexOf(":") > -1
                    || this.getConfigurationFilename() == null || this.getConfigurationFilename().isEmpty()) {
                confFile = new File(this.getConfigurationFilename());
            } else {
                confFile = new File(this.getConfigurationPath(), this.getConfigurationFilename());
            }
            if (confFile.exists()) {
                this.initConfiguration();
                this.prepareConfiguration();
            } else {
                if (spooler_task.order().xml_payload() != null) {
                    spooler_log.info("Configuration File: " + confFile.getAbsolutePath() + " not found (Probably running on an agent).");
                    spooler_log.info("Reading configuration from xml payload...");
                    try {
                        this.prepareConfiguration();
                    } catch (Exception e) {
                    }
                }
            }
            return true;
        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_before: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean spooler_process_after(final boolean rc) throws Exception {
        this.cleanupConfiguration();
        return rc;
    }

}