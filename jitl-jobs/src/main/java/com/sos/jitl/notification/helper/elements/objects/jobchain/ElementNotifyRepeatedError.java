package com.sos.jitl.notification.helper.elements.objects.jobchain;

import org.w3c.dom.Node;

public class ElementNotifyRepeatedError {

    private Node xml;

    public ElementNotifyRepeatedError(Node node) {
        xml = node;
    }

    public Node getXml() {
        return xml;
    }

}
