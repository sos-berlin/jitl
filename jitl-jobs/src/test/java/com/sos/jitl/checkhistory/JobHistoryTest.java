package com.sos.jitl.checkhistory;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.sos.jitl.checkhistory.classes.JobSchedulerHistoryInfoEntry;
import com.sos.jitl.checkhistory.classes.WebserviceCredentials;

public class JobHistoryTest {

    private static final Logger LOGGER = Logger.getLogger(JobHistoryTest.class);

    @Test
    public void testJobHistory() throws Exception {

        WebserviceCredentials webserviceCredentials = new WebserviceCredentials();
        webserviceCredentials.setPassword("root");
        webserviceCredentials.setUser("root");
        webserviceCredentials.setSchedulerId("scheduler_joc_cockpit");

        JobHistory jobHistory = new com.sos.jitl.checkhistory.JobHistory("http://localhost:4446/joc/api", webserviceCredentials);
        JobSchedulerHistoryInfo jobHistoryInfo = jobHistory.getJobInfo("job1");
        report(jobHistoryInfo.getLastCompleted());
        report(jobHistoryInfo.getRunning());
        report(jobHistoryInfo.lastCompletedSuccessful);
        report(jobHistoryInfo.lastCompletedWithError);
        LOGGER.info("lastCompletedRunEndedSuccessful:" + jobHistoryInfo.lastCompletedRunEndedSuccessful());
        LOGGER.info("lastCompletedRunEndedWithError:" + jobHistoryInfo.lastCompletedRunEndedWithError());
        LOGGER.info("lastCompletedRunEndedTodaySuccessful:" + jobHistoryInfo.lastCompletedRunEndedTodaySuccessful());
        LOGGER.info("lastCompletedRunEndedTodayWithError:" + jobHistoryInfo.lastCompletedRunEndedTodayWithError());
        LOGGER.info("lastCompletedRunEndedSuccessfulAtTop):" + jobHistoryInfo.lastSuccessfulCompletedRunEndedAtTop());
        LOGGER.info("lastCompletedRunEndedWithError:" + jobHistoryInfo.lastWithErrorCompletedRunEndedAtTop());
        LOGGER.info("lastSuccessfulCompletedRunEndedTodayAtTop:" + jobHistoryInfo.lastSuccessfulCompletedRunEndedTodayAtTop());
        LOGGER.info("lastWithErrorCompletedRunEndedTodayAtTop:" + jobHistoryInfo.lastWithErrorCompletedRunEndedTodayAtTop());
        LOGGER.info("isStartedToday:" + jobHistoryInfo.isStartedToday());
        LOGGER.info("isStartedTodayCompletedSuccessful:" + jobHistoryInfo.isStartedTodayCompletedSuccessful());
        LOGGER.info("isStartedTodayCompletedWithError:" + jobHistoryInfo.isStartedTodayCompletedWithError());
        LOGGER.info("isStartedTodayCompleted:" + jobHistoryInfo.isStartedTodayCompleted());
        LOGGER.info("isCompletedToday:" + jobHistoryInfo.isCompletedToday());
        LOGGER.info("isCompletedTodaySuccessfully:" + jobHistoryInfo.isCompletedTodaySuccessful());
        LOGGER.info("isCompletedTodayWithError:" + jobHistoryInfo.isCompletedTodayWithError());
        LOGGER.info("isCompletedAfter:" + jobHistoryInfo.isCompletedAfter("-1:10:48:33"));
        LOGGER.info("isCompletedWithErrorAfter:" + jobHistoryInfo.isCompletedWithErrorAfter("03:00:00"));
        LOGGER.info("isCompletedSuccessfulAfter:" + jobHistoryInfo.isCompletedSuccessfulAfter("03:00:00"));
        LOGGER.info("isStartedAfter:" + jobHistoryInfo.isStartedAfter("-1:10:48:33"));
        LOGGER.info("isStartedWithErrorAfter:" + jobHistoryInfo.isStartedWithErrorAfter("03:00:00"));
        LOGGER.info("isStartedSuccessfulAfter:" + jobHistoryInfo.isStartedSuccessfulAfter("03:00:00"));
        LOGGER.info("isStartedToday:" + jobHistoryInfo.queryHistory("isStartedToday"));
        LOGGER.info("isStartedTodayCompletedSuccessful:" + jobHistoryInfo.queryHistory("isStartedTodayCompletedSuccessful"));
        LOGGER.info("isStartedTodayCompletedWithError:" + jobHistoryInfo.queryHistory("isStartedTodayCompletedWithError"));
        LOGGER.info("isStartedTodayCompleted:" + jobHistoryInfo.queryHistory("isStartedTodayCompleted"));
        LOGGER.info("isCompletedToday:" + jobHistoryInfo.queryHistory("isCompletedToday"));
        LOGGER.info("isCompletedTodaySuccessfully:" + jobHistoryInfo.queryHistory("isCompletedTodaySuccessful"));
        LOGGER.info("isCompletedTodayWithError:" + jobHistoryInfo.queryHistory("isCompletedTodayWithError "));
        LOGGER.info("isCompletedAfter:" + jobHistoryInfo.queryHistory("isCompletedAfter(-1:10:48:33)"));
        LOGGER.info("isCompletedWithErrorAfter:" + jobHistoryInfo.queryHistory("isCompletedWithErrorAfter(03:00:00)"));
        LOGGER.info("isCompletedSuccessfulAfter:" + jobHistoryInfo.queryHistory("isCompletedSuccessfulAfter(03:00:00)"));
        LOGGER.info("isStartedAfter:" + jobHistoryInfo.queryHistory("isStartedAfter(-1:10:48:33)"));
        LOGGER.info("isStartedWithErrorAfter:" + jobHistoryInfo.queryHistory("isStartedWithErrorAfter(03:00:00)"));
        LOGGER.info("isStartedSuccessfulAfter:" + jobHistoryInfo.queryHistory("isStartedSuccessfulAfter(03:00:00)"));
        LOGGER.info("To check whether the job started before a time, limit the query with the time limit");
        jobHistory.setTimeLimit("-1:10:43:56");
        jobHistoryInfo = jobHistory.getJobInfo("job1", "-1:10:43:56");
        LOGGER.info("isCompletedBefore -1:10:43:56:" + jobHistoryInfo.isCompletedBefore());
        LOGGER.info("isCompletedWithErrorBefore -1:10:43:56:" + jobHistoryInfo.isCompletedWithErrorBefore());
        LOGGER.info("isCompletedSuccessfulBefore -1:10:43:56:" + jobHistoryInfo.isCompletedSuccessfulBefore());
        jobHistoryInfo = jobHistory.getJobInfo("job1", "-8:10:00:00..-4:14:00:00");
    }

    private void report(JobSchedulerHistoryInfoEntry reportItem) {
        LOGGER.info("_____________________________");
        if (reportItem.found) {
            LOGGER.info("Name:" + reportItem.name);
            LOGGER.info("id:" + reportItem.id);
            LOGGER.info("Job name:" + reportItem.jobName);
            LOGGER.info("Top:" + reportItem.top);
            LOGGER.info("Start:" + reportItem.start);
            LOGGER.info("End:" + reportItem.end);
            LOGGER.info("Duration:" + reportItem.duration);
            LOGGER.info("Result:" + reportItem.executionResult);
            LOGGER.info("Message:" + reportItem.errorMessage);
            LOGGER.info("Error:" + reportItem.error);
            LOGGER.info("ErrorCode:" + reportItem.errorCode);
        } else {
            LOGGER.info("Name:" + reportItem.name + " not found");
        }
    }

}