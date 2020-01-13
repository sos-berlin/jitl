package com.sos.jitl.reporting.db.filter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sos.hibernate.classes.SOSHibernateIntervalFilter;
import com.sos.joc.model.common.Folder;

public class ReportHistoryFilter extends SOSHibernateIntervalFilter {

    private Set<Long> historyIds;
    private ArrayList<String> listOfJobchains;

    private String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    private Date executedFrom;
    private Date executedTo;
    private Date startTime;
    private Date endTime;
    private String schedulerId = "";
    private String orderId;
    private String jobChain;
    private Set<Folder> listOfFolders;

    public ReportHistoryFilter() {
        super();
    }

    public void setListOfFolders(Set<Folder> listOfFolders) {
        this.listOfFolders = listOfFolders;
    }

    public Set<Folder> getListOfFolders() {
        return listOfFolders;
    }
    
    public void addFolderPaths(Set<Folder> folders) {
        if (listOfFolders == null) {
            listOfFolders = new HashSet<Folder>();
        }
        if (folders != null) {
            listOfFolders.addAll(folders);
        }
    }
    
    public void addFolderPath(Folder folder) {
        if (listOfFolders == null) {
            listOfFolders = new HashSet<Folder>();
        }
        if (folder != null) {
            listOfFolders.add(folder);
        }
    }

    public void addFolderPath(String folder, boolean recursive) {
        if (listOfFolders == null) {
            listOfFolders = new HashSet<Folder>();
        }
        Folder filterFolder = new Folder();
        filterFolder.setFolder(folder);
        filterFolder.setRecursive(recursive);
        listOfFolders.add(filterFolder);
    }

    @Override
    public String getDateFormat() {
        return dateFormat;
    }

    @Override
    public void setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setExecutedFrom(final String executedFrom, String parseDateFormat) throws ParseException {
        if ("".equals(executedFrom)) {
            this.executedFrom = null;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(parseDateFormat);
            setExecutedFrom(formatter.parse(executedFrom));
        }
    }

    public Date getExecutedFrom() {
        return executedFrom;
    }

    public Date getExecutedTo() {
        return executedTo;
    }

    public void setExecutedTo(final String executedTo, String parseDateFormat) throws ParseException {
        if ("".equals(executedTo)) {
            this.executedTo = null;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(parseDateFormat);
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
        executedFrom = from;
    }

    public void setExecutedTo(final Date to) {
        executedTo = to;
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
    public void setIntervalFromDate(Date d) {
        this.executedFrom = d;
    }

    @Override
    public void setIntervalToDate(Date d) {
        this.executedTo = d;
    }

    @Override
    public void setIntervalFromDateIso(String s) {
    }

    @Override
    public void setIntervalToDateIso(String s) {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getJobChain() {
        return jobChain;
    }

    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    public Set<Long> getHistoryIds() {
        return historyIds;
    }

    public void setHistoryIds(Set<Long> historyIds) {
        this.historyIds = historyIds;
    }
    
    public void setHistoryIds(List<Long> historyIds) {
        this.historyIds = new HashSet<Long>(historyIds);
    }


    public ArrayList<String> getListOfJobchains() {
        return listOfJobchains;
    }
    
    public void addJobChainPath(String jobChain) {
        if (listOfJobchains == null) {
            listOfJobchains = new ArrayList<String>();
        }
        listOfJobchains.add(jobChain);

    }

}