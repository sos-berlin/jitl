package com.sos.jitl.notification.helper.elements.objects.jobchain;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.NotificationXmlHelper;

import sos.util.SOSString;

public class ElementNotifyRepeatedErrorByPeriod {

    private Node xml;
    private String period;

    public ElementNotifyRepeatedErrorByPeriod(Node node) {
        xml = node;
        Element el = (Element) this.xml;
        period = getValue(NotificationXmlHelper.getPeriod(el));
    }

    private String getValue(String val) {
        return SOSString.isEmpty(val) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : val;
    }

    public Node getXml() {
        return xml;
    }

    public String getPeriod() {
        return period;
    }
}
