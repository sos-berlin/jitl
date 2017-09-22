package com.sos.jitl.checkhistory.interfaces;

import com.sos.jitl.checkhistory.classes.WebserviceCredentials;

public interface IJobSchedulerHistory {
    
    public void setTimeLimit(String timeLimit);

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobName) throws Exception;

    public String getActHistoryObjectName();

    public void setRelativePath(String pathOfJob);
    
    public WebserviceCredentials getWebserviceCredentials();      
     

}
