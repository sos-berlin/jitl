

package sos.scheduler.CheckRunHistory;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * \class 		JobSchedulerCheckRunHistoryOptionsJUnitTest - Check the last job run
 *
 * \brief 
 *
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerCheckRunHistory.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com at 20110225184502 
 * \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um f�r einen Test eine HashMap
 * mit sinnvollen Werten f�r die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JobSchedulerCheckRunHistoryOptionsJUnitTest.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
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
		objOptions = objE.options();
		objOptions.registerMessageListener(this);
		
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@After
	public void tearDown() throws Exception {
	}


		

/**
 * \brief testJobChainName : The name of a job chain.
 * 
 * \details
 * The name of a job chain.
 *
 */
    @Test
    public void testJobChainName() {  // JSJobChainName
    	objOptions.jobChainName.Value("++----++");
    	assertEquals ("The name of a job chain.", objOptions.jobChainName.Value(),"++----++");
    	
    }

                

/**
 * \brief testJobName : The name of a job.
 * 
 * \details
 * The name of a job.
 *
 */
    @Test
    public void testJobName() {  // JSJobName
    	objOptions.jobName.Value("++----++");
    	assertEquals ("The name of a job.", objOptions.jobName.Value(),"++----++");
    	
    }

                

/**
 * \brief testmail_bcc : Email blind carbon copy address of the recipient, see ./c
 * 
 * \details
 * Email blind carbon copy address of the recipient, see ./config/factory.ini, log_mail_bcc.
 *
 */
    @Test
    public void testmail_bcc() {  // JSOptionMailOptions
    	objOptions.mail_bcc.Value("++----++");
    	assertEquals ("Email blind carbon copy address of the recipient, see ./c", objOptions.mail_bcc.Value(),"++----++");
    	
    }

                

/**
 * \brief testmail_cc : Email carbon copy address of the recipient, see ./config/
 * 
 * \details
 * Email carbon copy address of the recipient, see ./config/factory.ini, log_mail_cc.
 *
 */
    @Test
    public void testmail_cc() {  // JSOptionMailOptions
    	objOptions.mail_cc.Value("++----++");
    	assertEquals ("Email carbon copy address of the recipient, see ./config/", objOptions.mail_cc.Value(),"++----++");
    	
    }

                

/**
 * \brief testmail_to : Email address of the recipient, see ./config/factory.ini,
 * 
 * \details
 * Email address of the recipient, see ./config/factory.ini, log_mail_to.
 *
 */
    @Test
    public void testmail_to() {  // JSOptionMailOptions
    	objOptions.mail_to.Value("++----++");
    	assertEquals ("Email address of the recipient, see ./config/factory.ini,", objOptions.mail_to.Value(),"++----++");
    	
    }

                

/**
 * \brief testmessage : Text in the email subject and in the log.
 * 
 * \details
 * Text in the email subject and in the log. ${JOBNAME} will be substituted with the value of the parameter jobname. ${NOW} will be substituted with the current time.
 *
 */
    @Test
    public void testmessage() {  // SOSOptionString
    	 objOptions.message.Value("++----++");
    	 assertEquals ("Text in the email subject and in the log.", objOptions.message.Value(),"++----++");
    	
    }

                

/**
 * \brief testoperation : Operation to be executed
 * 
 * \details
 * 
 *
 */
    @Test
    public void testoperation() {  // SOSOptionStringValueList
    	objOptions.query.Value("++late++");
    	assertEquals ("Operation to be executed", objOptions.query.Value(),"++late++");
    	
    }

                

/**
 * \brief testOrderId : The name or the identification of an order.
 * 
 * \details
 * The name or the identification of an order.
 *
 */
    @Test
    public void testOrderId() {  // JSOrderId
    	objOptions.orderId.Value("++----++");
    	assertEquals ("The name or the identification of an order.", objOptions.orderId.Value(),"++----++");
    	
    }

                

/**
 * \brief teststart_time : The start time from which the parametrisized job is check
 * 
 * \details
 * The start time from which the parametrisized job is checked wether it has successfully run or not. The start time must be set in the form [number of elapsed days],Time(HH:MM:SS), so that the default value is last midnight.
 *
 */
    @Test
    public void teststart_time() {  // SOSOptionString
    	 objOptions.start_time.Value("++0,00:00:00++");
    	 assertEquals ("The start time from which the parametrisized job is check", objOptions.start_time.Value(),"++0,00:00:00++");
    	
    }

                
        
} // public class JobSchedulerCheckRunHistoryOptionsJUnitTest