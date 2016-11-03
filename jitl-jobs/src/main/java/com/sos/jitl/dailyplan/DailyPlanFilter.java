package com.sos.jitl.dailyplan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import com.sos.dailyschedule.db.DailyScheduleDBItem;
import com.sos.dashboard.globals.DashBoardConstants;
import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateIntervalFilter;
import com.sos.hibernate.classes.SOSSearchFilterData;
import com.sos.hibernate.classes.UtcTimeHelper;
import com.sos.hibernate.interfaces.ISOSHibernateFilter;
import com.sos.scheduler.history.classes.SOSIgnoreList;

public class DailyPlanFilter extends SOSHibernateIntervalFilter implements ISOSHibernateFilter {

    private static final Logger LOGGER = Logger.getLogger(DailyPlanFilter.class);
    private Date plannedFrom;
    private Date executedFrom;
    private Date plannedTo;
    private Date executedTo;
    private boolean showJobs = true;
    private boolean showJobChains = true;
    private boolean late = false;
    private String status = "";
    private String schedulerId = "";
    private SOSIgnoreList ignoreList = null;
    private SOSSearchFilterData sosSearchFilterData;
    private String plannedToIso;
    private String plannedFromIso;
 

    public DailyPlanFilter() {
        super(DashBoardConstants.conPropertiesFileName);
        sosSearchFilterData = new SOSSearchFilterData();
        ignoreList = new SOSIgnoreList();
    }

    public SOSIgnoreList getIgnoreList() {
        return ignoreList;
    }

    public Date getPlannedUtcFrom() {
        if (plannedFrom == null) {
            return null;
        } else {
            return UtcTimeHelper.convertTimeZonesToDate(UtcTimeHelper.localTimeZoneString(), "UTC", new DateTime(plannedFrom));
        }
    }

    public Date getPlannedFrom() {
        if (plannedFrom == null) {
            return null;
        } else {
            return convertFromTimeZoneToUtc(plannedFrom);
        }
    }

    public void setPlannedFrom(Date plannedFrom) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String d = formatter.format(plannedFrom);
        try {
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.plannedFrom = formatter.parse(d);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void setPlannedFrom(String plannedFrom) throws ParseException {
        if ("".equals(plannedFrom)) {
            this.plannedFrom = null;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            Date d = formatter.parse(plannedFrom);
            setPlannedFrom(d);
        }
    }

    public void setPlannedFrom(String plannedFrom, String dateFormat) throws ParseException {
        this.dateFormat = dateFormat;
        setPlannedFrom(plannedFrom);
    }

    public void setPlannedTo(String plannedTo, String dateFormat) throws ParseException {
        this.dateFormat = dateFormat;
        setPlannedTo(plannedTo);
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

    public Date getPlannedUtcTo() {
        if (plannedTo == null) {
            return null;
        } else {
            return UtcTimeHelper.convertTimeZonesToDate(UtcTimeHelper.localTimeZoneString(), "UTC", new DateTime(plannedTo));
        }
    }

    public Date getPlannedTo() {
        if (plannedTo == null) {
            return null;
        } else {
            return convertFromTimeZoneToUtc(plannedTo);
        }
    }

    public void setPlannedTo(Date plannedTo) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        String d = formatter.format(plannedTo);
        try {
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.plannedTo = formatter.parse(d);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void setPlannedTo(String plannedTo) throws ParseException {
        if ("".equals(plannedTo)) {
            this.plannedTo = null;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            Date d = formatter.parse(plannedTo);
            setPlannedTo(d);
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

    public boolean isFiltered(DbItem dbitem) {
        DailyScheduleDBItem h = (DailyScheduleDBItem) dbitem;
        boolean show =
                this.isShowJobChains() && this.isShowJobs() || this.isShowJobChains() && h.getJobChain() != null || this.isShowJobs()
                        && h.getJob() != null;
        return !show || this.getIgnoreList().contains(h) || this.isLate() && !h.getExecutionState().isLate() || !"".equals(this.getStatus())
                && !this.getStatus().equalsIgnoreCase(h.getExecutionState().getExecutionState()) || this.getSosSearchFilterData() != null
                && this.getSosSearchFilterData().getSearchfield() != null
                && !"".equals(this.getSosSearchFilterData().getSearchfield())
                && (h.getJobName() != null && !h.getJobName().toLowerCase().contains(this.getSosSearchFilterData().getSearchfield().toLowerCase()) || (h.getJobChain() != null
                        && h.getOrderId() != null && !(h.getJobChain().toLowerCase() + "~*~" + h.getOrderId()).toLowerCase().contains(
                        this.getSosSearchFilterData().getSearchfield().toLowerCase())));
    }

    public boolean isShowJobs() {
        return showJobs;
    }

    public void setShowJobs(boolean jobs) {
        this.showJobs = jobs;
    }

    public boolean isShowJobChains() {
        return showJobChains;
    }

    public void setShowJobChains(boolean showJobChains) {
        this.showJobChains = showJobChains;
    }

    public boolean isLate() {
        return late;
    }

    public void setLate(boolean late) {
        this.late = late;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        if (plannedFrom != null) {
            s += String.format(Messages.getLabel(DashBoardConstants.conSOSDashB_FROM) + ": %s ", date2Iso(plannedFrom));
        }
        if (plannedTo != null) {
            s += String.format(Messages.getLabel(DashBoardConstants.conSOSDashB_TO) + ": %s ", date2Iso(plannedTo));
        }
        if (showJobs) {
            s += String.format(Messages.getLabel(DashBoardConstants.conSOSDashB_JOBS));
        }
        if (showJobChains) {
            s += " " + String.format(Messages.getLabel(DashBoardConstants.conSOSDashB_JOBCHAINS));
        }
        if (late) {
            s += " " + String.format(Messages.getLabel(DashBoardConstants.conSOSDashB_LATE));
        }
        return String.format("%1s %2s %3s %3s", s, status, getSosSearchFilterData().getSearchfield(), ignoreCount);
    }

    @Override
    public void setIntervalFromDate(Date d) {
        this.plannedFrom = d;
    }

    @Override
    public void setIntervalToDate(Date d) {
        this.plannedTo = d;
    }

    @Override
    public void setIntervalFromDateIso(String s) {
        this.plannedFromIso = s;
    }

    @Override
    public void setIntervalToDateIso(String s) {
        this.plannedToIso = s;
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

}