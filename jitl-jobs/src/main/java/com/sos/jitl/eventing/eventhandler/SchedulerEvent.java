package com.sos.jitl.eventing.eventhandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import com.sos.jitl.eventing.db.SchedulerEventDBItem;

public class SchedulerEvent {

	private String eventName;
	private String eventTitle;
	private String eventClass;
	private String eventId;
	private String jobName;
	private String jobChain;
	private String orderId;
	private String exitCode;
	private String created;
	private String expires;
	private String remoteSchedulerHost;
	private String remoteSchedulerPort;
	private String schedulerId;
	private String condition = "";
	private String comment = "";

	public SchedulerEvent(SchedulerEventDBItem schedulerEventDBItem) {
		this.created = schedulerEventDBItem.getCreatedAsString();
		this.eventClass = schedulerEventDBItem.getEventClass();
		this.eventId = schedulerEventDBItem.getEventId();
		this.eventClass = schedulerEventDBItem.getEventClass();
		this.exitCode = schedulerEventDBItem.getExitCodeAsString();
		this.expires = schedulerEventDBItem.getExpiresAsString();
		this.jobChain = schedulerEventDBItem.getJobChain();
		this.jobName = schedulerEventDBItem.getJobName();
		this.orderId = schedulerEventDBItem.getOrderId();
		this.remoteSchedulerHost = schedulerEventDBItem.getRemoteSchedulerHost();
		this.remoteSchedulerPort = schedulerEventDBItem.getRemoteSchedulerPortAsString();
		this.schedulerId = schedulerEventDBItem.getSchedulerId();
	}

	public SchedulerEvent() {
	}

	public String getEventTitle() {
		return eventTitle;
	}

	public String getEventClass() {
		return eventClass;
	}

	public String getEventId() {
		return eventId;
	}

	public String getJobName() {
		return jobName;
	}

	public String getJobChain() {
		return jobChain;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getExitCode() {
		return exitCode;
	}

	public String getCreated() {
		return created;
	}

	public String getExpires() {
		return expires;
	}

	public String getRemoteSchedulerHost() {
		return remoteSchedulerHost;
	}

	public String getRemoteSchedulerPort() {
		return remoteSchedulerPort;
	}

	public String getSchedulerId() {
		return schedulerId;
	}

	private String getText(final Node n) {
		if (n != null) {
			return n.getNodeValue();
		} else {
			return "";
		}
	}

	private String getTextDefault(final String d, final Node n) {
		if (n != null) {
			return n.getNodeValue();
		} else {
			return d;
		}
	}

	public void setProperties(NamedNodeMap attr) {
		eventClass = getTextDefault("", attr.getNamedItem("event_class"));
		eventId = getText(attr.getNamedItem("event_id"));
		jobName = getText(attr.getNamedItem("job_name"));
		jobChain = getText(attr.getNamedItem("job_chain"));
		orderId = getText(attr.getNamedItem("order_id"));
		exitCode = getText(attr.getNamedItem("exit_code"));
		created = getText(attr.getNamedItem("created"));
		expires = getText(attr.getNamedItem("expires"));
		comment = getText(attr.getNamedItem("comment"));
		schedulerId = getText(attr.getNamedItem("scheduler_id"));
		remoteSchedulerHost = getText(attr.getNamedItem("remote_scheduler_host"));
		remoteSchedulerPort = getText(attr.getNamedItem("remote_scheduler_port"));
	}

	public void setEventClassIfBlank(String eClass) {
		if ("".equals(eventClass)) {
			eventClass = eClass;
		}
	}

	public String getCondition() {
		return condition;
	}

	private HashMap<String, String> properties() {
		HashMap<String, String> attr = new HashMap<String, String>();
		attr.put("event_title", eventTitle);
		attr.put("event_class", eventClass);
		attr.put("event_id", eventId);
		attr.put("job_name", jobName);
		attr.put("job_chain", jobChain);
		attr.put("order_id", orderId);
		attr.put("exit_code", exitCode);
		attr.put("remote_scheduler_host", remoteSchedulerHost);
		attr.put("remote_scheduler_port", remoteSchedulerPort);
		attr.put("scheduler_id", schedulerId);
		return attr;
	}

	public boolean isEqual(SchedulerEvent eActive) {
		boolean erg = true;
		Iterator<String> iProperties = properties().keySet().iterator();
		while (iProperties.hasNext()) {
			String trigger = iProperties.next().toString();
			if (!"".equals(properties().get(trigger)) && eActive.properties().get(trigger) != null
					&& !"expires".equalsIgnoreCase(trigger) && !"created".equalsIgnoreCase(trigger)
					&& !eActive.properties().get(trigger).equals(properties().get(trigger))) {
				erg = false;
			}
		}
		return erg;
	}

	public boolean isIn(List<SchedulerEvent> listOfActiveEvents) {
		boolean erg = false;
		Iterator<SchedulerEvent> i = listOfActiveEvents.iterator();
		while (i.hasNext() && !erg) {
			if (this.isEqual(i.next())) {
				erg = true;
			}
		}
		return erg;
	}

	public void setEvent_name(String eventName) {
		this.eventName = eventName;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getComment() {
		return comment;
	}

	public String getEventName() {
		if ("".equals(this.eventName)) {
			if ("".equals(this.eventClass)) {
				return this.eventId;
			} else {
				return this.eventClass + "." + this.eventId;
			}
		} else {
			return eventName;
		}
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public void setExpires(String expires) {
		this.expires = expires;
	}
}