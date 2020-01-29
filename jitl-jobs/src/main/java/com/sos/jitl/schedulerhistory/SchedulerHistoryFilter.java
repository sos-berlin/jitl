package com.sos.jitl.schedulerhistory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateIntervalFilter;
import com.sos.hibernate.classes.SOSSearchFilterData;
import com.sos.hibernate.interfaces.ISOSHibernateFilter;

public class SchedulerHistoryFilter extends SOSHibernateIntervalFilter implements ISOSHibernateFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerHistoryFilter.class);
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";
    private Date executedFrom;
    private Date executedTo;
    private Date startTime;
    private Date endTime;
    private String schedulerId = "";
    private boolean showWithError = false;
    private boolean showRunning = false;
    private boolean showSuccessfull = false;
    private String executedFromIso = null;
    private String executedToIso = null;
    private boolean showJobs = true;
    private boolean showJobChains = true;
    private SOSSearchFilterData sosSearchFilterData;

    public SchedulerHistoryFilter() {
        super();
        sosSearchFilterData = new SOSSearchFilterData();
    }

    public boolean isShowJobs() {
        return showJobs;
    }

    public void setShowJobs(final boolean showJobs) {
        this.showJobs = showJobs;
    }

    public boolean isShowJobChains() {
        return showJobChains;
    }

    public void setShowJobChains(final boolean showJobChains) {
        this.showJobChains = showJobChains;
    }

    public String getExecutedFromIso() {
        return executedFromIso;
    }

    public void setExecutedFromIso(final String executedFromIso) {
        this.executedFromIso = executedFromIso;
    }

    public String getExecutedToIso() {
        return executedToIso;
    }

    public void setExecutedToIso(final String executedToIso) {
        this.executedToIso = executedToIso;
    }


    @Override
    public String getDateFormat() {
        return dateFormat;
    }

    @Override
    public void setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Date getExecutedUtcFrom() {
        if (executedFrom == null) {
            return null;
        } else {
            return convertFromTimeZoneToUtc(executedFrom);
        }
    }

    public void setExecutedFrom(final String executedFrom) throws ParseException {
        if ("".equals(executedFrom)) {
            this.executedFrom = null;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            setExecutedFrom(formatter.parse(executedFrom));
        }
    }

    public Date getExecutedUtcTo() {
        if (executedTo == null) {
            return null;
        } else {
            return convertFromTimeZoneToUtc(executedTo);
        }
    }

    public void setExecutedTo(final String executedTo) throws ParseException {
        if ("".equals(executedTo)) {
            this.executedTo = null;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            setExecutedTo(formatter.parse(executedTo));
        }
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(final String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public void setExecutedFrom(final Date from) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String d = formatter.format(from);
        try {
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            executedFrom = formatter.parse(d);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        executedFromIso = formatter.format(from);
    }

    public void setExecutedTo(final Date to) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        String d = formatter.format(to);
        try {
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            executedTo = formatter.parse(d);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        executedToIso = formatter.format(to);
    }

    public void setStartTime(final Date start) {
        startTime = start;
    }

    public void setEndTime(final Date end) {
        endTime = end;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    @Override
    public boolean isFiltered(final DbItem h) {
        return false;
    }

    public void setShowWithError(final boolean showWithError) {
        this.showWithError = showWithError;
    }

    public boolean isShowWithError() {
        return showWithError;
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

    public void setShowRunning(boolean showRunning) {
        this.showRunning = showRunning;
    }

    public boolean isShowRunning() {
        return showRunning;
    }

    public boolean isShowSuccessfull() {
        return showSuccessfull;
    }

    public void setShowSuccessfull(boolean showSuccessfull) {
        this.showSuccessfull = showSuccessfull;
    }

    public SOSSearchFilterData getSosSearchFilterData() {
        return this.sosSearchFilterData;
    }

    public void setSosSearchFilterData(final SOSSearchFilterData sosSearchFilterData) {
        this.sosSearchFilterData = sosSearchFilterData;
    }

    @Override
    public String getTitle() {
        return "";
    }

}