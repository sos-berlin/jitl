package sos.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.search.SubjectTerm;

import org.apache.log4j.Logger;

import sos.mail.options.SOSMailProcessInboxOptions;
import sos.net.SOSMailAttachment;
import sos.net.SOSMailReceiver;
import sos.net.SOSMimeMessage;
import sos.scheduler.command.SOSSchedulerCommand;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSSchedulerLogger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/**
 *
 */
public class SOSMailProcessInbox extends JobSchedulerJobAdapter {

	@SuppressWarnings("unused")
	private final String				conClassName		= this.getClass().getSimpleName();
	private final Logger				logger				= Logger.getLogger(this.getClass());
	private final String				conSVNVersion		= "$Id: SOSMailProcessInbox.java 21407 2013-11-24 10:31:53Z kb $";

	private SOSMailReceiver				objMailReader;
	private SOSMailProcessInboxOptions	objO				= null;
	private boolean						isLocalScheduler	= true;
	private long						lngMessagesSkipped	= 0;

	@SuppressWarnings("deprecation")
	@Override
	public boolean spooler_process() throws Exception {
		long lngProcessCount = 0;

		try {
			super.spooler_process();
			logger.info(conSVNVersion);
			objO = new SOSMailProcessInboxOptions();
			objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
			setStateText("*** running ***");
			spooler_job.set_state_text("*** running ***");

			if (!objO.mail_scheduler_host.isDirty()) {
				objO.mail_scheduler_host.Value(spooler.hostname());
				if (!objO.mail_scheduler_port.isDirty()) {
					objO.mail_scheduler_port.value(spooler.tcp_port());
				}
			}

			if (objO.mail_use_seen.value()) {
				objO.mail_set_seen.value(true);
			}

			objO.CheckMandatory();
			logger.info(objO.dirtyString());
			isLocalScheduler = objO.mail_scheduler_host.Value().equalsIgnoreCase(spooler.hostname()) && objO.mail_scheduler_port.value() == spooler.tcp_port();

			Date dteMinAge = null; // TODO Select mails to process by age (http://www.sos-berlin.com/jira/browse/JITL-36)
			boolean flgCheckdate = false;
			if (objO.MinAge.isDirty() == true) {
				String strT = objO.MinAge.Value();
				if (strT.startsWith("-") == false) {
					objO.MinAge.Value("-" + strT);
				}
				dteMinAge = objO.MinAge.getEndFromNow();
				logger.info(String.format("Min Age defined: %1$s", dteMinAge.toLocaleString()));
				flgCheckdate = true;
			}

			String strMailHost = objO.mail_host.Value();
			int intMailPort = objO.mail_port.value();
			String strServerType = objO.mail_server_type.Value();
			logger.debug(String.format("Connecting to Mailserver %1$s:%2$d (%3$s)...", strMailHost, intMailPort, strServerType));

			objMailReader = new SOSMailReceiver(objO.mail_host.Value(), objO.mail_port.Value(), objO.mail_user.Value(), objO.mailPassword.Value());
			objMailReader.setLogger(new SOSSchedulerLogger(spooler_log));
			if (objO.mail_server_timeout.value() > 0) {
				objMailReader.setTimeout(objO.mail_server_timeout.value());
			}
			objMailReader.connect(objO.mail_server_type.Value());

			// TODO process more than one folders (JITL-37)

			for (String strMailFolderName : objO.mail_message_folder.Value().split("[,|;]")) {
				spooler_job.set_state_text(String.format("processing folder %1$s", strMailFolderName));
				ArrayList<SOSMimeMessage> messages = findMessages(strMailFolderName.trim());

				if (messages != null && !messages.isEmpty()) {
					SOSMimeMessage newestMail = messages.get(0);
					for (SOSMimeMessage sosMimeMessage : messages) {
						Date messageDate = sosMimeMessage.getSentDate();
						if (messageDate != null) {
							logger.info(sosMimeMessage.getSubject() + " " + messageDate.toLocaleString());
							if (messageDate.after(newestMail.getSentDate())) {
								newestMail = sosMimeMessage;
							}
						}
						if (flgCheckdate && messageDate != null) {
							if (dteMinAge.before(messageDate)) {
								logger.debug("message skipped due to date constraint: \n" + sosMimeMessage.getSubject() + " " + messageDate);
								lngMessagesSkipped++;
								continue;
							}
						}
						performAction(sosMimeMessage);
						lngProcessCount++;
					}
					if (objMailReader != null) {
						objMailReader.closeFolder(true);
					}
				}
			}
		}
		catch (Exception e) {
			String stateText = e.toString();
			spooler_job.set_state_text(stateText);
			logger.info("Job " + spooler_job.name() + " terminated with errors.");
			logger.error("an error occurred while processing: " + stateText);
			throw new JobSchedulerException("", e);
		}
		finally {
			if (objMailReader != null) {
				objMailReader.closeFolder(true);
				objMailReader.disconnect();
				objMailReader = null;
			}
		}
		logger.info(String.format("%1$d objects processed \n%2$d objects skipped", lngProcessCount, lngMessagesSkipped));
		spooler_job.set_state_text("*** completed ***");
		return signalSuccess();
	}

