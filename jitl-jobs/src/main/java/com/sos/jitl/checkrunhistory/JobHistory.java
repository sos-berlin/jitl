package com.sos.jitl.checkrunhistory;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import sos.spooler.Spooler;
import sos.util.SOSDate;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Answer;
import com.sos.scheduler.model.answers.ERROR;
import com.sos.scheduler.model.answers.HistoryEntry;
import com.sos.scheduler.model.commands.JSCmdShowHistory;
import com.sos.scheduler.model.commands.ShowHistory;
import com.sos.scheduler.model.exceptions.JSCommandErrorException;
 

public class JobHistory {
	private static final int NUMBER_OF_RUNS = 100;
	private static Logger logger = Logger.getLogger(JobHistory.class);
    private String host;
	private int port;
	private Spooler spooler;
	private HistoryEntry lastCompletedHistoryEntry=null;
	private HistoryEntry lastRunningHistoryEntry=null;
	private HistoryEntry lastCompletedSuccessfullHistoryEntry=null;
	private HistoryEntry lastCompletedWithErrorHistoryEntry=null;
	private int lastCompletedHistoryEntryPos;
	private int lastRunningHistoryEntryPos;
	private int lastCompletedSuccessfullHistoryEntryPos;
	private int lastCompletedWithErrorHistoryEntryPos;
 
	private String timeLimit;
 
	public JobHistory(String host_, int port_) {
		super();
		this.host = host_;
		this.port = port_;
		timeLimit = "";
 	}	 

	public JobHistory(Spooler spooler_) {
		super();
		this.spooler = spooler_;
		timeLimit = "";
 	}

	public JobHistoryInfo getJobInfo(String jobName) throws Exception{
		lastCompletedHistoryEntry=null;
		lastRunningHistoryEntry=null;
		lastCompletedSuccessfullHistoryEntry=null;
		lastCompletedWithErrorHistoryEntry=null;
		return getJobInfo(jobName,NUMBER_OF_RUNS);
	}
	
	public JobHistoryInfo getJobInfo(String jobName, String timeLimit_) throws Exception{
		lastCompletedHistoryEntry=null;
		lastRunningHistoryEntry=null;
		lastCompletedSuccessfullHistoryEntry=null;
		lastCompletedWithErrorHistoryEntry=null;		
		timeLimit = timeLimit_;
		return getJobInfo(jobName,NUMBER_OF_RUNS);
	}  
	
	public JobHistoryInfo getJobInfo(String jobName, int limit, String timeLimit_) throws Exception{
		lastCompletedHistoryEntry=null;
		lastRunningHistoryEntry=null;
		lastCompletedSuccessfullHistoryEntry=null;
		lastCompletedWithErrorHistoryEntry=null;		
		timeLimit = timeLimit_;
		return getJobInfo(jobName, limit);
	}  

