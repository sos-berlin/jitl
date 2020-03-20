package com.sos.jitl.mailprocessor;

import com.sos.jitl.mailprocessor.SOSMailProcessInbox;
import com.sos.jitl.mailprocessor.SOSMailProcessInboxOptions;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.messages.JSMessages;
import sos.net.SOSMailReceiver;
import com.sos.JSHelper.Basics.JSJobUtilities;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSMailProcessInbox extends JSJobUtilitiesClass<SOSMailProcessInboxOptions> {

	protected SOSMailProcessInboxOptions sosMailProcessInboxOptions = null;
	private static final String CLASSNAME = "SOSMailProcessInbox";
	private JSJobUtilities jsJobUtilities = this;
	private static final Logger LOGGER = LoggerFactory.getLogger(SOSMailProcessInboxOptions.class);
	private List<PostproccesingEntry> listOfPostprocessing;

	public SOSMailProcessInbox() {
		super(new SOSMailProcessInboxOptions());
	}

	public SOSMailProcessInboxOptions getOptions() {
		if (sosMailProcessInboxOptions == null) {
			sosMailProcessInboxOptions = new SOSMailProcessInboxOptions();
		}
		return sosMailProcessInboxOptions;
	}

	public SOSMailProcessInboxOptions getOptions(final SOSMailProcessInboxOptions psosMailProcessInboxOptionsptions) {
		sosMailProcessInboxOptions = psosMailProcessInboxOptionsptions;
		return sosMailProcessInboxOptions;
	}

	public SOSMailProcessInbox execute() throws Exception {
		final String METHODNAME = CLASSNAME + "::execute";
		LOGGER.debug(String.format(JSMessages.JSJ_I_110.get(), METHODNAME));
		SOSMailReceiver mailReader = null;
		try {

			String mailHost = sosMailProcessInboxOptions.mailHost.getValue();
			int mailPort = sosMailProcessInboxOptions.mailPort.value();
			String serverType = sosMailProcessInboxOptions.mailServerType.getValue();
			mailReader = new SOSMailReceiver(sosMailProcessInboxOptions.mailHost.getValue(),
					sosMailProcessInboxOptions.mailPort.getValue(), sosMailProcessInboxOptions.mailUser.getValue(),
					sosMailProcessInboxOptions.mailPassword.getValue(),sosMailProcessInboxOptions.mailSsl.value(),sosMailProcessInboxOptions.mailServerType.getValue());
			if (sosMailProcessInboxOptions.mailServerTimeout.value() > 0) {
				mailReader.setTimeout(sosMailProcessInboxOptions.mailServerTimeout.value());
			}

			LOGGER.debug(String.format("Connecting to Mailserver %1$s:%2$d (%3$s)...", mailHost, mailPort, serverType));
			SOSMailProcessor sosMailProcessor = new SOSMailProcessor(sosMailProcessInboxOptions);
			
			for (String mailFolderName : sosMailProcessInboxOptions.mailMessageFolder.getValue().split("[,|;]")) {
				mailReader.connect(sosMailProcessInboxOptions.mailServerType.getValue());
				sosMailProcessor.performMessagesInFolder(mailReader,mailFolderName.trim());
			}
			listOfPostprocessing  = sosMailProcessor.getListOfPostprocessing();

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			LOGGER.error(String.format(JSMessages.JSJ_F_107.get(), METHODNAME), e);
			throw e;
		} finally {
			LOGGER.debug(String.format(JSMessages.JSJ_I_111.get(), METHODNAME));
			if (mailReader != null) {
				mailReader.closeFolder(true);
				mailReader.disconnect();
				mailReader = null;
			}
		}
		return this;
	}

	public List<PostproccesingEntry> getListOfPostprocessing() {
		return listOfPostprocessing;
	}

	@Override
	public String replaceSchedulerVars(String pstrString2Modify) {
		LOGGER.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
		return pstrString2Modify;
	}

	@Override
	public void setJSParam(String pstrKey, String pstrValue) {
	}

	@Override
	public void setJSParam(String pstrKey, StringBuilder pstrValue) {
	}

	public void setJSJobUtilites(JSJobUtilities pobjJSJobUtilities) {
		if (pobjJSJobUtilities == null) {
			jsJobUtilities = this;
		} else {
			jsJobUtilities = pobjJSJobUtilities;
		}
		LOGGER.debug("objJSJobUtilities = " + jsJobUtilities.getClass().getName());
	}

}
