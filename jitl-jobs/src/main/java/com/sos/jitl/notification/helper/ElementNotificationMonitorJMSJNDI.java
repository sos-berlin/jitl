package com.sos.jitl.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ElementNotificationMonitorJMSJNDI {

    public static String DEFAULT_LOOKUP_NAME = "ConnectionFactory";

    private final Element xmlElement;
    private String file;
    private String lookupName;

    public ElementNotificationMonitorJMSJNDI(Node node) {
        xmlElement = (Element) node;

        file = AElementNotificationMonitor.getValue(NotificationXmlHelper.getJMSJndiFile(xmlElement));
        lookupName = AElementNotificationMonitor.getValue(NotificationXmlHelper.getJMSJndiLookupName(xmlElement), DEFAULT_LOOKUP_NAME);
    }

    public Element getXmlElement() {
        return xmlElement;
    }

    public String getFile() {
        return file;
    }

    public String getLookupName() {
        return lookupName;
    }
}
