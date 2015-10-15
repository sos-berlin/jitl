

package com.sos.jitl.checkrunhistory;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;


public class JobSchedulerCheckRunHistoryOptionsJUnitTest extends  JSToolBox {
	private final String					conClassName						= "JobSchedulerCheckRunHistoryOptionsJUnitTest"; //$NON-NLS-1$
		@SuppressWarnings("unused") //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerCheckRunHistoryOptionsJUnitTest.class);
	private JobSchedulerCheckRunHistory objE = null;

	protected JobSchedulerCheckRunHistoryOptions	objOptions			= null;

	public JobSchedulerCheckRunHistoryOptionsJUnitTest() {
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
		objE = new JobSchedulerCheckRunHistory();
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
    public void testJobChainName() {  // JSJobChainName
    	objOptions.JobChainName.Value("++----++");
    	assertEquals ("The name of a job chain.", objOptions.JobChainName.Value(),"++----++");
    	
    }

   
    @Test
    public void testJobName() {  // JSJobName
    	objOptions.JobName.Value("++----++");
    	assertEquals ("The name of a job.", objOptions.JobName.Value(),"++----++");
    	
    }

  
    @Test
    public void testmail_bcc() {  // JSOptionMailOptions
    	objOptions.mail_bcc.Value("++----++");
    	assertEquals ("Email blind carbon copy address of the recipient, see ./c", objOptions.mail_bcc.Value(),"++----++");
    	
    }

     
    @Test
    public void testmail_cc() {  // JSOptionMailOptions
    	objOptions.mail_cc.Value("++----++");
    	assertEquals ("Email carbon copy address of the recipient, see ./config/", objOptions.mail_cc.Value(),"++----++");
    	
    }

  
    @Test
    public void testmail_to() {  // JSOptionMailOptions
    	objOptions.mail_to.Value("++----++");
    	assertEquals ("Email address of the recipient, see ./config/factory.ini,", objOptions.mail_to.Value(),"++----++");
    	
    }

          
    @Test
    public void testmessage() {  // SOSOptionString
    	 objOptions.message.Value("++----++");
    	 assertEquals ("Text in the email subject and in the log.", objOptions.message.Value(),"++----++");
    	
    }

    
    @Test
    public void testoperation() {  // SOSOptionStringValueList
    	objOptions.operation.Value("++late++");
    	assertEquals ("Operation to be executed", objOptions.operation.Value(),"++late++");
    	
    }

  
    @Test
    public void testOrderId() {  // JSOrderId
    	objOptions.OrderId.Value("++----++");
    	assertEquals ("The name or the identification of an order.", objOptions.OrderId.Value(),"++----++");
    	
    }

  
    @Test
    public void teststart_time() {  // SOSOptionString
    	 objOptions.start_time.Value("++0,00:00:00++");
    	 assertEquals ("The start time from which the parametrisized job is check", objOptions.start_time.Value(),"++0,00:00:00++");
    	
    }

                
        
} // public class JobSchedulerCheckRunHistoryOptionsJUnitTest