

package com.sos.jitl.splitter;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

import org.apache.log4j.Logger;
import org.junit.*;

/**
 * \class 		JobChainSplitterJUnitTest - JUnit-Test for "Start a parallel processing in a jobchain"
 *
 * \brief MainClass to launch JobChainSplitter as an executable command-line program
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-121986169113382203.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler_4446\config\JOETemplates\java\xsl\JSJobDoc2JSJUnitClass.xsl from http://www.sos-berlin.com at 20130315155436 
 * \endverbatim
 */
public class JobChainSplitterJUnitTest extends JSToolBox {
	@SuppressWarnings("unused")	 //$NON-NLS-1$
	private final static String					conClassName						= "JobChainSplitterJUnitTest"; //$NON-NLS-1$
	@SuppressWarnings("unused")	 //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobChainSplitterJUnitTest.class);

	protected JobChainSplitterOptions	objOptions			= null;
	private JobChainSplitter objE = null;
	
	
	public JobChainSplitterJUnitTest() {
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
		objE = new JobChainSplitter();
		objE.registerMessageListener(this);
		objOptions = objE.Options();
		objOptions.registerMessageListener(this);
		
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testExecute() throws Exception {
		
		
		objE.Execute();
		
//		assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
//		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$


	}
}  // class JobChainSplitterJUnitTest