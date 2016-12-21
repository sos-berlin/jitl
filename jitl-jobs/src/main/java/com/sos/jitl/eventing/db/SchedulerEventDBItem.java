package com.sos.jitl.eventing.db;

/** @author Uwe Risse */
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.persistence.*;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = "SCHEDULER_EVENTS")
public class SchedulerEventDBItem extends DbItem {

    private long id;
    private String schedulerId;
    private String remoteSchedulerHost;
    private long remoteSchedulerPort;
    private String jobChain;
    private String orderId;
    private String jobName;
    private String eventClass;
    private String eventId;
    private String exitCode = "0";
    private String parameters;
    private DateTime created;
    private DateTime expires;

    public SchedulerEventDBItem() {
        super();
    }

    @Id
    @GeneratedValue(generator = "SCHEDULER_EVENT_ID_GEN", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "SCHEDULER_EVENT_ID_GEN", sequenceName = "SCHEDULER_EVENTS_ID_SEQ", allocationSize = 1)
    @Column(name = "`ID`")
    public long getId() {
        return id;
    }

    @Column(name = "`ID`")
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "`SPOOLER_ID`", nullable = false)
    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    @Column(name = "`SPOOLER_ID`", nullable = true)
    public String getSchedulerId() {
        return schedulerId;
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
    public void setRemoteSchedulerPort(Long remoteSchedulerPort) {
        this.remoteSchedulerPort = remoteSchedulerPort;
    }

    @Column(name = "`REMOTE_SCHEDULER_PORT`", nullable = true)
    public Long getRemoteSchedulerPort() {
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
    public void setExitCode(String exitCode) {
        this.exitCode = exitCode;
    }

    @Column(name = "`EXIT_CODE`", nullable = false)
    public String getExitCode() {
        return exitCode;
    }

    @Column(name = "CREATED", nullable = false)
    @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
    public void setCreated(DateTime created) {
        this.created = created;
    }

    @Column(name = "CREATED", nullable = false)
    @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
    public DateTime getCreated() {
        return created;
    }

    @Column(name = "EXPIRES", nullable = true)
    @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
    public void setExpires(DateTime expires) {
        this.expires = expires;
    }

    @Column(name = "EXPIRES", nullable = true)
    @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
    public DateTime getExpires() {
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
        attr.put("exit_code", exitCode);
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
            if (!"".equals(properties().get(trigger)) && eActive.properties().get(trigger) != null && !"expires".equalsIgnoreCase(trigger) 
                    && !"created".equalsIgnoreCase(trigger) && !(eActive.properties().get(trigger).equals(properties().get(trigger)))) {
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

}