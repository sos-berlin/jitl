package com.sos.jitl.eventing;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.jitl.eventing.checkevents.JobSchedulerCheckEvents;
import com.sos.jitl.eventing.checkevents.JobSchedulerCheckEventsOptions;

public class JobSchedulerCheckEventsOptionsJUnitTest extends JSToolBox {

	private final String conClassName = "JobSchedulerCheckEventsOptionsJUnitTest"; //$NON-NLS-1$
	@SuppressWarnings("unused")
	private JobSchedulerCheckEvents objE = null;

	protected JobSchedulerCheckEventsOptions objOptions = null;

	public JobSchedulerCheckEventsOptionsJUnitTest() {
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
		objOptions = objE.getOptions();
		objOptions.registerMessageListener(this);

		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testconfiguration_file() { // SOSOptionString
		objOptions.configuration_file.setValue("++----++");
		assertEquals("", objOptions.configuration_file.getValue(), "++----++");

	}

	@Test
	public void testevent_class() { // SOSOptionString
		objOptions.event_class.setValue("++----++");
		assertEquals("", objOptions.event_class.getValue(), "++----++");

	}

	@Test
	public void testevent_condition() { // SOSOptionString
		objOptions.event_condition.setValue("++----++");
		assertEquals("", objOptions.event_condition.getValue(), "++----++");

	}

	@Test
	public void testevent_exit_code() { // SOSOptionString
		objOptions.event_exit_code.setValue("++----++");
		assertEquals("", objOptions.event_exit_code.getValue(), "++----++");

	}

	@Test
	public void testevent_id() { // SOSOptionString
		objOptions.event_id.setValue("++----++");
		assertEquals("", objOptions.event_id.getValue(), "++----++");

	}

	@Test
	public void testevent_job() { // SOSOptionString
		objOptions.event_job.setValue("++----++");
		assertEquals("", objOptions.event_job.getValue(), "++----++");

	}

	@Test
	public void testevent_job_chain() { // SOSOptionString
		objOptions.event_job_chain.setValue("++----++");
		assertEquals("", objOptions.event_job_chain.getValue(), "++----++");

	}

	@Test
	public void testevent_order_id() { // SOSOptionString
		objOptions.event_order_id.setValue("++----++");
		assertEquals("", objOptions.event_order_id.getValue(), "++----++");

	}

	@Test
	public void testevent_scheduler_id() { // SOSOptionString
		objOptions.event_scheduler_id.setValue("++----++");
		assertEquals("", objOptions.event_scheduler_id.getValue(), "++----++");

	}

	@Test
	public void testhandle_existing_as() { // SOSOptionString
		objOptions.handle_existing_as.setValue("++----++");
		assertEquals("", objOptions.handle_existing_as.getValue(), "++----++");

	}

	@Test
	public void testhandle_not_existing_as() { // SOSOptionString
		objOptions.handle_not_existing_as.setValue("++----++");
		assertEquals("", objOptions.handle_not_existing_as.getValue(), "++----++");

	}

	@Test
	public void testremote_scheduler_host() { // SOSOptionString
		objOptions.remote_scheduler_host.setValue("++----++");
		assertEquals("", objOptions.remote_scheduler_host.getValue(), "++----++");

	}

	@Test
	public void testremote_scheduler_port() { // SOSOptionString
		objOptions.remote_scheduler_port.setValue("++----++");
		assertEquals("", objOptions.remote_scheduler_port.getValue(), "++----++");

	}

} // public class JobSchedulerCheckEventsOptionsJUnitTest