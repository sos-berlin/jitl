package com.sos.jitl.reporting.plugin;

import java.nio.file.Path;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateStatelessConnection;
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.helper.NotificationReportExecution;
import com.sos.jitl.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.jitl.notification.model.history.CheckHistoryModel;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTrigger;

public class FactNotificationPlugin {
	private static Logger LOGGER = LoggerFactory.getLogger(FactNotificationPlugin.class);

	private final String className = FactNotificationPlugin.class.getSimpleName();
	private static final String SCHEMA_PATH = "notification/SystemMonitorNotification_v1.0.xsd";
	private SOSHibernateFactory factory;
	private CheckHistoryModel model;
	private boolean hasErrorOnInit = false;
	private PluginMailer mailer = null;

	public void init(PluginMailer pluginMailer, Path configDir, String hibernateFile) {
		String method = "init";
		hasErrorOnInit = false;
		try {
			mailer = pluginMailer;
			factory = new SOSHibernateFactory(hibernateFile);
			factory.setConnectionIdentifier("notification");
			factory.setAutoCommit(false);
			factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			factory.addClassMapping(DBLayer.getNotificationClassMapping());
			factory.build();

			CheckHistoryJobOptions opt = new CheckHistoryJobOptions();
			opt.hibernate_configuration_file.setValue(hibernateFile);
			opt.schema_configuration_file.setValue(configDir.resolve(SCHEMA_PATH).toString());

			model = new CheckHistoryModel(opt);
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

		SOSHibernateStatelessConnection connection = new SOSHibernateStatelessConnection(factory);
		try {
			connection.connect();

			model.setConnection(connection);
			model.initPlugins();
			model.process(item);
		} catch (Exception e) {
			LOGGER.error(String.format("%s: %s", method, e.toString()), e);
			mailer.sendOnError(className, method, e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
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
		item.setJobChainName(rt.getParentName());
		item.setJobChainTitle(rt.getParentTitle());
		item.setOrderId(rt.getName());
		item.setOrderTitle(rt.getTitle());
		item.setOrderStartTime(rt.getStartTime());
		item.setOrderEndTime(rt.getEndTime());
		item.setOrderStepState(re.getState());
		item.setOrderStepStartTime(re.getStartTime());
		item.setOrderStepEndTime(re.getEndTime());
		item.setJobName(re.getName());
		item.setJobTitle(re.getTitle());
		item.setTaskStartTime(re.getTaskStartTime());
		item.setTaskEndTime(re.getTaskEndTime());
		item.setReturnCode(re.getExitCode() == null ? null : new Long(re.getExitCode().intValue()));
		item.setError(re.getError());
		item.setErrorCode(re.getErrorCode());
		item.setErrorText(re.getErrorText());
		return item;
	}

	public void exit() {
		if (factory != null) {
			factory.close();
		}
	}

	public boolean getHasErrorOnInit() {
		return this.hasErrorOnInit;
	}
}
