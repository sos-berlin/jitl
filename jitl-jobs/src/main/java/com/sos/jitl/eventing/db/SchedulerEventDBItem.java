package com.sos.jitl.eventing.db;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.reporting.db.DBLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = DBLayer.TABLE_REPORT_CUSTOM_EVENTS)
@SequenceGenerator(name = DBLayer.TABLE_REPORT_CUSTOM_EVENTS, sequenceName = DBLayer.TABLE_REPORT_CUSTOM_EVENTS_SEQUENCE, allocationSize = 1)
public class SchedulerEventDBItem extends DbItem {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerEventDBItem.class);
	private long id;
	private String schedulerId;
	private String remoteUrl;
	private String remoteSchedulerHost;
	private Integer remoteSchedulerPort;
	private String jobChain;
	private String orderId;
	private String jobName;
	private String eventClass;
	private String eventId;
	private Integer exitCode = 0;
	private String parameters;
	private Date created;
	private Date expires;

	public SchedulerEventDBItem() {
		super();
	}

	/** Primary key */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORT_CUSTOM_EVENTS)
	@Column(name = "`ID`", nullable = false)
	public Long getId() {
		return id;
	}

	/** Primary key */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORT_CUSTOM_EVENTS)
	@Column(name = "`ID`", nullable = false)
	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "`SCHEDULER_ID`", nullable = false)
	public void setSchedulerId(String schedulerId) {
		this.schedulerId = schedulerId;
	}

	@Column(name = "`SCHEDULER_ID`", nullable = true)
	public String getSchedulerId() {
		return schedulerId;
	}

	@Column(name = "`REMOTE_URL`", nullable = true)
	public String getRemoteUrl() {
		return remoteUrl;
	}

	@Column(name = "`REMOTE_URL`", nullable = true)
	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	@Column(name = "`REMOTE_SCHEDULER_HOST`", nullable = true)
	public void setRemoteSchedulerHost(String remoteSchedulerHost) {
		this.remoteSchedulerHost = remoteSchedulerHost;
	}

	@Column(name = "`REMOTE_SCHEDULER_HOST`", nullable = true)
	public String getRemoteSchedulerHost() {
		return remoteSchedulerHost;
	}

	@Column(name = "`REMOTE_SCHEDULER_PORT`", nullable = true)
	public void setRemoteSchedulerPort(Integer remoteSchedulerPort) {
		this.remoteSchedulerPort = remoteSchedulerPort;
	}

	public void setRemoteSchedulerPort(String remoteSchedulerPort) {
		try {
			this.remoteSchedulerPort = Integer.parseInt(remoteSchedulerPort);
		} catch (NumberFormatException e) {
			LOGGER.warn("NumberFormatException: could not set remoteSchedulerPort: " + exitCode);

		}
	}

	@Column(name = "`REMOTE_SCHEDULER_PORT`", nullable = true)
	public Integer getRemoteSchedulerPort() {
		return remoteSchedulerPort;
	}

	@Column(name = "`JOB_NAME`", nullable = true)
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Column(name = "`JOB_NAME`", nullable = true)
	public String getJobName() {
		return jobName;
	}

	@Transient
	public String getJobNotNull() {
		return null2Blank(jobName);
	}

	@Column(name = "`ORDER_ID`", nullable = true)
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Column(name = "`ORDER_ID`", nullable = true)
	public String getOrderId() {
		return orderId;
	}

	@Transient
	public String getOrderIdNotNull() {
		return null2Blank(orderId);
	}

	@Column(name = "`JOB_CHAIN`", nullable = true)
	public void setJobChain(String jobChain) {
		this.jobChain = jobChain;
	}

	@Column(name = "`JOB_CHAIN`", nullable = true)
	public String getJobChain() {
		return jobChain;
	}

	@Transient
	public String getJobChainNotNull() {
		return null2Blank(jobChain);
	}

	@Column(name = "`EVENT_CLASS`", nullable = true)
	public void setEventClass(String eventClass) {
		this.eventClass = eventClass;
	}

	@Column(name = "`EVENT_CLASS`", nullable = true)
	public String getEventClass() {
		return eventClass;
	}

	@Column(name = "`EVENT_ID`", nullable = true)
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	@Column(name = "`EVENT_ID`", nullable = true)
	public String getEventId() {
		return eventId;
	}

	@Column(name = "`EXIT_CODE`", nullable = false)
	public void setExitCode(Integer exitCode) {
		this.exitCode = exitCode;
	}

	public void setExitCode(String exitCode) {
		try {
			this.exitCode = Integer.parseInt(exitCode);
		} catch (NumberFormatException e) {
			LOGGER.warn("NumberFormatException: could not set exit code: " + exitCode);
		}
	}

	@Column(name = "`EXIT_CODE`", nullable = false)
	public Integer getExitCode() {
		return exitCode;
	}

	@Column(name = "`CREATED`", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public void setCreated(Date created) {
		this.created = created;
	}

	@Column(name = "`CREATED`", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreated() {
		return created;
	}

	@Column(name = "`EXPIRES`", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	public void setExpires(Date expires) {
		this.expires = expires;
	}

	@Column(name = "`EXPIRES`", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getExpires() {
		return expires;
	}

	@Column(name = "`PARAMETERS`", nullable = true)
	public String getParameters() {
		return parameters;
	}

	@Column(name = "`PARAMETERS`", nullable = true)
	public void setParameters(final String parameters) {
		this.parameters = parameters;
	}

	@Transient
	private HashMap<String, String> properties() {
		HashMap<String, String> attr = new HashMap<String, String>();
		attr.put("event_class", eventClass);
		attr.put("event_id", eventId);
		attr.put("job_name", jobName);
		attr.put("job_chain", jobChain);
		attr.put("order_id", orderId);
		attr.put("exit_code", String.valueOf(exitCode));
		attr.put("remote_scheduler_host", remoteSchedulerHost);
		attr.put("remote_scheduler_port", String.valueOf(remoteSchedulerPort));
		attr.put("scheduler_id", schedulerId);
		return attr;
	}

	@Transient
	public boolean isEqual(SchedulerEventDBItem eActive) {
		boolean erg = true;
		Iterator<String> iProperties = properties().keySet().iterator();
		while (iProperties.hasNext()) {
			String trigger = iProperties.next().toString();
			if (!"".equals(properties().get(trigger)) && eActive.properties().get(trigger) != null
					&& !"expires".equalsIgnoreCase(trigger) && !"created".equalsIgnoreCase(trigger)
					&& !(eActive.properties().get(trigger).equals(properties().get(trigger)))) {
				erg = false;
			}
		}
		return erg;
	}

	@Transient
	public boolean isIn(List<SchedulerEventDBItem> listOfActiveEvents) {
		boolean erg = false;
		Iterator<SchedulerEventDBItem> i = listOfActiveEvents.iterator();
		while (i.hasNext() && !erg) {
			if (this.isEqual((SchedulerEventDBItem) i.next())) {
				erg = true;
			}
		}
		return erg;
	}

	@Transient
	public String getEventName() {
		if ("".equals(this.eventClass)) {
			return this.eventId;
		} else {
			return this.eventClass + "." + this.eventId;
		}
	}

	@Transient
	public String getRemoteSchedulerPortAsString() {
		if (remoteSchedulerPort != null) {
			return String.valueOf(remoteSchedulerPort);
		} else {
			return null;
		}
	}

	@Transient
	public String getExpiresAsString() {
		if (this.getExpires() == null) {
			return null;
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return formatter.format(this.getExpires());
		}
	}

	@Transient
	public String getCreatedAsString() {
		if (this.getCreated() == null) {
			return "";
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return formatter.format(this.getCreated());
		}
	}

	@Override
	public int hashCode() {
		// always build on unique constraint
		return new HashCodeBuilder().append(eventId).append(eventClass).append(exitCode).toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		// always compare on unique constraint
		if (other == this) {
			return true;
		}
		if (!(other instanceof SchedulerEventDBItem)) {
			return false;
		}
		SchedulerEventDBItem rhs = ((SchedulerEventDBItem) other);
		return new EqualsBuilder().append(eventId, rhs.eventId).append(eventClass, rhs.eventClass)
				.append(exitCode, rhs.exitCode).isEquals();
	}

	@Transient
	public String getExitCodeAsString() {
		return String.valueOf(exitCode);
	}
}