

package com.sos.jitl.md5;

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

/**
 * \class 		JobSchedulerMD5FileOptionsJUnitTest - title
 *
 * \brief 
 *
 */

 
public class JobSchedulerMD5FileOptionsJUnitTest extends  JSToolBox {
	private final String					conClassName						= "JobSchedulerMD5FileOptionsJUnitTest"; //$NON-NLS-1$
		@SuppressWarnings("unused") //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerMD5FileOptionsJUnitTest.class);
	private JobSchedulerMD5File objE = null;

	protected JobSchedulerMD5FileOptions	objOptions			= null;

	public JobSchedulerMD5FileOptionsJUnitTest() {
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
		objE = new JobSchedulerMD5File();
		objE.registerMessageListener(this);
		objOptions = objE.Options();
		objOptions.registerMessageListener(this);
		
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@After
	public void tearDown() throws Exception {
	}


		

/**
 * \brief testfile : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testfile() {  // SOSOptionString
    	 objOptions.file.Value("++----++");
    	 assertEquals ("", objOptions.file.Value(),"++----++");
    	
    }

                

/**
 * \brief testmd5_suffix : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testmd5_suffix() {  // SOSOptionString
    	 objOptions.md5_suffix.Value("++----++");
    	 assertEquals ("", objOptions.md5_suffix.Value(),"++----++");
    	
    }

                

/**
 * \brief testmode : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testmode() {  // SOSOptionString
    	 objOptions.mode.Value("++----++");
    	 assertEquals ("", objOptions.mode.Value(),"++----++");
    	
    }

                

/**
 * \brief testresult : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testresult() {  // SOSOptionString
    	 objOptions.result.Value("++----++");
    	 assertEquals ("", objOptions.result.Value(),"++----++");
    	
    }

                
        
} // public class JobSchedulerMD5FileOptionsJUnitTest