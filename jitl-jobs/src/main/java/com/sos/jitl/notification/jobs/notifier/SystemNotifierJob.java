package com.sos.jitl.notification.jobs.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.model.notifier.SystemNotifierModel;

import sos.spooler.Spooler;

public class SystemNotifierJob extends JSJobUtilitiesClass<SystemNotifierJobOptions> {
	private static Logger LOGGER = LoggerFactory.getLogger(SystemNotifierJob.class);
	private final String className = SystemNotifierJob.class.getSimpleName();
	private SOSHibernateFactory factory;
	private SOSHibernateSession session;
	private Spooler spooler;

	public SystemNotifierJob() {
		super(new SystemNotifierJobOptions());
	}

	public void init(Spooler sp) throws Exception {
		spooler = sp;

		try {
			factory = new SOSHibernateFactory(getOptions().hibernate_configuration_file_reporting.getValue());
			factory.setAutoCommit(getOptions().connection_autocommit.value());
			factory.setTransactionIsolation(getOptions().connection_transaction_isolation.value());
			factory.addClassMapping(DBLayer.getNotificationClassMapping());
			factory.build();
		} catch (Exception ex) {
			throw new Exception(String.format("init connection: %s", ex.toString()));
		}
	}

	public void openSession() throws Exception {
	    session = factory.openStatelessSession();
	}

	public void closeSession() throws Exception {
		if (session != null) {
		    session.close();
		}
	}

	public void exit() {
		if (factory != null) {
			factory.close();
		}
	}

	public SystemNotifierJob execute() throws Exception {
		final String methodName = className + "::execute";

		LOGGER.debug(methodName);

		try {
			getOptions().checkMandatory();
			LOGGER.debug(getOptions().toString());

			SystemNotifierModel model = new SystemNotifierModel(session, getOptions(), spooler);
			model.process();
		} catch (Exception e) {
			LOGGER.error(String.format("%s: %s", methodName, e.getMessage()));
			throw e;
		}
		return this;
	}

	public SystemNotifierJobOptions getOptions() {

		if (objOptions == null) {
			objOptions = new SystemNotifierJobOptions();
		}
		return objOptions;
	}
}