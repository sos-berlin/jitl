package com.sos.jitl.notification.helper.elements.objects.jobchain;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.jitl.notification.helper.NotificationXmlHelper;

public class ElementNotifyRepeatedError {

    public static String ELEMENT_NAME_NOTIFY_REPEATED_ERROR_BY_PERIOD = "NotifyByPeriod";
    public static String ELEMENT_NAME_NOTIFY_REPEATED_ERROR_BY_INTERVENTION = "NotifyByIntervention";

    private Node xml;
    private ElementNotifyRepeatedErrorByPeriod notifyByPeriod;
    private ElementNotifyRepeatedErrorByIntervention notifyByIntervention;

    public ElementNotifyRepeatedError(Node node) throws Exception {
        xml = node;
        Element el = (Element) xml;
        Node n = NotificationXmlHelper.getChildNode(el, ELEMENT_NAME_NOTIFY_REPEATED_ERROR_BY_PERIOD);
        if (n != null) {
            notifyByPeriod = new ElementNotifyRepeatedErrorByPeriod(n);
        }
        n = NotificationXmlHelper.getChildNode(el, ELEMENT_NAME_NOTIFY_REPEATED_ERROR_BY_INTERVENTION);
        if (n != null) {
            notifyByIntervention = new ElementNotifyRepeatedErrorByIntervention(n);
        }
    }

    public Node getXml() {
        return xml;
    }

    public ElementNotifyRepeatedErrorByPeriod getNotifyByPeriod() {
        return notifyByPeriod;
    }

    public ElementNotifyRepeatedErrorByIntervention getNotifyByIntervention() {
        return notifyByIntervention;
    }

    @Override
    public String toString() {
        StringBuilder r = new StringBuilder("[NotifyRepeatedError]");
        if (notifyByIntervention != null) {
            r.append("[NotifyByIntervention]");
        }
        if (notifyByPeriod != null) {
            r.append("[NotifyByPeriod period=").append(notifyByPeriod.getPeriod()).append("]");
        }
        return r.toString();
    }
}
