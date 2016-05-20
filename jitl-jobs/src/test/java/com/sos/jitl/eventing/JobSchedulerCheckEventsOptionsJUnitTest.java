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
        objOptions.configuration_file.setValue("++----++");
        assertEquals("", objOptions.configuration_file.getValue(), "++----++");

    }

    /** \brief testevent_class :
     * 
     * \details */
    @Test
    public void testevent_class() {  // SOSOptionString
        objOptions.event_class.setValue("++----++");
        assertEquals("", objOptions.event_class.getValue(), "++----++");

    }

    /** \brief testevent_condition :
     * 
     * \details */
    @Test
    public void testevent_condition() {  // SOSOptionString
        objOptions.event_condition.setValue("++----++");
        assertEquals("", objOptions.event_condition.getValue(), "++----++");

    }

    /** \brief testevent_exit_code :
     * 
     * \details */
    @Test
    public void testevent_exit_code() {  // SOSOptionString
        objOptions.event_exit_code.setValue("++----++");
        assertEquals("", objOptions.event_exit_code.getValue(), "++----++");

    }

    /** \brief testevent_id :
     * 
     * \details */
    @Test
    public void testevent_id() {  // SOSOptionString
        objOptions.event_id.setValue("++----++");
        assertEquals("", objOptions.event_id.getValue(), "++----++");

    }

    /** \brief testevent_job :
     * 
     * \details */
    @Test
    public void testevent_job() {  // SOSOptionString
        objOptions.event_job.setValue("++----++");
        assertEquals("", objOptions.event_job.getValue(), "++----++");

    }

    /** \brief testevent_job_chain :
     * 
     * \details */
    @Test
    public void testevent_job_chain() {  // SOSOptionString
        objOptions.event_job_chain.setValue("++----++");
        assertEquals("", objOptions.event_job_chain.getValue(), "++----++");

    }

    /** \brief testevent_order_id :
     * 
     * \details */
    @Test
    public void testevent_order_id() {  // SOSOptionString
        objOptions.event_order_id.setValue("++----++");
        assertEquals("", objOptions.event_order_id.getValue(), "++----++");

    }

    /** \brief testevent_scheduler_id :
     * 
     * \details */
    @Test
    public void testevent_scheduler_id() {  // SOSOptionString
        objOptions.event_scheduler_id.setValue("++----++");
        assertEquals("", objOptions.event_scheduler_id.getValue(), "++----++");

    }

    /** \brief testhandle_existing_as :
     * 
     * \details */
    @Test
    public void testhandle_existing_as() {  // SOSOptionString
        objOptions.handle_existing_as.setValue("++----++");
        assertEquals("", objOptions.handle_existing_as.getValue(), "++----++");

    }

    /** \brief testhandle_not_existing_as :
     * 
     * \details */
    @Test
    public void testhandle_not_existing_as() {  // SOSOptionString
        objOptions.handle_not_existing_as.setValue("++----++");
        assertEquals("", objOptions.handle_not_existing_as.getValue(), "++----++");

    }

    /** \brief testremote_scheduler_host :
     * 
     * \details */
    @Test
    public void testremote_scheduler_host() {  // SOSOptionString
        objOptions.remote_scheduler_host.setValue("++----++");
        assertEquals("", objOptions.remote_scheduler_host.getValue(), "++----++");

    }

    /** \brief testremote_scheduler_port :
     * 
     * \details */
    @Test
    public void testremote_scheduler_port() {  // SOSOptionString
        objOptions.remote_scheduler_port.setValue("++----++");
        assertEquals("", objOptions.remote_scheduler_port.getValue(), "++----++");

    }

} // public class JobSchedulerCheckEventsOptionsJUnitTest