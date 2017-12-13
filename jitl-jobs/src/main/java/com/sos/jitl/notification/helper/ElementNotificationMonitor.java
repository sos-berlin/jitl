package com.sos.jitl.notification.helper;

import org.w3c.dom.Node;

import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;

public class ElementNotificationMonitor extends AElementNotificationMonitor {

    public static final String NOTIFICATION_INTERFACE = "NotificationInterface";
    public static final String NOTIFICATION_COMMAND = "NotificationCommand";
    public static final String NOTIFICATION_MAIL = "NotificationMail";
    
    private String serviceNameOnError;
    private String serviceNameOnSuccess;
    private String serviceStatusOnError;
    private String serviceStatusOnSuccess;
    private AElementNotificationMonitor monitorInterface;
    private ISystemNotifierPlugin pluginObject;
    private SystemNotifierJobOptions options;

    public ElementNotificationMonitor(Node node, SystemNotifierJobOptions opt) throws Exception {
        super(node);

        options = opt;

        serviceNameOnError = NotificationXmlHelper.getServiceNameOnError(getXmlElement());
        serviceNameOnSuccess = NotificationXmlHelper.getServiceNameOnSuccess(getXmlElement());
        serviceStatusOnError = NotificationXmlHelper.getServiceStatusOnError(getXmlElement());
        serviceStatusOnSuccess = NotificationXmlHelper.getServiceStatusOnSuccess(getXmlElement());

        Node notificationInterface = NotificationXmlHelper.selectNotificationInterface(getXmlElement(), NOTIFICATION_INTERFACE);
        if (notificationInterface != null) {
            monitorInterface = new ElementNotificationMonitorInterface(notificationInterface);
        }
        if (monitorInterface == null) {
            notificationInterface = NotificationXmlHelper.selectNotificationInterface(getXmlElement(), NOTIFICATION_COMMAND);
            if (notificationInterface != null) {
                monitorInterface = new ElementNotificationMonitorCommand(notificationInterface);
            }
        }
        if (monitorInterface == null) {
            notificationInterface = NotificationXmlHelper.selectNotificationInterface(getXmlElement(), NOTIFICATION_MAIL);
            if (notificationInterface != null) {
                monitorInterface = new ElementNotificationMonitorMail(notificationInterface);
            }
        }
    }

    @Override
    public ISystemNotifierPlugin getOrCreatePluginObject() throws Exception {
        if (pluginObject == null) {
            if (monitorInterface != null) {
                pluginObject = monitorInterface.getOrCreatePluginObject();
                if (pluginObject != null) {
                    pluginObject.init(this, options);
                }
            }
        }
        if (pluginObject == null) {
            throw new Exception("pluginObject is NULL");
        }
        return pluginObject;
    }

    public ISystemNotifierPlugin getPluginObject() {
        return pluginObject;
    }

    public String getServiceNameOnSuccess() {
        return serviceNameOnSuccess;
    }

    public void setServiceNameOnSuccess(String val) {
        serviceNameOnSuccess = val;
    }

    public String getServiceNameOnError() {
        return serviceNameOnError;
    }

    public String getServiceStatusOnError() {
        return serviceStatusOnError;
    }

    public String getServiceStatusOnSuccess() {
        return serviceStatusOnSuccess;
    }

    public AElementNotificationMonitor getMonitorInterface() {
        return monitorInterface;
    }

}
