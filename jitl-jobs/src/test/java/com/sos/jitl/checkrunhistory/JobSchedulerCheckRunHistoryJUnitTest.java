package com.sos.jitl.checkrunhistory;

import static org.junit.Assert.assertEquals;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

import org.apache.log4j.Logger;
import org.junit.*;

// sp 10.06.14 Test hängt im Jenkins build, lokal gibt es eine
// SocketTimeoutException! [SP]
@Ignore("Test set to Ignore for later examination")
public class JobSchedulerCheckRunHistoryJUnitTest extends JSToolBox {

    @SuppressWarnings("unused")
    private final static String conClassName = "JobSchedulerCheckRunHistoryJUnitTest";						//$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerCheckRunHistoryJUnitTest.class);
    protected JobSchedulerCheckRunHistoryOptions objOptions = null;
    private JobSchedulerCheckRunHistory jobSchedulerCheckRunHistory = null;

    public JobSchedulerCheckRunHistoryJUnitTest() {
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
        jobSchedulerCheckRunHistory = new JobSchedulerCheckRunHistory();
        jobSchedulerCheckRunHistory.registerMessageListener(this);
        objOptions = jobSchedulerCheckRunHistory.options();
        objOptions.registerMessageListener(this);
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testExecute() throws Exception {
        objOptions.message.Value("[JOB_NAME] is too late!");
        objOptions.start_time.Value("0:03:00:00");
        objOptions.query.Value("isCompleteddBeforeWithError (15:20:30)");
        objOptions.jobName.Value("job1");
        objOptions.schedulerPort.value(4197);
        objOptions.schedulerHostName.Value("localhost");
        jobSchedulerCheckRunHistory.Execute();

    }
} // class JobSchedulerCheckRunHistoryJUnitTest