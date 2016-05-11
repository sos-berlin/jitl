package com.sos.jitl.jobchainnodeparameter.monitor;

import java.util.HashMap;

import com.sos.jitl.jobchainnodeparameter.JobchainNodeConfiguration;
import com.sos.jitl.jobchainnodeparameter.monitor.JobchainNodeSubstitute;
import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.messages.JSMessages;
 
public class JobchainNodeSubstitute extends JSJobUtilitiesClass<JobchainNodeSubstituteOptions> {
    private final String CLASSNAME = "ConfigurationMonitor";
    private final static Logger LOGGER = Logger.getLogger(JobchainNodeSubstitute.class);

    protected JobchainNodeSubstituteOptions configurationMonitorOptions = null;
 
    private HashMap <String,String>taskParameters;
    private HashMap <String,String>orderParameters;

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

    public JobchainNodeSubstitute Execute() throws Exception {
        final String METHODNAME = CLASSNAME + "::Execute";

        LOGGER.debug(String.format(JSMessages.JSJ_I_110.get(), METHODNAME));

        try {
            LOGGER.debug(getOptions().toString());

            jobchainNodeConfiguration = new JobchainNodeConfiguration();

            jobchainNodeConfiguration.setOrderId(orderId);
            jobchainNodeConfiguration.setJobChainPath(jobChainName);
            jobchainNodeConfiguration.setOrderPayload(orderPayload);
            jobchainNodeConfiguration.setLiveFolder(configurationMonitorOptions.configurationMonitor_configuration_path.Value());
            jobchainNodeConfiguration.setJobChainNodeConfigurationFileName(configurationMonitorOptions.configurationMonitor_configuration_file.Value());
            jobchainNodeConfiguration.setListOfOrderParameters(orderParameters);
            jobchainNodeConfiguration.setListOfTaskParameters(taskParameters);
            jobchainNodeConfiguration.substituteOrderParamters(getOptions().CurrentNodeName());
        }

        catch (Exception e) {
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

    }

    @Override
    public void setJSParam(String pstrKey, StringBuffer pstrValue) {
        
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

} // class ConfigurationMonitor