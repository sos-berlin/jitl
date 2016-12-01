package com.sos.jitl.schedulerhistory.classes;

/** @author Uwe Risse */
public class HistorySeverity {

    private String strValue = "";
    private Integer intValue = 0;

    public HistorySeverity(String strValue) {
        super();
        this.setStrValue(strValue);
    }

    public HistorySeverity(Integer intValue) {
        super();
        this.setIntValue(intValue);
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
        if ("success".equalsIgnoreCase(strValue)) {
            this.intValue = 0;
        } else {
            this.intValue = 1;
        }
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
        if (intValue == 0) {
            strValue = "success";
        } else {
            strValue = "error";
        }
    }

    public boolean hasValue() {
        return !"".equals(this.getStrValue());
    }

}
