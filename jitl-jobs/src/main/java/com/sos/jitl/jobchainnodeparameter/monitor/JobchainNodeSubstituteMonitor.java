package com.sos.jitl.jobchainnodeparameter.monitor;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.IMonitor_impl;
import sos.spooler.Order;
import sos.spooler.Variable_set;

public class JobchainNodeSubstituteMonitor extends JobSchedulerJobAdapter implements IMonitor_impl {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobchainNodeSubstituteMonitor.class);
    private JobchainNodeSubstitute jobchainNodeSubstitute;

    @Override
    public boolean spooler_process_before() throws Exception {
        try {
            super.spooler_process_before();
            doProcessingBefore();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
        return true;
    }

    @Override
    public boolean spooler_process_after(final boolean rc) throws Exception {
        try {
            Variable_set resultParameters = spooler.create_variable_set();
            if (spooler_task.job().order_queue() != null) {
                Order order = spooler_task.order();
                Variable_set orderParams = order.params();
                String[] parameterNames = orderParams.names().split(";");
                for (String paramName : parameterNames) {
                    if (!"".equals(paramName) && jobchainNodeSubstitute.getJobchainNodeConfiguration().getJobchainNodeParameterValue(
                            paramName) == null) {
                        String paramValue = orderParams.value(paramName);
                        LOGGER.debug(String.format("set '%1$s' to value '%2$s'", paramName, paramValue));
                        resultParameters.set_var(paramName, paramValue);
                    }
                }
                order.set_params(resultParameters);
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
        return rc;
    }

    private void doProcessingBefore() throws Exception {
        jobchainNodeSubstitute = new JobchainNodeSubstitute();
        JobchainNodeSubstituteOptions configurationMonitorOptions = jobchainNodeSubstitute.getOptions();

        Variable_set v = spooler.create_variable_set();
        v.merge(spooler_task.params());

        Order order = spooler_task.order();
        boolean isJobChain = order != null;
        Variable_set orderParams = null;
        if (isJobChain) {
            orderParams = order.params();
            v.merge(orderParams);
        }
        if (!"".equals(v.value("configurationMonitor_configuration_path"))) {
            configurationMonitorOptions.configurationMonitorConfigurationPath.setValue(v.value("configurationMonitor_configuration_path"));
        }

        if (!"".equals(v.value("configurationMonitor_configuration_file"))) {
            configurationMonitorOptions.configurationMonitorConfigurationFile.setValue(v.value("configurationMonitor_configuration_file"));
        }

        if (isJobChain) {
            LOGGER.debug("Setting job_chain_name: " + order.job_chain().path());
            configurationMonitorOptions.setCurrentNodeName(this.getCurrentNodeName(order, false));
            jobchainNodeSubstitute.setOrderId(order.id());
            jobchainNodeSubstitute.setJobChainPath(order.job_chain().path());
            jobchainNodeSubstitute.setOrderPayload(order.xml_payload());
            jobchainNodeSubstitute.setOrderParameters(convertVariableSet2HashMap(orderParams));
        }

        jobchainNodeSubstitute.setSchedulerParameters(convertVariableSet2HashMap(spooler.variables()));
        jobchainNodeSubstitute.setTaskParameters(convertVariableSet2HashMap(spooler_task.params()));

        if (!configurationMonitorOptions.configurationMonitorConfigurationPath.isDirty()) {
            configurationMonitorOptions.configurationMonitorConfigurationPath.setValue(spooler.configuration_directory());
        }

        jobchainNodeSubstitute.execute();

        if (isJobChain && !"".equals(jobchainNodeSubstitute.getFileContent())) {
            spooler_task.order().set_xml_payload(jobchainNodeSubstitute.getFileContent());
        }

        for (Entry<String, String> entry : jobchainNodeSubstitute.getJobchainNodeConfiguration().getListOfTaskParameters().entrySet()) {
            String paramName = entry.getKey();
            String paramValue = entry.getValue();
            if (paramValue != null) {
                LOGGER.debug("Replace task parameter " + paramName + " old value=" + spooler_task.params().value(paramName) + " with new value="
                        + paramValue);
                spooler_task.params().set_value(paramName, paramValue);
            }
        }

        if (isJobChain) {
            for (Entry<String, String> entry : jobchainNodeSubstitute.getJobchainNodeConfiguration().getListOfOrderParameters().entrySet()) {
                String paramName = entry.getKey();
                String paramValue = entry.getValue();
                if (paramValue != null) {
                    LOGGER.debug("Replace order parameter " + paramName + " old value=" + orderParams.value(paramName) + " with new value="
                            + paramValue);
                    orderParams.set_var(paramName, paramValue);
                }
            }
        }
    }
}
