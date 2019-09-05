package com.sos.jitl.eventhandler.plugin.notifier;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import sos.net.SOSMail;
import sos.net.mail.options.SOSSmtpMailOptions;
import sos.util.SOSDate;
import sos.util.SOSString;

public class Mailer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mailer.class);

    private static final String NEW_LINE = "\r\n";
    private SOSSmtpMailOptions options = null;
    private Map<String, String> settings = null;
    private String pluginName;
    private boolean sendOnError = false;
    private boolean sendOnWarning = false;
    private boolean queueOnly = false;

    public Mailer(String pluginName, Map<String, String> ms) {
        settings = ms;
        init(pluginName);
    }

    private void init(String name) {
        pluginName = name;
        sendOnError = false;
        sendOnWarning = false;

        if (settings == null || SOSString.isEmpty(settings.get("smtp")) || SOSString.isEmpty(settings.get("from")) || SOSString.isEmpty(settings.get(
                "to"))) {
            return;
        }

        SOSDate.setDateTimeFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            options = new SOSSmtpMailOptions();
            options.host.setValue(settings.get("smtp"));
            options.port.setValue(settings.get("mail.smtp.port"));
            options.smtp_user.setValue(settings.get("mail.smtp.user"));
            options.smtp_password.setValue(settings.get("mail.smtp.password"));
            options.queue_directory.setValue(settings.get("queue_dir"));

            options.from.setValue(settings.get("from"));
            options.from_name.setValue(settings.get("from_name"));
            options.to.setValue(settings.get("to"));
            options.cc.setValue(settings.get("cc"));
            options.bcc.setValue(settings.get("bcc"));
        } catch (Exception e) {
            LOGGER.error(e.toString());
            options = null;
            return;
        }

        try {
            sendOnError = settings.get("mail_on_error").equals("1");
        } catch (Exception ex) {
        }

        try {
            sendOnWarning = settings.get("mail_on_warning").equals("1");
        } catch (Exception ex) {
        }

        try {
            queueOnly = settings.get("queue_only").equals("1");
        } catch (Exception ex) {
        }

    }

    public void sendOnError(String callerClass, String callerMethod, String body) {
        if (sendOnError) {
            send("ERROR", String.format("[error] Plugin %s, %s.%s processed with errors", pluginName, callerClass, callerMethod), null, body);
        }
    }

    public void sendOnError(String callerClass, String callerMethod, Throwable t) {
        sendOnError(callerClass, callerMethod, null, t);
    }

    public void sendOnError(String callerClass, String callerMethod, String bodyPart, Throwable t) {
        if (sendOnError) {
            send("ERROR", String.format("[error] Plugin %s, %s.%s processed with errors", pluginName, callerClass, callerMethod), bodyPart,
                    getStackTrace(t));
        }
    }

    public void sendOnWarning(String callerClass, String callerMethod, String body) {
        if (sendOnWarning) {
            send("WARNING", String.format("[warn] Plugin %s, %s.%s processed with warnings", pluginName, callerClass, callerMethod), null, body);
        }
    }

    public void sendOnWarning(String callerClass, String callerMethod, Throwable t) {
        if (sendOnWarning) {
            send("WARNING", String.format("[warn] Plugin %s, %s.%s processed with warnings", pluginName, callerClass, callerMethod), null,
                    getStackTrace(t));
        }
    }

    public void sendOnRecovery(String callerClass, String callerMethod, Throwable t) {
        if (sendOnError || sendOnWarning) {
            send("RECOVERY", String.format("[recovery] Plugin %s, %s.%s recovered from previous error", pluginName, callerClass, callerMethod), null,
                    getStackTrace(t));
        }
    }

    private String getStackTrace(Throwable t) {
        return t == null ? "null" : Throwables.getStackTraceAsString(t);
    }

    private void send(String range, String subject, String bodyPart, String body) {
        try {
            options.subject.setValue(subject);
            StringBuilder sb = new StringBuilder();
            sb.append(SOSDate.getCurrentTimeAsString());
            sb.append("Z ");
            sb.append(String.format(options.from_name.getValue()));
            sb.append(String.format("%s%s", NEW_LINE, NEW_LINE));
            sb.append(String.format("Plugin %s", pluginName));
            sb.append(String.format("%s%s", NEW_LINE, NEW_LINE));
            sb.append(String.format("%s ", range));
            if (!SOSString.isEmpty(bodyPart)) {
                sb.append(String.format("%s", NEW_LINE));
                sb.append(bodyPart);
                sb.append(String.format("%s", NEW_LINE));
            }
            sb.append(body);
            sb.append(String.format("%s%s%s", NEW_LINE, NEW_LINE, NEW_LINE));
            sb.append("Please refer to the scheduler.log");

            options.body.setValue(sb.toString());
            SOSMail mail = new SOSMail(options.host.getValue());
            mail.sendMail(options, queueOnly, false);
        } catch (Throwable e) {
            LOGGER.error(String.format(e.toString()), e);
        }
    }

    public String getPluginName() {
        return pluginName;
    }
}
