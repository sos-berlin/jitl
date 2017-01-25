package com.sos.jitl.reporting.job.inventory;

import java.nio.file.Paths;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.jitl.inventory.model.InventoryModel;
import com.sos.jitl.reporting.db.DBLayer;

public class InventoryJob extends JSJobUtilitiesClass<InventoryJobOptions> {

    private final String className = InventoryJob.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(InventoryJob.class);
    private SOSHibernateConnection connection;
    SOSHibernateFactory factory ;
    private String answerXml;

    public InventoryJob() {
        super(new InventoryJobOptions());
    }

    public void init() throws Exception {
        try {
            
            factory = new SOSHibernateFactory(getOptions().hibernate_configuration_file.getValue());
            factory.setAutoCommit(getOptions().connection_autocommit.value());
            factory.setTransactionIsolation(getOptions().connection_transaction_isolation.value());
            factory.setIgnoreAutoCommitTransactions(true);
            factory.addClassMapping(DBLayer.getInventoryClassMapping());
            factory.open();
            connection = new SOSHibernateConnection(factory);
            connection.connect();
        } catch (Exception ex) {
            throw new Exception(String.format("init connection: %s", ex.toString()));
        }
    }

    public void exit() {
        if (factory != null) {
            factory.close();
        }
        if (connection != null) {
            connection.disconnect();
        }
    }

    public InventoryJob execute() throws Exception {
        final String methodName = className + "::execute";

        LOGGER.debug(methodName);

        try {
            getOptions().checkMandatory();
            LOGGER.debug(getOptions().toString());

            InventoryModel model = new InventoryModel(connection, null, Paths.get(getOptions().getcurrent_scheduler_configuration_directory() + "scheduler.xml"));
            //InventoryModel(SOSHibernateConnection reportingConn, DBItemInventoryInstance jsInstanceItem, Path schedulerXmlPath)
            model.setAnswerXml(answerXml);
            model.process();
        } catch (Exception e) {
            LOGGER.error(String.format("%s: %s", methodName, e.toString()));
            throw e;
        }

        return this;
    }

    public InventoryJobOptions getOptions() {
        if (objOptions == null) {
            objOptions = new InventoryJobOptions();
        }
        return objOptions;
    }

    
    public void setAnswerXml(String answerXml) {
        this.answerXml = answerXml;
    }
    
}