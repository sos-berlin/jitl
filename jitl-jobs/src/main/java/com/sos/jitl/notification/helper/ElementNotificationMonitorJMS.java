package com.sos.jitl.notification.helper;

import javax.jms.DeliveryMode;
import javax.jms.Session;

import org.apache.activemq.Message;
import org.w3c.dom.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;
import com.sos.jitl.notification.plugins.notifier.SystemNotifierSendJMSPlugin;

import sos.util.SOSString;

public class ElementNotificationMonitorJMS extends AElementNotificationMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElementNotificationMonitorJMS.class);

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

    private ElementNotificationMonitorJMSConnectionFactory connectionFactory;
    private ElementNotificationMonitorJMSJNDI connectionJndi;

    private String clientId;
    private String destination;
    private boolean isQueueDestination;
    private int acknowledgeMode;
    private int priority;
    private int deliveryMode;
    private long timeToLive;
    private String message;

    public ElementNotificationMonitorJMS(Node node) throws Exception {
        super(node);

        Node cf = NotificationXmlHelper.getChildNode(getXmlElement(), ELEMENT_NAME_CONNECTION_FACTORY);
        if (cf != null) {
            connectionFactory = new ElementNotificationMonitorJMSConnectionFactory(cf);
        }
        Node cj = NotificationXmlHelper.getChildNode(getXmlElement(), ELEMENT_NAME_CONNECTION_JNDI);
        if (cj != null) {
            connectionJndi = new ElementNotificationMonitorJMSJNDI(cj);
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
        if (!SOSString.isEmpty(val)) {
            val = val.trim();
            Long mills = new Long(0);
            String[] arr = val.split(" ");
            for (String s : arr) {
                s = s.trim().toLowerCase();
                if (!SOSString.isEmpty(s)) {
                    String sub = s;
                    try {
                        if (s.endsWith("w")) {
                            sub = s.substring(0, s.length() - 1);
                            mills += 1_000 * 60 * 60 * 24 * 7 * Long.parseLong(sub);
                        } else if (s.endsWith("d")) {
                            sub = s.substring(0, s.length() - 1);
                            mills += 1_000 * 60 * 60 * 24 * Long.parseLong(sub);
                        } else if (s.endsWith("h")) {
                            sub = s.substring(0, s.length() - 1);
                            mills += 1_000 * 60 * 60 * Long.parseLong(sub);
                        } else if (s.endsWith("m")) {
                            sub = s.substring(0, s.length() - 1);
                            mills += 1_000 * 60 * Long.parseLong(sub);
                        } else if (s.endsWith("s")) {
                            sub = s.substring(0, s.length() - 1);
                            mills += 1_000 * Long.parseLong(sub);
                        } else {
                            mills += Long.parseLong(sub);
                        }
                    } catch (Exception ex) {
                        LOGGER.warn(String.format("invalid integer value = %s (%s) : %s", sub, s, ex.toString()));
                        return DEFAULT_TIME_TO_LIVE;
                    }
                }
            }
            return mills;

        }
        return DEFAULT_TIME_TO_LIVE;
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

    public ElementNotificationMonitorJMSConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public ElementNotificationMonitorJMSJNDI getConnectionJNDI() {
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
