package com.sos.jitl.latecomers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

public class JobSchedulerStartLatecomersOptionsJUnitTest extends JSToolBox {

	protected JobSchedulerStartLatecomersOptions objOptions = null;
	private static final int DEBUG9 = 9;
	private JobSchedulerStartLatecomers objE = null;

	@Before
	public void setUp() throws Exception {
		objE = new JobSchedulerStartLatecomers();
		objE.registerMessageListener(this);
		objOptions = objE.getOptions();
		objOptions.registerMessageListener(this);
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = DEBUG9;
	}

	@Test
	public void testday_offset() {
		objOptions.dayOffset.setValue("++0d++");
		assertEquals("Specify the number of days to look in the past. Example: 10d looks te",
				objOptions.dayOffset.getValue(), "++0d++");
	}

	@Test
	public void testignore_folder_list() {
		objOptions.ignoreFolderList.setValue("++----++");
		assertEquals("A comma seperated list of folders. These folders will be ignored by t",
				objOptions.ignoreFolderList.getValue(), "++----++");
	}

	@Test
	public void testignore_job_list() {
		objOptions.ignoreJobList.setValue("++----++");
		assertEquals("A comma seperated list of jobs. Then name can contain wildcards % whi",
				objOptions.ignoreJobList.getValue(), "++----++");
	}

	@Test
	public void testignore_jobchain_list() {
		objOptions.ignoreOrderList.setValue("++----++");
		assertEquals("A comma seperated list of job chains. Then name can contain wildcards",
				objOptions.ignoreOrderList.getValue(), "++----++");
	}

	@Test
	public void testjobs() {
		objOptions.jobs.setValue("++----++");
		assertEquals("A comma seperated list of jobs. If this parameter is set, only those", objOptions.jobs.getValue(),
				"++----++");
	}

	@Test
	public void testonly_report() {
		objOptions.onlyReport.setValue("++false++");
		assertEquals("If true no job (orders) will be startet but just listed in the log.",
				objOptions.onlyReport.getValue(), "++false++");
	}

	@Test
	public void testorders() {
		objOptions.orders.setValue("++----++");
		assertEquals("A comma seperated list of orders. If this parameter is set, only thos",
				objOptions.orders.getValue(), "++----++");
	}

}
