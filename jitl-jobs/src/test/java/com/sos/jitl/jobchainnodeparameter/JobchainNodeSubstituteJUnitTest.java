package com.sos.jitl.jobchainnodeparameter;

import static org.junit.Assert.assertEquals;

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

    protected JobchainNodeSubstituteOptions objOptions = null;
    private JobchainNodeSubstitute objE = null;

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
        objE = new JobchainNodeSubstitute();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;

    }

    @After
    public void tearDown() throws Exception {
        // Implement Method here
    }

    @Test
    public void testExecute() throws Exception {

        // Implement Method here

    }
}