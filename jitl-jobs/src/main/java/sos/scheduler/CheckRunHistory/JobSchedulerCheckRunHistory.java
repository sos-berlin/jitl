package sos.scheduler.CheckRunHistory;

import java.util.Locale;

import org.apache.log4j.Logger;

import sos.spooler.Mail;
import sos.spooler.Spooler;

import com.sos.JSHelper.Basics.IJSCommands;
import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.jitl.checkrunhistory.JobHistory;
import com.sos.jitl.checkrunhistory.JobHistoryHelper;
import com.sos.jitl.checkrunhistory.JobHistoryInfo;
import com.sos.localization.Messages;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerCheckRunHistory extends JSToolBox implements JSJobUtilities, IJSCommands {
	private final String							conClassName		= "JobSchedulerCheckRunHistory";						//$NON-NLS-1$
	private static Logger							logger				= Logger.getLogger(JobSchedulerCheckRunHistory.class);
	protected JobSchedulerCheckRunHistoryOptions	objOptions			= null;
	private JSJobUtilities							objJSJobUtilities	= this;
	private IJSCommands								objJSCommands		= this;

 
	public JobSchedulerCheckRunHistory() {
		super();
		Messages = new Messages("com_sos_scheduler_messages", Locale.getDefault());
	}
	 
	public JobSchedulerCheckRunHistoryOptions options() {
		if (objOptions == null) {
			objOptions = new JobSchedulerCheckRunHistoryOptions();
		}
		return objOptions;
	}
 
	public JobSchedulerCheckRunHistoryOptions Options(final JobSchedulerCheckRunHistoryOptions pobjOptions) {
		objOptions = pobjOptions;
		return objOptions;
	}
  
	public JobSchedulerCheckRunHistory Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$
		logger.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
		boolean result = false;
		
		options().CheckMandatory();
		logger.debug(options().toString());

		try {
 			
    		String startTime = "00:00:00";
			String endTime = "00:00:00";
 			
			String query = options().query.Value();
			
			JobHistoryHelper jobHistoryHelper = new JobHistoryHelper();
			String methodName = jobHistoryHelper.getMethodName(options().query.Value());
 			
			if (options().start_time.isDirty()){
			    startTime =  options().start_time.Value();
			}
			
			if (options().end_time.isDirty()){
			    endTime =  options().end_time.Value();
			}
			
			String jobName = options().jobName.Value();
			String message = options().message.Value();
			String mailTo  = options().mail_to.Value();
			String mailCc  = options().mail_cc.Value();
			String mailBcc = options().mail_bcc.Value();
			
			message = Messages.getMsg("JCH_T_0001", jobName, myReplaceAll(message,"\\[?JOB_NAME\\]?", jobName));
 			
	    	Spooler schedulerInstance = (Spooler) objJSCommands.getSpoolerObject();
	    	
	    	if (schedulerInstance != null){
	    		Mail mail = schedulerInstance.log().mail();
				if(isNotEmpty(mailTo)) {
					mail.set_to(mailTo);
				}
				if(isNotEmpty(mailCc)) {
					mail.set_cc(mailCc);
				}
				if(isNotEmpty(mailBcc)) {
					mail.set_bcc(mailBcc);
				}
				if(isNotEmpty(message)) {
					mail.set_subject(message);
				}
	    	}
	  
			JobHistory jobHistory=null;
			if (options().schedulerPort.value() == 0 && options().schedulerHostName.Value().length() == 0){
				jobHistory = new JobHistory(schedulerInstance);
			}else{
				jobHistory = new JobHistory(options().schedulerHostName.Value(),options().schedulerPort.value());
			}
 			
			if (methodName.equalsIgnoreCase("isCompletedBefore") || methodName.equalsIgnoreCase("isCompletedSuccessfulBefore") || methodName.equalsIgnoreCase("isCompletedWithErrorBefore")){
				String time = jobHistoryHelper.getTime(endTime,query);
				jobHistory.setTimeLimit(time);
			}
			
			JobHistoryInfo jobHistoryInfo = jobHistory.getJobInfo(jobName);
			 
 			jobHistoryInfo.setEndTime(endTime);
			jobHistoryInfo.setStartTime(startTime);
			System.out.println("query:" + query);
			
			result = jobHistoryInfo.queryHistory(query);
 
			options().result.value(result);
			
			if (jobHistoryInfo.lastCompleted.error != 0){
				logger.info(Messages.getMsg("JCH_I_0001", jobName, jobHistoryInfo.lastCompleted.end));
				logger.info(Messages.getMsg("JCH_I_0002", jobHistoryInfo.lastCompleted.errorMessage));
				logger.info(Messages.getMsg("JCH_I_0003", jobName, jobHistoryInfo.lastCompletedSuccessful.end));
			}else{
				logger.info(Messages.getMsg("JCH_I_0001", jobName, jobHistoryInfo.lastCompleted.end + " successfully"));
			}
			
			if(!result) {
				message = message + " " + methodName +"=false";
				logger.error(message);
 				throw new JobSchedulerException(message);
			} 
			
		}
		catch (Exception e) {
			logger.error(Messages.getMsg("JSJ-F-107", conMethodName), e);
			throw e;
		}
		return this;
	}

	@Override
	public String myReplaceAll(final String pstrSourceString, final String pstrReplaceWhat, final String pstrReplaceWith) {
		String newReplacement = pstrReplaceWith.replaceAll("\\$", "\\\\\\$");
		return pstrSourceString.replaceAll("(?m)" + pstrReplaceWhat, newReplacement);
	}

	 
	@Override
	public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
		logger.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
		return pstrString2Modify;
	}

 
	@Override
	public void setJSParam(final String pstrKey, final String pstrValue) {}

	@Override
	public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {}

	 
	@Override
	public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
		if (pobjJSJobUtilities == null) {
			objJSJobUtilities = this;
		} else {
			objJSJobUtilities = pobjJSJobUtilities;
		}
		logger.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
	}

	 
	public void setJSCommands(final IJSCommands pobjJSCommands) {
		if (pobjJSCommands == null) {
			objJSCommands = this;
		} else {
			objJSCommands = pobjJSCommands;
		}
		logger.debug("pobjJSCommands = " + pobjJSCommands.getClass().getName());
	}

	@Override
	public String getCurrentNodeName() {
		return null;
	}

	@Override
	public Object getSpoolerObject() {
		return null;
	}

	@Override
	public String executeXML(final String pstrJSXmlCommand) {
		return null;
	}

	@Override
	public void setStateText(final String pstrStateText) {}

	@Override
	public void setCC(final int pintCC) {}

	@Override public void setNextNodeState(final String pstrNodeName) {}


} // class JobSchedulerCheckRunHistory