package com.sos.jitl.schedulerhistory.db;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = "SCHEDULER_ORDER_STEP_HISTORY")
public class SchedulerOrderStepHistoryDBItem extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private SchedulerOrderStepHistoryCompoundKey id;
    private Long taskId;
    private String state;
    private Date startTime;
    private Date endTime;
    private Boolean error;
    private String errorText;
    private String errorCode;
    private SchedulerOrderHistoryDBItem schedulerOrderHistoryDBItem;
    private SchedulerTaskHistoryDBItem schedulerTaskHistoryDBItem;

    public SchedulerOrderStepHistoryDBItem() {
    }

    @ManyToOne(optional = true)
    @JoinColumn(name = "`HISTORY_ID`", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public SchedulerOrderHistoryDBItem getSchedulerOrderHistoryDBItem() {
        return this.schedulerOrderHistoryDBItem;
    }

    public void setSchedulerOrderHistoryDBItem(SchedulerOrderHistoryDBItem schedulerOrderHistoryDBItem) {
        this.schedulerOrderHistoryDBItem = schedulerOrderHistoryDBItem;
    }

    @ManyToOne(optional = true)
    @JoinColumn(name = "`TASK_ID`", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public SchedulerTaskHistoryDBItem getSchedulerTaskHistoryDBItem() {
        return this.schedulerTaskHistoryDBItem;
    }

    public void setSchedulerTaskHistoryDBItem(SchedulerTaskHistoryDBItem schedulerTaskHistoryDBItem) {
        this.schedulerTaskHistoryDBItem = schedulerTaskHistoryDBItem;
    }

    @Id
    public SchedulerOrderStepHistoryCompoundKey getId() {
        return id;
    }

    public void setId(SchedulerOrderStepHistoryCompoundKey id) {
        this.id = id;
    }

    @Column(name = "`TASK_ID`", nullable = false)
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Column(name = "`TASK_ID`", nullable = false)
    public Long getTaskId() {
        return taskId;
    }

    @Column(name = "`STATE`", nullable = true)
    public void setState(String state) {
        this.state = state;
    }

    @Column(name = "`STATE`", nullable = true)
    public String getState() {
        return state;
    }

    @Column(name = "`START_TIME`", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getStartTime() {
        return startTime;
    }

    @Column(name = "`START_TIME`", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Column(name = "`END_TIME`", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getEndTime() {
        return endTime;
    }

    @Column(name = "`END_TIME`", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Column(name = "`ERROR`", nullable = true)
    public Boolean isError() {
        return error;
    }

    @Column(name = "`ERROR`", nullable = true)
    public void setError(Boolean error) {
        this.error = error;
    }

    @Column(name = "`ERROR_CODE`", nullable = true)
    public String getErrorCode() {
        return errorCode;
    }

    @Column(name = "`ERROR_CODE`", nullable = true)
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Column(name = "`ERROR_TEXT`", nullable = true)
    public String getErrorText() {
        return errorText;
    }

    @Column(name = "`ERROR_TEXT`", nullable = true)
    public void setErrorText(String errorText) {
        this.errorText = errorText;
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
    public String getExecResult() {
        if (schedulerTaskHistoryDBItem == null) {
            return "";
        } else {
            return String.valueOf(schedulerTaskHistoryDBItem.getExecResult());
        }
    }

    @Transient
    public boolean haveError() {
        if (schedulerTaskHistoryDBItem == null) {
            return false;
        } else {
            return schedulerTaskHistoryDBItem.haveError();
        }
    }

    @Transient
    public Long getLogId() {
        return this.getTaskId();
    }

    @Transient
    public boolean isStandalone() {
        return true;
    }

    @Transient
    public String getTitle() {
        if (schedulerOrderHistoryDBItem == null) {
            return String.format("%s:%s (%s)", this.getState(), "", "");
        } else {
            return String.format("%s:%s (%s)", this.getState(), schedulerOrderHistoryDBItem.getJobChain(), schedulerOrderHistoryDBItem.getOrderId());
        }
    }

    @Transient
    public String getIdentifier() {
        return getTitle() + getLogId().toString();
    }

}