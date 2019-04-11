package com.sos.jitl.classes.event;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.sos.jitl.classes.event.JobSchedulerEvent.EventOverview;
import com.sos.jitl.classes.event.JobSchedulerEvent.EventPath;
import com.sos.jitl.classes.event.JobSchedulerEvent.EventSeq;
import com.sos.jitl.classes.event.JobSchedulerEvent.EventType;
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.scheduler.engine.data.events.custom.VariablesCustomEvent;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class JobSchedulerPluginEventHandler extends JobSchedulerEventHandler implements IJobSchedulerPluginEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerPluginEventHandler.class);

    private final SchedulerXmlCommandExecutor xmlCommandExecutor;
    private final EventPublisher eventBus;
    private EventHandlerSettings settings;
    private PluginMailer mailer;

    private boolean closed = false;
    private boolean ended = false;
    private EventOverview eventOverview;
    private EventType[] eventTypes;
    private String eventTypesJoined;
    private Map<String, Map<String, String>> customEvents;
    private Long tornEventId = null;

    private String bodyParamPathForEventId = "/not_exists/";
    /* all intervals in seconds */
    private int waitIntervalOnError = 30;
    private int waitIntervalOnEnd = 30;

    public JobSchedulerPluginEventHandler(SchedulerXmlCommandExecutor sxce, EventPublisher eb) {
        xmlCommandExecutor = sxce;
        eventBus = eb;
        customEvents = new HashMap<String, Map<String, String>>();
    }

    public JobSchedulerPluginEventHandler() {
        xmlCommandExecutor = null;
        eventBus = null;
        customEvents = new HashMap<String, Map<String, String>>();
    }

    /** called from a separate thread */
    @Override
    public void onPrepare(EventHandlerSettings st) {
        setSettings(st);
    }

    /** called from a separate thread */
    @Override
    public void onActivate(PluginMailer pm) {
        closed = false;
        ended = false;
        mailer = pm;
    }

    /** called from the JobScheduler thread */
    @Override
    public void close() {
        closed = true;
    }

    /** called from the JobScheduler thread */
    @Override
    public void awaitEnd() {
        int counter = 0;
        int limit = waitIntervalOnEnd * 2;
        while (!ended) {
            if (counter > limit) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (Throwable e) {
                break;
            }
            counter++;
        }
    }

    public void start() {
        start(null, null);
    }

    public void start(EventType[] et) {
        start(et, null);
    }

    public void start(EventType[] et, EventOverview ov) {
        String method = getMethodName("start");

        if (ov == null && (et == null || et.length == 0)) {
            ov = EventOverview.FileBasedOverview;
        } else if (ov == null && et != null && et.length > 0) {
            ov = getEventOverviewByEventTypes(et);
        }
        this.eventOverview = ov;
        this.eventTypes = et;
        this.eventTypesJoined = joinEventTypes(this.eventTypes);

        LOGGER.debug(String.format("%s: eventOverview=%s, eventTypes=%s", method, eventOverview, eventTypesJoined));

        EventPath path = getEventPathByEventOverview(this.eventOverview);
        Long eventId = null;
        try {
            eventId = getEventId(path, this.eventOverview, this.bodyParamPathForEventId);
        } catch (Exception e) {
            eventId = rerunGetEventId(method, e, path, this.eventOverview, this.bodyParamPathForEventId);
        }

        while (!closed) {
            try {
                eventId = process(eventId);
            } catch (Throwable ex) {
                if (closed) {
                    LOGGER.info(String.format("%s: processing stopped.", method));
                } else {
                    LOGGER.error(String.format("%s: exception: %s", method, ex.toString()), ex);
                    closeRestApiClient();
                    if (tornEventId != null) {
                        eventId = tornEventId;
                    }
                    wait(waitIntervalOnError);
                }
            }
        }
        onEnded();
        ended = true;
        LOGGER.debug(String.format("%s: end", method));
    }

    public void onEnded() {
    }

    public void onEmptyEvent(Long eventId) {
        String method = getMethodName("onEmptyEvent");
        LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
    }

    public void onNonEmptyEvent(Long eventId, JsonArray events) {
        String method = getMethodName("onNonEmptyEvent");
        LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
    }

    public void onTornEvent(Long eventId, JsonArray events) {
        String method = getMethodName("onTornEvent");
        LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
    }

    public void onRestart(Long eventId, JsonArray events) {
        String method = getMethodName("onRestart");
        LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
    }

    private Long getEventId(EventPath path, EventOverview overview, String bodyParamPath) throws Exception {
        String method = getMethodName("getEventId");

        tryCreateRestApiClient();
        customEvents.clear();

        LOGGER.debug(String.format("%s: eventPath=%s, eventOverview=%s, bodyParamPath=%s", method, path, overview, bodyParamPath));
        JsonObject result = getOverview(path, overview, bodyParamPath);

        return getEventId(result);
    }

    private Long rerunGetEventId(String callerMethod, Exception ex, EventPath path, EventOverview overview, String bodyParamPath) {
        String method = getMethodName("rerunGetEventId");

        if (closed) {
            LOGGER.info(String.format("%s: processing stopped.", method));
            return null;
        }
        if (ex != null) {
            LOGGER.error(String.format("%s: error on %s: %s", method, callerMethod, ex.toString()), ex);
            if (ex instanceof UncheckedTimeoutException) {
                LOGGER.debug(String.format("%s: close httpClient due method execution timeout (%sms). see details above ...", method,
                        getMethodExecutionTimeout()));
            } else {
                LOGGER.debug(String.format("%s: close httpClient due exeption. see details above ...", method));
            }
            closeRestApiClient();
        }
        LOGGER.debug(String.format("%s", method));

        wait(waitIntervalOnError);
        Long eventId = null;
        try {
            eventId = getEventId(path, overview, bodyParamPath);
        } catch (Exception e) {
            eventId = rerunGetEventId(method, e, path, overview, bodyParamPath);
        }
        return eventId;
    }

    private Long process(Long eventId) throws Exception {
        String method = getMethodName("process");
        LOGGER.debug(String.format("%s: eventId=%s", method, eventId));

        tryCreateRestApiClient();

        customEvents.clear();
        JsonObject result = getEvents(eventId, this.eventTypesJoined);
        Long newEventId = getEventId(result);
        String type = getEventType(result);
        JsonArray events = getEventSnapshots(result);

        LOGGER.debug(String.format("%s: newEventId=%s, type=%s", method, newEventId, type));

        if (type.equalsIgnoreCase(EventSeq.NonEmpty.name())) {
            tornEventId = null;
            onNonEmptyEvent(newEventId, events);
        } else if (type.equalsIgnoreCase(EventSeq.Empty.name())) {
            tornEventId = null;
            onEmptyEvent(newEventId);
        } else if (type.equalsIgnoreCase(EventSeq.Torn.name())) {
            tornEventId = newEventId;
            onTornEvent(newEventId, events);
            throw new Exception(String.format("%s: Torn event occured. Try to retry events ...", method));
        } else {
            throw new Exception(String.format("%s: unknown event type=%s", method, type));
        }
        return newEventId;
    }

    private void tryCreateRestApiClient() {
        if (getRestApiClient() == null) {
            createRestApiClient();
        }
    }

    public void wait(int interval) {
        if (!closed && interval > 0) {
            String method = getMethodName("wait");
            LOGGER.debug(String.format("%s: waiting %ss ...", method, interval));
            try {
                Thread.sleep(interval * 1000);
            } catch (InterruptedException e) {
                if (closed) {
                    LOGGER.debug(String.format("%s: sleep interrupted due plugin close", method));
                } else {
                    LOGGER.warn(String.format("%s: %s", method, e.toString()), e);
                }
            }
        }
    }

    public void addCustomEventValue(String eventKey, String valueKey, String value) {
        String method = getMethodName("addCustomEventValue");
        LOGGER.debug(String.format("%s: eventKey=%s, valueKey=%s, value=%s", method, eventKey, valueKey, value));
        Map<String, String> values = null;
        if (customEvents.containsKey(eventKey)) {
            values = customEvents.get(eventKey);
        } else {
            values = new HashMap<String, String>();
        }
        values.put(valueKey, value);
        customEvents.put(eventKey, values);
    }

    public void publishCustomEvent(String eventKey, String valueKey, String value) {
        Map<String, String> values = new HashMap<String, String>();
        values.put(valueKey, value);
        publishCustomEvent(eventKey, values);
    }

    public void publishCustomEvent(String eventKey, Map<String, String> values) {
        String method = getMethodName("publishCustomEvent");
        try {
            if (eventBus != null) {
                LOGGER.debug(String.format("%s: eventKey=%s, values=%s", method, eventKey, values));
                eventBus.publishCustomEvent(VariablesCustomEvent.keyed(eventKey, values));
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("%s: %s", method, e.toString()));
        }
    }

    public void publishCustomEvents() {
        String method = getMethodName("publishCustomEvents");
        try {
            if (eventBus != null && customEvents != null) {
                for (String eventKey : customEvents.keySet()) {
                    Map<String, String> values = customEvents.get(eventKey);
                    LOGGER.debug(String.format("%s: eventKey=%s, values=%s", method, eventKey, values));
                    eventBus.publishCustomEvent(VariablesCustomEvent.keyed(eventKey, values));
                }
                customEvents.clear();
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("%s: %s", method, e.toString()));
        }
    }

    public Map<String, Map<String, String>> getCustomEvents() {
        return customEvents;
    }

    public SchedulerXmlCommandExecutor getXmlCommandExecutor() {
        return xmlCommandExecutor;
    }

    public EventPublisher getEventBus() {
        return eventBus;
    }

    public void setSettings(EventHandlerSettings st) {
        settings = st;
        setBaseUrl(st.getHttpHost(), settings.getHttpPort());
    }

    public EventHandlerSettings getSettings() {
        return settings;
    }

    public String getBodyParamPathForEventId() {
        return bodyParamPathForEventId;
    }

    public void setBodyParamPathForEventId(String val) {
        bodyParamPathForEventId = val;
    }

    public EventOverview getEventOverview() {
        return eventOverview;
    }

    public int getWaitIntervalOnError() {
        return waitIntervalOnError;
    }

    public void setWaitIntervalOnError(int val) {
        waitIntervalOnError = val;
    }

    public String getEventTypesJoined() {
        return eventTypesJoined;
    }

    public EventType[] getEventTypes() {
        return eventTypes;
    }

    public PluginMailer getMailer() {
        return mailer;
    }

    public boolean isClosed() {
        return closed;
    }
}