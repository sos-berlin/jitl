package com.sos.jitl.jobstreams.db;

import com.sos.hibernate.classes.SOSHibernateFilter;

public class FilterJobStreamTaskContext extends SOSHibernateFilter {

    private Long id;
    private Long taskId;
    private String jobstreamHistoryId;
    private String jobStream;
    
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

    
    public String getJobStream() {
        return jobStream;
    }

    
    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }
    
    
    

    
    
}
