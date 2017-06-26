package com.sos.jitl.dailyplan.db;

import java.util.Date;

public class DailyPlanCalender2DBFilter {

    private String forJob;
    private String forJobChain;
    private String forOrderId;

    public String getForJob() {
        return forJob;
    }

    public void setForJob(String forJob) {
        this.forJob = forJob;
    }

    public String getForJobChain() {
        return forJobChain;
    }

    public void setForJobChain(String forJobChain) {
        this.forJobChain = forJobChain;
    }

    public String getForOrderId() {
        return forOrderId;
    }

    public void setForOrderId(String forOrderId) {
        this.forOrderId = forOrderId;
    }

    public boolean handleEntry(String job, String jobChain, String orderId) {
        if (!"".equals(forJob) && job != null && job.equals(forJob)) {
            return true;
        }
        if (!"".equals(forJobChain) && jobChain != null && jobChain.equals(forJobChain)) {
            return true;
        }
        if (!"".equals(forOrderId) && orderId != null && orderId.equals(forOrderId)) {
            return true;
        }
        return false;

    }

}
