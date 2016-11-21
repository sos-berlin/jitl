package com.sos.jitl.dailyplan.db;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.persistence.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.joda.time.DateTime;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.UtcTimeHelper;
import com.sos.jitl.dailyplan.ExecutionState;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTrigger;

@Entity
@Table(name = "DAILY_PLAN")
public class DailyPlanDBItem extends DbItem {

    private Long id;
    private String schedulerId;
    private String job;
    private String jobChain;
    private String orderId;
    private Date plannedStart;
    private Date expectedEnd;
    private Date periodBegin;
    private Date periodEnd;
    private boolean startStart;
    private Long repeatInterval;
    private boolean isAssigned;
    private boolean isLate;
    private String state;
    private Date created;
    private Date modified;
    private Long reportTriggerId;
    private Long reportExecutionId;
    private DBItemReportTrigger dbItemReportTrigger;
    private DBItemReportExecution dbItemReportExecution;
    private String dateFormat = "yyyy-MM-dd hh:mm";
    private ExecutionState executionState;

    public DailyPlanDBItem(String dateFormat_) {
        this.dateFormat = dateFormat_;
    }

    public DailyPlanDBItem() {

    }

    @ManyToOne(optional = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "`REPORT_TRIGGER_ID`", referencedColumnName = "`ID`", insertable = false, updatable = false)
    public DBItemReportTrigger getDbItemReportTrigger() {
        return dbItemReportTrigger;
    }

    public void setDbItemReportTrigger(DBItemReportTrigger dbItemReportTrigger) {
        this.dbItemReportTrigger = dbItemReportTrigger;
    }

    @ManyToOne(optional = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "`REPORT_EXECUTIONS_ID`", referencedColumnName = "`ID`", insertable = false, updatable = false)
    public DBItemReportExecution getDbItemReportExecution() {
        return dbItemReportExecution;
    }

