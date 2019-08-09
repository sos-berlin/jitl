package com.sos.jitl.schedulerhistory.db;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "SCHEDULER_ORDER_HISTORY")
public class SchedulerOrderHistoryDBItem extends SchedulerHistoryLogDBItem {

    private Long historyId;
    private String spoolerId;
    private String orderId;
    private String jobChain;
    private Date startTime;
    private Date endTime;
    private String title;
    private String state;
    private String stateText;
    private boolean assignToDaysScheduler = false;

    public SchedulerOrderHistoryDBItem() {
        assignToDaysScheduler = false;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "[HISTORY_ID]", nullable = false)
    public Long getHistoryId() {
        return historyId;
    }

    @Column(name = "[HISTORY_ID]", nullable = false)
    public void setHistoryId(final Long id) {
        historyId = id;
    }

    @Column(name = "[SPOOLER_ID]", nullable = false)
    public String getSpoolerId() {
        return spoolerId;
    }

    @Column(name = "[SPOOLER_ID]", nullable = false)
    public void setSpoolerId(final String spoolerId) {
        this.spoolerId = spoolerId;
    }

    @Column(name = "[ORDER_ID]", nullable = false)
    public String getOrderId() {
        return orderId;
    }

    @Column(name = "[ORDER_ID]", nullable = false)
    public void setOrderId(final String orderId) {
        this.orderId = orderId;
    }

    @Column(name = "[JOB_CHAIN]", nullable = false)
    public String getJobChain() {
        return jobChain;
    }

    @Column(name = "[JOB_CHAIN]", nullable = false)
    public void setJobChain(final String jobChain) {
        this.jobChain = jobChain;
    }

    @Column(name = "[START_TIME]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getStartTime() {
        return startTime;
    }

    @Column(name = "[START_TIME]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public void setStartTime(final Date startTime) {
        this.startTime = startTime;
    }

    @Column(name = "[END_TIME]", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getEndTime() {
        return endTime;
    }

    @Column(name = "[END_TIME]", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    public void setEndTime(final Date endTime) {
        this.endTime = endTime;
    }

    @Column(name = "[TITLE]", nullable = true)
    public String getCause() {
        return title;
    }

    @Column(name = "[TITLE]", nullable = true)
    public void setCause(final String title) {
        this.title = title;
    }

    @Column(name = "[STATE]", nullable = true)
    public String getState() {
        return state;
    }

    @Column(name = "[STATE]", nullable = true)
    public void setState(final String state) {
        this.state = state;
    }

    @Transient
    public String getStartTimeIso() {
        if (this.getStartTime() == null) {
            return "";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return formatter.format(this.getStartTime());
        }
    }

    @Transient
    public String getEndTimeIso() {
        if (this.getEndTime() == null) {
            return "";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return formatter.format(this.getEndTime());
        }
    }

    @Column(name = "[STATE_TEXT]", nullable = true)
    public String getStateText() {
        return stateText;
    }

    @Column(name = "[STATE_TEXT]", nullable = true)
    public void setStateText(final String stateText) {
        this.stateText = stateText;
    }

    // If the name would be getStartTimeIso, a setter for startTimeIso must be
    // implemented
    public String readStartTimeIso() {
        if (this.getStartTime() == null) {
            return "";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return formatter.format(this.getStartTime());
        }
    }

    // If the name would be getEndTimeIso, a setter for endTimeIso must be
    // implemented
    public String readEndTimeIso() {
        if (this.getEndTime() == null) {
            return "";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return formatter.format(this.getEndTime());
        }
    }

    @Transient
    public String getStartTimeFormated() {
        return getDateFormatted(this.getStartTime());
    }

    @Transient
    public String getEndTimeFormated() {
        return getDateFormatted(this.getEndTime());
    }

    @Transient
    public String getDurationFormated() {
        return this.getDateDiff(this.getStartTime(), this.getEndTime());
    }

    @Transient
    public boolean isAssignToDaysScheduler() {
        return assignToDaysScheduler;
    }

    @Transient
    public String getJobOrJobchain() {
        return null2Blank(String.format("%s(%s)", getJobChain(), getOrderId()));
    }

    @Transient
    public void setAssignToDaysScheduler(final boolean assignToDaysScheduler) {
        this.assignToDaysScheduler = assignToDaysScheduler;
    }

    @Transient
    public Long getLogId() {
        return this.getHistoryId();
    }

    @Transient
    public String getIdentifier() {
        return this.getJobChain() + "/" + this.getOrderId();
    }

    @Transient
    public boolean isStandalone() {
        return false;
    }

    @Transient
    public String getSchedulerId() {
        return this.getSpoolerId();
    }

    @Transient
    public String getTitle() {
        return jobChain + "/" + orderId;
    }

    @Transient
    public String getExecResult() {
        return this.getState();
    }
    
    @Transient
    public boolean haveError() {
        if (this.getState() == null) {
            return false;
        } else {
            return this.getState().toLowerCase().contains("error") || this.getState().toLowerCase().contains("fehler")
                    || this.getState().startsWith("!") || this.getState().toLowerCase().contains("fault");
        }
    }

    @Transient
    public boolean equals(final Object h) {
        return ((SchedulerOrderHistoryDBItem) h).getJobChain().equals(this.getJobChain())
                && ((SchedulerOrderHistoryDBItem) h).getOrderId().equals(this.getOrderId());
    }

    @Transient
    public int hashCode() {
        return this.historyId.intValue();
    }

    @Transient
    public boolean isOrderJob() {
        return true;
    }

}