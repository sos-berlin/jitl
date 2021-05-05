package com.sos.jitl.reporting.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = "SCHEDULER_HISTORY")
public class DBItemSchedulerHistory extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    
    private String spoolerId;
    private String clusterMemberId;
    private String jobName;
    private Date startTime;
    private Date endTime;
    private String cause;
    private Integer steps;
    private Integer exitCode;
    private boolean error;
    private String errorCode;
    private String errorText;
    private String taskAgentUrl;
    private String transferHistory;
    
    public DBItemSchedulerHistory() {
    }

    @Id
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @Column(name = "[ID]", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    @Column(name = "[SPOOLER_ID]", nullable = false)
    public String getSpoolerId() {
        return spoolerId;
    }

    @Column(name = "[SPOOLER_ID]", nullable = false)
    public void setSpoolerId(String val) {
        this.spoolerId = val;
    }

    @Column(name = "[CLUSTER_MEMBER_ID]", nullable = true)
    public String getClusterMemberId() {
        return clusterMemberId;
    }

    @Column(name = "[CLUSTER_MEMBER_ID]", nullable = true)
    public void setClusterMemberId(String val) {
        this.clusterMemberId = val;
    }
    
    @Column(name = "[JOB_NAME]", nullable = true)
    public String getJobName() {
        return jobName;
    }

    @Column(name = "[JOB_NAME]", nullable = true)
    public void setJobName(String val) {
        this.jobName = val;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[START_TIME]", nullable = false)
    public Date getStartTime() {
        return startTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[START_TIME]", nullable = false)
    public void setStartTime(Date val) {
        this.startTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[END_TIME]", nullable = true)
    public Date getEndTime() {
        return endTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[END_TIME]", nullable = true)
    public void setEndTime(Date val) {
        this.endTime = val;
    }

    @Column(name = "[CAUSE]", nullable = true)
    public String getCause() {
        return cause;
    }

    @Column(name = "[CAUSE]", nullable = true)
    public void setCause(String val) {
        this.cause = val;
    }

    @Column(name = "[STEPS]", nullable = true)
    public Integer getSteps() {
        return steps;
    }

    @Column(name = "[STEPS]", nullable = true)
    public void setSteps(Integer val) {
        this.steps = val;
    }
    
    @Column(name = "[EXIT_CODE]", nullable = true)
    public Integer getExitCode() {
        return exitCode;
    }

    @Column(name = "[EXIT_CODE]", nullable = true)
    public void setExitCode(Integer val) {
        this.exitCode = val;
    }

    @Column(name = "[ERROR]", nullable = true)
    @Type(type = "numeric_boolean")
    public boolean isError() {
        return error;
    }

    @Column(name = "[ERROR]", nullable = true)
    @Type(type = "numeric_boolean")
    public void setError(Boolean val) {
        if (val == null) {
            val = false;
        }
        this.error = val;
    }

    @Column(name = "[ERROR_CODE]", nullable = true)
    public String getErrorCode() {
        return errorCode;
    }

    @Column(name = "[ERROR_CODE]", nullable = true)
    public void setErrorCode(String val) {
        this.errorCode = val;
    }

    @Column(name = "[ERROR_TEXT]", nullable = true)
    public String getErrorText() {
        return errorText;
    }

    @Column(name = "[ERROR_TEXT]", nullable = true)
    public void setErrorText(String val) {
        this.errorText = val;
    }
    
    @Column(name = "[AGENT_URL]", nullable = true)
    public String getAgentUrl() {
        return taskAgentUrl;
    }

    @Column(name = "[AGENT_URL]", nullable = true)
    public void setAgentUrl(String val) {
        this.taskAgentUrl = val;
    }
    
    @Column(name = "[TRANSFER_HISTORY]", nullable = true)
    public String getTransferHistory() {
        return transferHistory;
    }

    @Column(name = "[TRANSFER_HISTORY]", nullable = true)
    public void setTransferHistory(String val) {
        this.transferHistory = val;
    }
}
