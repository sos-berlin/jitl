
package com.sos.jitl.mailprocessor;

import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSMailProcessInboxJUnitTest extends JSToolBox {

	protected SOSMailProcessInboxOptions sosMailProcessInboxOptions = null;
	private static final String CLASSNAME = "SOSMailProcessInboxJUnitTest";
	private static final Logger LOGGER = LoggerFactory.getLogger(SOSMailProcessInboxOptions.class);
	private SOSMailProcessInbox sosMailProcessInbox = null;

	public SOSMailProcessInboxJUnitTest() {
		//
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// TODO: Implement Method setUpBeforeClass here
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// TODO: Implement Method tearDownAfterClass here
	}

	@Before
	public void setUp() throws Exception {
		sosMailProcessInbox = new SOSMailProcessInbox();
		sosMailProcessInboxOptions = sosMailProcessInbox.getOptions();
		sosMailProcessInbox.registerMessageListener(this);
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9; 
	}

	@After
	public void tearDown() throws Exception {
		// TODO: Implement Method tearDown here
	}

	@Test
	public void testFile2Email() throws Exception {
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
	
	@Test
	public void testExecute() throws Exception {
		int httpPort = 4444;
		sosMailProcessInboxOptions.mailSchedulerHost.setValue("localhost");
		sosMailProcessInboxOptions.mailSchedulerPort.value(httpPort);
		sosMailProcessInboxOptions.mailHost.setValue("mail.sos-berlin.com");
		sosMailProcessInboxOptions.mailUser.setValue("uwe.risse@sos-berlin.com");
		sosMailProcessInboxOptions.mailPassword.setValue("********");
		sosMailProcessInboxOptions.mailPort.value(993);
		sosMailProcessInboxOptions.mailSubjectFilter.setValue("Proof of Concept Enquiry Job Scheduler for CONO");
		// sosMailProcessInboxOptions.mailSubjectPattern.setValue("^.*TEST$");
		sosMailProcessInboxOptions.mailServerType.setValue("IMAP");
		sosMailProcessInboxOptions.attachementDirectoryName.setValue("c:/temp/attachment");
		sosMailProcessInboxOptions.copyMail2File.value(true);
		sosMailProcessInboxOptions.copyAttachmentsToFile.value(true);
		sosMailProcessInboxOptions.maxMailsToProcess.value(20000);
		sosMailProcessInboxOptions.mailMessageFolder.setValue("c:/temp/mails");
		sosMailProcessInboxOptions.mailSsl.value(true);
		sosMailProcessInboxOptions.mailAction.setValue("dump,order,command,processAttachments");
		sosMailProcessInboxOptions.mailServerTimeout.value(30000);
		sosMailProcessInboxOptions.attachmentFileNamePattern.setValue("${subject}_${filename}");
		sosMailProcessInboxOptions.afterProcessEmail.setValue("copy");
		sosMailProcessInboxOptions.afterProcessEmailDirectoryName.setValue("email:INBOX/test");
		

		sosMailProcessInboxOptions.checkMandatory();

		sosMailProcessInbox.execute();
		for (PostproccesingEntry postproccesingEntry : sosMailProcessInbox.getListOfPostprocessing()) {
			if (postproccesingEntry.isAddOrder()) {
				LOGGER.info("Will add order");
			}
			if (postproccesingEntry.isExecuteCommand()) {
				LOGGER.info("Will execute command");
			}
		}
	}

}
