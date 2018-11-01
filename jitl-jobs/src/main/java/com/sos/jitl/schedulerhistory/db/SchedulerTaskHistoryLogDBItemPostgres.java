package com.sos.jitl.schedulerhistory.db;

import java.io.IOException;
import java.nio.file.Path;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.schedulerhistory.classes.SOSStreamUnzip;

@Entity
@Table(name = "SCHEDULER_HISTORY")
public class SchedulerTaskHistoryLogDBItemPostgres extends DbItem {

    private Long id;
    private String spoolerId;
    private String jobName;
    private String clusterMemberId;
    private byte[] log;

    public SchedulerTaskHistoryLogDBItemPostgres() {
     }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(name = "[SPOOLER_ID]", nullable = false)
    public String getSpoolerId() {
        return spoolerId;
    }

    @Column(name = "[SPOOLER_ID]", nullable = false)
    public void setSpoolerId(final String spoolerId) {
        this.spoolerId = spoolerId;
    }

    @Column(name = "[JOB_NAME]", nullable = false)
    public String getJobName() {
        return jobName;
    }

    @Column(name = "[JOB_NAME]", nullable = false)
    public void setJobName(final String jobName) {
        this.jobName = jobName;
    }
    
    @Column(name = "[CLUSTER_MEMBER_ID]", nullable = true)
    public String getClusterMemberId() {
        return clusterMemberId;
    }

    @Column(name = "[CLUSTER_MEMBER_ID]", nullable = true)
    public void setClusterMemberId(String clusterMemberId) {
        this.clusterMemberId = clusterMemberId;
    }

    @Column(name = "[LOG]", nullable = true)
    public byte[] getLog() {
        return log;
    }

    @Column(name = "[LOG]", nullable = true)
    public void setLog(final byte[] log) {
        this.log = log;
    }
    
    @Transient
    public String getLogAsString() throws IOException {
        if (log == null) {
            return null;
        } else {
            return SOSStreamUnzip.unzip2String(log);
        }
    }
    
    @Transient
    public byte[] getLogAsByteArray() throws IOException {
        if (log == null) {
            return null;
        } else {
            return SOSStreamUnzip.unzip(log);
        }
    }
    
    @Transient
    public Path writeLogFile(String prefix) throws IOException {
        if (log == null) {
            return null;
        } else {
            return SOSStreamUnzip.unzipToFile(log, prefix);
        }
    }
    
    @Transient
    public Path writeGzipLogFile(String prefix) throws IOException {
        if (log == null) {
            return null;
        } else {
            return SOSStreamUnzip.zippedToFile(log, prefix);
        }
    }
    
    @Transient
    public String getSchedulerId() {
        return this.getSpoolerId();
    }
}