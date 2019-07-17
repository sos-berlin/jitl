package com.sos.jitl.notification.helper.elements.objects;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.helper.elements.monitor.ElementNotificationMonitor;

import sos.util.SOSString;

public class ElementTimerRef {

    private ElementNotificationMonitor monitor;
    private Node xml;

    private String ref;
    private Long notifications;
    private boolean notifyOnError;

    public ElementTimerRef(ElementNotificationMonitor element, Node node) {
        monitor = element;

        xml = node;
        Element el = (Element) xml;
        ref = getValue(NotificationXmlHelper.getTimerRef(el));
        notifications = getLongValue(NotificationXmlHelper.getNotifications(el));
        setNotifyOnError(el);
    }

    private void setNotifyOnError(Element el) {
        notifyOnError = false;
        String noe = NotificationXmlHelper.getTimerNotifyOnError(el);
        try {
            notifyOnError = noe == null ? false : Boolean.parseBoolean(noe);
        } catch (Exception ex) {
        }
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

    public String getRef() {
        return ref;
    }

    public Long getNotifications() {
        return notifications;
    }

    public boolean getNotifyOnError() {
        return notifyOnError;
    }
}
