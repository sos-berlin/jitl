

package com.sos.jitl.inventory.job;

import static org.junit.Assert.assertEquals;
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

}   
