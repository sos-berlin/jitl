package com.sos.jitl.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;

public abstract class AElementNotificationMonitor {

    private final Element xmlElement;

    public AElementNotificationMonitor(Node node) {
        xmlElement = (Element) node;
    }

    public abstract ISystemNotifierPlugin getOrCreatePluginObject() throws Exception;

    @SuppressWarnings("unchecked")
    public ISystemNotifierPlugin initializePlugin(String plugin) throws Exception {

        try {
            Class<ISystemNotifierPlugin> c = (Class<ISystemNotifierPlugin>) Class.forName(plugin);
            return c.newInstance();
        } catch (Exception ex) {
            throw new Exception(String.format("plugin cannot be initialized(%s) : %s", plugin, ex.getMessage()));
        }
    }

    public Element getXmlElement() {
        return xmlElement;
    }
}
