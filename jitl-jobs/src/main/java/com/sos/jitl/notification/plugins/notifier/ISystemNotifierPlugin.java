package com.sos.jitl.notification.plugins.notifier;

import sos.spooler.Spooler;

import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.EServiceMessagePrefix;
import com.sos.jitl.notification.helper.EServiceStatus;
import com.sos.jitl.notification.helper.ElementNotificationMonitor;
import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;

public interface ISystemNotifierPlugin {
	
	public void init(ElementNotificationMonitor monitor) throws Exception;		
	
	public int notifySystem(Spooler spooler,
			SystemNotifierJobOptions options, 
			DBLayerSchedulerMon dbLayer, 
			DBItemSchedulerMonNotifications notification,
			DBItemSchedulerMonSystemNotifications systemNotification,
			DBItemSchedulerMonChecks check,
			EServiceStatus status, 
			EServiceMessagePrefix prefix) throws Exception;		
	
	public int notifySystemReset(String serviceName,EServiceStatus status,EServiceMessagePrefix prefix, String message) throws Exception;
}
