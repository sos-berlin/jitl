package com.sos.jitl.reporting.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.schedulerhistory.db.SchedulerOrderStepHistoryCompoundKey;

@Entity
@Table(name = "SCHEDULER_ORDER_STEP_HISTORY")
public class DBItemSchedulerOrderStepHistory extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private SchedulerOrderStepHistoryCompoundKey id;
    private Long taskId;
    private String state;
    private Date startTime;
    private Date endTime;
    private Boolean error;
    private String errorText;
    private String errorCode;
    
    public DBItemSchedulerOrderStepHistory() {
    }
   
    @Id
    public SchedulerOrderStepHistoryCompoundKey getId() {
        return id;
    }

    public void setId(SchedulerOrderStepHistoryCompoundKey id) {
        this.id = id;
    }

    @Column(name = "[TASK_ID]", nullable = false)
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Column(name = "[TASK_ID]", nullable = false)
    public Long getTaskId() {
        return taskId;
    }

    @Column(name = "[STATE]", nullable = true)
    public void setState(String state) {
        this.state = state;
    }

    @Column(name = "[STATE]", nullable = true)
    public String getState() {
        return state;
    }

    @Column(name = "[START_TIME]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getStartTime() {
        return startTime;
    }

    @Column(name = "[START_TIME]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Column(name = "[END_TIME]", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getEndTime() {
        return endTime;
    }

    @Column(name = "[END_TIME]", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Column(name = "[ERROR]", nullable = true)
    public Boolean isError() {
        return error;
    }

    @Column(name = "[ERROR]", nullable = true)
    public void setError(Boolean error) {
        this.error = error;
    }

    @Column(name = "[ERROR_CODE]", nullable = true)
    public String getErrorCode() {
        return errorCode;
    }

    @Column(name = "[ERROR_CODE]", nullable = true)
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Column(name = "[ERROR_TEXT]", nullable = true)
    public String getErrorText() {
        return errorText;
    }

    @Column(name = "[ERROR_TEXT]", nullable = true)
    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

}