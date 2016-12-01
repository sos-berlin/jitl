package com.sos.jitl.schedulerhistory.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
public class SchedulerOrderStepHistoryCompoundKey implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long historyId;
    private Long step;

    public SchedulerOrderStepHistoryCompoundKey() {
    }

    public SchedulerOrderStepHistoryCompoundKey(Long historyId, Long step) {
        this.historyId = historyId;
        this.step = step;
    }

    @Column(name = "`HISTORY_ID`", nullable = false)
    public Long getHistoryId() {
        return historyId;
    }

    @Column(name = "`HISTORY_ID`", nullable = false)
    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    @Column(name = "`STEP`", nullable = false)
    public Long getStep() {
        return step;
    }

    @Column(name = "`STEP`", nullable = false)
    public void setStep(Long step) {
        this.step = step;
    }

    @Transient
    public String getStepValue() {
        return String.valueOf(step);
    }

    public boolean equals(Object key) {
        boolean result = true;
        if (!(key instanceof SchedulerOrderStepHistoryCompoundKey)) {
            return false;
        }
        Long otherHistoryId = ((SchedulerOrderStepHistoryCompoundKey) key).getHistoryId();
        Long otherStep = ((SchedulerOrderStepHistoryCompoundKey) key).getStep();
        if (step == null || otherStep == null) {
            result = false;
        } else {
            result = step.equals(otherStep);
        }
        if (historyId == null || otherHistoryId == null) {
            result = false;
        } else {
            result = historyId.equals(otherHistoryId);
        }
        return result;
    }

    public int hashCode() {
        int code = 0;
        if (step != null) {
            code += step;
        }
        if (historyId != null) {
            code += historyId;
        }
        return code;
    }
}