package com.sos.jitl.notification.helper.elements.monitor.mail;

import org.w3c.dom.Node;

import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.helper.elements.monitor.AElementNotificationMonitor;
import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;
import com.sos.jitl.notification.plugins.notifier.SystemNotifierSendMailPlugin;

import sos.util.SOSString;

public class ElementNotificationMail extends AElementNotificationMonitor {

    public static String ELEMENT_NAME_FROM = "From";
    public static String ELEMENT_NAME_TO = "To";
    public static String ELEMENT_NAME_CC = "CC";
    public static String ELEMENT_NAME_BCC = "BCC";
    public static String ELEMENT_NAME_SUBJECT = "Subject";
    public static String ELEMENT_NAME_BODY = "Body";

    public static String ATTRIBUTE_NAME_CONTENT_TYPE = "content_type";
    public static String ATTRIBUTE_NAME_CHARSET = "charset";
    public static String ATTRIBUTE_NAME_ENCODING = "encoding";
    public static String ATTRIBUTE_NAME_PRIORITY = "priority";

    private String contentType;
    private String charset;
    private String encoding;
    private String priority;

    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String body;

    public ElementNotificationMail(Node node) {
        super(node);

        contentType = getValue(getXmlElement().getAttribute(ATTRIBUTE_NAME_CONTENT_TYPE));
        charset = getValue(getXmlElement().getAttribute(ATTRIBUTE_NAME_CHARSET));
        encoding = getValue(getXmlElement().getAttribute(ATTRIBUTE_NAME_ENCODING));
        priority = getValue(getXmlElement().getAttribute(ATTRIBUTE_NAME_PRIORITY));

        from = getValue(NotificationXmlHelper.getChildNodeValue(getXmlElement(), ELEMENT_NAME_FROM));
        to = getValue(NotificationXmlHelper.getChildNodeValue(getXmlElement(), ELEMENT_NAME_TO));
        cc = getValue(NotificationXmlHelper.getChildNodeValue(getXmlElement(), ELEMENT_NAME_CC));
        bcc = getValue(NotificationXmlHelper.getChildNodeValue(getXmlElement(), ELEMENT_NAME_BCC));
        subject = getValue(NotificationXmlHelper.getChildNodeValue(getXmlElement(), ELEMENT_NAME_SUBJECT));
        body = getValue(NotificationXmlHelper.getChildNodeValue(getXmlElement(), ELEMENT_NAME_BODY));

    }

    @Override
    public ISystemNotifierPlugin getOrCreatePluginObject() throws Exception {
        if (SOSString.isEmpty(getPlugin())) {
            return new SystemNotifierSendMailPlugin();
        } else {
            return initializePlugin(getPlugin());
        }
    }

    public String getContentType() {
        return contentType;
    }

    public String getCharset() {
        return charset;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getPriority() {
        return priority;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getCC() {
        return cc;
    }

    public String getBCC() {
        return bcc;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}
