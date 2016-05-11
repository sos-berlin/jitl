package com.sos.jitl.jobchainnodeparameter.monitor;

import java.util.HashMap;
import java.util.Map.Entry;

import com.sos.jitl.jobchainnodeparameter.monitor.JobchainNodeSubstituteOptions;

import sos.scheduler.job.JobSchedulerJobAdapter; // Super-Class for JobScheduler Java-API-Jobs
import sos.spooler.Variable_set;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobchainNodeSubstituteMonitor extends JobSchedulerJobAdapter {

    private static final String FILENAMEEXTENSIONCONFIG_XML = ".config.xml";

    private final String CLASSNAME = "ConfigurationMonitorJSAdapterClass";
    private static Logger LOGGER = Logger.getLogger(JobchainNodeSubstituteMonitor.class);
    private JobchainNodeSubstitute jobchainNodeSubstitute;

    @Override
    public boolean spooler_process_before() throws Exception {
        try {
            super.spooler_process_before();
            doProcessingBefore();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
        return signalSuccess();

    }

    @Override
    public boolean spooler_process_after(final boolean rc) throws Exception {
        try {
            Variable_set resultParameters = spooler.create_variable_set();
            String[] parameterNames = spooler_task.order().params().names().split(";");
            for (String paramName : parameterNames) {
                if (jobchainNodeSubstitute.getJobchainNodeConfiguration().getJobchainNodeParameterValue(paramName) == null) {
                    String paramValue = spooler_task.order().params().value(paramName);
                    LOGGER.debug(String.format("set '%1$s' to value '%2$s'", paramName, paramValue));
                    resultParameters.set_var(paramName, paramValue);
                }
            }
            if (spooler_task.order() != null) {
                spooler_task.order().set_params(resultParameters);
            }

        } catch (Exception e) {
            throw new JobSchedulerException(CLASSNAME + ": error occurred in monitor on cleanup: " + e.getMessage(), e);
        }
        return rc;
    }

    private void doProcessingBefore() throws Exception {
        jobchainNodeSubstitute = new JobchainNodeSubstitute();
        JobchainNodeSubstituteOptions configurationMonitorOptions = jobchainNodeSubstitute.getOptions();

        Variable_set v = spooler.create_variable_set();
        v.merge(spooler_task.params());
        v.merge(spooler_task.order().params());
        if (!"".equals(v.value("configurationMonitor_configuration_path"))) {
            configurationMonitorOptions.configurationMonitor_configuration_path.Value(v.value("configurationMonitor_configuration_path"));
        }

        if (!"".equals(v.value("configurationMonitor_configuration_file"))) {
            configurationMonitorOptions.configurationMonitor_configuration_file.Value(v.value("configurationMonitor_configuration_file"));
        }

        configurationMonitorOptions.CurrentNodeName(this.getCurrentNodeName());

        jobchainNodeSubstitute.setOrderId(spooler_task.order().id());
        jobchainNodeSubstitute.setJobChainName(spooler_task.order().job_chain().name());
        jobchainNodeSubstitute.setOrderPayload(spooler_task.order().payload().toString());
        jobchainNodeSubstitute.setTaskParameters(convertVariableSet2HashMap(spooler_task.params()));
        jobchainNodeSubstitute.setOrderParameters(convertVariableSet2HashMap(spooler_task.order().params()));

               
        if (!configurationMonitorOptions.configurationMonitor_configuration_path.isDirty()) {
            configurationMonitorOptions.configurationMonitor_configuration_path.Value(spooler.configuration_directory());
        }

        if (!configurationMonitorOptions.configurationMonitor_configuration_file.isDirty()) {
            String s = spooler_task.order().job_chain().path() + FILENAMEEXTENSIONCONFIG_XML;
            configurationMonitorOptions.configurationMonitor_configuration_file.Value(s);
        }

        jobchainNodeSubstitute.Execute();

        
        for (Entry<String, String> entry : jobchainNodeSubstitute.getJobchainNodeConfiguration().getListOfTaskParameters().entrySet()) {
            if (entry.getValue() != null) {
                spooler_task.order().params().set_var(entry.getKey(), entry.getValue());
                spooler_task.params().set_value(entry.getKey(), entry.getValue());
            }
        }
        
        for (Entry<String, String> entry : jobchainNodeSubstitute.getJobchainNodeConfiguration().getListOfOrderParameters().entrySet()) {
            if (entry.getValue() != null) {
                spooler_task.order().params().set_var(entry.getKey(), entry.getValue());
            }
        }

 
    }
}
