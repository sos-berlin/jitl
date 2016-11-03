package com.sos.jitl.dailyplan;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ExecutionState {

    private Date schedulePlanned = null;
    private Date scheduleExecuted = null;
    private Date periodBegin = null;
    private int tolerance = 5;
    private int toleranceUnit = Calendar.MINUTE;

    public ExecutionState() {
        //
    }

    public void setPeriodBegin(Date periodBegin) {
        this.periodBegin = periodBegin;
    }

    public boolean isLate() {
        if (scheduleExecuted == null) {
            return schedulePlanned.before(new Date());
        } else {
            if (periodStart()) {
                return false;
            } else {
                Date now = schedulePlanned;
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(now);
                calendar.add(toleranceUnit, tolerance);
                Date scheduleToleranz = calendar.getTime();
                return scheduleExecuted.after(scheduleToleranz);
            }
        }
    }

    public boolean singleStart() {
        return periodBegin == null;
    }

    public boolean periodStart() {
        return periodBegin != null;
    }

    public String getLate() {
        if (isLate()) {
            return "late";
        } else {
            return "";
        }
    }

    public String getExecutionLateState() {
        String status = getExecutionState();
        if (isLate()) {
            status = status + ":late";
        }
        return status;
    }

    public String getExecutionState() {
        String status = "*";
        if (scheduleExecuted == null) {
            status = "waiting";
        } else {
            status = "executed";
        }
        return status;
    }

    public void setSchedulePlanned(Date schedulePlanned) {
        this.schedulePlanned = schedulePlanned;
    }

    public void setScheduleExecuted(Date scheduleExecuted) {
        this.scheduleExecuted = scheduleExecuted;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    public void setToleranceUnit(int toleranceUnit) {
        this.toleranceUnit = toleranceUnit;
    }

}