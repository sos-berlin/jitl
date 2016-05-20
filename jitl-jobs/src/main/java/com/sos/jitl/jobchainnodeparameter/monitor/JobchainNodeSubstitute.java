package com.sos.jitl.jobchainnodeparameter.monitor;

import java.util.HashMap;
import java.util.Map;

import com.sos.jitl.jobchainnodeparameter.JobchainNodeConfiguration;
import com.sos.jitl.jobchainnodeparameter.monitor.JobchainNodeSubstitute;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.messages.JSMessages;

public class JobchainNodeSubstitute extends JSJobUtilitiesClass<JobchainNodeSubstituteOptions> {
    private static final String CLASSNAME = "ConfigurationMonitor";
    private static final Logger LOGGER = Logger.getLogger(JobchainNodeSubstitute.class);

    protected JobchainNodeSubstituteOptions configurationMonitorOptions = null;

    private Map<String, String> taskParameters;
    private Map<String, String> orderParameters;

    private String orderPayload;
    private String orderId;
    private String jobChainName;

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
            jobchainNodeConfiguration.setJobChainPath(jobChainName);
            jobchainNodeConfiguration.setOrderPayload(orderPayload);
            jobchainNodeConfiguration.setLiveFolder(configurationMonitorOptions.configurationMonitorConfigurationPath.getValue());
            jobchainNodeConfiguration.setJobChainNodeConfigurationFileName(configurationMonitorOptions.configurationMonitorConfigurationFile.getValue());
            jobchainNodeConfiguration.setListOfOrderParameters(orderParameters);
            jobchainNodeConfiguration.setListOfTaskParameters(taskParameters);
            jobchainNodeConfiguration.substituteOrderParamters(getOptions().getCurrentNodeName());
        
        } catch (Exception e) {
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

    public void setTaskParameters(HashMap<String, String> taskParameters) {
        this.taskParameters = taskParameters;
    }

    public JobchainNodeConfiguration getJobchainNodeConfiguration() {
        return jobchainNodeConfiguration;
    }

    public void setJobChainName(String jobChainName) {
        this.jobChainName = jobChainName;
    }

} 