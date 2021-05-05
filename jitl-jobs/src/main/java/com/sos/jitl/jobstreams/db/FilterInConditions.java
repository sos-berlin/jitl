package com.sos.jitl.jobstreams.db;

public class FilterInConditions {

    private String jobSchedulerId;
    private String job;
    private String jobStream;
    private String folder;
    private String join;

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

    
    public String getFolder() {
        return folder;
    }

    
    public void setFolder(String folder) {
        this.folder = folder;
    }

    
    public String getJoin() {
        return join;
    }

    
    public void setJoin(String join) {
        this.join = join;
    }

}
