package com.sos.jitl.eventhandler.handler;

import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.eventhandler.EventMeta.EventPath;
import com.sos.jitl.eventhandler.EventMeta.EventType;

public class EventHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandlerTest.class);

    public static void main(String[] args) {
        EventHandler ev = new EventHandler();
        ev.setBaseUrl("localhost", "40444");

        try {
            ev.getHttpClient().create();

            JsonObject jo = ev.getOverview(EventPath.fileBased, "/not_exists/");
            Long eventId = ev.getEventId(jo);
            LOGGER.info("eventId: " + eventId);

            jo = ev.getEvents(eventId, new EventType[] { EventType.TaskStarted, EventType.TaskEnded });
            eventId = ev.getEventId(jo);
            LOGGER.info("eventId: " + eventId);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            ev.getHttpClient().close();
        }
    }

}
