package com.sos.jitl.notification.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateStatelessConnection;
import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.jitl.notification.model.notifier.SystemNotifierModel;

public class SystemNotifierModelTest {
	private static Logger logger = LoggerFactory.getLogger(SystemNotifierModelTest.class);

	private SOSHibernateStatelessConnection connection;
	private SystemNotifierJobOptions options;

	public SystemNotifierModelTest(SystemNotifierJobOptions opt) {
		options = opt;
	}

	public void init() throws Exception {
		// connection = new
		// SOSHibernateConnection(options.hibernate_configuration_file.getValue());
		// connection.setAutoCommit(options.connection_autocommit.value());
		// connection.setTransactionIsolation(options.connection_transaction_isolation.value());
		// connection.setIgnoreAutoCommitTransactions(true);
		// connection.setUseOpenStatelessSession(true);
		// connection.addClassMapping(DBLayer.getNotificationClassMapping());
		// connection.connect();
	}

	public void exit() {
		if (connection != null) {
			connection.disconnect();
		}
	}

	public static void main(String[] args) throws Exception {

		SystemNotifierJobOptions opt = new SystemNotifierJobOptions();
		opt.hibernate_configuration_file_reporting.setValue(Config.HIBERNATE_CONFIGURATION_FILE);
		opt.schema_configuration_file.setValue(Config.SCHEMA_CONFIGURATION_FILE);
		opt.system_configuration_file.setValue(Config.SYSTEM_CONFIGURATION_FILE);

		SystemNotifierModelTest t = new SystemNotifierModelTest(opt);

		try {
			logger.info("START --");
			t.init();

			SystemNotifierModel model = new SystemNotifierModel(t.connection, t.options, null);
			model.process();
			logger.info("END --");

		} catch (Exception ex) {
			throw ex;
		} finally {
			t.exit();
		}

	}

}
