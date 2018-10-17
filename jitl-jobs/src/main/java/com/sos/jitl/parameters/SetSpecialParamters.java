package com.sos.jitl.parameters;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job_chain;
import sos.spooler.Job_chain_node;
import sos.spooler.Order;
import sos.spooler.Supervisor_client;

public class SetSpecialParamters extends JobSchedulerJobAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SetSpecialParamters.class);

	@Override
	public boolean spooler_process_before() throws Exception {
		LOGGER.debug("Starting spooler_process_before");

		try {

			if (spooler_job != null && getJobSettings() != null) {
				setJobProperties(getJobSettings().getSection("job " + spooler_job.name()));
			}
			if (spooler_task != null) {
				this.setJobId(spooler_task.id());
			}
			if (spooler_job != null) {
				this.setJobName(spooler_job.name());
			}
			if (spooler_job != null) {
				this.setJobFolder(spooler_job.folder_path());
			}
			if (spooler_job != null) {
				this.setJobTitle(spooler_job.title());
			}

			HashMap<String, String> specialParams = new HashMap<String, String>();

			specialParams.put("SCHEDULER_HOST", spooler.hostname());
			specialParams.put("SCHEDULER_TCP_PORT", "" + spooler.tcp_port());
			specialParams.put("SCHEDULER_UDP_PORT", "" + spooler.udp_port());
			specialParams.put("SCHEDULER_ID", spooler.id());
			specialParams.put("SCHEDULER_DIRECTORY", spooler.directory());
			specialParams.put("SCHEDULER_CONFIGURATION_DIRECTORY", spooler.configuration_directory());
			if (isJobchain()) {
				Order order = getOrder();
				Job_chain jobChain = order.job_chain();
				Job_chain_node jobChainNode = order.job_chain_node();
				specialParams.put("SCHEDULER_JOB_CHAIN_NAME", jobChain.name());
				specialParams.put("SCHEDULER_JOB_CHAIN_TITLE", jobChain.title());
				specialParams.put("SCHEDULER_JOB_CHAIN_PATH", jobChain.path());
				specialParams.put("SCHEDULER_ORDER_ID", order.id());
				specialParams.put("SCHEDULER_NODE_NAME", getCurrentNodeName(false));
				specialParams.put("SCHEDULER_NEXT_NODE_NAME", jobChainNode.next_state());
				specialParams.put("SCHEDULER_NEXT_ERROR_NODE_NAME", jobChainNode.error_state());
			}

			specialParams.put("SCHEDULER_JOB_NAME", this.getJobName());
			specialParams.put("SCHEDULER_JOB_FOLDER", this.getJobFolder());
			specialParams.put("SCHEDULER_JOB_PATH", this.getJobFolder() + "/" + this.getJobName());
			specialParams.put("SCHEDULER_JOB_TITLE", this.getJobTitle());
			specialParams.put("SCHEDULER_TASK_ID", "" + spooler_task.id());
			Supervisor_client supervisorClient;
			try {
				supervisorClient = spooler.supervisor_client();
				if (supervisorClient != null) {
					specialParams.put("SCHEDULER_SUPERVISOR_HOST", supervisorClient.hostname());
					specialParams.put("SCHEDULER_SUPERVISOR_PORT", "" + supervisorClient.tcp_port());
				}
			} catch (Exception e) {
				specialParams.put("SCHEDULER_SUPERVISOR_HOST", "n.a.");
				specialParams.put("SCHEDULER_SUPERVISOR_PORT", "n.a.");
			}
			
			for (Entry<String, String> e : specialParams.entrySet()) {
				if (e.getValue() == null) {
					specialParams.put(e.getKey(),"n.a.");
				}
			}

			if (spooler_task != null) {
				if (spooler_task.order() != null) {
					for (Entry<String, String> e : specialParams.entrySet()) {
						LOGGER.debug(e.getKey() + "=" + e.getValue());
						spooler_task.order().params().set_value(e.getKey(), e.getValue());
					}
				} else {
					for (Entry<String, String> e : specialParams.entrySet()) {
						spooler_task.params().set_value(e.getKey(), e.getValue());
					}
				}
			} else {
				for (Entry<String, String> e : specialParams.entrySet()) {
					LOGGER.info(e.getKey() + "=" + e.getValue());
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
		return true;

	}

}
