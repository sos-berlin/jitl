package com.sos.jitl.jobstreams.db;

public class FilterJobStreams {

    private Long jobStreamId;
    private String schedulerId;
    private String jobStream;
    private String folder;
    private String status;

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

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    
    public Long getJobStreamId() {
        return jobStreamId;
    }

    
    public void setJobStreamId(Long jobStreamId) {
        this.jobStreamId = jobStreamId;
    }

    
    public String getJobStream() {
        return jobStream;
    }

    
    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

   

}
