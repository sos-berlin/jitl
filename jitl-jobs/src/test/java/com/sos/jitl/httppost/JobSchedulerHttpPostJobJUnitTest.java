package com.sos.jitl.httppost;

import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

public class JobSchedulerHttpPostJobJUnitTest extends JSToolBox {

    protected JobSchedulerHttpPostJobOptions objOptions = null;
    private JobSchedulerHttpPostJob objE = null;

    public JobSchedulerHttpPostJobJUnitTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        objE = new JobSchedulerHttpPostJob();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @Test
    public void testExecute() throws Exception {
        objE.Execute();
    }

}