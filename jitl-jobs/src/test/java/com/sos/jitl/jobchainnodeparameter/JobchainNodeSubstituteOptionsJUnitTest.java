

package com.sos.jitl.jobchainnodeparameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.jitl.jobchainnodeparameter.monitor.JobchainNodeSubstitute;
import com.sos.jitl.jobchainnodeparameter.monitor.JobchainNodeSubstituteOptions;

 

 
public class JobchainNodeSubstituteOptionsJUnitTest extends  JSToolBox {
	private final String CLASSNAME = "ConfigurationMonitorOptionsJUnitTest"; 
		@SuppressWarnings("unused") 
	private final static Logger LOGGER = Logger.getLogger(JobchainNodeSubstituteOptionsJUnitTest.class);
	private JobchainNodeSubstitute objE = null;

	protected JobchainNodeSubstituteOptions	objOptions			= null;

	public JobchainNodeSubstituteOptionsJUnitTest() {
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
		objE = new JobchainNodeSubstitute();
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
    public void testconfigurationMonitor_configuration_file() {  // SOSOptionString
    	 objOptions.configurationMonitor_configuration_file.Value("++----++");
    	 assertEquals ("The default value is the name of the job chain of the actual running o", objOptions.configurationMonitor_configuration_file.Value(),"++----++");
    	
    }
 
                
 
    @Test
    public void testconfigurationMonitor_configuration_path() {  // SOSOptionString
    	 objOptions.configurationMonitor_configuration_path.Value("++----++");
    	 assertEquals ("The default value is the directory that contains the job chain definit", objOptions.configurationMonitor_configuration_path.Value(),"++----++");
    	
    }
 
                
        
} // public class ConfigurationMonitorOptionsJUnitTest