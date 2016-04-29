package com.sos.jitl.checkrunhistory;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Answer;
import com.sos.scheduler.model.answers.ERROR;
import com.sos.scheduler.model.answers.JobChain;
import com.sos.scheduler.model.answers.JobChainNode;
import com.sos.scheduler.model.commands.JSCmdShowJobChain;
import com.sos.scheduler.model.commands.ShowHistory;

import sos.spooler.Spooler;

public class JobChainHistory implements IJobSchedulerHistory {

    private static final int NUMBER_OF_RUNS = 100;
    private static final Logger LOGGER = Logger.getLogger(JobHistory.class);
    private String host;
    private int port;
    private Spooler spooler;
    private JobChain.OrderHistory.Order lastCompletedHistoryEntry = null;
    private JobChain.OrderHistory.Order lastRunningHistoryEntry = null;
    private JobChain.OrderHistory.Order lastCompletedSuccessfullHistoryEntry = null;
    private JobChain.OrderHistory.Order lastCompletedWithErrorHistoryEntry = null;
    private String timeLimit;
    private int numberOfStarts;
    private int numberOfCompletedSuccessful;
    private int numberOfCompletedWithError;
    private int numberOfCompleted;
    private int lastCompletedHistoryEntryPos;
    private int lastRunningHistoryEntryPos;
    private int lastCompletedSuccessfullHistoryEntryPos;
    private int lastCompletedWithErrorHistoryEntryPos;
    private int count;
    private JobHistoryHelper jobHistoryHelper;
    private String relativePath = "";
    private String actHistoryObjectName = "";

    public JobChainHistory(String host_, int port_) {
        super();
        jobHistoryHelper = new JobHistoryHelper();
        this.host = host_;
        this.port = port_;
        timeLimit = "";
    }

    public JobChainHistory(Spooler spooler_) {
        super();
        jobHistoryHelper = new JobHistoryHelper();
        this.spooler = spooler_;
        timeLimit = "";
    }

    public JobSchedulerHistoryInfo getJobChainInfo(String jobName) throws Exception {
        return (JobSchedulerHistoryInfo) getJobSchedulerHistoryInfo(jobName);
    }

    public JobSchedulerHistoryInfo getJobChainInfo(String jobName, String timeLimit_) throws Exception {
        return (JobSchedulerHistoryInfo) getJobSchedulerHistoryInfo(jobName, timeLimit_);
    }

    public JobSchedulerHistoryInfo getJobChainInfo(String jobName, int limit, String timeLimit_) throws Exception {
        return (JobSchedulerHistoryInfo) getJobSchedulerHistoryInfo(jobName, limit, timeLimit_);
    }

