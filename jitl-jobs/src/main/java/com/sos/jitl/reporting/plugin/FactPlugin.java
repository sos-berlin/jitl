package com.sos.jitl.reporting.plugin;

import javax.inject.Inject;

import com.sos.jitl.classes.plugin.JobSchedulerEventPlugin;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class FactPlugin extends JobSchedulerEventPlugin {
	private static final String SCHEDULER_PARAM_NOTIFICATION = "sos.use_notification";

	@Inject
	public FactPlugin(SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables) {
		super(xmlCommandExecutor, variables);
		setIdentifier("reporting");
	}

	@Override
	public void onPrepare() {
		boolean useNotification = getUseNotification();
		super.executeOnPrepare(new FactEventHandler(useNotification));
	}

	@Override
	public void onActivate() {
		super.executeOnActivate();
	}

	@Override
	public void close() {
		super.executeClose();
	}

	private boolean getUseNotification() {
		boolean result = false;
		String param = getJobSchedulerVariable(SCHEDULER_PARAM_NOTIFICATION);
		if (param != null) {
			try {
				result = Boolean.parseBoolean(param);
			} catch (Exception e) {
			}
		}
		return result;
	}
}