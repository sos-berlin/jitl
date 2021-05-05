package com.sos.jitl.dailyplan.db;


public class DailyPlanSaveOrUpdateRecord {
    private DailyPlanDBItem dailyPlanDBItem;
    private Boolean newRecord;
    
    public DailyPlanSaveOrUpdateRecord(DailyPlanDBItem dailyPlanDBItem, Boolean newRecord) {
        super();
        this.dailyPlanDBItem = dailyPlanDBItem;
        this.newRecord = newRecord;
    }

    public DailyPlanDBItem getDailyPlanDBItem() {
        return dailyPlanDBItem;
    }
    
    public void setDailyPlanDBItem(DailyPlanDBItem dailyPlanDBItem) {
        this.dailyPlanDBItem = dailyPlanDBItem;
    }
    
    public Boolean getNewRecord() {
        return newRecord;
    }
    
    public void setNewRecord(Boolean newRecord) {
        this.newRecord = newRecord;
    }
    
    public String getKeyAsString() {
        return new DailyPlanUniqueKey(dailyPlanDBItem).asString();
    }

}
