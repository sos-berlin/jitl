package com.sos.jitl.mailprocessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.MimeMessage;

import sos.net.SOSMail;
import sos.net.SOSMimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSFileToMailProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(SOSFileToMailProcessor.class);

	public static MimeMessage getMimeMessage(String workFileName) throws Exception {
		File workFile = new File(workFileName);
		LOGGER.info("processing mail file: " + workFileName);
		SOSMail sosMail = new SOSMail("");
		sosMail.loadFile(workFile);
		MimeMessage message = sosMail.getMessage();
		return message;
	}

	public static SOSMimeMessage getMessage(String workFileName) throws Exception {
		File workFile = new File(workFileName);
		LOGGER.info("processing mail file: " + workFileName);
		SOSMail sosMail = new SOSMail("");
		sosMail.loadFile(workFile);
		MimeMessage message = sosMail.getMessage();
		SOSMimeMessage sosMimeMessage = new SOSMimeMessage(message);
		return sosMimeMessage;
	}

	public static void main(String[] args) throws Exception {
		sos.net.SOSMimeMessage message = SOSFileToMailProcessor
				.getMessage("C:/temp/mails/10f7-5b35e680-71-456fb280@195509842");
		LOGGER.info(message.getSubject());
		String body = message.getPlainTextBody();
		LOGGER.info(body);

		LOGGER.info(message.getFirstToRecipient());
		LOGGER.info(message.getToRecipient(0));
		LOGGER.info(message.getRecipient("TO", 0));
		LOGGER.info(message.getFrom());
	}

}
