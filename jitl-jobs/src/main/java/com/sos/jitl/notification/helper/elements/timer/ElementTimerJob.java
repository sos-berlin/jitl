package com.sos.jitl.notification.helper.elements.timer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.NotificationXmlHelper;

import sos.util.SOSString;

public class ElementTimerJob {

    private Node xml;

    private ElementTimer timer;
    private String schedulerId;
    private String name;

    public ElementTimerJob(ElementTimer element, Node node) {
        timer = element;

        xml = node;
        Element el = (Element) xml;
        schedulerId = getValue(NotificationXmlHelper.getSchedulerId(el));
        name = getValue(NotificationXmlHelper.getJobChainName(el));
    }

    private String getValue(String val) {
        return SOSString.isEmpty(val) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : val;
    }

    public ElementTimer getTimer() {
        return timer;
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
}
