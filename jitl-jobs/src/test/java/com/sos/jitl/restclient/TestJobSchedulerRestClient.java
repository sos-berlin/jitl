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
        String body = "<show_state/>";
        URL url = new URL("http://localhost:4446/jobscheduler/master/api/command");
        String s = JobSchedulerRestClient.executeRestServiceCommand("post", url, body);
        assertNotNull(s);
    }

    @Test
    public void testRestServiceWithCommand() throws Exception {
        JobSchedulerRestClient.accept = "application/json";
        JobSchedulerRestClient.headers.put("Content-Type", "application/json");
        JobSchedulerRestClient.headers.put("Authorization","Basic dXI6YXBsc29z");
        
        URL url = new URL("http://localhost:4446/joc/api/security/login");
        String s = JobSchedulerRestClient.executeRestServiceCommand("post", url, "");
        
        String access_token = JobSchedulerRestClient.getResponseHeader("access_token");
        
        JobSchedulerRestClient.headers.put("access_token",access_token);
        JobSchedulerRestClient.headers.put("Content-Type", "application/xml");
        String body = "<jobscheduler_commands jobschedulerId=\"scheduler_joc_cockpit\"><start_job job=\"job2\" at=\"now\"><params><param name=\"command\" value=\"echo hello world\"/></params></start_job></jobscheduler_commands>";        
        url = new URL("http://localhost:4446/joc/api/jobscheduler/commands");
        s = JobSchedulerRestClient.executeRestServiceCommand("post", url, body);
        
        System.out.println(s);
        
        
        assertNotNull(s);
    }
}
