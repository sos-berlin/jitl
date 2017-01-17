package com.sos.jitl.dailyplan.db;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.*;
import org.hibernate.annotations.Type;
import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.reporting.db.DBLayer;

@Entity
@Table(name = "DAILY_PLAN")
@SequenceGenerator(name = DBLayer.TABLE_DAILY_PLAN_SEQUENCE, sequenceName = DBLayer.TABLE_DAILY_PLAN_SEQUENCE, allocationSize = 1)

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
    private String dateFormat = "yyyy-MM-dd hh:mm";

    public DailyPlanDBItem(String dateFormat_) {
        this.dateFormat = dateFormat_;
    }

    public DailyPlanDBItem() {

    }


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO,generator = DBLayer.TABLE_DAILY_PLAN_SEQUENCE)
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

    @Column(name = "`ORDER_ID`", nullable = true)
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Column(name = "`ORDER_ID`", nullable = true)
    public String getOrderId() {
        return orderId;
    }

    @Column(name = "`JOB_CHAIN`", nullable = true)
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    @Column(name = "`JOB_CHAIN`", nullable = true)
    public String getJobChain() {
        return jobChain;
    }


    @Column(name = "`IS_ASSIGNED`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setIsAssigned(Boolean isAssigned) {
        this.isAssigned = isAssigned;
    }

    @Column(name = "`IS_ASSIGNED`", nullable = false)
    @Type(type = "numeric_boolean")
    public Boolean getIsAssigned() {
        return isAssigned;
    }

    @Column(name = "`IS_LATE`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setIsLate(Boolean isLate) {
        this.isLate = isLate;
    }

    @Column(name = "`IS_LATE`", nullable = false)
    @Type(type = "numeric_boolean")
    public Boolean getIsLate() {
        return isLate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`PLANNED_START`", nullable = true)
    public void setPlannedStart(Date plannedStart) {
        this.plannedStart = plannedStart;
    }
    

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`PLANNED_START`", nullable = true)
    public Date getPlannedStart() {
        return plannedStart;
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
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`PERIOD_END`", nullable = true)
    public Date getPeriodEnd() {
        return periodEnd;
    }

    @Column(name = "`START_START`", nullable = true)
    @Type(type = "numeric_boolean")
    public void setStartStart(Boolean startStart) {
        this.startStart = startStart;
    }

    @Column(name = "`START_START`", nullable = true)
    @Type(type = "numeric_boolean")
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
    public void setPlannedStart(String plannedStart) throws ParseException {
        DailyPlanDate dailyScheduleDate = new DailyPlanDate(dateFormat);
        dailyScheduleDate.setSchedule(plannedStart);
        this.setPlannedStart(dailyScheduleDate.getSchedule());
    }

    @Transient
    public void setPeriodBegin(String periodBegin) throws ParseException {
        DailyPlanDate daysScheduleDate = new DailyPlanDate(dateFormat);
        daysScheduleDate.setSchedule(periodBegin);
        this.setPeriodBegin(daysScheduleDate.getSchedule());
        this.setPlannedStart(this.getPeriodBegin());
    }    
    
    @Transient
    public void setPeriodEnd(String periodEnd) throws ParseException {
        DailyPlanDate daysScheduleDate = new DailyPlanDate(dateFormat);
        daysScheduleDate.setSchedule(periodEnd);
        this.setPeriodEnd(daysScheduleDate.getSchedule());
    }    
    
    @Transient
    public void setRepeatInterval(BigInteger absolutRepeat_, BigInteger repeat_) {
        BigInteger r = BigInteger.ZERO;
        Long l = Long.valueOf(0);
        if (absolutRepeat_ != null && !absolutRepeat_.equals(BigInteger.ZERO)) {
            r = absolutRepeat_;
            this.setStartStart(true);
            if (r != null) {
                l = Long.valueOf(r.longValue());
            }
        } else {
            r = repeat_;
            this.setStartStart(false);
            if (r != null) {
                l = Long.valueOf(r.longValue());
            }
        }
        this.setRepeatInterval(l);
    }    
    
    @Transient
    public boolean isOrderJob() {
        return !this.isStandalone();
    }

    @Transient
    public boolean isStandalone() {
        return this.getJob() != null && !"".equals(this.getJob()) && 
                (this.getJobChain() == null || "".equals(this.getJobChain()));
    }
    @Transient
    public String getJobNotNull() {
        return null2Blank(getJob());
    }

    @Transient
    public String getOrderIdNotNull() {
        return null2Blank(getOrderId());
    }

    @Transient
    public String getJobChainNotNull() {
        return null2Blank(getJobChain());
    }

    @Transient
    public String getJobOrJobchain() {
        if (this.isOrderJob()) {
            return null2Blank(String.format("%s(%s)", getJobChainNotNull(), getOrderIdNotNull()));
        } else {
            return null2Blank(getJobNotNull());
        }
    }

    @Transient
    public void setScheduleExecuted(String scheduleExecuted) throws ParseException {
        DailyPlanDate dailyScheduleDate = new DailyPlanDate(dateFormat);
        dailyScheduleDate.setSchedule(scheduleExecuted);
        this.setExpectedEnd(dailyScheduleDate.getSchedule());
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
    public String getName() {
        if (isStandalone()) {
            return this.getJob();
        } else {
            return this.getJobChain() + "/" + this.getOrderId();
        }
    }
 
}