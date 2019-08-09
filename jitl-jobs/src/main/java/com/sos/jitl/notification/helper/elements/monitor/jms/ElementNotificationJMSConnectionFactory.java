package com.sos.jitl.notification.helper.elements.monitor.jms;

import java.util.LinkedHashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.helper.elements.AElement;

import sos.util.SOSString;

public class ElementNotificationJMSConnectionFactory extends AElement {

    public static String DEFAULT_CONNECTION_FACTORY = "org.apache.activemq.ActiveMQConnectionFactory";

    public static String ELEMENT_NAME_CONSTRUCTOR_ARGUMENTS = "ConstructorArguments";
    public static String ELEMENT_NAME_ARGUMENT = "Argument";

    public static String ATTRIBUTE_NAME_JAVA_CLASS = "java_class";
    public static String ATTRIBUTE_NAME_USER_NAME = "user_name";
    public static String ATTRIBUTE_NAME_PASSWORD = "password";
    public static String ATTRIBUTE_NAME_TYPE = "type";

    private String javaClass;
    private String userName;
    private String password;
    private LinkedHashMap<String, String> constructorArguments;

    public ElementNotificationJMSConnectionFactory(Node node) throws Exception {
        super(node);

        javaClass = getValue(getXmlElement().getAttribute(ATTRIBUTE_NAME_JAVA_CLASS), DEFAULT_CONNECTION_FACTORY);
        userName = getValue(getXmlElement().getAttribute(ATTRIBUTE_NAME_USER_NAME));
        password = getValue(getXmlElement().getAttribute(ATTRIBUTE_NAME_PASSWORD));

        setConstructorArguments();
    }

    private void setConstructorArguments() throws Exception {
        constructorArguments = new LinkedHashMap<>();

        NodeList nl = null;
        NodeList nlca = getXmlElement().getElementsByTagName(ELEMENT_NAME_CONSTRUCTOR_ARGUMENTS);
        if (nlca != null && nlca.getLength() > 0) {
            nl = ((Element) nlca.item(0)).getElementsByTagName(ELEMENT_NAME_ARGUMENT);
        }
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element ca = (Element) nl.item(i);
                String type = ca.getAttribute(ATTRIBUTE_NAME_TYPE);
                if (!SOSString.isEmpty(type)) {
                    String value = NotificationXmlHelper.getValue(ca);
                    if (!SOSString.isEmpty(value)) {
                        constructorArguments.put(type, value);
                    }
                }
            }
        }
    }

    public String getJavaClass() {
        return javaClass;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public LinkedHashMap<String, String> getConstructorArguments() {
        return constructorArguments;
    }
}
