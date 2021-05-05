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
@Table(name = Constants.JOB_STREAM_PARAMETER_TABLE)
@SequenceGenerator(name = Constants.JOB_STREAM_PARAMETER_TABLE_SEQUENCE, sequenceName = Constants.JOB_STREAM_PARAMETER_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemJobStreamParameter {

    private Long id;
    private Long jobStreamStarter;
    private String name;
    private String value;
    private Date created;

    public DBItemJobStreamParameter() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = Constants.JOB_STREAM_PARAMETER_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Id
    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "[NAME]", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "[VALUE]", nullable = false)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Column(name = "[JOBSTREAM_STARTER]", nullable = false)
    public Long getJobStreamStarter() {
        return jobStreamStarter;
    }

    public void setJobStreamStarter(Long jobStreamStarter) {
        this.jobStreamStarter = jobStreamStarter;
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