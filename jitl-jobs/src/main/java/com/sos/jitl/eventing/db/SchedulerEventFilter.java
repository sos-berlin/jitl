package com.sos.jitl.eventing.db;

import java.util.Date;
import java.util.List;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateIntervalFilter;
import com.sos.hibernate.interfaces.ISOSHibernateFilter;
import com.sos.joc.model.job.JobPath;
import com.sos.joc.model.order.OrderPath;

/** @author Uwe Risse */
public class SchedulerEventFilter extends SOSHibernateIntervalFilter implements ISOSHibernateFilter {

    private String remoteUrl;
    private String remoteSchedulerHost;
    private String conditon;
    private Integer remoteSchedulerPort;
    private String jobChain;
    private String orderId;
    private String jobName;
    private String eventClass;
    private String eventId;
    private Integer exitCode;
    private Date expiresFrom;
    private Date expiresTo;
    private String expiresFromIso;
    private String expiresToIso;
    private String schedulerId = "";
    private boolean schedulerIdEmpty = false;
    private List<String> listOfEventClasses;
    private List<String> listOfEventIds;
    private List<Integer> listOfExitCodes;
    private List<OrderPath> listOfOrders;
    private List<JobPath> listOfJobs;
    private List<Long> listOfIds;

    public SchedulerEventFilter() {
        super();
    }
    
    public boolean hasEventClasses() {
        return listOfEventClasses != null && !listOfEventClasses.isEmpty();
    }
    
    public boolean hasIds() {
        return listOfIds != null && !listOfIds.isEmpty();
    }

    public boolean hasEventIds() {
        return listOfEventIds != null && !listOfEventIds.isEmpty();
    }

    public boolean hasExitCodes() {
        return listOfExitCodes != null && !listOfExitCodes.isEmpty();
    }

    public boolean hasOrders() {
        return listOfOrders != null && !listOfOrders.isEmpty();
    }

    public boolean hasJobs() {
        return listOfJobs != null && !listOfJobs.isEmpty();
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
        this.expiresFrom = d;
    }

    public void setExpiresFrom(Date d) {
        this.expiresFrom = d;
    }

    @Override
    public void setIntervalToDate(Date d) {
        this.expiresTo = d;
    }

    public void setExpiresTo(Date d) {
        this.expiresTo = d;
    }

    public Date getExpiresTo() {
        return this.expiresTo;
    }
    
    public Date getExpiresFrom() {
        return this.expiresFrom;
    }
 
 
    @Override
    public void setIntervalFromDateIso(String s) {
        this.expiresFromIso = s;
    }

    @Override
    public void setIntervalToDateIso(String s) {
        this.expiresToIso = s;
    }

    public void setExpires(Date d) {
        this.expiresTo = d;
        this.expiresFrom = null;
    }
    public String getRemoteSchedulerHost() {
        return remoteSchedulerHost;
    }

    public void setRemoteSchedulerHost(String remoteSchedulerHost) {
        this.remoteSchedulerHost = remoteSchedulerHost;
    }

    public Integer getRemoteSchedulerPort() {
        return remoteSchedulerPort;
    }

    public void setRemoteSchedulerPort(Integer remoteSchedulerPort) {
        this.remoteSchedulerPort = remoteSchedulerPort;
    }

    public void setRemoteSchedulerPort(String remoteSchedulerPort) {
        try {
            this.remoteSchedulerPort = Integer.parseInt(remoteSchedulerPort);
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

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
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

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public void setSchedulerIdEmpty(boolean schedulerIdEmpty) {
        this.schedulerIdEmpty = schedulerIdEmpty;
    }

    public boolean isSchedulerIdEmpty() {
        return schedulerIdEmpty;
    }

    public void setEventClasses(List<String> eventClasses) {
        listOfEventClasses =  eventClasses;
    }

    public void setExitCodes(List<Integer> exitCodes) {
        listOfExitCodes = exitCodes;
    }

    public void setEventIds(List<String> eventIds) {
        listOfEventIds = eventIds;
    }

    public void setOrders(List<OrderPath> orders) {
        listOfOrders = orders;
    }

    public void setJobs(List<JobPath> jobs) {
        listOfJobs = jobs;
    }

    public List<String> getListOfEventClasses() {
        return listOfEventClasses;
    }

    public List<String> getListOfEventIds() {
        return listOfEventIds;
    }

    public List<Integer> getListOfExitCodes() {
        return listOfExitCodes;
    }

    public List<OrderPath> getListOfOrders() {
        return listOfOrders;
    }

    public List<JobPath> getListOfJobs() {
        return listOfJobs;
    }

    public List<Long> getListOfIds() {
        return listOfIds;
    }

    public void setIds(List<Long> ids) {
        this.listOfIds = ids;
    }

    @Override
    public boolean isFiltered(DbItem h) {
        return false;
    }

}