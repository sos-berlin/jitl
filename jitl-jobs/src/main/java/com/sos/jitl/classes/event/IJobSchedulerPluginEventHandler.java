package com.sos.jitl.classes.event;

import javax.json.JsonArray;

import com.sos.jitl.classes.plugin.PluginMailer;

public interface IJobSchedulerPluginEventHandler {

    void onActivate(PluginMailer mailer);

    void onPrepare(EventHandlerSettings settings);

    void onEmptyEvent(Long eventId);

    void onNonEmptyEvent(Long eventId, JsonArray events);

    void onTornEvent(Long eventId, JsonArray events);

    void onRestart(Long eventId, JsonArray events);

    void setIdentifier(String identifier);

    void close();
}
