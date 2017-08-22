package com.sos.jitl.inventory.helper;

import java.util.HashMap;

import com.sos.jitl.dailyplan.db.Calendar2DB;
import com.sos.jitl.dailyplan.job.CreateDailyPlanOptions;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;


public class Calendar2DBHelper {

    private static final String WEBSERVICE_COMMAND_URL = "/jobscheduler/master/api/command";

    public static Calendar2DB initCalendar2Db (DBLayerInventory dbLayer, DBItemInventoryInstance instance) throws Exception {
        Calendar2DB calendar2Db = new Calendar2DB(dbLayer.getSession(), instance.getSchedulerId());
        HashMap<String, String> createDaysScheduleOptionsMap = new HashMap<String, String>();
        String commandUrl = instance.getUrl() + WEBSERVICE_COMMAND_URL;
        if (instance.getAuth() != null && !instance.getAuth().isEmpty()) {
            createDaysScheduleOptionsMap.put("basic_authorization", instance.getAuth());
        }
        createDaysScheduleOptionsMap.put("command_url", commandUrl);
        CreateDailyPlanOptions options = new CreateDailyPlanOptions();
        options.setAllOptions(createDaysScheduleOptionsMap);
        calendar2Db.setOptions(options);
        calendar2Db.setSpooler(null);
        return calendar2Db;
    }
}
