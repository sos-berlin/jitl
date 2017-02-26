package com.sos.jitl.classes.event;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.UncheckedTimeoutException;
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
	/* all intervals in seconds */
	private int waitIntervalOnError = 30;

	public JobSchedulerPluginEventHandler() {
	}

	@Override
	public void onPrepare(SchedulerXmlCommandExecutor sxce, EventHandlerSettings st) {
		xmlCommandExecutor = sxce;
		settings = st;
		setBaseUrl(this.settings.getHttpPort());
	}

	@Override
	public void onActivate(PluginMailer pm) {
		closed = false;
		mailer = pm;
	}

	@Override
	public void close() {
		closeRestApiClient();
		destroy();
		closed = true;
	}

	public void start() {
		start(null, null);
	}

	public void start(EventType[] et) {
		start(et, null);
	}

	public void start(EventType[] et, EventOverview ov) {
		String method = getMethodName("start");

		if (closed) {
			LOGGER.info(String.format("%s: processing stopped.", method));
			return;
		}

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
			} catch (Exception ex) {
				if (closed) {
					LOGGER.info(String.format("%s: processing stopped.", method));
				} else {
					LOGGER.error(String.format("%s: exception: %s", method, ex.toString()), ex);
					closeRestApiClient();
					wait(waitIntervalOnError);
				}
			}
		}
		LOGGER.debug(String.format("%s: end", method));
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
			if (ex instanceof UncheckedTimeoutException) {
				LOGGER.debug(
						String.format("%s: close httpClient due method execution timeout (%sms). see details above ...",
								method, getMethodExecutionTimeout()));
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

		JsonObject result = getEvents(eventId, this.eventTypesJoined);
		Long newEventId = getEventId(result);
		String type = getEventType(result);
		JsonArray events = getEventSnapshots(result);

		LOGGER.debug(String.format("%s: newEventId=%s, type=%s", method, newEventId, type));

		if (type.equalsIgnoreCase(EventSeq.NonEmpty.name())) {
			onNonEmptyEvent(newEventId, events);
		} else if (type.equalsIgnoreCase(EventSeq.Empty.name())) {
			onEmptyEvent(newEventId);
		} else if (type.equalsIgnoreCase(EventSeq.Torn.name())) {
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

	private void destroy() {
		xmlCommandExecutor = null;
		settings = null;
		mailer = null;
		eventOverview = null;
		eventTypes = null;
		eventTypesJoined = null;
	}

	public void wait(int interval) {
		if (!closed && interval > 0) {
			String method = getMethodName("wait");
			LOGGER.debug(String.format("%s: waiting %s ms ...", method, interval));
			try {
				Thread.sleep(interval * 1000);
			} catch (InterruptedException e) {
				if(closed){
					LOGGER.debug(String.format("%s: sleep interrupted due plugin close", method));
				}
				else{
					LOGGER.warn(String.format("%s: %s", method, e.toString()), e);
				}
			}
		}
	}

	public SchedulerXmlCommandExecutor getXmlCommandExecutor() {
		return xmlCommandExecutor;
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