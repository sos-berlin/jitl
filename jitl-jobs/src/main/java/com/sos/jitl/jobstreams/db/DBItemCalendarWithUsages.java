package com.sos.jitl.jobstreams.db;

import com.sos.jitl.reporting.db.DBItemInventoryClusterCalendar;
import com.sos.jitl.reporting.db.DBItemInventoryClusterCalendarUsage;

public class DBItemCalendarWithUsages {

    private DBItemInventoryClusterCalendar dbItemInventoryClusterCalendar;
    private DBItemInventoryClusterCalendarUsage dbItemInventoryClusterCalendarUsage;

    public DBItemCalendarWithUsages(DBItemInventoryClusterCalendar dbItemInventoryClusterCalendar,
            DBItemInventoryClusterCalendarUsage dbItemInventoryClusterCalendarUsage) {
        this.dbItemInventoryClusterCalendar = dbItemInventoryClusterCalendar;
        this.dbItemInventoryClusterCalendarUsage = dbItemInventoryClusterCalendarUsage;
    }

    public DBItemInventoryClusterCalendar getDBItemInventoryClusterCalendar() {
        return dbItemInventoryClusterCalendar;
    }

    public void setDBItemInventoryClusterCalendar(DBItemInventoryClusterCalendar dbItemInCondition) {
        this.dbItemInventoryClusterCalendar = dbItemInCondition;
    }

    public DBItemInventoryClusterCalendarUsage getDBItemInventoryClusterCalendarUsage() {
        return dbItemInventoryClusterCalendarUsage;
    }

    public void setDBItemInventoryClusterCalendarUsage(DBItemInventoryClusterCalendarUsage dbItemInConditionCommand) {
        this.dbItemInventoryClusterCalendarUsage = dbItemInConditionCommand;
    }

}