package com.sos.jitl.reporting.db;

import java.io.Serializable;
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
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.sos.hibernate.classes.DbItem;
import com.sos.scheduler.SOSJobSchedulerGlobal;

import sos.util.SOSString;

@Entity
@Table(name = DBLayer.TABLE_REPORT_EXECUTIONS)
@SequenceGenerator(name = DBLayer.TABLE_REPORT_EXECUTIONS_SEQUENCE, sequenceName = DBLayer.TABLE_REPORT_EXECUTIONS_SEQUENCE, allocationSize = 1)
public class DBItemReportExecution extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_STATE = ".";

    /** Primary key */
    private Long id;

    private String schedulerId;
    /** Foreign key SCHEDULER_HISTORY.ID */
    private Long historyId;
    /** Foreign key REPORTING_TRIGGERS.ID */
    private Long triggerId;
    /** Foreign key REPORTING_TRIGGERS.HISTORY_ID */
    private Long triggerHistoryId;
    /** Foreign key REPORTING_TASKS.ID */
    private Long taskId;

    /** Others */
    private String clusterMemberId;
    private Long step;
    private String folder;
    private String name;
    private String basename;
    private String title;
    private Date startTime;
    private Date endTime;
    private String state;
    private String cause;
    private Integer exitCode;
    private boolean error;
    private String errorCode;
    private String errorText;
    private String agentUrl;
    private String criticality;
    private boolean isRuntimeDefined;
    private boolean syncCompleted;
    private boolean resultsCompleted;

    private Date taskStartTime;
    private Date taskEndTime;

    private Date created;
    private Date modified;

    public DBItemReportExecution() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORT_EXECUTIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORT_EXECUTIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public String getSchedulerId() {
        return this.schedulerId;
    }

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }

    @Column(name = "[HISTORY_ID]", nullable = false)
    public Long getHistoryId() {
        return this.historyId;
    }

    @Column(name = "[HISTORY_ID]", nullable = false)
    public void setHistoryId(Long val) {
        this.historyId = val;
    }

    /** Foreign key REPORTING_TRIGGERS.ID */
    @Column(name = "[TRIGGER_ID]", nullable = false)
    public Long getTriggerId() {
        return this.triggerId;
    }

    @Column(name = "[TRIGGER_ID]", nullable = false)
    public void setTriggerId(Long val) {
        this.triggerId = val;
    }

    /** Foreign key REPORTING_TRIGGERS.HISTORY_ID */
    @Column(name = "[TRIGGER_HISTORY_ID]", nullable = false)
    public Long getTriggerHistoryId() {
        return this.triggerHistoryId;
    }

    @Column(name = "[TRIGGER_HISTORY_ID]", nullable = false)
    public void setTriggerHistoryId(Long val) {
        this.triggerHistoryId = val;
    }

    /** Foreign key REPORTING_TASKS.ID */
    @Column(name = "[TASK_ID]", nullable = false)
    public Long getTaskId() {
        return this.taskId;
    }

    @Column(name = "[TASK_ID]", nullable = false)
    public void setTaskId(Long val) {
        this.taskId = val;
    }
    
    /** Others */
    @Column(name = "[CLUSTER_MEMBER_ID]", nullable = true)
    public void setClusterMemberId(String val) {
        this.clusterMemberId = val;
    }

    @Column(name = "[CLUSTER_MEMBER_ID]", nullable = true)
    public String getClusterMemberId() {
        return this.clusterMemberId;
    }

    @Column(name = "[STEP]", nullable = false)
    public void setStep(Long val) {
        this.step = val;
    }

    @Column(name = "[STEP]", nullable = false)
    public Long getStep() {
        return this.step;
    }

    @Column(name = "[FOLDER]", nullable = false)
    public void setFolder(String val) {
        if (val == null) {
            val = DBLayer.DEFAULT_FOLDER;
        }
        this.folder = normalizePath(val);
    }

    @Column(name = "[FOLDER]", nullable = false)
    public String getFolder() {
        return this.folder;
    }

    @Column(name = "[NAME]", nullable = false)
    public void setName(String val) {
        this.name = normalizePath(val);
    }

    @Column(name = "[NAME]", nullable = false)
    public String getName() {
        return this.name;
    }

    @Column(name = "[BASENAME]", nullable = false)
    public void setBasename(String val) {
        this.basename = val;
    }

    @Column(name = "[BASENAME]", nullable = false)
    public String getBasename() {
        return this.basename;
    }

    @Column(name = "[TITLE]", nullable = true)
    public void setTitle(String val) {
        if (val != null && val.trim().length() == 0) {
            val = null;
        }
        this.title = val;
    }

    @Column(name = "[TITLE]", nullable = true)
    public String getTitle() {
        return this.title;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[START_TIME]", nullable = false)
    public void setStartTime(Date val) {
        this.startTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[START_TIME]", nullable = false)
    public Date getStartTime() {
        return this.startTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[END_TIME]", nullable = true)
    public void setEndTime(Date val) {
        this.endTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[END_TIME]", nullable = true)
    public Date getEndTime() {
        return this.endTime;
    }

    @Column(name = "[STATE]", nullable = false)
    public void setState(String val) {
        if (SOSString.isEmpty(val)) {
            val = DEFAULT_STATE;
        }
        this.state = val;
    }

    @Column(name = "[STATE]", nullable = false)
    public String getState() {
        return this.state;
    }

    @Column(name = "[CAUSE]", nullable = false)
    public void setCause(String val) {
        this.cause = val;
    }

    @Column(name = "[CAUSE]", nullable = false)
    public String getCause() {
        return this.cause;
    }

    @Column(name = "[EXIT_CODE]", nullable = false)
    public void setExitCode(Integer val) {
        if (val == null) {
            val = new Integer(0);
        }
        this.exitCode = val;
    }

    @Column(name = "[EXIT_CODE]", nullable = false)
    public Integer getExitCode() {
        return this.exitCode;
    }

    @Transient
    public void setError(Boolean val) {
        if (val == null) {
            val = false;
        }
        this.setError(val.booleanValue());
    }

    @Column(name = "[ERROR]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setError(boolean val) {
        this.error = val;
    }

    @Column(name = "[ERROR]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getError() {
        return this.error;
    }

    @Column(name = "[ERROR_CODE]", nullable = true)
    public void setErrorCode(String val) {
        this.errorCode = val;
    }

    @Column(name = "[ERROR_CODE]", nullable = true)
    public String getErrorCode() {
        return this.errorCode;
    }

    @Column(name = "[ERROR_TEXT]", nullable = true)
    public void setErrorText(String val) {
        this.errorText = val;
    }

    @Column(name = "[ERROR_TEXT]", nullable = true)
    public String getErrorText() {
        return this.errorText;
    }

    @Column(name = "[AGENT_URL]", nullable = true)
    public void setAgentUrl(String val) {
        this.agentUrl = val;
    }

    @Column(name = "[AGENT_URL]", nullable = true)
    public String getAgentUrl() {
        return this.agentUrl;
    }

    @Column(name = "[CRITICALITY]", nullable = false)
    public void setCriticality(String val) {
        if(SOSString.isEmpty(val)) {
            val = SOSJobSchedulerGlobal.JOB_CRITICALITY.NORMAL.toString();
        }
        this.criticality = val;
    }

    @Column(name = "[CRITICALITY]", nullable = false)
    public String getCriticality() {
        return this.criticality;
    }
    
    @Column(name = "[IS_RUNTIME_DEFINED]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setIsRuntimeDefined(boolean val) {
        this.isRuntimeDefined = val;
    }

    @Column(name = "[IS_RUNTIME_DEFINED]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getIsRuntimeDefined() {
        return this.isRuntimeDefined;
    }

    @Column(name = "[SYNC_COMPLETED]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setSyncCompleted(boolean val) {
        this.syncCompleted = val;
    }

    @Column(name = "[SYNC_COMPLETED]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getSyncCompleted() {
        return this.syncCompleted;
    }

    @Column(name = "[RESULTS_COMPLETED]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setResultsCompleted(boolean val) {
        this.resultsCompleted = val;
    }

    @Column(name = "[RESULTS_COMPLETED]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getResultsCompleted() {
        return this.resultsCompleted;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public void setCreated(Date val) {
        this.created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public Date getCreated() {
        return this.created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public void setModified(Date val) {
        this.modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public Date getModified() {
        return this.modified;
    }

    @Transient
    public void setTaskStartTime(Date val) {
        this.taskStartTime = val;
    }

    @Transient
    public Date getTaskStartTime() {
        return this.taskStartTime;
    }

    @Transient
    public void setTaskEndTime(Date val) {
        this.taskEndTime = val;
    }

    @Transient
    public Date getTaskEndTime() {
        return this.taskEndTime;
    }

    public boolean haveError() {
        return this.getExitCode() != 0;
    }

    @Transient
    public String getHistoryIdAsString() {
        return String.valueOf(historyId);
    }

    @Transient
    public boolean isSuccessFull() {
        return (getEndTime() != null && !getError());
    }

    @Transient
    public boolean isInComplete() {
        return (getStartTime() != null && getEndTime() == null);
    }

    @Transient
    public boolean isFailed() {
        return (getEndTime() != null && getError());
    }
}
