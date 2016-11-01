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
        String config = "C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT2/sp_41110x2/config";

        InventoryJobOptions opt = new InventoryJobOptions();
        opt.hibernate_configuration_file.setValue(config + "/hibernate.cfg.xml");
        opt.current_scheduler_configuration_directory.setValue(config + "/live");
        opt.current_scheduler_id.setValue("sp_41110x2");
        opt.current_scheduler_hostname.setValue("sp");
        opt.current_scheduler_port.value(4117);
        String answerXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><spooler><answer time=\"2016-11-01T09:47:14.046Z\">"
                + "<state config_file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT2/sp_41110x2/config/scheduler.xml\""
                + " db=\"jdbc -id=spooler -class=oracle.jdbc.driver.OracleDriver jdbc:oracle:thin:@//SP:1521/xe -user=sos_scheduler\""
                + " host=\"SP\" http_port=\"40118\" id=\"SP_41110x2\" log_file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT2/"
                + "sp_41110x2/logs/scheduler-2016-10-31-151004.SP_41110x2.log\" loop=\"7860\" pid=\"12544\" spooler_id=\"SP_41110x2\""
                + " spooler_running_since=\"2016-10-31T15:10:04Z\" state=\"running\" tcp_port=\"4118\" time=\"2016-11-01T09:47:14.047Z\""
                + " time_zone=\"Europe/Berlin\" udp_port=\"4118\" version=\"1.11.0-SNAPSHOT\""
                + " version_commit_hash=\"0bdcac6a9acc9dc81c9739129d62e9e6cc105671\" wait_until=\"2016-11-01T23:00:00.000Z\" waits=\"2200\">"
                + "<order_id_spaces/><subprocesses/><remote_schedulers active=\"0\" count=\"0\"/><http_server/><connections>"
                + "<connection operation_type=\"HTTP\" received_bytes=\"2266\" responses=\"10\" sent_bytes=\"12693\" state=\"ready/receiving\">"
                + "<peer host_ip=\"192.11.0.50\" port=\"55768\"/></connection><connection operation_type=\"HTTP\" received_bytes=\"6425\""
                + " responses=\"33\" sent_bytes=\"262090\" state=\"ready/receiving\"><peer host_ip=\"192.11.0.50\" port=\"55769\"/></connection>"
                + "<connection operation_type=\"HTTP\" received_bytes=\"4985\" responses=\"25\" sent_bytes=\"233421\" state=\"ready/receiving\">"
                + "<peer host_ip=\"192.11.0.50\" port=\"55770\"/></connection><connection operation_type=\"HTTP\" received_bytes=\"8500\""
                + " responses=\"44\" sent_bytes=\"173415\" state=\"ready/receiving\"><peer host_ip=\"192.11.0.50\" port=\"55771\"/></connection>"
                + "</connections></state></answer></spooler>";
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
