package com.sos.jitl.checkrunhistory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

// sp 10.06.14 Test hängt im Jenkins build, lokal gibt es eine
// SocketTimeoutException! [SP]
@Ignore("Test set to Ignore for later examination")
public class JobSchedulerCheckRunHistoryJUnitTest extends JSToolBox {

    @SuppressWarnings("unused")
    private final static String conClassName = "JobSchedulerCheckRunHistoryJUnitTest";						//$NON-NLS-1$
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
        objOptions.message.setValue("[JOB_NAME] is too late!");
        objOptions.start_time.setValue("0:03:00:00");
        objOptions.query.setValue("isCompleteddBeforeWithError (15:20:30)");
        objOptions.jobName.setValue("job1");
        objOptions.schedulerPort.value(4197);
        objOptions.schedulerHostName.setValue("localhost");
        jobSchedulerCheckRunHistory.Execute();

    }
} // class JobSchedulerCheckRunHistoryJUnitTest