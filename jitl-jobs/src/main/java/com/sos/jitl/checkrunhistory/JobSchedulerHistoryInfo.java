package com.sos.jitl.checkrunhistory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerHistoryInfo implements IJobSchedulerHistoryInfo {

    public JobSchedulerHistoryInfoEntry lastCompleted;
    public JobSchedulerHistoryInfoEntry running;
    public JobSchedulerHistoryInfoEntry lastCompletedSuccessful;
    public JobSchedulerHistoryInfoEntry lastCompletedWithError;

    private String startTime = "0:00:00:00";
    private String endTime = "0:00:00:00";
    private JobHistoryHelper jobHistoryHelper;

    public JobSchedulerHistoryInfo() {
        super();
        jobHistoryHelper = new JobHistoryHelper();
        running = new JobSchedulerHistoryInfoEntry();
        running.name = "running";

        lastCompleted = new JobSchedulerHistoryInfoEntry();
        lastCompleted.name = "last";

        lastCompletedSuccessful = new JobSchedulerHistoryInfoEntry();
        lastCompletedSuccessful.name = "lastSuccessful";

        lastCompletedWithError = new JobSchedulerHistoryInfoEntry();
        lastCompletedWithError.name = "lastWithError";
    }

    private JobSchedulerHistoryInfoEntry getYoungerEntry(JobSchedulerHistoryInfoEntry e1, JobSchedulerHistoryInfoEntry e2) {
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

        if (e1 != null && e2 != null && e1.position < e2.position) {
            return e1;
        } else {
            return e2;
        }
    }

    public JobSchedulerHistoryInfoEntry getLastExecution() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getYoungerEntry(lastCompleted, running);
        return jobHistoryInfoEntry;
    }

    // Return true if the last completed run that ended successful is on position
    public boolean lastSuccessfulCompletedRunEndedAtPosition(String position) {
        try {
            int p = Integer.parseInt(position);
            JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompletedSuccessful();
            return jobHistoryInfoEntry.found && (jobHistoryInfoEntry.error == 0) && (jobHistoryInfoEntry.position == p);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Return true if the last completed run that ended with error is on position
    public boolean lastWithErrorCompletedRunEndedAtPosition(String position) {
        try {
            int p = Integer.parseInt(position);
            JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompletedWithError();
            return jobHistoryInfoEntry.found && (jobHistoryInfoEntry.error != 0) && (jobHistoryInfoEntry.position == p);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Return true if the last completed run that ended successful ended today and is on position
    public boolean lastSuccessfulCompletedRunEndedTodayAtPosition(String position) {
        try {
            int p = Integer.parseInt(position);
            JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompletedSuccessful();
            return jobHistoryInfoEntry.found && jobHistoryInfoEntry.error == 0 && jobHistoryHelper.isToday(jobHistoryInfoEntry.end)
                    && (jobHistoryInfoEntry.position == p);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Return true if the last completed run that ended with error ended today and is on position
    public boolean lastWithErrorCompletedRunEndedTodayAtPosition(String position) {
        try {
            int p = Integer.parseInt(position);
            JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompletedWithError();

            return jobHistoryInfoEntry.found && (jobHistoryInfoEntry.error != 0) && jobHistoryHelper.isToday(jobHistoryInfoEntry.end)
                    && (jobHistoryInfoEntry.position == p);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Return true if the last completed run ended successful
    public boolean lastCompletedRunEndedSuccessful() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompleted();
        return jobHistoryInfoEntry.found && (jobHistoryInfoEntry.error == 0);
    }

    // Return true if the last completed run ended with error
    public boolean lastCompletedRunEndedWithError() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompleted();
        return jobHistoryInfoEntry.found && (jobHistoryInfoEntry.error != 0);
    }

    // Return true if the last completed run ended successful and the last run ended today
    public boolean lastCompletedRunEndedTodaySuccessful() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompleted();
        return jobHistoryInfoEntry.found && jobHistoryInfoEntry.error == 0 && jobHistoryHelper.isToday(jobHistoryInfoEntry.end);
    }

    // Return true if the last completed run ended with error and the last runended today
    public boolean lastCompletedRunEndedTodayWithError() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastCompleted();
        return jobHistoryInfoEntry.found && (jobHistoryInfoEntry.error != 0 && jobHistoryHelper.isToday(jobHistoryInfoEntry.end));
    }

    // Includes running and ended jobs. Looking for start time
    public boolean isStartedToday() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = getLastExecution();
        return (jobHistoryInfoEntry != null) && (jobHistoryHelper.isToday(jobHistoryInfoEntry.start));
    }

    // Includes successful ended jobs. Looking for start time
    public boolean isStartedTodayCompletedSuccessful() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = lastCompletedSuccessful;
        return (jobHistoryInfoEntry != null) && (jobHistoryHelper.isToday(jobHistoryInfoEntry.start));
    }

    // Includes ended with error jobs. Looking for start time
    public boolean isStartedTodayCompletedWithError() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = lastCompletedWithError;
        return (jobHistoryInfoEntry != null) && (jobHistoryHelper.isToday(jobHistoryInfoEntry.start));
    }

    // Includes ended jobs. Looking for start time
    public boolean isStartedTodayCompleted() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = lastCompleted;
        return (jobHistoryInfoEntry != null) && (jobHistoryHelper.isToday(jobHistoryInfoEntry.start));
    }

    // Includes ended jobs. Looking for end time
    public boolean isCompletedToday() {
        return (lastCompleted != null) && (jobHistoryHelper.isToday(lastCompleted.end));
    }

    // Includes successfull ended jobs. Looking for end time
    public boolean isCompletedTodaySuccessful() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = lastCompletedSuccessful;
        return (jobHistoryInfoEntry != null) && (jobHistoryHelper.isToday(jobHistoryInfoEntry.end));
    }

    // Includes with error ended jobs. Looking for end time
    public boolean isCompletedTodayWithError() {
        JobSchedulerHistoryInfoEntry jobHistoryInfoEntry = lastCompletedWithError;
        return (jobHistoryInfoEntry != null) && (jobHistoryHelper.isToday(jobHistoryInfoEntry.end));
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
        return lastCompleted.found;
    }

    public boolean isCompletedSuccessfulBefore() {
        return lastCompletedSuccessful.found;
    }

    public boolean isCompletedBefore() {
        return lastCompletedSuccessful.found;
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
        JobHistoryHelper jobHistoryHelper = new JobHistoryHelper();
        String methodName = jobHistoryHelper.getMethodName(query);
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
        case "lastsuccessfulcompletedrunendedatposition":
            result = lastSuccessfulCompletedRunEndedAtPosition(jobHistoryHelper.getParameter("0", query));
            break;
        // lastSuccessfulCompletedRunEndedTodayatposition
        case "lastsuccessfulcompletedrunendedtodayatposition":
            result = lastSuccessfulCompletedRunEndedTodayAtPosition(jobHistoryHelper.getParameter("0", query));
            break;
        // lastWithErrorCompletedRunEndedAtPosition
        case "lastwitherrorcompletedrunendedatposition":
            result = lastWithErrorCompletedRunEndedAtPosition(jobHistoryHelper.getParameter("0", query));
            break;
        // lastWithErrorCompletedRunEndedAtPosition
        case "lastwitherrorcompletedrunendedtodayatposition":
            result = lastWithErrorCompletedRunEndedTodayAtPosition(jobHistoryHelper.getParameter("0", query));
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
