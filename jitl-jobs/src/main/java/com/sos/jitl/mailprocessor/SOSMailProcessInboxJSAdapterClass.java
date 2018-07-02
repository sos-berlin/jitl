package com.sos.jitl.mailprocessor;

import com.sos.jitl.mailprocessor.SOSMailProcessInbox;
import com.sos.jitl.mailprocessor.SOSMailProcessInboxOptions;

import sos.net.SOSMimeMessage;
import sos.scheduler.command.SOSSchedulerCommand;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSMailProcessInboxJSAdapterClass extends JobSchedulerJobAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(SOSMailProcessInboxJSAdapterClass.class);
	private SOSMailProcessInboxOptions sosMailProcessInboxOptions;

	@Override
	public boolean spooler_process() throws Exception {
		try {
			super.spooler_process();
			doProcessing();
		} catch (Exception e) {
			throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
		} finally {
		}
		return signalSuccess();
	}

	private void doProcessing() throws Exception {
		spooler_job.set_state_text("*** running ***");
		int httpPort = SOSSchedulerCommand.getHTTPPortFromScheduler(spooler);

		SOSMailProcessInbox sosMailProcessInbox = new SOSMailProcessInbox();
		sosMailProcessInboxOptions = sosMailProcessInbox.getOptions();
		sosMailProcessInboxOptions.setCurrentNodeName(this.getCurrentNodeName());
		sosMailProcessInboxOptions.setAllOptions(getSchedulerParameterAsProperties());

		if (sosMailProcessInboxOptions.mailSchedulerHost.isNotDirty()) {
			sosMailProcessInboxOptions.mailSchedulerHost.setValue(spooler.hostname());
			if (sosMailProcessInboxOptions.mailSchedulerPort.isNotDirty()) {
				sosMailProcessInboxOptions.mailSchedulerPort.value(httpPort);
			}
		}

		sosMailProcessInboxOptions.checkMandatory();
		sosMailProcessInbox.setJSJobUtilites(this);
		boolean isLocalScheduler = sosMailProcessInboxOptions.mailSchedulerHost.getValue().equalsIgnoreCase(
				spooler.hostname()) && sosMailProcessInboxOptions.mailSchedulerPort.value() == httpPort;

		sosMailProcessInbox.execute();
		for (PostproccesingEntry postproccesingEntry : sosMailProcessInbox.getListOfPostprocessing()) {
			if (postproccesingEntry.isAddOrder()) {
				LOGGER.info("Will add order");
				startOrder(postproccesingEntry.getSosMimeMessage());
			}
			if (postproccesingEntry.isExecuteCommand()) {
				LOGGER.info("Will execute command");
				executeCommand(postproccesingEntry.getSosMimeMessage());
			}
		}
	}

	private void startOrder(final SOSMimeMessage message) throws Exception {
		String jobchain = sosMailProcessInboxOptions.mailJobchain.getValue();
		Variable_set returnParams = spooler.create_variable_set();
		spooler_log.debug("....merge");
		returnParams.merge(spooler_task.params());
		returnParams.set_var("mail_from", message.getFrom());
		if (message.getFromName() != null) {
			returnParams.set_var("mail_from_name", message.getFromName());
		} else {
			returnParams.set_var("mail_from_name", "");
		}
		returnParams.set_var("mail_message_id", message.getMessageId());
		returnParams.set_var("mail_subject", message.getSubject());
		returnParams.set_var("mail_body", message.getPlainTextBody());
		returnParams.set_var("mail_send_at", message.getSentDateAsString());
		Job_chain objJobChain = spooler.job_chain(jobchain);
		spooler_log.debug("...jobchain " + jobchain + " object created.");
		Order newOrder = spooler.create_order();
		newOrder.params().merge(returnParams);
		if (sosMailProcessInboxOptions.mailOrderState.isNotEmpty()) {
			newOrder.set_state(sosMailProcessInboxOptions.mailOrderState.getValue());
		}
		if (sosMailProcessInboxOptions.mailOrderTitle.isNotEmpty()) {
			newOrder.set_title(sosMailProcessInboxOptions.mailOrderTitle.getValue());
		}
		objJobChain.add_order(newOrder);
		spooler_log.debug("...order added to " + jobchain);
	}

	private void executeCommand(final SOSMimeMessage message) throws Exception {
		String body = message.getPlainTextBody();
		if (body != null && !body.isEmpty()) {
			spooler.execute_xml(body);
		}
	}
}
