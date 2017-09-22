package com.sos.jitl.checkhistory.interfaces;

import com.sos.jitl.checkhistory.classes.JobSchedulerHistoryInfoEntry;

public interface IJobSchedulerHistoryInfo {

    public void setEndTime(String endTime);

    public void setStartTime(String endTime);

    public boolean queryHistory(String query);

    public JobSchedulerHistoryInfoEntry getRunning();

    public JobSchedulerHistoryInfoEntry getLastCompleted();

    public JobSchedulerHistoryInfoEntry getLastCompletedWithError();

    public JobSchedulerHistoryInfoEntry getLastCompletedSuccessful();

}
