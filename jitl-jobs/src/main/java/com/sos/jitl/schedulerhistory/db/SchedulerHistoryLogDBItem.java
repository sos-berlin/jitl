package com.sos.jitl.schedulerhistory.db;

import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.sos.hibernate.classes.DbItem;
import com.sos.scheduler.history.classes.SOSStreamUnzip;

@MappedSuperclass
public class SchedulerHistoryLogDBItem extends DbItem {

    private byte[] log;

    public SchedulerHistoryLogDBItem() {

    }

    @Lob
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
            SOSStreamUnzip SOSUnzip = new SOSStreamUnzip(log);
            return SOSUnzip.unzip2String();
        }
    }

}