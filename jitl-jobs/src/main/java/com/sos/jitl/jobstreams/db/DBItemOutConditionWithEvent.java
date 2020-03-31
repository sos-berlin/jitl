package com.sos.jitl.jobstreams.db;

import com.sos.jitl.jobstreams.interfaces.IJSJobConditionKey;
 
public class DBItemOutConditionWithEvent implements IJSJobConditionKey {

    private DBItemOutCondition dbItemOutCondition;
    private DBItemEvent dbItemEvent;

    public DBItemOutConditionWithEvent(DBItemOutCondition dbItemOutCondition, DBItemEvent dbItemEvent) {
        this.dbItemOutCondition = dbItemOutCondition;
        this.dbItemEvent = dbItemEvent;
    }

    public DBItemOutCondition getDbItemOutCondition() {
        return dbItemOutCondition;
    }

    public void setDbItemOutCondition(DBItemOutCondition dbItemOutCondition) {
        this.dbItemOutCondition = dbItemOutCondition;
    }

    public DBItemEvent getDbItemEvent() {
        return dbItemEvent;
    }

    public void setDbItemEvent(DBItemEvent dbItemEvent) {
        this.dbItemEvent = dbItemEvent;
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