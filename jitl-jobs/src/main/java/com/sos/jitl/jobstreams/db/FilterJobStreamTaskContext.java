package com.sos.jitl.jobstreams.db;

public class FilterJobStreamTaskContext {

    private Long id;
    private Long taskId;
    private String jobstreamHistoryId;
    
    public Long getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    
    public String getJobstreamHistoryId() {
        return jobstreamHistoryId;
    }
    
    public void setJobstreamHistoryId(String jobstreamHistoryId) {
        this.jobstreamHistoryId = jobstreamHistoryId;
    }

    
    public Long getId() {
        return id;
    }

    
    public void setId(Long id) {
        this.id = id;
    }
    
    
    

    
    
}
