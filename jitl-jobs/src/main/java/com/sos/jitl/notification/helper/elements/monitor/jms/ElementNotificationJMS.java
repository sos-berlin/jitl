package com.sos.jitl.notification.helper.elements.monitor.jms;

import javax.jms.DeliveryMode;
import javax.jms.Session;

import org.apache.activemq.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.helper.elements.monitor.AElementNotificationMonitor;
import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;
import com.sos.jitl.notification.plugins.notifier.SystemNotifierSendJMSPlugin;

import sos.util.SOSDate;
import sos.util.SOSString;

public class ElementNotificationJMS extends AElementNotificationMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElementNotificationJMS.class);

    public static int DEFAULT_ACKNOWLEDGE_MODE = Session.CLIENT_ACKNOWLEDGE;
    public static int DEFAULT_PRIOPITY = Message.DEFAULT_PRIORITY;
    public static int DEFAULT_DELIVERY_MODE = Message.DEFAULT_DELIVERY_MODE;
    public static long DEFAULT_TIME_TO_LIVE = Message.DEFAULT_TIME_TO_LIVE;
    public static String DEFAULT_DESTINATION = "Queue";

    public static String ELEMENT_NAME_CONNECTION_FACTORY = "ConnectionFactory";
    public static String ELEMENT_NAME_CONNECTION_JNDI = "ConnectionJNDI";
    public static String ELEMENT_NAME_MESSAGE = "Message";

    public static String ATTRIBUTE_NAME_CLIENT_ID = "client_id";
    public static String ATTRIBUTE_NAME_DESTINATION = "destination";
    public static String ATTRIBUTE_NAME_ACKNOWLEDGE_MODE = "acknowledge_mode";
    public static String ATTRIBUTE_NAME_DELIVERY_MODE = "delivery_mode";
    public static String ATTRIBUTE_NAME_PRIORITY = "priority";
    public static String ATTRIBUTE_NAME_TIME_TO_LIVE = "time_to_live";

    private ElementNotificationJMSConnectionFactory connectionFactory;
    private ElementNotificationJMSJNDI connectionJndi;

    private String clientId;
    private String destination;
    private boolean isQueueDestination;
    private int acknowledgeMode;
    private int priority;
    private int deliveryMode;
    private long timeToLive;
    private String message;

    public ElementNotificationJMS(Node node) throws Exception {
        super(node);

        Node cf = NotificationXmlHelper.getChildNode(getXmlElement(), ELEMENT_NAME_CONNECTION_FACTORY);
        if (cf != null) {
            connectionFactory = new ElementNotificationJMSConnectionFactory(cf);
        }
        Node cj = NotificationXmlHelper.getChildNode(getXmlElement(), ELEMENT_NAME_CONNECTION_JNDI);
        if (cj != null) {
            connectionJndi = new ElementNotificationJMSJNDI(cj);
        }

        clientId = getValue(getXmlElement().getAttribute(ATTRIBUTE_NAME_CLIENT_ID));
        destination = getValue(getXmlElement().getAttribute(ATTRIBUTE_NAME_DESTINATION), DEFAULT_DESTINATION);
        isQueueDestination = destination.toLowerCase().equals(DEFAULT_DESTINATION.toLowerCase());
        acknowledgeMode = getAcknowledgeMode(getXmlElement().getAttribute(ATTRIBUTE_NAME_ACKNOWLEDGE_MODE));
        priority = getValue(getXmlElement().getAttribute(ATTRIBUTE_NAME_PRIORITY), DEFAULT_PRIOPITY);
        deliveryMode = getDeliveryMode(getXmlElement().getAttribute(ATTRIBUTE_NAME_DELIVERY_MODE));
        timeToLive = getTimeToLive(getXmlElement().getAttribute(ATTRIBUTE_NAME_TIME_TO_LIVE));
        message = getValue(NotificationXmlHelper.getChildNodeValue(getXmlElement(), ELEMENT_NAME_MESSAGE));
    }

    private long getTimeToLive(String val) {
        try {
            return SOSDate.resolveAge("ms", val);
        } catch (Exception ex) {
            LOGGER.warn(ex.toString(), ex);
            return DEFAULT_TIME_TO_LIVE;
        }
    }

    private int getAcknowledgeMode(String mode) {
        if (!SOSString.isEmpty(mode)) {
            switch (mode.trim().toUpperCase()) {
            case "SESSION.CLIENT_ACKNOWLEDGE":
                return Session.CLIENT_ACKNOWLEDGE;
            case "SESSION.AUTO_ACKNOWLEDGE":
                return Session.AUTO_ACKNOWLEDGE;
            case "SESSION.DUPS_OK_ACKNOWLEDGE":
                return Session.DUPS_OK_ACKNOWLEDGE;
            }
        }
        return DEFAULT_ACKNOWLEDGE_MODE;
    }

    private int getDeliveryMode(String mode) {
        if (!SOSString.isEmpty(mode)) {
            switch (mode.trim().toUpperCase()) {
            case "DELIVERYMODE.PERSISTENT":
                return DeliveryMode.PERSISTENT;
            case "DELIVERYMODE.NON_PERSISTENT":
                return DeliveryMode.NON_PERSISTENT;
            }
        }
        return DEFAULT_DELIVERY_MODE;
    }

    @Override
    public ISystemNotifierPlugin getOrCreatePluginObject() throws Exception {
        if (SOSString.isEmpty(getPlugin())) {
            return new SystemNotifierSendJMSPlugin();
        } else {
            return initializePlugin(getPlugin());
        }
    }

    public ElementNotificationJMSConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public ElementNotificationJMSJNDI getConnectionJNDI() {
        return connectionJndi;
    }

    public String getClientId() {
        return clientId;
    }

    public String getDestination() {
        return destination;
    }

    public boolean isQueueDestination() {
        return isQueueDestination;
    }

    public int getAcknowledgeMode() {
        return acknowledgeMode;
    }

    public int getPriority() {
        return priority;
    }

    public int getDeliveryMode() {
        return deliveryMode;
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public String getMessage() {
        return message;
    }
}
