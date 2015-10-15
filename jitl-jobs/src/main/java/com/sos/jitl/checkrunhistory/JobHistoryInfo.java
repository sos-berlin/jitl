package com.sos.jitl.checkrunhistory;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
 
public class JobHistoryInfo {
 
	public JobHistoryInfoEntry lastCompleted;
	public JobHistoryInfoEntry running;
	public JobHistoryInfoEntry lastCompletedSuccessful;
	public JobHistoryInfoEntry lastComletedWithError;
 
 	public JobHistoryInfo() {
		super();
		running = new JobHistoryInfoEntry();
		running.name = "running";
		
		lastCompleted = new JobHistoryInfoEntry();
		lastCompleted.name = "last";

		lastCompletedSuccessful = new JobHistoryInfoEntry();
		lastCompletedSuccessful.name = "lastSuccessful";
		
		lastComletedWithError = new JobHistoryInfoEntry();
		lastComletedWithError.name = "lastWithError";
 	}
  
 	private JobHistoryInfoEntry getYoungerEntry(JobHistoryInfoEntry e1,JobHistoryInfoEntry e2){
 		if (e1 != null && ! e1.found){
 			return e2;
 		}
 		
 		if (e2 != null && ! e2.found){
 			return e1;
 		}
 		
 		if (e2 == null && e1 == null){
 			return null;
 		}
 		
 		if (e1 == null && e2 != null){
 			return e2;
 		}

 		if (e2 == null && e1 != null){
 			return e1;
 		}
 		
 		if (e1.position < e2.position){
			return e1;
		}else{
			return e2;
		}
 	}
 	
   private boolean isToday(Date d){
	   Date today = new Date();
	   if (d == null){
		   return false;
	   }else{
		   return (DateUtils.isSameDay(today,d));
	   }
   }
 	
   public JobHistoryInfoEntry getLastExecution() {
		JobHistoryInfoEntry jobHistoryInfoEntry = getYoungerEntry(lastCompleted,running);
   		return jobHistoryInfoEntry;
	}	
     
    
   //Includes running and ended jobs. Looking for start time
   public boolean isStartedToday(){
	   JobHistoryInfoEntry  jobHistoryInfoEntry = getLastExecution();
	   return  (jobHistoryInfoEntry != null) && (isToday(jobHistoryInfoEntry.start));
   }
		   
   //Includes successful ended jobs. Looking for start time
   public boolean isStartedTodayCompletedSuccessful(){
	   JobHistoryInfoEntry  jobHistoryInfoEntry = lastCompletedSuccessful;
	   return  (jobHistoryInfoEntry != null) && (isToday(jobHistoryInfoEntry.start));
   }

   //Includes ended with error jobs. Looking for start time
   public boolean isStartedTodayCompletedWithError(){
	   JobHistoryInfoEntry  jobHistoryInfoEntry = lastComletedWithError;
	   return  (jobHistoryInfoEntry != null) && (isToday(jobHistoryInfoEntry.start));
   }
   
   //Includes ended jobs. Looking for start time
   public boolean isStartedTodayCompleted(){
	   JobHistoryInfoEntry  jobHistoryInfoEntry = lastCompleted;
	   return  (jobHistoryInfoEntry != null) && (isToday(jobHistoryInfoEntry.start));
   }
		 
   //Includes ended jobs. Looking for end time
   public boolean isCompletedToday(){
	   return  (lastCompleted != null) && (isToday(lastCompleted.end));	   
   }

   //Includes successfull ended jobs. Looking for end time
   public boolean isCompletedTodaySuccessful(){
	   JobHistoryInfoEntry  jobHistoryInfoEntry = lastCompletedSuccessful;
	   return  (jobHistoryInfoEntry != null) && (isToday(jobHistoryInfoEntry.end));   
   }   
 
   //Includes with error ended jobs. Looking for end time
   public boolean isCompletedTodayWithError(){
	   JobHistoryInfoEntry  jobHistoryInfoEntry = lastComletedWithError;
	   return  (jobHistoryInfoEntry != null) && (isToday(jobHistoryInfoEntry.end));   
   }
   
   private boolean endedAfter(JobHistoryInfoEntry jobHistoryInfoEntry, String time){
	   if (jobHistoryInfoEntry.end == null){
		   return false;
	   }
	 
	   if (time.length() == 8){
		   time = "0:" + time;
	   }	   
	   JobSchedulerCheckRunHistoryOptions options = new JobSchedulerCheckRunHistoryOptions();
	   options.start_time.Value(time);
		 
	   DateTime limit = new DateTime(options.start_time.getDateObject());
       DateTime ended = new DateTime(jobHistoryInfoEntry.end); 
	   return limit.toLocalDateTime().isBefore(ended.toLocalDateTime());
   }
   
   private boolean startedAfter(JobHistoryInfoEntry jobHistoryInfoEntry, String time){
	   if (jobHistoryInfoEntry.end == null){
		   return false;
	   }
	   
	   if (time.length() == 8){
		   time = "0:" + time;
	   }
	   JobSchedulerCheckRunHistoryOptions options = new JobSchedulerCheckRunHistoryOptions();
	   options.start_time.Value(time);
		 
	   DateTime limit = new DateTime(options.start_time.getDateObject());
       DateTime ended = new DateTime(jobHistoryInfoEntry.start); 
	   return limit.toLocalDateTime().isBefore(ended.toLocalDateTime());
   }
   
   public boolean endedWithErrorAfter(String time){
	 	   return  endedAfter(lastComletedWithError,time);
   }
   
   public boolean endedSuccessfulAfter(String time){
	   return  endedAfter(lastCompletedSuccessful,time);
   }
   
   public boolean endedAfter(String time){
	   return  endedAfter(lastCompleted,time);
   }
   
   
   public boolean startedWithErrorAfter(String time){
	   return  startedAfter(lastComletedWithError,time);
   }
   
   public boolean startedSuccessfulAfter(String time){
	   return  startedAfter(lastCompletedSuccessful,time);
   }
   
   public boolean startedAfter(String time){
	   return  startedAfter(getLastExecution(),time);
   }   
   

public JobHistoryInfoEntry getLastCompleted() {
	return lastCompleted;
}

public JobHistoryInfoEntry getRunning() {
	return running;
}

public JobHistoryInfoEntry getLastCompletedSuccessful() {
	return lastCompletedSuccessful;
}

public JobHistoryInfoEntry getLastCompletedWithError() {
	return lastComletedWithError;
}      
  	
}
