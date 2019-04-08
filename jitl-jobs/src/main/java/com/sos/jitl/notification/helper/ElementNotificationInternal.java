package com.sos.jitl.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.util.SOSString;

import com.sos.jitl.notification.db.DBLayerSchedulerMon;

public class ElementNotificationInternal {

    private Node xml;
    private ElementNotificationMonitor monitor;
    private String schedulerId;
    private Long notifications;

    public ElementNotificationInternal(ElementNotificationMonitor monitor, Node internalTask) {
        this.monitor = monitor;
        this.xml = internalTask;
        Element el = (Element) this.xml;
        this.schedulerId = this.getValue(NotificationXmlHelper.getSchedulerId(el));
        this.notifications = this.getLongValue(NotificationXmlHelper.getNotifications(el));
    }

    private String getValue(String val) {
        return SOSString.isEmpty(val) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : val;
    }

    private Long getLongValue(String val) {
        return SOSString.isEmpty(val) ? new Long(1) : new Long(val);
    }

    public ElementNotificationMonitor getMonitor() {
        return this.monitor;
    }

    public Node getXml() {
        return this.xml;
    }

    public String getSchedulerId() {
        return this.schedulerId;
    }

    public Long getNotifications() {
        return this.notifications;
    }
}