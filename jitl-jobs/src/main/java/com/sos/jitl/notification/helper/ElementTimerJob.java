package com.sos.jitl.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.util.SOSString;

import com.sos.jitl.notification.db.DBLayerSchedulerMon;

public class ElementTimerJob {

    private Node xml;

    private ElementTimer timer;
    private String schedulerId;
    private String name;

    public ElementTimerJob(ElementTimer timer, Node job) {
        this.timer = timer;

        this.xml = job;
        Element el = (Element) this.xml;
        this.schedulerId = this.getValue(NotificationXmlHelper.getSchedulerId(el));
        this.name = this.getValue(NotificationXmlHelper.getJobChainName(el));
    }

    private String getValue(String val) {
        return SOSString.isEmpty(val) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : val;
    }

    public ElementTimer getTimer() {
        return this.timer;
    }

    public Node getXml() {
        return this.xml;
    }

    public String getSchedulerId() {
        return this.schedulerId;
    }

    public String getName() {
        return this.name;
    }
}
