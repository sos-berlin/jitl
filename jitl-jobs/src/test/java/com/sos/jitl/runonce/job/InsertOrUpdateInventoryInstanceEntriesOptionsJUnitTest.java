package com.sos.jitl.runonce.job;

import org.apache.log4j.Logger;
import org.junit.Before;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

public class InsertOrUpdateInventoryInstanceEntriesOptionsJUnitTest extends  JSToolBox {
	
	protected InsertOrUpdateInventoryInstanceEntriesOptions	options = null;
	private static final int DEBUG9 = 9;
	private static final Logger LOGGER = Logger.getLogger(InsertOrUpdateInventoryInstanceEntriesOptionsJUnitTest.class);
	private InsertOrUpdateInventoryInstanceEntriesJob entriesJob = null;

	@Before
	public void setUp() throws Exception {
		entriesJob = new InsertOrUpdateInventoryInstanceEntriesJob();
		entriesJob.registerMessageListener(this);
		options = entriesJob.getOptions();
		options.registerMessageListener(this);
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = DEBUG9;
	}
		       
}  
	