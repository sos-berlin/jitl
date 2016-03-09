package com.sos.jitl.housekeeping.cleanup;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.jitl.housekeeping.cleanupdb.JobSchedulerCleanupSchedulerDb;
import com.sos.jitl.housekeeping.cleanupdb.JobSchedulerCleanupSchedulerDbOptions;
import org.apache.log4j.Logger;
import org.junit.*;

/** \class JobSchedulerCleanupSchedulerDbJUnitTest - JUnit-Test for
 * "Delete log entries in the Job Scheduler history Databaser tables"
 *
 * \brief MainClass to launch JobSchedulerCleanupSchedulerDb as an executable
 * command-line program
 *
 * 
 *
 * see \see C:\Dokumente und Einstellungen\Uwe Risse\Lokale
 * Einstellungen\Temp\scheduler_editor-3271913404894833399.html for (more)
 * details.
 *
 * \verbatim ; mechanicaly created by C:\Dokumente und Einstellungen\Uwe
 * Risse\Eigene Dateien\sos-berlin.com\jobscheduler\scheduler_ur_current\config\
 * JOETemplates\java\xsl\JSJobDoc2JSJUnitClass.xsl from
 * http://www.sos-berlin.com at 20121211160841 \endverbatim */
public class JobSchedulerCleanupSchedulerDbJUnitTest extends JSToolBox {

    @SuppressWarnings("unused")//$NON-NLS-1$
    private final static String conClassName = "JobSchedulerCleanupSchedulerDbJUnitTest"; //$NON-NLS-1$
    @SuppressWarnings("unused")//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JobSchedulerCleanupSchedulerDbJUnitTest.class);

    protected JobSchedulerCleanupSchedulerDbOptions objOptions = null;
    private JobSchedulerCleanupSchedulerDb objE = null;
    protected String configurationFilename = "R:/nobackup/junittests/hibernate/hibernate_oracle.cfg.xml";

    public JobSchedulerCleanupSchedulerDbJUnitTest() {
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

    @Test
    public void testExecute() throws Exception {

        objOptions.hibernate_configuration_file.Value(configurationFilename);
        objOptions.delete_interval.Value("10");
        objOptions.cleanup_daily_plan_execute.Value("false");
        objOptions.cleanup_jade_history_execute.Value("false");
        objOptions.cleanup_job_scheduler_history_execute.Value("false");
        objE.Execute();

        //		assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
        //		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$

    }
}  // class JobSchedulerCleanupSchedulerDbJUnitTest