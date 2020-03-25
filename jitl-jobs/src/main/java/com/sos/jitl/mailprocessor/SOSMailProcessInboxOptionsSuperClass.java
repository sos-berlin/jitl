
package com.sos.jitl.mailprocessor;

import java.util.HashMap;
import com.sos.JSHelper.Options.*;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "SOSMailProcessInboxOptionsSuperClass", description = "SOSMailProcessInboxOptionsSuperClass")
public class SOSMailProcessInboxOptionsSuperClass extends JSOptionsClass {

	private static final long serialVersionUID = 1L;
	private static final String CLASSNAME = "SOSMailProcessInboxOptionsSuperClass";

	public SOSMailProcessInboxOptionsSuperClass() {
		objParentClass = this.getClass();
	}

	public SOSMailProcessInboxOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	}

	public SOSMailProcessInboxOptionsSuperClass(HashMap<String, String> jsSettings) throws Exception {
		this();
		this.setAllOptions(jsSettings);
	}

	@JSOptionDefinition(name = "after_process_email", description = "", key = "after_process_email", type = "SOSOptionString", mandatory = false)
	public SOSOptionString afterProcessEmail = new SOSOptionString(this, CLASSNAME + ".after_process_email", "",
			// InitValue, DefaultValue, isMandatory
			"markAsRead", "markAsRead", false);

	public SOSOptionString getAfterProcessEmail() {
		return afterProcessEmail;
	}

	public void setAfterProcessEmail(SOSOptionString afterProcessEmail) {
		this.afterProcessEmail = afterProcessEmail;
	}

	@JSOptionDefinition(name = "after_process_email_directory_name", description = "", key = "after_process_email_directory_name", type = "SOSOptionString", mandatory = false)
	public SOSOptionString afterProcessEmailDirectoryName = new SOSOptionString(this, CLASSNAME + ".after_process_email_directory_name", "",
			// InitValue, DefaultValue, isMandatory
			"", "", false);
	

	public SOSOptionString getAfterProcessEmailDirectoryName() {
		return afterProcessEmailDirectoryName;
	}

	public void setAfterProcessEmailDirectoryName(SOSOptionString afterProcessEmailDirectoryName) {
		this.afterProcessEmailDirectoryName = afterProcessEmailDirectoryName;
	}
	@JSOptionDefinition(name = "attachment_directory_name", description = "", key = "attachment_directory_name", type = "SOSOptionString", mandatory = false)
	public SOSOptionString attachmentDirectoryName = new SOSOptionString(this,
			CLASSNAME + ".attachment_directory_name", "",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionString getAttachmentDirectoryName() {
		return attachmentDirectoryName;
	}

	public void setAttachmentDirectoryName(SOSOptionString attachmentDirectoryName) {
		this.attachmentDirectoryName = attachmentDirectoryName;
	}

	@JSOptionDefinition(name = "copy_mail_to_file", description = "", key = "copyMail2File", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean copyMail2File = new SOSOptionBoolean(this, CLASSNAME + ".copy_mail_to_file", "",
			// InitValue, DefaultValue, isMandatory
			"false", "false", false);

	public SOSOptionBoolean getCopyMail2File() {
		return copyMail2File;
	}

	public void setCopyMail2File(SOSOptionBoolean copyMail2File) {
		this.copyMail2File = copyMail2File;
	}


	@JSOptionDefinition(name = "save_body_as_attachments", description = "", key = "save_body_as_attachments", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean saveBodyAsAttachments = new SOSOptionBoolean(this, CLASSNAME + ".save_body_as_attachments", "",
			// InitValue, DefaultValue, isMandatory
			"false", "false", false);

	public SOSOptionBoolean getSaveBodyAsAttachments() {
		return saveBodyAsAttachments;
	}

	public void setSaveBodyAsAttachments(SOSOptionBoolean saveBodyAsAttachments) {
		this.saveBodyAsAttachments = saveBodyAsAttachments;
	}

	
	@JSOptionDefinition(name = "create_order", description = "", key = "createOrder", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean createOrder = new SOSOptionBoolean(this, CLASSNAME + ".create_order", "",
			// InitValue, DefaultValue, isMandatory
			"false", "false", false);

	public SOSOptionBoolean getCreateOrder() {
		return createOrder;
	}

	public void setCreateOrder(SOSOptionBoolean createOrder) {
		this.createOrder = createOrder;
	}

	@JSOptionDefinition(name = "execute_command", description = "", key = "execute_command", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean executeCommand = new SOSOptionBoolean(this, CLASSNAME + ".execute_command", "",
			// InitValue, DefaultValue, isMandatory
			"false", "false", false);

	public SOSOptionBoolean getExecuteCommand() {
		return executeCommand;
	}

	public void setExecuteCommand(SOSOptionBoolean executeCommand) {
		this.executeCommand = executeCommand;
	}

	@JSOptionDefinition(name = "delete_message", description = "", key = "delete_message", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean deleteMessage = new SOSOptionBoolean(this, CLASSNAME + ".delete_message", "",
			// InitValue, DefaultValue, isMandatory
			"false", "false", false);

	public SOSOptionBoolean getDeleteMessage() {
		return deleteMessage;
	}

	public void setDeleteMessage(SOSOptionBoolean deleteMessage) {
		this.deleteMessage = deleteMessage;
	}

	@JSOptionDefinition(name = "mail_action", description = "", key = "mail_action", type = "SOSOptionString", mandatory = false)
	public SOSOptionString mailAction = new SOSOptionString(this, CLASSNAME + ".mail_action", "",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionString getMailAction() {
		return mailAction;
	}

	public void setMailAction(SOSOptionString mailAction) {
		this.mailAction = mailAction;
	}

	@JSOptionDefinition(name = "mail_body_pattern", description = "", key = "mail_body_pattern", type = "SOSOptionRegExp", mandatory = false)
	public SOSOptionRegExp mailBodyPattern = new SOSOptionRegExp(this, CLASSNAME + ".mail_body_pattern", "",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionRegExp getMailBodyPattern() {
		return mailBodyPattern;
	}

	public void setMailBodyPattern(SOSOptionRegExp mailBodyPattern) {
		this.mailBodyPattern = mailBodyPattern;
	}

	@JSOptionDefinition(name = "mail_directory_name", description = "", key = "mail_directory_name", type = "SOSOptionFolderName", mandatory = false)
	public SOSOptionFolderName mailDirectoryName = new SOSOptionFolderName(this, CLASSNAME + ".mail_directory_name", "",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionFolderName mailDumpDir = (SOSOptionFolderName) mailDirectoryName.setAlias("mail_dump_dir");

	
	public SOSOptionFolderName getMailDirectoryName() {
		return mailDirectoryName;
	}

	public void setMailDirectoryName(SOSOptionFolderName mailDirectoryName) {
		this.mailDirectoryName = mailDirectoryName;
	}

	@JSOptionDefinition(name = "mail_host", description = "", key = "mail_host", type = "SOSOptionHostName", mandatory = true)
	public SOSOptionHostName mailHost = new SOSOptionHostName(this, CLASSNAME + ".mail_host", "",
			// InitValue, DefaultValue, isMandatory
			" ", " ", true);

	public SOSOptionHostName getMailHost() {
		return mailHost;
	}

	public void setMailHost(SOSOptionHostName mailHost) {
		this.mailHost = mailHost;
	}

	@JSOptionDefinition(name = "mail_jobchain", description = "", key = "mail_jobchain", type = "JSJobChainName", mandatory = false)
	public JSJobChainName mailJobchain = new JSJobChainName(this, CLASSNAME + ".mail_jobchain", "",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public JSJobChainName getMailJobchain() {
		return mailJobchain;
	}

	public void setMailJobchain(JSJobChainName mailJobchain) {
		this.mailJobchain = mailJobchain;
	}

	@JSOptionDefinition(name = "mail_message_folder", description = "", key = "mail_message_folder", type = "SOSOptionString", mandatory = false)
	public SOSOptionString mailMessageFolder = new SOSOptionString(this, CLASSNAME + ".mail_message_folder", "",
			// InitValue, DefaultValue, isMandatory
			"INBOX", "INBOX", false);

	public SOSOptionString getMailMessageFolder() {
		return mailMessageFolder;
	}

	public void setMailMessageFolder(SOSOptionString mailMessageFolder) {
		this.mailMessageFolder = mailMessageFolder;
	}

	@JSOptionDefinition(name = "mail_order_id", description = "", key = "mail_order_id", type = "JSOrderId", mandatory = false)
	public JSOrderId mailOrderId = new JSOrderId(this, CLASSNAME + ".mail_order_id", "",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public JSOrderId getMailOrderId() {
		return mailOrderId;
	}

	public void setMailOrderId(JSOrderId mailOrderId) {
		this.mailOrderId = mailOrderId;
	}

	@JSOptionDefinition(name = "mail_order_state", description = "", key = "mail_order_state", type = "SOSOptionJobChainNode", mandatory = false)
	public SOSOptionJobChainNode mailOrderState = new SOSOptionJobChainNode(this, CLASSNAME + ".mail_order_state", "",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionJobChainNode getMailOrderState() {
		return mailOrderState;
	}

	public void setMailOrderState(SOSOptionJobChainNode mailOrderState) {
		this.mailOrderState = mailOrderState;
	}

	@JSOptionDefinition(name = "mail_order_title", description = "", key = "mail_order_title", type = "SOSOptionString", mandatory = false)
	public SOSOptionString mailOrderTitle = new SOSOptionString(this, CLASSNAME + ".mail_order_title", "",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionString getMailOrderTitle() {
		return mailOrderTitle;
	}

	public void setMailOrderTitle(SOSOptionString mailOrderTitle) {
		this.mailOrderTitle = mailOrderTitle;
	}

	@JSOptionDefinition(name = "mail_password", description = "", key = "mail_password", type = "SOSOptionPassword", mandatory = false)
	public SOSOptionPassword mailPassword = new SOSOptionPassword(this, CLASSNAME + ".mail_password", "",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionPassword getMailPassword() {
		return mailPassword;
	}

	public void setMailPassword(SOSOptionPassword mailPassword) {
		this.mailPassword = mailPassword;
	}

	@JSOptionDefinition(name = "mail_port", description = "", key = "mail_port", type = "SOSOptionPortNumber", mandatory = false)
	public SOSOptionPortNumber mailPort = new SOSOptionPortNumber(this, CLASSNAME + ".mail_port", "",
			// InitValue, DefaultValue, isMandatory
			"110", "110", false);

	public SOSOptionPortNumber getMailPort() {
		return mailPort;
	}

	public void setMailPort(SOSOptionPortNumber mailPort) {
		this.mailPort = mailPort;
	}

	@JSOptionDefinition(name = "mail_scheduler_host", description = "", key = "mail_scheduler_host", type = "SOSOptionHostName", mandatory = false)
	public SOSOptionHostName mailSchedulerHost = new SOSOptionHostName(this, CLASSNAME + ".mail_scheduler_host", "",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionHostName getMailSchedulerHost() {
		return mailSchedulerHost;
	}

	public void setMailSchedulerHost(SOSOptionHostName mailSchedulerHost) {
		this.mailSchedulerHost = mailSchedulerHost;
	}

	@JSOptionDefinition(name = "mail_scheduler_port", description = "", key = "mail_scheduler_port", type = "SOSOptionPortNumber", mandatory = false)
	public SOSOptionPortNumber mailSchedulerPort = new SOSOptionPortNumber(this, CLASSNAME + ".mail_scheduler_port", "",
			// InitValue, DefaultValue, isMandatory
			"0", "0", false);

	public SOSOptionPortNumber getMailSchedulerPort() {
		return mailSchedulerPort;
	}

	public void setMailSchedulerPort(SOSOptionPortNumber mailSchedulerPort) {
		this.mailSchedulerPort = mailSchedulerPort;
	}

	@JSOptionDefinition(name = "mail_server_timeout", description = "", key = "mail_server_timeout", type = "SOSOptionInteger", mandatory = false)
	public SOSOptionInteger mailServerTimeout = new SOSOptionInteger(this, CLASSNAME + ".mail_server_timeout", "",
			// InitValue, DefaultValue, isMandatory
			"0", "0", false);

	public SOSOptionInteger getMailServerTimeout() {
		return mailServerTimeout;
	}

	public void setMailServerTimeout(SOSOptionInteger mailServerTimeout) {
		this.mailServerTimeout = mailServerTimeout;
	}

	@JSOptionDefinition(name = "mail_server_type", description = "", key = "mail_server_type", type = "SOSOptionString", mandatory = false)
	public SOSOptionString mailServerType = new SOSOptionString(this, CLASSNAME + ".mail_server_type", "",
			// InitValue, DefaultValue, isMandatory
			"POP3", "POP3", false);

	public SOSOptionString getMailServerType() {
		return mailServerType;
	}

	public void setMailServerType(SOSOptionString mailServerType) {
		this.mailServerType = mailServerType;
	}

    @JSOptionDefinition(name = "mail_subject_filter", description = "", key = "mail_subject_filter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mailSubjectFilter = new SOSOptionString(this, CLASSNAME + ".mail_subject_filter", "",
            // InitValue, DefaultValue, isMandatory
            " ", " ", false);

    public SOSOptionString getMail_subject_filter() {
        return mailSubjectFilter;
    }

    public void setMailSubjectFilter(SOSOptionString mailSubjectFilter) {
        this.mailSubjectFilter = mailSubjectFilter;
    }

    @JSOptionDefinition(name = "mail_from_filter", description = "", key = "mail_from_filter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mailFromFilter = new SOSOptionString(this, CLASSNAME + ".mail_from_filter", "",
            // InitValue, DefaultValue, isMandatory
            " ", " ", false);

    public SOSOptionString getMail_from_filter() {
        return mailFromFilter;
    }

    public void setMailFromFilter(SOSOptionString mailFromFilter) {
        this.mailFromFilter = mailFromFilter;
    }

	@JSOptionDefinition(name = "mail_subject_pattern", description = "", key = "mail_subject_pattern", type = "SOSOptionRegExp", mandatory = false)
	public SOSOptionRegExp mailSubjectPattern = new SOSOptionRegExp(this, CLASSNAME + ".mail_subject_pattern", "",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionRegExp getMailSubjectPattern() {
		return mailSubjectPattern;
	}

	public void setMailSubjectPattern(SOSOptionRegExp mailSubjectPattern) {
		this.mailSubjectPattern = mailSubjectPattern;
	}

	@JSOptionDefinition(name = "attachment_file_name_pattern", description = "", key = "attachment_file_name_pattern", type = "SOSOptionString", mandatory = false)
	public SOSOptionString attachmentFileNamePattern = new SOSOptionString(this, CLASSNAME + ".attachment_file_name_pattern", "",
			// InitValue, DefaultValue, isMandatory
			"", "", false);

	public SOSOptionString getAttachmentFileNamePattern() {
		return attachmentFileNamePattern;
	}

	public void setAttachmentFileNamePattern(SOSOptionString attachmentFileNamePattern) {
		this.attachmentFileNamePattern = attachmentFileNamePattern;
	}
	
	@JSOptionDefinition(name = "mail_use_seen", description = "", key = "mail_use_seen", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean mailUseSeen = new SOSOptionBoolean(this, CLASSNAME + ".mail_use_seen", "",
			// InitValue, DefaultValue, isMandatory
			"true", "true", false);

	public SOSOptionBoolean getMailUseSeen() {
		return mailUseSeen;
	}

	public void setMailUseSeen(SOSOptionBoolean mailUseSeen) {
		this.mailUseSeen = mailUseSeen;
	}

	@JSOptionDefinition(name = "mail_ssl", description = "", key = "mail_ssl", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean mailSsl = new SOSOptionBoolean(this, CLASSNAME + ".mail_ssl", "",
			// InitValue, DefaultValue, isMandatory
			"false", "false", false);

	public SOSOptionBoolean getMailSSln() {
		return mailSsl;
	}

	public void setMailSsl(SOSOptionBoolean mailSsl) {
		this.mailSsl = mailSsl;
	}
	@JSOptionDefinition(name = "mail_user", description = "", key = "mail_user", type = "SOSOptionUserName", mandatory = true)
	public SOSOptionUserName mailUser = new SOSOptionUserName(this, CLASSNAME + ".mail_user", "",
			// InitValue, DefaultValue, isMandatory
			" ", " ", true);

	public SOSOptionUserName getMailUser() {
		return mailUser;
	}

	public void setMailUser(SOSOptionUserName mailUser) {
		this.mailUser = mailUser;
	}

	@JSOptionDefinition(name = "copy_attachments_to_file", description = "", key = "copy_attachments_to_file", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean copyAttachmentsToFile = new SOSOptionBoolean(this, CLASSNAME + ".copy_attachments_to_file", "",
			// InitValue, DefaultValue, isMandatory
			"false", "false", false);

	public SOSOptionBoolean getCopyAttachmentsToFile() {
		return copyAttachmentsToFile;
	}

	public void setCopyAttachmentToFile(SOSOptionBoolean copyAttachmentsToFile) {
		this.copyAttachmentsToFile = copyAttachmentsToFile;
	}

	@JSOptionDefinition(name = "min_age", description = "Objects, which are younger than min_age are not processed", key = "min_age", type = "SOSOptionTime", mandatory = false)
	public SOSOptionTimeHorizon minAge = new SOSOptionTimeHorizon(this, CLASSNAME + ".min_age",
			"Objects, which are younger than min_age are not processed", "", "", false);

	public String getMinAge() {
		return minAge.getValue();
	}

	public SOSMailProcessInboxOptionsSuperClass setMinFileAge(final String pstrValue) {
		minAge.setValue(pstrValue);
		return this;
	}

	@JSOptionDefinition(name = "max_mails_to_process", description = "", key = "max_mails_to_process", type = "SOSOptionInteger", mandatory = false)
	public SOSOptionInteger maxMailsToProcess = new SOSOptionInteger(this, CLASSNAME + ".max_mails_to_process", "", "0",
			"1000", false);

	public SOSOptionInteger getMaxMailsToProcess() {
		return maxMailsToProcess;
	}

	public void setMaxMailsToProcess(final SOSOptionInteger pMaxMailsToProcess) {
		maxMailsToProcess = pMaxMailsToProcess;
	}

	public void setAllOptions(HashMap<String, String> settings) {
        super.setAllOptions(settings);
    }

	@Override
	public void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
		try {
			super.checkMandatory();
		} catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	}

	@Override
	public void commandLineArgs(String[] pstrArgs) {
		super.commandLineArgs(pstrArgs);
		this.setAllOptions(super.getSettings());
	}

}
