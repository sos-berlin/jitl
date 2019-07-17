package com.sos.jitl.notification.helper.counters;

public class CounterSystemNotifier {

    private int success = 0;
    private int error = 0;
    private int skip = 0;
    private int total = 0;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int val) {
        success = val;
    }

    public void addSuccess() {
        success++;
    }

    public int getError() {
        return error;
    }

    public void setError(int val) {
        error = val;
    }

    public void addError() {
        error++;
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

    public int getTotal() {
        return total;
    }

    public void setTotal(int val) {
        total = val;
    }

    public void addTotal() {
        total++;
    }
}
