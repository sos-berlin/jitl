package com.sos.jitl.restclient;

import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.junit.Test;

public class TestJobSchedulerRestClient {

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

    @Test
    public void testRestService() throws Exception {
        JobSchedulerRestClient.accept = "application/xml";
        JobSchedulerRestClient.headers.put("content-type", "application/xml");
        String body = "<show_state subsystems=\"folder process_class\" what=\"folders cluster\" />";
        URL url = new URL("http://localhost:4404/jobscheduler/master/api/command");
        String s = JobSchedulerRestClient.executeRestServiceCommand("get", url, body);
        assertNotNull(s);
    }

}