	public JobHistoryInfo getJobInfo(String jobName, int numberOfRuns) throws Exception{
		getHistory(jobName,numberOfRuns);		 
        JobHistoryInfo jobHistoryInfo = new JobHistoryInfo();
        
		if (lastCompletedHistoryEntry != null){
			jobHistoryInfo.lastCompleted.found = true;
			jobHistoryInfo.lastCompleted.position = lastCompletedHistoryEntryPos; 
	        jobHistoryInfo.lastCompleted.errorMessage = lastCompletedHistoryEntry.getErrorText();
	        jobHistoryInfo.lastCompleted.executionResult = big2int(lastCompletedHistoryEntry.getExitCode());
	        jobHistoryInfo.lastCompleted.start = getDateFromString(lastCompletedHistoryEntry.getStartTime());
	        jobHistoryInfo.lastCompleted.end = getDateFromString(lastCompletedHistoryEntry.getEndTime());
	        jobHistoryInfo.lastCompleted.error = big2int(lastCompletedHistoryEntry.getError());
	        jobHistoryInfo.lastCompleted.errorCode = lastCompletedHistoryEntry.getErrorCode();
	        jobHistoryInfo.lastCompleted.id = big2int(lastCompletedHistoryEntry.getId());
	        jobHistoryInfo.lastCompleted.jobName= lastCompletedHistoryEntry.getJobName();
			 
		}else{
			jobHistoryInfo.lastCompleted.found = false;
			logger.debug(String.format("no completed job run found for the job:%s in the last %s job runs",jobName,numberOfRuns));
		}
		
		if (lastCompletedSuccessfullHistoryEntry != null){
			jobHistoryInfo.lastCompletedSuccessful.found = true;
	        jobHistoryInfo.lastCompletedSuccessful.position = lastCompletedSuccessfullHistoryEntryPos; 
	        jobHistoryInfo.lastCompletedSuccessful.errorMessage = lastCompletedSuccessfullHistoryEntry.getErrorText();
	        jobHistoryInfo.lastCompletedSuccessful.executionResult = big2int(lastCompletedSuccessfullHistoryEntry.getExitCode());
	        jobHistoryInfo.lastCompletedSuccessful.start = getDateFromString(lastCompletedSuccessfullHistoryEntry.getStartTime());
	        jobHistoryInfo.lastCompletedSuccessful.end = getDateFromString(lastCompletedSuccessfullHistoryEntry.getEndTime());
	        jobHistoryInfo.lastCompletedSuccessful.error = big2int(lastCompletedSuccessfullHistoryEntry.getError());
	        jobHistoryInfo.lastCompletedSuccessful.errorCode = lastCompletedSuccessfullHistoryEntry.getErrorCode();
	        jobHistoryInfo.lastCompletedSuccessful.id = big2int(lastCompletedSuccessfullHistoryEntry.getId());
	        jobHistoryInfo.lastCompletedSuccessful.jobName= lastCompletedSuccessfullHistoryEntry.getJobName();
			 
		}else{
			jobHistoryInfo.lastCompletedSuccessful.found = false;
			logger.debug(String.format("no successfull job run found for the job:%s in the last %s job runs",jobName,numberOfRuns));
		}
		
		if (lastCompletedWithErrorHistoryEntry != null){
			jobHistoryInfo.lastComletedWithError.found = true;
	        jobHistoryInfo.lastComletedWithError.position = lastCompletedWithErrorHistoryEntryPos; 
	        jobHistoryInfo.lastComletedWithError.errorMessage = lastCompletedWithErrorHistoryEntry.getErrorText();
	        jobHistoryInfo.lastComletedWithError.executionResult = big2int(lastCompletedWithErrorHistoryEntry.getExitCode());
	        jobHistoryInfo.lastComletedWithError.start = getDateFromString(lastCompletedWithErrorHistoryEntry.getStartTime());
	        jobHistoryInfo.lastComletedWithError.end = getDateFromString(lastCompletedWithErrorHistoryEntry.getEndTime());
	        jobHistoryInfo.lastComletedWithError.error = big2int(lastCompletedWithErrorHistoryEntry.getError());
	        jobHistoryInfo.lastComletedWithError.errorCode = lastCompletedWithErrorHistoryEntry.getErrorCode();
	        jobHistoryInfo.lastComletedWithError.id = big2int(lastCompletedWithErrorHistoryEntry.getId());
	        jobHistoryInfo.lastComletedWithError.jobName= lastCompletedWithErrorHistoryEntry.getJobName();
			 
		}else{
			jobHistoryInfo.lastComletedWithError.found = false;
			logger.debug(String.format("no job runs with error found for the job:%s in the last %s job runs",jobName,numberOfRuns));
		}
		
		if (lastRunningHistoryEntry != null){
			jobHistoryInfo.running.found = true;
	        jobHistoryInfo.running.position = lastRunningHistoryEntryPos; 
	        jobHistoryInfo.running.errorMessage = lastRunningHistoryEntry.getErrorText();
	        jobHistoryInfo.running.executionResult = big2int(lastRunningHistoryEntry.getExitCode());
	        jobHistoryInfo.running.start = getDateFromString(lastRunningHistoryEntry.getStartTime());
	        jobHistoryInfo.running.end = getDateFromString(lastRunningHistoryEntry.getEndTime());
	        jobHistoryInfo.running.error = big2int(lastRunningHistoryEntry.getError());
	        jobHistoryInfo.running.errorCode = lastRunningHistoryEntry.getErrorCode();
	        jobHistoryInfo.running.id = big2int(lastRunningHistoryEntry.getId());
	        jobHistoryInfo.running.jobName= lastRunningHistoryEntry.getJobName();

			 
		}else{
			jobHistoryInfo.running.found = false;
			logger.debug(String.format("no running jobs found for the job:%s in the last %s job runs",jobName,numberOfRuns));
		}
		return jobHistoryInfo;
	}
	
