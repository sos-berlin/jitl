package com.sos.jitl.latecomers;

import com.sos.jitl.latecomers.JobSchedulerStartLatecomers;
import com.sos.jitl.latecomers.JobSchedulerStartLatecomersOptions;
import com.sos.jitl.latecomers.classes.DailyPlanExecuter;
import com.sos.jitl.latecomers.classes.JobChainStartExecuter;
import com.sos.jitl.latecomers.classes.JobStartExecuter;
import com.sos.jitl.latecomers.classes.LateComersHelper;
import com.sos.jitl.restclient.ApiAccessToken;
import com.sos.jitl.restclient.WebserviceCredentials;
import com.sos.joc.model.plan.PlanItem;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.messages.JSMessages;
import sos.spooler.Spooler;

public class JobSchedulerStartLatecomers extends JSJobUtilitiesClass<JobSchedulerStartLatecomersOptions> {

	protected JobSchedulerStartLatecomersOptions jobSchedulerStartLatecomersOptions = null;
	private static final String CLASSNAME = "JobSchedulerStartLatecomers";
	private static final Logger LOGGER = Logger.getLogger(JobSchedulerStartLatecomers.class);

	// Only for JUnit Test
	private String jocUrl;
	private String schedulerId;
	private String xAccessToken;

	public JobSchedulerStartLatecomers() {
		super(new JobSchedulerStartLatecomersOptions());
	}

	public JobSchedulerStartLatecomersOptions getOptions() {
		if (jobSchedulerStartLatecomersOptions == null) {
			jobSchedulerStartLatecomersOptions = new JobSchedulerStartLatecomersOptions();
		}
		return jobSchedulerStartLatecomersOptions;
	}

	public JobSchedulerStartLatecomersOptions getOptions(final JobSchedulerStartLatecomersOptions options) {
		jobSchedulerStartLatecomersOptions = options;
		return jobSchedulerStartLatecomersOptions;
	}

	public JobSchedulerStartLatecomers execute() throws Exception {
		final String METHODNAME = CLASSNAME + "::execute";

		LOGGER.debug(String.format(JSMessages.JSJ_I_110.get(), METHODNAME));
		getOptions().checkMandatory();
		LOGGER.debug(getOptions().toString());
		LateComersHelper lateComersHelper = new LateComersHelper(jobSchedulerStartLatecomersOptions);

		if (jocUrl == null) {
			Spooler schedulerInstance = (Spooler) objJSCommands.getSpoolerObject();
			xAccessToken = schedulerInstance.variables().value("X-Access-Token");
			jocUrl = schedulerInstance.variables().value("joc_url");
			schedulerId = schedulerInstance.id();
		}

		if (jocUrl == null) {
			jocUrl = "";
		}

		WebserviceCredentials webserviceCredentials = new WebserviceCredentials();
		webserviceCredentials.setSchedulerId(schedulerId);

		ApiAccessToken apiAccessToken = new ApiAccessToken(jocUrl);

		apiAccessToken.setJocUrl(jocUrl);
		if (xAccessToken == null) {
			xAccessToken = "";
		}

		if (!apiAccessToken.isValidAccessToken(xAccessToken)) {
			throw new Exception("no valid access token found");
		}

		webserviceCredentials.setAccessToken(xAccessToken);

		DailyPlanExecuter dailyPlanExecuter = null;
		JobStartExecuter jobStartExecuter = null;
		JobChainStartExecuter jobChainStartExecuter = null;

		dailyPlanExecuter = new DailyPlanExecuter(jocUrl);
		dailyPlanExecuter.setSchedulerId(webserviceCredentials.getSchedulerId());
		dailyPlanExecuter.login(webserviceCredentials.getAccessToken());

		jobStartExecuter = new JobStartExecuter(jocUrl);
		jobStartExecuter.setSchedulerId(webserviceCredentials.getSchedulerId());
		jobStartExecuter.login(dailyPlanExecuter.getAccessToken());

		jobChainStartExecuter = new JobChainStartExecuter(jocUrl);
		jobChainStartExecuter.setSchedulerId(webserviceCredentials.getSchedulerId());
		jobChainStartExecuter.login(dailyPlanExecuter.getAccessToken());
		
		ArrayList<PlanItem> listOfPlanItems = dailyPlanExecuter.getDailyPlan(jobSchedulerStartLatecomersOptions.dayOffset.getValue());
		if (listOfPlanItems != null) {

			for (PlanItem plan : listOfPlanItems) {
				if (lateComersHelper.ignoreFolder(lateComersHelper.getParent(plan.getJob()))) {
					LOGGER.info("Job: " + plan.getJob() + " is late but will not be started as in folder"
							+ jobSchedulerStartLatecomersOptions.getIgnoreFolderList().getValue());
					continue;
				}
				if (lateComersHelper.ignoreFolder(lateComersHelper.getParent(plan.getJobChain()))) {
					LOGGER.info("JobChain: " + plan.getJobChain() + " is late but will not be started as in folder"
							+ jobSchedulerStartLatecomersOptions.getIgnoreFolderList().getValue());
					continue;
				}
				if (lateComersHelper.ignoreOrder(plan.getJobChain(), plan.getOrderId())) {
					LOGGER.info("Order: " + plan.getJobChain() + ")" + plan.getOrderId() + ")"
							+ " is late but will not be started as in list of ignored orders:"
							+ jobSchedulerStartLatecomersOptions.getIgnoreOrderList().getValue());
					continue;
				}
				if (lateComersHelper.ignoreJob(plan.getJob())) {
					LOGGER.info("Job: " + plan.getJob() + " is late but will not be started as in list of ignored jobs:"
							+ jobSchedulerStartLatecomersOptions.getIgnoreJobList().getValue());
					continue;
				}

				if (lateComersHelper.considerJob(plan.getJob())) {
					if (jobSchedulerStartLatecomersOptions.onlyReport.value()) {
						LOGGER.info("Job: " + plan.getJob() + " is late");
					} else {
						jobStartExecuter.startJob(plan.getJob());
					}
				} else {
					LOGGER.info("Job: " + plan.getJob() + " is late but not considered");
				}
				if (lateComersHelper.considerOrder(plan.getJobChain(), plan.getOrderId())) {
					if (jobSchedulerStartLatecomersOptions.onlyReport.value()) {
						LOGGER.info("Order: " + plan.getJobChain() + "(" + plan.getOrderId() + ") is late");
						jobChainStartExecuter.startJobChain(plan.getJobChain(), plan.getOrderId());
					} else {
						LOGGER.info("Order: " + plan.getJobChain() + "(" + plan.getOrderId()
								+ ") is late but not considered");
					}
				}

			}
		}

		return this;

	}

	@Override
	public String replaceSchedulerVars(String pstrString2Modify) {
		LOGGER.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
		return pstrString2Modify;
	}

	@Override
	public void setJSParam(String pstrKey, String pstrValue) {
	}

	@Override
	public void setJSParam(String pstrKey, StringBuffer pstrValue) {
	}

	public void setJocUrl(String jocUrl) {
		this.jocUrl = jocUrl;
	}

	public void setSchedulerId(String schedulerId) {
		this.schedulerId = schedulerId;
	}

	public void setxAccessToken(String xAccessToken) {
		this.xAccessToken = xAccessToken;
	}

}
