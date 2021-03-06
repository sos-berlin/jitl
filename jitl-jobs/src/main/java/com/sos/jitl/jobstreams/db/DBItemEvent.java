package com.sos.jitl.jobstreams.db;

import java.util.Date;
import javax.persistence.*;
import org.hibernate.annotations.Type;
import com.sos.jitl.jobstreams.Constants;

@Entity
@Table(name = Constants.EVENTS_TABLE)
@SequenceGenerator(name = Constants.EVENTS_TABLE_SEQUENCE, sequenceName = Constants.EVENTS_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemEvent {

    private Long id;
    private Long outConditionId;
    private Long jobStreamHistoryId;
    private String session;
    private String event;
    private String jobStream;
    private Boolean globalEvent;
    private Date created;

    public DBItemEvent() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = Constants.EVENTS_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Id
    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "[SESSION]", nullable = false)
    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    @Column(name = "[EVENT]", nullable = false)
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Column(name = "[JOBSTREAM]", nullable = false)
    public String getJobStream() {
        return jobStream;
    }

    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    @Column(name = "[OUT_CONDITION_ID]", nullable = false)
    public Long getOutConditionId() {
        return outConditionId;
    }

    public void setOutConditionId(Long outConditionId) {
        this.outConditionId = outConditionId;
    }

    @Column(name = "[JOBSTREAM_HISTORY_ID]", nullable = false)
    public Long getJobStreamHistoryId() {
        return jobStreamHistoryId;
    }

    public void setJobStreamHistoryId(Long jobStreamHistoryId) {
        this.jobStreamHistoryId = jobStreamHistoryId;
    }

    @Column(name = "[GLOBAL_EVENT]", nullable = false)
    @Type(type = "numeric_boolean")
    public Boolean getGlobalEvent() {
        return this.globalEvent;
    }

    public void setGlobalEvent(Boolean globalEvent) {
        this.globalEvent = globalEvent;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public Date getCreated() {
        return created;
    }

    @Column(name = "[CREATED]", nullable = false)
    public void setCreated(Date created) {
        this.created = created;
    }

}