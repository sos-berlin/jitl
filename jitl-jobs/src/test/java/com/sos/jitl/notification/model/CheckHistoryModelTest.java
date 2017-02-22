package com.sos.jitl.notification.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateStatelessConnection;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.jitl.notification.model.history.CheckHistoryModel;
import com.sos.jitl.notification.plugins.history.CheckHistoryTimerPlugin;

public class CheckHistoryModelTest {
	private static Logger LOGGER = LoggerFactory.getLogger(CheckHistoryModelTest.class);

	private SOSHibernateFactory factory;
	private SOSHibernateStatelessConnection connection;
	private CheckHistoryJobOptions options;

	public CheckHistoryModelTest(CheckHistoryJobOptions opt) {
		options = opt;
	}

	public void init() throws Exception {
		factory = new SOSHibernateFactory(options.hibernate_configuration_file_reporting.getValue());
		factory.setConnectionIdentifier("notification");
		factory.setAutoCommit(false);
		factory.addClassMapping(DBLayer.getNotificationClassMapping());
		factory.build();

		connection = new SOSHibernateStatelessConnection(factory);
		connection.connect();
	}

	public void exit() {
		if (connection != null) {
			connection.disconnect();
		}

		if (factory != null) {
			factory.close();
		}
	}

	public static void main(String[] args) throws Exception {

		CheckHistoryJobOptions opt = new CheckHistoryJobOptions();
		opt.hibernate_configuration_file_reporting.setValue(Config.HIBERNATE_CONFIGURATION_FILE);
		opt.schema_configuration_file.setValue(Config.SCHEMA_CONFIGURATION_FILE);
		opt.plugins.setValue(CheckHistoryTimerPlugin.class.getName());

		CheckHistoryModelTest t = new CheckHistoryModelTest(opt);

		try {
			LOGGER.info("START --");
			t.init();

			CheckHistoryModel model = new CheckHistoryModel(t.connection, t.options);
			model.init();
			model.process();

			LOGGER.info("END --");

		} catch (Exception ex) {
			throw ex;
		} finally {
			t.exit();
		}

	}

}
