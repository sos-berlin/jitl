package com.sos.jitl.notification.helper.counters;

public class CounterCheckHistory {

    int total = 0;
    int skip = 0;
    int insert = 0;
    int update = 0;
    int insertTimer = 0;
    int batchInsert = 0;
    int batchInsertTimer = 0;

    public int getTotal() {
        return total;
    }

    public void setTotal(int val) {
        total = val;
    }

    public void addTotal() {
        total++;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int val) {
        skip = val;
    }

    public void addSkip() {
        skip++;
    }

    public int getInsert() {
        return insert;
    }

    public void setInsert(int val) {
        insert = val;
    }

    public void addInsert() {
        insert++;
    }

    public int getUpdate() {
        return update;
    }

    public void setUpdate(int val) {
        update = val;
    }

    public void addUpdate() {
        update++;
    }

    public int getInsertTimer() {
        return insertTimer;
    }

    public void setInsertTimer(int val) {
        insertTimer = val;
    }

    public void addInsertTimer() {
        insertTimer++;
    }

    public int getBatchInsert() {
        return batchInsert;
    }

    public void setBatchInsert(int val) {
        batchInsert = val;
    }

    public void addBatchInsert(int val) {
        batchInsert += val;
    }

    public void addBatchInsert() {
        addBatchInsert(1);
    }

    public int getBatchInsertTimer() {
        return batchInsertTimer;
    }

    public void setBatchInsertTimer(int val) {
        batchInsertTimer = val;
    }

    public void addBatchInsertTimer(int val) {
        batchInsertTimer += val;
    }

    public void addBatchInsertTimer() {
        addBatchInsertTimer(1);
    }
}
