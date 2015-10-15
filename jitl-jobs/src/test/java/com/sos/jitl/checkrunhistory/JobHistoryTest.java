package com.sos.jitl.checkrunhistory;

import static org.junit.Assert.*;
import org.junit.Test;

public class JobHistoryTest {

	@Test
	public void testJobHistory() throws Exception {
		JobHistory jobHistory = new com.sos.jitl.checkrunhistory.JobHistory("localhost",4100);
	 
		JobHistoryInfo jobHistoryInfo = jobHistory.getJobInfo("job1");

		report(jobHistoryInfo.getLastCompleted());
		report(jobHistoryInfo.running);
		report(jobHistoryInfo.lastCompletedSuccessful);
		report(jobHistoryInfo.lastComletedWithError);
		
		System.out.println ("isStartedToday:" + jobHistoryInfo.isStartedToday());
		System.out.println ("isStartedTodayCompletedSuccessful:" + jobHistoryInfo.isStartedTodayCompletedSuccessful());
		System.out.println ("isStartedTodayCompletedWithError:" + jobHistoryInfo.isStartedTodayCompletedWithError());
		System.out.println ("isStartedTodayCompleted:" + jobHistoryInfo.isStartedTodayCompleted());
		System.out.println ("isCompletedToday:" + jobHistoryInfo.isCompletedToday());
		System.out.println ("isCompletedTodaySuccessfully:" + jobHistoryInfo.isCompletedTodaySuccessful());
		System.out.println ("isCompletedTodayWithError:" + jobHistoryInfo.isCompletedTodayWithError());
		
		System.out.println ("endedAfter:" + jobHistoryInfo.endedAfter("-1:10:48:33"));
		System.out.println ("endedWithErrorAfter:" + jobHistoryInfo.endedWithErrorAfter("03:00:00"));
		System.out.println ("endedSuccessfulAfter:" + jobHistoryInfo.endedSuccessfulAfter("03:00:00"));

		System.out.println ("startedAfter:" + jobHistoryInfo.startedAfter("-1:10:48:33"));
		System.out.println ("startedWithErrorAfter:" + jobHistoryInfo.startedWithErrorAfter("03:00:00"));
		System.out.println ("startedSuccessfulAfter:" + jobHistoryInfo.startedSuccessfulAfter("03:00:00"));

		System.out.println("To check whether the job started before a time, limit the query with the time limit");
		jobHistory.setTimeLimit("-1:10:43:56");
		jobHistoryInfo = jobHistory.getJobInfo("job1","-1:10:43:56");
//		jobHistoryInfo = jobHistory.getJobInfo("job1","-1:10:43:56");
		System.out.println ("endedBefore -1:10:43:56:" + jobHistoryInfo.lastCompleted.found);
		System.out.println ("endedBeforeWithError -1:10:43:56:" + jobHistoryInfo.lastComletedWithError.found);
		System.out.println ("endedBeforeSuccessful -1:10:43:56:" + jobHistoryInfo.lastCompletedSuccessful.found);
 
	}
	
 
	private void report(JobHistoryInfoEntry reportItem) {
		System.out.println("_____________________________");
		if (reportItem.found){
			System.out.println("Name:" + reportItem.name);
			System.out.println("id:" + reportItem.id);
			System.out.println("Job name:" + reportItem.jobName);
			System.out.println("Position:" + reportItem.position);
			System.out.println("Start:" + reportItem.start);
			System.out.println("End:" + reportItem.end);
			System.out.println("Result:" + reportItem.executionResult);
			System.out.println("Message:" + reportItem.errorMessage);
			System.out.println("Error:" + reportItem.error);
			System.out.println("ErrorCode:" + reportItem.errorCode);
		}else{
			System.out.println("Name:" + reportItem.name + " not found");
		}
	}



}
