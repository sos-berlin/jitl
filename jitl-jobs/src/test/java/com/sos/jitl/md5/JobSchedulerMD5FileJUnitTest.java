

package com.sos.jitl.md5;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;


public class JobSchedulerMD5FileJUnitTest extends JSToolBox {
	@SuppressWarnings("unused")	 //$NON-NLS-1$
	private final static String					conClassName						= "JobSchedulerMD5FileJUnitTest"; //$NON-NLS-1$
	@SuppressWarnings("unused")	 //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerMD5FileJUnitTest.class);

	protected JobSchedulerMD5FileOptions	objOptions			= null;
	private JobSchedulerMD5File objE = null;
	
	
	public JobSchedulerMD5FileJUnitTest() {
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
		objE = new JobSchedulerMD5File();
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
	public void testExecute() throws Exception {
		
		
		objE.Execute();
		
//		assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
//		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$


	}
}  // class JobSchedulerMD5FileJUnitTest