package com.sos.jitl.dailyplan.db;

import java.util.Date;
import javax.persistence.Transient;
import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.dailyplan.ExecutionState;
import com.sos.jitl.reporting.db.DBItemReportTask;

public class DailyPlanWithReportExecutionDBItem extends DbItem{

  
    private DailyPlanDBItem dailyPlanDbItem;
    private DBItemReportTask dbItemReportTask;
    private ExecutionState executionState;

    public DailyPlanWithReportExecutionDBItem(DailyPlanDBItem dailyPlanDbItem,DBItemReportTask dbItemReportTask) {
        super();
        this.dailyPlanDbItem = dailyPlanDbItem;
        this.dbItemReportTask = dbItemReportTask;
    }

    
    public DailyPlanDBItem getDailyPlanDbItem() {
        return dailyPlanDbItem;
    }

    public void setDailyPlanDbItem(DailyPlanDBItem dailyPlanDbItem) {
        this.dailyPlanDbItem = dailyPlanDbItem;
    }


    public DBItemReportTask getDbItemReportTask() {
        return dbItemReportTask;
    }


    public void setDbItemReportExecution(DBItemReportTask dbItemReportTask) {
        this.dbItemReportTask = dbItemReportTask;
    }
    
    @Transient
    public String getScheduleEndedFormated() {
        if (this.getDbItemReportTask() != null) {
            return getDateFormatted(this.getDbItemReportTask().getEndTime());
        } else {
            return "";
        }

    }

    @Transient
    public Date getEndTimeFromHistory() {
        if (this.getDbItemReportTask() != null) {
            return this.getDbItemReportTask().getEndTime();
        } else {
            return null;
        }
    }

    @Transient
    public ExecutionState getExecutionState() {
       // if (executionState != null) {
       //     return executionState;
       // } else {
            executionState = new ExecutionState();
            Date startTime = null;
            Date endTime = null;
                if (dbItemReportTask != null) {
                    endTime = dbItemReportTask.getEndTime();
                    startTime = dbItemReportTask.getStartTime();
                }
            this.executionState.setPlannedTime(dailyPlanDbItem.getPlannedStart());
            this.executionState.setEndTime(endTime);
            this.executionState.setStartTime(startTime);
            this.executionState.setPeriodBegin(dailyPlanDbItem.getPeriodBegin());
            this.executionState.setHaveError(this.haveError());
            return executionState;
       // }
    }

   

    @Transient
    public boolean haveError() {
            return this.dbItemReportTask != null && this.dbItemReportTask.haveError();
    }

    @Transient
    public String getDurationFormated() {
        return this.getDateDiff(this.dailyPlanDbItem.getExpectedEnd(), this.getEndTimeFromHistory());
    }
 
    @Transient
    public boolean isCompleted() {
            return (dbItemReportTask != null && dbItemReportTask.getStartTime() != null && dbItemReportTask.getEndTime() != null);
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
    public boolean isEqual(DBItemReportTask dbItemReportTask) {
        String job = normalizePath(this.dailyPlanDbItem.getJob());
        String job2 = normalizePath(dbItemReportTask.getName());
        return (this.dailyPlanDbItem.getPlannedStart().equals(dbItemReportTask.getStartTime()) || 
                this.dailyPlanDbItem.getPlannedStart().before(dbItemReportTask.getStartTime())) && job.equalsIgnoreCase(
                job2);
    }

    @Transient
    public void setExecutionState(ExecutionState executionState) {
        this.executionState = executionState;        
    }




}
