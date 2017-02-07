package com.sos.jitl.classes.event;

import javax.json.JsonArray;

import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public interface IJobSchedulerPluginEventHandler {

	void onActivate();

	void onPrepare(SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variableSet,
			EventHandlerSettings settings);

	void onEmptyEvent(Long eventId);

	void onNonEmptyEvent(Long eventId, JsonArray events);

	void onTornEvent(Long eventId, JsonArray events);

	void onRestart(Long eventId, JsonArray events);

	void setIdentifier(String identifier);

	void close();
}