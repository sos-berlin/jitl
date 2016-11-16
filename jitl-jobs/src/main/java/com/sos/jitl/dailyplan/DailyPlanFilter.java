package com.sos.jitl.dailyplan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import com.sos.dashboard.globals.DashBoardConstants;
import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateIntervalFilter;
import com.sos.hibernate.classes.SOSSearchFilterData;
import com.sos.hibernate.classes.UtcTimeHelper;
import com.sos.hibernate.interfaces.ISOSHibernateFilter;
import com.sos.scheduler.history.classes.SOSIgnoreList;

public class DailyPlanFilter extends SOSHibernateIntervalFilter implements ISOSHibernateFilter {

    private static final Logger LOGGER = Logger.getLogger(DailyPlanFilter.class);
    private Date plannedStartFrom;
    private Date executedFrom;
    private Date plannedStartTo;
    private Date executedTo;
    private boolean late = false;
    private String schedulerId;
    private SOSIgnoreList ignoreList = null;
    private SOSSearchFilterData sosSearchFilterData;
    private String plannedStartToIso;
    private String plannedStartFromIso;
    private String jobChain;
    private String orderId;
    private String job;
 

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

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public DailyPlanFilter() {
        super(DashBoardConstants.conPropertiesFileName);
        sosSearchFilterData = new SOSSearchFilterData();
        ignoreList = new SOSIgnoreList();
    }

    public SOSIgnoreList getIgnoreList() {
        return ignoreList;
    }

    public Date getPlannedStartUtcFrom() {
        if (plannedStartFrom == null) {
            return null;
        } else {
            return UtcTimeHelper.convertTimeZonesToDate(UtcTimeHelper.localTimeZoneString(), "UTC", new DateTime(plannedStartFrom));
        }
    }

    public Date getPlannedStartFrom() {
        if (plannedStartFrom == null) {
            return null;
        } else {
            return convertFromTimeZoneToUtc(plannedStartFrom);
        }
    }

    public void setPlannedStartFrom(Date plannedStartFrom) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String d = formatter.format(plannedStartFrom);
        try {
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.plannedStartFrom = formatter.parse(d);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void setPlannedStartFrom(String plannedStartFrom) throws ParseException {
        if (plannedStartFrom == null || "".equals(plannedStartFrom)) {
            this.plannedStartFrom = null;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            Date d = formatter.parse(plannedStartFrom);
            setPlannedStartFrom(d);
        }
    }

    public void setPlannedStartFrom(String plannedStartFrom, String dateFormat) throws ParseException {
        this.dateFormat = dateFormat;
        setPlannedStartFrom(plannedStartFrom);
    }

    public void setPlannedStartTo(String plannedStartTo, String dateFormat) throws ParseException {
        this.dateFormat = dateFormat;
        setPlannedStartTo(plannedStartTo);
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Date getExecutedUtcFrom() {
        if (executedFrom == null) {
            return null;
        } else {
            return UtcTimeHelper.convertTimeZonesToDate(UtcTimeHelper.localTimeZoneString(), "UTC", new DateTime(executedFrom));
        }
    }

    public void setExecutedFrom(Date executedFrom) {
        this.executedFrom = executedFrom;
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

    public Date getPlannedStartUtcTo() {
        if (plannedStartTo == null) {
            return null;
        } else {
            return UtcTimeHelper.convertTimeZonesToDate(UtcTimeHelper.localTimeZoneString(), "UTC", new DateTime(plannedStartTo));
        }
    }

    public Date getPlannedStartTo() {
        if (plannedStartTo == null) {
            return null;
        } else {
            return convertFromTimeZoneToUtc(plannedStartTo);
        }
    }

    public void setPlannedStartTo(Date plannedStartTo) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        String d = formatter.format(plannedStartTo);
        try {
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.plannedStartTo = formatter.parse(d);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void setPlannedStartTo(String plannedStartTo) throws ParseException {
        if (plannedStartTo == null || "".equals(plannedStartTo)) {
            this.plannedStartTo = null;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            Date d = formatter.parse(plannedStartTo);
            setPlannedStartTo(d);
        }
    }

    public Date getExecutedTo() {
        return UtcTimeHelper.convertTimeZonesToDate(UtcTimeHelper.localTimeZoneString(), "UTC", new DateTime(executedTo));
    }

    public void setExecutedTo(Date executedTo) {
        this.executedTo = executedTo;
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
 

    public boolean isLate() {
        return late;
    }

    public void setLate(boolean late) {
        this.late = late;
    }


    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    @Override
    public String getTitle() {
        String ignoreCount = "";
        int ignoreJobCount = getIgnoreList().size();
        if (ignoreJobCount > 0 || ignoreJobCount > 0) {
            ignoreCount = String.format("%1s Entries ignored", ignoreJobCount);
        }
        String s = "";
        if (schedulerId != null && !"".equals(schedulerId)) {
            s += String.format("Id: %s ", schedulerId);
        }
        if (plannedStartFrom != null) {
            s += String.format(Messages.getLabel(DashBoardConstants.conSOSDashB_FROM) + ": %s ", date2Iso(plannedStartFrom));
        }
        if (plannedStartTo != null) {
            s += String.format(Messages.getLabel(DashBoardConstants.conSOSDashB_TO) + ": %s ", date2Iso(plannedStartTo));
        }

        if (late) {
            s += " " + String.format(Messages.getLabel(DashBoardConstants.conSOSDashB_LATE));
        }
        return String.format("%1s %2s %3s %3s", s, status, getSosSearchFilterData().getSearchfield(), ignoreCount);
    }

    @Override
    public void setIntervalFromDate(Date d) {
        this.plannedStartFrom = d;
    }

    @Override
    public void setIntervalToDate(Date d) {
        this.plannedStartTo = d;
    }

    @Override
    public void setIntervalFromDateIso(String s) {
        this.plannedStartFromIso = s;
    }

    @Override
    public void setIntervalToDateIso(String s) {
        this.plannedStartToIso = s;
    }

    public SOSSearchFilterData getSosSearchFilterData() {
        if (sosSearchFilterData == null) {
            sosSearchFilterData = new SOSSearchFilterData();
        }
        return sosSearchFilterData;
    }

    public void setSosSearchFilterData(SOSSearchFilterData sosSearchFilterData) {
        this.sosSearchFilterData = sosSearchFilterData;
    }

    @Override
    public boolean isFiltered(DbItem h) {
        return false;
    }

}