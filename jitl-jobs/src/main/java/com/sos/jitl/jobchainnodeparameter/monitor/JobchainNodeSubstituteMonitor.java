package com.sos.jitl.jobchainnodeparameter.monitor;

import java.util.Map.Entry;
import com.sos.jitl.jobchainnodeparameter.monitor.JobchainNodeSubstituteOptions;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.IMonitor_impl;
import sos.spooler.Variable_set;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobchainNodeSubstituteMonitor extends JobSchedulerJobAdapter implements IMonitor_impl {

	private static final String CLASSNAME = "ConfigurationMonitorJSAdapterClass";
	private static final Logger LOGGER = Logger.getLogger(JobchainNodeSubstituteMonitor.class);
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
				if (!"".equals(paramName) && jobchainNodeSubstitute.getJobchainNodeConfiguration()
						.getJobchainNodeParameterValue(paramName) == null) {
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
		if (this.isJobchain()) {
			v.merge(spooler_task.order().params());
		}
		if (!"".equals(v.value("configurationMonitor_configuration_path"))) {
			configurationMonitorOptions.configurationMonitorConfigurationPath
					.setValue(v.value("configurationMonitor_configuration_path"));
		}

		if (!"".equals(v.value("configurationMonitor_configuration_file"))) {
			configurationMonitorOptions.configurationMonitorConfigurationFile
					.setValue(v.value("configurationMonitor_configuration_file"));
		}

		if (this.isJobchain()) {
			LOGGER.debug("Setting job_chain_name: " + spooler_task.order().job_chain().path());
			configurationMonitorOptions.setCurrentNodeName(this.getCurrentNodeName());
			jobchainNodeSubstitute.setOrderId(spooler_task.order().id());
			jobchainNodeSubstitute.setJobChainPath(spooler_task.order().job_chain().path());
			jobchainNodeSubstitute.setOrderPayload(spooler_task.order().xml_payload());
			jobchainNodeSubstitute.setOrderParameters(convertVariableSet2HashMap(spooler_task.order().params()));
		}
		jobchainNodeSubstitute.setSchedulerParameters(convertVariableSet2HashMap(spooler.variables()));
		jobchainNodeSubstitute.setTaskParameters(convertVariableSet2HashMap(spooler_task.params()));

		if (!configurationMonitorOptions.configurationMonitorConfigurationPath.isDirty()) {
			configurationMonitorOptions.configurationMonitorConfigurationPath
					.setValue(spooler.configuration_directory());
		}

		jobchainNodeSubstitute.execute();
		
		if (this.isJobchain()) {
			spooler_task.order().set_xml_payload(jobchainNodeSubstitute.getFileContent());
		}

		for (Entry<String, String> entry : jobchainNodeSubstitute.getJobchainNodeConfiguration()
				.getListOfTaskParameters().entrySet()) {
			if (entry.getValue() != null) {
				if (this.isJobchain()) {
					spooler_task.order().params().set_var(entry.getKey(), entry.getValue());
				}
				spooler_task.params().set_value(entry.getKey(), entry.getValue());
			}
		}

		if (this.isJobchain()) {
			for (Entry<String, String> entry : jobchainNodeSubstitute.getJobchainNodeConfiguration()
					.getListOfOrderParameters().entrySet()) {
				if (entry.getValue() != null) {
					spooler_task.order().params().set_var(entry.getKey(), entry.getValue());
				}
			}
		}
	}
}
