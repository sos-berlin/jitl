package com.sos.jitl.jobstreams.db;

public class FilterConsumedInConditions {

    private Long inConditionId;
    private String jobSchedulerId="";
    private String jobStream="";
    private String folder="";
    private String job="";
    private String session;
    private String join;

    public Long getInConditionId() {
        return inConditionId;
    }

    public void setInConditionId(Long inConditionId) {
        this.inConditionId = inConditionId;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getJobStream() {
        return jobStream;
    }

    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    
    public String getJob() {
        return job;
    }

    
    public void setJob(String job) {
        this.job = job;
    }

    
    public String getJobSchedulerId() {
        return jobSchedulerId;
    }

    
    public void setJobSchedulerId(String jobSchedulerId) {
        this.jobSchedulerId = jobSchedulerId;
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
