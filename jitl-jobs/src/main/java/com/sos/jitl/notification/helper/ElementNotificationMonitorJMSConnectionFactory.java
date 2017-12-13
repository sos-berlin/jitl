package com.sos.jitl.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ElementNotificationMonitorJMSConnectionFactory {

    public static String DEFAULT_CONNECTION_FACTORY = "org.apache.activemq.ActiveMQConnectionFactory";

    private final Element xmlElement;
    private String factory;
    private String providerUrl;
    private String userName;
    private String password;

    public ElementNotificationMonitorJMSConnectionFactory(Node node) {
        xmlElement = (Element) node;

        factory = AElementNotificationMonitor.getValue(NotificationXmlHelper.getJMSFactory(xmlElement), DEFAULT_CONNECTION_FACTORY);
        providerUrl = AElementNotificationMonitor.getValue(NotificationXmlHelper.getJMSProviderUrl(xmlElement));
        userName = AElementNotificationMonitor.getValue(NotificationXmlHelper.getJMSUserName(xmlElement));
        password = AElementNotificationMonitor.getValue(NotificationXmlHelper.getJMSPassword(xmlElement));
    }

    public Element getXmlElement() {
        return xmlElement;
    }

    public String getFactory() {
        return factory;
    }

    public String getProviderUrl() {
        return providerUrl;
    }
    
    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
