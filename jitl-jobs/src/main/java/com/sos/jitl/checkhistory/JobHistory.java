package com.sos.jitl.checkhistory;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSException;
import com.sos.jitl.checkhistory.classes.HistoryDataSource;
import com.sos.jitl.checkhistory.classes.HistoryDatabaseExecuter;
import com.sos.jitl.checkhistory.classes.HistoryWebserviceExecuter;
import com.sos.jitl.checkhistory.interfaces.IJobSchedulerHistory;
import com.sos.jitl.checkhistory.interfaces.IJobSchedulerHistoryInfo;
import com.sos.jitl.restclient.WebserviceCredentials;
import com.sos.scheduler.model.answers.HistoryEntry;

import sos.spooler.Spooler;

public class JobHistory implements IJobSchedulerHistory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobHistory.class);

    private HistoryEntry lastCompletedHistoryEntry = null;
    private HistoryEntry lastRunningHistoryEntry = null;
    private HistoryEntry lastCompletedSuccessfullHistoryEntry = null;
    private HistoryEntry lastCompletedWithErrorHistoryEntry = null;
    private String timeLimit;
    private HistoryHelper jobHistoryHelper;
    private String actHistoryObjectName = "";
    private String relativePath;
    private WebserviceCredentials webserviceCredentials;
    private HistoryDataSource historyDatasourceExecuter;

    public JobHistory(WebserviceCredentials webserviceCredentials) {
        super();

        jobHistoryHelper = new HistoryHelper();
        this.webserviceCredentials = webserviceCredentials;
        timeLimit = "";
    }

    // For calling from javascript
    public JobHistory(Spooler spooler) {
        super();

        jobHistoryHelper = new HistoryHelper();
        this.webserviceCredentials = new WebserviceCredentials();
        String jocUser = spooler.variables().value("joc_user");
        this.webserviceCredentials.setJocUrl(spooler.variables().value("joc_url"));
        this.webserviceCredentials.setAccessToken(spooler.variables().value(jocUser + "_X-Access-Token"));
        this.webserviceCredentials.setUser(jocUser);
        this.webserviceCredentials.setKeyPassword(spooler.variables().value("key_password"));
        this.webserviceCredentials.setKeyStorePassword(spooler.variables().value("keystore_password"));
        this.webserviceCredentials.setKeyStorePath(spooler.variables().value("keystore_path"));
        this.webserviceCredentials.setKeyStoreType(spooler.variables().value("keystore_type"));
        this.webserviceCredentials.setTrustStorePassword(spooler.variables().value("truststore_password"));
        this.webserviceCredentials.setTrustStorePath(spooler.variables().value("truststore_path"));
        this.webserviceCredentials.setTrustStoreType(spooler.variables().value("truststore_type"));

        this.webserviceCredentials.setSchedulerId(spooler.id());
        timeLimit = "";
    }

    public JobSchedulerHistoryInfo getJobInfo(String jobName) throws Exception {
        try {
            return (JobSchedulerHistoryInfo) getJobSchedulerHistoryInfo(jobName);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public JobSchedulerHistoryInfo getJobInfo(String jobName, String timeLimit_) throws Exception {
        try {
            return (JobSchedulerHistoryInfo) getJobSchedulerHistoryInfo(jobName, timeLimit_);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobName) throws Exception {
        lastCompletedHistoryEntry = null;
        lastRunningHistoryEntry = null;
        lastCompletedSuccessfullHistoryEntry = null;
        lastCompletedWithErrorHistoryEntry = null;
        return jobSchedulerHistoryInfo(jobName);
    }

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobName, String timeLimit_) throws Exception {
        lastCompletedHistoryEntry = null;
        lastRunningHistoryEntry = null;
        lastCompletedSuccessfullHistoryEntry = null;
        lastCompletedWithErrorHistoryEntry = null;
        timeLimit = timeLimit_;
        return jobSchedulerHistoryInfo(jobName);
    }

    public IJobSchedulerHistoryInfo jobSchedulerHistoryInfo(String jobName) throws Exception {
        getHistoryByWebServiceCall(jobName);

        JobSchedulerHistoryInfo jobHistoryInfo = new JobSchedulerHistoryInfo();
        if (lastCompletedHistoryEntry != null) {
            jobHistoryInfo.lastCompleted.found = true;
            jobHistoryInfo.lastCompleted.top = (lastRunningHistoryEntry == null || lastRunningHistoryEntry.getTaskId() == null
                    || lastRunningHistoryEntry.getTaskId().compareTo(lastCompletedHistoryEntry.getTaskId()) <= 0);
            jobHistoryInfo.lastCompleted.errorMessage = lastCompletedHistoryEntry.getErrorText();
            jobHistoryInfo.lastCompleted.executionResult = jobHistoryHelper.big2int(lastCompletedHistoryEntry.getExitCode());
            jobHistoryInfo.lastCompleted.start = jobHistoryHelper.getDateFromString(lastCompletedHistoryEntry.getStartTime());
            jobHistoryInfo.lastCompleted.end = jobHistoryHelper.getDateFromString(lastCompletedHistoryEntry.getEndTime());
            jobHistoryInfo.lastCompleted.error = jobHistoryHelper.big2int(lastCompletedHistoryEntry.getError());
            jobHistoryInfo.lastCompleted.errorCode = lastCompletedHistoryEntry.getErrorCode();
            jobHistoryInfo.lastCompleted.id = jobHistoryHelper.big2int(lastCompletedHistoryEntry.getId());
            jobHistoryInfo.lastCompleted.jobName = lastCompletedHistoryEntry.getJobName();
            jobHistoryInfo.lastCompleted.duration = jobHistoryHelper.getDuration(jobHistoryInfo.lastCompleted.start,
                    jobHistoryInfo.lastCompleted.end);
        } else {
            jobHistoryInfo.lastCompleted.found = false;
            LOGGER.debug(String.format("no completed job run found for the job:%s", jobName));
        }
        if (lastCompletedSuccessfullHistoryEntry != null) {
            jobHistoryInfo.lastCompletedSuccessful.found = true;
            jobHistoryInfo.lastCompletedSuccessful.top = (lastRunningHistoryEntry == null || lastRunningHistoryEntry.getTaskId() == null
                    || lastRunningHistoryEntry.getTaskId().compareTo(lastCompletedSuccessfullHistoryEntry.getTaskId()) <= 0)
                    && (lastCompletedWithErrorHistoryEntry == null || lastCompletedWithErrorHistoryEntry.getTaskId() == null
                            || lastCompletedWithErrorHistoryEntry.getTaskId().compareTo(lastCompletedSuccessfullHistoryEntry.getTaskId()) <= 0);
            jobHistoryInfo.lastCompletedSuccessful.errorMessage = lastCompletedSuccessfullHistoryEntry.getErrorText();
            jobHistoryInfo.lastCompletedSuccessful.executionResult = jobHistoryHelper.big2int(lastCompletedSuccessfullHistoryEntry.getExitCode());
            jobHistoryInfo.lastCompletedSuccessful.start = jobHistoryHelper.getDateFromString(lastCompletedSuccessfullHistoryEntry.getStartTime());
            jobHistoryInfo.lastCompletedSuccessful.end = jobHistoryHelper.getDateFromString(lastCompletedSuccessfullHistoryEntry.getEndTime());
            jobHistoryInfo.lastCompletedSuccessful.error = jobHistoryHelper.big2int(lastCompletedSuccessfullHistoryEntry.getError());
            jobHistoryInfo.lastCompletedSuccessful.errorCode = lastCompletedSuccessfullHistoryEntry.getErrorCode();
            jobHistoryInfo.lastCompletedSuccessful.id = jobHistoryHelper.big2int(lastCompletedSuccessfullHistoryEntry.getId());
            jobHistoryInfo.lastCompletedSuccessful.jobName = lastCompletedSuccessfullHistoryEntry.getJobName();
            jobHistoryInfo.lastCompletedSuccessful.duration = jobHistoryHelper.getDuration(jobHistoryInfo.lastCompletedSuccessful.start,
                    jobHistoryInfo.lastCompletedSuccessful.end);
        } else {
            jobHistoryInfo.lastCompletedSuccessful.found = false;
            LOGGER.debug(String.format("no successfull job run found for the job:%s", jobName));
        }
        if (lastCompletedWithErrorHistoryEntry != null) {
            jobHistoryInfo.lastCompletedWithError.found = true;
            jobHistoryInfo.lastCompletedWithError.top = (lastRunningHistoryEntry == null || lastRunningHistoryEntry.getTaskId() == null
                    || lastRunningHistoryEntry.getTaskId().compareTo(lastCompletedWithErrorHistoryEntry.getTaskId()) <= 0)
                    && (lastCompletedSuccessfullHistoryEntry == null || lastCompletedSuccessfullHistoryEntry.getTaskId() == null
                            || lastCompletedSuccessfullHistoryEntry.getTaskId().compareTo(lastCompletedWithErrorHistoryEntry.getTaskId()) <= 0);
            jobHistoryInfo.lastCompletedWithError.errorMessage = lastCompletedWithErrorHistoryEntry.getErrorText();
            jobHistoryInfo.lastCompletedWithError.executionResult = jobHistoryHelper.big2int(lastCompletedWithErrorHistoryEntry.getExitCode());
            jobHistoryInfo.lastCompletedWithError.start = jobHistoryHelper.getDateFromString(lastCompletedWithErrorHistoryEntry.getStartTime());
            jobHistoryInfo.lastCompletedWithError.end = jobHistoryHelper.getDateFromString(lastCompletedWithErrorHistoryEntry.getEndTime());
            jobHistoryInfo.lastCompletedWithError.error = jobHistoryHelper.big2int(lastCompletedWithErrorHistoryEntry.getError());
            jobHistoryInfo.lastCompletedWithError.errorCode = lastCompletedWithErrorHistoryEntry.getErrorCode();
            jobHistoryInfo.lastCompletedWithError.id = jobHistoryHelper.big2int(lastCompletedWithErrorHistoryEntry.getId());
            jobHistoryInfo.lastCompletedWithError.jobName = lastCompletedWithErrorHistoryEntry.getJobName();
            jobHistoryInfo.lastCompletedWithError.duration = jobHistoryHelper.getDuration(jobHistoryInfo.lastCompletedWithError.start,
                    jobHistoryInfo.lastCompletedWithError.end);
        } else {
            jobHistoryInfo.lastCompletedWithError.found = false;
            LOGGER.debug(String.format("no job runs with error found for the job:%s", jobName));
        }
        if (lastRunningHistoryEntry != null) {
            jobHistoryInfo.running.found = true;
            jobHistoryInfo.running.top = !jobHistoryInfo.lastCompleted.top;
            jobHistoryInfo.running.errorMessage = lastRunningHistoryEntry.getErrorText();
            jobHistoryInfo.running.executionResult = jobHistoryHelper.big2int(lastRunningHistoryEntry.getExitCode());
            jobHistoryInfo.running.start = jobHistoryHelper.getDateFromString(lastRunningHistoryEntry.getStartTime());
            jobHistoryInfo.running.end = null;
            jobHistoryInfo.running.error = jobHistoryHelper.big2int(lastRunningHistoryEntry.getError());
            jobHistoryInfo.running.errorCode = lastRunningHistoryEntry.getErrorCode();
            jobHistoryInfo.running.id = jobHistoryHelper.big2int(lastRunningHistoryEntry.getId());
            jobHistoryInfo.running.jobName = lastRunningHistoryEntry.getJobName();
            jobHistoryInfo.running.duration = "";
        } else {
            jobHistoryInfo.running.found = false;
            LOGGER.debug(String.format("no running jobs found for the job:%s", jobName));
        }
        return jobHistoryInfo;
    }

    private void createDatasource() throws SOSException, URISyntaxException {

        if (historyDatasourceExecuter == null) {
            historyDatasourceExecuter = new HistoryWebserviceExecuter(this.webserviceCredentials);
            historyDatasourceExecuter.login(webserviceCredentials.getAccessToken());
        }
    }

    private void getHistoryByWebServiceCall(String jobName) throws Exception {
        jobName = this.jobHistoryHelper.normalizePath(relativePath, jobName);
        actHistoryObjectName = jobName;

        createDatasource();

        historyDatasourceExecuter.setTimeLimit(timeLimit);
        historyDatasourceExecuter.setJobName(jobName);
        historyDatasourceExecuter.setSchedulerId(webserviceCredentials.getSchedulerId());

        lastCompletedSuccessfullHistoryEntry = historyDatasourceExecuter.getLastCompletedSuccessfullJobHistoryEntry();
        lastCompletedHistoryEntry = historyDatasourceExecuter.getLastCompletedJobHistoryEntry();
        lastCompletedWithErrorHistoryEntry = historyDatasourceExecuter.getLastCompletedWithErrorJobHistoryEntry();
        lastRunningHistoryEntry = historyDatasourceExecuter.getLastRunningJobHistoryEntry();
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
    }

    @Override
    public String getActHistoryObjectName() {
        return actHistoryObjectName;
    }

    @Override
    public void setRelativePath(String relativePath_) {
        if (!relativePath_.startsWith("/")) {
            relativePath_ = "/" + relativePath_;
        }
        relativePath_ = relativePath_.replace('\\', '/');
        this.relativePath = relativePath_;
    }

    @Override
    public WebserviceCredentials getWebserviceCredentials() {
        return this.webserviceCredentials;
    }

    public void setHistoryDatasourceExecuter(HistoryDataSource historyDatasourceExecuter) {
        this.historyDatasourceExecuter = historyDatasourceExecuter;
    }

}
