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
    int transferHistory = 0;

    public int getTotal() {
        return total;
    }

    public void setTotal(int val) {
        total = val;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int val) {
        skip = val;
    }

    public int getInsertedTriggers() {
        return insertedTriggers;
    }

    public void setInsertedTriggers(int val) {
        insertedTriggers = val;
    }

    public int getUpdatedTriggers() {
        return updatedTriggers;
    }

    public void setUpdatedTriggers(int val) {
        updatedTriggers = val;
    }

    public int getInsertedExecutions() {
        return insertedExecutions;
    }

    public void setInsertedExecutions(int val) {
        insertedExecutions = val;
    }

    public int getUpdatedExecutions() {
        return updatedExecutions;
    }

    public void setUpdatedExecutions(int val) {
        updatedExecutions = val;
    }

    public int getInsertedTasks() {
        return insertedTasks;
    }

    public void setInsertedTasks(int val) {
        insertedTasks = val;
    }

    public int getUpdatedTasks() {
        return updatedTasks;
    }

    public void setUpdatedTasks(int val) {
        updatedTasks = val;
    }

    public int getTransferHistory() {
        return transferHistory;
    }

    public void setTransferHistory(int val) {
        transferHistory = val;
    }
}
