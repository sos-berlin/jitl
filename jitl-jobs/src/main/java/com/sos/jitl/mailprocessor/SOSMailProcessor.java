package com.sos.jitl.mailprocessor;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.search.SubjectTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.net.SOSMailReceiver;
import sos.net.SOSMimeMessage;

public class SOSMailProcessor {

	public List<PostproccesingEntry> getListOfPostprocessing() {
		return listOfPostprocessing;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(SOSMailProcessor.class);
	private SOSMailProcessInboxOptions sosMailProcessInboxOptions = null;
	private Date dateMinAge = new Date();
	private List<PostproccesingEntry> listOfPostprocessing;
	Folder inFolder;
	Folder targetFolder;

	public SOSMailProcessor(SOSMailProcessInboxOptions sosMailProcessInboxOptions) {
		super();
		listOfPostprocessing = new ArrayList<PostproccesingEntry>();
		this.sosMailProcessInboxOptions = sosMailProcessInboxOptions;
	}

	private boolean checkDate() {
		boolean result = false;
		if (sosMailProcessInboxOptions.minAge.isDirty()) {
			String strT = sosMailProcessInboxOptions.minAge.getValue();
			if (!strT.startsWith("-")) {
				sosMailProcessInboxOptions.minAge.setValue("-" + strT);
			}
			dateMinAge = sosMailProcessInboxOptions.minAge.getEndFromNow();
			LOGGER.info(String.format("Min Age defined: %1$s", dateMinAge.toString()));
			result = true;
		}
		return result;
	}

	private boolean isPerformMessage(SOSMimeMessage sosMimeMessage) throws Exception {
		Date messageDate = sosMimeMessage.getSentDate();
		boolean result = true;
		if (messageDate != null) {
			LOGGER.info(sosMimeMessage.getSubject() + " " + messageDate.toString());
		}
		if (checkDate() && messageDate != null && dateMinAge.before(messageDate)) {
			LOGGER.debug(
					"message skipped due to date constraint: \n" + sosMimeMessage.getSubject() + " " + messageDate);
			result = false;
		}
		return result;
	}

	private void executeMessage(SOSMimeMessage sosMimeMessage) throws Exception {
		if (isPerformMessage(sosMimeMessage)) {
			performAction(sosMimeMessage);
		}
	}

	private void performAction(final SOSMimeMessage message) throws Exception {
		String action = "";
		String actions = sosMailProcessInboxOptions.mailAction.getValue();
		boolean copyMail2File = sosMailProcessInboxOptions.getCopyMail2File().value();
		boolean createOrder = sosMailProcessInboxOptions.getCreateOrder().value();
		boolean executeCommand = sosMailProcessInboxOptions.getExecuteCommand().value();
		boolean deleteMessage = sosMailProcessInboxOptions.getDeleteMessage().value();
		boolean copyAttachmentsToFile = sosMailProcessInboxOptions.getCopyAttachmentsToFile().value();

		StringTokenizer t = new StringTokenizer(actions, ",");
		while (t.hasMoreTokens()) {
			action = t.nextToken();
			if ("dump".equalsIgnoreCase(action) || "copy_mail_to_file".equalsIgnoreCase(action)) {
				copyMail2File = true;
			} else if ("order".equalsIgnoreCase(action) || "create_order".equalsIgnoreCase(action)) {
				createOrder = true;
			} else if ("command".equalsIgnoreCase(action)) {
				executeCommand = true;
			} else if ("delete".equalsIgnoreCase(action)) {
				deleteMessage = true;
			} else if ("copy_attachments_to_file".equalsIgnoreCase(action)) {
				copyAttachmentsToFile = true;
			}
		}

		if (copyMail2File) {
			if (sosMailProcessInboxOptions.mailDirectoryName.IsEmpty()) {
				throw new JobSchedulerException("No output directory [parameter mail_directory_name] specified.");
			}
			dumpMessage(message, sosMailProcessInboxOptions.mailDirectoryName.getValue());
		}
		
		if (createOrder) {
			startOrder(message);
		}
		
		if (executeCommand) {
			executeCommand(message);		
		}

		if (copyAttachmentsToFile) {
			copyAttachmentsToFile(message);
		}

		if (deleteMessage) {
			deleteMessage(message);
		} else {
			handleAfterProcessEmail(message);
		}

	}

	private void copyMailToFolder(SOSMimeMessage message) throws RuntimeException, Exception {
		if (targetFolder != null) {
			List<Message> tempList = new ArrayList<>();
			tempList.add(message.getMessage());
			Message[] m = tempList.toArray(new Message[tempList.size()]);
			inFolder.copyMessages(m, targetFolder);
		} else {
			dumpMessage(message, sosMailProcessInboxOptions.afterProcessEmailDirectoryName.getValue());
		}

	}

	private void handleAfterProcessEmail(SOSMimeMessage message) throws Exception {

		if ("markAsRead".equals(sosMailProcessInboxOptions.getAfterProcessEmail().getValue())) {
			message.setFlag(Flags.Flag.SEEN, true);
		}

		if ("delete".equals(sosMailProcessInboxOptions.getAfterProcessEmail().getValue())) {
			deleteMessage(message);
		}

		if ("move".equals(sosMailProcessInboxOptions.getAfterProcessEmail().getValue())) {
			if (sosMailProcessInboxOptions.afterProcessEmailDirectoryName.getValue().isEmpty()) {
				throw new JobSchedulerException("No output directory [parameter after_process_email_directory_name] specified.");
			}

			copyMailToFolder(message);
			deleteMessage(message);
		}
		if ("copy".equals(sosMailProcessInboxOptions.getAfterProcessEmail().getValue())) {
			if (sosMailProcessInboxOptions.afterProcessEmailDirectoryName.getValue().isEmpty()) {
				throw new JobSchedulerException("No output directory [parameter after_process_email_directory_name] specified.");
			}
			copyMailToFolder(message);
		}
	}

	private void executeCommand(final SOSMimeMessage message) throws Exception {
		LOGGER.debug(String.format("execute command. subject=%s, date=%s", message.getSubject(),
				message.getSentDateAsString()));

		PostproccesingEntry postproccesingEntry = new PostproccesingEntry();
		postproccesingEntry.setAddOrder(false);
		postproccesingEntry.setExecuteCommand(true);
		postproccesingEntry.setSosMimeMessage(message);
		listOfPostprocessing.add(postproccesingEntry);
	}

	private void startOrder(final SOSMimeMessage message) throws Exception {
		LOGGER.debug(
				String.format("add order. subject=%s, date=%s", message.getSubject(), message.getSentDateAsString()));

		PostproccesingEntry postproccesingEntry = new PostproccesingEntry();
		postproccesingEntry.setAddOrder(true);
		postproccesingEntry.setExecuteCommand(false);
		postproccesingEntry.setSosMimeMessage(message);
		listOfPostprocessing.add(postproccesingEntry);
	}

	private String getEmailFolderName(String folder) {
		String s = "";
		if (folder.toLowerCase().startsWith("email:")) {
			s = folder.replace("email:", "");
		}
		return s;
	}

	public void performMessagesInFolder(SOSMailReceiver mailReader, final String messageFolder) throws Exception {
		LOGGER.debug("reading " + messageFolder);
		inFolder = mailReader.openFolder(messageFolder, mailReader.READ_WRITE);

		String targetFolderName = getEmailFolderName(
				sosMailProcessInboxOptions.afterProcessEmailDirectoryName.getValue());
		if (!targetFolderName.isEmpty()) {
			targetFolder = mailReader.openFolder(targetFolderName, mailReader.READ_WRITE);
		}
		int maxObjectsToProcess = inFolder.getMessageCount();
		SubjectTerm term = null;
		Message[] msgs = null;
		Message[] msgs2 = null;
		term = new SubjectTerm(sosMailProcessInboxOptions.mailSubjectFilter.getValue());
		int intBufferSize = sosMailProcessInboxOptions.maxMailsToProcess.value();
		 
		if (maxObjectsToProcess > intBufferSize && intBufferSize > 0) {
			maxObjectsToProcess = intBufferSize;
		}
		msgs = inFolder.getMessages(1, maxObjectsToProcess);
        Pattern subjectPattern = null;
        Pattern bodyPattern = null;
        // to compile the pattern only once before the loop and not in the loop with each round
        if (sosMailProcessInboxOptions.mailSubjectPattern.isNotEmpty()) {
            sosMailProcessInboxOptions.mailSubjectPattern.setRegExpFlags(0);
            subjectPattern  = sosMailProcessInboxOptions.mailSubjectPattern.getPattern();
        }
        if (sosMailProcessInboxOptions.mailBodyPattern.isNotEmpty()) {
            sosMailProcessInboxOptions.mailBodyPattern.setRegExpFlags(0);
            bodyPattern = sosMailProcessInboxOptions.mailBodyPattern.getPattern();
        }

        if (sosMailProcessInboxOptions.mailSubjectFilter.isNotEmpty()) {
			LOGGER.debug(String.format("looking for %1$s", sosMailProcessInboxOptions.mailSubjectFilter.getValue()));
			msgs2 = inFolder.search(term, msgs);
			LOGGER.debug(String.format("%1$s messages found with %2$s", msgs2.length,
					sosMailProcessInboxOptions.mailSubjectFilter.getValue()));
		} else {
			msgs2 = msgs;
			LOGGER.debug(msgs2.length + " messages found, folder = " + messageFolder);
		}
		
		if (msgs2.length > 0) {
			for (Message messageElement : msgs2) {
				if (sosMailProcessInboxOptions.mailUseSeen.value() && messageElement.isSet(Flags.Flag.SEEN)) {
					LOGGER.trace("message skipped, already seen: " + messageElement.getSubject());
					continue;
				}
				try {
				    // instantiate the mail item without additional information, so that not each mailItem has all information
				    // e.g. when checking the mail item with the patterns, we do not want to have all attachments loaded as well
					SOSMimeMessage sosMailItem = new SOSMimeMessage(messageElement, true);
					if (sosMailProcessInboxOptions.mailSubjectPattern.isNotEmpty()) {
						if (!subjectPattern.matcher(sosMailItem.getSubject()).find()) {
							LOGGER.trace(String.format("message skipped, subject does not match [%1$s]: %2$s",
									sosMailProcessInboxOptions.mailSubjectPattern.getValue(),
									sosMailItem.getSubject()));
							continue;
						}
					}
					if (sosMailProcessInboxOptions.mailBodyPattern.isNotEmpty()) {
						if (!bodyPattern.matcher(sosMailItem.getPlainTextBody()).find()) {
							LOGGER.trace(String.format("message skipped, body does not match [%1$s]: %2$s",
									sosMailProcessInboxOptions.mailBodyPattern.getValue(),
									sosMailItem.getPlainTextBody()));
							continue;
						}
					}
					// mailItem is used further, now is the time to initiate the additional information
					// e.g. if the mail has attachments we can load them now for further proccesing
					sosMailItem.init();
					executeMessage(sosMailItem);
				} catch (Exception e) {
				    LOGGER.info("message skipped, exception occured: " + messageElement.getSubject(), e);
//					LOGGER.info(e.getMessage()+ ":" + e.getCause());
					continue;
				}
			}
		}
	}

	private void dumpMessage(final SOSMimeMessage message, String directory) throws Exception {
		File messageFile = new File(directory, message.getMessageId());
		LOGGER.debug(String.format("dump message. subject=%s, date=%s, file=%s: ", message.getSubject(),
				message.getSentDateAsString(), messageFile));
		message.dumpMessageToFile(messageFile, true, false);
	}

	private void deleteMessage(final SOSMimeMessage message) throws Exception {
		LOGGER.debug(String.format("deleting message. subject=%s, date=%s", message.getSubject(),
				message.getSentDateAsString()));
		message.deleteMessage();
	}

	private void copyAttachmentsToFile(final SOSMimeMessage message) throws Exception {
		String directory = sosMailProcessInboxOptions.attachmentDirectoryName.getValue();
		LOGGER.debug(String.format("saving attachments. subject=%s, date=%s, directory=%s: ", message.getSubject(),
				message.getSentDateAsString(), directory));
		message.saveAttachments(message, sosMailProcessInboxOptions.attachmentFileNamePattern.getValue(), directory,
				sosMailProcessInboxOptions.saveBodyAsAttachments.value());
	}

}
