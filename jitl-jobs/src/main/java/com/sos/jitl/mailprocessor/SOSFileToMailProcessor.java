package com.sos.jitl.mailprocessor;

import java.io.File;
import javax.mail.internet.MimeMessage;
import sos.net.SOSMail;
import sos.net.SOSMimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSFileToMailProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(SOSFileToMailProcessor.class);

	public static MimeMessage getMimeMessage(String workFileName) throws Exception {
		File workFile = new File(workFileName);
		LOGGER.debug("processing mail file: " + workFileName);
		SOSMail sosMail = new SOSMail("");
		sosMail.loadFile(workFile);
		MimeMessage message = sosMail.getMessage();
		return message;
	}

	public static SOSMimeMessage getMessage(String workFileName) throws Exception {
		File workFile = new File(workFileName);
		LOGGER.debug("processing mail file: " + workFileName);
		SOSMail sosMail = new SOSMail("");
		sosMail.loadFile(workFile);
		MimeMessage message = sosMail.getMessage();
		SOSMimeMessage sosMimeMessage = new SOSMimeMessage(message);
		return sosMimeMessage;
	}


}
