

package com.sos.jitl.eventing;

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
import com.sos.jitl.eventing.checkevents.JobSchedulerCheckEvents;
import com.sos.jitl.eventing.checkevents.JobSchedulerCheckEventsOptions;

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
 * \brief testconfiguration_file : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testconfiguration_file() {  // SOSOptionString
    	 objOptions.configuration_file.Value("++----++");
    	 assertEquals ("", objOptions.configuration_file.Value(),"++----++");
    	
    }

                


                

/**
 * \brief testevent_class : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testevent_class() {  // SOSOptionString
    	 objOptions.event_class.Value("++----++");
    	 assertEquals ("", objOptions.event_class.Value(),"++----++");
    	
    }

                

/**
 * \brief testevent_condition : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testevent_condition() {  // SOSOptionString
    	 objOptions.event_condition.Value("++----++");
    	 assertEquals ("", objOptions.event_condition.Value(),"++----++");
    	
    }

                

/**
 * \brief testevent_exit_code : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testevent_exit_code() {  // SOSOptionString
    	 objOptions.event_exit_code.Value("++----++");
    	 assertEquals ("", objOptions.event_exit_code.Value(),"++----++");
    	
    }

                

/**
 * \brief testevent_id : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testevent_id() {  // SOSOptionString
    	 objOptions.event_id.Value("++----++");
    	 assertEquals ("", objOptions.event_id.Value(),"++----++");
    	
    }

                

/**
 * \brief testevent_scheduler_id : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testevent_scheduler_id() {  // SOSOptionString
    	 objOptions.event_scheduler_id.Value("++----++");
    	 assertEquals ("", objOptions.event_scheduler_id.Value(),"++----++");
    	
    }

                

/**
 * \brief testhandle_existing_as : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testhandle_existing_as() {  // SOSOptionString
    	 objOptions.handle_existing_as.Value("++----++");
    	 assertEquals ("", objOptions.handle_existing_as.Value(),"++----++");
    	
    }

                

/**
 * \brief testhandle_not_existing_as : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testhandle_not_existing_as() {  // SOSOptionString
    	 objOptions.handle_not_existing_as.Value("++----++");
    	 assertEquals ("", objOptions.handle_not_existing_as.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_event_handler_host : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_event_handler_host() {  // SOSOptionString
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
    public void testscheduler_event_handler_port() {  // SOSOptionString
    	 objOptions.scheduler_event_handler_port.Value("++----++");
    	 assertEquals ("", objOptions.scheduler_event_handler_port.Value(),"++----++");
    	
    }

                
        
} // public class JobSchedulerCheckEventsOptionsJUnitTest