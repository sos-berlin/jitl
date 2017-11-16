package com.sos.plugins.plugin;

import com.sos.jitl.classes.plugin.JobSchedulerEventPlugin;
import com.sos.plugins.event.CustomEventHandler;
import com.sos.scheduler.engine.eventbus.EventBus;
import com.sos.scheduler.engine.kernel.Scheduler;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;


public class KeyedEventProcessorPlugin extends JobSchedulerEventPlugin {
    
    private CustomEventHandler customEventHandler;

    public KeyedEventProcessorPlugin(Scheduler scheduler, SchedulerXmlCommandExecutor executor, VariableSet variables, EventBus eventBus) {
        super(scheduler, executor, variables);
        this.customEventHandler = new CustomEventHandler(executor, eventBus);
    }

    @Override
    public void onPrepare() {
        super.executeOnPrepare(customEventHandler);
    }
    
    @Override
    public void onActivate() {
        super.executeOnActivate(customEventHandler);
    }
    
    @Override
    public void close() {
        super.executeClose(customEventHandler);
    }
}