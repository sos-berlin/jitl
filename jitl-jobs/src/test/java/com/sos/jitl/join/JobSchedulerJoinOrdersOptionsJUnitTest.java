

package com.sos.jitl.join;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;


 
public class JobSchedulerJoinOrdersOptionsJUnitTest extends  JSToolBox {
	
	protected JobSchedulerJoinOrdersOptions	objOptions = null;
	private static final int DEBUG9 = 9;
	private static final String CLASSNAME = "JobSchedulerJoinOrdersOptionsJUnitTest"; 
	private JobSchedulerJoinOrders objE = null;

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
		objE = new JobSchedulerJoinOrders();
		objE.registerMessageListener(this);
		objOptions = objE.getOptions();
		objOptions.registerMessageListener(this);
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = DEBUG9;
	}

	@After
	public void tearDown() throws Exception {
        // Implement Method here
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
	