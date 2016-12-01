package com.sos.jitl.schedulerhistory;

import com.sos.hibernate.classes.DbItem;
import com.sos.scheduler.history.db.SchedulerOrderHistoryDBItem;

public class SchedulerOrderHistoryFilter extends SchedulerHistoryFilter implements com.sos.hibernate.interfaces.ISOSHibernateFilter {

    protected String jobchain = null;
    protected String orderid = null;
    protected String orderStates = null;
    protected Long schedulerOrderHistoryId = null;
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

    private boolean filterRunningOrError(SchedulerOrderHistoryDBItem h) {
        if (this.isShowWithError() && !this.isShowRunning()) {
            return !h.haveError();
        }
        if (!this.isShowWithError() && this.isShowRunning()) {
            return h.getEndTime() != null || h.haveError();
        }
        if (this.isShowWithError() && this.isShowRunning()) {
            return !(h.getEndTime() == null || h.haveError());
        }
        return false;
    }

    public boolean isFiltered(DbItem dbitem) {
        SchedulerOrderHistoryDBItem h = (SchedulerOrderHistoryDBItem) dbitem;
        return (this.getOrderIgnoreList().contains(h) || filterRunningOrError(h) || (this.getSosSearchFilterData() != null 
                && this.getSosSearchFilterData().getSearchfield() != null && !"".equals(this.getSosSearchFilterData().getSearchfield())) 
                && h.getJobChain() != null && !h.getJobChain().toLowerCase().contains(this.getSosSearchFilterData().getSearchfield().toLowerCase()) 
                && h.getOrderId() != null && !h.getOrderId().toLowerCase().contains(this.getSosSearchFilterData().getSearchfield().toLowerCase()));
    }

    public SchedulerOrderHistoryFilter() {
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

    public Long getSchedulerOrderHistoryId() {
        return schedulerOrderHistoryId;
    }

    public void setSchedulerOrderHistoryId(Long schedulerOrderHistoryId) {
        this.schedulerOrderHistoryId = schedulerOrderHistoryId;
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