package com.sos.jitl.jobstreams.db;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sos.jitl.jobstreams.Constants;

@Entity
@Table(name = Constants.JOB_STREAM_TASK_CONTEXT_TABLE)
@SequenceGenerator(name = Constants.JOB_STREAM_TASK_CONTEXT_TABLE_SEQUENCE, sequenceName = Constants.JOB_STREAM_TASK_CONTEXT_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemJobStreamTaskContext {

    private Long id;
    private Long taskId;
    private String schedulerId;
    private String jobStreamHistoryId;
    private String job;
    private String jobStream;
    private Date created;

    public DBItemJobStreamTaskContext() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = Constants.JOB_STREAM_TASK_CONTEXT_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Id
    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    @Column(name = "[JOBSTREAM_HISTORY_ID]", nullable = false)
    public String getJobStreamHistoryId() {
        return jobStreamHistoryId;
    }

    public void setJobStreamHistoryId(String jobStreamHistoryId) {
        this.jobStreamHistoryId = jobStreamHistoryId;
    }

    @Column(name = "[JOB]", nullable = false)
    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    @Column(name = "[JOB_STREAM]", nullable = false)
    public String getJobStream() {
        return jobStream;
    }

    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    @Column(name = "[TASK_ID]", nullable = true)
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
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