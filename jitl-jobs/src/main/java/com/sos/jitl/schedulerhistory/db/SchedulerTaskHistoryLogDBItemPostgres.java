package com.sos.jitl.schedulerhistory.db;

import java.io.IOException;
import java.nio.file.Path;

import javax.persistence.*;

import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.schedulerhistory.classes.SOSStreamUnzip;

@Entity
@Table(name = "SCHEDULER_HISTORY")
public class SchedulerTaskHistoryLogDBItemPostgres extends DbItem {

    private Long id;
    private String spoolerId;
    private String jobName;
    private byte[] log;

    public SchedulerTaskHistoryLogDBItemPostgres() {
     }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`ID`")
    public Long getId() {
        return id;
    }

    @Column(name = "`ID`")
    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(name = "`SPOOLER_ID`", nullable = false)
    public String getSpoolerId() {
        return spoolerId;
    }

    @Column(name = "`SPOOLER_ID`", nullable = false)
    public void setSpoolerId(final String spoolerId) {
        this.spoolerId = spoolerId;
    }

    @Column(name = "`JOB_NAME`", nullable = false)
    public String getJobName() {
        return jobName;
    }

    @Column(name = "`JOB_NAME`", nullable = false)
    public void setJobName(final String jobName) {
        this.jobName = jobName;
    }

    @Column(name = "`LOG`", nullable = true)
    public byte[] getLog() {
        return log;
    }

    @Column(name = "`LOG`", nullable = true)
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
    public Path writeLogFile() throws IOException {
        if (log == null) {
            return null;
        } else {
            return SOSStreamUnzip.unzipToFile(log);
        }
    }
    
    @Transient
    public Path writeGzipLogFile() throws IOException {
        if (log == null) {
            return null;
        } else {
            return SOSStreamUnzip.zippedToFile(log);
        }
    }
    
    @Transient
    public String getSchedulerId() {
        return this.getSpoolerId();
    }
}