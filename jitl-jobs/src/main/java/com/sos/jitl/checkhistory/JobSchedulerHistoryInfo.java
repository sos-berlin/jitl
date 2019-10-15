package com.sos.jitl.checkhistory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.jitl.checkhistory.classes.JobSchedulerHistoryInfoEntry;
import com.sos.jitl.checkhistory.interfaces.IJobSchedulerHistoryInfo;

public class JobSchedulerHistoryInfo implements IJobSchedulerHistoryInfo {

    private String startTime = "0:00:00:00";
    private String endTime = "0:00:00:00";
    private HistoryHelper jobHistoryHelper;
    public JobSchedulerHistoryInfoEntry lastCompleted;
    public JobSchedulerHistoryInfoEntry running;
    public JobSchedulerHistoryInfoEntry lastCompletedSuccessful;
    public JobSchedulerHistoryInfoEntry lastCompletedWithError;

    public JobSchedulerHistoryInfo() {
        super();
        jobHistoryHelper = new HistoryHelper();
        running = new JobSchedulerHistoryInfoEntry();
        running.name = "running";
        lastCompleted = new JobSchedulerHistoryInfoEntry();
        lastCompleted.name = "last";
        lastCompletedSuccessful = new JobSchedulerHistoryInfoEntry();
        lastCompletedSuccessful.name = "lastSuccessful";
        lastCompletedWithError = new JobSchedulerHistoryInfoEntry();
        lastCompletedWithError.name = "lastWithError";
    }

   
    private JobSchedulerHistoryInfoEntry getYoungerStartEntry(JobSchedulerHistoryInfoEntry e1, JobSchedulerHistoryInfoEntry e2) {
        if (e1 != null && !e1.found) {
            return e2;
        }
        if (e2 != null && !e2.found) {
            return e1;
        }
        if (e2 == null && e1 == null) {
            return null;
        }
        if (e1 == null && e2 != null) {
            return e2;
        }
        if (e2 == null && e1 != null) {
            return e1;
        }
        if (e1 != null && e2 != null && e1.start != null && e2.start != null && e1.start.isAfter(e2.start)) {
            return e1;
        } else {
            return e2;
        }
    }

