package com.sos.jitl.reporting;

import java.sql.Connection;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.job.report.FactJobOptions;
import com.sos.jitl.reporting.model.report.FactModel;

public class FactModelTest {

    private SOSHibernateConnection reportingConnection;
    private SOSHibernateConnection schedulerConnection;

    private FactJobOptions options;

    public FactModelTest(FactJobOptions opt) {
        this.options = opt;
    }

    public void init() throws Exception {
        try {
            reportingConnection = new SOSHibernateConnection(options.hibernate_configuration_file.getValue());
            reportingConnection.setConnectionIdentifier("reporting");
            reportingConnection.setAutoCommit(options.connection_autocommit.value());
            reportingConnection.setIgnoreAutoCommitTransactions(true);
            reportingConnection.setTransactionIsolation(options.connection_transaction_isolation.value());
            reportingConnection.setUseOpenStatelessSession(true);
            reportingConnection.addClassMapping(DBLayer.getReportingClassMapping());
            reportingConnection.connect();
        } catch (Exception ex) {
            throw new Exception(String.format("reporting connection: %s", ex.toString()));
        }

        try {
            schedulerConnection = new SOSHibernateConnection(options.hibernate_configuration_file_scheduler.getValue());
            schedulerConnection.setConnectionIdentifier("scheduler");
            schedulerConnection.setAutoCommit(options.connection_autocommit_scheduler.value());
            schedulerConnection.setIgnoreAutoCommitTransactions(true);
            schedulerConnection.setTransactionIsolation(options.connection_transaction_isolation_scheduler.value());
            schedulerConnection.setUseOpenStatelessSession(true);
            schedulerConnection.addClassMapping(DBLayer.getSchedulerClassMapping());
            schedulerConnection.connect();
        } catch (Exception ex) {
            throw new Exception(String.format("scheduler connection: %s", ex.toString()));
        }

    }

    public void exit() {
        if (reportingConnection != null) {
            reportingConnection.disconnect();
        }
        if (schedulerConnection != null) {
            schedulerConnection.disconnect();
        }

    }

    public static void main(String[] args) throws Exception {

        String config = "D:/Arbeit/scheduler/jobscheduler_data/re-dell_4646_snap_1_8/config";
        FactJobOptions opt = new FactJobOptions();

        opt.hibernate_configuration_file.setValue(config + "/hibernate_reporting.cfg.xml");
        opt.connection_autocommit.value(false);

        opt.hibernate_configuration_file_scheduler.setValue(config + "/hibernate_reporting_scheduler.cfg.xml");
        opt.connection_transaction_isolation.value(Connection.TRANSACTION_READ_COMMITTED);
        opt.connection_autocommit_scheduler.value(false);
        opt.connection_transaction_isolation_scheduler.value(Connection.TRANSACTION_READ_COMMITTED);

        opt.max_history_age.setValue("1d");
        opt.force_max_history_age.value(true);
        opt.max_uncompleted_age.setValue("1d");

        FactModelTest imt = new FactModelTest(opt);

        try {
            imt.init();

            FactModel model = new FactModel(imt.reportingConnection, imt.schedulerConnection, imt.options);

            model.process();
        } catch (Exception ex) {
            throw ex;
        } finally {
            imt.exit();
        }

    }
}
