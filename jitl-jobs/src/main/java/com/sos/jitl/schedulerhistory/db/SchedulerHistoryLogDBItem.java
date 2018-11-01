package com.sos.jitl.schedulerhistory.db;

import java.io.IOException;
import java.nio.file.Path;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.schedulerhistory.classes.SOSStreamUnzip;

@MappedSuperclass
public class SchedulerHistoryLogDBItem extends DbItem {

    private byte[] log;

    public SchedulerHistoryLogDBItem() {

    }

    @Lob
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
    public long getSize() throws IOException {
        if (log == null) {
            return 0L;
        } else {
            return SOSStreamUnzip.getSize(log);
        }
    }

}