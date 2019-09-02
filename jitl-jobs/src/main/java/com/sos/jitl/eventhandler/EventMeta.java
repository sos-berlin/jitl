package com.sos.jitl.eventhandler;

public final class EventMeta {

    public static final String MASTER_API_PATH = "/jobscheduler/master/api/";

    public static enum EventType {
        FileBasedEvent, FileBasedAdded, FileBasedRemoved, FileBasedReplaced, FileBasedActivated, TaskEvent, TaskStarted, TaskEnded, TaskClosed, OrderEvent, OrderStarted, OrderFinished, OrderRemoved, OrderStepStarted, OrderStepEnded, OrderSetBack, OrderNodeChanged, OrderSuspended, OrderResumed, OrderWaitingInTask, JobChainEvent, JobChainStateChanged, JobChainNodeActionChanged, SchedulerClosed, SchedulerEvent, VariablesCustomEvent
    };

    public static enum EventSeq {
        NonEmpty, Empty, Torn
    };

    public static enum EventPath {
        event, fileBased, task, order, jobChain
    };

    public static enum EventKey {
        TYPE, key, eventId, eventSnapshots, jobPath
    };

    public static enum EventOverview {
        FileBasedOverview, FileBasedDetailed, TaskOverview, OrderOverview, JobChainOverview
    };
}
