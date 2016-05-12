package com.sos.jitl.jobchainnodeparameter;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.sos.jitl.jobchainnodeparameter.model.JobChain;
import com.sos.jitl.jobchainnodeparameter.model.Param;
import com.sos.jitl.jobchainnodeparameter.model.Params;
import com.sos.jitl.jobchainnodeparameter.model.Process;
import com.sos.jitl.jobchainnodeparameter.model.Settings;

import sos.scheduler.misc.ParameterSubstitutor;

public class JobchainNodeConfiguration {
    private static final Logger LOGGER = Logger.getLogger(JobchainNodeConfiguration.class);

    private static final String FILENAMEEXTENSIONCONFIG_XML = ".config.xml";
    private static final String DEFAULTFILENAME4CACHE = "cache";

    private JAXBContext context;

    private String liveFolder;

    private String jobChainNodeConfigurationFileName;
    private String orderId;
    private String jobChainPath;
    private String orderPayload;

    private File jobChainNodeConfigurationFile;
    private Settings settings;
    private Params listOfJobchainParameters;
    private Params listOfJobchainNodeParameters;

    private Map<String, String> jobchainGlobalParameters;
    private Map<String, String> jobchainNodeParameters;
    private Map<String, String> jobchainParameters;

    private ParameterSubstitutor parameterSubstitutor;
    private Map<String, String> listOfOrderParameters;
    private Map<String, String> listOfTaskParameters;

    public JobchainNodeConfiguration() throws JAXBException {
        super();
        context = JAXBContext.newInstance(Settings.class);
    }

    public JobchainNodeConfiguration(File jobChainNodeConfigurationFile_) throws JAXBException {
        super();
        if (jobChainNodeConfigurationFile_.exists()) {
            context = JAXBContext.newInstance(Settings.class);
            this.jobChainNodeConfigurationFile = jobChainNodeConfigurationFile_;
        } else {
            LOGGER.warn(String.format("File %s does not exist", jobChainNodeConfigurationFile_.getAbsolutePath()));
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

    private File getFileFromCacheFolder(File configurationFile) {
        if (!configurationFile.exists()) {
            File fCacheBaseFolder = new File(configurationFile.getParentFile(), DEFAULTFILENAME4CACHE);
            return new File(fCacheBaseFolder, configurationFile.getName());
        }
        return configurationFile;
    }

    private void setJobChainNodeConfigurationFile() {
        String jobChainName = new File(jobChainPath).getName();
        String orderConfigurationFileName = jobChainName + "," + orderId + FILENAMEEXTENSIONCONFIG_XML;
        jobChainNodeConfigurationFile = new File(liveFolder, orderConfigurationFileName);
        jobChainNodeConfigurationFile = getFileFromCacheFolder(jobChainNodeConfigurationFile);

        if (!jobChainNodeConfigurationFile.exists()) {
            if (jobChainNodeConfigurationFileName == null || "".equals(jobChainNodeConfigurationFileName)) {
                jobChainNodeConfigurationFileName = jobChainPath + FILENAMEEXTENSIONCONFIG_XML;
            }
            jobChainNodeConfigurationFile = new File(liveFolder, jobChainNodeConfigurationFileName);
            jobChainNodeConfigurationFile = getFileFromCacheFolder(jobChainNodeConfigurationFile);

        }
        LOGGER.debug("Looking for job chain configuration path: " + jobChainNodeConfigurationFile.getAbsolutePath());

    }

    private void getParametersFromConfigFile() throws JAXBException {

        if (listOfJobchainParameters == null || listOfJobchainNodeParameters == null) {

            Unmarshaller unmarshaller = context.createUnmarshaller();
            if (jobChainNodeConfigurationFile.exists()) {
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
            jobchainGlobalParameters.put(param.getName(), param.getValue());
            jobchainParameters.put(param.getName(), param.getValue());
        }
    }

    private void getJobchainNodeParameters(String node) throws JAXBException {
        List<Process> processes = settings.getJobChain().getOrder().getProcess();
        for (Process process : processes) {
            if (process.getState().equals(node)) {
                listOfJobchainNodeParameters = process.getParams();
                for (Param param : listOfJobchainNodeParameters.getParam()) {
                    jobchainParameters.put(param.getName(), param.getValue());
                    jobchainNodeParameters.put(param.getName(), param.getValue());
                }
            }

        }
    }

    private void getParametersForNode(String node) throws Exception {
        jobchainGlobalParameters = new HashMap<String, String>();
        jobchainNodeParameters = new HashMap<String, String>();
        jobchainParameters = new HashMap<String, String>();

        if (jobChainNodeConfigurationFile == null) {
            setJobChainNodeConfigurationFile();
        }

        if (!"".equals(orderPayload) || jobChainNodeConfigurationFile != null) {
            getParametersFromConfigFile();
            getJobchainParameters();
            getJobchainNodeParameters(node);
        } else {
            throw new Exception("Please set the job chain configuration file");
        }
    }

    public String getJobchainGlobalParameterValue(String key) {
        return jobchainGlobalParameters.get(key);
    }

    public String getJobchainParameterValue(String key) {
        return jobchainParameters.get(key);
    }

    public String getJobchainNodeParameterValue(String key) {
        return jobchainNodeParameters.get(key);
    }

    private void addSubstituterValues(Map<String, String> h) {
        if (h != null) {
            if (parameterSubstitutor == null) {
                parameterSubstitutor = new ParameterSubstitutor();
            }
            for (Entry<String, String> entry : h.entrySet()) {
                String value = entry.getValue();
                String paramName = entry.getKey();
                if (value != null && !value.isEmpty()) {
                    parameterSubstitutor.addKey(paramName, value);
                }
            }
        }
    }

    public void substituteOrderParamters(String node) throws Exception {

        getParametersForNode(node);

        addSubstituterValues(listOfTaskParameters);
        addSubstituterValues(listOfOrderParameters);
        addSubstituterValues(jobchainParameters);

        // Make the node parameters available in the order parameter set.
        for (String key : jobchainParameters.keySet()) {
            String value = jobchainParameters.get(key);
            if (value != null) {
                listOfOrderParameters.put(key, value);
            }
        }

        // Substitute the task parameter set ${param}
        for (String key : listOfTaskParameters.keySet()) {
            String value = listOfTaskParameters.get(key);
            if (value != null) {
                String replacedValue = parameterSubstitutor.replaceEnvVars(value);
                replacedValue = parameterSubstitutor.replaceSystemProperties(value);
                replacedValue = parameterSubstitutor.replace(value);
                if (!replacedValue.equalsIgnoreCase(value)) {
                    listOfTaskParameters.put(key, replacedValue);
                }
            }
        }

        // Substitute the order parameter set ${param}
        for (String key : listOfOrderParameters.keySet()) {
            String value = listOfOrderParameters.get(key);
            if (value != null) {
                String replacedValue = parameterSubstitutor.replaceEnvVars(value);
                replacedValue = parameterSubstitutor.replaceSystemProperties(value);
                replacedValue = parameterSubstitutor.replace(value);
                if (!replacedValue.equalsIgnoreCase(value)) {
                    listOfOrderParameters.put(key, replacedValue);
                }
            }
        }
    }

    public void setJobChainNodeConfigurationFileName(String jobChainNodeConfigurationFileName) {
        this.jobChainNodeConfigurationFileName = jobChainNodeConfigurationFileName;
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

}
