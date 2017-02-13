package com.sos.jitl.classes.plugin;

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
	private MailSettings settings = null;
	private String pluginName;
	private String schedulerId;
	private String schedulerHost;
	private String schedulerPort;

	public PluginMailer(MailSettings ms) {
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

		options = new SOSSmtpMailOptions();
		options.host.setValue(settings.getHost());
		options.port.value(settings.getPort());
		options.smtp_user.setValue(settings.getUser());
		options.smtp_password.setValue(settings.getPassword());
		options.queue_directory.setValue(settings.getQueueDir());

		options.from.setValue(settings.getFrom());
		options.from_name.setValue(
				String.format("JobScheduler %s at %s:%s", this.schedulerId, this.schedulerHost, this.schedulerPort));
		options.to.setValue(settings.getTo());
		options.cc.setValue(settings.getCc());
		options.bcc.setValue(settings.getBcc());
	}

	public void sendOnError(String callerClass, String callerMethod, Exception e) {
		if (options != null && settings.isSendOnError()) {
			send("ERROR", String.format("[error] Plugin %s, %s.%s processed with errors", this.pluginName, callerClass,
					callerMethod), Throwables.getStackTraceAsString(e));
		}
	}

	public void sendOnWarning(String callerClass, String callerMethod, String body) {
		if (options != null && settings.isSendOnWarning()) {
			send("WARNING", String.format("[warn] Plugin %s, %s processed with warnings", this.pluginName, callerClass,
					callerMethod), body);
		}
	}

	public void sendOnWarning(String callerClass, String callerMethod, Exception e) {
		if (options != null && settings.isSendOnWarning()) {
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
