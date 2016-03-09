package com.sos.jitl.restclient;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestJobSchedulerRestClient {

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
    public void testgetRestService() throws Exception {
        JobSchedulerRestClient.accept = "application/json";
        JobSchedulerRestClient.headers.put("content-typ", "text/plain");
        String s = JobSchedulerRestClient.executeRestServiceCommand("post", "localhost:4999/jobscheduler/agent/api/");
        s = JobSchedulerRestClient.executeRestServiceCommand("get", "localhost:4999/jobscheduler/agent/api/");
        s = JobSchedulerRestClient.executeRestService("localhost:4999/jobscheduler/agent/api/");
        s = JobSchedulerRestClient.executeRestServiceCommand("put", "localhost:4999/jobscheduler/agent/api/");
        s = JobSchedulerRestClient.executeRestServiceCommand("delete", "localhost:4999/jobscheduler/agent/api/");
        s = JobSchedulerRestClient.executeRestServiceCommand("xxx", "localhost:4999/jobscheduler/agent/api/");

    }

}
