

package com.sos.jitl.eventing.checkevents;

import java.io.File;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.resources.SOSResourceFactory;
import com.sos.resources.SOSTestResource;

import org.apache.log4j.Logger;
//import org.junit.*;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.After;

/**
 * \class 		JobSchedulerCheckEventsJUnitTest - JUnit-Test for "Check if events exist"
 *
 * \brief MainClass to launch JobSchedulerCheckEvents as an executable command-line program
 *

 *
 *
 * \verbatim ;
 * \endverbatim
 */
public class JobSchedulerCheckEventsJUnitTest extends JSToolBox {
	@SuppressWarnings("unused")	 //$NON-NLS-1$
	private final static String					conClassName						= "JobSchedulerCheckEventsJUnitTest"; //$NON-NLS-1$
	@SuppressWarnings("unused")	 //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerCheckEventsJUnitTest.class);

	protected JobSchedulerCheckEventsOptions	objOptions			= null;
	private JobSchedulerCheckEvents objE = null;
	
	
	public JobSchedulerCheckEventsJUnitTest() {
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
		objE = new JobSchedulerCheckEvents();
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
		
    File configurationFile = SOSResourceFactory.asFile(SOSTestResource.HIBERNATE_CONFIGURATION_ORACLE);;
		objE.Options().setconfiguration_file(new SOSOptionString(configurationFile.getAbsolutePath()));
		objE.Execute();
		
//		assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
//		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$


	}
}  // class JobSchedulerCheckEventsJUnitTest