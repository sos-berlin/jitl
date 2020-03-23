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
@Table(name = Constants.JOB_STREAM_HISTORY_TABLE)
@SequenceGenerator(name = Constants.JOB_STREAM_HISTORY_TABLE_SEQUENCE, sequenceName = Constants.JOB_STREAM_HISTORY_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemJobStreamHistory {

    private Long id;
    private Long jobStreamStarter;
    private Date started;
    private Date ended;
    private Date created;

    public DBItemJobStreamHistory() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = Constants.JOB_STREAM_HISTORY_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Id
    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }

    

    @Column(name = "[JOBSTREAM_STARTER]", nullable = true)
    public Long getJobStreamStarter() {
        return jobStreamStarter;
    }

    public void setJobStreamStarter(Long jobStreamStarter) {
        this.jobStreamStarter = jobStreamStarter;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[STARTED]", nullable = false)
    public Date getStarted() {
        return started;
    }

    @Column(name = "[STARTED]", nullable = false)
    public void setStarted(Date started) {
        this.started = started;
    }
 
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ENDED]", nullable = false)
    public Date getEnded() {
        return ended;
    }

    @Column(name = "[ENDED]", nullable = false)
    public void setEnded(Date ended) {
        this.ended = ended;
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