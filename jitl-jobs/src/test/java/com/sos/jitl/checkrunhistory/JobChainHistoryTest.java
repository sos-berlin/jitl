package com.sos.jitl.checkrunhistory;

import static org.junit.Assert.*;

import org.junit.Test;

public class JobChainHistoryTest {
	
	 

	@Test
	public void testJobHistory() throws Exception {
		JobChainHistory jobChainHistory = new com.sos.jitl.checkrunhistory.JobChainHistory("localhost",4000);
	 
		JobSchedulerHistoryInfo jobChainHistoryInfo = jobChainHistory.getJobChainInfo("test");

		report(jobChainHistoryInfo.getLastCompleted());
		report(jobChainHistoryInfo.getRunning());
		report(jobChainHistoryInfo.getLastCompletedSuccessful());
		report(jobChainHistoryInfo.getLastCompletedWithError());
		
		System.out.println ("isStartedToday:" + jobChainHistoryInfo.isStartedToday());
		System.out.println ("isStartedTodayCompletedSuccessful:" + jobChainHistoryInfo.isStartedTodayCompletedSuccessful());
		System.out.println ("isStartedTodayCompletedWithError:" + jobChainHistoryInfo.isStartedTodayCompletedWithError());
		System.out.println ("isStartedTodayCompleted:" + jobChainHistoryInfo.isStartedTodayCompleted());
		System.out.println ("isCompletedToday:" + jobChainHistoryInfo.isCompletedToday());
		System.out.println ("isCompletedTodaySuccessfully:" + jobChainHistoryInfo.isCompletedTodaySuccessful());
		System.out.println ("isCompletedTodayWithError:" + jobChainHistoryInfo.isCompletedTodayWithError());
		
		System.out.println ("isCompletedAfter:" + jobChainHistoryInfo.isCompletedAfter("-1:10:48:33"));
		System.out.println ("isCompletedWithErrorAfter:" + jobChainHistoryInfo.isCompletedWithErrorAfter("03:00:00"));
		System.out.println ("isCompletedSuccessfulAfter:" + jobChainHistoryInfo.isCompletedSuccessfulAfter("03:00:00"));

		System.out.println ("isStartedAfter:" + jobChainHistoryInfo.isStartedAfter("-1:10:48:33"));
		System.out.println ("isStartedWithErrorAfter:" + jobChainHistoryInfo.isStartedWithErrorAfter("03:00:00"));
		System.out.println ("isStartedSuccessfulAfter:" + jobChainHistoryInfo.isStartedSuccessfulAfter("03:00:00"));

		
		System.out.println ("isStartedToday:" + jobChainHistoryInfo.queryHistory("isStartedToday"));
		System.out.println ("isStartedTodayCompletedSuccessful:" + jobChainHistoryInfo.queryHistory("isStartedTodayCompletedSuccessful"));
		System.out.println ("isStartedTodayCompletedWithError:" + jobChainHistoryInfo.queryHistory("isStartedTodayCompletedWithError"));
		System.out.println ("isStartedTodayCompleted:" + jobChainHistoryInfo.queryHistory("isStartedTodayCompleted"));
		System.out.println ("isCompletedToday:" + jobChainHistoryInfo.queryHistory("isCompletedToday"));
		System.out.println ("isCompletedTodaySuccessfully:" + jobChainHistoryInfo.queryHistory("isCompletedTodaySuccessful"));
		System.out.println ("isCompletedTodayWithError:" + jobChainHistoryInfo.queryHistory("isCompletedTodayWithError "));
		
		System.out.println ("isCompletedAfter(-1:10:48:33):" + jobChainHistoryInfo.queryHistory("isCompletedAfter(-1:10:48:33)"));
		System.out.println ("isCompletedWithErrorAfter(03:00:00):" + jobChainHistoryInfo.queryHistory("isCompletedWithErrorAfter(03:00:00)"));
		System.out.println ("isCompletedSuccessfulAfter(03:00:00):" + jobChainHistoryInfo.queryHistory("isCompletedSuccessfulAfter(03:00:00)"));

		System.out.println ("isStartedAfter(-1:10:48:33):" + jobChainHistoryInfo.queryHistory("isStartedAfter(-1:10:48:33)"));
		System.out.println ("isStartedWithErrorAfter(03:00:00):" + jobChainHistoryInfo.queryHistory("isStartedWithErrorAfter(03:00:00)"));
		System.out.println ("isStartedSuccessfulAfter(03:00:00):" + jobChainHistoryInfo.queryHistory("isStartedSuccessfulAfter(03:00:00)"));
		
		System.out.println("To check whether the job started before a time, limit the query with the time limit");
		jobChainHistory.setTimeLimit("-1:10:43:56");
		jobChainHistoryInfo = jobChainHistory.getJobChainInfo("sos/events/scheduler_event_service","-1:10:43:56");
		System.out.println ("isCompletedBefore -1:10:43:56:" + jobChainHistoryInfo.isCompletedBefore());
		System.out.println ("isCompletedWithErrorBefore -1:10:43:56:" + jobChainHistoryInfo.isCompletedWithErrorBefore());
		System.out.println ("isCompletedSuccessfulBefore -1:10:43:56:" + jobChainHistoryInfo.isCompletedSuccessfulBefore());
 
// Some counters for job starts between 10:00 and 14:00
		
		jobChainHistoryInfo = jobChainHistory.getJobChainInfo("sos/events/scheduler_event_service","-8:10:00:00..-4:14:00:00");
		System.out.println ("Records found:" + jobChainHistory.getCount());
		System.out.println ("Completed Records found:" + jobChainHistory.getNumberOfCompleted());
		System.out.println ("CompletedSuccessfulRecords found:" + jobChainHistory.getNumberOfCompletedSuccessful());
		System.out.println ("CompletedWithError Records found:" + jobChainHistory.getNumberOfCompletedWithError());
		System.out.println ("Starts Records found:" + jobChainHistory. getNumberOfStarts());
 
		
	}
	
 
	private void report(JobSchedulerHistoryInfoEntry reportItem) {
		System.out.println("_____________________________");
		if (reportItem.found){
			System.out.println("Name:" + reportItem.name);
			System.out.println("id:" + reportItem.id);
			System.out.println("Job chain name:" + reportItem.jobChainName);
			System.out.println("Position:" + reportItem.position);
			System.out.println("Start:" + reportItem.start);
			System.out.println("End:" + reportItem.end);
			System.out.println("Duration:" + reportItem.duration);
			System.out.println("State:" + reportItem.state);
			System.out.println("Error:" + reportItem.error);
		}else{
			System.out.println("Name:" + reportItem.name + " not found");
		}
	}



}
