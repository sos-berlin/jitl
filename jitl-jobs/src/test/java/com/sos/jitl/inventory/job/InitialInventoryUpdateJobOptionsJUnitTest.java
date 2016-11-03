package com.sos.jitl.inventory.job;

import org.apache.log4j.Logger;
import org.junit.Before;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.jitl.inventory.job.InitialInventoryUpdateJob;
import com.sos.jitl.inventory.job.InitialInventoryUpdateJobOptions;

public class InitialInventoryUpdateJobOptionsJUnitTest extends  JSToolBox {
	
	protected InitialInventoryUpdateJobOptions	options = null;
	private static final int DEBUG9 = 9;
	private static final Logger LOGGER = Logger.getLogger(InitialInventoryUpdateJobOptionsJUnitTest.class);
	private InitialInventoryUpdateJob entriesJob = null;

	@Before
	public void setUp() throws Exception {
		entriesJob = new InitialInventoryUpdateJob();
		entriesJob.registerMessageListener(this);
		options = entriesJob.getOptions();
		options.registerMessageListener(this);
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = DEBUG9;
	}
		       
}  
	