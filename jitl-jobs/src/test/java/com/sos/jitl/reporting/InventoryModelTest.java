package com.sos.jitl.reporting;

import org.apache.log4j.Logger;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.job.inventory.InventoryJobOptions;
import com.sos.jitl.reporting.model.inventory.InventoryModel;

public class InventoryModelTest {

    private static Logger logger = Logger.getLogger(InventoryModelTest.class);

    private SOSHibernateConnection connection;
    private InventoryJobOptions options;

    public InventoryModelTest(InventoryJobOptions opt) {
        options = opt;
    }

    public void init() throws Exception {
        connection = new SOSHibernateConnection(options.hibernate_configuration_file.Value());
        connection.setAutoCommit(options.connection_autocommit.value());
        connection.setTransactionIsolation(options.connection_transaction_isolation.value());
        connection.setIgnoreAutoCommitTransactions(true);
        connection.addClassMapping(DBLayer.getInventoryClassMapping());
        connection.connect();
    }

    public void exit() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public static void main(String[] args) throws Exception {
        String config = "D:/Arbeit/scheduler/jobscheduler_data/re-dell_4646_snap_1_8/config";

        InventoryJobOptions opt = new InventoryJobOptions();
        opt.hibernate_configuration_file.Value(config + "/hibernate_reporting.cfg.xml");
        opt.current_scheduler_configuration_directory.Value(config + "/live");
        opt.current_scheduler_id.Value("re-dell_4646_snap_1_8");
        opt.current_scheduler_hostname.Value("re-dell");
        opt.current_scheduler_port.value(4646);

        InventoryModelTest imt = new InventoryModelTest(opt);

        try {
            imt.init();

            InventoryModel model = new InventoryModel(imt.connection, imt.options);
            model.process();
        } catch (Exception ex) {
            throw ex;
        } finally {
            imt.exit();
        }

    }
}
