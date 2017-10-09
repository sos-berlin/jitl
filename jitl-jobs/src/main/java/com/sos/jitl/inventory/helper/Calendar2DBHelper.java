package com.sos.jitl.inventory.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.dailyplan.db.Calendar2DB;
import com.sos.jitl.dailyplan.job.CreateDailyPlanOptions;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.reporting.db.DBItemInventoryCalendarUsage;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryJob;
import com.sos.jitl.reporting.db.DBItemInventoryOrder;
import com.sos.jitl.reporting.db.DBItemInventorySchedule;


public class Calendar2DBHelper {

    private static final String WEBSERVICE_COMMAND_URL = "/jobscheduler/master/api/command";

    public static Calendar2DB initCalendar2Db (DBLayerInventory dbLayer, DBItemInventoryInstance instance, String httpHost
            , Integer httpPort) throws Exception {
        Calendar2DB calendar2Db = new Calendar2DB(dbLayer.getSession(), instance.getSchedulerId());
        HashMap<String, String> createDaysScheduleOptionsMap = new HashMap<String, String>();
        StringBuilder strb = new StringBuilder();
        strb.append("http://").append(httpHost).append(":").append(httpPort).append(WEBSERVICE_COMMAND_URL);
        String commandUrl = strb.toString();
        createDaysScheduleOptionsMap.put("command_url", commandUrl);
        CreateDailyPlanOptions options = new CreateDailyPlanOptions();
        options.setAllOptions(createDaysScheduleOptionsMap);
        calendar2Db.setOptions(options);
        calendar2Db.setSpooler(null);
        return calendar2Db;
    }

    public static Set<DBItemInventoryCalendarUsage> createCalendarUsage(DbItem item, Set<String> calendarIds){
        Set<DBItemInventoryCalendarUsage> usages = new HashSet<DBItemInventoryCalendarUsage>();
        DBItemInventoryCalendarUsage calendarUsage = new DBItemInventoryCalendarUsage();
        for(String id : calendarIds) {
            Long calendarId = Long.parseLong(id);
            calendarUsage.setCalendarId(calendarId);
            if(item instanceof DBItemInventoryJob) {
                calendarUsage.setInstanceId(((DBItemInventoryJob) item).getInstanceId());
                calendarUsage.setObjectType(ObjectType.JOB.toString());
                calendarUsage.setPath(((DBItemInventoryJob) item).getName());
                usages.add(calendarUsage);
            } else if (item instanceof DBItemInventoryOrder) {
                calendarUsage.setInstanceId(((DBItemInventoryOrder) item).getInstanceId());
                calendarUsage.setObjectType(ObjectType.ORDER.toString());
                calendarUsage.setPath(((DBItemInventoryOrder) item).getName());
                usages.add(calendarUsage);
            } else if (item instanceof DBItemInventorySchedule) {
                calendarUsage.setInstanceId(((DBItemInventorySchedule) item).getInstanceId());
                calendarUsage.setObjectType(ObjectType.SCHEDULE.toString());
                calendarUsage.setPath(((DBItemInventorySchedule) item).getName());
                usages.add(calendarUsage);
            }
        }
        return usages;
    }
    
}
