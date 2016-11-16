package com.sos.jitl.dailyplan.filter;

public class ReportTriggerFilter extends ReportHistoryFilter {

    protected String jobchain = null;
    protected String orderid = null;
    protected String orderStates = null;
    protected Long reportTriggerId = null;
    private boolean isShowRunning = false;
    private String status = "";

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getJobchain() {
        if (jobchain != null && jobchain.startsWith("/")) {
            return jobchain.substring(1);
        } else {
            return jobchain;
        }
    }


    public ReportTriggerFilter() {
        super();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setJobchain(String jobchain) {
        this.jobchain = jobchain;
    }

    public Long getReportTriggerId() {
        return reportTriggerId;
    }

    public void setReportTriggerId(Long reportTriggerId) {
        this.reportTriggerId = reportTriggerId;
    }

    public String getOrderStates() {
        return orderStates;
    }

    public void setOrderStates(String orderStates) {
        this.orderStates = orderStates;
    }

    public boolean isShowRunning() {
        return isShowRunning;
    }

    public void setShowRunning(boolean isShowRunning) {
        this.isShowRunning = isShowRunning;
    }

}