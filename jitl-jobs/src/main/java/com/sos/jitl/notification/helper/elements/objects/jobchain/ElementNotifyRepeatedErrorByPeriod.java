package com.sos.jitl.notification.helper.elements.objects.jobchain;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.NotificationXmlHelper;

import sos.util.SOSDate;
import sos.util.SOSString;

public class ElementNotifyRepeatedErrorByPeriod {

    private Node xml;
    private String period;
    private Long periodAsSeconds;

    public ElementNotifyRepeatedErrorByPeriod(Node node) throws Exception {
        xml = node;
        Element el = (Element) xml;
        period = getValue(NotificationXmlHelper.getPeriod(el));
        periodAsSeconds = SOSDate.resolveAge("s", period);
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

    public Long getPeriodAsSeconds() {
        return periodAsSeconds;
    }
}
