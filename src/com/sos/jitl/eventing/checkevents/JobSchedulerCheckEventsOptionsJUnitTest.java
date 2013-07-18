

package com.sos.jitl.eventing.checkevents;

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
import com.sos.JSHelper.Logging.Log4JHelper;

/**
 * \class 		JobSchedulerCheckEventsOptionsJUnitTest - Check if events exist
 *
 * \brief 
 *
 *

 *
 * 
 * \verbatim ;
 * \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JobSchedulerCheckEventsOptionsJUnitTest.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
public class JobSchedulerCheckEventsOptionsJUnitTest extends  JSToolBox {
	private final String					conClassName						= "JobSchedulerCheckEventsOptionsJUnitTest"; //$NON-NLS-1$
		@SuppressWarnings("unused") //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerCheckEventsOptionsJUnitTest.class);
	@SuppressWarnings("unused")
	private static Log4JHelper	objLogger		= null;
	private JobSchedulerCheckEvents objE = null;

	protected JobSchedulerCheckEventsOptions	objOptions			= null;

	public JobSchedulerCheckEventsOptionsJUnitTest() {
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
		objLogger = new Log4JHelper("./log4j.properties"); //$NON-NLS-1$
		objE = new JobSchedulerCheckEvents();
		objE.registerMessageListener(this);
		objOptions = objE.Options();
		objOptions.registerMessageListener(this);
		
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@After
	public void tearDown() throws Exception {
	}


		

/**
 * \brief testEventClassName : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testEventClassName() {  // SOSOptionString
    	 objOptions.EventClassName.Value("++----++");
    	 assertEquals ("", objOptions.EventClassName.Value(),"++----++");
    	
    }

                

/**
 * \brief testEventNames : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testEventNames() {  // SOSOptionString
    	 objOptions.EventNames.Value("++----++");
    	 assertEquals ("", objOptions.EventNames.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_event_class : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_event_class() {  // SOSOptionString
    	 objOptions.scheduler_event_class.Value("++----++");
    	 assertEquals ("", objOptions.scheduler_event_class.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_event_exit_code : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_event_exit_code() {  // SOSOptionString
    	 objOptions.scheduler_event_exit_code.Value("++----++");
    	 assertEquals ("", objOptions.scheduler_event_exit_code.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_event_handler_host : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_event_handler_host() {  // SOSOptionHostName
    	objOptions.scheduler_event_handler_host.Value("++----++");
    	assertEquals ("", objOptions.scheduler_event_handler_host.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_event_handler_port : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_event_handler_port() {  // SOSOptionPortNumber
    	objOptions.scheduler_event_handler_port.Value("++----++");
    	assertEquals ("", objOptions.scheduler_event_handler_port.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_event_id : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_event_id() {  // SOSOptionString
    	 objOptions.scheduler_event_id.Value("++----++");
    	 assertEquals ("", objOptions.scheduler_event_id.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_event_job : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_event_job() {  // SOSOptionString
    	 objOptions.scheduler_event_job.Value("++----++");
    	 assertEquals ("", objOptions.scheduler_event_job.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_event_jobchain : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_event_jobchain() {  // SOSOptionString
    	 objOptions.scheduler_event_jobchain.Value("++----++");
    	 assertEquals ("", objOptions.scheduler_event_jobchain.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_event_xpath : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_event_xpath() {  // SOSOptionString
    	 objOptions.scheduler_event_xpath.Value("++----++");
    	 assertEquals ("", objOptions.scheduler_event_xpath.Value(),"++----++");
    	
    }

                
        
} // public class JobSchedulerCheckEventsOptionsJUnitTest