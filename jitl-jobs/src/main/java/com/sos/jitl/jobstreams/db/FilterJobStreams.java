package com.sos.jitl.jobstreams.db;

public class FilterJobStreams {

    private String jobStream;
    private String schedulerId;
    private String status;
    
    public String getJobStream() {
        return jobStream;
    }
    
    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    
    public String getSchedulerId() {
        return schedulerId;
    }

    
    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    

}
