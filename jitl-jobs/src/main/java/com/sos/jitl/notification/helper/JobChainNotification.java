package com.sos.jitl.notification.helper;

import java.util.List;

import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;

public class JobChainNotification {

    private DBItemSchedulerMonNotifications lastStepForNotification;
    private DBItemSchedulerMonNotifications lastStep;
    private DBItemSchedulerMonNotifications stepFrom;
    private DBItemSchedulerMonNotifications stepTo;
    private List<DBItemSchedulerMonNotifications> steps;
    private Long stepFromIndex = new Long(0);
    private Long stepToIndex = new Long(0);

    public DBItemSchedulerMonNotifications getLastStepForNotification() {
        return lastStepForNotification;
    }

    public void setLastStepForNotification(DBItemSchedulerMonNotifications step) {
        lastStepForNotification = step;
    }

    public DBItemSchedulerMonNotifications getLastStep() {
        return lastStep;
    }

    public void setLastStep(DBItemSchedulerMonNotifications step) {
        lastStep = step;
    }

    public DBItemSchedulerMonNotifications getStepFrom() {
        return stepFrom;
    }

    public void setStepFrom(DBItemSchedulerMonNotifications step) {
        stepFrom = step;
    }

    public DBItemSchedulerMonNotifications getStepTo() {
        return stepTo;
    }

    public void setStepTo(DBItemSchedulerMonNotifications step) {
        stepTo = step;
    }

    public List<DBItemSchedulerMonNotifications> getSteps() {
        return steps;
    }

    public void setSteps(List<DBItemSchedulerMonNotifications> st) {
        steps = st;
    }

    public Long getStepFromIndex() {
        return stepFromIndex;
    }

    public void setStepFromIndex(Long index) {
        stepFromIndex = index;
    }

    public Long getStepToIndex() {
        return stepToIndex;
    }

    public void setStepToIndex(Long index) {
        stepToIndex = index;
    }

}
