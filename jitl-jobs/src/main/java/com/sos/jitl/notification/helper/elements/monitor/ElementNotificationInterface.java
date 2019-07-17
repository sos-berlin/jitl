package com.sos.jitl.notification.helper.elements.monitor;

import org.w3c.dom.Node;

import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;
import com.sos.jitl.notification.plugins.notifier.SystemNotifierSendNscaPlugin;

import sos.util.SOSString;

public class ElementNotificationInterface extends AElementNotificationMonitor {

    private String serviceHost;
    private int monitorPort;
    private String monitorHost;
    private String monitorEncryption;
    private String monitorPassword;
    private int monitorConnectionTimeout;
    private int monitorResponseTimeout;
    private String command;

    public ElementNotificationInterface(Node node) {
        super(node);

        serviceHost = NotificationXmlHelper.getServiceHost(getXmlElement());
        monitorHost = NotificationXmlHelper.getMonitorHost(getXmlElement());
        monitorEncryption = NotificationXmlHelper.getMonitorEncryption(getXmlElement());
        monitorPassword = NotificationXmlHelper.getMonitorPassword(getXmlElement());

        monitorPort = -1;
        monitorConnectionTimeout = -1;
        monitorResponseTimeout = -1;

        String mp = NotificationXmlHelper.getMonitorPort(getXmlElement());
        String ct = NotificationXmlHelper.getMonitorConnectionTimeout(getXmlElement());
        String rt = NotificationXmlHelper.getMonitorResponseTimeout(getXmlElement());
        try {
            if (mp != null) {
                monitorPort = Integer.parseInt(mp);
            }
        } catch (Exception ex) {
        }
        try {
            if (ct != null) {
                monitorConnectionTimeout = Integer.parseInt(ct);
            }
        } catch (Exception ex) {
        }
        try {
            if (rt != null) {
                monitorResponseTimeout = Integer.parseInt(rt);
            }
        } catch (Exception ex) {
        }

        String c = NotificationXmlHelper.getValue(getXmlElement());
        if (!SOSString.isEmpty(c)) {
            command = c.trim();
        }
    }

    @Override
    public ISystemNotifierPlugin getOrCreatePluginObject() throws Exception {
        if (SOSString.isEmpty(getPlugin())) {
            return new SystemNotifierSendNscaPlugin();
        } else {
            return initializePlugin(getPlugin());
        }
    }

    public String getServiceHost() {
        return serviceHost;
    }

    public String getMonitorPassword() {
        return monitorPassword;
    }

    public int getMonitorConnectionTimeout() {
        return monitorConnectionTimeout;
    }

    public int getMonitorResponseTimeout() {
        return monitorResponseTimeout;
    }

    public int getMonitorPort() {
        return monitorPort;
    }

    public String getMonitorHost() {
        return monitorHost;
    }

    public String getMonitorEncryption() {
        return monitorEncryption;
    }

    public String getCommand() {
        return command;
    }

}
