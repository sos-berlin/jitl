package com.sos.jitl.reporting.helper;

public enum EReferenceType {
    TRIGGER(new Long(0)), EXECUTION(new Long(1)), TASK(new Long(2));

    private Long value;

    private EReferenceType(Long val) {
        value = val;
    }

    public Long value() {
        return value;
    }
}
