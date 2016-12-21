package com.sos.jitl.schedulerhistory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateIntervalFilter;
import com.sos.jitl.schedulerhistory.db.SchedulerOrderStepHistoryDBItem;

public class SchedulerOrderStepHistoryFilter extends SOSHibernateIntervalFilter implements com.sos.hibernate.interfaces.ISOSHibernateFilter {

    private Long historyId = null;
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";
    private Date executedFrom;
    private Date executedTo;
    private Date startTime;
    private Date endTime;
    private String status = "";
    private String executedFromIso = null;
    private String executedToIso = null;

    public String getExecutedFromIso() {
        return executedFromIso;
    }

    public void setExecutedFromIso(String executedFromIso) {
        this.executedFromIso = executedFromIso;
    }

    public String getExecutedToIso() {
        return executedToIso;
    }

    public void setExecutedToIso(String executedToIso) {
        this.executedToIso = executedToIso;
    }

    public SchedulerOrderStepHistoryFilter() {
        //
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Date getExecutedFromUtc() {
        return convertFromTimeZoneToUtc(executedFrom);
    }

    public void setExecutedFrom(String executedFrom) throws ParseException {
        if ("".equals(executedFrom)) {
            this.executedFrom = null;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            Date d = formatter.parse(executedFrom);
            setExecutedFrom(d);
        }
    }

    public Date getExecutedToUtc() {
        return convertFromTimeZoneToUtc(executedTo);
    }

    public void setExecutedTo(String executedTo) throws ParseException {
        if ("".equals(executedTo)) {
            this.executedTo = null;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            Date d = formatter.parse(executedTo);
            setExecutedTo(d);
        }
    }

    public boolean isFiltered(DbItem dbitem) {
        SchedulerOrderStepHistoryDBItem h = (SchedulerOrderStepHistoryDBItem) dbitem;
        return false;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setExecutedFrom(Date from) {
        this.executedFrom = from;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        executedFromIso = formatter.format(from);
    }

    public void setExecutedTo(Date to) {
        this.executedTo = to;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        executedToIso = formatter.format(to);
    }

    public void setStartTime(Date start) {
        this.startTime = start;
    }

    public void setEndTime(Date end) {
        this.endTime = end;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    @Override
    public String getTitle() {
        String s = "";
        if (executedFrom != null) {
            s += String.format("from: %s ", date2Iso(executedFrom));
        }
        if (executedTo != null) {
            s += String.format("to: %s ", date2Iso(executedTo));
        }
        return s;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(long historyId) {
        this.historyId = historyId;
    }

    @Override
    public void setIntervalFromDate(Date d) {
        this.executedFrom = d;
    }

    @Override
    public void setIntervalToDate(Date d) {
        this.executedTo = d;
    }

    @Override
    public void setIntervalFromDateIso(String s) {
        this.executedFromIso = s;
    }

    @Override
    public void setIntervalToDateIso(String s) {
        this.executedToIso = s;
    }

}
