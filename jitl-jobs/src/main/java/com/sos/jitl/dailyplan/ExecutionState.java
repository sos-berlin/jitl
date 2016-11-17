package com.sos.jitl.dailyplan;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bouncycastle.asn1.isismtt.ISISMTTObjectIdentifiers;

public class ExecutionState {

    private Date plannedTime = null;
    private Date endTime = null;
    private Date startTime = null;
    private Date periodBegin = null;
    private int tolerance = 2;
    private int toleranceUnit = Calendar.MINUTE;
    private boolean haveError;

    public void setHaveError(boolean haveError) {
        this.haveError = haveError;
    }

    public ExecutionState() {
    }

    public void setPeriodBegin(Date periodBegin) {
        this.periodBegin = periodBegin;
    }

    public boolean isLate() {
        if (startTime == null) {
            return plannedTime.before(new Date());
        } else {
            if (periodStart()) {
                return false;
            } else {
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(plannedTime);
                calendar.add(toleranceUnit, tolerance);
                Date scheduleToleranz = calendar.getTime();
                return startTime.after(scheduleToleranz);
            }
        }
    }

    private boolean isSuccessful() {
        return (this.endTime != null && !this.haveError);
    }

    private boolean isFailed() {
        return (this.endTime != null && this.haveError);
    }

    private boolean isPlanned() {
        return (this.endTime == null && this.startTime == null);
    }

    private boolean isIncomplete() {
        return (this.endTime == null && this.startTime != null);
    }

    public String getState() {
        if (isSuccessful()) {
            return "SUCCESSFUL";
        }
        if (isFailed()) {
            return "FAILED";
        }
        if (isIncomplete()) {
            return "INCOMPLETE";
        }
        if (isPlanned()) {
            return "PLANNED";
        }
        return null;
    }

    public boolean singleStart() {
        return periodBegin == null;
    }

    public boolean periodStart() {
        return periodBegin != null;
    }

    public void setPlannedTime(Date plannedTime) {
        this.plannedTime = plannedTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    public void setToleranceUnit(int toleranceUnit) {
        this.toleranceUnit = toleranceUnit;
    }

}