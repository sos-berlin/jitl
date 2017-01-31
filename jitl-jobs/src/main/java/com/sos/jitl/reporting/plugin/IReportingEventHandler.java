package com.sos.jitl.reporting.plugin;

import javax.json.JsonArray;

import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public interface IReportingEventHandler {

	void onActivate();

	void onPrepare(SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variableSet, PluginSettings settings);

	void onEmptyEvent(Long eventId);

	void onNonEmptyEvent(Long eventId, String type, JsonArray events);

	void onTornEvent(Long eventId, String type, JsonArray events);

	void onRestart(Long eventId, String type, JsonArray events);

	void close();
}
