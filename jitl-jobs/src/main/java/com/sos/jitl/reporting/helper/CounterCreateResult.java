package com.sos.jitl.reporting.helper;

public class CounterCreateResult {

    int totalUncompleted = 0;
    int triggerResults = 0;
    int triggerResultsBatch = 0;
    int executionsDates = 0;
    int executionsDatesBatch = 0;

    public int getTotalUncompleted() {
        return totalUncompleted;
    }

    public void setTotalUncompleted(int totalUncompleted) {
        this.totalUncompleted = totalUncompleted;
    }

    public int getTriggerResults() {
        return triggerResults;
    }

    public void setTriggerResults(int triggerResults) {
        this.triggerResults = triggerResults;
    }

    public int getTriggerResultsBatch() {
        return triggerResultsBatch;
    }

    public void setTriggerResultsBatch(int triggerResultsBatch) {
        this.triggerResultsBatch = triggerResultsBatch;
    }

    public int getExecutionsDates() {
        return executionsDates;
    }

    public void setExecutionsDates(int executionsDates) {
        this.executionsDates = executionsDates;
    }

    public int getExecutionsDatesBatch() {
        return executionsDatesBatch;
    }

    public void setExecutionsDatesBatch(int executionsDatesBatch) {
        this.executionsDatesBatch = executionsDatesBatch;
    }

}
