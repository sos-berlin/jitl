package com.sos.jitl.notification.helper.elements.monitor.jms;

import org.w3c.dom.Node;

import com.sos.jitl.notification.helper.elements.AElement;

public class ElementNotificationJMSJNDI extends AElement {

    public static String DEFAULT_LOOKUP_NAME = "ConnectionFactory";

    public static String ATTRIBUTE_NAME_FILE = "file";
    public static String ATTRIBUTE_NAME_LOOKUP_NAME = "lookup_name";

    private String file;
    private String lookupName;

    public ElementNotificationJMSJNDI(Node node) {
        super(node);

        file = getValue(getXmlElement().getAttribute(ATTRIBUTE_NAME_FILE));
        lookupName = getValue(getXmlElement().getAttribute(DEFAULT_LOOKUP_NAME), DEFAULT_LOOKUP_NAME);
    }

    public String getFile() {
        return file;
    }

    public String getLookupName() {
        return lookupName;
    }
}
