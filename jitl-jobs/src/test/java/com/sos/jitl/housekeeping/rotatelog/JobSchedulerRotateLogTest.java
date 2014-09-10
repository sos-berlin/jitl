package com.sos.jitl.housekeeping.rotatelog;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Options.SOSOptionElement;

public class JobSchedulerRotateLogTest {
	@SuppressWarnings("unused")
	private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused")
	private final Logger logger = Logger.getLogger(this.getClass());

	
	JobSchedulerRotateLog objM;
	JobSchedulerRotateLogOptions objO;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("SCHEDULER_DATA", "C:\\ProgramData\\sos-berlin.com\\jobscheduler\\kb-xps-laptop_4445");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		objM = new JobSchedulerRotateLog();
		objO = objM.Options();

		BasicConfigurator.configure();
		logger.setLevel(Level.DEBUG);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOptions() {
//		fail("Not yet implemented");
	}

	@Test
	public void testExecute() {
		objO.JobSchedulerID.Value("KB-XPS-Laptop_4445");
		objO.delete_file_age.Value("8:00:00:00");
		objO.compress_file_age.Value("2d");
		objM.Execute();
	}

	@Test
	public void testProperty () {;
		System.setProperty("SCHEDULER_DATA", "C:\\ProgramData\\sos-berlin.com\\jobscheduler\\kb-xps-laptop_4445");
		assertEquals ("Property", "C:\\ProgramData\\sos-berlin.com\\jobscheduler\\kb-xps-laptop_4445", System.getProperty("SCHEDULER_DATA"));
		
		SOSOptionElement objE = new SOSOptionElement("${SCHEDULER_DATA}");
		assertEquals ("Property", "C:\\ProgramData\\sos-berlin.com\\jobscheduler\\kb-xps-laptop_4445", objE.Value());
		
	}
	@Test
	public void testInit() {
//		fail("Not yet implemented");
	}

}
