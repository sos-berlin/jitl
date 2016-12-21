package com.sos.jitl.schedulerhistory.db;

import java.io.IOException;

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
@Table(name = "SCHEDULER_ORDER_HISTORY")
public class SchedulerOrderHistoryLogDBItemPostgres extends DbItem {

    private byte[] log;
    private Long historyId;

    public SchedulerOrderHistoryLogDBItemPostgres() {
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`HISTORY_ID`", nullable = false)
    public Long getHistoryId() {
        return historyId;
    }
    
    @Column(name = "`HISTORY_ID`", nullable = false)
    public void setHistoryId(final Long id) {
        historyId = id;
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
            SOSStreamUnzip SOSUnzip = new SOSStreamUnzip(log);
            return SOSUnzip.unzip2String();
        }
    }
    
 
}