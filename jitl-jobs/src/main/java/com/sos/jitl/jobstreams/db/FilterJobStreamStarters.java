package com.sos.jitl.jobstreams.db;

public class FilterJobStreamStarters {

    private Long id;
    private Long jobStreamId;
    private String jobStream;
    private String status;
    private String title;
    

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

    
    public Long getId() {
        return id;
    }

    
    public void setId(Long id) {
        this.id = id;
    }

    
    public Long getJobStreamId() {
        return jobStreamId;
    }

    
    public void setJobStreamId(Long jobStreamId) {
        this.jobStreamId = jobStreamId;
    }

    
    public String getTitle() {
        return title;
    }

    
    public void setTitle(String title) {
        this.title = title;
    }

 

}