    public void setDbItemReportExecution(DBItemReportExecution dbItemReportExecution) {
        this.dbItemReportExecution = dbItemReportExecution;
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

    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    @Column(name = "`REPORT_EXECUTIONS_ID`", nullable = true)
    public void setReportExecutionId(Long reportExecutionId) {
        this.reportExecutionId = reportExecutionId;
    }

    @Column(name = "`REPORT_EXECUTIONS_ID`", nullable = true)
    public Long getReportExecutionId() {
        return reportExecutionId;
    }

    @Column(name = "`REPORT_TRIGGER_ID`", nullable = true)
    public void setReportTriggerId(Long reportTriggerId) {
        this.reportTriggerId = reportTriggerId;
    }

    @Column(name = "`REPORT_TRIGGER_ID`", nullable = true)
    public Long getReportTriggerId() {
        return reportTriggerId;
    }

    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public String getSchedulerId() {
        return schedulerId;
    }

    @Column(name = "`JOB`", nullable = true)
    public void setJob(String job) {
        this.job = job;
    }

    @Column(name = "`JOB`", nullable = true)
    public String getJob() {
        return job;
    }

    @Column(name = "`STATE`", nullable = true)
    public void setState(String state) {
        this.state = state;
    }

    @Column(name = "`STATE`", nullable = true)
    public String getState() {
        return state;
    }

    @Transient
    public String getJobNotNull() {
        return null2Blank(job);
    }

    @Column(name = "`ORDER_ID`", nullable = true)
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Column(name = "`ORDER_ID`", nullable = true)
    public String getOrderId() {
        return orderId;
    }

    @Transient
    public String getOrderIdNotNull() {
        return null2Blank(orderId);
    }

    @Column(name = "`JOB_CHAIN`", nullable = true)
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    @Column(name = "`JOB_CHAIN`", nullable = true)
    public String getJobChain() {
        return jobChain;
    }

    @Transient
    public String getJobChainNotNull() {
        return null2Blank(jobChain);
    }

    @Transient
    public String getJobOrJobchain() {
        if (this.isOrderJob()) {
            return null2Blank(String.format("%s(%s)", getJobChainNotNull(), getOrderIdNotNull()));
        } else {
            return null2Blank(getJobNotNull());
        }
    }

    @Column(name = "`IS_ASSIGNED`", nullable = false)
    public void setIsAssigned(Boolean isAssigned) {
        this.isAssigned = isAssigned;
    }

    @Column(name = "`IS_ASSIGNED`", nullable = false)
    public Boolean getIsAssigned() {
        return isAssigned;
    }

    @Column(name = "`IS_LATE`", nullable = false)
    public void setIsLate(Boolean isLate) {
        this.isLate = isLate;
    }

    @Column(name = "`IS_LATE`", nullable = false)
    public Boolean getIsLate() {
        return isLate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`PLANNED_START`", nullable = true)
    public void setPlannedStart(Date plannedStart) {
        this.plannedStart = plannedStart;
    }

    @Transient
    public void setPlannedStart(String plannedStart) throws ParseException {
        DailyPlanDate dailyScheduleDate = new DailyPlanDate(dateFormat);
        dailyScheduleDate.setSchedule(plannedStart);
        this.plannedStart = dailyScheduleDate.getSchedule();
    }

    @Transient
    public void setScheduleExecuted(String scheduleExecuted) throws ParseException {
        DailyPlanDate dailyScheduleDate = new DailyPlanDate(dateFormat);
        dailyScheduleDate.setSchedule(scheduleExecuted);
        this.expectedEnd = dailyScheduleDate.getSchedule();
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`PLANNED_START`", nullable = true)
    public Date getPlannedStart() {
        return plannedStart;
    }

    @Transient
    public String getPlannedStartIso() {
        if (this.getPlannedStart() == null) {
            return "";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return formatter.format(this.getPlannedStart());
        }
    }

    @Transient
    public String getPlannedStartFormated() {
        return getDateFormatted(this.getPlannedStart());
    }

    @Transient
    public String getScheduleEndedFormated() {
        if (this.getDbItemReportTrigger() != null) {
            return getDateFormatted(this.getDbItemReportTrigger().getEndTime());
        } else {
            return "";
        }

    }

    @Transient
    public Date getEndTimeFromHistory() {
        if (this.getDbItemReportTrigger() != null) {
            return this.getDbItemReportTrigger().getEndTime();
        } else {
            return null;
        }
    }

    @Transient
    public String getScheduleExecutedIso() {
        if (this.getExpectedEnd() == null) {
            return "";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return formatter.format(this.getExpectedEnd());
        }
    }

    @Transient
    public String getExpectedEndFormated() {
        return getDateFormatted(this.getExpectedEnd());
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`EXPECTED_END`", nullable = true)
    public void setExpectedEnd(Date expectedEnd) {
        this.expectedEnd = expectedEnd;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`EXPECTED_END`", nullable = true)
    public Date getExpectedEnd() {
        return expectedEnd;
    }

    @Column(name = "`REPEAT_INTERVAL`", nullable = true)
    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    @Override
    @Transient
    public String getDurationFormated() {
        return this.getDateDiff(this.getExpectedEnd(), this.getEndTimeFromHistory());
    }

    @Transient
    public void setRepeatInterval(BigInteger absolutRepeat_, BigInteger repeat_) {
        BigInteger r = BigInteger.ZERO;
        Long l = Long.valueOf(0);
        if (absolutRepeat_ != null && !absolutRepeat_.equals(BigInteger.ZERO)) {
            r = absolutRepeat_;
            this.startStart = true;
            if (r != null) {
                l = Long.valueOf(r.longValue());
            }
        } else {
            r = repeat_;
            this.startStart = false;
            if (r != null) {
                l = Long.valueOf(r.longValue());
            }
        }
        this.repeatInterval = l;
    }

    @Column(name = "`REPEAT_INTERVAL`", nullable = true)
    public Long getRepeatInterval() {
        return repeatInterval;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`PERIOD_BEGIN`", nullable = true)
    public void setPeriodBegin(Date periodBegin) {
        this.periodBegin = periodBegin;
        this.plannedStart = periodBegin;

    }

    @Transient
    public void setPeriodBegin(String periodBegin) throws ParseException {
        DailyPlanDate daysScheduleDate = new DailyPlanDate(dateFormat);
        daysScheduleDate.setSchedule(periodBegin);
        this.periodBegin = daysScheduleDate.getSchedule();
        this.plannedStart = this.periodBegin;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`PERIOD_BEGIN`", nullable = true)
    public Date getPeriodBegin() {
        return periodBegin;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`PERIOD_END`", nullable = true)
    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    @Transient
    public void setPeriodEnd(String periodEnd) throws ParseException {
        DailyPlanDate daysScheduleDate = new DailyPlanDate(dateFormat);
        daysScheduleDate.setSchedule(periodEnd);
        this.periodEnd = daysScheduleDate.getSchedule();
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`PERIOD_END`", nullable = true)
    public Date getPeriodEnd() {
        return periodEnd;
    }

    @Column(name = "`START_START`", nullable = true)
    public void setStartStart(Boolean startStart) {
        this.startStart = startStart;
    }

    @Column(name = "`START_START`", nullable = true)
    public Boolean getStartStart() {
        return startStart;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date created) {
        this.created = created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = true)
    public Date getModified() {
        return modified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = true)
    public void setModified(Date modified) {
        this.modified = modified;
    }

    @Transient
    public boolean isOrderJob() {
        return !this.isStandalone();
    }

    @Transient
    public boolean isStandalone() {
        return this.job != null && !"".equals(this.job) && (this.jobChain == null || "".equals(this.jobChain));
    }

    @Transient
    public String getName() {
        if (isStandalone()) {
            return this.job;
        } else {
            return this.jobChain + "/" + this.orderId;
        }
    }

    @Transient
    public ExecutionState getExecutionState() {
        if (executionState != null) {
            return executionState;
        } else {
            executionState = new ExecutionState();
            String fromTimeZoneString = "UTC";
            Date endTime = null;
            DateTime plannedTimeInUtc = new DateTime(plannedStart);
            DateTime endTimeInUtc = null;
            DateTime startTimeInUtc = null;
            DateTime dateTimePeriodBeginInUtc = null;
            if (isStandalone()) {
                if (dbItemReportExecution != null) {
                    endTime = dbItemReportExecution.getEndTime();
                }
            } else {
                if (dbItemReportTrigger != null) {
                    endTime = dbItemReportTrigger.getEndTime();
                }
            }
            if (endTime != null) {
                endTimeInUtc = new DateTime(endTime);
            }
            if (periodBegin != null) {
                dateTimePeriodBeginInUtc = new DateTime(periodBegin);
            }
            String toTimeZoneString = TimeZone.getDefault().getID();
            Date plannedTimeLocal = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, plannedTimeInUtc);
            Date endTimeLocal = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, endTimeInUtc);
            Date startTimeLocal = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, startTimeInUtc);
            Date periodBeginLocal = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, dateTimePeriodBeginInUtc);
            this.executionState.setPlannedTime(plannedTimeLocal);
            this.executionState.setEndTime(endTimeLocal);
            this.executionState.setStartTime(startTimeLocal);
            this.executionState.setPeriodBegin(periodBeginLocal);
            this.executionState.setHaveError(this.haveError());
            return executionState;
        }
    }

    @Transient
    public Long getLogId() {
        if (isOrderJob()) {
            return this.getReportTriggerId();
        } else {
            return this.getReportExecutionId();
        }
    }

    @Transient
    public String getTitle() {
        if (isOrderJob()) {
            return this.getJobChain() + "/" + this.getOrderId();
        } else {
            return this.getJob();
        }
    }

    @Transient
    public String getIdentifier() {
        if (getLogId() != null) {
            return getTitle() + getLogId().toString();
        } else {
            return getTitle();
        }
    }

    @Transient
    public String getJobName() {
        return getJob();
    }

    @Transient
    public boolean haveError() {
        if (this.isOrderJob()) {
            return this.dbItemReportTrigger != null && this.dbItemReportTrigger.haveError();
        } else {
            return this.dbItemReportExecution != null && this.dbItemReportExecution.haveError();
        }
    }

    @Transient
    public boolean isCompleted() {
        if (this.isOrderJob()) {
            return (dbItemReportTrigger != null && dbItemReportTrigger.getStartTime() != null && dbItemReportTrigger.getEndTime() != null);
        } else {
            return (dbItemReportExecution != null && dbItemReportExecution.getStartTime() != null && dbItemReportExecution.getEndTime() != null);
        }
    }

    @Transient
    public Integer getStartMode() {
        if (this.getExecutionState().singleStart()) {
            return 0;
        } else {
            if (this.startStart) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    @Transient
    public boolean isEqual(DBItemReportTrigger dbItemReportTrigger) {
        String job_chain = this.getJobChain().replaceAll("^/", "");
        String job_chain2 = dbItemReportTrigger.getParentName().replaceAll("^/", "");
        return (this.getPlannedStart().equals(dbItemReportTrigger.getStartTime()) || this.getPlannedStart().before(dbItemReportTrigger.getStartTime())) && job_chain
                .equalsIgnoreCase(job_chain2) && this.getOrderId().equalsIgnoreCase(dbItemReportTrigger.getName());
    }

    @Transient
    public boolean isEqual(DBItemReportExecution dbItemReportExecution) {
        String job = this.getJob().replaceAll("^/", "");
        String job2 = dbItemReportExecution.getName().replaceAll("^/", "");
        return (this.getPlannedStart().equals(dbItemReportExecution.getStartTime()) || this.getPlannedStart().before(dbItemReportExecution.getStartTime())) && job.equalsIgnoreCase(
                job2);
    }

}