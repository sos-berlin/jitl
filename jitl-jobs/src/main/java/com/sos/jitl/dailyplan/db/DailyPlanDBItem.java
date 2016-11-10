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
import com.sos.scheduler.history.db.SchedulerTaskHistoryDBItem;
import com.sos.scheduler.history.db.SchedulerOrderHistoryDBItem;

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
    private Date created;
    private Date modified;
    private Long reportTriggerId;
    private Long reportExecutionId;
    private SchedulerOrderHistoryDBItem schedulerOrderHistoryDBItem;
    private SchedulerTaskHistoryDBItem schedulerTaskHistoryDBItem;
    private String dateFormat = "yyyy-MM-dd hh:mm";
    private ExecutionState executionState = new ExecutionState();

    public DailyPlanDBItem(String dateFormat_) {
        this.dateFormat = dateFormat_;
    }

    public DailyPlanDBItem() {

    }

    @ManyToOne(optional = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "`REPORT_TRIGGER_ID`", referencedColumnName = "`HISTORY_ID`", insertable = false, updatable = false)
    public SchedulerOrderHistoryDBItem getSchedulerOrderHistoryDBItem() {
        return schedulerOrderHistoryDBItem;
    }

    public void setSchedulerOrderHistoryDBItem(SchedulerOrderHistoryDBItem schedulerOrderHistoryDBItem) {
        this.schedulerOrderHistoryDBItem = schedulerOrderHistoryDBItem;
    }

    @ManyToOne(optional = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "`REPORT_EXECUTIONS_ID`", referencedColumnName = "`ID`", insertable = false, updatable = false)
    public SchedulerTaskHistoryDBItem getSchedulerTaskHistoryDBItem() {
        return schedulerTaskHistoryDBItem;
    }

    public void setSchedulerTaskHistoryDBItem(SchedulerTaskHistoryDBItem schedulerTaskHistoryDBItem) {
        this.schedulerTaskHistoryDBItem = schedulerTaskHistoryDBItem;
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
    public void setIsIsAssigned(Boolean isAssigned) {
        this.isAssigned = isAssigned;
    }

    @Transient
    public void setIsAssigned(Boolean isAssigned) {
        this.isAssigned = isAssigned;
    }
    
    @Column(name = "`IS_ASSIGNED`", nullable = false)
    public Boolean getIsIsAssigned() {
        return isAssigned;
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
        if (this.isOrderJob()) {
            if (this.getSchedulerOrderHistoryDBItem() != null) {
                return getDateFormatted(this.getSchedulerOrderHistoryDBItem().getEndTime());
            } else {
                return "";
            }

        } else {
            if (this.getSchedulerTaskHistoryDBItem() != null) {
                return getDateFormatted(this.getSchedulerTaskHistoryDBItem().getEndTime());
            } else {
                return "";
            }

        }
    }

    @Transient
    public Date getEndTimeFromHistory() {
        if (this.isOrderJob()) {
            if (this.getSchedulerOrderHistoryDBItem() != null) {
                return this.getSchedulerOrderHistoryDBItem().getEndTime();
            } else {
                return null;
            }

        } else {
            if (this.getSchedulerTaskHistoryDBItem() != null) {
                return this.getSchedulerTaskHistoryDBItem().getEndTime();
            } else {
                return null;
            }

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
    public boolean isEqual(SchedulerOrderHistoryDBItem schedulerOrderHistoryDBItem) {
        String job_chain = this.getJobChain().replaceAll("^/", "");
        String job_chain2 = schedulerOrderHistoryDBItem.getJobChain().replaceAll("^/", "");
        return (this.getPlannedStart().equals(schedulerOrderHistoryDBItem.getStartTime()) 
                || this.getPlannedStart().before(schedulerOrderHistoryDBItem.getStartTime()))
                && job_chain.equalsIgnoreCase(job_chain2) && this.getOrderId().equalsIgnoreCase(schedulerOrderHistoryDBItem.getOrderId());
    }

    @Transient
    public boolean isEqual(SchedulerTaskHistoryDBItem schedulerHistoryDBItem) {
        String job = this.getJob().replaceAll("^/", "");
        String job2 = schedulerHistoryDBItem.getJobName().replaceAll("^/", "");
        return (this.getPlannedStart().equals(schedulerHistoryDBItem.getStartTime()) 
                || this.getPlannedStart().before(schedulerHistoryDBItem.getStartTime())) && job.equalsIgnoreCase(job2);
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
        String fromTimeZoneString = "UTC";
        DateTime dateTimePlannedInUtc = new DateTime(plannedStart);
        DateTime dateTimeExecutedInUtc = null;
        DateTime dateTimePeriodBeginInUtc = null;
        if (expectedEnd != null) {
            dateTimeExecutedInUtc = new DateTime(expectedEnd);
        }
        if (periodBegin != null) {
            dateTimePeriodBeginInUtc = new DateTime(periodBegin);
        }
        String toTimeZoneString = TimeZone.getDefault().getID();
        Date plannedLocal = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, dateTimePlannedInUtc);
        Date executedLocal = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, dateTimeExecutedInUtc);
        Date periodBeginLocal = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, dateTimePeriodBeginInUtc);
        this.executionState.setSchedulePlanned(plannedLocal);
        this.executionState.setScheduleExecuted(executedLocal);
        this.executionState.setPeriodBegin(periodBeginLocal);
        return executionState;
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
            return this.schedulerOrderHistoryDBItem != null && this.schedulerOrderHistoryDBItem.haveError();
        } else {
            return this.schedulerTaskHistoryDBItem != null && this.schedulerTaskHistoryDBItem.haveError();
        }
    }

    

}