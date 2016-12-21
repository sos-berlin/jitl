package com.sos.jitl.eventing.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateIntervalFilter;
import com.sos.hibernate.interfaces.ISOSHibernateFilter;

/** @author Uwe Risse */
public class SchedulerEventFilter extends SOSHibernateIntervalFilter implements ISOSHibernateFilter {

    private List<SchedulerEventDBItem> eventList = new ArrayList<SchedulerEventDBItem>();
    private String remoteSchedulerHost;
    private String conditon;
    private Long remoteSchedulerPort;
    private String jobChain;
    private String orderId;
    private String jobName;
    private String eventClass;
    private String eventId;
    private String exitCode;
    private Date expires;
    private String expiresIso;
    private String schedulerId = "";

    public SchedulerEventFilter() {
        super();
    }

    public boolean isFiltered(DbItem dbitem) {
        SchedulerEventDBItem h = (SchedulerEventDBItem) dbitem;
        return false;
    }

    public List<SchedulerEventDBItem> getEventList() {
        return eventList;
    }

    public void setEventList(List<SchedulerEventDBItem> eventList) {
        this.eventList.clear();
        for (SchedulerEventDBItem e : eventList) {
            if (!"".equals(e.getEventName())) {
                this.eventList.add(e);
            }
        }
    }

    public void addEventId(String eventClass, String eventId) {
        SchedulerEventDBItem s = new SchedulerEventDBItem();
        s.setEventId(eventId);
        s.setEventClass(eventClass);
        this.eventList.add(s);
    }

    public boolean hasEvents() {
        return !eventList.isEmpty();
    }

    @Override
    public String getTitle() {
        String s = "";
        if (remoteSchedulerHost != null && !"".equals(remoteSchedulerHost)) {
            s += String.format("RemoteScheduler: %s:%s ", remoteSchedulerHost, remoteSchedulerPort);
        }
        if (schedulerId != null && !"".equals(schedulerId)) {
            s += String.format("Scheduler Id: %s ", schedulerId);
        }
        if (jobChain != null && !"".equals(jobChain)) {
            s += String.format("JobChain: %s ", jobChain);
        }
        if (jobName != null && !"".equals(jobName)) {
            s += String.format("JobName: %s ", jobName);
        }
        if (eventClass != null && !"".equals(eventClass)) {
            s += String.format("Class: %s ", eventClass);
        }
        if (eventId != null && !"".equals(eventId)) {
            s += String.format("Id: %s ", eventId);
        }
        if (exitCode != null && !"".equals(exitCode)) {
            s += String.format("Exit: %s ", exitCode);
        }
        return String.format("%1s", s);
    }

    @Override
    public void setIntervalFromDate(Date d) {
        this.expires = d;
    }

    @Override
    public void setIntervalToDate(Date d) {
        this.expires = d;
    }

    @Override
    public void setIntervalFromDateIso(String s) {
        this.expiresIso = s;
    }

    @Override
    public void setIntervalToDateIso(String s) {
        this.expiresIso = s;
    }

    public String getRemoteSchedulerHost() {
        return remoteSchedulerHost;
    }

    public void setRemoteSchedulerHost(String remoteSchedulerHost) {
        this.remoteSchedulerHost = remoteSchedulerHost;
    }

    public Long getRemoteSchedulerPort() {
        return remoteSchedulerPort;
    }

    public void setRemoteSchedulerPort(Long remoteSchedulerPort) {
        this.remoteSchedulerPort = remoteSchedulerPort;
    }

    public void setRemoteSchedulerPort(String remoteSchedulerPort) {
        try {
            this.remoteSchedulerPort = Long.parseLong(remoteSchedulerPort);
        } catch (NumberFormatException e) {
            this.remoteSchedulerPort = null;
        }
    }

    public String getJobChain() {
        return jobChain;
    }

    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getEventClass() {
        return eventClass;
    }

    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getExitCode() {
        return exitCode;
    }

    public void setExitCode(String exitCode) {
        this.exitCode = exitCode;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public String getConditon() {
        return conditon;
    }

    public void setConditon(String conditon) {
        this.conditon = conditon;
    }

}