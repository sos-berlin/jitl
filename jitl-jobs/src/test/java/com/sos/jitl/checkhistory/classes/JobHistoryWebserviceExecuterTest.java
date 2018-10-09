package com.sos.jitl.checkhistory.classes;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.restclient.WebserviceCredentials;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.scheduler.model.answers.HistoryEntry;

public class JobHistoryWebserviceExecuterTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobHistoryWebserviceExecuterTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testExecute() throws Exception {

        HistoryWebserviceExecuter jobHistoryWebserviceExecuter = new HistoryWebserviceExecuter("http://localhost:4446/joc/api","root:root");
       
        jobHistoryWebserviceExecuter.login();
        jobHistoryWebserviceExecuter.setSchedulerId("scheduler_joc_cockpit");
        jobHistoryWebserviceExecuter.setTimeLimit("00:00:01..12:14:00");
        jobHistoryWebserviceExecuter.setJobName("/job1");
        HistoryEntry h = jobHistoryWebserviceExecuter.getLastCompletedSuccessfullJobHistoryEntry();
        LOGGER.info(h.getJobName());
        jobHistoryWebserviceExecuter.setTimeLimit("");
         h = jobHistoryWebserviceExecuter.getLastCompletedJobHistoryEntry();
        LOGGER.info(h.getJobName());
        jobHistoryWebserviceExecuter.setTimeLimit("-5.00:00:01..12:14:00");
         h = jobHistoryWebserviceExecuter.getLastCompletedWithErrorJobHistoryEntry();
        LOGGER.info(h.getJobName());
        jobHistoryWebserviceExecuter.setTimeLimit("-100.00:00:01..12:14:00");
         h = jobHistoryWebserviceExecuter.getLastRunningJobHistoryEntry();
        LOGGER.info(h.getJobName());
    }
    
    @Test
    public void testOrderExecute() throws Exception {
    	  WebserviceCredentials webserviceCredentials = new WebserviceCredentials();
          webserviceCredentials.setPassword("api");
          webserviceCredentials.setUser("api_user");
          webserviceCredentials.setSchedulerId("scheduler_joc_cockpit");

        HistoryWebserviceExecuter jobHistoryWebserviceExecuter = new HistoryWebserviceExecuter("http://localhost:4446/joc/api","root:r:oot");
       
        jobHistoryWebserviceExecuter.login();
        jobHistoryWebserviceExecuter.setSchedulerId("scheduler_joc_cockpit");
        jobHistoryWebserviceExecuter.setJobChainName("/check_history/job_chain3");
        OrderHistoryItem h = jobHistoryWebserviceExecuter.getJobChainOrderHistoryEntry(BigInteger.valueOf(16259));
        LOGGER.info(String.valueOf(h.getState().getSeverity()));
    }

}
