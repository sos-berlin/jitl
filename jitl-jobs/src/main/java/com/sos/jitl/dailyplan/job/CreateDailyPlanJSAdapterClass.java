package com.sos.jitl.dailyplan.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Spooler;

public class CreateDailyPlanJSAdapterClass extends JobSchedulerJobAdapter {

	private static final int DEFAULT_DAYS_OFFSET = 31;
	private static final String CLASSNAME = "CreateDailyScheduleJSAdapterClass";
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateDailyPlanJSAdapterClass.class);

	@Override
	public boolean spooler_init() {
		final String conMethodName = CLASSNAME + "::spooler_init";
		LOGGER.debug(String.format(getMessages().getMsg("JSJ-I-110"), conMethodName));
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() throws Exception {
		final String conMethodName = CLASSNAME + "::spooler_process";
		LOGGER.debug(String.format(getMessages().getMsg("JSJ-I-110"), conMethodName));
		try {
			super.spooler_process();
			doProcessing();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			LOGGER.debug("Exception:" + e.getMessage(), e);
			return false;
		}
		return spooler_task.job().order_queue() != null;
	}

	private void doProcessing() throws Exception {
		final String conMethodName = CLASSNAME + "::doProcessing";
		LOGGER.debug(String.format(getMessages().getMsg("JSJ-I-110"), conMethodName));
		CreateDailyPlan createDailyPlan = new CreateDailyPlan();
		CreateDailyPlanOptions createDailyPlanOptions = createDailyPlan.getOptions();
		createDailyPlanOptions.setAllOptions(getSchedulerParameterAsProperties());

		if (createDailyPlanOptions.dayOffset.isNotDirty()) {
			createDailyPlanOptions.dayOffset.value(DEFAULT_DAYS_OFFSET);
		}

		Spooler spooler = (Spooler) getSpoolerObject();

		String configuration_file = "";
		if (createDailyPlanOptions.getItem("configuration_file") != null) {
			LOGGER.debug("configuration_file from param");
			configuration_file = createDailyPlanOptions.configuration_file.getValue();
		} else {
			LOGGER.debug("configuration_file from scheduler");
			configuration_file = getHibernateConfigurationReporting().toFile().getAbsolutePath();
		}

		createDailyPlanOptions.configuration_file.setValue(configuration_file);
		createDailyPlanOptions.checkMandatory();
		createDailyPlan.setJSJobUtilites(this);
		createDailyPlan.setSpooler(spooler);
		createDailyPlan.setSchedulerId(spooler.id());
		createDailyPlan.Execute();
	}

}
