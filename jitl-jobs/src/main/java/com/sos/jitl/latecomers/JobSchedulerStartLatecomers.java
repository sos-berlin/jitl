package com.sos.jitl.latecomers;

import com.sos.jitl.latecomers.JobSchedulerStartLatecomers;
import com.sos.jitl.latecomers.JobSchedulerStartLatecomersOptions;
import com.sos.jitl.latecomers.classes.DailyPlanExecuter;
import com.sos.jitl.latecomers.classes.JobChainStartExecuter;
import com.sos.jitl.latecomers.classes.JobStartExecuter;
import com.sos.jitl.latecomers.classes.LateComersHelper;
import com.sos.jitl.restclient.AccessTokenProvider;
import com.sos.jitl.restclient.JobSchedulerCredentialStoreJOCParameters;
import com.sos.jitl.restclient.WebserviceCredentials;
import com.sos.joc.model.plan.PlanItem;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.messages.JSMessages;

import sos.spooler.Spooler;

public class JobSchedulerStartLatecomers extends JSJobUtilitiesClass<JobSchedulerStartLatecomersOptions> {

	protected JobSchedulerStartLatecomersOptions jobSchedulerStartLatecomersOptions = null;
	private static final String CLASSNAME = "JobSchedulerStartLatecomers";
	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerStartLatecomers.class);

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
		
		JobSchedulerCredentialStoreJOCParameters jobSchedulerCredentialStoreJOCParameters = new JobSchedulerCredentialStoreJOCParameters();
    	jobSchedulerCredentialStoreJOCParameters.setCredentialStoreEntryPath(jobSchedulerStartLatecomersOptions.credential_store_entry_path.getValue());
    	jobSchedulerCredentialStoreJOCParameters.setCredentialStoreFile(jobSchedulerStartLatecomersOptions.credential_store_file.getValue());
    	jobSchedulerCredentialStoreJOCParameters.setCredentialStoreKeyFile(jobSchedulerStartLatecomersOptions.credential_store_key_file.getValue());
    	jobSchedulerCredentialStoreJOCParameters.setCredentialStorePassword(jobSchedulerStartLatecomersOptions.credential_store_password.getValue());
    	jobSchedulerCredentialStoreJOCParameters.setJocUrl(jobSchedulerStartLatecomersOptions.jocUrl.getValue());
    	jobSchedulerCredentialStoreJOCParameters.setPassword(jobSchedulerStartLatecomersOptions.password.getValue());
    	jobSchedulerCredentialStoreJOCParameters.setUser(jobSchedulerStartLatecomersOptions.user.getValue());
    	
        jobSchedulerCredentialStoreJOCParameters.setKeyStorePassword(jobSchedulerStartLatecomersOptions.keystorePassword.getValue());
        jobSchedulerCredentialStoreJOCParameters.setKeyStorePath(jobSchedulerStartLatecomersOptions.keystorePath.getValue());
        jobSchedulerCredentialStoreJOCParameters.setKeyStoreType(jobSchedulerStartLatecomersOptions.keystoreType.getValue());
        jobSchedulerCredentialStoreJOCParameters.setTrustStorePassword(jobSchedulerStartLatecomersOptions.truststorePassword.getValue());
        jobSchedulerCredentialStoreJOCParameters.setTrustStorePath(jobSchedulerStartLatecomersOptions.truststorePath.getValue());
        jobSchedulerCredentialStoreJOCParameters.setTrustStoreType(jobSchedulerStartLatecomersOptions.truststoreType.getValue());

		
		AccessTokenProvider accessTokenProvider = new AccessTokenProvider(jobSchedulerCredentialStoreJOCParameters);
		WebserviceCredentials webserviceCredentials = new WebserviceCredentials();
		if (jocUrl == null) {
			Spooler schedulerInstance = (Spooler) objJSCommands.getSpoolerObject();
			if (schedulerInstance != null) {
				webserviceCredentials = accessTokenProvider.getAccessToken(schedulerInstance);
				jocUrl = schedulerInstance.variables().value("joc_url");
			}
		} else {
			webserviceCredentials.setAccessToken(xAccessToken);
			webserviceCredentials.setSchedulerId(schedulerId);
		}

		if (jocUrl == null) {
			jocUrl = "";
		}

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

		List<PlanItem> listOfPlanItems = dailyPlanExecuter
				.getDailyPlan(jobSchedulerStartLatecomersOptions.dayOffset.getValue());
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
						LOGGER.info("Job: " + plan.getJob() + " is late. Will be started now");
						jobStartExecuter.startJob(plan.getJob());
					}
				} else {
					if (!plan.getJob().isEmpty()) {
						LOGGER.info("Job: " + plan.getJob() + " is late but not considered in "
								+ jobSchedulerStartLatecomersOptions.jobs.getValue());
					}
				}
				if (lateComersHelper.considerOrder(plan.getJobChain(), plan.getOrderId())) {
					if (jobSchedulerStartLatecomersOptions.onlyReport.value()) {
						LOGGER.info("Order: " + plan.getJobChain() + "(" + plan.getOrderId() + ") is late");
					} else {
						LOGGER.info("Order: " + plan.getJobChain() + "(" + plan.getOrderId()
								+ ") is late. Will be started now");
						jobChainStartExecuter.startJobChain(plan.getJobChain(), plan.getOrderId());
					}
				} else {
					if (!(plan.getJobChain() + plan.getOrderId()).isEmpty()) {
						LOGGER.info("Order: " + plan.getJobChain() + "(" + plan.getOrderId()
								+ ") is late but not considered in "
								+ jobSchedulerStartLatecomersOptions.orders.getValue());
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
	public void setJSParam(String pstrKey, StringBuilder pstrValue) {
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
