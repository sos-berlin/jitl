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

@Entity
@Table(name = DBLayer.TABLE_REPORT_EXECUTIONS)
@SequenceGenerator(name = DBLayer.TABLE_REPORT_EXECUTIONS_SEQUENCE, sequenceName = DBLayer.TABLE_REPORT_EXECUTIONS_SEQUENCE, allocationSize = 1)
public class DBItemReportExecution extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;

    private String schedulerId;
    private Long historyId;
    /** Foreign key REPORT_TRIGGERS.ID */
    private Long triggerId;

    /** Others */
    private Long step;
    private String name;
    private String basename;
    private String title;
    private Date startTime;
    private Date endTime;
    private String state;
    private String cause;
    private boolean error;
    private String errorCode;
    private String errorText;
    private String agentUrl;
    private boolean isRuntimeDefined;
    private boolean suspended;

    private Date created;
    private Date modified;

    public DBItemReportExecution() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORT_EXECUTIONS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORT_EXECUTIONS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public String getSchedulerId() {
        return this.schedulerId;
    }

    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }

    @Column(name = "`HISTORY_ID`", nullable = false)
    public Long getHistoryId() {
        return this.historyId;
    }

    @Column(name = "`HISTORY_ID`", nullable = false)
    public void setHistoryId(Long val) {
        this.historyId = val;
    }

    /** Foreign key REPORT_TRIGGERS.ID */
    @Column(name = "`TRIGGER_ID`", nullable = false)
    public Long getTriggerId() {
        return this.triggerId;
    }

    @Column(name = "`TRIGGER_ID`", nullable = false)
    public void setTriggerId(Long val) {
        this.triggerId = val;
    }

    /** Others */
    @Column(name = "`STEP`", nullable = false)
    public void setStep(Long val) {
        this.step = val;
    }

    @Column(name = "`STEP`", nullable = false)
    public Long getStep() {
        return this.step;
    }

    @Column(name = "`NAME`", nullable = false)
    public void setName(String val) {
        this.name = val;
    }

    @Column(name = "`NAME`", nullable = false)
    public String getName() {
        return this.name;
    }

    @Column(name = "`BASENAME`", nullable = false)
    public void setBasename(String val) {
        this.basename = val;
    }

    @Column(name = "`BASENAME`", nullable = false)
    public String getBasename() {
        return this.basename;
    }

    @Column(name = "`TITLE`", nullable = true)
    public void setTitle(String val) {
        this.title = val;
    }

    @Column(name = "`TITLE`", nullable = true)
    public String getTitle() {
        return this.title;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public void setStartTime(Date val) {
        this.startTime = val;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public Date getStartTime() {
        return this.startTime;
    }

    @Column(name = "`END_TIME`", nullable = true)
    public void setEndTime(Date val) {
        this.endTime = val;
    }

    @Column(name = "`END_TIME`", nullable = true)
    public Date getEndTime() {
        return this.endTime;
    }

    @Column(name = "`STATE`", nullable = false)
    public void setState(String val) {
        this.state = val;
    }

    @Column(name = "`STATE`", nullable = false)
    public String getState() {
        return this.state;
    }

    @Column(name = "`CAUSE`", nullable = false)
    public void setCause(String val) {
        this.cause = val;
    }

    @Column(name = "`CAUSE`", nullable = false)
    public String getCause() {
        return this.cause;
    }

    @Transient
    public void setError(Boolean val) {
        if (val == null) {
            val = false;
        }
        this.setError(val.booleanValue());
    }

    @Column(name = "`ERROR`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setError(boolean val) {
        this.error = val;
    }

    @Column(name = "`ERROR`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getError() {
        return this.error;
    }

    @Column(name = "`ERROR_CODE`", nullable = true)
    public void setErrorCode(String val) {
        this.errorCode = val;
    }

    @Column(name = "`ERROR_CODE`", nullable = true)
    public String getErrorCode() {
        return this.errorCode;
    }

    @Column(name = "`ERROR_TEXT`", nullable = true)
    public void setErrorText(String val) {
        this.errorText = val;
    }

    @Column(name = "`ERROR_TEXT`", nullable = true)
    public String getErrorText() {
        return this.errorText;
    }

    @Column(name = "`AGENT_URL`", nullable = true)
    public void setAgentUrl(String val) {
        this.agentUrl = val;
    }

    @Column(name = "`AGENT_URL`", nullable = true)
    public String getAgentUrl() {
        return this.agentUrl;
    }

    @Column(name = "`IS_RUNTIME_DEFINED`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setIsRuntimeDefined(boolean val) {
        this.isRuntimeDefined = val;
    }

    @Column(name = "`IS_RUNTIME_DEFINED`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getIsRuntimeDefined() {
        return this.isRuntimeDefined;
    }

    @Column(name = "`SUSPENDED`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setSuspended(boolean val) {
        this.suspended = val;
    }

    @Column(name = "`SUSPENDED`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getSuspended() {
        return this.suspended;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        this.created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return this.created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date val) {
        this.modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return this.modified;
    }
}
