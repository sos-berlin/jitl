package com.sos.jitl.notification.plugins.notifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.JSMailOptions;
import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.jitl.notification.helper.EServiceMessagePrefix;
import com.sos.jitl.notification.helper.EServiceStatus;
import com.sos.jitl.notification.helper.ElementNotificationMonitor;
import com.sos.jitl.notification.helper.ElementNotificationMonitorMail;
import com.sos.jitl.notification.helper.NotificationMail;
import com.sos.jitl.notification.helper.NotificationMail.Joc;
import com.sos.jitl.notification.helper.NotificationMail.MailHeaderKeyName;
import com.sos.jitl.notification.helper.NotificationMail.MailServerKeyName;

import sos.net.SOSMail;
import sos.settings.SOSProfileSettings;
import sos.settings.SOSSettings;
import sos.util.SOSString;

public class SystemNotifierSendMailPlugin extends SystemNotifierCustomPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierSendMailPlugin.class);
    private ElementNotificationMonitorMail config = null;
    private SOSMail mail = null;
    private boolean queueMailOnError = true;

    @Override
    public void onInit() throws Exception {
        config = (ElementNotificationMonitorMail) getNotificationMonitor().getMonitorInterface();
        if (config == null) {
            throw new Exception(String.format("%s: %s element is missing (not configured)", getClass().getSimpleName(),
                    ElementNotificationMonitor.NOTIFICATION_MAIL));
        }

        JSMailOptions mailOptions = getSchedulerMailOptions();
        mail = NotificationMail.createMail(mailOptions, queueMailOnError);
        setMailHeaders(config, mailOptions);
    }

    @Override
    public void onNotifySystem(DBItemSchedulerMonSystemNotifications systemNotification, DBItemSchedulerMonNotifications notification,
            DBItemSchedulerMonChecks check, EServiceStatus status, EServiceMessagePrefix prefix) throws Exception {

        String url = "";
        try {
            url = getOptions().scheduler_mail_settings.value().settings().get(Joc.CONFIG_ENTRY);
        } catch (Exception e) {
            LOGGER.warn(String.format("[%s]exception on get link url: %s", systemNotification.getServiceName(), e.toString()), e);
        }
        String jocHrefJobChain = getJocHref4JobChain(url, notification);
        String jocHrefOrder = getJocHref4Order(url, notification);
        String jocHrefJob = getJocHref4Job(url, notification);

        mail.setSubject(resolveVars(config.getSubject(), systemNotification, notification, check, status, prefix, jocHrefJobChain, jocHrefOrder,
                jocHrefJob));
        mail.setBody(resolveVars(config.getBody(), systemNotification, notification, check, status, prefix, jocHrefJobChain, jocHrefOrder,
                jocHrefJob));

        if (!mail.send()) {
            if (queueMailOnError) {
                // - mail will be stored to the mail queue directory
                // - a warning message will be logged by SOSMail
            } else {
                throw new Exception(String.format("[%s]can't send mail", systemNotification.getServiceName()));
            }
        }

    }

    @Override
    public void onClose() {
    }

    private void setMailHeaders(ElementNotificationMonitorMail config, JSMailOptions mailOptions) throws Exception {
        if (!SOSString.isEmpty(config.getContentType())) {
            mail.setContentType(config.getContentType());
        }
        if (!SOSString.isEmpty(config.getCharset())) {
            mail.setCharset(config.getCharset());
        }
        if (!SOSString.isEmpty(config.getEncoding())) {
            mail.setEncoding(config.getEncoding());
        }
        if (!SOSString.isEmpty(config.getPriority())) {
            setMailPriority(config);
        }
        if (!SOSString.isEmpty(config.getFrom())) {
            mail.setFrom(config.getFrom());
        }

        if (!SOSString.isEmpty(config.getTo())) {
            setMailRecipients(config);
        } else if (!SOSString.isEmpty(config.getCC())) {
            setMailCCBCC(config, mailOptions);
        } else if (!SOSString.isEmpty(config.getBCC())) {
            setMailBCC(config, mailOptions);
        }
    }

    private void setMailPriority(ElementNotificationMonitorMail config) throws MessagingException {
        switch (config.getPriority().toUpperCase()) {
        case "HIGHEST":
            mail.setPriorityHighest();
            break;
        case "HIGH":
            mail.setPriorityHigh();
            break;
        case "LOW":
            mail.setPriorityLow();
            break;
        case "LOWEST":
            mail.setPriorityLowest();
            break;
        }
    }

    private void setMailRecipients(ElementNotificationMonitorMail config) throws Exception {
        mail.clearRecipients();
        mail.addRecipient(config.getTo());
        if (!SOSString.isEmpty(config.getCC())) {
            mail.addCC(config.getCC());
        }
        if (!SOSString.isEmpty(config.getBCC())) {
            mail.addBCC(config.getBCC());
        }
    }

    private void setMailCCBCC(ElementNotificationMonitorMail config, JSMailOptions mailOptions) throws Exception {
        mail.clearRecipients();
        if (mailOptions.settings().containsKey(MailHeaderKeyName.TO)) {
            mail.addRecipient(mailOptions.settings().get(MailHeaderKeyName.TO));
        }
        mail.addCC(config.getCC());
        if (!SOSString.isEmpty(config.getBCC())) {
            mail.addBCC(config.getBCC());
        }
    }

    private void setMailBCC(ElementNotificationMonitorMail config, JSMailOptions mailOptions) throws Exception {
        mail.clearRecipients();
        if (mailOptions.settings().containsKey(MailHeaderKeyName.TO)) {
            mail.addRecipient(mailOptions.settings().get(MailHeaderKeyName.TO));
        }
        if (mailOptions.settings().containsKey(MailHeaderKeyName.CC)) {
            mail.addCC(mailOptions.settings().get(MailHeaderKeyName.CC));
        }
        mail.addBCC(config.getBCC());
    }

    private JSMailOptions getSchedulerMailOptions() throws Exception {
        JSMailOptions mailOptions = getOptions().scheduler_mail_settings.value();
        if (!mailOptions.settings().containsKey(MailServerKeyName.SMTP_PORT)) {
            String host = mailOptions.settings().get(MailServerKeyName.SMTP_HOST);
            if (SOSString.isEmpty(host) || host.toLowerCase().equals("-queue")) {
                throw new Exception(String.format("smtp host not configured to send mails. settings=%s", mailOptions.settings()));
            }
            String ini = mailOptions.settings().get(MailServerKeyName.SCHEDULER_INI_PATH);
            if (SOSString.isEmpty(ini)) {
                throw new Exception(String.format("scheduler factory.ini file not founded. settings=%s", mailOptions.settings()));
            }
            LOGGER.debug(String.format("read %s", ini));
            SOSSettings settings = new SOSProfileSettings(ini);
            Properties smtp = settings.getSection(MailServerKeyName.SMTP_SECTION);
            if (smtp != null) {
                mailOptions.loadProperties(smtp);
            }

            mailOptions.settings().put(Joc.CONFIG_ENTRY, getJocUrl(ini));
        }
        LOGGER.debug(String.format("mailOptions.settings=%s", mailOptions.settings()));
        return mailOptions;
    }

    private String getJocUrl(String factoryIni) {
        String jocUrl = "";
        Path privateConf = Paths.get(factoryIni).getParent().resolve(Joc.CONFIG_DIR).resolve(Joc.CONFIG_FILE);
        try {
            if (privateConf.toFile().exists()) {
                Optional<String> entry = Files.lines(privateConf).map(s -> s.trim()).filter(s -> s.startsWith(Joc.CONFIG_ENTRY)).map(s -> s.split(
                        "=")).map(arr -> arr[arr.length - 1].trim().replaceAll("\"", "")).findFirst();

                if (entry.isPresent()) {
                    jocUrl = entry.get();
                    LOGGER.debug(String.format("[%s]%s=%s", privateConf, Joc.CONFIG_ENTRY, jocUrl));
                } else {
                    LOGGER.debug(String.format("[%s]not found %s entry", privateConf, Joc.CONFIG_ENTRY));
                }
            } else {
                LOGGER.debug(String.format("not found configuration file %s", privateConf));
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("[%s]exception on read configuration file: %s", privateConf, e.toString()), e);
        }
        return jocUrl;
    }

    private String getJocHref4JobChain(String url, DBItemSchedulerMonNotifications notification) {
        String href = "";
        if (!SOSString.isEmpty(url)) {
            href = url + Joc.URL_PART_JOB_CHAIN + normalizeNameForLink(notification.getJobChainName()) + "&scheduler_id=" + notification
                    .getSchedulerId();
        }
        return href;
    }

    private String getJocHref4Order(String url, DBItemSchedulerMonNotifications notification) {
        String href = "";
        if (!SOSString.isEmpty(url)) {
            href = url + Joc.URL_PART_ORDER + normalizeNameForLink(notification.getJobChainName()) + "," + notification.getOrderId()
                    + "&scheduler_id=" + notification.getSchedulerId();
        }
        return href;
    }

    private String getJocHref4Job(String url, DBItemSchedulerMonNotifications notification) {
        String href = "";
        if (!SOSString.isEmpty(url)) {
            href = url + Joc.URL_PART_JOB + normalizeNameForLink(notification.getJobName()) + "&scheduler_id=" + notification.getSchedulerId();
        }
        return href;
    }

    private String normalizeNameForLink(String name) {
        if (name == null) {
            return "/";
        }
        return name.startsWith("/") ? name : "/" + name;
    }

    private String resolveVars(String txt, DBItemSchedulerMonSystemNotifications systemNotification, DBItemSchedulerMonNotifications notification,
            DBItemSchedulerMonChecks check, EServiceStatus status, EServiceMessagePrefix prefix, String jocHrefJobChain, String jocHrefOrder,
            String jocHrefJob) throws Exception {
        if (SOSString.isEmpty(txt)) {
            return "";
        }
        txt = resolveAllVars(systemNotification, notification, check, status, prefix, txt);

        txt = resolveJocLinkJobChain(txt, jocHrefJobChain);
        txt = resolveJocLinkOrder(txt, jocHrefOrder);
        txt = resolveJocLinkJob(txt, jocHrefJob);
        return txt.trim();
    }
}
