package com.sos.jitl.inventory.helper;

import java.util.HashMap;

import com.sos.jitl.dailyplan.db.Calendar2DB;
import com.sos.jitl.dailyplan.job.CreateDailyPlanOptions;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;


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

}
