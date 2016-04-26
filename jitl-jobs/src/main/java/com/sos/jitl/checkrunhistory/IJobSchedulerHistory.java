package com.sos.jitl.checkrunhistory;

public interface IJobSchedulerHistory {

    public void setTimeLimit(String timeLimit);

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobName) throws Exception;

    public int getNumberOfCompleted();

    public int getNumberOfStarts();

    public int getNumberOfCompletedSuccessful();

    public int getNumberOfCompletedWithError();

    public int getCount();
    
    public void setRelativePath(String relativePath_);
    
    public String getActHistoryObjectName();

}
