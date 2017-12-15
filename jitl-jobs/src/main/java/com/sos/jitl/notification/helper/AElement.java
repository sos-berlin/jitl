package com.sos.jitl.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.util.SOSString;

public abstract class AElement {

    private final Element xmlElement;

    public AElement(Node node) {
        xmlElement = (Element) node;
    }

    public Element getXmlElement() {
        return xmlElement;
    }

    protected static String getValue(String val) {
        if (SOSString.isEmpty(val)) {
            return val;
        }
        return val.trim();
    }

    protected static String getValue(String val, String defaultValue) {
        if (SOSString.isEmpty(val)) {
            return defaultValue;
        }
        return val.trim();
    }

    protected static int getValue(String val, int defaultValue) {
        if (SOSString.isEmpty(val)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    protected static long getValue(String val, long defaultValue) {
        if (SOSString.isEmpty(val)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
