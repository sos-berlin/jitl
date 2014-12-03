package com.sos.jitl.housekeeping.rotatelog;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.jitl.housekeeping.rotatelog.JobSchedulerRotateLog;
import com.sos.jitl.housekeeping.rotatelog.JobSchedulerRotateLogOptions;

/**
 * \class 		JobSchedulerRotateLogJUnitTest - JUnit-Test for "Rotate compress and delete log files"
 *
 * \brief MainClass to launch JobSchedulerRotateLog as an executable command-line program
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-1724231827372138737html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by com/sos/resources/xsl/JSJobDoc2JSJUnitClass.xsl from http://www.sos-berlin.com at 20140906131052 
 * \endverbatim
 */
public class JobSchedulerRotateLogJUnitTest extends JSToolBox {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private final static String				conClassName	= "JobSchedulerRotateLogJUnitTest";						//$NON-NLS-1$
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static Logger					logger			= Logger.getLogger(JobSchedulerRotateLogJUnitTest.class);

	protected JobSchedulerRotateLogOptions	objOptions		= null;
	private JobSchedulerRotateLog			objE			= null;

	public JobSchedulerRotateLogJUnitTest() {
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
		objE = new JobSchedulerRotateLog();
		objE.registerMessageListener(this);
		objOptions = objE.Options();

		BasicConfigurator.configure();
		logger.setLevel(Level.DEBUG);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testExecute() throws Exception {

		logger.setLevel(Level.ALL);
		objE.Execute();

		//		assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
		//		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$

	}
} // class JobSchedulerRotateLogJUnitTest