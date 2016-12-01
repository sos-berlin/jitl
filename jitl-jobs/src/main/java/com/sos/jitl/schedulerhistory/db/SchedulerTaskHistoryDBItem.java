package com.sos.jitl.schedulerhistory.db;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "SCHEDULER_HISTORY")
public class SchedulerTaskHistoryDBItem extends SchedulerHistoryLogDBItem {

    private Long id;
    private String spoolerId;
    private String clusterMemberId;
    private String jobName;
    private Date startTime;
    private Date endTime;
    private String cause;
    private Integer steps;
    private Integer exitCode;
    private Boolean error;
    private String errorText;
    private String errorCode;
    private Integer pid;
    private String agentUrl;
    private boolean assignToDaysScheduler = false;

    public SchedulerTaskHistoryDBItem() {
        this.assignToDaysScheduler = false;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`ID`")
    public Long getId() {
        return id;
    }

    @Column(name = "`ID`")
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "`SPOOLER_ID`")
    public String getSpoolerId() {
        return spoolerId;
    }

    @Column(name = "`SPOOLER_ID`")
    public void setSpoolerId(String spoolerId) {
        this.spoolerId = spoolerId;
    }

    @Column(name = "`CLUSTER_MEMBER_ID`", nullable = true)
    public String getClusterMemberId() {
        return clusterMemberId;
    }

    @Column(name = "`CLUSTER_MEMBER_ID`", nullable = true)
    public void setClusterMemberId(String clusterMemberId) {
        this.clusterMemberId = clusterMemberId;
    }

    @Column(name = "`JOB_NAME`")
    public String getJobName() {
        return jobName;
    }

    @Column(name = "`JOB_NAME`")
    public void setJobName(String jobName) {
        this.jobName = jobName;
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

    @Column(name = "`CAUSE`", nullable = true)
    public String getCause() {
        return cause;
    }

    @Column(name = "`CAUSE`", nullable = true)
    public void setCause(String cause) {
        this.cause = cause;
    }

    @Column(name = "`STEPS`", nullable = true)
    public Integer getSteps() {
        return steps;
    }

    @Column(name = "`STEPS`", nullable = true)
    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    @Column(name = "`EXIT_CODE`", nullable = true)
    public Integer getExitCode() {
        if (exitCode == null) {
            return 0;
        } else {
            return exitCode;
        }
    }

    @Column(name = "`EXIT_CODE`", nullable = true)
    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
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

    @Column(name = "`PID`")
    public Integer getPid() {
        return pid;
    }

    @Column(name = "`PID`")
    public void setPid(Integer pid) {
        this.pid = pid;
    }
    
    @Column(name = "`AGENT_URL`", nullable = true)
    public String getAgentUrl() {
        return agentUrl;
    }

    @Column(name = "`AGENT_URL`", nullable = true)
    public void setAgentUrl(String val) {
        this.agentUrl = val;
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
    public String getDurationFormated() {
        return this.getDateDiff(this.getStartTime(), this.getEndTime());
    }

    @Transient
    public String getJobOrJobchain() {
        return null2Blank(getJob());
    }

    @Transient
    public boolean isAssignToDaysScheduler() {
        return assignToDaysScheduler;
    }

    @Transient
    public void setAssignToDaysScheduler(boolean assignToDaysScheduler) {
        this.assignToDaysScheduler = assignToDaysScheduler;
    }

    @Transient
    public Long getLogId() {
        return this.getId();
    }

    @Transient
    public String getTitle() {
        return this.getJobName();
    }

    @Transient
    public boolean isStandalone() {
        return true;
    }

    @Transient
    public String getIdentifier() {
        return getTitle() + getLogId().toString();
    }

    @Transient
    public String getSchedulerId() {
        return this.getSpoolerId();
    }

    @Transient
    public String getJob() {
        return this.getJobName();
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
    public String getExecResult() {
        return String.valueOf(this.getExitCode());
    }

    @Transient
    public boolean haveError() {
        return this.getExitCode() != 0;
    }

    @Override
    @Transient
    public boolean equals(Object h) {
        return ((SchedulerTaskHistoryDBItem) h).getJobName().equals(this.getJobName());
    }

    @Override
    @Transient
    public int hashCode() {
        return this.id.intValue();
    }

}