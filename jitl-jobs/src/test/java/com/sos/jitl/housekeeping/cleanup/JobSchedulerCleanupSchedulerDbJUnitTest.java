package com.sos.jitl.housekeeping.cleanup;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.jitl.housekeeping.cleanupdb.JobSchedulerCleanupSchedulerDb;
import com.sos.jitl.housekeeping.cleanupdb.JobSchedulerCleanupSchedulerDbOptions;


public class JobSchedulerCleanupSchedulerDbJUnitTest extends JSToolBox {

    @SuppressWarnings("unused")//$NON-NLS-1$
    private final static String conClassName = "JobSchedulerCleanupSchedulerDbJUnitTest"; //$NON-NLS-1$

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

        objOptions.hibernate_configuration_file_scheduler.setValue(configurationFilename);
        objOptions.hibernate_configuration_file_reporting.setValue(configurationFilename);
        objOptions.delete_interval.setValue("10");
        objOptions.cleanup_daily_plan_execute.setValue("true");
        objOptions.cleanup_jade_history_execute.setValue("true");
        objOptions.cleanup_job_scheduler_history_execute.setValue("true");
        objE.Execute();

        //		assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
        //		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$

    }
}  // class JobSchedulerCleanupSchedulerDbJUnitTest