package com.sos.jitl.notification.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.jitl.notification.model.notifier.SystemNotifierModel;

public class SystemNotifierModelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierModelTest.class);

    private SOSHibernateFactory factory;
    private SOSHibernateSession session;

    private SystemNotifierJobOptions options;

    public SystemNotifierModelTest(SystemNotifierJobOptions opt) {
        options = opt;
    }

    public void init() throws Exception {
        try {
            factory = new SOSHibernateFactory(options.hibernate_configuration_file_reporting.getValue());
            factory.setIdentifier("notification");
            factory.setAutoCommit(options.connection_autocommit.value());
            factory.setTransactionIsolation(options.connection_transaction_isolation.value());
            factory.addClassMapping(DBLayer.getNotificationClassMapping());
            factory.build();

            session = factory.openStatelessSession();
        } catch (Exception ex) {
            throw new Exception(String.format("reporting connection: %s", ex.toString()));
        }
    }

    public void exit() {
        if (session != null) {
            session.close();
        }
        if (factory != null) {
            factory.close();
        }
    }
 
    public static void main(String[] args) throws Exception {

        SystemNotifierJobOptions opt = new SystemNotifierJobOptions();
        opt.hibernate_configuration_file_reporting.setValue(Config.HIBERNATE_CONFIGURATION_FILE);
        opt.schema_configuration_file.setValue(Config.SCHEMA_CONFIGURATION_FILE);
        opt.system_configuration_file.setValue(Config.SYSTEM_CONFIGURATION_FILE);

        SystemNotifierModelTest t = new SystemNotifierModelTest(opt);

        try {
            LOGGER.info("START --");
            t.init();

            SystemNotifierModel model = new SystemNotifierModel(t.session, t.options, null);
            model.process();
            LOGGER.info("END --");

        } catch (Exception ex) {
            throw ex;
        } finally {
            t.exit();
        }

    }

}
