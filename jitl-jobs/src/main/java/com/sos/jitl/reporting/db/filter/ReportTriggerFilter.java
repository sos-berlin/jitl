package com.sos.jitl.reporting.db.filter;

import java.util.ArrayList;
import java.util.List;
import com.sos.jitl.reporting.db.DBItemReportTrigger;

public class ReportTriggerFilter extends ReportHistoryFilter {

    private Long reportTriggerId = null;
    private List<DBItemReportTrigger> listOfReportItems;
    private Boolean failed;
    private Boolean success;
    private ArrayList<String> states;

    public List<DBItemReportTrigger> getListOfReportItems() {
        return listOfReportItems;
    }

    public ReportTriggerFilter() {
        super();
    }

    public void addState(String state) {
        if (states == null) {
            states = new ArrayList<String>();
        }
        states.add(state);
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getReportTriggerId() {
        return reportTriggerId;
    }

    public void setReportTriggerId(Long reportTriggerId) {
        this.reportTriggerId = reportTriggerId;
    }

    public void addOrderPath(String jobChain, String orderId) {
        if (listOfReportItems == null) {
            listOfReportItems = new ArrayList<DBItemReportTrigger>();
        }
        DBItemReportTrigger d = new DBItemReportTrigger();
        d.setParentName(jobChain);
        d.setName(orderId);
        listOfReportItems.add(d);
    }

    public Boolean getFailed() {
        return failed;
    }

    public void setFailed(Boolean failed) {
        this.failed = failed;
        this.success = null;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
        this.failed = null;
    }

    public ArrayList<String> getStates() {
        return states;
    }

}