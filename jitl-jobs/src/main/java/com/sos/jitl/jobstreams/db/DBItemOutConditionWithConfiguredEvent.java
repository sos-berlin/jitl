package com.sos.jitl.jobstreams.db;

import java.util.Date;

import com.sos.jitl.jobstreams.interfaces.IJSJobConditionKey;

public class DBItemOutConditionWithConfiguredEvent implements IJSJobConditionKey {

    private Long outId;
    private String jobSchedulerId;
    private String job;
    private String expression;
    private String jobStream;
    private String folder;
    private Date created;

    private Long oEventId;
    private Long outConditionId;
    private String event;
    private String command;
    private Boolean globalEvent;

    public DBItemOutConditionWithConfiguredEvent() {
    }

    public Long getOutId() {
        return outId;
    }

    public void setOutId(Long outId) {
        this.outId = outId;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Long getoEventId() {
        return oEventId;
    }

    public void setoEventId(Long oEventId) {
        this.oEventId = oEventId;
    }

    public Long getOutConditionId() {
        return outConditionId;
    }

    public void setOutConditionId(Long outConditionId) {
        this.outConditionId = outConditionId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Boolean getGlobalEvent() {
        return globalEvent;
    }

    public void setGlobalEvent(Boolean globalEvent) {
        this.globalEvent = globalEvent;
    }

    public String getJobSchedulerId() {
        return jobSchedulerId;
    }

    public void setJobSchedulerId(String jobSchedulerId) {
        this.jobSchedulerId = jobSchedulerId;
    }

}