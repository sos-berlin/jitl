package com.sos.jitl.reporting.plugin;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public interface IReportingEventHandler {
    
    void onActivate() throws Exception;
    void onPrepare(SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variableSet, SOSHibernateConnection connection, SchedulerAnswer answer) throws Exception;

    void close() throws Exception;
}
