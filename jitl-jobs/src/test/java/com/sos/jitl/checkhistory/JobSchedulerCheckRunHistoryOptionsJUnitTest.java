package com.sos.jitl.checkhistory;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/** \class JobSchedulerCheckRunHistoryOptionsJUnitTest - Check the last job run
 *
 * \brief
 *
 *
 * 
 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\
 * JobSchedulerCheckRunHistory.xml for (more) details.
 * 
 * \verbatim ; mechanicaly created by
 * C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl
 * from http://www.sos-berlin.com at 20110225184502 \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim private HashMap <String, String> SetJobSchedulerSSHJobOptions
 * (HashMap <String, String> pobjHM) { pobjHM.put
 * ("		JobSchedulerCheckRunHistoryOptionsJUnitTest.auth_file", "test"); // This
 * parameter specifies the path and name of a user's pr return pobjHM; } //
 * private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim */
public class JobSchedulerCheckRunHistoryOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "JobSchedulerCheckRunHistoryOptionsJUnitTest"; //$NON-NLS-1$
    @SuppressWarnings("unused")//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JobSchedulerCheckRunHistoryOptionsJUnitTest.class);
    private JobSchedulerCheckHistory objE = null;

    protected JobSchedulerCheckHistoryOptions objOptions = null;

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
        objE = new JobSchedulerCheckHistory();
        objE.registerMessageListener(this);
        objOptions = objE.options();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    /** \brief testJobChainName : The name of a job chain.
     * 
     * \details The name of a job chain. */
    @Test
    public void testJobChainName() { // JSJobChainName
        objOptions.jobChainName.setValue("++----++");
        assertEquals("The name of a job chain.", objOptions.jobChainName.getValue(), "++----++");

    }

    /** \brief testJobName : The name of a job.
     * 
     * \details The name of a job. */
    @Test
    public void testJobName() { // JSJobName
        objOptions.jobName.setValue("++----++");
        assertEquals("The name of a job.", objOptions.jobName.getValue(), "++----++");

    }

    /** \brief testmail_bcc : Email blind carbon copy address of the recipient,
     * see ./c
     * 
     * \details Email blind carbon copy address of the recipient, see
     * ./config/factory.ini, log_mail_bcc. */
    @Test
    public void testmail_bcc() { // JSOptionMailOptions
        objOptions.mail_bcc.setValue("++----++");
        assertEquals("Email blind carbon copy address of the recipient, see ./c", objOptions.mail_bcc.getValue(), "++----++");

    }

    /** \brief testmail_cc : Email carbon copy address of the recipient, see
     * ./config/
     * 
     * \details Email carbon copy address of the recipient, see
     * ./config/factory.ini, log_mail_cc. */
    @Test
    public void testmail_cc() { // JSOptionMailOptions
        objOptions.mailCC.setValue("++----++");
        assertEquals("Email carbon copy address of the recipient, see ./config/", objOptions.mailCC.getValue(), "++----++");

    }

    /** \brief testmail_to : Email address of the recipient, see
     * ./config/factory.ini,
     * 
     * \details Email address of the recipient, see ./config/factory.ini,
     * log_mail_to. */
    @Test
    public void testmail_to() { // JSOptionMailOptions
        objOptions.mailTo.setValue("++----++");
        assertEquals("Email address of the recipient, see ./config/factory.ini,", objOptions.mailTo.getValue(), "++----++");

    }

    /** \brief testmessage : Text in the email subject and in the log.
     * 
     * \details Text in the email subject and in the log. ${JOBNAME} will be
     * substituted with the value of the parameter jobname. ${NOW} will be
     * substituted with the current time. */
    @Test
    public void testmessage() { // SOSOptionString
        objOptions.message.setValue("++----++");
        assertEquals("Text in the email subject and in the log.", objOptions.message.getValue(), "++----++");

    }

    /** \brief testoperation : Operation to be executed
     * 
     * \details */
    @Test
    public void testoperation() { // SOSOptionStringValueList
        objOptions.query.setValue("++late++");
        assertEquals("Operation to be executed", objOptions.query.getValue(), "++late++");

    }

    /** \brief testOrderId : The name or the identification of an order.
     * 
     * \details The name or the identification of an order. */
    @Test
    public void testOrderId() { // JSOrderId
        objOptions.orderId.setValue("++----++");
        assertEquals("The name or the identification of an order.", objOptions.orderId.getValue(), "++----++");

    }

    /** \brief testend_time : The start time from which the parametrisized job is
     * check
     * 
     * \details The end time from which the parametrisized job is checked
     * whether it has successfully run or not. The end time must be set in the
     * form [number of elapsed days],Time(HH:MM:SS), so that the default value
     * is last midnight. */
    @Test
    public void testend_time() { // SOSOptionString
        objOptions.end_time.setValue("++0,00:00:00++");
        assertEquals("The end time from which the parametrisized job is check", objOptions.end_time.getValue(), "++0,00:00:00++");

    }

    /** \brief teststart_time : The start time from which the parametrisized job
     * is check
     * 
     * \details The start time from which the parametrisized job is checked
     * whether it has successfully run or not. The start time must be set in the
     * form [number of elapsed days],Time(HH:MM:SS), so that the default value
     * is last midnight. */
    @Test
    public void teststart_time() { // SOSOptionString
        objOptions.start_time.setValue("++0,00:00:00++");
        assertEquals("The start time from which the parametrisized job is check", objOptions.start_time.getValue(), "++0,00:00:00++");

    }

    /** \brief failOnQueryResultFalse */
    @Test
    public void testfailOnQueryResultFalse() { // SOSOptionBoolean
        assertEquals("testfailOnQueryResultFalse", objOptions.failOnQueryResultFalse.value(), true);

    }

} // public class JobSchedulerCheckRunHistoryOptionsJUnitTest