	/**
	 *
	*
	* \brief performAction
	*
	* \details
	*
	* \return void
	*
	 */
	private void performAction(final SOSMimeMessage message) throws Exception {
		String action = "";
		StringTokenizer t = new StringTokenizer(objO.mail_action.Value(), ",");
		while (t.hasMoreTokens()) {
			action = t.nextToken();
			if (action.equalsIgnoreCase("dump"))
				dumpMessage(message);

			if (action.equalsIgnoreCase("order"))
				startOrder(message);

			if (action.equalsIgnoreCase("command"))
				executeCommand(message);

			if (action.equalsIgnoreCase("delete"))
				deleteMessage(message);

			if (objO.mail_set_seen.value()) {
				message.setFlag(Flags.Flag.SEEN, true);
			}
		}
	}

	/**
	 *
	*
	* \brief findMessages
	*
	* \details
	*
	* \return ArrayList<SOSMimeMessage>
	*
	 */
	private ArrayList<SOSMimeMessage> findMessages(final String pstrMessageFolder) throws Exception {
		// TODO implement alternatively a callback solution to avoid huge arrays
		ArrayList<SOSMimeMessage> arrMessages = new ArrayList<SOSMimeMessage>();
		try {

			logger.debug("reading " + pstrMessageFolder);
			Folder folder = objMailReader.openFolder(pstrMessageFolder, objMailReader.READ_WRITE);
			int intMaxObjectsToProcess = folder.getMessageCount();
			SubjectTerm term = null;
			Message[] msgs = null;
			Message[] msgs2 = null;
			term = new SubjectTerm(objO.mail_subject_filter.Value());
			int intBufferSize = 1000; // TODO use Option BufferSize to define the maximum number of messages to process at once
			if (intMaxObjectsToProcess > intBufferSize) {
				intMaxObjectsToProcess = intBufferSize;
			}
			msgs = folder.getMessages(1, intMaxObjectsToProcess);
			if (objO.mail_subject_filter.IsNotEmpty()) {
				logger.debug(String.format("looking for %1$s", objO.mail_subject_filter.Value()));
				msgs2 = folder.search(term, msgs);
				logger.debug(String.format("%1$s messages found with %2$s", msgs2.length, objO.mail_subject_filter.Value()));
			}
			else {
				msgs2 = msgs;
				logger.debug(msgs2.length + " messages found, folder = " + pstrMessageFolder);
			}
			if (msgs2.length > 0) {
				for (Message objMessageElement : msgs2) {
					if (objO.mail_use_seen.value() && objMessageElement.isSet(Flags.Flag.SEEN)) {
						logger.info("message skipped, already seen: " + objMessageElement.getSubject());
						lngMessagesSkipped++;
						continue;
					}
					try {
						SOSMimeMessage objSOSMailItem = new SOSMimeMessage(objMessageElement, new SOSSchedulerLogger(spooler_log));
						// skip mails that do not match the subject pattern
						if (objO.mail_subject_pattern.IsNotEmpty()) {
							objO.mail_subject_pattern.setRegExpFlags(0);
							Matcher subjectMatcher = objO.mail_subject_pattern.getPattern().matcher(objSOSMailItem.getSubject());
							if (!subjectMatcher.find()) {
								logger.info(String.format("message skipped, subject does not match [%1$s]: %2$s", objO.mail_subject_pattern.Value(),
										objSOSMailItem.getSubject()));
								lngMessagesSkipped++;
								continue;
							}
						}

						if (objO.mail_body_pattern.IsNotEmpty()) {
							objO.mail_body_pattern.setRegExpFlags(0);
							Matcher bodyMatcher = objO.mail_body_pattern.getPattern().matcher(objSOSMailItem.getPlainTextBody());
							if (!bodyMatcher.find()) {
								logger.info(String.format("message skipped, body does not match [%1$s]: %2$s", objO.mail_body_pattern.Value(),
										objSOSMailItem.getPlainTextBody()));
								lngMessagesSkipped++;
								continue;
							}
						}
						arrMessages.add(objSOSMailItem);
					}
					catch (Exception e) {
						e.printStackTrace();
						logger.info("message skipped, exception occured: " + objMessageElement.getSubject());
						lngMessagesSkipped++;
						continue;
					}
				}
			}
		}
		catch (Exception e) {
			if (e instanceof JobSchedulerException) {
				throw e;
			}
			throw new JobSchedulerException("Error occured querying mail server. " + e, e);
		}
		finally {
		}
		return arrMessages;
	}

	/**
	 *
	*
	* \brief executeCommand
	*
	* \details
	*
	* \return void
	*
	 */
	private void executeCommand(final SOSMimeMessage message) throws Exception {
		String strText = message.getPlainTextBody();
		if (strText != null && strText.length() > 0) {
			if (isLocalScheduler) {
				logger.debug("...host/port is this host and port. Using API");
				spooler.execute_xml(strText);
			}
			else {
				executeXml(strText);
			}
		}
	}

