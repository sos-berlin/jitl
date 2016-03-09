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

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_REPORT_EXECUTION_DATES)
@SequenceGenerator(name = DBLayer.TABLE_REPORT_EXECUTION_DATES_SEQUENCE, sequenceName = DBLayer.TABLE_REPORT_EXECUTION_DATES_SEQUENCE, allocationSize = 1)
public class DBItemReportExecutionDate extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;

    /** Foreign key REPORT_TRIGGERS.ID or REPORT_EXECUTIONS.ID */
    private Long referenceId;

    /** Others */
    private String schedulerId;
    private Long historyId;

    /** 0 - REPORT_TRIGGERS, 1 - REPORT_EXECUTIONS */
    private Long referenceType;
    private Long startDay;
    private Long startWeek;
    private Long startMonth;
    private Long startQuarter;
    private Long startYear;
    private Long endDay;
    private Long endWeek;
    private Long endMonth;
    private Long endQuarter;
    private Long endYear;
    private Date created;
    private Date modified;

    public DBItemReportExecutionDate() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORT_EXECUTION_DATES_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORT_EXECUTION_DATES_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    /** Foreign key REPORT_TRIGGERS.ID or REPORT_EXECUTIONS.ID */
    @Column(name = "`REFERENCE_ID`", nullable = false)
    public Long getReferenceId() {
        return this.referenceId;
    }

    @Column(name = "`REFERENCE_ID`", nullable = false)
    public void setReferenceId(Long val) {
        this.referenceId = val;
    }

    /** Others */
    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public String getSchedulerId() {
        return this.schedulerId;
    }

    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }

    @Column(name = "`HISTORY_ID`", nullable = false)
    public void setHistoryId(Long val) {
        this.historyId = val;
    }

    @Column(name = "`HISTORY_ID`", nullable = false)
    public Long getHistoryId() {
        return this.historyId;
    }

    /** 0 - REPORT_TRIGGERS, 1 - REPORT_EXECUTIONS */
    @Column(name = "`REFERENCE_TYPE`", nullable = false)
    public Long getReferenceType() {
        return this.referenceType;
    }

    @Column(name = "`REFERENCE_TYPE`", nullable = false)
    public void setReferenceType(Long val) {
        this.referenceType = val;
    }

    @Column(name = "`START_DAY`", nullable = false)
    public void setStartDay(Long val) {
        this.startDay = val;
    }

    @Column(name = "`START_DAY`", nullable = false)
    public Long getStartDay() {
        return this.startDay;
    }

    @Column(name = "`START_WEEK`", nullable = false)
    public void setStartWeek(Long val) {
        this.startWeek = val;
    }

    @Column(name = "`START_WEEK`", nullable = false)
    public Long getStartWeek() {
        return this.startWeek;
    }

    @Column(name = "`START_MONTH`", nullable = false)
    public void setStartMonth(Long val) {
        this.startMonth = val;
    }

    @Column(name = "`START_MONTH`", nullable = false)
    public Long getStartMonth() {
        return this.startMonth;
    }

    @Column(name = "`START_QUARTER`", nullable = false)
    public void setStartQuarter(Long val) {
        this.startQuarter = val;
    }

    @Column(name = "`START_QUARTER`", nullable = false)
    public Long getStartQuarter() {
        return this.startQuarter;
    }

    @Column(name = "`START_YEAR`", nullable = false)
    public void setStartYear(Long val) {
        this.startYear = val;
    }

    @Column(name = "`START_YEAR`", nullable = false)
    public Long getStartYear() {
        return this.startYear;
    }

    @Column(name = "`END_DAY`", nullable = false)
    public void setEndDay(Long val) {
        this.endDay = val;
    }

    @Column(name = "`END_DAY`", nullable = false)
    public Long getEndDay() {
        return this.endDay;
    }

    @Column(name = "`END_WEEK`", nullable = false)
    public void setEndWeek(Long val) {
        this.endWeek = val;
    }

    @Column(name = "`END_WEEK`", nullable = false)
    public Long getEndWeek() {
        return this.endWeek;
    }

    @Column(name = "`END_MONTH`", nullable = false)
    public void setEndMonth(Long val) {
        this.endMonth = val;
    }

    @Column(name = "`END_MONTH`", nullable = false)
    public Long getEndMonth() {
        return this.endMonth;
    }

    @Column(name = "`END_QUARTER`", nullable = false)
    public void setEndQuarter(Long val) {
        this.endQuarter = val;
    }

    @Column(name = "`END_QUARTER`", nullable = false)
    public Long getEndQuarter() {
        return this.endQuarter;
    }

    @Column(name = "`END_YEAR`", nullable = false)
    public void setEndYear(Long val) {
        this.endYear = val;
    }

    @Column(name = "`END_YEAR`", nullable = false)
    public Long getEndYear() {
        return this.endYear;
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
