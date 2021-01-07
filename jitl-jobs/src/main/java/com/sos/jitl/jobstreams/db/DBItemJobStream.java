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
@Table(name = Constants.JOB_STREAM_TABLE)
@SequenceGenerator(name = Constants.JOB_STREAM_TABLE_SEQUENCE, sequenceName = Constants.JOB_STREAM_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemJobStream {

    private Long id;
    private String schedulerId;
    private String jobStream;
    private String folder;
    private String state;
    private String version="1.13.8";
    private Date created;

    public DBItemJobStream() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = Constants.JOB_STREAM_TABLE_SEQUENCE)
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


    @Column(name = "[STATE]", nullable = false)
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Column(name = "[FOLDER]", nullable = false)
    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    @Column(name = "[JOBSTREAM]", nullable = false)
    public String getJobStream() {
        return jobStream;
    }

    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    @Column(name = "[VERSION]", nullable = false)
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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