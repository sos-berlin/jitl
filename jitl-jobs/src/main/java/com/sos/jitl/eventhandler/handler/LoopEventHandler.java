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
import com.sos.jitl.eventhandler.plugin.notifier.Notifier;
import com.sos.scheduler.engine.data.events.custom.VariablesCustomEvent;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public abstract class LoopEventHandler extends EventHandler implements ILoopEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoopEventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private static final int MAX_RERUNS_ON_GET_START_EVENT_ID = 10;
    private final SchedulerXmlCommandExecutor xmlCommandExecutor;
    private final EventPublisher publisher;
    private EventHandlerSettings settings;
    private Notifier notifier;

    private boolean closed = false;
    private EventType[] eventTypes;
    private String eventTypesJoined;
    private Map<String, Map<String, String>> customEvents;
    private Long tornEventId = null;

    private String pathForStartEventId = "/not_exists/";
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

    @Override
    public void onPrepare(EventHandlerSettings settings) {
        setSettings(settings);
    }

    @Override
    public void onActivate(Notifier pluginNotifier) {
        closed = false;
        notifier = pluginNotifier;
    }

    @Override
    public void close() {
        closed = true;
        getHttpClient().close();

        synchronized (getHttpClient()) {
            getHttpClient().notifyAll();
        }
    }

    public void start(EventType[] types) {
        String method = getMethodName("start");

        if (types == null || types.length == 0) {
            LOGGER.error(String.format("%s[processing stopped]event types are NULL or empty", method));
            return;
        }

        eventTypes = types;
        eventTypesJoined = getEventTypes(eventTypes);

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[eventTypes=%s]", method, eventTypesJoined));
        }

        Long eventId = null;
        try {
            eventId = getStartEventId();
        } catch (Exception e) {
            LOGGER.error(String.format("%s[processing stopped]%s", method, e.toString()), e);
            if (notifier != null) {
                notifier.notifyOnError("start", String.format("%s processing stopped", method), e);
            }
            closed = true;
        }
        if (!closed) {
            if (eventId == null) {
                EventOverview overview = getEventOverviewByEventTypes(types);
                EventPath path = getEventPathByEventOverview(overview);
                try {
                    eventId = getStartEventIdFromOverview(path, overview, pathForStartEventId);
                } catch (Exception e) {
                    eventId = rerunGetStartEventIdFromOverview(method, e, path, overview, pathForStartEventId);
                }
            }
            if (!closed) {
                onProcessingStart(eventId);
                eventId = doProcessing(eventId);
                onProcessingEnd(eventId);
            }
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[end]%s", method, eventId));
        }
    }

    private Long getStartEventId() throws Exception {
        String method = "getStartEventId";
        int count = 0;
        boolean run = true;
        Long eventId = null;
        while (run) {
            count++;

            if (closed) {
                return null;
            }

            try {
                eventId = onGetStartEventId();
                run = false;
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s%s", getMethodName(method), eventId));
                }
            } catch (Throwable e) {
                if (count >= MAX_RERUNS_ON_GET_START_EVENT_ID) {
                    run = false;
                    throw new Exception(String.format("%s[exception %s of %s]%s", getMethodName(method), count, MAX_RERUNS_ON_GET_START_EVENT_ID, e
                            .toString()), e);
                } else {
                    LOGGER.error(String.format("%s[%s]%s", getMethodName(method), count, e.toString()), e);
                    if (notifier != null) {
                        notifier.smartNotifyOnError(this.getClass(), String.format("%s[%s]", getMethodName(method), count), e);
                    }
                    wait(waitIntervalOnError);
                }
            }
        }
        return eventId;
    }

    @Override
    public Long onGetStartEventId() throws Exception {
        return null;
    }

    @Override
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

    @Override
    public void onProcessingEnd(Long eventId) {
    }

    @Override
    public void onEmptyEvent(Long eventId) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", getMethodName("onEmptyEvent"), eventId));
        }
    }

    @Override
    public void onNonEmptyEvent(Long eventId, JsonArray events) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", getMethodName("onNonEmptyEvent"), eventId));
        }
    }

    @Override
    public void onTornEvent(Long eventId, JsonArray events) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", getMethodName("onTornEvent"), eventId));
        }
    }

    @Override
    public void onRestart(Long eventId, JsonArray events) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", getMethodName("onRestart"), eventId));
        }
    }

    private Long getStartEventIdFromOverview(EventPath path, EventOverview overview, String pathForStartEventId) throws Exception {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[eventPath=%s][eventOverview=%s][pathForStartEventId=%s]", getMethodName("getStartEventIdFromOverview"),
                    path, overview, pathForStartEventId));
        }

        customEvents.clear();
        getHttpClient().tryCreate();

        return getEventId(getOverview(path, overview, pathForStartEventId));
    }

    private Long rerunGetStartEventIdFromOverview(String callerMethod, Exception ex, EventPath path, EventOverview overview,
            String pathForStartEventId) {
        String method = getMethodName("rerunGetStartEventIdFromOverview");

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
            eventId = getStartEventIdFromOverview(path, overview, pathForStartEventId);
        } catch (Exception e) {
            eventId = rerunGetStartEventIdFromOverview(method, e, path, overview, pathForStartEventId);
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
                LOGGER.debug(String.format("%s%ss ...", method, interval));
            }
            try {
                // Thread.sleep(interval * 1_000);
                synchronized (getHttpClient()) {
                    getHttpClient().wait(interval * 1_000);
                }
            } catch (InterruptedException e) {
                if (closed) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[processing stopped]sleep interrupted due close", method));
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

    public String getPathForStartEventId() {
        return pathForStartEventId;
    }

    public void setPathForStartEventId(String val) {
        pathForStartEventId = val;
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

    public boolean isClosed() {
        return closed;
    }

    public Notifier getNotifier() {
        return notifier;
    }
}