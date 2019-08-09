package com.sos.jitl.notification.helper.elements.objects.jobchain;

import org.w3c.dom.Node;

public class ElementNotifyRepeatedErrorByIntervention {

    private Node xml;

    public ElementNotifyRepeatedErrorByIntervention(Node node) {
        xml = node;
    }

    public Node getXml() {
        return xml;
    }

}
