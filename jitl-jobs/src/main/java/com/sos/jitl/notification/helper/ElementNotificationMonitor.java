package com.sos.jitl.notification.helper;

import org.w3c.dom.Node;

import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;

public class ElementNotificationMonitor extends AElementNotificationMonitor {

    private String serviceNameOnError;
    private String serviceNameOnSuccess;
    private String serviceStatusOnError;
    private String serviceStatusOnSuccess;
    private ElementNotificationMonitorInterface monitorInterface;
    private ElementNotificationMonitorCommand monitorCommand;
    private ISystemNotifierPlugin pluginObject;
    private SystemNotifierJobOptions options;

    public ElementNotificationMonitor(Node node, SystemNotifierJobOptions opt) throws Exception {
        super(node);

        options = opt;

        serviceNameOnError = NotificationXmlHelper.getServiceNameOnError(getXmlElement());
        serviceNameOnSuccess = NotificationXmlHelper.getServiceNameOnSuccess(getXmlElement());
        serviceStatusOnError = NotificationXmlHelper.getServiceStatusOnError(getXmlElement());
        serviceStatusOnSuccess = NotificationXmlHelper.getServiceStatusOnSuccess(getXmlElement());

        Node notificationInterface = NotificationXmlHelper.selectNotificationInterface(getXmlElement());
        if (notificationInterface != null) {
            monitorInterface = new ElementNotificationMonitorInterface(notificationInterface);
        }
        Node notificationCommand = NotificationXmlHelper.selectNotificationCommand(getXmlElement());
        if (notificationCommand != null) {
            monitorCommand = new ElementNotificationMonitorCommand(notificationCommand);
        }
    }

    @Override
    public ISystemNotifierPlugin getPluginObject() throws Exception {
        if (pluginObject == null) {
            if (monitorCommand != null) {
                pluginObject = monitorCommand.getPluginObject();
            } else if (monitorInterface != null) {
                pluginObject = monitorInterface.getPluginObject();
            }
            if (pluginObject != null) {
                pluginObject.init(this, options);
            }
        }
        if (pluginObject == null) {
            throw new Exception("pluginObject is NULL");
        }
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

    public ElementNotificationMonitorInterface getMonitorInterface() {
        return monitorInterface;
    }

    public ElementNotificationMonitorCommand getMonitorCommand() {
        return monitorCommand;
    }
}
