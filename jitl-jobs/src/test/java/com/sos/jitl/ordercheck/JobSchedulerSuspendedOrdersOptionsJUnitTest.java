

package com.sos.jitl.ordercheck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

 

 
public class JobSchedulerSuspendedOrdersOptionsJUnitTest extends  JSToolBox {
	private final String					conClassName						= "SuspendedOrdersOptionsJUnitTest"; //$NON-NLS-1$
		@SuppressWarnings("unused") //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerSuspendedOrdersOptionsJUnitTest.class);
	private JobSchedulerSuspendedOrders objE = null;

	protected JobSchedulerSuspendedOrdersOptions	objOptions			= null;

	public JobSchedulerSuspendedOrdersOptionsJUnitTest() {
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
		objE = new JobSchedulerSuspendedOrders();
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
    public void testjava_class() {  // SOSOptionString
    	 objOptions.java_class.Value("++com.sos.jitl.sync.JobSchedulerSynchronizeJobChainsJSAdapterClass++");
    	 assertEquals ("", objOptions.java_class.Value(),"++com.sos.jitl.sync.JobSchedulerSynchronizeJobChainsJSAdapterClass++");
    	
    }
 
    @Test
    public void testorders_answer() {  // SOSOptionString
    	 objOptions.orders_answer.Value("++----++");
    	 assertEquals ("", objOptions.orders_answer.Value(),"++----++");
    	
    }

                
        
} // public class SuspendedOrdersOptionsJUnitTest