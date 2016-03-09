package com.sos.jitl.housekeeping.cleanup;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.jitl.housekeeping.cleanupdb.JobSchedulerCleanupSchedulerDb;
import com.sos.jitl.housekeeping.cleanupdb.JobSchedulerCleanupSchedulerDbOptions;
import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/** \class JobSchedulerCleanupSchedulerDbOptionsJUnitTest - Delete log entries in
 * the Job Scheduler history Databaser tables
 *
 * \brief
 *
 *
 * 
 *
 * see \see C:\Dokumente und Einstellungen\Uwe Risse\Lokale
 * Einstellungen\Temp\scheduler_editor-3271913404894833399.html for (more)
 * details.
 * 
 * \verbatim ; mechanicaly created by C:\Dokumente und Einstellungen\Uwe
 * Risse\Eigene Dateien\sos-berlin.com\jobscheduler\scheduler_ur_current\config\
 * JOETemplates\java\xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from
 * http://www.sos-berlin.com at 20121211160841 \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim private HashMap <String, String> SetJobSchedulerSSHJobOptions
 * (HashMap <String, String> pobjHM) { pobjHM.put
 * ("		JobSchedulerCleanupSchedulerDbOptionsJUnitTest.auth_file", "test"); //
 * This parameter specifies the path and name of a user's pr return pobjHM; } //
 * private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim */
public class JobSchedulerCleanupSchedulerDbOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "JobSchedulerCleanupSchedulerDbOptionsJUnitTest"; //$NON-NLS-1$
    @SuppressWarnings("unused")//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JobSchedulerCleanupSchedulerDbOptionsJUnitTest.class);
    private JobSchedulerCleanupSchedulerDb objE = null;

    protected JobSchedulerCleanupSchedulerDbOptions objOptions = null;

    public JobSchedulerCleanupSchedulerDbOptionsJUnitTest() {
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
        objE = new JobSchedulerCleanupSchedulerDb();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    /** \brief testdelete_daily_plan_interval :
     * 
     * \details Items in the table DAYS_SCHEDULE which are older than the given
     * number of days will be deleted. */
    @Test
    public void testdelete_daily_plan_interval() {  // SOSOptionString
        objOptions.delete_daily_plan_interval.Value("++0++");
        assertEquals("", objOptions.delete_daily_plan_interval.Value(), "++0++");

    }

    /** \brief testdelete_ftp_history_interval :
     * 
     * \details Items in the tables JADE_FILES and JADE_FILES_HISTORY which are
     * older than the given number of days will be deleted. */
    @Test
    public void testdelete_ftp_history_interval() {  // SOSOptionString
        objOptions.delete_jade_history_interval.Value("++0++");
        assertEquals("", objOptions.delete_jade_history_interval.Value(), "++0++");

    }

    /** \brief testdelete_history_interval :
     * 
     * \details Items in the tables SCHEDULER_HISTORY and
     * SCHEDULER_ORDER_HISTORY which are older than the given number of days
     * will be deleted. */
    @Test
    public void testdelete_history_interval() {  // SOSOptionString
        objOptions.delete_history_interval.Value("++0++");
        assertEquals("", objOptions.delete_history_interval.Value(), "++0++");

    }

    /** \brief testdelete_interval :
     * 
     * \details This parameter will be used if a table specific parameter is
     * missing. */
    @Test
    public void testdelete_interval() {  // SOSOptionString
        objOptions.delete_interval.Value("++0++");
        assertEquals("", objOptions.delete_interval.Value(), "++0++");

    }

    /** \brief testscheduler_id :
     * 
     * \details */
    @Test
    public void testscheduler_id() {  // SOSOptionString
        objOptions.scheduler_id.Value("++----++");
        assertEquals("", objOptions.scheduler_id.Value(), "++----++");

    }

} // public class JobSchedulerCleanupSchedulerDbOptionsJUnitTest