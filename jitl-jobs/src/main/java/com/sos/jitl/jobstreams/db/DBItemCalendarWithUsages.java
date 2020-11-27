package com.sos.jitl.jobstreams.db;

public class DBItemCalendarWithUsages {

    private String name;
    private String path;
    private String restrictionConfiguration;
    private String calendarConfiguration;

    public DBItemCalendarWithUsages() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRestrictionConfiguration() {
        return restrictionConfiguration;
    }

    public void setRestrictionConfiguration(String restrictionConfiguration) {
        this.restrictionConfiguration = restrictionConfiguration;
    }

    public String getCalendarConfiguration() {
        return calendarConfiguration;
    }

    public void setCalendarConfiguration(String calendarConfiguration) {
        this.calendarConfiguration = calendarConfiguration;
    }

}