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
@Table(name = Constants.JOB_STREAM_STARTER_TABLE)
@SequenceGenerator(name = Constants.JOB_STREAM_STARTER_TABLE_SEQUENCE, sequenceName = Constants.JOB_STREAM_STARTER_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemJobStreamStarter {

    private Long id;
    private Long jobStream;
    private String title;
    private String state;
    private String runTime;
    private Date nextStart;
    private Date created;

    public DBItemJobStreamStarter() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = Constants.JOB_STREAM_STARTER_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Id
    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }


    @Column(name = "[STATE]", nullable = false)
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }    
   
    @Column(name = "[TITLE]", nullable = false)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }    
   
   
    @Column(name = "[JOBSTREAM]", nullable = false)
    public Long getJobStream() {
        return jobStream;
    }

    public void setJobStream(Long jobStream) {
        this.jobStream  = jobStream ;
    }

    @Column(name = "[RUN_TIME]", nullable = true)
    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime ;
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
 
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[NEXT_START]", nullable = false)
    public Date getNextStart() {
        return nextStart;
    }

    @Column(name = "[NEXT_START]", nullable = false)
    public void setNextStart(Date nextStart) {
        this.nextStart = nextStart;
    }
   
}