	/**
	 *
	*
	* \brief dumpMessage
	*
	* \details
	*
	* \return void
	*
	 */
	private void dumpMessage(final SOSMimeMessage message) throws Exception {
		if (objO.mail_dump_dir.IsNotEmpty()) {
			throw new JobSchedulerException("No output directory specified.");
		}
		File messageFile = new File(objO.mail_dump_dir.Value(), message.getMessageId());
		logger.debug("saving message to file: " + messageFile.getAbsolutePath());
		message.dumpMessageToFile(messageFile, true, false);
	}

	/**
	 *
	*
	* \brief deleteMessage
	*
	* \details
	*
	* \return void
	*
	 */
	private void deleteMessage(final SOSMimeMessage message) throws Exception {
		logger.debug("deleting message : " + message.getSubject() + " " + message.getSentDateAsString());
		message.deleteMessage();
	}

	/**
	 *
	*
	* \brief startOrder
	*
	* \details
	*
	* \return void
	*
	 */
	private void startOrder(final SOSMimeMessage message) throws Exception {
		String jobchain = objO.mail_jobchain.Value();
		Variable_set objReturnParams = spooler.create_variable_set();
		logger.debug("....merge");
		objReturnParams.merge(spooler_task.params());
		objReturnParams.set_var("mail_from", message.getFrom());

		if (message.getFromName() != null) {
			objReturnParams.set_var("mail_from_name", message.getFromName());
		}
		else {
			objReturnParams.set_var("mail_from_name", "");
		}
		objReturnParams.set_var("mail_message_id", message.getMessageId());
		objReturnParams.set_var("mail_subject", message.getSubject());
		objReturnParams.set_var("mail_body", message.getPlainTextBody());
		objReturnParams.set_var("mail_send_at", message.getSentDateAsString());

		Vector<SOSMailAttachment> lstAttachments = message.getSosMailAttachments();

		if (isLocalScheduler) {
			logger.debug("...host/port is this host and port. Using API");
			Job_chain objJobChain = spooler.job_chain(jobchain);
			logger.debug("...jobchain " + jobchain + " object created.");
			Order objNewOrder = spooler.create_order();
			objNewOrder.params().merge(objReturnParams);
			// if (!id.equals(""))o.set_id(id);
			logger.debug("...order " + objNewOrder.id() + " object created.");
			if (objO.mail_order_state.IsNotEmpty())
				objNewOrder.set_state(objO.mail_order_state.Value());
			if (objO.mail_order_title.IsNotEmpty())
				objNewOrder.set_title(objO.mail_order_title.Value());
			objJobChain.add_order(objNewOrder);
			logger.debug("...order added to " + jobchain);
		}
		else {
			startOrderXML(objReturnParams);
		}
	}

	/**
	 *
	*
	* \brief startOrderXML
	*
	* \details
	*
	* \return void
	*
	 */
	private void startOrderXML(final Variable_set params_) throws Exception {
		String id = objO.mail_order_id.Value();
		String state = objO.mail_order_state.Value();
		String title = objO.mail_order_title.Value();
		String jobchain = objO.mail_jobchain.Value();
		logger.debug("Starting order " + id + " at " + jobchain + " with xml-command");
		if (objO.mail_scheduler_host.IsEmpty())
			throw new Exception("Missing host while starting order.");
		if (objO.mail_order_id.IsNotEmpty())
			id = " id=\"" + id + "\"";
		if (objO.mail_order_state.IsNotEmpty())
			state = " state=\"" + state + "\"";
		if (objO.mail_order_title.IsNotEmpty())
			title = " title=\"" + title + "\"";
		String xml = "<add_order replace=\"yes\"" + id + title + state + " job_chain=\"" + jobchain + "\"><params>";
		if (params_ != null && params_.xml() != null && params_.xml().length() > 0) {
			String pparamsXml = params_.xml();
			int begin = pparamsXml.indexOf("<sos.spooler.variable_set>") + 26;
			int end = pparamsXml.lastIndexOf("</sos.spooler.variable_set>");
			if (begin >= 26 && end >= 26)
				xml += pparamsXml.substring(begin, end).replaceAll("variable", "param");
		}
		xml += "</params></add_order>";
		executeXml(xml);
	}

	/**
	 *
	*
	* \brief executeXml
	*
	* \details
	*
	* \return void
	*
	 */
	private void executeXml(final String xml) throws Exception {
		SOSSchedulerCommand command;
		command = new SOSSchedulerCommand(objO.mail_scheduler_host.Value(), objO.mail_scheduler_port.value());
		command.setProtocol("udp");
		logger.debug("Trying connection to " + objO.mail_scheduler_host.Value() + ":" + objO.mail_scheduler_port.Value());
		command.connect();
		logger.debug("...connected");
		logger.debug("Sending add_order command:\n" + xml);
		command.sendRequest(xml);
	}
}
