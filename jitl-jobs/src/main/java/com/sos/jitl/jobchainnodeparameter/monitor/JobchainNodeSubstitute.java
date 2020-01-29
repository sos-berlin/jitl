package com.sos.jitl.jobchainnodeparameter.monitor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobchainnodeparameter.JobchainNodeConfiguration;
import com.sos.jitl.jobchainnodeparameter.monitor.JobchainNodeSubstitute;


import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.messages.JSMessages;

public class JobchainNodeSubstitute extends JSJobUtilitiesClass<JobchainNodeSubstituteOptions> {

    private static final String CLASSNAME = "ConfigurationMonitor";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobchainNodeSubstitute.class);

    protected JobchainNodeSubstituteOptions configurationMonitorOptions = null;

    private Map<String, String> taskParameters;
    private Map<String, String> orderParameters;
    private Map<String, String> schedulerParameters;

    private String orderPayload;
    private String orderId;
    private String jobChainPath;
    private String fileContent;

    JobchainNodeConfiguration jobchainNodeConfiguration;

    public JobchainNodeSubstitute() {
        super(new JobchainNodeSubstituteOptions());
    }

    public JobchainNodeSubstituteOptions getOptions() {

        if (configurationMonitorOptions == null) {
            configurationMonitorOptions = new JobchainNodeSubstituteOptions();
        }
        return configurationMonitorOptions;
    }

    public JobchainNodeSubstitute execute() throws Exception {
        final String METHODNAME = CLASSNAME + "::Execute";
        LOGGER.debug(String.format(JSMessages.JSJ_I_110.get(), METHODNAME));

        try {
            LOGGER.debug(getOptions().toString());

            jobchainNodeConfiguration = new JobchainNodeConfiguration();

            jobchainNodeConfiguration.setOrderId(orderId);
            jobchainNodeConfiguration.setJobChainPath(jobChainPath);

            jobchainNodeConfiguration.setOrderPayload(orderPayload);
            jobchainNodeConfiguration.setLiveFolder(configurationMonitorOptions.configurationMonitorConfigurationPath.getValue());
            jobchainNodeConfiguration.setJobChainNodeConfigurationFileName(configurationMonitorOptions.configurationMonitorConfigurationFile
                    .getValue());
            jobchainNodeConfiguration.setListOfSchedulerParameters(schedulerParameters);
            jobchainNodeConfiguration.setListOfOrderParameters(orderParameters);
            jobchainNodeConfiguration.setListOfTaskParameters(taskParameters);
            if (orderParameters != null) {
                jobchainNodeConfiguration.substituteOrderParamters(getOptions().getCurrentNodeName());
            } else {
                jobchainNodeConfiguration.substituteTaskParamters();
            }
            fileContent = jobchainNodeConfiguration.getFileContent();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
            LOGGER.error(String.format(JSMessages.JSJ_F_107.get(), METHODNAME), e);
            throw e;
        } finally {
            LOGGER.debug(String.format(JSMessages.JSJ_I_111.get(), METHODNAME));
        }

        return this;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setOrderPayload(String orderPayload) {
        this.orderPayload = orderPayload;
    }

    @Override
    public String replaceSchedulerVars(String pstrString2Modify) {
        LOGGER.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
        return pstrString2Modify;
    }

    @Override
    public void setJSParam(String pstrKey, String pstrValue) {
        // Implement Method here
    }

    @Override
    public void setJSParam(String pstrKey, StringBuffer pstrValue) {
        // Implement Method here
    }

    public void setOrderParameters(HashMap<String, String> orderParameters) {
        this.orderParameters = orderParameters;
    }

    public void setSchedulerParameters(HashMap<String, String> schedulerParameters) {
        this.schedulerParameters = schedulerParameters;
    }

    public void setTaskParameters(HashMap<String, String> taskParameters) {
        this.taskParameters = taskParameters;
    }

    public JobchainNodeConfiguration getJobchainNodeConfiguration() {
        return jobchainNodeConfiguration;
    }

    public void setJobChainPath(String jobChainPath) {
        this.jobChainPath = jobChainPath;
    }

    public String getFileContent() {
        return fileContent;
    }

}