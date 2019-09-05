package com.sos.jitl.eventhandler.handler;

import javax.json.JsonArray;

import com.sos.jitl.eventhandler.plugin.notifier.Mailer;

public interface ILoopEventHandler {

    void onActivate(Mailer mailer);

    void onPrepare(EventHandlerSettings settings);

    Long onGetStartEventId() throws Exception;

    void onProcessingStart(Long eventId);

    void onProcessingEnd(Long eventId);

    void onEmptyEvent(Long eventId);

    void onNonEmptyEvent(Long eventId, JsonArray events);

    void onTornEvent(Long eventId, JsonArray events);

    void onRestart(Long eventId, JsonArray events);

    void setIdentifier(String identifier);

    String getIdentifier();

    void close();

    EventHandlerSettings getSettings();

    void setSettings(EventHandlerSettings settings);
}
