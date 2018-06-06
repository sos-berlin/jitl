package com.sos.jitl.eventing;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;

public class JSEventsClientOptionsJUnitTest extends JSToolBox {

	@SuppressWarnings("unused")
	private final String conClassName = "JSEventsClientOptionsJUnitTest"; //$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(JSEventsClientOptionsJUnitTest.class);
	private JSEventsClient objE = null;

	protected JSEventsClientOptions objOptions = null;

	@Before
	public void setUp() throws Exception {
		objE = new JSEventsClient();
		objOptions = objE.getOptions();
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@Test
	public void testdel_events() { // SOSOptionString
		objOptions.del_events.setValue("++----++");
		assertEquals("", objOptions.del_events.getValue(), "++----++");
	}

	@Test
	public void testscheduler_event_action() { // SOSOptionString
		objOptions.scheduler_event_action.setValue("++add++");
		assertEquals("", objOptions.scheduler_event_action.getValue(), "++add++");

	}

	@Test
	public void testscheduler_event_class() { // SOSOptionString
		objOptions.scheduler_event_class.setValue("++----++");
		assertEquals("", objOptions.scheduler_event_class.getValue(), "++----++");

	}

	@Test
	public void testscheduler_event_exit_code() { // SOSOptionInteger
		objOptions.scheduler_event_exit_code.setValue("12345");
		assertEquals("", objOptions.scheduler_event_exit_code.getValue(), "12345");
		assertEquals("", objOptions.scheduler_event_exit_code.value(), 12345);
		objOptions.scheduler_event_exit_code.value(12345);
		assertEquals("", objOptions.scheduler_event_exit_code.getValue(), "12345");
		assertEquals("", objOptions.scheduler_event_exit_code.value(), 12345);

	}

	@Test
	public void testscheduler_event_expiration_cycle() { // SOSOptionTime
		objOptions.scheduler_event_expiration_cycle.setValue("30");
		assertEquals("", objOptions.scheduler_event_expiration_cycle.getValue(), "30");
		assertEquals("", objOptions.scheduler_event_expiration_cycle.getTimeAsSeconds(), 30);
		objOptions.scheduler_event_expiration_cycle.setValue("1:30");
		assertEquals("", objOptions.scheduler_event_expiration_cycle.getValue(), "1:30");
		assertEquals("", objOptions.scheduler_event_expiration_cycle.getTimeAsSeconds(), 90);
		objOptions.scheduler_event_expiration_cycle.setValue("1:10:30");
		assertEquals("", objOptions.scheduler_event_expiration_cycle.getValue(), "1:10:30");
		assertEquals("", objOptions.scheduler_event_expiration_cycle.getTimeAsSeconds(), 30 + 10 * 60 + 60 * 60);

	}

	@Test
	public void testscheduler_event_expiration_period() { // SOSOptionTimeRange
		objOptions.scheduler_event_expiration_period.setValue("++----++");
		assertEquals("", objOptions.scheduler_event_expiration_period.getValue(), "++----++");

	}

	@Test
	public void testscheduler_event_expires() { // SOSOptionTime
		objOptions.scheduler_event_expires.setValue("30");
		assertEquals("", objOptions.scheduler_event_expires.getValue(), "30");
		assertEquals("", objOptions.scheduler_event_expires.getTimeAsSeconds(), 30);
		objOptions.scheduler_event_expires.setValue("1:30");
		assertEquals("", objOptions.scheduler_event_expires.getValue(), "1:30");
		assertEquals("", objOptions.scheduler_event_expires.getTimeAsSeconds(), 90);
		objOptions.scheduler_event_expires.setValue("1:10:30");
		assertEquals("", objOptions.scheduler_event_expires.getValue(), "1:10:30");
		assertEquals("", objOptions.scheduler_event_expires.getTimeAsSeconds(), 30 + 10 * 60 + 60 * 60);

	}

	@Test
	public void testscheduler_event_handler_host() { // SOSOptionHostName
		objOptions.scheduler_event_handler_host.setValue("++----++");
		assertEquals("", objOptions.scheduler_event_handler_host.getValue(), "++----++");

	}

	@Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testscheduler_event_handler_port() { // SOSOptionPortNumber
		objOptions.scheduler_event_handler_port.setValue("++----++");
		assertEquals("", objOptions.scheduler_event_handler_port.getValue(), "++----++");

	}

	@Test
	public void testscheduler_event_id() { // SOSOptionString
		objOptions.scheduler_event_id.setValue("++----++");
		assertEquals("", objOptions.scheduler_event_id.getValue(), "++----++");

	}

	@Test
	public void testscheduler_event_job() { // SOSOptionString
		objOptions.scheduler_event_job.setValue("++----++");
		assertEquals("", objOptions.scheduler_event_job.getValue(), "++----++");

	}

	@Test
	public void testsupervisor_job_chain() { // SOSOptionCommandString
		objOptions.supervisor_job_chain.setValue("++scheduler_event_service++");
		assertEquals("", objOptions.supervisor_job_chain.getValue(), "++scheduler_event_service++");

	}

} // public class JSEventsClientOptionsJUnitTest