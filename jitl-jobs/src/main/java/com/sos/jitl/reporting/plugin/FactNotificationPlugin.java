package com.sos.jitl.reporting.plugin;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.jitl.notification.helper.NotificationReportExecution;
import com.sos.jitl.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.jitl.notification.model.history.CheckHistoryModel;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTrigger;

public class FactNotificationPlugin {
	private static Logger LOGGER = LoggerFactory.getLogger(FactNotificationPlugin.class);

	private final String className = FactNotificationPlugin.class.getSimpleName();
	private static final String SCHEMA_PATH = "notification/SystemMonitorNotification_v1.0.xsd";
	private CheckHistoryModel model;
	private boolean hasErrorOnInit = false;
	private PluginMailer mailer = null;

	public void init(SOSHibernateSession conn,PluginMailer pluginMailer, Path configDir) {
		String method = "init";
		hasErrorOnInit = false;
		try {
			mailer = pluginMailer;

			CheckHistoryJobOptions opt = new CheckHistoryJobOptions();
			opt.schema_configuration_file.setValue(configDir.resolve(SCHEMA_PATH).toString());

			model = new CheckHistoryModel(conn,opt);
			model.init();
		} catch (Exception e) {
			hasErrorOnInit = true;
			Exception ex = new Exception(
					String.format("skip notification processing due %s errors", method, e.toString()), e);
			LOGGER.error(String.format("%s.%s %s", className, method, ex.toString()), ex);
			mailer.sendOnError(className, method, ex);
		}
	}

	public void process(NotificationReportExecution item) {
		String method = "process";
		if (hasErrorOnInit) {
			return;
		}

		try {
			model.process(item);
		} catch (Exception e) {
			LOGGER.error(String.format("%s: %s", method, e.toString()), e);
			mailer.sendOnError(className, method, e);
		}
	}

	public NotificationReportExecution convert(DBItemReportTrigger rt, DBItemReportExecution re) {
		NotificationReportExecution item = new NotificationReportExecution();
		// unique
		item.setSchedulerId(re.getSchedulerId());
		item.setStandalone(re.getTriggerId().equals(new Long(0)));
		item.setTaskId(re.getHistoryId());
		item.setStep(re.getStep());
		item.setOrderHistoryId(rt.getHistoryId());
		// others
		item.setJobChainName(normalizeName(rt.getParentName()));
		item.setJobChainTitle(rt.getParentTitle());
		item.setOrderId(rt.getName());
		item.setOrderTitle(rt.getTitle());
		item.setOrderStartTime(rt.getStartTime());
		item.setOrderEndTime(rt.getEndTime());
		item.setOrderStepState(re.getState());
		item.setOrderStepStartTime(re.getStartTime());
		item.setOrderStepEndTime(re.getEndTime());
		item.setJobName(normalizeName(re.getName()));
		item.setJobTitle(re.getTitle());
		item.setTaskStartTime(re.getTaskStartTime());
		item.setTaskEndTime(re.getTaskEndTime());
		item.setReturnCode(re.getExitCode() == null ? null : new Long(re.getExitCode().intValue()));
		item.setError(re.getError());
		item.setErrorCode(re.getErrorCode());
		item.setErrorText(re.getErrorText());
		return item;
	}

	private String normalizeName(String val) {
		if (val != null && val.startsWith("/")) {
			val = val.substring(1);
		}
		return val;
	}

	public boolean getHasErrorOnInit() {
		return this.hasErrorOnInit;
	}
}
