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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import com.sos.hibernate.classes.DbItem;
import com.sos.scheduler.SOSJobSchedulerGlobal;

import sos.util.SOSString;

@Entity
@Table(name = DBLayer.TABLE_REPORT_TASKS)
@SequenceGenerator(name = DBLayer.TABLE_REPORT_TASKS_SEQUENCE, sequenceName = DBLayer.TABLE_REPORT_TASKS_SEQUENCE, allocationSize = 1)
public class DBItemReportTask extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;

    /** Others */
    private String schedulerId;
    private Long historyId;

    private boolean isOrder;
    private String clusterMemberId;
    private Integer steps;
    private String folder;
    private String name;
    private String basename;
    private String title;
    private Date startTime;
    private Date endTime;
    private String cause;
    private Integer exitCode;
    private boolean error;
    private String errorCode;
    private String errorText;
    private String agentUrl;
    private String criticality;
    private boolean transferHistory;
    private boolean isRuntimeDefined;
    private boolean syncCompleted;
    private boolean resultsCompleted;

    private Date created;
    private Date modified;

    public DBItemReportTask() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORT_TASKS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORT_TASKS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    /** Others */
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

    @Column(name = "[IS_ORDER]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setIsOrder(boolean val) {
        this.isOrder = val;
    }

    @Column(name = "[IS_ORDER]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getIsOrder() {
        return this.isOrder;
    }

    @Column(name = "[CLUSTER_MEMBER_ID]", nullable = true)
    public void setClusterMemberId(String val) {
        this.clusterMemberId = val;
    }

    @Column(name = "[CLUSTER_MEMBER_ID]", nullable = true)
    public String getClusterMemberId() {
        return this.clusterMemberId;
    }

    @Column(name = "[STEPS]", nullable = false)
    public void setSteps(Integer val) {
        if (val == null || val.equals(new Integer(0))) {
            val = new Integer(1);
        }
        this.steps = val;
    }

    @Column(name = "[STEPS]", nullable = false)
    public Integer getSteps() {
        return this.steps;
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
        if (SOSString.isEmpty(val)) {
            val = SOSJobSchedulerGlobal.JOB_CRITICALITY.NORMAL.toString();
        }
        this.criticality = val;
    }

    @Column(name = "[CRITICALITY]", nullable = false)
    public String getCriticality() {
        return this.criticality;
    }

    @Column(name = "[TRANSFER_HISTORY]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setTransferHistory(boolean val) {
        this.transferHistory = val;
    }

    @Column(name = "[TRANSFER_HISTORY]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getTransferHistory() {
        return this.transferHistory;
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

    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(schedulerId).append(historyId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemReportTask)) {
            return false;
        }
        DBItemReportTask rhs = ((DBItemReportTask) other);
        return new EqualsBuilder().append(schedulerId, rhs.schedulerId).append(historyId, rhs.historyId).isEquals();
    }

}
