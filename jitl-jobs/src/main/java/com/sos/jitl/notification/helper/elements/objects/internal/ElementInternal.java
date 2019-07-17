package com.sos.jitl.notification.helper.elements.objects.internal;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.helper.elements.monitor.ElementNotificationMonitor;

import sos.util.SOSString;

public class ElementInternal {

    private Node xml;
    private ElementNotificationMonitor monitor;
    private String schedulerId;
    private Long notifications;

    public ElementInternal(ElementNotificationMonitor element, Node node) {
        monitor = element;
        xml = node;
        Element el = (Element) xml;
        schedulerId = getValue(NotificationXmlHelper.getSchedulerId(el));
        notifications = getLongValue(NotificationXmlHelper.getNotifications(el));
    }

    private String getValue(String val) {
        return SOSString.isEmpty(val) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : val;
    }

    private Long getLongValue(String val) {
        return SOSString.isEmpty(val) ? new Long(1) : new Long(val);
    }

    public ElementNotificationMonitor getMonitor() {
        return monitor;
    }

    public Node getXml() {
        return xml;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public Long getNotifications() {
        return notifications;
    }
}