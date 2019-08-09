package com.sos.jitl.reporting.db.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReportExecutionFilter extends ReportHistoryFilter {

    private Set<Long> taskIds;
    private String orderHistoryId;
    String status = "";
    private ArrayList<String> listOfJobs;
    private ArrayList<String> listOfExcludedJobs;
    private ArrayList<String> states;

    public ArrayList<String> getListOfJobs() {
        return listOfJobs;
    }

    public ArrayList<String> getListOfExcludedJobs() {
        return listOfExcludedJobs;
    }

    public ReportExecutionFilter() {
        super();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void addState(String state) {
        if (states == null) {
            states = new ArrayList<String>();
        }
        states.add(state);
    }

    public void addJobPath(String job) {
        if (listOfJobs == null) {
            listOfJobs = new ArrayList<String>();
        }
        listOfJobs.add(job);

    }

    public void addExcludedJob(String job) {
        if (listOfExcludedJobs == null) {
            listOfExcludedJobs = new ArrayList<String>();
        }
        listOfExcludedJobs.add(job);

    }

    public String getOrderHistoryId() {
        return orderHistoryId;
    }

    public void setOrderHistoryId(String orderHistoryId) {
        this.orderHistoryId = orderHistoryId;
    }

    public ArrayList<String> getStates() {
        return states;
    }

    public Set<Long> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(Set<Long> taskIds) {
        this.taskIds = taskIds;
    }
    
    public void setTaskIds(List<Long> taskIds) {
        this.taskIds = new HashSet<Long>(taskIds);
    }

}