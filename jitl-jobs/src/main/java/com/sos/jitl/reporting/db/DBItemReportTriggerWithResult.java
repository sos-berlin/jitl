package com.sos.jitl.reporting.db;


public class DBItemReportTriggerWithResult {

  
    private DBItemReportTrigger dbItemReportTrigger;
    private DBItemReportTriggerResult dbItemReportTriggerResult;
    

    public DBItemReportTriggerWithResult(DBItemReportTrigger dbItemReportTrigger, DBItemReportTriggerResult dbItemReportTriggerResult) {
        super();
        this.dbItemReportTrigger = dbItemReportTrigger;
        this.dbItemReportTriggerResult = dbItemReportTriggerResult;
    }

 
    public DBItemReportTriggerResult getDbItemReportTriggerResult() {
        return dbItemReportTriggerResult;
    }


    public void setDbItemReportTriggerResult(DBItemReportTriggerResult dbItemReportTriggerResult) {
        this.dbItemReportTriggerResult = dbItemReportTriggerResult;
    }


    public DBItemReportTrigger getDbItemReportTrigger() {
        return dbItemReportTrigger;
    }


    public void setDbItemReportTrigger(DBItemReportTrigger dbItemReportTrigger) {
        this.dbItemReportTrigger = dbItemReportTrigger;
    }
 
    public boolean haveError(){
        return (dbItemReportTriggerResult != null && dbItemReportTriggerResult.getError());
    }

}
