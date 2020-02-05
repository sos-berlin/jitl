package com.sos.jitl.eventing;

import org.apache.xpath.XPathAPI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import sos.scheduler.job.JobSchedulerExistsEventJob;

public class JSEventsClientBaseClassTest extends JSEventsClientBaseClass {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSEventsClientBaseClassTest.class);

	@Before
	public void setUp() throws Exception {
		jsEventsClientOptions = new JSEventsClientOptions();
		jsEventsClientOptions.scheduler_event_handler_host.setValue("localhost");
		jsEventsClientOptions.scheduler_event_handler_port.value(4446);
		this.setOptions(jsEventsClientOptions);
	}

	@Test
	@Ignore("Test set to Ignore for later examination")
	public void testGetEventsFromSchedulerVar() throws Exception {
		readEventsFromDB();
	}

	@Test
	public void testSetEvent() throws Exception {
		jsEventsClientOptions.operation.setValue("add");
		jsEventsClientOptions.EventClass.setValue("kbtest");
		jsEventsClientOptions.id.setValue("TestEvent");
		this.setOptions(jsEventsClientOptions);
		JSEventsClient objCl = new JSEventsClient();
		objCl.getOptions(jsEventsClientOptions);
		objCl.execute();
		jsEventsClientOptions.id.setValue("TestEvent2");
		objCl.execute();
	}

	@Test
	@Ignore("Test set to Ignore for later examination")
	public void testCheckEvent() throws Exception {
		String eventSpec = "//event[@event_class='kbtest' and (@event_id='TestEvent' and @event_id='TestEvent')]";
		checkEvents(eventSpec);
		eventSpec = "//event[@event_class='kbtest']";
		checkEvents(eventSpec);
		eventSpec = "//event[@event_class='kbtest' and (@event_id='TestEvent' and not(@event_id='TestEvent'))]";
		checkEvents(eventSpec);
		eventSpec = "//event[@event_class='kbtest' and (@event_id='TestEvent' and not(@event_id='TestEvent'))]";
		checkEvents(eventSpec);
		eventSpec = "//event[@event_class='kbtest' and (@event_id='a' and @event_id='b') and  not(@event_id='c')]";
		checkEvents(eventSpec);
	}

	@Test
	public void testDeleteEvent() throws Exception {
		jsEventsClientOptions.operation.setValue("remove");
		jsEventsClientOptions.EventClass.setValue("kbtest");
		jsEventsClientOptions.id.setValue("TestEvent");
		this.setOptions(jsEventsClientOptions);
		JSEventsClient objCl = new JSEventsClient();
		objCl.getOptions(jsEventsClientOptions);
		objCl.execute();
		jsEventsClientOptions.id.setValue("TestEvent2");
		jsEventsClientOptions.id.setValue("TestEvent2");
		objCl.execute();
	}

	private Document readEventsFromDB() throws Exception {
		JobSchedulerExistsEventJob jobSchedulerExistsEventJob = new JobSchedulerExistsEventJob();
		jobSchedulerExistsEventJob.setConfigurationFile("R:/nobackup/junittests/hibernate/hibernate_oracle.cfg.xml");
		return jobSchedulerExistsEventJob.readEventsFromDB();
	}

	private int checkEvents(final String eventSpec) throws Exception {
		Document eventDocument = readEventsFromDB();
		int intNoOfEvents = 0;
		NodeList nodes = XPathAPI.selectNodeList(eventDocument, eventSpec);
		if (nodes == null || nodes.getLength() == 0) {
			LOGGER.info(String.format("*No* matching events '%1$s' were found.", eventSpec));
		} else {
			intNoOfEvents = nodes.getLength();
			LOGGER.info(String.format("%1$d Match found.", intNoOfEvents));
		}
		return intNoOfEvents;
	}

	@Test
	@Ignore("Test set to Ignore for later examination")
	public void testCreateXPath() throws Exception {
		String strR = "";
		String strExp = "a and b and c";
		strR = createXPath(strExp);
		Assert.assertEquals("", "@event_id='a' and @event_id='b' and @event_id='c'", strR);
		strR = createXPath("a or b or c");
		Assert.assertEquals("", "@event_id='a' or @event_id='b' or @event_id='c'", strR);
		strR = createXPath("(a and b) or c");
		Assert.assertEquals("", "(@event_id='a' and @event_id='b') or @event_id='c'", strR);
		strR = createXPath("(a and b) and not(c)");
		Assert.assertEquals("", "(@event_id='a' and @event_id='b') and  not(@event_id='c')", strR);
		strR = createXPath("((a or b) and c) and not(d)");
		Assert.assertEquals("", "((@event_id='a' or @event_id='b') and @event_id='c') and  not(@event_id='d')", strR);
		checkEvents("//event[@event_class='kbtest' and " + strR + "]");
		strR = createXPath("((a or b) and c) and (not(d) or e)");
		Assert.assertEquals("",
				"((@event_id='a' or @event_id='b') and @event_id='c') and ( not(@event_id='d') or @event_id='e')",
				strR);
		checkEvents("//event[@event_class='kbtest' and " + strR + "]");
		strR = createXPath("((a or b) and c) and (not(d or e))");
		Assert.assertEquals("",
				"((@event_id='a' or @event_id='b') and @event_id='c') and ( not(@event_id='d' or @event_id='e'))",
				strR);
		checkEvents("//event[@event_class='kbtest' and " + strR + "]");
	}

	private String createXPath(final String pstrString) {
		String[] strA = pstrString.split(" ");
		LOGGER.debug(pstrString);
		boolean flgOpexp = false;
		String strX = "";
		String strKlaZu = "";
		for (String string : strA) {
			string = string.trim();
			if (flgOpexp) {
				flgOpexp = false;
				strX += " " + string + " ";
			} else {
				flgOpexp = true;
				while (string.startsWith("(")) {
					strX += "(";
					string = string.substring(1);
				}
				if (string.startsWith("not(")) {
					strX += " not(";
					string = string.substring(4);
				}
				if (string.startsWith("(")) {
					while (string.startsWith("(")) {
						strX += "(";
						string = string.substring(1);
					}
				}
				if (string.endsWith(")")) {
					while (string.endsWith(")")) {
						strKlaZu += ")";
						string = string.substring(0, string.length() - 1);
					}
				}
				strX += "@event_id='" + string + "'" + strKlaZu;
				strKlaZu = "";
			}
		}
		LOGGER.debug(strX);
		return strX;
	}

}