package com.sos.jitl.checkrunhistory;

import java.math.BigInteger;
import java.util.List;
import org.apache.log4j.Logger;
import sos.spooler.Spooler;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Answer;
import com.sos.scheduler.model.answers.ERROR;
import com.sos.scheduler.model.answers.HistoryEntry;
import com.sos.scheduler.model.commands.JSCmdShowHistory;
import com.sos.scheduler.model.commands.ShowHistory;

public class JobHistory implements IJobSchedulerHistory {

    private static final int NUMBER_OF_RUNS = 100;
    private static Logger logger = Logger.getLogger(JobHistory.class);
    private String host;
    private int port;
    private Spooler spooler;
    private HistoryEntry lastCompletedHistoryEntry = null;
    private HistoryEntry lastRunningHistoryEntry = null;
    private HistoryEntry lastCompletedSuccessfullHistoryEntry = null;
    private HistoryEntry lastCompletedWithErrorHistoryEntry = null;

    private int lastCompletedHistoryEntryPos;
    private int lastRunningHistoryEntryPos;
    private int lastCompletedSuccessfullHistoryEntryPos;
    private int lastCompletedWithErrorHistoryEntryPos;

    private String timeLimit;

    private int numberOfStarts;
    private int numberOfCompletedSuccessful;
    private int numberOfCompletedWithError;
    private int numberOfCompleted;

    private int count;
    private JobHistoryHelper jobHistoryHelper;

    public JobHistory(String host_, int port_) {
        super();
        jobHistoryHelper = new JobHistoryHelper();
        this.host = host_;
        this.port = port_;
        timeLimit = "";
    }

    public JobHistory(Spooler spooler_) {
        super();
        jobHistoryHelper = new JobHistoryHelper();
        this.spooler = spooler_;
        timeLimit = "";
    }

    public JobSchedulerHistoryInfo getJobInfo(String jobName) throws Exception {
        return (JobSchedulerHistoryInfo) getJobSchedulerHistoryInfo(jobName);
    }

    public JobSchedulerHistoryInfo getJobInfo(String jobName, String timeLimit_) throws Exception {
        return (JobSchedulerHistoryInfo) getJobSchedulerHistoryInfo(jobName, timeLimit_);
    }

    public JobSchedulerHistoryInfo getJobInfo(String jobName, int limit, String timeLimit_) throws Exception {
        return (JobSchedulerHistoryInfo) getJobSchedulerHistoryInfo(jobName, limit, timeLimit_);
    }

