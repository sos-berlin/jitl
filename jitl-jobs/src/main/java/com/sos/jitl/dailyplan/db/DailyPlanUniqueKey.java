package com.sos.jitl.dailyplan.db;

import java.util.Date;

public class DailyPlanUniqueKey {

    private String schedulerId;
    private String job;
    private String jobChain;
    private String orderId;
    private Date plannedStart;
    
    

    public DailyPlanUniqueKey(DailyPlanDBItem dailyPlanDBItem) {
        super();
        this.schedulerId = dailyPlanDBItem.getSchedulerId();
        this.job = dailyPlanDBItem.getJob();
        this.jobChain = dailyPlanDBItem.getJobChain();
        this.orderId = dailyPlanDBItem.getOrderId();
        this.plannedStart = dailyPlanDBItem.getPlannedStart();
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getJobChain() {
        return jobChain;
    }

    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getPlannedStart() {
        return plannedStart;
    }

    public void setPlannedStart(Date plannedStart) {
        this.plannedStart = plannedStart;
    }

    
    public String asString() {
        return schedulerId + "." + job + "." + jobChain + "." + orderId + "." + plannedStart;
    }
    
    @Override
    public int hashCode() {
        return (schedulerId + "." + job + "." + jobChain + "." + orderId + "." + plannedStart).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        DailyPlanUniqueKey other = (DailyPlanUniqueKey) obj;
        if (job == null) {
            if (other.job != null) {
                return false;
            }
        } else if (!job.equals(other.job)) {
            return false;
        }
        if (jobChain == null) {
            if (other.jobChain != null) {
                return false;
            }
        } else if (!jobChain.equals(other.jobChain)) {
            return false;
        }
        if (orderId == null) {
            if (other.orderId != null) {
                return false;
            }
        } else if (!orderId.equals(other.orderId)) {
            return false;
        }
        if (plannedStart == null) {
            if (other.plannedStart != null) {
                return false;
            }
        } else if (!plannedStart.equals(other.plannedStart)) {
            return false;
        }
        if (schedulerId == null) {
            if (other.schedulerId != null) {
                return false;
            }
        } else if (!schedulerId.equals(other.schedulerId)) {
            return false;
        }
        return true;
    }

}
