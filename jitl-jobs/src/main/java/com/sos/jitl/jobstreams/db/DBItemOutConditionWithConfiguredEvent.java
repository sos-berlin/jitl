package com.sos.jitl.jobstreams.db;

import com.sos.jitl.jobstreams.interfaces.IJSJobConditionKey;

public class DBItemOutConditionWithConfiguredEvent implements IJSJobConditionKey {

    private DBItemOutCondition dbItemOutCondition;
    private DBItemOutConditionEvent dbItemOutConditionEvent;

    public DBItemOutConditionWithConfiguredEvent(DBItemOutCondition dbItemOutCondition, DBItemOutConditionEvent dbItemOutConditionEvent) {
        this.dbItemOutCondition = dbItemOutCondition;
        this.dbItemOutConditionEvent = dbItemOutConditionEvent;
    }

    public DBItemOutCondition getDbItemOutCondition() {
        return dbItemOutCondition;
    }

    public void setDbItemOutCondition(DBItemOutCondition dbItemOutCondition) {
        this.dbItemOutCondition = dbItemOutCondition;
    }

    public DBItemOutConditionEvent getDbItemOutConditionEvent() {
        return dbItemOutConditionEvent;
    }

    public void setDbItemOutConditionEvent(DBItemOutConditionEvent dbItemOutConditionEvent) {
        this.dbItemOutConditionEvent = dbItemOutConditionEvent;
    }

    @Override
    public String getJobSchedulerId() {
        return dbItemOutCondition.getJobSchedulerId();
    }

    @Override
    public String getJob() {
        return dbItemOutCondition.getJob();
    }

    @Override
    public String getJobStream() {
        return dbItemOutCondition.getJobStream();
    }

}