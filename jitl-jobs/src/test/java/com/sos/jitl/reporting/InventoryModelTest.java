package com.sos.jitl.reporting;

import java.time.Instant;
import java.util.Date;

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
        String schedulerData = "C:/sp/jobschedulers/cluster/primary/sp_scheduler_cluster";
        String config = "/config";

        InventoryJobOptions opt = new InventoryJobOptions();
        opt.hibernate_configuration_file.setValue(schedulerData + config + "/hibernate.cfg.xml");
        opt.current_scheduler_configuration_directory.setValue(schedulerData + config + "/live");
        opt.schedulerData.setValue(schedulerData);
        opt.current_scheduler_id.setValue("sp_scheduler_cluster");
        opt.current_scheduler_hostname.setValue("sp");
        opt.current_scheduler_port.value(40441);
        String answerXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><spooler><answer time=\"2016-11-23T08:39:45.853Z\">"
                + "<state config_file=\"C:/sp/jobschedulers/cluster/primary/sp_scheduler_cluster/config/scheduler.xml\" "
                + "db=\"jdbc -id=spooler -class=org.mariadb.jdbc.Driver jdbc:mysql://SP:3305/scheduler -user=scheduler\" "
                + "host=\"SP\" http_port=\"40441\" id=\"sp_scheduler_cluster\" "
                + "log_file=\"C:/sp/jobschedulers/cluster/primary/sp_scheduler_cluster/logs/scheduler-2016-11-23-074050.sp_scheduler_cluster.log\" "
                + "loop=\"8399\" pid=\"13056\" spooler_id=\"sp_scheduler_cluster\" spooler_running_since=\"2016-11-23T07:40:50Z\" state=\"running\" "
                + "tcp_port=\"4441\" time=\"2016-11-23T08:39:45.853Z\" time_zone=\"Europe/Berlin\" udp_port=\"4441\" version=\"1.11.0-SNAPSHOT\" "
                + "version_commit_hash=\"62e437bb0b123d1f2ca1dc138f348da27de82eec\" wait_until=\"2016-11-23T09:00:00.000Z\" waits=\"3545\">"
                + "<order_id_spaces/><subprocesses/><remote_schedulers active=\"0\" count=\"0\"/><cluster active=\"yes\" "
                + "cluster_member_id=\"sp_scheduler_cluster/SP:40441\" exclusive=\"yes\" is_member_allowed_to_start=\"yes\">"
                + "<cluster_member active=\"yes\" backup_precedence=\"0\" cluster_member_id=\"sp_scheduler_cluster/SP:40441\" "
                + "demand_exclusiveness=\"yes\" exclusive=\"yes\" heart_beat_count=\"51\" heart_beat_quality=\"good\" host=\"SP\" "
                + "http_url=\"http://SP:4441\" last_detected_heart_beat=\"2016-11-23T08:39:45Z\" last_detected_heart_beat_age=\"0\" pid=\"13056\" "
                + "running_since=\"2016-11-23T07:40:51.416Z\" tcp_port=\"4441\" udp_port=\"4441\" version=\"1.11.0-SNAPSHOT\"/>"
                + "<cluster_member backup=\"yes\" backup_precedence=\"1\" cluster_member_id=\"sp_scheduler_cluster/SP:40442\" "
                + "demand_exclusiveness=\"yes\" heart_beat_count=\"48\" heart_beat_quality=\"good\" host=\"SP\" http_url=\"http://SP:4442\" "
                + "last_detected_heart_beat=\"2016-11-23T08:39:45Z\" last_detected_heart_beat_age=\"0\" pid=\"6228\" "
                + "running_since=\"2016-11-23T07:42:35.621Z\" tcp_port=\"4442\" udp_port=\"4442\" version=\"1.11.0-SNAPSHOT\"/></cluster>"
                + "<http_server/><connections/></state></answer></spooler>";
        InventoryModelTest imt = new InventoryModelTest(opt);

        try {
            imt.init();

            InventoryModel model = new InventoryModel(imt.connection, imt.options);
            model.setAnswerXml(answerXml);
            Date start = Date.from(Instant.now());
            model.process();
            Date end = Date.from(Instant.now());
            Long diff = end.getTime() - start.getTime();
            LOGGER.info("Initial Inventory Job running time in ms: " + diff.toString());
        } catch (Exception ex) {
            throw ex;
        } finally {
            imt.exit();
        }

    }
}
