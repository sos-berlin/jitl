package com.sos.jitl.notification.model;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.notification.jobs.cleanup.CleanupNotificationsJobOptions;
import com.sos.jitl.notification.model.cleanup.CleanupNotificationsModel;

public class CleanupNotificationsModelTest {

    private SOSHibernateSession sosHibernateFactory;
    private CleanupNotificationsJobOptions options;

    public CleanupNotificationsModelTest(CleanupNotificationsJobOptions opt) {
        options = opt;
    }

    public void init() throws Exception {
//        connection = new SOSHibernateConnection(options.hibernate_configuration_file.getValue());
//        connection.setAutoCommit(options.connection_autocommit.value());
//        connection.setTransactionIsolation(options.connection_transaction_isolation.value());
//        connection.setIgnoreAutoCommitTransactions(true);
//        connection.addClassMapping(DBLayer.getNotificationClassMapping());
//        connection.connect();
    }

    public void exit() {
        if (sosHibernateFactory != null) {
            sosHibernateFactory.close();
        }
    }

    public static void main(String[] args) throws Exception {
       
        CleanupNotificationsJobOptions opt = new CleanupNotificationsJobOptions();
        opt.hibernate_configuration_file_reporting.setValue(Config.HIBERNATE_CONFIGURATION_FILE);

        CleanupNotificationsModelTest t = new CleanupNotificationsModelTest(opt);

        try {
            t.init();

            CleanupNotificationsModel model = new CleanupNotificationsModel(t.sosHibernateFactory, t.options);
            model.process();

        } catch (Exception ex) {
            throw ex;
        } finally {
            t.exit();
        }

    }

}
