package com.sos.jitl.checkrunhistory;

public interface IJobSchedulerHistoryInfo {

    public void setStartTime(String startTime);

    public void setEndTime(String endTime);

    public boolean queryHistory(String query);

    public JobSchedulerHistoryInfoEntry getRunning();

    public JobSchedulerHistoryInfoEntry getLastCompleted();

    public JobSchedulerHistoryInfoEntry getLastCompletedWithError();

    public JobSchedulerHistoryInfoEntry getLastCompletedSuccessful();
}
