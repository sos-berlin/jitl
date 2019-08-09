package com.sos.jitl.notification.plugins.notifier;

import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.exceptions.SOSSystemNotifierSendException;
import com.sos.jitl.notification.helper.EServiceMessagePrefix;
import com.sos.jitl.notification.helper.EServiceStatus;
import com.sos.jitl.notification.helper.elements.monitor.ElementNotificationMonitor;
import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;

import sos.spooler.Spooler;

public interface ISystemNotifierPlugin {

    public void init(ElementNotificationMonitor monitor, SystemNotifierJobOptions opt) throws Exception;

    public int notifySystem(Spooler spooler, SystemNotifierJobOptions options, DBLayerSchedulerMon dbLayer,
            DBItemSchedulerMonNotifications notification, DBItemSchedulerMonSystemNotifications systemNotification, DBItemSchedulerMonChecks check,
            EServiceStatus status, EServiceMessagePrefix prefix) throws SOSSystemNotifierSendException;

    public int notifySystemReset(String serviceName, EServiceStatus status, EServiceMessagePrefix prefix, String message) throws Exception;

    public void close();

    public boolean hasErrorOnInit();

    public String getInitError();
}
