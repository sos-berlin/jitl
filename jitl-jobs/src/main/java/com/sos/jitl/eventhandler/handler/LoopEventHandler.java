package com.sos.jitl.eventhandler.handler;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.eventhandler.EventMeta.EventOverview;
import com.sos.jitl.eventhandler.EventMeta.EventPath;
import com.sos.jitl.eventhandler.EventMeta.EventSeq;
import com.sos.jitl.eventhandler.EventMeta.EventType;
import com.sos.jitl.eventhandler.plugin.notifier.Mailer;
import com.sos.jitl.eventhandler.plugin.notifier.Notifier;
import com.sos.scheduler.engine.data.events.custom.VariablesCustomEvent;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class LoopEventHandler extends EventHandler implements ILoopEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoopEventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private final SchedulerXmlCommandExecutor xmlCommandExecutor;
    private final EventPublisher publisher;
    private EventHandlerSettings settings;
    private Mailer mailer;
    private Notifier notifier;

    private boolean closed = false;
    private EventOverview eventOverview;
    private EventType[] eventTypes;
    private String eventTypesJoined;
    private Map<String, Map<String, String>> customEvents;
    private Long tornEventId = null;

    private String bodyParamPathForEventId = "/not_exists/";
    /* all intervals in seconds */
    private int waitIntervalOnError = 30;

    public LoopEventHandler() {
        this(null, null);
    }

    public LoopEventHandler(SchedulerXmlCommandExecutor commandExecutor, EventPublisher eventPublisher) {
        super();
        xmlCommandExecutor = commandExecutor;
        publisher = eventPublisher;
        customEvents = new HashMap<String, Map<String, String>>();
    }

    /** called from a separate thread */
    @Override
    public void onPrepare(EventHandlerSettings st) {
        setSettings(st);
    }

    /** called from a separate thread */
    @Override
    public void onActivate(Mailer pm) {
        closed = false;
        mailer = pm;
        notifier = new Notifier(mailer, this.getClass().getSimpleName());
    }

    /** called from the JobScheduler thread */
    @Override
    public void close() {
        closed = true;
        getHttpClient().close();
    }

    public void start() {
        start(null, null);
    }

    public void start(EventType[] types) {
        start(types, null);
    }

    public void start(EventType[] types, EventOverview overview) {
        String method = getMethodName("start");

        if (overview == null && (types == null || types.length == 0)) {
            overview = EventOverview.FileBasedOverview;
        } else if (overview == null && types != null && types.length > 0) {
            overview = getEventOverviewByEventTypes(types);
        }
        eventOverview = overview;
        eventTypes = types;
        eventTypesJoined = joinEventTypes(eventTypes);

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[eventOverview=%s][eventTypes=%s]", method, eventOverview, eventTypesJoined));
        }

        EventPath path = getEventPathByEventOverview(eventOverview);
        Long eventId = null;
        try {
            eventId = getEventId(path, eventOverview, bodyParamPathForEventId);
        } catch (Exception e) {
            eventId = rerunGetEventId(method, e, path, eventOverview, bodyParamPathForEventId);
        }

        onProcessingStart(eventId);
        eventId = doProcessing(eventId);
        onProcessingEnd(eventId);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[end]%s", method, eventId));
        }
    }

    public void onProcessingStart(Long eventId) {
    }

    private Long doProcessing(Long eventId) {
        String method = getMethodName("doProcessing");
        while (!closed) {
            try {
                eventId = process(eventId);
            } catch (Throwable ex) {
                if (closed) {
                    LOGGER.info(String.format("%s[%s][processing stopped][exception ignored]%s", method, eventId, ex.toString()));
                } else {
                    LOGGER.error(String.format("%s[%s][exception]%s", method, eventId, ex.toString()), ex);
                    getHttpClient().close();
                    if (tornEventId != null) {
                        eventId = tornEventId;
                    }
                    wait(waitIntervalOnError);
                }
            }
        }
        return eventId;
    }

    public void onProcessingEnd(Long eventId) {
    }

    public void onEmptyEvent(Long eventId) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", getMethodName("onEmptyEvent"), eventId));
        }
    }

    public void onNonEmptyEvent(Long eventId, JsonArray events) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", getMethodName("onNonEmptyEvent"), eventId));
        }
    }

    public void onTornEvent(Long eventId, JsonArray events) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", getMethodName("onTornEvent"), eventId));
        }
    }

    public void onRestart(Long eventId, JsonArray events) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", getMethodName("onRestart"), eventId));
        }
    }

    private Long getEventId(EventPath path, EventOverview overview, String bodyParamPath) throws Exception {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[eventPath=%s][eventOverview=%s][bodyParamPath=%s]", getMethodName("getEventId"), path, overview,
                    bodyParamPath));
        }

        customEvents.clear();
        getHttpClient().tryCreate();

        return getEventId(getOverview(path, overview, bodyParamPath));
    }

    private Long rerunGetEventId(String callerMethod, Exception ex, EventPath path, EventOverview overview, String bodyParamPath) {
        String method = getMethodName("rerunGetEventId");

        if (closed) {
            LOGGER.info(String.format("%s[processing stopped]", method));
            return null;
        }
        if (ex != null) {
            LOGGER.error(String.format("%s[error on %s]%s", method, callerMethod, ex.toString()), ex);
            getHttpClient().close();
        }
        LOGGER.debug(method);

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
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", method, eventId));
        }
        customEvents.clear();
        getHttpClient().tryCreate();

        JsonObject result = getEvents(eventId, eventTypesJoined);
        Long newEventId = getEventId(result);
        if (closed) {
            LOGGER.info(String.format("%s[processing stopped][eventId=%s][newEventId=%s]", method, eventId, newEventId));
            return eventId;
        }

        String type = getEventType(result);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[new][%s][%s]", method, newEventId, type));
        }

        JsonArray events = getEventSnapshots(result);
        if (type.equalsIgnoreCase(EventSeq.NonEmpty.name())) {
            tornEventId = null;
            onNonEmptyEvent(newEventId, events);
        } else if (type.equalsIgnoreCase(EventSeq.Empty.name())) {
            tornEventId = null;
            onEmptyEvent(newEventId);
        } else if (type.equalsIgnoreCase(EventSeq.Torn.name())) {
            tornEventId = newEventId;
            onTornEvent(newEventId, events);
            throw new Exception(String.format("%s[Torn event occured]Try to retry events ...", method));
        } else {
            throw new Exception(String.format("%s[unknown event type]%s", method, type));
        }
        return newEventId;
    }

    public void wait(int interval) {
        if (!closed && interval > 0) {
            String method = getMethodName("wait");
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[waiting]%ss ...", method, interval));
            }
            try {
                Thread.sleep(interval * 1000);
            } catch (InterruptedException e) {
                if (closed) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s sleep interrupted due plugin close", method));
                    }
                } else {
                    LOGGER.warn(String.format("%s%s", method, e.toString()), e);
                }
            }
        }
    }

    public void addCustomEventValue(String eventKey, String valueKey, String value) {
        if (isDebugEnabled) {
            String method = getMethodName("addCustomEventValue");
            LOGGER.debug(String.format("%s[eventKey=%s][valueKey=%s][value=%s]", method, eventKey, valueKey, value));
        }
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
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[eventKey=%s][values=%s]", method, eventKey, values));
                if (closed) {
                    LOGGER.debug(String.format("%s[skip]processing stopped", method));
                }
            }
            if (publisher != null && !closed) {
                publisher.publishCustomEvent(VariablesCustomEvent.keyed(eventKey, values));
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("%s%s", method, e.toString()), e);
        }
    }

    public void publishCustomEvents() {
        String method = getMethodName("publishCustomEvents");
        try {
            if (customEvents == null) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%scustomEvents is null", method));
                }
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%scustomEvents=%s", method, customEvents));
                    if (closed) {
                        LOGGER.debug(String.format("%s[skip]processing stopped", method));
                    }
                }
                if (publisher != null && !closed) {
                    for (String eventKey : customEvents.keySet()) {
                        publisher.publishCustomEvent(VariablesCustomEvent.keyed(eventKey, customEvents.get(eventKey)));
                    }
                }
                customEvents.clear();
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("%s%s", method, e.toString()), e);
        }
    }

    public Map<String, Map<String, String>> getCustomEvents() {
        return customEvents;
    }

    public SchedulerXmlCommandExecutor getXmlCommandExecutor() {
        return xmlCommandExecutor;
    }

    public EventPublisher getEventPublisher() {
        return publisher;
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

    public Mailer getMailer() {
        return mailer;
    }

    public boolean isClosed() {
        return closed;
    }

    public Notifier getNotifier() {
        return notifier;
    }
}