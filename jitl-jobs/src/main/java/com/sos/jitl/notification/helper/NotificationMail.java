package com.sos.jitl.notification.helper;

import java.util.HashMap;

import com.sos.JSHelper.Options.JSMailOptions;

import sos.net.SOSMail;
import sos.spooler.Mail;
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
        public static final String URL_PART_JOB = "/joc/#!/job?path=";
    }

    public static JSMailOptions getSchedulerMailOptions(sos.spooler.Spooler spooler, sos.spooler.Log spooler_log) {
        Mail mail = spooler_log.mail();
        String iniPath = spooler.ini_path();
        String smtp = mail.smtp();
        String queueDir = mail.queue_dir();
        String from = mail.from();
        String to = mail.to();
        String cc = mail.cc();
        String bcc = mail.bcc();

        JSMailOptions mo = new JSMailOptions();
        HashMap<String, String> ms = new HashMap<String, String>();
        if (!SOSString.isEmpty(iniPath)) {
            ms.put(MailServerKeyName.SCHEDULER_INI_PATH, iniPath);
        }
        if (!SOSString.isEmpty(smtp)) {
            ms.put(MailServerKeyName.SMTP_HOST, smtp);
        }
        if (!SOSString.isEmpty(queueDir)) {
            ms.put(MailServerKeyName.QUEUE_DIR, queueDir);
        }
        if (!SOSString.isEmpty(from)) {
            ms.put(MailHeaderKeyName.FROM, from);
        }
        if (!SOSString.isEmpty(to)) {
            ms.put(MailHeaderKeyName.TO, to);
        }
        if (!SOSString.isEmpty(cc)) {
            ms.put(MailHeaderKeyName.CC, cc);
        }
        if (!SOSString.isEmpty(bcc)) {
            ms.put(MailHeaderKeyName.BCC, bcc);
        }
        mo.setAllOptions(ms);
        return mo;
    }

    public static SOSMail createMail(JSMailOptions mailOptions, boolean queueMailOnError) throws Exception {

        HashMap<String, String> settings = mailOptions.settings();
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
