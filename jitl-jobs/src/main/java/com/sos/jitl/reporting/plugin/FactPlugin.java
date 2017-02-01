package com.sos.jitl.reporting.plugin;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class FactPlugin extends ReportingPlugin {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactPlugin.class);

	@Inject
	public FactPlugin(SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables) {
		super(xmlCommandExecutor, variables);
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