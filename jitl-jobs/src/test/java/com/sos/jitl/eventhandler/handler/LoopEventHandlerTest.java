package com.sos.jitl.eventhandler.handler;

import javax.json.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.eventhandler.EventMeta.EventType;

public class LoopEventHandlerTest extends LoopEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoopEventHandlerTest.class);

    public static void closeEventHandlerAfter(ILoopEventHandler eh, int seconds) {
        Thread thread = new Thread() {

            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("[%s][start]closeEventHandlerAfter %ss...", name, seconds));
                try {
                    Thread.sleep(seconds * 1_000);
                } catch (InterruptedException e) {
                    LOGGER.info(String.format("[%s][exception]%s", name, e.toString()), e);
                }
                eh.close();
                LOGGER.info(String.format("[%s][end]closeEventHandlerAfter %ss", name, seconds));
            }
        };
        thread.start();
    }

    @Override
    public Long onGetStartEventId() throws Exception {
        // throw new Exception("123");
        return new Long(123);
    }

    @Override
    public void onProcessingStart(Long eventId) {
        LOGGER.info("onProcessingStart: eventId=" + eventId);
    }

    @Override
    public void onProcessingEnd(Long eventId) {
        LOGGER.info("onProcessingEnd: eventId=" + eventId);
    }

    @Override
    public void onEmptyEvent(Long eventId) {
        LOGGER.info("onEmptyEvent: eventId=" + eventId);

        wait(1);
    }

    @Override
    public void onNonEmptyEvent(Long eventId, JsonArray events) {
        LOGGER.info("onNonEmptyEvent: eventId=" + eventId);

        wait(1);
    }

    public static void main(String[] args) {

        LoopEventHandlerTest test = new LoopEventHandlerTest();
        EventHandlerSettings settings = new EventHandlerSettings();
        settings.setHttpHost("localhost");
        settings.setHttpPort("40444");
        test.onPrepare(settings); // test.setSettings(settings);

        LoopEventHandlerTest.closeEventHandlerAfter(test, 40);// close after n seconds

        EventType[] eventTypes = new EventType[] { EventType.TaskStarted, EventType.TaskEnded };

        test.start(eventTypes);
    }

}
