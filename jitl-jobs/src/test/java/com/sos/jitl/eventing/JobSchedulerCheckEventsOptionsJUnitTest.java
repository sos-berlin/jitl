package com.sos.jitl.eventing;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.jitl.eventing.checkevents.JobSchedulerCheckEvents;
import com.sos.jitl.eventing.checkevents.JobSchedulerCheckEventsOptions;
import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/** \class JobSchedulerCheckEventsOptionsJUnitTest - Check if events exist
 *
 * \brief
 *
 *
 * 
 *
 * 
 * \verbatim ; \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim private HashMap <String, String> SetJobSchedulerSSHJobOptions
 * (HashMap <String, String> pobjHM) { pobjHM.put
 * ("		JobSchedulerCheckEventsOptionsJUnitTest.auth_file", "test"); // This
 * parameter specifies the path and name of a user's pr return pobjHM; } //
 * private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim */
public class JobSchedulerCheckEventsOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "JobSchedulerCheckEventsOptionsJUnitTest"; //$NON-NLS-1$
    @SuppressWarnings("unused")//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JobSchedulerCheckEventsOptionsJUnitTest.class);
    @SuppressWarnings("unused")
    private JobSchedulerCheckEvents objE = null;

    protected JobSchedulerCheckEventsOptions objOptions = null;

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
        objE = new JobSchedulerCheckEvents();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    /** \brief testconfiguration_file :
     * 
     * \details */
    @Test
    public void testconfiguration_file() {  // SOSOptionString
        objOptions.configuration_file.Value("++----++");
        assertEquals("", objOptions.configuration_file.Value(), "++----++");

    }

    /** \brief testevent_class :
     * 
     * \details */
    @Test
    public void testevent_class() {  // SOSOptionString
        objOptions.event_class.Value("++----++");
        assertEquals("", objOptions.event_class.Value(), "++----++");

    }

    /** \brief testevent_condition :
     * 
     * \details */
    @Test
    public void testevent_condition() {  // SOSOptionString
        objOptions.event_condition.Value("++----++");
        assertEquals("", objOptions.event_condition.Value(), "++----++");

    }

    /** \brief testevent_exit_code :
     * 
     * \details */
    @Test
    public void testevent_exit_code() {  // SOSOptionString
        objOptions.event_exit_code.Value("++----++");
        assertEquals("", objOptions.event_exit_code.Value(), "++----++");

    }

    /** \brief testevent_id :
     * 
     * \details */
    @Test
    public void testevent_id() {  // SOSOptionString
        objOptions.event_id.Value("++----++");
        assertEquals("", objOptions.event_id.Value(), "++----++");

    }

    /** \brief testevent_job :
     * 
     * \details */
    @Test
    public void testevent_job() {  // SOSOptionString
        objOptions.event_job.Value("++----++");
        assertEquals("", objOptions.event_job.Value(), "++----++");

    }

    /** \brief testevent_job_chain :
     * 
     * \details */
    @Test
    public void testevent_job_chain() {  // SOSOptionString
        objOptions.event_job_chain.Value("++----++");
        assertEquals("", objOptions.event_job_chain.Value(), "++----++");

    }

    /** \brief testevent_order_id :
     * 
     * \details */
    @Test
    public void testevent_order_id() {  // SOSOptionString
        objOptions.event_order_id.Value("++----++");
        assertEquals("", objOptions.event_order_id.Value(), "++----++");

    }

    /** \brief testevent_scheduler_id :
     * 
     * \details */
    @Test
    public void testevent_scheduler_id() {  // SOSOptionString
        objOptions.event_scheduler_id.Value("++----++");
        assertEquals("", objOptions.event_scheduler_id.Value(), "++----++");

    }

    /** \brief testhandle_existing_as :
     * 
     * \details */
    @Test
    public void testhandle_existing_as() {  // SOSOptionString
        objOptions.handle_existing_as.Value("++----++");
        assertEquals("", objOptions.handle_existing_as.Value(), "++----++");

    }

    /** \brief testhandle_not_existing_as :
     * 
     * \details */
    @Test
    public void testhandle_not_existing_as() {  // SOSOptionString
        objOptions.handle_not_existing_as.Value("++----++");
        assertEquals("", objOptions.handle_not_existing_as.Value(), "++----++");

    }

    /** \brief testremote_scheduler_host :
     * 
     * \details */
    @Test
    public void testremote_scheduler_host() {  // SOSOptionString
        objOptions.remote_scheduler_host.Value("++----++");
        assertEquals("", objOptions.remote_scheduler_host.Value(), "++----++");

    }

    /** \brief testremote_scheduler_port :
     * 
     * \details */
    @Test
    public void testremote_scheduler_port() {  // SOSOptionString
        objOptions.remote_scheduler_port.Value("++----++");
        assertEquals("", objOptions.remote_scheduler_port.Value(), "++----++");

    }

} // public class JobSchedulerCheckEventsOptionsJUnitTest