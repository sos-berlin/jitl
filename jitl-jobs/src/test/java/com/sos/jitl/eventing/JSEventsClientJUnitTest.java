package com.sos.jitl.eventing;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * \class 		JSEventsClientJUnitTest - JUnit-Test for "Submit and Delete Events"
 *
 * \brief MainClass to launch JSEventsClient as an executable command-line program
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSJUnitClass.xsl from http://www.sos-berlin.com at 20130109134235
 * \endverbatim
 */
public class JSEventsClientJUnitTest extends JSToolBox {
	@SuppressWarnings("unused")
	private final static String		conClassName	= "JSEventsClientJUnitTest";						//$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger			logger			= Logger.getLogger(JSEventsClientJUnitTest.class);

	protected JSEventsClientOptions	objOptions		= null;
	private JSEventsClient			objE			= null;

	public JSEventsClientJUnitTest() {
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
		objE = new JSEventsClient();
		objOptions = objE.getOptions();

		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExecute() throws Exception {

		objOptions.scheduler_event_class.Value("kb-TestClass");
		objOptions.scheduler_event_id.Value("kb-Event1");

		objE.Execute();

		assertEquals("auth_file", "test", "test"); //$NON-NLS-1$

	}

	@Test
	public void testExecuteLocalHost() throws Exception {

		objOptions.scheduler_event_class.Value("kb-TestClass");
		objOptions.scheduler_event_id.Value("kb-Event1");

		objOptions.scheduler_event_handler_host.Value("8of9.sos");
		objOptions.scheduler_event_handler_port.value(4210);

		objE.Execute();

		assertEquals("auth_file", "test", "test"); //$NON-NLS-1$
		//		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$

	}
	@Test
	public void testExecuteMultipleEvents() throws Exception {

		objOptions.scheduler_event_class.Value("kb-TestClass");
		objOptions.scheduler_event_id.Value("kb-Event1;kb-Event2;kb-Event3");
		objOptions.del_events.Value("kb-Event1;kb-Event2;kb-Event3");

		objOptions.scheduler_event_handler_host.Value("8of9.sos");
		objOptions.scheduler_event_handler_port.value(4210);

		objE.Execute();

		assertEquals("auth_file", "test", "test"); //$NON-NLS-1$
		//		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$

	}


} // class JSEventsClientJUnitTest