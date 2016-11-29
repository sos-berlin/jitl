package com.sos.jitl.dailyplan.filter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.log4j.Logger;
import com.sos.hibernate.classes.SOSHibernateIntervalFilter;

public class DailyPlanFilter extends SOSHibernateIntervalFilter {

    public ArrayList<String> getStates() {
        return states;
    }

    private static final Logger LOGGER = Logger.getLogger(DailyPlanFilter.class);
    private Date plannedStartFrom;
    private Date plannedStartTo;
    private Boolean isLate;
    private String schedulerId;
    private String jobChain;
    private String orderId;
    private String job;
    private ArrayList<String> states;

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

}