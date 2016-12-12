package com.sos.jitl.reporting.db.filter;

import java.util.ArrayList;

public class ReportExecutionFilter extends ReportHistoryFilter {

    private String orderHistoryId;
    String status = "";
    private ArrayList<String> listOfJobs;
    private ArrayList<String> states;

    public ArrayList<String> getListOfJobs() {
        return listOfJobs;
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

    public String getOrderHistoryId() {
        return orderHistoryId;
    }

    public void setOrderHistoryId(String orderHistoryId) {
        this.orderHistoryId = orderHistoryId;
    }

    public ArrayList<String> getStates() {
        return states;
    }

}