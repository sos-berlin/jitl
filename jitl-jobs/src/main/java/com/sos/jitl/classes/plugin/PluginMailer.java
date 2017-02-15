package com.sos.jitl.classes.plugin;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import sos.net.SOSMail;
import sos.net.mail.options.SOSSmtpMailOptions;
import sos.util.SOSDate;

public class PluginMailer {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginMailer.class);

	private static final String NEW_LINE = "\r\n";
	private SOSSmtpMailOptions options = null;
	private Map<String, String> settings = null;
	private String pluginName;
	private String schedulerId;
	private String schedulerHost;
	private String schedulerPort;
	private boolean sendOnError = false;
	private boolean sendOnWarning = false;

	public PluginMailer(Map<String, String> ms) {
		settings = ms;
	}

	public void init(String pluginName, String schedulerId, String schedulerHost, String schedulerPort) {
		if (settings == null) {
			return;
		}
		SOSDate.setDateTimeFormat("yyyy-MM-dd HH:mm:ss.SSS");

		this.pluginName = pluginName;
		this.schedulerId = schedulerId;
		this.schedulerHost = schedulerHost;
		this.schedulerPort = schedulerPort;

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
	}

	public void sendOnError(String callerClass, String callerMethod, Exception e) {
		if (options != null && sendOnError) {
			send("ERROR", String.format("[error] Plugin %s, %s.%s processed with errors", this.pluginName, callerClass,
					callerMethod), Throwables.getStackTraceAsString(e));
		}
	}

	public void sendOnWarning(String callerClass, String callerMethod, String body) {
		if (options != null && sendOnWarning) {
			send("WARNING", String.format("[warn] Plugin %s, %s processed with warnings", this.pluginName, callerClass,
					callerMethod), body);
		}
	}

	public void sendOnWarning(String callerClass, String callerMethod, Exception e) {
		if (options != null && sendOnWarning) {
			send("WARNING", String.format("[warn] Plugin %s, %s processed with warnings", this.pluginName, callerClass,
					callerMethod), Throwables.getStackTraceAsString(e));
		}
	}

	private void send(String range, String subject, String body) {
		try {
			options.subject.setValue(subject);
			StringBuilder sb = new StringBuilder();
			sb.append(SOSDate.getCurrentTimeAsString());
			sb.append("Z ");
			sb.append(String.format(" JobScheduler -id=%s host=%s port=%s", schedulerId, schedulerHost, schedulerPort));
			sb.append(String.format("%s%s", NEW_LINE, NEW_LINE));
			sb.append(String.format("Plugin %s", pluginName));
			sb.append(String.format("%s%s", NEW_LINE, NEW_LINE));
			sb.append(String.format("%s ", range));
			sb.append(body);
			sb.append(String.format("%s%s%s", NEW_LINE, NEW_LINE, NEW_LINE));
			sb.append("Please refer to the scheduler.log");

			options.body.setValue(sb.toString());
			SOSMail mail = new SOSMail(options.host.getValue());
			mail.sendMail(options);
		} catch (Exception e) {
			LOGGER.error(String.format(e.toString()), e);
		}
	}

	public String getPluginName() {
		return this.pluginName;
	}
}
