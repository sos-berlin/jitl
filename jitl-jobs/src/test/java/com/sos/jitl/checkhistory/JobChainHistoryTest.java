package com.sos.jitl.checkhistory;

import java.time.format.DateTimeFormatter;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.sos.jitl.checkhistory.classes.JobSchedulerHistoryInfoEntry;
import com.sos.jitl.restclient.WebserviceCredentials;

public class JobChainHistoryTest {
    
    private static final Logger LOGGER = Logger.getLogger(JobChainHistoryTest.class);

    @Test
    public void testJobChainHistory() throws Exception {
        WebserviceCredentials webserviceCredentials = new WebserviceCredentials();
        webserviceCredentials.setPassword("api");
        webserviceCredentials.setUser("api_user");
        webserviceCredentials.setSchedulerId("scheduler_joc_cockpit");

        JobChainHistory jobChainHistory = new com.sos.jitl.checkhistory.JobChainHistory("http://localhost:4446/joc/api", webserviceCredentials);

        JobSchedulerHistoryInfo jobChainHistoryInfo = jobChainHistory.getJobChainInfo("/check_history/job_chain3");
        report(jobChainHistoryInfo.getLastCompleted());
        report(jobChainHistoryInfo.getRunning());
        report(jobChainHistoryInfo.getLastCompletedSuccessful());
        report(jobChainHistoryInfo.getLastCompletedWithError());
        LOGGER.info("lastcompletedrunEndedSuccessful:" + jobChainHistoryInfo.lastCompletedRunEndedSuccessful());
        LOGGER.info("isStartedToday:" + jobChainHistoryInfo.isStartedToday());
        LOGGER.info("isStartedTodayCompletedSuccessful:" + jobChainHistoryInfo.isStartedTodayCompletedSuccessful());
        LOGGER.info("isStartedTodayCompletedWithError:" + jobChainHistoryInfo.isStartedTodayCompletedWithError());
        LOGGER.info("isStartedTodayCompleted:" + jobChainHistoryInfo.isStartedTodayCompleted());
        LOGGER.info("isCompletedToday:" + jobChainHistoryInfo.isCompletedToday());
        LOGGER.info("isCompletedTodaySuccessfully:" + jobChainHistoryInfo.isCompletedTodaySuccessful());
        LOGGER.info("isCompletedTodayWithError:" + jobChainHistoryInfo.isCompletedTodayWithError());
        LOGGER.info("isCompletedAfter:" + jobChainHistoryInfo.isCompletedAfter("-1:10:48:33"));
        LOGGER.info("isCompletedWithErrorAfter:" + jobChainHistoryInfo.isCompletedWithErrorAfter("03:00:00"));
        LOGGER.info("isCompletedSuccessfulAfter:" + jobChainHistoryInfo.isCompletedSuccessfulAfter("03:00:00"));
        LOGGER.info("isStartedAfter:" + jobChainHistoryInfo.isStartedAfter("-1:10:48:33"));
        LOGGER.info("isStartedWithErrorAfter:" + jobChainHistoryInfo.isStartedWithErrorAfter("03:00:00"));
        LOGGER.info("isStartedSuccessfulAfter:" + jobChainHistoryInfo.isStartedSuccessfulAfter("03:00:00"));
        LOGGER.info("isStartedToday:" + jobChainHistoryInfo.queryHistory("isStartedToday"));
        LOGGER.info("isStartedTodayCompletedSuccessful:" + jobChainHistoryInfo.queryHistory("isStartedTodayCompletedSuccessful"));
        LOGGER.info("isStartedTodayCompletedWithError:" + jobChainHistoryInfo.queryHistory("isStartedTodayCompletedWithError"));
        LOGGER.info("isStartedTodayCompleted:" + jobChainHistoryInfo.queryHistory("isStartedTodayCompleted"));
        LOGGER.info("isCompletedToday:" + jobChainHistoryInfo.queryHistory("isCompletedToday"));
        LOGGER.info("isCompletedTodaySuccessfully:" + jobChainHistoryInfo.queryHistory("isCompletedTodaySuccessful"));
        LOGGER.info("isCompletedTodayWithError:" + jobChainHistoryInfo.queryHistory("isCompletedTodayWithError "));
        LOGGER.info("isCompletedAfter(-100:10:48:33):" + jobChainHistoryInfo.queryHistory("isCompletedAfter(-100:10:48:33)"));
        LOGGER.info("isCompletedWithErrorAfter(03:00:00):" + jobChainHistoryInfo.queryHistory("isCompletedWithErrorAfter(03:00:00)"));
        LOGGER.info("isCompletedSuccessfulAfter(03:00:00):" + jobChainHistoryInfo.queryHistory("isCompletedSuccessfulAfter(03:00:00)"));
        LOGGER.info("isStartedAfter(-1:10:48:33):" + jobChainHistoryInfo.queryHistory("isStartedAfter(-1:10:48:33)"));
        LOGGER.info("isStartedWithErrorAfter(03:00:00):" + jobChainHistoryInfo.queryHistory("isStartedWithErrorAfter(03:00:00)"));
        LOGGER.info("isStartedSuccessfulAfter(03:00:00):" + jobChainHistoryInfo.queryHistory("isStartedSuccessfulAfter(03:00:00)"));
        LOGGER.info("To check whether the job started before a time, limit the query with the time limit");
        jobChainHistory.setTimeLimit("-1:10:43:56");
        jobChainHistoryInfo = jobChainHistory.getJobChainInfo("sos/events/scheduler_event_service", "-1:10:43:56");
        LOGGER.info("isCompletedBefore -1:10:43:56:" + jobChainHistoryInfo.isCompletedBefore());
        LOGGER.info("isCompletedWithErrorBefore -1:10:43:56:" + jobChainHistoryInfo.isCompletedWithErrorBefore());
        LOGGER.info("isCompletedSuccessfulBefore -1:10:43:56:" + jobChainHistoryInfo.isCompletedSuccessfulBefore());
        // Some counters for job starts between 10:00 and 14:00
        jobChainHistoryInfo = jobChainHistory.getJobChainInfo("sos/events/scheduler_event_service", "-8:10:00:00..-4:14:00:00");
        
    }

    private void report(JobSchedulerHistoryInfoEntry reportItem) {
        LOGGER.info("_____________________________");
        if (reportItem.found) {
            LOGGER.info("Name:" + reportItem.name);
            LOGGER.info("id:" + reportItem.id);
            LOGGER.info("Job chain name:" + reportItem.jobChainName);
            LOGGER.info("Top:" + reportItem.top);
            LOGGER.info("Start:" + reportItem.start);
            LOGGER.info("End:" + reportItem.end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            LOGGER.info("End:" + reportItem.end);
            LOGGER.info("Duration in seconds:" + java.time.temporal.ChronoUnit.SECONDS.between(reportItem.start, reportItem.end));
            LOGGER.info("Duration:" + reportItem.duration);
            LOGGER.info("State:" + reportItem.state);
            LOGGER.info("Error:" + reportItem.error);
        } else {
            LOGGER.info("Name:" + reportItem.name + " not found");
        }
    }

}