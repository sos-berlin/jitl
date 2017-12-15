package com.sos.jitl.notification.helper;

import org.w3c.dom.Node;

public class ElementNotificationMonitorJMSJNDI extends AElement {

    public static String DEFAULT_LOOKUP_NAME = "ConnectionFactory";

    public static String ATTRIBUTE_NAME_FILE = "file";
    public static String ATTRIBUTE_NAME_LOOKUP_NAME = "lookup_name";

    private String file;
    private String lookupName;

    public ElementNotificationMonitorJMSJNDI(Node node) {
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
