package com.sos.jitl.jobchainnodeparameter;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.io.Files.JSFile;
import com.sos.jitl.jobchainnodeparameter.model.JobChain;
import com.sos.jitl.jobchainnodeparameter.model.Param;
import com.sos.jitl.jobchainnodeparameter.model.Params;
import com.sos.jitl.jobchainnodeparameter.model.Process;
import com.sos.jitl.jobchainnodeparameter.model.Settings;

import sos.util.ParameterSubstitutor;

public class JobchainNodeConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobchainNodeConfiguration.class);

    private static final String FILENAMEEXTENSIONCONFIG_XML = ".config.xml";
    private static final String DEFAULTFILENAME4CACHE = "cache";

    private JAXBContext context;

    private String liveFolder;

    private String jobChainNodeConfigurationFileName;
    private String orderId;
    private String jobChainPath;
    private String orderPayload;

    private JSFile jobChainNodeConfigurationFile;
    private Settings settings;
    private Params listOfJobchainParameters;
    private Params listOfJobchainNodeParameters;

    private Map<String, String> jobchainGlobalParameters;
    private Map<String, String> jobchainNodeParameters;
    private Map<String, String> jobchainParameters;

    private ParameterSubstitutor parameterSubstitutor;
    private Map<String, String> listOfSchedulerParameters;
    private Map<String, String> listOfOrderParameters;
    private Map<String, String> listOfTaskParameters;

    public JobchainNodeConfiguration() throws JAXBException {
        super();
        listOfTaskParameters = new HashMap<String, String>();
        listOfOrderParameters = new HashMap<String, String>();
        listOfSchedulerParameters = new HashMap<String, String>();
        context = JAXBContext.newInstance(Settings.class);
    }

    public JobchainNodeConfiguration(JSFile jobChainNodeConfigurationFile) throws JAXBException {
        super();
        context = JAXBContext.newInstance(Settings.class);
        listOfTaskParameters = new HashMap<String, String>();
        listOfOrderParameters = new HashMap<String, String>();
        listOfSchedulerParameters = new HashMap<String, String>();
        if (jobChainNodeConfigurationFile.exists()) {
            this.jobChainNodeConfigurationFile = jobChainNodeConfigurationFile;
        } else {
            LOGGER.warn(String.format("File %s does not exist", jobChainNodeConfigurationFile.getAbsolutePath()));
        }

    }

    public void addParam(String key, String value) {
        if (listOfOrderParameters == null) {
            listOfOrderParameters = new HashMap<String, String>();
        }
        listOfOrderParameters.put(key, value);
    }

    public String getParam(String key) {
        if (listOfOrderParameters != null) {
            return listOfOrderParameters.get(key);
        } else {
            return null;
        }
    }

    private JSFile getFileFromCacheFolder(JSFile configurationFile) {
        if (!configurationFile.exists()) {
            File fCacheBaseFolder = new File(configurationFile.getParentFile(), DEFAULTFILENAME4CACHE);
            return new JSFile(fCacheBaseFolder, configurationFile.getName());
        }
        return configurationFile;
    }

    private void setJobChainNodeConfigurationFile() {
        String orderConfigurationFileName = jobChainPath + "," + orderId + FILENAMEEXTENSIONCONFIG_XML;
        LOGGER.debug("orderConfigurationFileName:" + orderConfigurationFileName);
        jobChainNodeConfigurationFile = new JSFile(liveFolder, orderConfigurationFileName);
        jobChainNodeConfigurationFile = getFileFromCacheFolder(jobChainNodeConfigurationFile);

        if (!jobChainNodeConfigurationFile.exists()) {
            if (jobChainNodeConfigurationFileName == null || "".equals(jobChainNodeConfigurationFileName)) {
                jobChainNodeConfigurationFileName = jobChainPath + FILENAMEEXTENSIONCONFIG_XML;
            }
            LOGGER.debug("jobChainNodeConfigurationFileName:" + jobChainNodeConfigurationFileName);
            jobChainNodeConfigurationFile = new JSFile(liveFolder, jobChainNodeConfigurationFileName);
            jobChainNodeConfigurationFile = getFileFromCacheFolder(jobChainNodeConfigurationFile);

        }

        LOGGER.debug("Looking for job chain configuration path: " + jobChainNodeConfigurationFile.getAbsolutePath());

    }

    private void getParametersFromConfigFile() throws JAXBException {

        if (listOfJobchainParameters == null || listOfJobchainNodeParameters == null) {

            Unmarshaller unmarshaller = context.createUnmarshaller();
            if (jobChainNodeConfigurationFile != null && jobChainNodeConfigurationFile.exists()) {
                settings = (Settings) unmarshaller.unmarshal(jobChainNodeConfigurationFile);
            } else {
                LOGGER.info("Configuration File: " + jobChainNodeConfigurationFile.getAbsolutePath() + " not found (Probably running on an agent).");
                LOGGER.info("Reading configuration from xml payload...");
                StringReader reader = new StringReader(orderPayload);
                settings = (Settings) unmarshaller.unmarshal(reader);
            }

            JobChain jobchain = settings.getJobChain();
            listOfJobchainParameters = jobchain.getOrder().getParams();
        }
    }

    private void getJobchainParameters() throws JAXBException {
        for (Param param : listOfJobchainParameters.getParam()) {
            if (!"".equals(param.getName())) {
                jobchainGlobalParameters.put(param.getName(), param.getValue());
                jobchainParameters.put(param.getName(), param.getValue());
            }
        }
    }

    private void getJobchainNodeParameters(String node) throws JAXBException {
        List<Process> processes = settings.getJobChain().getOrder().getProcess();
        for (Process process : processes) {
            if (process.getState().equals(node)) {
                listOfJobchainNodeParameters = process.getParams();
                for (Param param : listOfJobchainNodeParameters.getParam()) {
                    if (!"".equals(param.getName())) {
                        jobchainParameters.put(param.getName(), param.getValue());
                        jobchainNodeParameters.put(param.getName(), param.getValue());
                    }
                }
            }

        }
    }

    private void getParametersForNode(String node)  {
        jobchainGlobalParameters = new HashMap<String, String>();
        jobchainNodeParameters = new HashMap<String, String>();
        jobchainParameters = new HashMap<String, String>();

        if (jobChainNodeConfigurationFile == null) {
            setJobChainNodeConfigurationFile();
        }

        try {
            if (!"".equals(orderPayload) || (jobChainNodeConfigurationFile != null && jobChainNodeConfigurationFile.exists())) {
                getParametersFromConfigFile();
                getJobchainParameters();
                getJobchainNodeParameters(node);
            }
        } catch (Exception e) {
        }

    }

    public String getJobchainGlobalParameterValue(String key) {
        return jobchainGlobalParameters.get(key);
    }

    public String getJobchainParameterValue(String key) {
        return jobchainParameters.get(key);
    }

    public String getJobchainNodeParameterValue(String key) {
        if ("".equals(key)) {
            return null;
        } else {
            return jobchainNodeParameters.get(key);
        }
    }

    private void addSubstituterValues(Map<String, String> h) {
        if (h != null) {
            if (parameterSubstitutor == null) {
                parameterSubstitutor = new ParameterSubstitutor();
            }
            for (Entry<String, String> entry : h.entrySet()) {
                String value = entry.getValue();
                String paramName = entry.getKey();
                if (value != null) {
                    parameterSubstitutor.addKey(paramName, value);
                }
            }
        }
    }

    private String doReplace(String value, String openTag, String closeTag) {
        parameterSubstitutor.setOpenTag(openTag);
        parameterSubstitutor.setCloseTag(closeTag);

        String replacedValue = parameterSubstitutor.replaceEnvVars(value);
        replacedValue = parameterSubstitutor.replaceSystemProperties(replacedValue);
        replacedValue = parameterSubstitutor.replace(replacedValue);
        return replacedValue;

    }

    public void substituteOrderParamters(String node)  {
        getParametersForNode(node);
        addSubstituterValues(listOfSchedulerParameters);
        addSubstituterValues(listOfTaskParameters);
        addSubstituterValues(listOfOrderParameters);
        addSubstituterValues(jobchainParameters);

        // Make the node parameters available in the order parameter set.
        if (jobchainParameters != null) {
            for (String key : jobchainParameters.keySet()) {
                String value = jobchainParameters.get(key);
                if (value != null) {
                    listOfOrderParameters.put(key, value);
                }
            }

        }

        // Substitute the task parameter set ${param}
        if (listOfTaskParameters != null) {
            for (String key : listOfTaskParameters.keySet()) {
                String value = listOfTaskParameters.get(key);
                if (value != null) {
                    String replacedValue = doReplace(value, "${", "}");
                    replacedValue = doReplace(replacedValue, "%", "%");
                    if (!replacedValue.equals(value)) {
                        listOfTaskParameters.put(key, replacedValue);
                    }
                }
            }
        }

        // Substitute the order parameter set ${param}
        if (listOfOrderParameters != null) {
            for (String key : listOfOrderParameters.keySet()) {
                String value = listOfOrderParameters.get(key);
                if (value != null) {
                    String replacedValue = doReplace(value, "${", "}");
                    replacedValue = doReplace(replacedValue, "%", "%");
                    if (!replacedValue.equals(value)) {
                        listOfOrderParameters.put(key, replacedValue);
                    }
                }
            }
        }
    }

    public void substituteTaskParamters() {
        addSubstituterValues(listOfSchedulerParameters);
        addSubstituterValues(listOfTaskParameters);

        // Substitute the task parameter set ${param}
        if (listOfTaskParameters != null) {
            for (String key : listOfTaskParameters.keySet()) {
                String value = listOfTaskParameters.get(key);
                if (value != null) {
                    String replacedValue = doReplace(value, "${", "}");
                    replacedValue = doReplace(replacedValue, "%", "%");
                    if (!replacedValue.equals(value)) {
                        listOfTaskParameters.put(key, replacedValue);
                    }
                }
            }
        }
    }

    public String getFileContent() throws IOException {
        if (jobChainNodeConfigurationFile != null && jobChainNodeConfigurationFile.exists()) {
            jobChainNodeConfigurationFile.close();
            return jobChainNodeConfigurationFile.getContent();
        } else {
            return orderPayload;
        }
    }

    public void setJobChainNodeConfigurationFileName(String jobChainNodeConfigurationFileName) {
        this.jobChainNodeConfigurationFileName = jobChainNodeConfigurationFileName.trim();
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setOrderPayload(String orderPayload) {
        this.orderPayload = orderPayload;
    }

    public void setLiveFolder(String liveFolder) {
        this.liveFolder = liveFolder;
    }

    public void setListOfOrderParameters(Map<String, String> listOfOrderParameters) {
        this.listOfOrderParameters = listOfOrderParameters;
    }

    public void setListOfSchedulerParameters(Map<String, String> listOfSchedulerParameters) {
        this.listOfSchedulerParameters = listOfSchedulerParameters;
    }

    public Map<String, String> getListOfSchedulerParameters() {
        return listOfSchedulerParameters;
    }

    public Map<String, String> getListOfOrderParameters() {
        return listOfOrderParameters;
    }

    public void setJobChainPath(String jobChainPath) {
        this.jobChainPath = jobChainPath;
    }

    public Map<String, String> getListOfTaskParameters() {
        return listOfTaskParameters;
    }

    public void setListOfTaskParameters(Map<String, String> listOfTaskParameters) {
        this.listOfTaskParameters = listOfTaskParameters;
    }

    
    public ParameterSubstitutor getParameterSubstitutor() {
        if (parameterSubstitutor == null) {
            parameterSubstitutor = new ParameterSubstitutor();
        }
        return parameterSubstitutor;
    }

}
