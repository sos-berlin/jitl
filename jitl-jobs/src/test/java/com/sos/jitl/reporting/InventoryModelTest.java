package com.sos.jitl.reporting;

import org.apache.log4j.Logger;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.job.inventory.InventoryJobOptions;
import com.sos.jitl.reporting.model.inventory.InventoryModel;

public class InventoryModelTest {

    private final static Logger LOGGER = Logger.getLogger(InventoryModelTest.class);

    private SOSHibernateConnection connection;
    private InventoryJobOptions options;

    public InventoryModelTest(InventoryJobOptions opt) {
        options = opt;
    }

    public void init() throws Exception {
        connection = new SOSHibernateConnection(options.hibernate_configuration_file.getValue());
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
//        String config = "C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config";
        String config = "C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/config";

        InventoryJobOptions opt = new InventoryJobOptions();
        opt.hibernate_configuration_file.setValue(config + "/hibernate.cfg.xml");
        opt.current_scheduler_configuration_directory.setValue(config + "/live");
        opt.current_scheduler_id.setValue("sp_41110x1");
        opt.current_scheduler_hostname.setValue("sp");
        opt.current_scheduler_port.value(40119);
        String answerXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><spooler><answer time=\"2016-11-18T09:11:27.698Z\">"
                + "<state config_file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/config/scheduler.xml\" "
                + "db=\"jdbc -id=spooler -class=org.mariadb.jdbc.Driver jdbc:mysql://SP:3305/scheduler -user=scheduler\" host=\"SP\" "
                + "http_port=\"40119\" id=\"SP_41110x1\" "
                + "log_file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/logs/scheduler-2016-11-18-084056.SP_41110x1.log\""
                + " loop=\"1523\" pid=\"3592\" spooler_id=\"SP_41110x1\" spooler_running_since=\"2016-11-18T08:40:56Z\" state=\"running\""
                + " tcp_port=\"4119\" time=\"2016-11-18T09:11:27.699Z\" time_zone=\"Europe/Berlin\" udp_port=\"4119\" version=\"1.11.0-SNAPSHOT\""
                + " version_commit_hash=\"64df410322875f07ecf6ddb963531493403b5990\" wait_until=\"2016-11-18T09:30:00.000Z\" waits=\"452\">"
                + "<order_id_spaces/><subprocesses/><remote_schedulers active=\"0\" count=\"0\"/><http_server/><connections>"
                + "<connection operation_type=\"HTTP\" received_bytes=\"745\" responses=\"2\" sent_bytes=\"7823\" state=\"processing/ready\">"
                + "<peer host_ip=\"127.0.0.1\" port=\"56408\"/><operation><http_operation/></operation></connection></connections></state>"
                + "</answer></spooler>";
        InventoryModelTest imt = new InventoryModelTest(opt);

        try {
            imt.init();

            InventoryModel model = new InventoryModel(imt.connection, imt.options);
            model.setAnswerXml(answerXml);
            model.process();
        } catch (Exception ex) {
            throw ex;
        } finally {
            imt.exit();
        }

    }
}
