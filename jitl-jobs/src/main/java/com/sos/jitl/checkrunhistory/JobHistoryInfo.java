package com.sos.jitl.checkrunhistory;

import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
  
public class JobHistoryInfo {
 
	public JobHistoryInfoEntry lastCompleted;
	public JobHistoryInfoEntry running;
	public JobHistoryInfoEntry lastCompletedSuccessful;
	public JobHistoryInfoEntry lastCompletedWithError;

	private String startTime="0:00:00:00";
	private String endTime="0:00:00:00";
	
	public JobHistoryInfo() {
		super();
		running = new JobHistoryInfoEntry();
		running.name = "running";
		
		lastCompleted = new JobHistoryInfoEntry();
		lastCompleted.name = "last";

		lastCompletedSuccessful = new JobHistoryInfoEntry();
		lastCompletedSuccessful.name = "lastSuccessful";
		
		lastCompletedWithError = new JobHistoryInfoEntry();
		lastCompletedWithError.name = "lastWithError";
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
	   JobHistoryInfoEntry  jobHistoryInfoEntry = lastCompletedWithError;
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
	   JobHistoryInfoEntry  jobHistoryInfoEntry = lastCompletedWithError;
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
	 	   return  endedAfter(lastCompletedWithError,time);
   }
   
   public boolean endedSuccessfulAfter(String time){
	   return  endedAfter(lastCompletedSuccessful,time);
   }
   
   public boolean endedAfter(String time){
	   return  endedAfter(lastCompleted,time);
   }

   public boolean isCompletedWithErrorAfter(String time){
 	   return  endedWithErrorAfter(time);
   }

   public boolean isCompletedSuccessfulAfter(String time){
      return  endedSuccessfulAfter(time);
   }

   public boolean isCompletedAfter(String time){
      return  endedAfter(time);
   }
 
   public boolean isEndedWithErrorAfter(String time){
 	   return  endedWithErrorAfter(time);
   }

   public boolean isEndedSuccessfulAfter(String time){
      return  endedSuccessfulAfter(time);
   }

   public boolean isEndedAfter(String time){
      return  endedAfter(time);
   }

   public boolean isCompletedWithErrorBefore(){
	  return lastCompleted.found;
    }

   public boolean isCompletedSuccessfulBefore(){
	   return lastCompletedSuccessful.found;
    }

   public boolean isCompletedBefore(){
	   return lastCompletedSuccessful.found;
    }

   
   public boolean startedWithErrorAfter(String time){
	   return  startedAfter(lastCompletedWithError,time);
   }
   
   public boolean startedSuccessfulAfter(String time){
	   return  startedAfter(lastCompletedSuccessful,time);
   }
   
   public boolean startedAfter(String time){
	   return  startedAfter(getLastExecution(),time);
   }   
   
   public boolean isStartedWithErrorAfter(String time){
	   return startedWithErrorAfter(time);
   }
   
   public boolean isStartedSuccessfulAfter(String time){
	   return startedSuccessfulAfter(time);
   }
   
   public boolean isStartedAfter(String time){
	   return startedAfter(time);
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
	return lastCompletedWithError;
}      

public boolean queryHistory(String query){
	
	boolean result = false;
	JobHistoryHelper jobHistoryHelper=new JobHistoryHelper(); 
	String methodName = jobHistoryHelper.getMethodName(query);
	String time = "";
 	
	 switch (methodName.toLowerCase()) {
        //isStartedToday  				
        case "isstartedtoday":  				
       	    result = isStartedToday();
            break;
        //isStartedToday  				
        case "isstartedtodaycompletedsuccessful":  				
        	result = isStartedTodayCompletedSuccessful();
            break;
        //isStartedTodayCompletedWithError  				
        case "isstartedtodaycompletedwitherror":  				
			result = isStartedTodayCompletedWithError();
            break;
        //isStartedTodayCompleted  				
        case "isstartedtodaycompleted":  				
			result = isStartedTodayCompleted();
            break;
        //isCompletedToday  				
        case "iscompletedtoday":  				
			result = isCompletedToday();
            break;
        //isCompletedTodaySuccessful  				
        case "iscompletedtodaysuccessful":  				
			result = isCompletedTodaySuccessful();
            break;
       //isCompletedTodayWithError  			
        case "iscompletedtodaywitherror":  		
 			result = isCompletedTodayWithError();
            break;
        //isCompletedAfter  				
        case "iscompletedafter":  				
        	time = jobHistoryHelper.getTime(endTime,query);
			result = isCompletedAfter(time);                    
			break;
        //isCompletedWithErrorAfter  				
        case "iscompletedwitherrorafter":  				
        	time = jobHistoryHelper.getTime(endTime,query);
			result = isCompletedWithErrorAfter(time);                    
			break;
        //isCompletedSuccessfulAfter  				
        case "iscompletedsuccessfulafter":  				
        	time = jobHistoryHelper.getTime(endTime,query);
			result = isCompletedSuccessfulAfter(time);
			break;
        //case "isStartedAfter  				
        case "isstartedafter":  				
        	time = jobHistoryHelper.getTime(startTime,query);
			result = isStartedAfter(time);                    
			break;
        //isStartedWithErrorAfter  	
        case "isstartedwitherrorafter":  	
        	time = jobHistoryHelper.getTime(startTime,query);
			result = isStartedWithErrorAfter(time);	            	
            break;
        //isStartedSuccessfulAfter  				
        case "isstartedsuccessfulafter":  				
        	time = jobHistoryHelper.getTime(startTime,query);
			result = isStartedSuccessfulAfter(time);
        	break;
        //isCompletedBefore  				
        case "iscompletedbefore":  				
			result = isCompletedBefore();
            break;
        //isCompletedSuccessfulBefore  				
        case "iscompletedsuccessfulbefore":  				
			result = isCompletedSuccessfulBefore();
            break;
        //isCompletedWithErrorBefore  				
        case "iscompletedwitherrorbefore":  				
			result = isCompletedWithErrorBefore();
            break;
        default: throw new JobSchedulerException("unknown command: " + query);
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
