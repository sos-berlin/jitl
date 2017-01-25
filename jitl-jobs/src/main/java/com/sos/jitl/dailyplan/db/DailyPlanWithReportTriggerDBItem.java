package com.sos.jitl.dailyplan.db;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;
import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.dailyplan.ExecutionState;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.DBItemReportTriggerResult;
             

public class DailyPlanWithReportTriggerDBItem extends DbItem {

    private DailyPlanDBItem dailyPlanDbItem;
    private DBItemReportTrigger dbItemReportTrigger;
    private DBItemReportTriggerResult dbItemReportTriggerResult;
    private ExecutionState executionState;

    public DailyPlanWithReportTriggerDBItem(DailyPlanDBItem dailyPlanDbItem, DBItemReportTrigger dbItemReportTrigger, DBItemReportTriggerResult dbItemReportTriggerResult) {
        super();
        this.dailyPlanDbItem = dailyPlanDbItem;
        this.dbItemReportTrigger = dbItemReportTrigger;
        this.dbItemReportTriggerResult = dbItemReportTriggerResult;
    }

    public DBItemReportTriggerResult getDbItemReportTriggerResult() {
        return dbItemReportTriggerResult;
    }

    public void setDbItemReportTriggerResult(DBItemReportTriggerResult dbItemReportTriggerResult) {
        this.dbItemReportTriggerResult = dbItemReportTriggerResult;
    }

    public DBItemReportTrigger getDbItemReportTrigger() {
        return dbItemReportTrigger;
    }

    public void setDbItemReportTrigger(DBItemReportTrigger dbItemReportTrigger) {
        this.dbItemReportTrigger = dbItemReportTrigger;
    }

    public DailyPlanDBItem getDailyPlanDbItem() {
        return dailyPlanDbItem;
    }

    public void setDailyPlanDbItem(DailyPlanDBItem dailyPlanDbItem) {
        this.dailyPlanDbItem = dailyPlanDbItem;
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
    public ExecutionState getExecutionState() {
        if (executionState != null) {
            return executionState;
        } else {
            executionState = new ExecutionState();
            Date startTime = null;
            Date endTime = null;
            if (dbItemReportTrigger != null) {
                endTime = dbItemReportTrigger.getEndTime();
                startTime = dbItemReportTrigger.getStartTime();
            }
            this.executionState.setPlannedTime(dailyPlanDbItem.getPlannedStart());
            this.executionState.setEndTime(endTime);
            this.executionState.setStartTime(startTime);
            this.executionState.setPeriodBegin(dailyPlanDbItem.getPeriodBegin());
            this.executionState.setHaveError(this.haveError());
            return executionState;
        }
    }

    @Transient
    public boolean haveError() {
        return this.dbItemReportTrigger != null && this.dbItemReportTriggerResult != null && this.dbItemReportTriggerResult.getError();
    }

    @Transient
    public String getDurationFormated() {
        return this.getDateDiff(this.dailyPlanDbItem.getExpectedEnd(), this.getEndTimeFromHistory());
    }

    @Transient
    public boolean isCompleted() {
        return (dbItemReportTrigger != null && dbItemReportTrigger.getStartTime() != null && dbItemReportTrigger.getEndTime() != null);
    }

    @Transient
    public Integer getStartMode() {
        if (this.getExecutionState().singleStart()) {
            return 0;
        } else {
            if (this.dailyPlanDbItem.getStartStart()) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    @Transient
    public boolean isEqual(DBItemReportTrigger dbItemReportTrigger) {
        String job_chain = this.dailyPlanDbItem.getJobChain().replaceAll("^/", "");
        String job_chain2 = dbItemReportTrigger.getParentName().replaceAll("^/", "");
        return (this.dailyPlanDbItem.getPlannedStart().equals(dbItemReportTrigger.getStartTime()) || this.dailyPlanDbItem.getPlannedStart().before(dbItemReportTrigger
                .getStartTime())) && job_chain.equalsIgnoreCase(job_chain2) && this.dailyPlanDbItem.getOrderId().equalsIgnoreCase(dbItemReportTrigger.getName());
    }

    @Transient
    public void setExecutionState(ExecutionState executionState) {
        this.executionState = executionState;
    }

}