    public JobSchedulerHistoryInfoEntry getLastExecution() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getYoungerStartEntry(lastCompleted, running);
        return jobHistoryInfoEntry;
    }

    public boolean lastSuccessfulCompletedRunEndedAtTop() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompletedSuccessful();
        return jobHistoryInfoEntry.found && jobHistoryInfoEntry.error == 0 && jobHistoryInfoEntry.top;
    }

    public boolean lastWithErrorCompletedRunEndedAtTop() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompletedWithError();
        return jobHistoryInfoEntry.found && jobHistoryInfoEntry.error != 0 && jobHistoryInfoEntry.top;
    }

    public boolean lastSuccessfulCompletedRunEndedTodayAtTop() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompletedSuccessful();
        return jobHistoryInfoEntry.found && jobHistoryInfoEntry.error == 0 && HistoryHelper.isToday(jobHistoryInfoEntry.end)
                && jobHistoryInfoEntry.top;
    }

    public boolean lastWithErrorCompletedRunEndedTodayAtTop() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompletedWithError();
        return jobHistoryInfoEntry.found && jobHistoryInfoEntry.error != 0 && HistoryHelper.isToday(jobHistoryInfoEntry.end)
                && jobHistoryInfoEntry.top;
    }

    public boolean lastCompletedRunEndedSuccessful() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompleted();
        return jobHistoryInfoEntry.found && jobHistoryInfoEntry.error == 0;
    }

    public boolean lastCompletedRunEndedWithError() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompleted();
        return jobHistoryInfoEntry.found && jobHistoryInfoEntry.error != 0;
    }

    public boolean lastCompletedRunEndedTodaySuccessful() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompleted();
        return jobHistoryInfoEntry.found && jobHistoryInfoEntry.error == 0 && HistoryHelper.isToday(jobHistoryInfoEntry.end);
    }

    public boolean lastCompletedRunEndedTodayWithError() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompleted();
        return jobHistoryInfoEntry.found && jobHistoryInfoEntry.error != 0 && HistoryHelper.isToday(jobHistoryInfoEntry.end);
    }

    public boolean isStartedToday() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastExecution();
        return jobHistoryInfoEntry != null && HistoryHelper.isToday(jobHistoryInfoEntry.start);
    }

    public boolean isStartedTodayCompletedSuccessful() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = lastCompletedSuccessful;
        return jobHistoryInfoEntry != null && HistoryHelper.isToday(jobHistoryInfoEntry.start);
    }

    public boolean isStartedTodayCompletedWithError() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = lastCompletedWithError;
        return jobHistoryInfoEntry != null && HistoryHelper.isToday(jobHistoryInfoEntry.start);
    }

    public boolean isStartedTodayCompleted() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = lastCompleted;
        return jobHistoryInfoEntry != null && HistoryHelper.isToday(jobHistoryInfoEntry.start);
    }

    public boolean isCompletedToday() {
        return lastCompleted != null && HistoryHelper.isToday(lastCompleted.end);
    }

    public boolean isCompletedTodaySuccessful() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = lastCompletedSuccessful;
        return jobHistoryInfoEntry != null && HistoryHelper.isToday(jobHistoryInfoEntry.end);
    }

    public boolean isCompletedTodayWithError() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = lastCompletedWithError;
        return jobHistoryInfoEntry != null && HistoryHelper.isToday(jobHistoryInfoEntry.end);
    }

    public boolean endedWithErrorAfter(String time) {
        return jobHistoryHelper.isAfter(lastCompletedWithError.end, time);
    }

    public boolean endedSuccessfulAfter(String time) {
        return jobHistoryHelper.isAfter(lastCompletedSuccessful.end, time);
    }

    public boolean endedAfter(String time) {
        return jobHistoryHelper.isAfter(lastCompleted.end, time);
    }

    public boolean isCompletedWithErrorAfter(String time) {
        return endedWithErrorAfter(time);
    }

    public boolean isCompletedSuccessfulAfter(String time) {
        return endedSuccessfulAfter(time);
    }

    public boolean isCompletedAfter(String time) {
        return endedAfter(time);
    }

    public boolean isEndedWithErrorAfter(String time) {
        return endedWithErrorAfter(time);
    }

    public boolean isEndedSuccessfulAfter(String time) {
        return endedSuccessfulAfter(time);
    }

    public boolean isEndedAfter(String time) {
        return endedAfter(time);
    }

    public boolean isCompletedWithErrorBefore() {
        return lastCompletedWithError.found;
    }

    public boolean isCompletedSuccessfulBefore() {
        return lastCompletedSuccessful.found;
    }

    public boolean isCompletedBefore() {
        return lastCompleted.found;
    }

    public boolean lastCompleteIsEndedBefore(String time) {
        return jobHistoryHelper.isBefore(lastCompleted.end, time);
    }

    public boolean lastCompleteSuccessfulIsEndedBefore(String time) {
        return jobHistoryHelper.isBefore(lastCompletedSuccessful.end, time);
    }

    public boolean lastCompleteWithErrorIsEndedBefore(String time) {
        return jobHistoryHelper.isBefore(lastCompletedWithError.end, time);
    }

    public boolean lastCompletedIsStartedBefore(String time) {
        return jobHistoryHelper.isBefore(lastCompleted.start, time);
    }

    public boolean lastCompleteSuccessfulIsStartedBefore(String time) {
        return jobHistoryHelper.isBefore(lastCompletedSuccessful.start, time);
    }

    public boolean lastCompleteWithErrorIsStartedBefore(String time) {
        return jobHistoryHelper.isBefore(lastCompletedWithError.start, time);
    }

    public boolean startedWithErrorAfter(String time) {
        return jobHistoryHelper.isAfter(lastCompletedWithError.start, time);
    }

    public boolean startedSuccessfulAfter(String time) {
        return jobHistoryHelper.isAfter(lastCompletedSuccessful.start, time);
    }

    public boolean startedAfter(String time) {
        return jobHistoryHelper.isAfter(getLastExecution().start, time);
    }

    public boolean isStartedWithErrorAfter(String time) {
        return startedWithErrorAfter(time);
    }

    public boolean isStartedSuccessfulAfter(String time) {
        return startedSuccessfulAfter(time);
    }

    public boolean isStartedAfter(String time) {
        return startedAfter(time);
    }

    public JobSchedulerHistoryInfoEntry getLastCompleted() {
        return lastCompleted;
    }

    public JobSchedulerHistoryInfoEntry getRunning() {
        return running;
    }

    public JobSchedulerHistoryInfoEntry getLastCompletedSuccessful() {
        return lastCompletedSuccessful;
    }

    public JobSchedulerHistoryInfoEntry getLastCompletedWithError() {
        return lastCompletedWithError;
    }

    public boolean queryHistory(String query) {
        boolean result = false;
        String methodName = HistoryHelper.getMethodName(query);
        String time = "";
        switch (methodName.toLowerCase()) {
        // isStartedToday
        case "isstartedtoday":
            result = isStartedToday();
            break;
        // isStartedTodayCompletedSuccessful
        case "isstartedtodaycompletedsuccessful":
            result = isStartedTodayCompletedSuccessful();
            break;
        // isStartedTodayCompletedWithError
        case "isstartedtodaycompletedwitherror":
            result = isStartedTodayCompletedWithError();
            break;
        // isStartedTodayCompleted
        case "isstartedtodaycompleted":
            result = isStartedTodayCompleted();
            break;
        // isCompletedToday
        case "iscompletedtoday":
            result = isCompletedToday();
            break;
        // isCompletedTodaySuccessful
        case "iscompletedtodaysuccessful":
            result = isCompletedTodaySuccessful();
            break;
        // isCompletedTodayWithError
        case "iscompletedtodaywitherror":
            result = isCompletedTodayWithError();
            break;
        // isCompletedAfter
        case "iscompletedafter":
            time = jobHistoryHelper.getParameter(endTime, query);
            result = isCompletedAfter(time);
            break;
        // isCompletedWithErrorAfter
        case "iscompletedwitherrorafter":
            time = jobHistoryHelper.getParameter(endTime, query);
            result = isCompletedWithErrorAfter(time);
            break;
        // isCompletedSuccessfulAfter
        case "iscompletedsuccessfulafter":
            time = jobHistoryHelper.getParameter(endTime, query);
            result = isCompletedSuccessfulAfter(time);
            break;
        // case "isStartedAfter
        case "isstartedafter":
            time = jobHistoryHelper.getParameter(startTime, query);
            result = isStartedAfter(time);
            break;
        // isStartedWithErrorAfter
        case "isstartedwitherrorafter":
            time = jobHistoryHelper.getParameter(startTime, query);
            result = isStartedWithErrorAfter(time);
            break;
        // isStartedSuccessfulAfter
        case "isstartedsuccessfulafter":
            time = jobHistoryHelper.getParameter(startTime, query);
            result = isStartedSuccessfulAfter(time);
            break;

        // lastCompletedIsEndedBefore
        case "lastcompletedisendedbefore":
            time = jobHistoryHelper.getParameter(endTime, query);
            result = lastCompleteIsEndedBefore(time);
            break;
        // lastCompletedSuccessdulIsEndedBefore
        case "lastcompletedsuccessfulisendedbefore":
            time = jobHistoryHelper.getParameter(endTime, query);
            result = lastCompleteSuccessfulIsEndedBefore(time);
            break;
        // lastCompletedWithErrorIsEndedBefore
        case "lastcompletedwitherrorisendedbefore":
            time = jobHistoryHelper.getParameter(endTime, query);
            result = lastCompleteWithErrorIsEndedBefore(time);
            break;
        // lastCompletedIsStartedBefore
        case "lastcompletedisstartedbefore":
            time = jobHistoryHelper.getParameter(startTime, query);
            result = lastCompletedIsStartedBefore(time);
            break;
        // lastCompletedSuccessfulIsStartedBefore
        case "lastcompletedsuccessfulisstartedbefore":
            time = jobHistoryHelper.getParameter(startTime, query);
            result = lastCompleteSuccessfulIsStartedBefore(time);
            break;
        // lastCompletedWithErrorIsStartedBefore
        case "lastcompletedwitherrorisstartedbefore":
            time = jobHistoryHelper.getParameter(startTime, query);
            result = lastCompleteWithErrorIsStartedBefore(time);
            break;

        // isCompletedBefore
        case "iscompletedbefore":
            result = isCompletedBefore();
            break;
        // isCompletedSuccessfulBefore
        case "iscompletedsuccessfulbefore":
            result = isCompletedSuccessfulBefore();
            break;
        // isCompletedWithErrorBefore
        case "iscompletedwitherrorbefore":
            result = isCompletedWithErrorBefore();
            break;
        // lastSuccessfulCompletedRunEndedAtPosition
        case "lastsuccessfulcompletedrunendedattop":
            result = lastSuccessfulCompletedRunEndedAtTop();
            break;
        // lastSuccessfulCompletedRunEndedTodayatposition
        case "lastsuccessfulcompletedrunendedtodayattop":
            result = lastSuccessfulCompletedRunEndedTodayAtTop();
            break;
        // lastWithErrorCompletedRunEndedAtPosition
        case "lastwitherrorcompletedrunendedattop":
            result = lastWithErrorCompletedRunEndedAtTop();
            break;
        // lastWithErrorCompletedRunEndedAtPosition
        case "lastwitherrorcompletedrunendedtodayattop":
            result = lastWithErrorCompletedRunEndedTodayAtTop();
            break;
        // lastCompletedRunEndedSuccessful
        case "lastcompletedrunendedsuccessful":
            result = lastCompletedRunEndedSuccessful();
            break;
        // lastCompletedRunEndedWithError
        case "lastcompletedrunendedwitherror":
            result = lastCompletedRunEndedWithError();
            break;
        // lastCompletedRunEndedTodaySuccessful
        case "lastcompletedrunendedtodaysuccessful":
            result = lastCompletedRunEndedTodaySuccessful();
            break;
        // lastCompletedRunEndedTodayWithError
        case "lastcompletedrunendedtodaywitherror":
            result = lastCompletedRunEndedTodayWithError();
            break;
        default:
            throw new JobSchedulerException("unknown command: " + query);
        }
        return result;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

}