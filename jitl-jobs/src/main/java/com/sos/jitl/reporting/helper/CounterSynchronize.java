package com.sos.jitl.reporting.helper;

public class CounterSynchronize {

    int total = 0;
    int skip = 0;
    int insertedTriggers = 0;
    int updatedTriggers = 0;
    int insertedExecutions = 0;
    int updatedExecutions = 0;
    int insertedTasks = 0;
    int updatedTasks = 0;
    
    public int getTotal() {
        return total;
    }

    public void setTotal(int val) {
        this.total = val;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int val) {
        this.skip = val;
    }

    public int getInsertedTriggers() {
        return insertedTriggers;
    }

    public void setInsertedTriggers(int val) {
        this.insertedTriggers = val;
    }

    public int getUpdatedTriggers() {
        return updatedTriggers;
    }

    public void setUpdatedTriggers(int val) {
        this.updatedTriggers = val;
    }
    
    public int getInsertedExecutions() {
        return insertedExecutions;
    }

    public void setInsertedExecutions(int val) {
        this.insertedExecutions = val;
    }

    public int getUpdatedExecutions() {
        return updatedExecutions;
    }

    public void setUpdatedExecutions(int val) {
        this.updatedExecutions = val;
    }

    public int getInsertedTasks() {
        return insertedTasks;
    }

    public void setInsertedTasks(int val) {
        this.insertedTasks = val;
    }
    
    public int getUpdatedTasks() {
        return updatedTasks;
    }

    public void setUpdatedTasks(int val) {
        this.updatedTasks = val;
    }
}
