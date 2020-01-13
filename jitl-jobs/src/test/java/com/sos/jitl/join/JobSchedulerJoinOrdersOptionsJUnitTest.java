package com.sos.jitl.join;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
 
public class JobSchedulerJoinOrdersOptionsJUnitTest extends  JSToolBox {
	
	protected JobSchedulerJoinOrdersOptions	objOptions = null;
	private static final int DEBUG9 = 9;
	private static final String CLASSNAME = "JobSchedulerJoinOrdersOptionsJUnitTest"; 
	private JobSchedulerJoinOrders objE = null;

	@Before
	public void setUp() throws Exception {
		objE = new JobSchedulerJoinOrders();
		objE.registerMessageListener(this);
		objOptions = objE.getOptions();
		objOptions.registerMessageListener(this);
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = DEBUG9;
	}

    @Test
    public void testrequired_orders() { 
    	 objOptions.required_orders.setValue("++----++");
    	 assertEquals ("", objOptions.required_orders.getValue(),"++----++");
    }
 
    @Test
    public void testjoin_session_id() { 
    	 objOptions.joinSessionId.setValue("++----++");
    	 assertEquals ("", objOptions.joinSessionId.getValue(),"++----++");
    }
		       
}  
	