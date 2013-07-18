package com.sos.jitl.mail.smtp;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.Logging.Log4JHelper;

/**
 * \class 		JSSmtpMailClientJUnitTest - JUnit-Test for "Submit and Delete Events"
 *
 * \brief MainClass to launch JSSmtpMailClient as an executable command-line program
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSJUnitClass.xsl from http://www.sos-berlin.com at 20130109134235
 * \endverbatim
 */
public class JSSmtpMailClientJUnitTest extends JSToolBox {
	@SuppressWarnings("unused")
	private final static String		conClassName	= "JSSmtpMailClientJUnitTest";						//$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger			logger			= Logger.getLogger(JSSmtpMailClientJUnitTest.class);
	@SuppressWarnings("unused")
	private static Log4JHelper		objLogger		= null;

	protected JSSmtpMailOptions	objOptions		= null;
	private JSSmtpMailClient			objE			= null;

	public JSSmtpMailClientJUnitTest() {
		//
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		objLogger = new Log4JHelper("./log4j.properties"); //$NON-NLS-1$
		objE = new JSSmtpMailClient();
		objOptions = objE.Options();

		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExecute() throws Exception {

		objOptions.host.Value("smtp.sos");
		objOptions.port.value(25);

		objOptions.from.Value("JUnit-Test@sos-berlin.com");

		objOptions.body.Value( "bodobodododo\nfjfjfjfjfjjf\nfkfkfkfkfkfkf\nfjfjfjfjfjf\n %{host}  ");
		objOptions.subject.Value( "mail from JSSmtpMailClientJUnitTest %{host}\n  date = %{date}\n");
		objOptions.to.Value("kb@sos-berlin.com");
		objOptions.cc.Value("kb@sos-berlin.com;info@sos-berlin.com");
		objOptions.bcc.Value("kb@sos-berlin.com;support@sos-berlin.com");

		objE.Execute();
	}


	

} // class JSSmtpMailClientJUnitTest