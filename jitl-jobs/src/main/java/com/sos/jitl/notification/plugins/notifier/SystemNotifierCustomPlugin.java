package com.sos.jitl.notification.plugins.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.exceptions.SOSSystemNotifierSendException;
import com.sos.jitl.notification.helper.EServiceMessagePrefix;
import com.sos.jitl.notification.helper.EServiceStatus;
import com.sos.jitl.notification.helper.ElementNotificationMonitor;
import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;

import sos.spooler.Spooler;

public abstract class SystemNotifierCustomPlugin extends SystemNotifierPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierCustomPlugin.class);

    @Override
    public void init(final ElementNotificationMonitor monitor, final SystemNotifierJobOptions opt) throws Exception {
        super.init(monitor, opt);
        try {
            onInit();
        } catch (Throwable e) {
            setInitError(e.toString());
            throw e;
        }
    }

    @Override
    public int notifySystem(final Spooler spooler, final SystemNotifierJobOptions options, final DBLayerSchedulerMon dbLayer,
            DBItemSchedulerMonNotifications notification, final DBItemSchedulerMonSystemNotifications systemNotification,
            final DBItemSchedulerMonChecks check, final EServiceStatus status, final EServiceMessagePrefix prefix)
            throws SOSSystemNotifierSendException {

        String method = "notifySystem";
        try {
            if (hasErrorOnInit()) {
                LOGGER.warn(String.format("[%s][%s][skip][due init error]%s", method, systemNotification.getServiceName(), getInitError()));
                return 0;
            }
            resetTableFields();
            onNotifySystem(systemNotification, notification, check, status, prefix);
            return 0;
        } catch (Throwable e) {
            throw new SOSSystemNotifierSendException(String.format("[%s]%s", method, e.toString()), e);
        }
    }

    @Override
    public void close() {
        onClose();
    }

    public abstract void onInit() throws Exception;

    public abstract void onClose();

    public abstract void onNotifySystem(final DBItemSchedulerMonSystemNotifications systemNotification,
            final DBItemSchedulerMonNotifications notification, final DBItemSchedulerMonChecks check, final EServiceStatus status,
            final EServiceMessagePrefix prefix) throws Exception;
}
