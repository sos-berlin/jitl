package com.sos.jitl.jobstreams.db;


public class FilterInConditionCommands {
    private Long inConditionId;
    private String command;
    private String jobStream;
    private String folder;
    private String commandParam;
    private String job;
    
    public Long getInConditionId() {
        return inConditionId;
    }
    
    public void setInConditionId(Long inConditionId) {
        this.inConditionId = inConditionId;
    }
    
    public String getCommand() {
        return command;
    }
    
    public void setCommand(String command) {
        this.command = command;
    }
    
    public String getCommandParam() {
        return commandParam;
    }
    
    public void setCommandParam(String commandParam) {
        this.commandParam = commandParam;
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
    
}
