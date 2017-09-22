package com.sos.jitl.checkhistory.classes;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

}
