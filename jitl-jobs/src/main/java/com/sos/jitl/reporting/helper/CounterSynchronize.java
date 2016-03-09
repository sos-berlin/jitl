package com.sos.jitl.reporting.helper;

public class CounterSynchronize {

    int total = 0;
    int skip = 0;
    int triggers = 0;
    int executions = 0;
    int executionsBatch = 0;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getTriggers() {
        return triggers;
    }

    public void setTriggers(int triggers) {
        this.triggers = triggers;
    }

    public int getExecutions() {
        return executions;
    }

    public void setExecutions(int executions) {
        this.executions = executions;
    }

    public int getExecutionsBatch() {
        return executionsBatch;
    }

    public void setExecutionsBatch(int executionsBatch) {
        this.executionsBatch = executionsBatch;
    }

}
