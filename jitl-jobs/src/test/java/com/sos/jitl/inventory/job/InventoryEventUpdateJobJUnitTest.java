

package com.sos.jitl.inventory.job;

import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;


public class InventoryEventUpdateJobJUnitTest extends JSToolBox {

	protected InventoryEventUpdateJobOptions	objOptions = null;
	private static final String	CLASSNAME = "InventoryEventUpdateJobJUnitTest"; 
	private static final Logger	LOGGER = Logger.getLogger(InventoryEventUpdateJobJUnitTest.class);
	private InventoryEventUpdateJob objE = null;
	
	public InventoryEventUpdateJobJUnitTest() {
		//
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
        // TODO: Implement Method setUpBeforeClass here
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
        // TODO: Implement Method tearDownAfterClass here
	}

	@Before
	public void setUp() throws Exception {
//		objE = new InventoryEventUpdateJob();
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@After
	public void tearDown() throws Exception {
        // TODO: Implement Method tearDown here
	}

	@Test
	public void testExecute() throws Exception {
		objE.execute();
	}
	
	@Test
	public void testUriBuilder() throws URISyntaxException {
	    URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http");
        uriBuilder.setHost("localhost");
        uriBuilder.setPort(Integer.parseInt("40444"));
        uriBuilder.setPath("/jobscheduler/master/api/event");
        uriBuilder.setParameter("return", "SchedulerEvent");
        uriBuilder.setParameter("after", "90");
        uriBuilder.setParameter("eventId", "1482092128000");
        LOGGER.info(uriBuilder.build().toString());
        uriBuilder.setParameter("eventId", "1482092128001");
        LOGGER.info(uriBuilder.build().toString());
	}

}   
