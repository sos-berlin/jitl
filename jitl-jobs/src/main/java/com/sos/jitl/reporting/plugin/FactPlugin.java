package com.sos.jitl.reporting.plugin;

import javax.inject.Inject;

import com.sos.jitl.classes.plugin.JobSchedulerEventPlugin;
import com.sos.scheduler.engine.eventbus.ColdEventBus;
import com.sos.scheduler.engine.kernel.Scheduler;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

import sos.scheduler.job.JobSchedulerJob;

public class FactPlugin extends JobSchedulerEventPlugin {

    private final FactEventHandler eventHandler;

    @Inject
    public FactPlugin(Scheduler scheduler, SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables, ColdEventBus eventBus) {
        super(scheduler, xmlCommandExecutor, variables);
        setIdentifier("reporting");

        eventHandler = new FactEventHandler(xmlCommandExecutor, eventBus);
        eventHandler.setIdentifier(getIdentifier());
    }

    @Override
    public void onPrepare() {
        //do nothing
    }

    @Override
    public void onActivate() {
        eventHandler.setUseNotificationPlugin(getUseNotification());
        super.executeOnActivate(eventHandler);
    }

    @Override
    public void close() {
        super.executeClose(eventHandler);
    }

    private boolean getUseNotification() {
        boolean result = false;
        String param = getJobSchedulerVariable(JobSchedulerJob.SCHEDULER_PARAM_USE_NOTIFICATION);
        if (param != null) {
            try {
                result = Boolean.parseBoolean(param);
            } catch (Exception e) {
            }
        }
        return result;
    }
}