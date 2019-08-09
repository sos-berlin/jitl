package com.sos.jitl.notification.helper.elements.objects;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.helper.elements.monitor.ElementNotificationMonitor;

import sos.util.SOSString;

public class ElementJob {

    private Node xml;
    private ElementNotificationMonitor monitor;
    private String schedulerId;
    private String name;
    private Long notifications;
    private String returnCodeFrom;
    private String returnCodeTo;

    public ElementJob(ElementNotificationMonitor element, Node node) {
        monitor = element;
        xml = node;
        Element el = (Element) xml;
        schedulerId = getValue(NotificationXmlHelper.getSchedulerId(el));
        name = getValue(NotificationXmlHelper.getJobName(el));
        notifications = getLongValue(NotificationXmlHelper.getNotifications(el));
        returnCodeFrom = getValue(NotificationXmlHelper.getReturnCodeFrom(el));
        returnCodeTo = getValue(NotificationXmlHelper.getReturnCodeTo(el));
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

    public String getName() {
        return name;
    }

    public Long getNotifications() {
        return notifications;
    }

    public String getReturnCodeFrom() {
        return returnCodeFrom;
    }

    public String getReturnCodeTo() {
        return returnCodeTo;
    }

}