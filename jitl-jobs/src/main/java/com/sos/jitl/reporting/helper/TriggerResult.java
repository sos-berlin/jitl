package com.sos.jitl.reporting.helper;

public class TriggerResult {

    private String startCause;
    private Long steps;
    private boolean error;
    private String errorCode;
    private String errorText;

    public String getStartCause() {
        return startCause;
    }

    public void setStartCause(String val) {
        this.startCause = val;
    }

    public Long getSteps() {
        return steps;
    }

    public void setSteps(Long val) {
        this.steps = val;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean val) {
        this.error = val;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String val) {
        this.errorCode = val;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String val) {
        this.errorText = val;
    }

}
