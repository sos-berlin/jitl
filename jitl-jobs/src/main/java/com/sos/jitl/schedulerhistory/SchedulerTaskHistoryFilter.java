package com.sos.jitl.schedulerhistory;

import com.sos.jitl.schedulerhistory.classes.HistorySeverity;
import com.sos.jitl.schedulerhistory.db.SchedulerTaskHistoryDBItem;

public class SchedulerTaskHistoryFilter extends SchedulerHistoryFilter implements com.sos.hibernate.interfaces.ISOSHibernateFilter {

    protected String jobname = null;
    protected HistorySeverity severity = null;
    private String status = "";

    public HistorySeverity getSeverity() {
        return severity;
    }

    public void setSeverity(HistorySeverity severity) {
        this.severity = severity;
    }

    public String getJobname() {
        if (jobname == null) {
            return jobname;
        }
        if (jobname.startsWith("/")) {
            return jobname.substring(1);
        } else {
            return jobname;
        }
    }

    public SchedulerTaskHistoryFilter() {
        super();
    }

    private boolean filterRunningOrError(SchedulerTaskHistoryDBItem h) {
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

    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setJobname(String jobname) {
        this.jobname = jobname;
    }

    public void setSeverity(String severity_) {
        if (this.severity == null) {
            this.severity = new HistorySeverity(severity_);
        } else {
            this.severity.setStrValue(severity_);
        }
    }

}