package com.sos.jitl.notification.helper.counters;

public class CounterCheckHistoryTimer {

    private int total = 0;
    private int rerun = 0;
    private int remove = 0;
    private int skip = 0;

    public int getTotal() {
        return total;
    }

    public void setTotal(int val) {
        total = val;
    }

    public void addTotal() {
        total++;
    }

    public int getRerun() {
        return rerun;
    }

    public void setRerun(int val) {
        rerun = val;
    }

    public void addRerun() {
        rerun++;
    }

    public int getRemove() {
        return remove;
    }

    public void setRemove(int val) {
        remove = val;
    }

    public void addRemove() {
        remove++;
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
}
