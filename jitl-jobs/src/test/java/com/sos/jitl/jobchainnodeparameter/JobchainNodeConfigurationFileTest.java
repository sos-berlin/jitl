package com.sos.jitl.jobchainnodeparameter;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.jitl.agentbatchinstaller.JSUniversalAgentBatchInstallerProcessTemplate;
import com.sos.jitl.jobchainnodeparameter.JobchainNodeConfiguration;

public class JobchainNodeConfigurationFileTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetJobChainParameters() throws Exception {
        JobchainNodeConfiguration jobchainNodeConfigurationFile = new JobchainNodeConfiguration(new File("C:/development/products/jitl/jitl-jobs/src/test/resources/com/sos/jitl/configurationmonitor/job_chain.config.xml"));
        //JobchainNodeConfigurationFile jobchainNodeConfigurationFile = new JobchainNodeConfigurationFile();
                
        HashMap <String,String> schedulerParameters = new HashMap<String, String>();
        schedulerParameters.put("var1","wert von var1 ${test}");
        schedulerParameters.put("order_param2", "test2${node_param_2}");
        
       // jobchainNodeConfigurationFile.setJobChainNodeConfigurationFileName("jobchain.config.xml");
        jobchainNodeConfigurationFile.setListOfOrderParameters(schedulerParameters);
        
        jobchainNodeConfigurationFile.setOrderId("4711");
        jobchainNodeConfigurationFile.setOrderPayload("");
        jobchainNodeConfigurationFile.setLiveFolder("C:/development/products/jitl/jitl-jobs/src/test/resources/com/sos/jitl/configurationmonitor");
        jobchainNodeConfigurationFile.setLiveFolder("C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_current/config/live");
        jobchainNodeConfigurationFile.setJobChainPath("test/job_chain1");
        jobchainNodeConfigurationFile.setJobChainPath("job_chain1");
       // jobchainNodeConfigurationFile.substituteOrderParamters("100");
        jobchainNodeConfigurationFile.substituteOrderParamters("x100");
        
              
        String jobchainGlobalParameter =  jobchainNodeConfigurationFile.getJobchainGlobalParameterValue("global_param_1");
        String jobchainParameter = jobchainNodeConfigurationFile.getJobchainNodeParameterValue("node_param_2");
        String jobchainNodeParameter = jobchainNodeConfigurationFile.getJobchainNodeParameterValue("node_param_1");
        String orderParamValue = jobchainNodeConfigurationFile.getParam("order_param2");
        
        String x = jobchainNodeConfigurationFile.getParam("var1");

        assertEquals("testGetJobChainParameters", "global_param_1_val3", jobchainGlobalParameter); 
        assertEquals("testGetJobChainParameters", "node_param_2_${global_param_1}_val2", jobchainParameter); 
        assertEquals("testGetJobChainParameters", "node_param_1_val1", jobchainNodeParameter); 
        assertEquals("testGetJobChainParameters", "test2node_param_2_global_param_1_val3_val2", orderParamValue); 

    }
    
    
}
