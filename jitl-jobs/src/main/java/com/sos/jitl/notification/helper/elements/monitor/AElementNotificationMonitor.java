package com.sos.jitl.notification.helper.elements.monitor;

import org.w3c.dom.Node;

import com.sos.jitl.notification.helper.elements.AElement;
import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;

public abstract class AElementNotificationMonitor extends AElement {

    public static String ATTRIBUTE_NAME_PLUGIN = "plugin";

    private String plugin;

    public AElementNotificationMonitor(Node node) {
        super(node);
        plugin = getValue(getXmlElement().getAttribute(ATTRIBUTE_NAME_PLUGIN));
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

    protected String getPlugin() {
        return plugin;
    }
}
