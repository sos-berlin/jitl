package com.sos.jitl.housekeeping.dequeuemail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

public class JobSchedulerDequeueMailJobJUnitTest extends JSToolBox {

    @SuppressWarnings("unused")//$NON-NLS-1$
    private final static String conClassName = "JobSchedulerDequeueMailJobJUnitTest"; //$NON-NLS-1$

    protected JobSchedulerDequeueMailJobOptions objOptions = null;
    private JobSchedulerDequeueMailJob objE = null;

    public JobSchedulerDequeueMailJobJUnitTest() {
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
        objE = new JobSchedulerDequeueMailJob();
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

        objOptions.smtpHost.setValue("mail.sos-berlin.com");
        objOptions.queueDirectory.setValue("C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_current/mail");
        objOptions.emailFileName.setValue("a file name in the mail folder");
        objOptions.iniPath.setValue("C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_current/config/factory.ini");

        objE.execute();

        //		assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
        //		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$

    }
    
    @Test
    public void testExecuteResend() throws Exception {

        objOptions.emailFileName.setValue("");
        objOptions.queueDirectory.setValue("C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_current/mail");
        objOptions.iniPath.setValue("C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_current/config/factory.ini");

        objE.execute();

        //      assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
        //      assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$

    }

}  // class JobSchedulerDequeueMailJobJUnitTest