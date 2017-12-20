package com.sos.jitl.checkhistory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.checkhistory.classes.HistoryWebserviceExecuter;
import com.sos.jitl.checkhistory.classes.WebserviceCredentials;
import com.sos.jitl.checkhistory.interfaces.IJobSchedulerHistory;
import com.sos.jitl.checkhistory.interfaces.IJobSchedulerHistoryInfo;
import com.sos.scheduler.model.answers.JobChain;

import sos.spooler.Spooler;

public class JobChainHistory implements IJobSchedulerHistory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobHistory.class);

    private String jocUrl;

    private JobChain.OrderHistory.Order lastCompletedHistoryEntry = null;
    private JobChain.OrderHistory.Order lastRunningHistoryEntry = null;
    private JobChain.OrderHistory.Order lastCompletedSuccessfullHistoryEntry = null;
    private JobChain.OrderHistory.Order lastCompletedWithErrorHistoryEntry = null;
    private String timeLimit;
    private historyHelper jobHistoryHelper;
    private String actHistoryObjectName = "";
    private String relativePath;
    private WebserviceCredentials webserviceCredentials;
    private HistoryWebserviceExecuter historyWebserviceExecuter;

    public JobChainHistory(String jocUrl, WebserviceCredentials webserviceCredentials) {
        super();

        jobHistoryHelper = new historyHelper();
        this.jocUrl = jocUrl;
        this.webserviceCredentials = webserviceCredentials;
        timeLimit = "";
    }

    public JobChainHistory(Spooler spooler) {
        super();

        jobHistoryHelper = new historyHelper();
        this.jocUrl = spooler.variables().value("joc_url");
        this.webserviceCredentials = new WebserviceCredentials();
        this.webserviceCredentials.setAccessToken(spooler.variables().value("X-Access-Token"));
        this.webserviceCredentials.setSchedulerId(spooler.id());
        timeLimit = "";
    }

    public JobSchedulerHistoryInfo getJobChainInfo(String jobChainName) throws Exception {
        return (JobSchedulerHistoryInfo) getJobSchedulerHistoryInfo(jobChainName);
    }

    public JobSchedulerHistoryInfo getJobChainInfo(String jobChainName, String timeLimit_) throws Exception {
        return (JobSchedulerHistoryInfo) getJobSchedulerHistoryInfo(jobChainName, timeLimit_);
    }

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobChainName) throws Exception {
        lastCompletedHistoryEntry = null;
        lastRunningHistoryEntry = null;
        lastCompletedSuccessfullHistoryEntry = null;
        lastCompletedWithErrorHistoryEntry = null;
        return jobSchedulerHistoryInfo(jobChainName);
    }

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobChainName, String timeLimit_) throws Exception {
        lastCompletedHistoryEntry = null;
        lastRunningHistoryEntry = null;
        lastCompletedSuccessfullHistoryEntry = null;
        lastCompletedWithErrorHistoryEntry = null;
        timeLimit = timeLimit_;
        return jobSchedulerHistoryInfo(jobChainName);
    }

    public IJobSchedulerHistoryInfo jobSchedulerHistoryInfo(String jobChainName) throws Exception {
        getHistoryByWebServiceCall(jobChainName);

        JobSchedulerHistoryInfo jobChainHistoryInfo = new JobSchedulerHistoryInfo();
        if (lastCompletedHistoryEntry != null) {
            jobChainHistoryInfo.lastCompleted.found = true;
            jobChainHistoryInfo.lastCompleted.orderId = lastCompletedHistoryEntry.getOrder();
            jobChainHistoryInfo.lastCompleted.state = lastCompletedHistoryEntry.getState();
            jobChainHistoryInfo.lastCompleted.top = (lastRunningHistoryEntry == null || lastRunningHistoryEntry.getHistoryId() == null || lastRunningHistoryEntry.getHistoryId()
                    .compareTo(lastCompletedHistoryEntry.getHistoryId()) <= 0);
            jobChainHistoryInfo.lastCompleted.start = jobHistoryHelper.getDateFromString(lastCompletedHistoryEntry.getStartTime());
            jobChainHistoryInfo.lastCompleted.end = jobHistoryHelper.getDateFromString(lastCompletedHistoryEntry.getEndTime());
            jobChainHistoryInfo.lastCompleted.id = jobHistoryHelper.big2int(lastCompletedHistoryEntry.getHistoryId());
            jobChainHistoryInfo.lastCompleted.jobChainName = lastCompletedHistoryEntry.getJobChain();
            jobChainHistoryInfo.lastCompleted.duration = jobHistoryHelper.getDuration(jobChainHistoryInfo.lastCompleted.start, jobChainHistoryInfo.lastCompleted.end);

        } else {
            jobChainHistoryInfo.lastCompleted.found = false;
            LOGGER.debug(String.format("no completed job chain run found for the job chain:%s", jobChainName));
        }
        if (lastCompletedSuccessfullHistoryEntry != null) {
            jobChainHistoryInfo.lastCompletedSuccessful.found = true;
            jobChainHistoryInfo.lastCompletedSuccessful.orderId = lastCompletedSuccessfullHistoryEntry.getOrder();
            jobChainHistoryInfo.lastCompletedSuccessful.state = lastCompletedSuccessfullHistoryEntry.getState();
            jobChainHistoryInfo.lastCompletedSuccessful.top = (lastRunningHistoryEntry == null || lastRunningHistoryEntry.getHistoryId() == null || lastRunningHistoryEntry
                    .getHistoryId().compareTo(lastCompletedSuccessfullHistoryEntry.getHistoryId()) <= 0) && (lastCompletedWithErrorHistoryEntry == null
                            || lastCompletedSuccessfullHistoryEntry.getHistoryId() == null || lastCompletedWithErrorHistoryEntry.getHistoryId().compareTo(
                                    lastCompletedSuccessfullHistoryEntry.getHistoryId()) <= 0);
            jobChainHistoryInfo.lastCompletedSuccessful.start = jobHistoryHelper.getDateFromString(lastCompletedSuccessfullHistoryEntry.getStartTime());
            jobChainHistoryInfo.lastCompletedSuccessful.end = jobHistoryHelper.getDateFromString(lastCompletedSuccessfullHistoryEntry.getEndTime());
            jobChainHistoryInfo.lastCompletedSuccessful.id = jobHistoryHelper.big2int(lastCompletedSuccessfullHistoryEntry.getHistoryId());
            jobChainHistoryInfo.lastCompletedSuccessful.jobChainName = lastCompletedSuccessfullHistoryEntry.getJobChain();
            jobChainHistoryInfo.lastCompletedSuccessful.duration = jobHistoryHelper.getDuration(jobChainHistoryInfo.lastCompletedSuccessful.start,
                    jobChainHistoryInfo.lastCompletedSuccessful.end);
        } else {
            jobChainHistoryInfo.lastCompletedSuccessful.found = false;
            LOGGER.debug(String.format("no successfull job chain run found for the job chain:%s", jobChainName));
        }
        if (lastCompletedWithErrorHistoryEntry != null) {
            jobChainHistoryInfo.lastCompletedWithError.found = true;
            jobChainHistoryInfo.lastCompletedWithError.orderId = lastCompletedWithErrorHistoryEntry.getOrder();
            jobChainHistoryInfo.lastCompletedWithError.state = lastCompletedWithErrorHistoryEntry.getState();
            jobChainHistoryInfo.lastCompletedWithError.top = (lastRunningHistoryEntry == null || lastRunningHistoryEntry.getHistoryId() == null || lastRunningHistoryEntry
                    .getHistoryId().compareTo(lastCompletedWithErrorHistoryEntry.getHistoryId()) <= 0) && (lastCompletedSuccessfullHistoryEntry == null
                            || lastCompletedSuccessfullHistoryEntry.getHistoryId() == null || lastCompletedSuccessfullHistoryEntry.getHistoryId().compareTo(
                                    lastCompletedWithErrorHistoryEntry.getHistoryId()) <= 0);
            jobChainHistoryInfo.lastCompletedWithError.start = jobHistoryHelper.getDateFromString(lastCompletedWithErrorHistoryEntry.getStartTime());
            jobChainHistoryInfo.lastCompletedWithError.end = jobHistoryHelper.getDateFromString(lastCompletedWithErrorHistoryEntry.getEndTime());
            jobChainHistoryInfo.lastCompletedWithError.id = jobHistoryHelper.big2int(lastCompletedWithErrorHistoryEntry.getHistoryId());
            jobChainHistoryInfo.lastCompletedWithError.jobChainName = lastCompletedWithErrorHistoryEntry.getJobChain();
            jobChainHistoryInfo.lastCompletedWithError.duration = jobHistoryHelper.getDuration(jobChainHistoryInfo.lastCompletedWithError.start,
                    jobChainHistoryInfo.lastCompletedWithError.end);
        } else {
            jobChainHistoryInfo.lastCompletedWithError.found = false;
            LOGGER.debug(String.format("no job chain runs with error found for the job chain:%s", jobChainName));
        }
        if (lastRunningHistoryEntry != null) {
            jobChainHistoryInfo.running.found = true;
            jobChainHistoryInfo.running.orderId = lastRunningHistoryEntry.getOrder();
            jobChainHistoryInfo.running.state = lastRunningHistoryEntry.getState();
            jobChainHistoryInfo.running.top = !jobChainHistoryInfo.lastCompleted.top;
            jobChainHistoryInfo.running.start = jobHistoryHelper.getDateFromString(lastRunningHistoryEntry.getStartTime());
            jobChainHistoryInfo.running.end = null;
            jobChainHistoryInfo.running.id = jobHistoryHelper.big2int(lastRunningHistoryEntry.getHistoryId());
            jobChainHistoryInfo.running.jobChainName = lastRunningHistoryEntry.getJobChain();
            jobChainHistoryInfo.running.duration = "";
        } else {
            jobChainHistoryInfo.running.found = false;
            LOGGER.debug(String.format("no running job chain found for the job chain:%s", jobChainName));
        }
        return jobChainHistoryInfo;
    }

    private void getHistoryByWebServiceCall(String jobChainName) throws Exception {
        String orderId = jobHistoryHelper.getOrderId(jobChainName);
        jobChainName = jobHistoryHelper.getJobChainName(jobChainName);

        jobChainName = this.jobHistoryHelper.normalizePath(relativePath, jobChainName);
        actHistoryObjectName = jobChainName;

        if (historyWebserviceExecuter == null) {
            if (!this.webserviceCredentials.account().isEmpty()) {
                historyWebserviceExecuter = new HistoryWebserviceExecuter(jocUrl, this.webserviceCredentials.account());
            } else {
                historyWebserviceExecuter = new HistoryWebserviceExecuter(jocUrl);
            }
            historyWebserviceExecuter.login(webserviceCredentials.getAccessToken());
        }

        historyWebserviceExecuter.setTimeLimit(timeLimit);
        historyWebserviceExecuter.setJobChainName(jobChainName);
        historyWebserviceExecuter.setOrderId(orderId);
        historyWebserviceExecuter.setSchedulerId(webserviceCredentials.getSchedulerId());

        lastCompletedSuccessfullHistoryEntry = historyWebserviceExecuter.getLastCompletedSuccessfullJobChainHistoryEntry();
        lastCompletedHistoryEntry = historyWebserviceExecuter.getLastCompletedJobChainHistoryEntry();
        lastCompletedWithErrorHistoryEntry = historyWebserviceExecuter.getLastCompletedWithErrorJobChainHistoryEntry();
        lastRunningHistoryEntry = historyWebserviceExecuter.getLastRunningJobChainHistoryEntry();
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

}
