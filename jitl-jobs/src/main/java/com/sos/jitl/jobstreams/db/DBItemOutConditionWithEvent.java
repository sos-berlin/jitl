package com.sos.jitl.jobstreams.db;

import java.util.Date;

public class DBItemOutConditionWithEvent {

    private String jobSchedulerId;
    private String job;
    private String jobStream;

    private Long eventId;
    private Long outConditionId;
    private Long jobStreamHistoryId;
    private String session;
    private String event;
    private Boolean globalEvent;
    private Date created;

    public DBItemOutConditionWithEvent() {

    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
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

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Long getOutConditionId() {
        return outConditionId;
    }

    public void setOutConditionId(Long outConditionId) {
        this.outConditionId = outConditionId;
    }

    public Long getJobStreamHistoryId() {
        return jobStreamHistoryId;
    }

    public void setJobStreamHistoryId(Long jobStreamHistoryId) {
        this.jobStreamHistoryId = jobStreamHistoryId;
    }

    public Boolean getGlobalEvent() {
        return this.globalEvent;
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

}