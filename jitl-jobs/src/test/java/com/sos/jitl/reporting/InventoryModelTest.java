package com.sos.jitl.reporting;

import java.nio.file.Paths;
import java.sql.Connection;
import java.time.Instant;
import java.util.Date;

import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.inventory.model.InventoryModel;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.job.inventory.InventoryJobOptions;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;

public class InventoryModelTest {

    private final static Logger LOGGER = Logger.getLogger(InventoryModelTest.class);
    private static final String FULL_COMMAND = "<show_state what=\"cluster source job_chains job_chain_orders schedules\" />";
//    private static final String COMMAND = "<show_state subsystems=\"folder\" what=\"folders cluster no_subfolders\" path=\"/any/path/that/does/not/exists\" />";

    private SOSHibernateConnection connection;
    private InventoryJobOptions options;
    private String answerXML;
    
    public InventoryModelTest(InventoryJobOptions opt) {
        options = opt;
    }

    public void init() throws Exception {
//        connection = new SOSHibernateConnection(options.hibernate_configuration_file.getValue());
//        connection.setAutoCommit(true);
//        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//        connection.setIgnoreAutoCommitTransactions(true);
////        connection.setSessionFlushMode(FlushMode.COMMIT);
//        connection.addClassMapping(DBLayer.getInventoryClassMapping());
//        connection.connect();
        StringBuilder connectTo = new StringBuilder();
//        connectTo.append("http://sp:40119");
        connectTo.append("http://sp:40441");
//        connectTo.append("http://oh:40411");
        URIBuilder uriBuilder = new URIBuilder(connectTo.toString());
        uriBuilder.setPath("/jobscheduler/master/api/command");
        JobSchedulerRestApiClient restApiClient = new JobSchedulerRestApiClient();
        restApiClient.addHeader("content-type", "application/xml");
        restApiClient.addHeader("accept", "application/xml");
        answerXML = null;
        try {
            answerXML = restApiClient.executeRestServiceCommand("post", uriBuilder.build(), FULL_COMMAND);
        } catch (Exception e) {
            // do Nothing
        }
    }

    public void exit() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public static void main(String[] args) throws Exception {
//        String schedulerData = "C:/ProgramData/sos-berlin.com/jobscheduler/scheduler.1.11";
        String schedulerData = "C:/sp/jobschedulers/cluster/primary/sp_scheduler_cluster";
//        String schedulerData = "C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1";
        String config = "/config";
        InventoryJobOptions opt = new InventoryJobOptions();
        opt.hibernate_configuration_file.setValue(schedulerData + config + "/hibernate.cfg.xml");
        opt.current_scheduler_configuration_directory.setValue(schedulerData + config + "/live");
        opt.schedulerData.setValue(schedulerData);
//        opt.current_scheduler_id.setValue("scheduler.1.11");
//        opt.current_scheduler_id.setValue("SP_41110x1");
         opt.current_scheduler_id.setValue("sp_scheduler_cluster");
//        opt.current_scheduler_hostname.setValue("oh");
        opt.current_scheduler_hostname.setValue("sp");
        opt.current_scheduler_port.value(40441);
//        opt.current_scheduler_port.value(40119);
        InventoryModelTest imt = new InventoryModelTest(opt);
        try {
            imt.init();
            DBLayerInventory layer = new DBLayerInventory(imt.connection);
//            DBItemInventoryInstance instance = layer.getInventoryInstance("oh", 40411);
//            DBItemInventoryInstance instance = layer.getInventoryInstance("sp", 40119);
            DBItemInventoryInstance instance = layer.getInventoryInstance("sp", 40441);
            InventoryModel model = new InventoryModel(imt.connection, instance, Paths.get(schedulerData, config, "scheduler.xml"));
            model.setAnswerXml(imt.answerXML);
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
