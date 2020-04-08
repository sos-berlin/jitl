package com.sos.jitl.jobstreams.db;

import java.util.Date;

public class FilterJobStreamHistory {

    private Long id;
    private Long jobStreamId;
    private Long jobStreamStarter;
    private String contextId;
    private String schedulerId;
    private Date startedFrom;
    private Date startedTo;
    private Boolean running;
    
    public Long getJobStreamStarter() {
        return jobStreamStarter;
    }
    
    public void setJobStreamStarter(Long jobStreamStarter) {
        this.jobStreamStarter = jobStreamStarter;
    }
       
    public String getContextId() {
        return contextId;
    }
    
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }
    
    public Boolean getRunning() {
        return running;
    }
    
    public void setRunning(Boolean running) {
        this.running = running;
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

    
    public Date getStartedFrom() {
        return startedFrom;
    }

    
    public void setStartedFrom(Date startedFrom) {
        this.startedFrom = startedFrom;
    }

    
    public Date getStartedTo() {
        return startedTo;
    }

    
    public void setStartedTo(Date startedTo) {
        this.startedTo = startedTo;
    }

    
    public String getSchedulerId() {
        return schedulerId;
    }

    
    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    
 
     
    
}