    public JobSchedulerHistoryInfo getJobHistoryInfo(String jobName, int numberOfRuns) throws Exception {
        return (JobSchedulerHistoryInfo) getJobSchedulerHistoryInfo(jobName, numberOfRuns);
    }

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobName) throws Exception {
        lastCompletedHistoryEntry = null;
        lastRunningHistoryEntry = null;
        lastCompletedSuccessfullHistoryEntry = null;
        lastCompletedWithErrorHistoryEntry = null;
        return getJobSchedulerHistoryInfo(jobName, NUMBER_OF_RUNS);
    }

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobName, String timeLimit_) throws Exception {
        lastCompletedHistoryEntry = null;
        lastRunningHistoryEntry = null;
        lastCompletedSuccessfullHistoryEntry = null;
        lastCompletedWithErrorHistoryEntry = null;
        timeLimit = timeLimit_;
        return getJobSchedulerHistoryInfo(jobName, NUMBER_OF_RUNS);
    }

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobName, int limit, String timeLimit_) throws Exception {
        lastCompletedHistoryEntry = null;
        lastRunningHistoryEntry = null;
        lastCompletedSuccessfullHistoryEntry = null;
        lastCompletedWithErrorHistoryEntry = null;
        timeLimit = timeLimit_;
        return getJobSchedulerHistoryInfo(jobName, limit);
    }

    public IJobSchedulerHistoryInfo getJobSchedulerHistoryInfo(String jobName, int numberOfRuns) throws Exception {
        getHistory(jobName, numberOfRuns);
        JobSchedulerHistoryInfo jobHistoryInfo = new JobSchedulerHistoryInfo();

        if (lastCompletedHistoryEntry != null) {
            jobHistoryInfo.lastCompleted.found = true;
            jobHistoryInfo.lastCompleted.position = lastCompletedHistoryEntryPos;
            jobHistoryInfo.lastCompleted.errorMessage = lastCompletedHistoryEntry.getErrorText();
            jobHistoryInfo.lastCompleted.executionResult = jobHistoryHelper.big2int(lastCompletedHistoryEntry.getExitCode());
            jobHistoryInfo.lastCompleted.start = jobHistoryHelper.getDateFromString(lastCompletedHistoryEntry.getStartTime());
            jobHistoryInfo.lastCompleted.end = jobHistoryHelper.getDateFromString(lastCompletedHistoryEntry.getEndTime());
            jobHistoryInfo.lastCompleted.error = jobHistoryHelper.big2int(lastCompletedHistoryEntry.getError());
            jobHistoryInfo.lastCompleted.errorCode = lastCompletedHistoryEntry.getErrorCode();
            jobHistoryInfo.lastCompleted.id = jobHistoryHelper.big2int(lastCompletedHistoryEntry.getId());
            jobHistoryInfo.lastCompleted.jobName = lastCompletedHistoryEntry.getJobName();
            jobHistoryInfo.lastCompleted.duration = jobHistoryHelper.getDuration(jobHistoryInfo.lastCompleted.start, jobHistoryInfo.lastCompleted.end);

        } else {
            jobHistoryInfo.lastCompleted.found = false;
            logger.debug(String.format("no completed job run found for the job:%s in the last %s job runs", jobName, numberOfRuns));
        }

        if (lastCompletedSuccessfullHistoryEntry != null) {
            jobHistoryInfo.lastCompletedSuccessful.found = true;
            jobHistoryInfo.lastCompletedSuccessful.position = lastCompletedSuccessfullHistoryEntryPos;
            jobHistoryInfo.lastCompletedSuccessful.errorMessage = lastCompletedSuccessfullHistoryEntry.getErrorText();
            jobHistoryInfo.lastCompletedSuccessful.executionResult = jobHistoryHelper.big2int(lastCompletedSuccessfullHistoryEntry.getExitCode());
            jobHistoryInfo.lastCompletedSuccessful.start = jobHistoryHelper.getDateFromString(lastCompletedSuccessfullHistoryEntry.getStartTime());
            jobHistoryInfo.lastCompletedSuccessful.end = jobHistoryHelper.getDateFromString(lastCompletedSuccessfullHistoryEntry.getEndTime());
            jobHistoryInfo.lastCompletedSuccessful.error = jobHistoryHelper.big2int(lastCompletedSuccessfullHistoryEntry.getError());
            jobHistoryInfo.lastCompletedSuccessful.errorCode = lastCompletedSuccessfullHistoryEntry.getErrorCode();
            jobHistoryInfo.lastCompletedSuccessful.id = jobHistoryHelper.big2int(lastCompletedSuccessfullHistoryEntry.getId());
            jobHistoryInfo.lastCompletedSuccessful.jobName = lastCompletedSuccessfullHistoryEntry.getJobName();
            jobHistoryInfo.lastCompletedSuccessful.duration = jobHistoryHelper.getDuration(jobHistoryInfo.lastCompletedSuccessful.start, jobHistoryInfo.lastCompletedSuccessful.end);
        } else {
            jobHistoryInfo.lastCompletedSuccessful.found = false;
            logger.debug(String.format("no successfull job run found for the job:%s in the last %s job runs", jobName, numberOfRuns));
        }

        if (lastCompletedWithErrorHistoryEntry != null) {
            jobHistoryInfo.lastCompletedWithError.found = true;
            jobHistoryInfo.lastCompletedWithError.position = lastCompletedWithErrorHistoryEntryPos;
            jobHistoryInfo.lastCompletedWithError.errorMessage = lastCompletedWithErrorHistoryEntry.getErrorText();
            jobHistoryInfo.lastCompletedWithError.executionResult = jobHistoryHelper.big2int(lastCompletedWithErrorHistoryEntry.getExitCode());
            jobHistoryInfo.lastCompletedWithError.start = jobHistoryHelper.getDateFromString(lastCompletedWithErrorHistoryEntry.getStartTime());
            jobHistoryInfo.lastCompletedWithError.end = jobHistoryHelper.getDateFromString(lastCompletedWithErrorHistoryEntry.getEndTime());
            jobHistoryInfo.lastCompletedWithError.error = jobHistoryHelper.big2int(lastCompletedWithErrorHistoryEntry.getError());
            jobHistoryInfo.lastCompletedWithError.errorCode = lastCompletedWithErrorHistoryEntry.getErrorCode();
            jobHistoryInfo.lastCompletedWithError.id = jobHistoryHelper.big2int(lastCompletedWithErrorHistoryEntry.getId());
            jobHistoryInfo.lastCompletedWithError.jobName = lastCompletedWithErrorHistoryEntry.getJobName();
            jobHistoryInfo.lastCompletedWithError.duration = jobHistoryHelper.getDuration(jobHistoryInfo.lastCompletedWithError.start, jobHistoryInfo.lastCompletedWithError.end);
        } else {
            jobHistoryInfo.lastCompletedWithError.found = false;
            logger.debug(String.format("no job runs with error found for the job:%s in the last %s job runs", jobName, numberOfRuns));
        }

        if (lastRunningHistoryEntry != null) {
            jobHistoryInfo.running.found = true;
            jobHistoryInfo.running.position = lastRunningHistoryEntryPos;
            jobHistoryInfo.running.errorMessage = lastRunningHistoryEntry.getErrorText();
            jobHistoryInfo.running.executionResult = jobHistoryHelper.big2int(lastRunningHistoryEntry.getExitCode());
            jobHistoryInfo.running.start = jobHistoryHelper.getDateFromString(lastRunningHistoryEntry.getStartTime());
            jobHistoryInfo.running.end = jobHistoryHelper.getDateFromString(lastRunningHistoryEntry.getEndTime());
            jobHistoryInfo.running.error = jobHistoryHelper.big2int(lastRunningHistoryEntry.getError());
            jobHistoryInfo.running.errorCode = lastRunningHistoryEntry.getErrorCode();
            jobHistoryInfo.running.id = jobHistoryHelper.big2int(lastRunningHistoryEntry.getId());
            jobHistoryInfo.running.jobName = lastRunningHistoryEntry.getJobName();
            jobHistoryInfo.running.duration = jobHistoryHelper.getDuration(jobHistoryInfo.running.start, jobHistoryInfo.running.end);

        } else {
            jobHistoryInfo.running.found = false;
            logger.debug(String.format("no running jobs found for the job:%s in the last %s job runs", jobName, numberOfRuns));
        }
        return jobHistoryInfo;
    }

    private void getHistory(String jobName, int numberOfRuns) throws Exception {

        SchedulerObjectFactory jsFactory = new SchedulerObjectFactory();
        jsFactory.initMarshaller(ShowHistory.class);
        JSCmdShowHistory showHistory = jsFactory.createShowHistory();

        showHistory.setJob(jobName);
        showHistory.setPrev(BigInteger.valueOf(numberOfRuns));
        Answer answer = null;
        String lastMsg = "";

        try {
            if (spooler == null) {
                jsFactory.Options().ServerName.Value(host);
                jsFactory.Options().PortNumber.value(port);
                showHistory.run();
                answer = showHistory.getAnswer();
            } else {
                showHistory.getAnswerFromSpooler(spooler);
                answer = showHistory.getAnswer();
            }
        } catch (Exception e) {
            lastMsg = String.format("Query to JobScheduler results into an exception:%s", e.getMessage());
            logger.debug(lastMsg);
        }

        numberOfCompleted = 0;
        numberOfStarts = 0;
        numberOfCompletedSuccessful = 0;
        numberOfCompletedWithError = 0;

        if (answer != null) {
            ERROR error = answer.getERROR();
            if (error != null) {
                String msg = String.format("Answer from JobScheduler have the error \"%s\"\nNo entries found for the job:%s", error.getText(), jobName);
                logger.debug(msg);
            } else {

                List<HistoryEntry> jobHistoryEntries = answer.getHistory().getHistoryEntry();

                count = jobHistoryEntries.size();
                if (count == 0) {
                    String msg = "No entries found for the job:" + jobName;
                    logger.debug(msg);
                } else {
                    int pos = 0;

                    for (HistoryEntry historyItem : jobHistoryEntries) {

                        if (jobHistoryHelper.isInTimeLimit(timeLimit, historyItem.getEndTime()) && historyItem.getSteps() != null
                                && historyItem.getSteps().intValue() > 0) {

                            numberOfStarts = numberOfStarts + 1;

                            if ((historyItem.getEndTime() != null)) {
                                numberOfCompleted = numberOfCompleted + 1;
                                if (lastCompletedHistoryEntry == null) {
                                    lastCompletedHistoryEntry = historyItem;
                                    lastCompletedHistoryEntryPos = pos;
                                }

                                if (historyItem.getExitCode().intValue() == 0) {
                                    numberOfCompletedSuccessful = numberOfCompletedSuccessful + 1;

                                    if ((lastCompletedSuccessfullHistoryEntry == null)) {
                                        lastCompletedSuccessfullHistoryEntry = historyItem;
                                        lastCompletedSuccessfullHistoryEntryPos = pos;
                                    }
                                }

                                if (historyItem.getExitCode().intValue() != 0) {
                                    numberOfCompletedWithError = numberOfCompletedWithError + 1;

                                    if ((lastCompletedWithErrorHistoryEntry == null)) {
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

    public String getTimeLimit() {
        return timeLimit;
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

}
