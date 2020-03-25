package com.sos.jitl.notification.helper;

import java.util.HashMap;

import com.sos.JSHelper.Options.JSMailOptions;
import com.sos.jitl.notification.helper.settings.MailSettings;

import sos.net.SOSMail;
import sos.util.SOSString;

public class NotificationMail {

    public class MailServerKeyName {

        public static final String SCHEDULER_INI_PATH = "scheduler_ini_path";
        public static final String SMTP_HOST = "mail_smtp_host";
        public static final String QUEUE_DIR = "mail_queue_dir";

        public static final String SMTP_SECTION = "smtp";
        public static final String SMTP_USER = "mail.smtp.user";
        public static final String SMTP_PASSWORD = "mail.smtp.password";
        public static final String SMTP_PORT = "mail.smtp.port";
        public static final String SMTP_CONNECTION_TIMEOUT = "mail.smtp.connectiontimeout";
        public static final String SMTP_TIMEOUT = "mail.smtp.timeout";
        public static final String SMTP_SECURITY_PROTOCOL = "mail.smtp.security_protocol";
    }

    public class MailHeaderKeyName {

        public static final String CONTENT_TYPE = "mail.content_type";
        public static final String ENCODING = "mail.encoding";

        public static final String FROM = "mail.from";
        public static final String TO = "mail.to";
        public static final String CC = "mail.cc";
        public static final String BCC = "mail.bcc";
    }

    public class Joc {

        public static final String CONFIG_DIR = "private";
        public static final String CONFIG_FILE = "private.conf";
        public static final String CONFIG_ENTRY = "joc.url";
        public static final String URL_PART_JOB_CHAIN = "/joc/#!/job_chain?path=";
        public static final String URL_PART_ORDER = "/joc/#!/order?path=";
        public static final String URL_PART_JOB = "/joc/#!/job?path=";
    }

    public static JSMailOptions getSchedulerMailOptions(MailSettings mailSettings) {
        JSMailOptions mo = new JSMailOptions();
        if (mailSettings != null) {
            HashMap<String, String> ms = new HashMap<String, String>();
            if (!SOSString.isEmpty(mailSettings.getIniPath())) {
                ms.put(MailServerKeyName.SCHEDULER_INI_PATH, mailSettings.getIniPath());
            }
            if (!SOSString.isEmpty(mailSettings.getSmtp())) {
                ms.put(MailServerKeyName.SMTP_HOST, mailSettings.getSmtp());
            }
            if (!SOSString.isEmpty(mailSettings.getQueueDir())) {
                ms.put(MailServerKeyName.QUEUE_DIR, mailSettings.getQueueDir());
            }
            if (!SOSString.isEmpty(mailSettings.getFrom())) {
                ms.put(MailHeaderKeyName.FROM, mailSettings.getFrom());
            }
            if (!SOSString.isEmpty(mailSettings.getTo())) {
                ms.put(MailHeaderKeyName.TO, mailSettings.getTo());
            }
            if (!SOSString.isEmpty(mailSettings.getCc())) {
                ms.put(MailHeaderKeyName.CC, mailSettings.getCc());
            }
            if (!SOSString.isEmpty(mailSettings.getBcc())) {
                ms.put(MailHeaderKeyName.BCC, mailSettings.getBcc());
            }
            mo.setAllOptions(ms);
        }
        return mo;
    }

    public static SOSMail createMail(JSMailOptions mailOptions, boolean queueMailOnError) throws Exception {

        HashMap<String, String> settings = mailOptions.getSettings();
        SOSMail mail = new SOSMail(settings.get(MailServerKeyName.SMTP_HOST));
        mail.setQueueMailOnError(queueMailOnError);
        if (settings.containsKey(MailServerKeyName.QUEUE_DIR)) {
            mail.setQueueDir(settings.get(MailServerKeyName.QUEUE_DIR));
        }
        // ??? mail.setQueuePraefix();
        if (settings.containsKey(MailServerKeyName.SMTP_PORT)) {
            mail.setPort(settings.get(MailServerKeyName.SMTP_PORT));
        }
        if (settings.containsKey(MailServerKeyName.SMTP_USER)) {
            mail.setUser(settings.get(MailServerKeyName.SMTP_USER));
        }
        if (settings.containsKey(MailServerKeyName.SMTP_PASSWORD)) {
            mail.setPassword(settings.get(MailServerKeyName.SMTP_PASSWORD));
        }
        if (settings.containsKey(MailServerKeyName.SMTP_SECURITY_PROTOCOL)) {
            mail.setSecurityProtocol(settings.get(MailServerKeyName.SMTP_SECURITY_PROTOCOL));
        }
        if (settings.containsKey(MailServerKeyName.SMTP_TIMEOUT)) {
            mail.setTimeout(Integer.parseInt(settings.get(MailServerKeyName.SMTP_TIMEOUT)));
        }
        // if (settings.containsKey(MailServerKeyName.SMTP_CONNECTION_TIMEOUT)) {
        // ???mail.setTimeout(Integer.parseInt(settings.get(MailServerKeyName.SMTP_CONNECTION_TIMEOUT)));
        // }
        if (settings.containsKey(MailHeaderKeyName.CONTENT_TYPE)) {
            mail.setContentType(settings.get(MailHeaderKeyName.CONTENT_TYPE));
        }
        if (settings.containsKey(MailHeaderKeyName.ENCODING)) {
            mail.setEncoding(settings.get(MailHeaderKeyName.ENCODING));
        }
        if (settings.containsKey(MailHeaderKeyName.FROM)) {
            mail.setFrom(settings.get(MailHeaderKeyName.FROM));
        }
        if (settings.containsKey(MailHeaderKeyName.TO)) {
            mail.addRecipient(settings.get(MailHeaderKeyName.TO));
        }
        if (settings.containsKey(MailHeaderKeyName.CC)) {
            mail.addCC(settings.get(MailHeaderKeyName.CC));
        }
        if (settings.containsKey(MailHeaderKeyName.BCC)) {
            mail.addBCC(settings.get(MailHeaderKeyName.BCC));
        }
        return mail;
    }
}
