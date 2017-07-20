package com.sos.jitl.dailyplan.filter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.log4j.Logger;
import com.sos.hibernate.classes.SOSHibernateIntervalFilter;
import com.sos.jitl.dailyplan.db.DailyPlanCalender2DBFilter;
import com.sos.jitl.reporting.db.filter.FilterFolder;

public class DailyPlanFilter extends SOSHibernateIntervalFilter {

    private static final Logger LOGGER = Logger.getLogger(DailyPlanFilter.class);
    private Date plannedStart;
    private Date plannedStartFrom;
    private Date plannedStartTo;
    private Boolean isLate;
    private String schedulerId;
    private String jobChain;
    private String orderId;
    private String job;
    private ArrayList<String> states;
    private ArrayList<FilterFolder> listOfFolders;

    public ArrayList<FilterFolder> getListOfFolders() {
        return listOfFolders;
    }

    public void addFolderPath(String folder, boolean recursive) {
        if (listOfFolders == null) {
            listOfFolders = new ArrayList<FilterFolder>();
        }
        FilterFolder filterFolder = new FilterFolder();
        filterFolder.setFolder(folder);
        filterFolder.setRecursive(recursive);
        listOfFolders.add(filterFolder);
    }

    public ArrayList<String> getStates() {
        return states;
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

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public DailyPlanFilter() {
        super();
    }

    public Date getPlannedStartFrom() {
        return plannedStartFrom;
    }

    public void setPlannedStartFrom(Date plannedStartFrom) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        String d = formatter.format(plannedStartFrom);
        try {
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

    public Date getPlannedStartTo() {
        return plannedStartTo;
    }

    public void setPlannedStartTo(Date plannedStartTo) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        String d = formatter.format(plannedStartTo);
        try {
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

    public Boolean isLate() {
        return isLate != null && isLate;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public void setLate(Boolean late) {
        this.isLate = late;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public void addState(String state) {
        if (states == null) {
            states = new ArrayList<String>();
        }
        states.add(state);
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
    }

    @Override
    public void setIntervalToDateIso(String s) {
    }

    public void setCalender2DBFilter(DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter) {
        if (!"".equals(dailyPlanCalender2DBFilter.getForJob())) {
            setJob(dailyPlanCalender2DBFilter.getForJob());
        }
        if (!"".equals(dailyPlanCalender2DBFilter.getForJobChain())) {
            setJobChain(dailyPlanCalender2DBFilter.getForJobChain());
        }
        if (!"".equals(dailyPlanCalender2DBFilter.getForOrderId())) {
            setOrderId(dailyPlanCalender2DBFilter.getForOrderId());
        }

    }

    public void setPlannedStart(Date plannedStart) {
        this.plannedStart = plannedStart;
    }

    public Date getPlannedStart() {
        return this.plannedStart;
    }

    public boolean containsFolder(String path) {
        if (listOfFolders == null || listOfFolders.size() == 0) {
            return true;
        } else {
            Path p = Paths.get(path).getParent();
            String parent = "";
            if (p != null) {
                parent = p.toString().replace('\\', '/');
            }
            for (FilterFolder folder : listOfFolders) {
                if ((folder.isRecursive() && parent.startsWith(folder.getFolder())) || folder.equals(parent)) {
                    return true;
                }
            }
        }
        return false;
    }

}