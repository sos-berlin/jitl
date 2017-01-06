package com.sos.jitl.reporting.plugin;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public interface IReportingEventHandler {

	void onActivate();

	void onPrepare(SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variableSet, SchedulerAnswer answer,
			SOSHibernateConnection reportingConnection, SOSHibernateConnection schedulerConnection);

	void close();
}