	private int big2int(BigInteger b){
		if (b==null){
			return -1;
		}else{
			return b.intValue();
		}
	}

	private Date getDateFromString(String inDateTime) throws Exception{
		Date dateResult=null;
		if (inDateTime != null ){
			if(inDateTime.endsWith("Z")) {
				DateTimeFormatter dateTimeFormatter  =  DateTimeFormat.forPattern("yyyy-MM-dd'T'H:mm:ss.SSSZ");
				DateTime dateTime = dateTimeFormatter.parseDateTime(inDateTime.replaceFirst("Z", "+00:00"));
				dateResult = dateTime.toDate();
	 		}
			else {
				 dateResult = SOSDate.getDate(inDateTime, SOSDate.dateTimeFormat);
			}
		}
		return dateResult;
	}
	
	private boolean isInTimeLimit(HistoryEntry historyItem){
		if (timeLimit.equals("")){
			return true;
		}
		 
	    if (timeLimit.length() == 8){
		   timeLimit = "0:" + timeLimit;
		}	   
		   
		JobSchedulerCheckRunHistoryOptions options = new JobSchedulerCheckRunHistoryOptions();
		options.start_time.Value(timeLimit);
			 
		DateTime limit = new DateTime(options.start_time.getDateObject());
	    DateTime ended = new DateTime(historyItem.getEndTime()); 
		return ended.toLocalDateTime().isBefore(limit.toLocalDateTime());
	}
	 
	private void getHistory(String jobName,int numberOfRuns) throws Exception{
 
		SchedulerObjectFactory jsFactory = new SchedulerObjectFactory();
		jsFactory.initMarshaller(ShowHistory.class);
		JSCmdShowHistory showHistory = jsFactory.createShowHistory();
			
		showHistory.setJob(jobName);
		showHistory.setPrev(BigInteger.valueOf(numberOfRuns));
		Answer answer = null;
			
		if (spooler == null){
			jsFactory.Options().ServerName.Value(host);
			jsFactory.Options().PortNumber.value(port);
			showHistory.run();
			answer = showHistory.getAnswer();
		}else{
			showHistory.getAnswerFromSpooler(spooler);
			answer = showHistory.getAnswer();
		}
			
		if(answer != null) {
			ERROR error = answer.getERROR();
			if(error != null) {
				throw new JSCommandErrorException(error.getText());
			}
			List<HistoryEntry> jobHistoryEntries = answer.getHistory().getHistoryEntry();
			if(jobHistoryEntries.size() == 0) {
				String msg = "No entries found for job:" + jobName;
				logger.error(msg);
				throw new JobSchedulerException(msg);
			}
			else {
				int pos = 0;
				for (HistoryEntry historyItem : jobHistoryEntries) {
 
					if (isInTimeLimit(historyItem)){
						if ((historyItem.getEndTime() != null) ){
							if (lastCompletedHistoryEntry == null){
							    lastCompletedHistoryEntry = historyItem;
							    lastCompletedHistoryEntryPos = pos;
							}
							if ((lastCompletedSuccessfullHistoryEntry == null) && (historyItem.getExitCode().intValue() == 0)){
								lastCompletedSuccessfullHistoryEntry = historyItem;
								lastCompletedSuccessfullHistoryEntryPos = pos;
							}
							if ((lastCompletedWithErrorHistoryEntry == null) && (historyItem.getExitCode().intValue() != 0)){
								lastCompletedWithErrorHistoryEntry = historyItem;
								lastCompletedWithErrorHistoryEntryPos = pos;
							}
						}else{
							if (lastRunningHistoryEntry == null){
								lastRunningHistoryEntry = historyItem;
								lastRunningHistoryEntryPos = pos;
							}
						}
					}
				}
 			}
		} else {
			throw new JobSchedulerException(String.format("No answer from JobScheduler %s:%s",host,port));
		}
	}


	public String getTimeLimit() {
		return timeLimit;
	}


	public void setTimeLimit(String timeLimit) {
		this.timeLimit = timeLimit;
	}

	 
	 
 }