    public JobSchedulerHistoryInfo getJobChainInfo(String jobName, int numberOfRuns) throws Exception {
        return (JobSchedulerHistoryInfo) getJobSchedulerHistoryInfo(jobName, numberOfRuns);
    }

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobChainName) throws Exception {
        lastCompletedHistoryEntry = null;
        lastRunningHistoryEntry = null;
        lastCompletedSuccessfullHistoryEntry = null;
        lastCompletedWithErrorHistoryEntry = null;
        return getJobSchedulerHistoryInfo(jobChainName, NUMBER_OF_RUNS);
    }

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobChainName, String timeLimit_) throws Exception {
        lastCompletedHistoryEntry = null;
        lastRunningHistoryEntry = null;
        lastCompletedSuccessfullHistoryEntry = null;
        lastCompletedWithErrorHistoryEntry = null;
        timeLimit = timeLimit_;
        return getJobSchedulerHistoryInfo(jobChainName, NUMBER_OF_RUNS);
    }

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobChainName, int limit, String timeLimit_) throws Exception {
        lastCompletedHistoryEntry = null;
        lastRunningHistoryEntry = null;
        lastCompletedSuccessfullHistoryEntry = null;
        lastCompletedWithErrorHistoryEntry = null;
        timeLimit = timeLimit_;
        return getJobSchedulerHistoryInfo(jobChainName, limit);
    }

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobChainName, int numberOfRuns) throws Exception {
        getHistory(jobChainName, numberOfRuns);
        JobSchedulerHistoryInfo jobChainHistoryInfo = new JobSchedulerHistoryInfo();
        if (lastCompletedHistoryEntry != null) {
            jobChainHistoryInfo.lastCompleted.found = true;
            jobChainHistoryInfo.lastCompleted.position = lastCompletedHistoryEntryPos;
            jobChainHistoryInfo.lastCompleted.start = jobHistoryHelper.getDateFromString(lastCompletedHistoryEntry.getStartTime());
            jobChainHistoryInfo.lastCompleted.end = jobHistoryHelper.getDateFromString(lastCompletedHistoryEntry.getEndTime());
            jobChainHistoryInfo.lastCompleted.id = jobHistoryHelper.big2int(lastCompletedHistoryEntry.getHistoryId());
            jobChainHistoryInfo.lastCompleted.orderId = lastCompletedHistoryEntry.getOrder();
            jobChainHistoryInfo.lastCompleted.jobChainName = lastCompletedHistoryEntry.getJobChain();
            jobChainHistoryInfo.lastCompleted.state = lastCompletedHistoryEntry.getState();
            jobChainHistoryInfo.lastCompleted.duration =
                    jobHistoryHelper.getDuration(jobChainHistoryInfo.lastCompleted.start, jobChainHistoryInfo.lastCompleted.end);
        } else {
            jobChainHistoryInfo.lastCompleted.found = false;
            LOGGER.debug(String.format("no completed job chain run found for the job chains:%s in the last %s job chain runs", jobChainName,
                    numberOfRuns));
        }
        if (lastCompletedSuccessfullHistoryEntry != null) {
            jobChainHistoryInfo.lastCompletedSuccessful.found = true;
            jobChainHistoryInfo.lastCompletedSuccessful.position = lastCompletedSuccessfullHistoryEntryPos;
            jobChainHistoryInfo.lastCompletedSuccessful.start =
                    jobHistoryHelper.getDateFromString(lastCompletedSuccessfullHistoryEntry.getStartTime());
            jobChainHistoryInfo.lastCompletedSuccessful.end = jobHistoryHelper.getDateFromString(lastCompletedSuccessfullHistoryEntry.getEndTime());
            jobChainHistoryInfo.lastCompletedSuccessful.id = jobHistoryHelper.big2int(lastCompletedSuccessfullHistoryEntry.getHistoryId());
            jobChainHistoryInfo.lastCompletedSuccessful.orderId = lastCompletedSuccessfullHistoryEntry.getOrder();
            jobChainHistoryInfo.lastCompletedSuccessful.jobChainName = lastCompletedSuccessfullHistoryEntry.getJobChain();
            jobChainHistoryInfo.lastCompletedSuccessful.state = lastCompletedSuccessfullHistoryEntry.getState();
            jobChainHistoryInfo.lastCompletedSuccessful.duration =
                    jobHistoryHelper.getDuration(jobChainHistoryInfo.lastCompletedSuccessful.start, jobChainHistoryInfo.lastCompletedSuccessful.end);
        } else {
            jobChainHistoryInfo.lastCompletedSuccessful.found = false;
            LOGGER.debug(String.format("no successfull job chain run found for the job chain:%s in the last %s job chain runs", jobChainName,
                    numberOfRuns));
        }
        if (lastCompletedWithErrorHistoryEntry != null) {
            jobChainHistoryInfo.lastCompletedWithError.found = true;
            jobChainHistoryInfo.lastCompletedWithError.position = lastCompletedWithErrorHistoryEntryPos;
            jobChainHistoryInfo.lastCompletedWithError.start = jobHistoryHelper.getDateFromString(lastCompletedWithErrorHistoryEntry.getStartTime());
            jobChainHistoryInfo.lastCompletedWithError.end = jobHistoryHelper.getDateFromString(lastCompletedWithErrorHistoryEntry.getEndTime());
            jobChainHistoryInfo.lastCompletedWithError.id = jobHistoryHelper.big2int(lastCompletedWithErrorHistoryEntry.getHistoryId());
            jobChainHistoryInfo.lastCompletedWithError.orderId = lastCompletedWithErrorHistoryEntry.getOrder();
            jobChainHistoryInfo.lastCompletedWithError.jobChainName = lastCompletedWithErrorHistoryEntry.getJobChain();
            jobChainHistoryInfo.lastCompletedWithError.state = lastCompletedWithErrorHistoryEntry.getState();
            jobChainHistoryInfo.lastCompletedWithError.duration =
                    jobHistoryHelper.getDuration(jobChainHistoryInfo.lastCompletedWithError.start, jobChainHistoryInfo.lastCompletedWithError.end);
        } else {
            jobChainHistoryInfo.lastCompletedWithError.found = false;
            LOGGER.debug(String.format("no job chain runs with error found for the job chain%s in the last %s job chain runs", jobChainName,
                    numberOfRuns));
        }
        if (lastRunningHistoryEntry != null) {
            jobChainHistoryInfo.running.found = true;
            jobChainHistoryInfo.running.position = lastRunningHistoryEntryPos;
            jobChainHistoryInfo.running.start = jobHistoryHelper.getDateFromString(lastRunningHistoryEntry.getStartTime());
            jobChainHistoryInfo.running.end = jobHistoryHelper.getDateFromString(lastRunningHistoryEntry.getEndTime());
            jobChainHistoryInfo.running.id = jobHistoryHelper.big2int(lastRunningHistoryEntry.getHistoryId());
            jobChainHistoryInfo.running.orderId = lastRunningHistoryEntry.getOrder();
            jobChainHistoryInfo.running.jobChainName = lastRunningHistoryEntry.getJobChain();
            jobChainHistoryInfo.running.state = lastRunningHistoryEntry.getState();
            jobChainHistoryInfo.running.duration = jobHistoryHelper.getDuration(jobChainHistoryInfo.running.start, jobChainHistoryInfo.running.end);
        } else {
            jobChainHistoryInfo.running.found = false;
            LOGGER.debug(String.format("no running job chains found for the job chain:%s in the last %s job chain runs", jobChainName, numberOfRuns));
        }
        return jobChainHistoryInfo;
    }

    private boolean isErrorNode(List<JobChainNode> jobChainNodes, String orderState) {
        for (JobChainNode jobChainNode : jobChainNodes) {
            if (jobChainNode.getErrorState() != null && jobChainNode.getErrorState().equals(orderState)) {
                return true;
            }
        }
        return false;
    }

    private void getHistory(String jobChainName, int numberOfRuns) throws Exception {
        String orderId = jobHistoryHelper.getOrderId(jobChainName);
        jobChainName = jobHistoryHelper.getJobChainName(jobChainName);
        SchedulerObjectFactory jsFactory = new SchedulerObjectFactory();
        jsFactory.initMarshaller(ShowHistory.class);
        JSCmdShowJobChain showJobChain = jsFactory.createShowJobChain();
        if (!jobChainName.startsWith("/") && !this.relativePath.isEmpty()) {
            String s = jobChainName;
            jobChainName = new File(this.relativePath, jobChainName).getPath();
            jobChainName = jobChainName.replace('\\', '/');
            LOGGER.debug(String.format("Changed job chain name from %s to %s", s, jobChainName));
        }
        actHistoryObjectName = jobChainName;
        showJobChain.setJobChain(jobChainName);
        showJobChain.setMaxOrderHistory(BigInteger.valueOf(numberOfRuns));
        Answer answer = null;
        String lastMsg = "";
        try {
            if (spooler == null) {
                jsFactory.Options().ServerName.Value(host);
                jsFactory.Options().PortNumber.value(port);
                showJobChain.run();
                answer = showJobChain.getAnswer();
            } else {
                showJobChain.getAnswerFromSpooler(spooler);
                answer = showJobChain.getAnswer();
            }
        } catch (Exception e) {
            lastMsg = String.format("Query to JobScheduler results into an exception: %s", e.getMessage());
            LOGGER.debug(lastMsg);
        }
        numberOfCompleted = 0;
        numberOfStarts = 0;
        numberOfCompletedSuccessful = 0;
        numberOfCompletedWithError = 0;
        if (answer != null) {
            ERROR error = answer.getERROR();
            if (error != null) {
                String msg =
                        String.format("Answer from JobScheduler have the error \"%s\"\nNo entries found for the job chain:%s", error.getText(),
                                jobChainName);
                LOGGER.debug(msg);
            } else {
                List<JobChain.OrderHistory.Order> jobChainHistoryEntries = answer.getJobChain().getOrderHistory().getOrder();
                List<JobChainNode> jobChainNodes = answer.getJobChain().getJobChainNode();
                count = jobChainHistoryEntries.size();
                if (count == 0) {
                    String msg = "No entries found for the job chain:" + jobChainName;
                    LOGGER.debug(msg);
                } else {
                    int pos = 0;
                    for (JobChain.OrderHistory.Order historyItem : jobChainHistoryEntries) {
                        if (jobHistoryHelper.isInTimeLimit(timeLimit, historyItem.getEndTime())
                                && ("".equals(orderId) || orderId.equals(historyItem.getOrder()))) {
                            numberOfStarts = numberOfStarts + 1;
                            boolean isError = isErrorNode(jobChainNodes, historyItem.getState());
                            if (historyItem.getEndTime() != null) {
                                numberOfCompleted = numberOfCompleted + 1;
                                if (lastCompletedHistoryEntry == null) {
                                    lastCompletedHistoryEntry = historyItem;
                                    lastCompletedHistoryEntryPos = pos;
                                }
                                if (!isError) {
                                    numberOfCompletedSuccessful = numberOfCompletedSuccessful + 1;
                                    if (lastCompletedSuccessfullHistoryEntry == null) {
                                        lastCompletedSuccessfullHistoryEntry = historyItem;
                                        lastCompletedSuccessfullHistoryEntryPos = pos;
                                    }
                                }
                                if (isError) {
                                    numberOfCompletedWithError = numberOfCompletedWithError + 1;
                                    if (lastCompletedWithErrorHistoryEntry == null) {
                                        lastCompletedWithErrorHistoryEntry = historyItem;
                                        lastCompletedWithErrorHistoryEntryPos = pos;
                                    }
                                }
                            } else {
                                if (lastRunningHistoryEntry == null) {
                                    lastRunningHistoryEntry = historyItem;
                                    lastRunningHistoryEntryPos = pos;
                                }
                            }
                        }
                        pos = pos + 1;
                    }
                }
            }
        } else {
            throw new JobSchedulerException(lastMsg);
        }
    }

    public void setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getNumberOfCompleted() {
        return numberOfCompleted;
    }

    public int getNumberOfStarts() {
        return numberOfStarts;
    }

    public int getNumberOfCompletedSuccessful() {
        return numberOfCompletedSuccessful;
    }

    public int getNumberOfCompletedWithError() {
        return numberOfCompletedWithError;
    }

    public int getCount() {
        return count;
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
    public String getActHistoryObjectName() {
        return actHistoryObjectName;
    }

}