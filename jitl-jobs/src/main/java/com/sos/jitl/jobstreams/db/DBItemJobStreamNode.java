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

import org.hibernate.annotations.Type;

import com.sos.jitl.jobstreams.Constants;

@Entity
@Table(name = Constants.JOB_STREAM_NODE_TABLE)
@SequenceGenerator(name = Constants.JOB_STREAM_NODE_TABLE_SEQUENCE, sequenceName = Constants.JOB_STREAM_NODE_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemJobStreamNode {

    private Long id;
    private Long jobStream;
    private String job;
    private Boolean skipOutCondition;
    private Date nextPeriod;
    private Date created;

    public DBItemJobStreamNode() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = Constants.JOB_STREAM_NODE_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Id
    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }


    @Column(name = "[JOB]", nullable = false)
    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    @Column(name = "[JOBSTREAM]", nullable = true)
    public Long getJobStream() {
        return jobStream;
    }

    public void setJobStream(Long jobStream) {
        this.jobStream = jobStream;
    }
    
    @Column(name = "[SKIP_NOT_IN_PERIOD_OUT_COND]", nullable = false)
    @Type(type = "numeric_boolean")
    public Boolean getSkipOutCondition() {
        return this.skipOutCondition;
    }

    public void setSkipOutCondition(Boolean skipOutCondition) {
        this.skipOutCondition = skipOutCondition;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[NEXT_PERIOD]", nullable = false)
    public Date getNextPeriod() {
        return nextPeriod;
    }

    @Column(name = "[NEXT_PERIOD]", nullable = false)
    public void setNextPeriod(Date nextPeriod) {
        this.nextPeriod = nextPeriod;
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