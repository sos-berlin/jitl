package com.sos.jitl.eventing;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/** \class JSEventsClientOptionsJUnitTest - Submit and Delete Events
 *
 * \brief
 *
 *
 * 
 *
 * see \see
 * C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for
 * (more) details.
 *
 * \verbatim ; mechanicaly created by
 * C:\ProgramData\sos-berlin.com\jobscheduler\
 * latestscheduler\config\JOETemplates
 * \java\xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com
 * at 20130109134235 \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim private HashMap <String, String> SetJobSchedulerSSHJobOptions
 * (HashMap <String, String> pobjHM) { pobjHM.put
 * ("		JSEventsClientOptionsJUnitTest.auth_file", "test"); // This parameter
 * specifies the path and name of a user's pr return pobjHM; } // private void
 * SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) \endverbatim */
public class JSEventsClientOptionsJUnitTest extends JSToolBox {

    @SuppressWarnings("unused")
    private final String conClassName = "JSEventsClientOptionsJUnitTest"; //$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JSEventsClientOptionsJUnitTest.class);
    private JSEventsClient objE = null;

    protected JSEventsClientOptions objOptions = null;

    @Before
    public void setUp() throws Exception {
        objE = new JSEventsClient();
        objOptions = objE.Options();
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    /** \brief testdel_events :
     *
     * \details Event ID (arbitrary) */
    @Test
    public void testdel_events() {  // SOSOptionString
        objOptions.del_events.Value("++----++");
        assertEquals("", objOptions.del_events.Value(), "++----++");
    }

    /** \brief testscheduler_event_action :
     *
     * \details Action to be performed: add - add Event remove - remove Event(s)
     * When removing an event, the parameters scheduler_event_job
     * scheduler_event_host scheduler_event_port scheduler_event_exit_code
     * (along with the parameters which are used for adding) can be used to
     * specify the event. */
    @Test
    public void testscheduler_event_action() {  // SOSOptionString
        objOptions.scheduler_event_action.Value("++add++");
        assertEquals("", objOptions.scheduler_event_action.Value(), "++add++");

    }

    /** \brief testscheduler_event_class :
     *
     * \details Event class (arbitrary) */
    @Test
    public void testscheduler_event_class() {  // SOSOptionString
        objOptions.scheduler_event_class.Value("++----++");
        assertEquals("", objOptions.scheduler_event_class.Value(), "++----++");

    }

    /** \brief testscheduler_event_exit_code :
     *
     * \details JobName for which the event is valid */
    @Test
    public void testscheduler_event_exit_code() {  // SOSOptionInteger
        objOptions.scheduler_event_exit_code.Value("12345");
        assertEquals("", objOptions.scheduler_event_exit_code.Value(), "12345");
        assertEquals("", objOptions.scheduler_event_exit_code.value(), 12345);
        objOptions.scheduler_event_exit_code.value(12345);
        assertEquals("", objOptions.scheduler_event_exit_code.Value(), "12345");
        assertEquals("", objOptions.scheduler_event_exit_code.value(), 12345);

    }

    /** \brief testscheduler_event_expiration_cycle :
     *
     * \details Similar to scheduler_event_expiration_period this parameter
     * specifies a time (e.g. 06:00) when an event will expire.
     * scheduler_event_expiration_cycle takes precedence over
     * scheduler_event_expiration_period . */
    @Test
    public void testscheduler_event_expiration_cycle() {  // SOSOptionTime
        objOptions.scheduler_event_expiration_cycle.Value("30");
        assertEquals("", objOptions.scheduler_event_expiration_cycle.Value(), "30");
        assertEquals("", objOptions.scheduler_event_expiration_cycle.getTimeAsSeconds(), 30);
        objOptions.scheduler_event_expiration_cycle.Value("1:30");
        assertEquals("", objOptions.scheduler_event_expiration_cycle.Value(), "1:30");
        assertEquals("", objOptions.scheduler_event_expiration_cycle.getTimeAsSeconds(), 90);
        objOptions.scheduler_event_expiration_cycle.Value("1:10:30");
        assertEquals("", objOptions.scheduler_event_expiration_cycle.Value(), "1:10:30");
        assertEquals("", objOptions.scheduler_event_expiration_cycle.getTimeAsSeconds(), 30 + 10 * 60 + 60 * 60);

    }

    /** \brief testscheduler_event_expiration_period :
     *
     * \details This parameter specifies an expiration period for events. */
    @Test
    public void testscheduler_event_expiration_period() {  // SOSOptionTimeRange
        objOptions.scheduler_event_expiration_period.Value("++----++");
        assertEquals("", objOptions.scheduler_event_expiration_period.Value(), "++----++");

    }

    /** \brief testscheduler_event_expires :
     *
     * \details Expiration date of the event (ISO-format yyyy-mm-dd hh:mm:ss) or
     * "never" */
    @Test
    public void testscheduler_event_expires() {  // SOSOptionTime
        objOptions.scheduler_event_expires.Value("30");
        assertEquals("", objOptions.scheduler_event_expires.Value(), "30");
        assertEquals("", objOptions.scheduler_event_expires.getTimeAsSeconds(), 30);
        objOptions.scheduler_event_expires.Value("1:30");
        assertEquals("", objOptions.scheduler_event_expires.Value(), "1:30");
        assertEquals("", objOptions.scheduler_event_expires.getTimeAsSeconds(), 90);
        objOptions.scheduler_event_expires.Value("1:10:30");
        assertEquals("", objOptions.scheduler_event_expires.Value(), "1:10:30");
        assertEquals("", objOptions.scheduler_event_expires.getTimeAsSeconds(), 30 + 10 * 60 + 60 * 60);

    }

    /** \brief testscheduler_event_handler_host :
     *
     * \details Uses a JobScheduler (other than the supervisor) as event handler */
    @Test
    public void testscheduler_event_handler_host() {  // SOSOptionHostName
        objOptions.scheduler_event_handler_host.Value("++----++");
        assertEquals("", objOptions.scheduler_event_handler_host.Value(), "++----++");

    }

    /** \brief testscheduler_event_handler_port :
     *
     * \details Defines a JobScheduler (other than the supervisor) as event
     * service. */
    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testscheduler_event_handler_port() {  // SOSOptionPortNumber
        objOptions.scheduler_event_handler_port.Value("++----++");
        assertEquals("", objOptions.scheduler_event_handler_port.Value(), "++----++");

    }

    /** \brief testscheduler_event_id :
     *
     * \details Event ID (arbitrary) */
    @Test
    public void testscheduler_event_id() {  // SOSOptionString
        objOptions.scheduler_event_id.Value("++----++");
        assertEquals("", objOptions.scheduler_event_id.Value(), "++----++");

    }

    /** \brief testscheduler_event_job :
     *
     * \details JobName for which the event is valid */
    @Test
    public void testscheduler_event_job() {  // SOSOptionString
        objOptions.scheduler_event_job.Value("++----++");
        assertEquals("", objOptions.scheduler_event_job.Value(), "++----++");

    }

    /** \brief testsupervisor_job_chain :
     *
     * \details Jobchain for processing events in the supervisor */
    @Test
    public void testsupervisor_job_chain() {  // SOSOptionCommandString
        objOptions.supervisor_job_chain.Value("++scheduler_event_service++");
        assertEquals("", objOptions.supervisor_job_chain.Value(), "++scheduler_event_service++");

    }

} // public class JSEventsClientOptionsJUnitTest