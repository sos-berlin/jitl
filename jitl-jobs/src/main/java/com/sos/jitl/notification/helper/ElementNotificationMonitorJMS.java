package com.sos.jitl.notification.helper;

import org.w3c.dom.Node;

import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;
import com.sos.jitl.notification.plugins.notifier.SystemNotifierSendJMSApacheMQPlugin;

import sos.util.SOSString;

public class ElementNotificationMonitorJMS extends AElementNotificationMonitor {

    private String uri;
    private String message;
    private String plugin;

    public ElementNotificationMonitorJMS(Node node) {
        super(node);

        uri = NotificationXmlHelper.getJMSUri(getXmlElement());

        String msg = NotificationXmlHelper.getValue(getXmlElement());
        if (!SOSString.isEmpty(msg)) {
            message = msg.trim();
        }

        String p = NotificationXmlHelper.getPlugin(getXmlElement());
        if (!SOSString.isEmpty(p)) {
            plugin = p.trim();
        }
    }

    @Override
    public ISystemNotifierPlugin getOrCreatePluginObject() throws Exception {
        if (SOSString.isEmpty(plugin)) {
            return new SystemNotifierSendJMSApacheMQPlugin();
        } else {
            return initializePlugin(plugin);
        }
    }

    public String getUri() {
        return uri;
    }

    public String getMessage() {
        return message;
    }
}
