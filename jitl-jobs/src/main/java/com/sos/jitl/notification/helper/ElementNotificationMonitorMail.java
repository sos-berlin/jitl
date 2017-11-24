package com.sos.jitl.notification.helper;

import org.w3c.dom.Node;

import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;
import com.sos.jitl.notification.plugins.notifier.SystemNotifierSendMailPlugin;

import sos.util.SOSString;

public class ElementNotificationMonitorMail extends AElementNotificationMonitor {

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
    private String plugin;

    public ElementNotificationMonitorMail(Node node) {
        super(node);

        contentType = NotificationXmlHelper.getMailContentType(getXmlElement());
        charset = NotificationXmlHelper.getMailCharset(getXmlElement());
        encoding = NotificationXmlHelper.getMailEncoding(getXmlElement());
        priority = NotificationXmlHelper.getMailPriority(getXmlElement());

        from = NotificationXmlHelper.getMailFrom(getXmlElement());
        to = NotificationXmlHelper.getMailTo(getXmlElement());
        cc = NotificationXmlHelper.getMailCC(getXmlElement());
        bcc = NotificationXmlHelper.getMailBCC(getXmlElement());
        subject = NotificationXmlHelper.getMailSubject(getXmlElement());
        body = NotificationXmlHelper.getMailBody(getXmlElement());

        String p = NotificationXmlHelper.getPlugin(getXmlElement());
        if (!SOSString.isEmpty(p)) {
            plugin = p.trim();
        }
    }

    @Override
    public ISystemNotifierPlugin getPluginObject() throws Exception {
        if (SOSString.isEmpty(plugin)) {
            return new SystemNotifierSendMailPlugin();
        } else {
            return initializePlugin(plugin);
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

    public String getPlugin() {
        return plugin;
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
