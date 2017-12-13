package com.sos.jitl.notification.helper;

import org.w3c.dom.Node;

import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;
import com.sos.jitl.notification.plugins.notifier.SystemNotifierProcessBuilderPlugin;

import sos.util.SOSString;

public class ElementNotificationMonitorCommand extends AElementNotificationMonitor {

    private String command = null;
    private String plugin;

    public ElementNotificationMonitorCommand(Node node) throws Exception {
        super(node);

        String cmd = NotificationXmlHelper.getValue(getXmlElement());
        if (!SOSString.isEmpty(cmd)) {
            command = cmd.trim();
        } else {
            throw new Exception(String.format("not found value on %s", getXmlElement().getNodeName()));
        }
        String p = NotificationXmlHelper.getPlugin(getXmlElement());
        if (!SOSString.isEmpty(p)) {
            plugin = p.trim();
        }
    }

    @Override
    public ISystemNotifierPlugin getOrCreatePluginObject() throws Exception {
        if (SOSString.isEmpty(plugin)) {
            return new SystemNotifierProcessBuilderPlugin();
        } else {
            return initializePlugin(plugin);
        }
    }

    public String getPlugin() {
        return plugin;
    }

    public String getCommand() {
        return command;
    }

}
