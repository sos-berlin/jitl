package com.sos.jitl.reporting.db.filter;

import java.util.ArrayList;

public class ReportExecutionFilter extends ReportHistoryFilter {

    private String orderHistoryId;
    String status = "";
    private ArrayList<String> listOfJobs;

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

}