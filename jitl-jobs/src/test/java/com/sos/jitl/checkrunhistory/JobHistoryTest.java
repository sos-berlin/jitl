package com.sos.jitl.checkrunhistory;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sos.scheduler.model.answers.HistoryEntry;

public class JobHistoryTest {
	
	

	@Test
	public void testJobHistory() throws Exception {
		JobHistory jobHistory = new com.sos.jitl.checkrunhistory.JobHistory("localhost",4000);
	 
		JobSchedulerHistoryInfo jobHistoryInfo = jobHistory.getJobInfo("job1");

		report(jobHistoryInfo.getLastCompleted());
		report(jobHistoryInfo.running);
		report(jobHistoryInfo.lastCompletedSuccessful);
		report(jobHistoryInfo.lastCompletedWithError);
		
		System.out.println ("isStartedToday:" + jobHistoryInfo.isStartedToday());
		System.out.println ("isStartedTodayCompletedSuccessful:" + jobHistoryInfo.isStartedTodayCompletedSuccessful());
		System.out.println ("isStartedTodayCompletedWithError:" + jobHistoryInfo.isStartedTodayCompletedWithError());
		System.out.println ("isStartedTodayCompleted:" + jobHistoryInfo.isStartedTodayCompleted());
		System.out.println ("isCompletedToday:" + jobHistoryInfo.isCompletedToday());
		System.out.println ("isCompletedTodaySuccessfully:" + jobHistoryInfo.isCompletedTodaySuccessful());
		System.out.println ("isCompletedTodayWithError:" + jobHistoryInfo.isCompletedTodayWithError());
		
		System.out.println ("isCompletedAfter:" + jobHistoryInfo.isCompletedAfter("-1:10:48:33"));
		System.out.println ("isCompletedWithErrorAfter:" + jobHistoryInfo.isCompletedWithErrorAfter("03:00:00"));
		System.out.println ("isCompletedSuccessfulAfter:" + jobHistoryInfo.isCompletedSuccessfulAfter("03:00:00"));

		System.out.println ("isStartedAfter:" + jobHistoryInfo.isStartedAfter("-1:10:48:33"));
		System.out.println ("isStartedWithErrorAfter:" + jobHistoryInfo.isStartedWithErrorAfter("03:00:00"));
		System.out.println ("isStartedSuccessfulAfter:" + jobHistoryInfo.isStartedSuccessfulAfter("03:00:00"));

		
		System.out.println ("isStartedToday:" + jobHistoryInfo.queryHistory("isStartedToday"));
		System.out.println ("isStartedTodayCompletedSuccessful:" + jobHistoryInfo.queryHistory("isStartedTodayCompletedSuccessful"));
		System.out.println ("isStartedTodayCompletedWithError:" + jobHistoryInfo.queryHistory("isStartedTodayCompletedWithError"));
		System.out.println ("isStartedTodayCompleted:" + jobHistoryInfo.queryHistory("isStartedTodayCompleted"));
		System.out.println ("isCompletedToday:" + jobHistoryInfo.queryHistory("isCompletedToday"));
		System.out.println ("isCompletedTodaySuccessfully:" + jobHistoryInfo.queryHistory("isCompletedTodaySuccessful"));
		System.out.println ("isCompletedTodayWithError:" + jobHistoryInfo.queryHistory("isCompletedTodayWithError "));
		
		System.out.println ("isCompletedAfter:" + jobHistoryInfo.queryHistory("isCompletedAfter(-1:10:48:33)"));
		System.out.println ("isCompletedWithErrorAfter:" + jobHistoryInfo.queryHistory("isCompletedWithErrorAfter(03:00:00)"));
		System.out.println ("isCompletedSuccessfulAfter:" + jobHistoryInfo.queryHistory("isCompletedSuccessfulAfter(03:00:00)"));

		System.out.println ("isStartedAfter:" + jobHistoryInfo.queryHistory("isStartedAfter(-1:10:48:33)"));
		System.out.println ("isStartedWithErrorAfter:" + jobHistoryInfo.queryHistory("isStartedWithErrorAfter(03:00:00)"));
		System.out.println ("isStartedSuccessfulAfter:" + jobHistoryInfo.queryHistory("isStartedSuccessfulAfter(03:00:00)"));
		
		System.out.println("To check whether the job started before a time, limit the query with the time limit");
		jobHistory.setTimeLimit("-1:10:43:56");
		jobHistoryInfo = jobHistory.getJobInfo("job1","-1:10:43:56");
//		jobHistoryInfo = jobHistory.getJobInfo("job1","-1:10:43:56");
		System.out.println ("isCompletedBefore -1:10:43:56:" + jobHistoryInfo.isCompletedBefore());
		System.out.println ("isCompletedWithErrorBefore -1:10:43:56:" + jobHistoryInfo.isCompletedWithErrorBefore());
		System.out.println ("isCompletedSuccessfulBefore -1:10:43:56:" + jobHistoryInfo.isCompletedSuccessfulBefore());
 
// Some counters for job starts between 10:00 and 14:00
		
		jobHistoryInfo = jobHistory.getJobInfo("job1","-8:10:00:00..-4:14:00:00");
		System.out.println ("Records found:" + jobHistory.getCount());
		System.out.println ("Completed Records found:" + jobHistory.getNumberOfCompleted());
		System.out.println ("CompletedSuccessfulRecords found:" + jobHistory.getNumberOfCompletedSuccessful());
		System.out.println ("CompletedWithError Records found:" + jobHistory.getNumberOfCompletedWithError());
		System.out.println ("Starts Records found:" + jobHistory. getNumberOfStarts());
 
		
	}
	
 
	private void report(JobSchedulerHistoryInfoEntry reportItem) {
		System.out.println("_____________________________");
		if (reportItem.found){
			System.out.println("Name:" + reportItem.name);
			System.out.println("id:" + reportItem.id);
			System.out.println("Job name:" + reportItem.jobName);
			System.out.println("Position:" + reportItem.position);
			System.out.println("Start:" + reportItem.start);
			System.out.println("End:" + reportItem.end);
			System.out.println("Duration:" + reportItem.duration);
			System.out.println("Result:" + reportItem.executionResult);
			System.out.println("Message:" + reportItem.errorMessage);
			System.out.println("Error:" + reportItem.error);
			System.out.println("ErrorCode:" + reportItem.errorCode);
		}else{
			System.out.println("Name:" + reportItem.name + " not found");
		}
	}



}
