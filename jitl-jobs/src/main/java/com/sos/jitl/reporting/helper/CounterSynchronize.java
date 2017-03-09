package com.sos.jitl.reporting.helper;

public class CounterSynchronize {

    int total = 0;
    int skip = 0;
    int triggers = 0;
    int executions = 0;
    int orderExecutions = 0;
    int standaloneExecutions = 0;

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

    public int getTriggers() {
        return triggers;
    }

    public void setTriggers(int val) {
        this.triggers = val;
    }

    public int getExecutions() {
        return executions;
    }

    public void setExecutions(int val) {
        this.executions = val;
    }

    public int getStandaloneExecutions() {
        return standaloneExecutions;
    }

    public void setStandaloneExecutions(int val) {
        this.standaloneExecutions = val;
    }

    public int getOrderExecutions() {
        return orderExecutions;
    }

    public void setOrderExecutions(int val) {
        this.orderExecutions = val;
    }
}
