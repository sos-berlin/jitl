package com.sos.jitl.splitter;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/** \class JobChainSplitterOptionsJUnitTest - Start a parallel processing in a
 * jobchain
 *
 * \brief
 *
 *
 * 
 *
 * see \see
 * C:\Users\KB\AppData\Local\Temp\scheduler_editor-121986169113382203.html for
 * (more) details.
 * 
 * \verbatim ; mechanicaly created by
 * C:\ProgramData\sos-berlin.com\jobscheduler\
 * latestscheduler_4446\config\JOETemplates
 * \java\xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com
 * at 20130315155436 \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim private HashMap <String, String> SetJobSchedulerSSHJobOptions
 * (HashMap <String, String> pobjHM) { pobjHM.put
 * ("		JobChainSplitterOptionsJUnitTest.auth_file", "test"); // This parameter
 * specifies the path and name of a user's pr return pobjHM; } // private void
 * SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) \endverbatim */
public class JobChainSplitterOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "JobChainSplitterOptionsJUnitTest"; //$NON-NLS-1$
    @SuppressWarnings("unused")//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JobChainSplitterOptionsJUnitTest.class);
    private JobChainSplitter objE = null;

    protected JobChainSplitterOptions objOptions = null;

    public JobChainSplitterOptionsJUnitTest() {
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
        objE = new JobChainSplitter();
        objE.registerMessageListener(this);
        objOptions = objE.Options();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    /** \brief testnext_state_name :
     * 
     * \details */
    @Test
    public void testnext_state_name() {  // SOSOptionString
        objOptions.next_state_name.Value("++----++");
        assertEquals("", objOptions.next_state_name.Value(), "++----++");

    }

    /** \brief teststate_names :
     * 
     * \details */
    @Test
    public void teststate_names() {  // SOSOptionString
        objOptions.StateNames.Value("++----++");
        assertEquals("", objOptions.StateNames.Value(), "++----++");

    }

    /** \brief testsync_state_name :
     * 
     * \details */
    @Test
    public void testsync_state_name() {  // SOSOptionString
        objOptions.SyncStateName.Value("++----++");
        assertEquals("", objOptions.SyncStateName.Value(), "++----++");

    }

} // public class JobChainSplitterOptionsJUnitTest