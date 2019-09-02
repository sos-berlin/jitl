package com.sos.jitl.reporting.plugin;

import javax.inject.Inject;

import com.sos.jitl.eventhandler.plugin.LoopEventHandlerPlugin;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.Scheduler;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

import sos.scheduler.job.JobSchedulerJob;

public class FactPlugin extends LoopEventHandlerPlugin {

    private final FactEventHandler eventHandler;

    @Inject
    public FactPlugin(Scheduler scheduler, SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables, EventPublisher eventBus) {
        super(scheduler, xmlCommandExecutor, variables);
        setIdentifier("reporting");

        eventHandler = new FactEventHandler(xmlCommandExecutor, eventBus);
        eventHandler.setIdentifier(getIdentifier());
    }

    @Override
    public void onPrepare() {
        // do nothing
    }

    @Override
    public void onActivate() {
        eventHandler.setUseNotificationPlugin(getUseNotification());
        super.onActivate(eventHandler);
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