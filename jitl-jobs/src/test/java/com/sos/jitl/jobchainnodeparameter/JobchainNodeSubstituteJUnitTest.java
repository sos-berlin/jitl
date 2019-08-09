package com.sos.jitl.jobchainnodeparameter;

import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.jitl.jobchainnodeparameter.monitor.JobchainNodeSubstitute;
import com.sos.jitl.jobchainnodeparameter.monitor.JobchainNodeSubstituteOptions;

public class JobchainNodeSubstituteJUnitTest extends JSToolBox {

    protected JobchainNodeSubstituteOptions configurationMonitorOptions = null;
    private JobchainNodeSubstitute jobchainNodeSubstitute = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Implement Method here
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // Implement Method here
    }

    @Before
    public void setUp() throws Exception {
        jobchainNodeSubstitute = new JobchainNodeSubstitute();
        jobchainNodeSubstitute.registerMessageListener(this);
        configurationMonitorOptions = jobchainNodeSubstitute.getOptions();
        configurationMonitorOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;

    }

    @After
    public void tearDown() throws Exception {
        // Implement Method here
    }

    @Test
    public void testExecute() throws Exception {

            configurationMonitorOptions.setCurrentNodeName("100");
            jobchainNodeSubstitute.setOrderId("start_win");
            jobchainNodeSubstitute.setJobChainPath("subst/job_chain1_win");
            jobchainNodeSubstitute.setOrderParameters(new HashMap<String, String>());
            configurationMonitorOptions.configurationMonitorConfigurationPath.setValue("C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_joc_cockpit/config/live");

           jobchainNodeSubstitute.execute();
    }
}