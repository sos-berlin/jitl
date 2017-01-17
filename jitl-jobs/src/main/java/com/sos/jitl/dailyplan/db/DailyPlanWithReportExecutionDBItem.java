package com.sos.jitl.dailyplan.db;

import java.util.Date;
import javax.persistence.Transient;
import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.dailyplan.ExecutionState;
import com.sos.jitl.reporting.db.DBItemReportExecution;

public class DailyPlanWithReportExecutionDBItem extends DbItem{

  
    private DailyPlanDBItem dailyPlanDbItem;
    private DBItemReportExecution dbItemReportExecution;
    private ExecutionState executionState;

    public DailyPlanWithReportExecutionDBItem(DailyPlanDBItem dailyPlanDbItem,DBItemReportExecution dbItemReportExecution) {
        super();
        this.dailyPlanDbItem = dailyPlanDbItem;
        this.dbItemReportExecution = dbItemReportExecution;
    }

    
    public DailyPlanDBItem getDailyPlanDbItem() {
        return dailyPlanDbItem;
    }

    public void setDailyPlanDbItem(DailyPlanDBItem dailyPlanDbItem) {
        this.dailyPlanDbItem = dailyPlanDbItem;
    }


    public DBItemReportExecution getDbItemReportExecution() {
        return dbItemReportExecution;
    }


    public void setDbItemReportExecution(DBItemReportExecution dbItemReportExecution) {
        this.dbItemReportExecution = dbItemReportExecution;
    }
    
    @Transient
    public String getScheduleEndedFormated() {
        if (this.getDbItemReportExecution() != null) {
            return getDateFormatted(this.getDbItemReportExecution().getEndTime());
        } else {
            return "";
        }

    }

    @Transient
    public Date getEndTimeFromHistory() {
        if (this.getDbItemReportExecution() != null) {
            return this.getDbItemReportExecution().getEndTime();
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
                if (dbItemReportExecution != null) {
                    endTime = dbItemReportExecution.getEndTime();
                    startTime = dbItemReportExecution.getStartTime();
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
            return this.dbItemReportExecution != null && this.dbItemReportExecution.haveError();
    }

    @Transient
    public String getDurationFormated() {
        return this.getDateDiff(this.dailyPlanDbItem.getExpectedEnd(), this.getEndTimeFromHistory());
    }
 
    @Transient
    public boolean isCompleted() {
            return (dbItemReportExecution != null && dbItemReportExecution.getStartTime() != null && dbItemReportExecution.getEndTime() != null);
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
    public boolean isEqual(DBItemReportExecution dbItemReportExecution) {
        String job = normalizePath(this.dailyPlanDbItem.getJob());
        String job2 = normalizePath(dbItemReportExecution.getName());
        return (this.dailyPlanDbItem.getPlannedStart().equals(dbItemReportExecution.getStartTime()) || 
                this.dailyPlanDbItem.getPlannedStart().before(dbItemReportExecution.getStartTime())) && job.equalsIgnoreCase(
                job2);
    }

    @Transient
    public void setExecutionState(ExecutionState executionState) {
        this.executionState = executionState;        
    }




}
