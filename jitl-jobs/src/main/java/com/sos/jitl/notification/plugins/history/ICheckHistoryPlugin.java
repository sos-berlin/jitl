package com.sos.jitl.notification.plugins.history;

import java.util.Date;
import java.util.LinkedHashMap;

import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.ElementTimer;
import com.sos.jitl.notification.jobs.history.CheckHistoryJobOptions;

public interface ICheckHistoryPlugin {
	
	public void onInit(LinkedHashMap<String, ElementTimer> timers,CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) throws Exception;
	
	public void onExit(LinkedHashMap<String, ElementTimer> timers,CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) throws Exception;
	
	public void onProcess(LinkedHashMap<String, ElementTimer> timers,CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer, Date dateFrom,Date dateTo) throws Exception;		
}
