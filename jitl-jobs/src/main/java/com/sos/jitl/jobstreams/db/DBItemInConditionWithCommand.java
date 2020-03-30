package com.sos.jitl.jobstreams.db;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Transient;

import com.sos.jitl.jobstreams.interfaces.IJSJobConditionKey;

public class DBItemInConditionWithCommand implements IJSJobConditionKey {

    private DBItemInCondition dbItemInCondition;
    private DBItemInConditionCommand dbItemInConditionCommand;
    private Set<String> consumedForContext;


    public DBItemInConditionWithCommand(DBItemInCondition dbItemInCondition, DBItemInConditionCommand dbItemInConditionCommand) {
        this.dbItemInCondition = dbItemInCondition;
        this.dbItemInConditionCommand = dbItemInConditionCommand;
    }

    public DBItemInCondition getDbItemInCondition() {
        return dbItemInCondition;
    }

    public void setDbItemInCondition(DBItemInCondition dbItemInCondition) {
        this.dbItemInCondition = dbItemInCondition;
    }

    public DBItemInConditionCommand getDbItemInConditionCommand() {
        return dbItemInConditionCommand;
    }

    public void setDbItemInConditionCommand(DBItemInConditionCommand dbItemInConditionCommand) {
        this.dbItemInConditionCommand = dbItemInConditionCommand;
    }

    @Transient
    public boolean isConsumed(String context) {
        return consumedForContext.contains(context);
    }

    public void setConsumed(String context) {
        if (consumedForContext == null) {
            consumedForContext = new HashSet<String>();
        }
        this.consumedForContext.add(context);
    }

    @Override
    public String getJobSchedulerId() {
        return this.dbItemInCondition.getJobSchedulerId();
    }

    @Override
    public String getJob() {
        return this.dbItemInCondition.getJob();
    }

    @Transient
    public Set<String> getConsumedForContext() {
        return consumedForContext;
    }
 

}