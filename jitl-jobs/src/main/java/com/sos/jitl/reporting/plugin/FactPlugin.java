package com.sos.jitl.reporting.plugin;

import javax.inject.Inject;

import com.sos.jitl.classes.plugin.JobSchedulerEventPlugin;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class FactPlugin extends JobSchedulerEventPlugin {

	@Inject
	public FactPlugin(SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables) {
		super(xmlCommandExecutor, variables);
		setIdentifier("reporting");
	}

	@Override
	public void onPrepare() {
		super.executeOnPrepare(new FactEventHandler());
	}

	@Override
	public void onActivate() {
		super.executeOnActivate();
	}

	@Override
	public void close() {
		super.executeClose();
	}
}