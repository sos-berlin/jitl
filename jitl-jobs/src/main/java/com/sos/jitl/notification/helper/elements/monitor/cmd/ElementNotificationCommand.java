package com.sos.jitl.notification.helper.elements.monitor.cmd;

import org.w3c.dom.Node;

import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.helper.elements.monitor.AElementNotificationMonitor;
import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;
import com.sos.jitl.notification.plugins.notifier.SystemNotifierProcessBuilderPlugin;

import sos.util.SOSString;

public class ElementNotificationCommand extends AElementNotificationMonitor {

    private String command = null;

    public ElementNotificationCommand(Node node) throws Exception {
        super(node);

        String cmd = NotificationXmlHelper.getValue(getXmlElement());
        if (!SOSString.isEmpty(cmd)) {
            command = cmd.trim();
        } else {
            throw new Exception(String.format("not found value on %s", getXmlElement().getNodeName()));
        }
    }

    @Override
    public ISystemNotifierPlugin getOrCreatePluginObject() throws Exception {
        if (SOSString.isEmpty(getPlugin())) {
            return new SystemNotifierProcessBuilderPlugin();
        } else {
            return initializePlugin(getPlugin());
        }
    }

    public String getCommand() {
        return command;
    }

}
