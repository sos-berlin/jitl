package com.sos.jitl.dailyplan.db;

import java.util.Date;

public class DailyPlanCalender2DBFilter {

    private String forJob;
    private String forJobChain;
    private String forOrderId;
    private String forSchedule;

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
    
    public void setForSchedule(String forSchedule) {
        this.forSchedule = forSchedule;
    }

    public boolean handleEntry(String job, String jobChain, String orderId) {
        if (!"".equals(forJob) && job != null && job.equals(forJob)) {
            return true;
        }
        String s1 = forJobChain + "(" + forOrderId + ")";
        String s2 = jobChain + "(" + orderId + ")";
        if (orderId != null && jobChain != null && s1.equals(s2) ) {
            return true;
        }
        return false;

    }

    public String getKey(){
        return forJob + ":" + forJobChain + ":" + forOrderId;
    }
    
    public String getForSchedule() {
        return forSchedule;
    }
}
