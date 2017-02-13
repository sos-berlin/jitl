package com.sos.jitl.classes.event;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class JobSchedulerPluginEventHandler extends JobSchedulerEventHandler
		implements IJobSchedulerPluginEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerPluginEventHandler.class);

	private SchedulerXmlCommandExecutor xmlCommandExecutor;
	private EventHandlerSettings settings;
	private PluginMailer mailer;

	private boolean closed = false;
	private EventOverview eventOverview;
	private EventType[] eventTypes;
	private String eventTypesJoined;

	private String bodyParamPathForEventId = "/not_exists/";
	private int waitIntervalOnError = 5;

	public JobSchedulerPluginEventHandler() {
	}

	@Override
	public void onPrepare(SchedulerXmlCommandExecutor sxce, EventHandlerSettings st, PluginMailer pm) {
		this.xmlCommandExecutor = sxce;
		this.settings = st;
		this.mailer = pm;
		this.mailer.init(getIdentifier(), settings.getSchedulerId(), settings.getHost(), settings.getTcpPort());
		setBaseUrl(this.settings.getHttpPort());
	}

	@Override
	public void onActivate() {
		this.closed = false;
	}

	@Override
	public void close() {
		closeRestApiClient();
		this.closed = true;
	}

	public void start() {
		start(null, null);
	}

	public void start(EventType[] et) {
		start(null, et);
	}

	public void start(EventOverview ov, EventType[] et) {
		String method = getMethodName("start");

		if (closed) {
			LOGGER.info(String.format("%s: processing stopped.", method));
			return;
		}
		tryClientConnect();

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
		try {
			process(eventId);
		} catch (Exception e) {
			rerunProcess(method, e, eventId);
		}
	}

	public void onEmptyEvent(Long eventId) {
		String method = getMethodName("onEmptyEvent");

		LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
		try {
			process(eventId);
		} catch (Exception e) {
			rerunProcess(method, e, eventId);
		}
	}

	public void onNonEmptyEvent(Long eventId, JsonArray events) {
		String method = getMethodName("onNonEmptyEvent");

		LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
		try {
			process(eventId);
		} catch (Exception e) {
			rerunProcess(method, e, eventId);
		}
	}

	public void onTornEvent(Long eventId, JsonArray events) {
		String method = getMethodName("onTornEvent");

		if (closed) {
			LOGGER.info(String.format("%s: processing stopped.", method));
		} else {
			LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
			onRestart(eventId, events);
			start(eventOverview, eventTypes);
		}
	}

	public void onRestart(Long eventId, JsonArray events) {
		String method = getMethodName("onRestart");
		LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
	}

	private Long getEventId(EventPath path, EventOverview overview, String bodyParamPath) throws Exception {
		String method = getMethodName("getEventId");

		LOGGER.debug(String.format("%s: eventPath=%s, eventOverview=%s, bodyParamPath=%s", method, path, overview,
				bodyParamPath));
		JsonObject result = getOverview(path, overview, bodyParamPath);

		return getEventId(result);
	}

	private Long rerunGetEventId(String callerMethod, Exception ex, EventPath path, EventOverview overview,
			String bodyParamPath) {
		String method = getMethodName("rerunGetEventId");

		if (closed) {
			LOGGER.info(String.format("%s: processing stopped.", method));
			return null;
		}
		if (ex != null) {
			LOGGER.error(String.format("%s: error on %s: %s", method, callerMethod, ex.toString()), ex);
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

		if (closed) {
			return null;
		}
		tryClientConnect();

		LOGGER.debug(String.format("%s: eventId=%s", method, eventId));

		JsonObject result = getEvents(eventId, this.eventTypesJoined);
		JsonArray events = getEventSnapshots(result);
		String type = getEventType(result);
		eventId = getEventId(result);

		if (type.equalsIgnoreCase(EventSeq.NonEmpty.name())) {
			onNonEmptyEvent(eventId, events);
		} else if (type.equalsIgnoreCase(EventSeq.Empty.name())) {
			onEmptyEvent(eventId);
		} else if (type.equalsIgnoreCase(EventSeq.Torn.name())) {
			onTornEvent(eventId, events);
		}
		return eventId;
	}

	private void rerunProcess(String callerMethod, Exception ex, Long eventId) {
		String method = getMethodName("rerunProcess");

		if (closed) {
			LOGGER.info(String.format("%s: processing stopped.", method));
			return;
		}
		if (ex != null) {
			LOGGER.error(String.format("%s: error on %s: %s", method, callerMethod, ex.toString()), ex);
		}
		LOGGER.debug(String.format("%s: eventId=%s", method, eventId));

		wait(waitIntervalOnError);
		try {
			process(eventId);
		} catch (Exception e) {
			rerunProcess(method, e, eventId);
		}
	}

	private void tryClientConnect() {
		if (getRestApiClient() == null) {
			createRestApiClient();
		}
	}

	public void wait(int interval) {
		if (interval > 0) {
			String method = getMethodName("wait");
			LOGGER.debug(String.format("%s: waiting %s seconds ...", method, interval));
			try {
				Thread.sleep(interval * 1_000);
			} catch (InterruptedException e) {
				LOGGER.warn(String.format("%s: %s", method, e.toString()), e);
			}
		}
	}

	public SchedulerXmlCommandExecutor getXmlCommandExecutor() {
		return this.xmlCommandExecutor;
	}

	public EventHandlerSettings getSettings() {
		return this.settings;
	}

	public String getBodyParamPathForEventId() {
		return this.bodyParamPathForEventId;
	}

	public void setBodyParamPathForEventId(String val) {
		this.bodyParamPathForEventId = val;
	}

	public EventOverview getEventOverview() {
		return this.eventOverview;
	}

	public int getWaitIntervalOnError() {
		return this.waitIntervalOnError;
	}

	public void setWaitIntervalOnError(int val) {
		this.waitIntervalOnError = val;
	}

	public String getEventTypesJoined() {
		return this.eventTypesJoined;
	}

	public EventType[] getEventTypes() {
		return this.eventTypes;
	}

	public PluginMailer getMailer() {
		return this.mailer;
	}
}