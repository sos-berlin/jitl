package com.sos.jitl.jobchainnodeparameter;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.jitl.jobchainnodeparameter.monitor.JobchainNodeSubstitute;
import com.sos.jitl.jobchainnodeparameter.monitor.JobchainNodeSubstituteOptions;

public class JobchainNodeSubstituteOptionsJUnitTest extends JSToolBox {

    protected JobchainNodeSubstituteOptions objOptions = null;
    private static final int DEBUG9 = 9;
    private static final String PARAMINITVALUE = "++----++";
    private JobchainNodeSubstitute objE = null;

    @Before
    public void setUp() throws Exception {
        objE = new JobchainNodeSubstitute();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = DEBUG9;
    }

    @Test
    public void testConfigurationMonitorConfigurationFile() {  
        objOptions.configurationMonitorConfigurationFile.setValue(PARAMINITVALUE);
        assertEquals("The default value is the name of the job chain of the actual running o", objOptions.configurationMonitorConfigurationFile.getValue(),
                PARAMINITVALUE);
    }

    @Test
    public void testConfigurationMonitorConfigurationPath() {
        objOptions.configurationMonitorConfigurationPath.setValue(PARAMINITVALUE);
        assertEquals("The default value is the directory that contains the job chain definit", objOptions.configurationMonitorConfigurationPath.getValue(),
                PARAMINITVALUE);
    }

}  