package com.sos.jitl.jobstreams.db;

import java.util.HashSet;
import java.util.Set;

import com.sos.jitl.jobstreams.classes.JSEventKey;

 
public class FilterOutConditions {

    private String jobSchedulerId;
    private String job;
    private String jobStream;
    private String folder;
    private Set<JSEventKey> events;

    public String getJobSchedulerId() {
        return jobSchedulerId;
    }

    public void setJobSchedulerId(String jobSchedulerId) {
        this.jobSchedulerId = jobSchedulerId;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getJobStream() {
        return jobStream;
    }

    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    public void addEvent(JSEventKey event) {
        if (events == null) {
            events = new HashSet<JSEventKey>();
        }
        event.setSession("");
        events.add(event);
    }

    public Set<JSEventKey> getListOfEvents() {
        return events;
    }

    
    public String getFolder() {
        return folder;
    }

    
    public void setFolder(String folder) {
        this.folder = folder;
    }
}
