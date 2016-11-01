package com.sos.jitl.reporting.job.inventory;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.model.inventory.InventoryModel;

public class InventoryJob extends JSJobUtilitiesClass<InventoryJobOptions> {

    private final String className = InventoryJob.class.getSimpleName();
    private static Logger logger = Logger.getLogger(InventoryJob.class);
    private SOSHibernateConnection connection;
    private String answerXml;

    public InventoryJob() {
        super(new InventoryJobOptions());
    }

    public void init() throws Exception {
        try {
            connection = new SOSHibernateConnection(getOptions().hibernate_configuration_file.getValue());
            connection.setAutoCommit(getOptions().connection_autocommit.value());
            connection.setTransactionIsolation(getOptions().connection_transaction_isolation.value());
            connection.setIgnoreAutoCommitTransactions(true);
            connection.addClassMapping(DBLayer.getInventoryClassMapping());
            connection.connect();
        } catch (Exception ex) {
            throw new Exception(String.format("init connection: %s", ex.toString()));
        }
    }

    public void exit() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public InventoryJob execute() throws Exception {
        final String methodName = className + "::execute";

        logger.debug(methodName);

        try {
            getOptions().checkMandatory();
            logger.debug(getOptions().toString());

            InventoryModel model = new InventoryModel(connection, getOptions());
            model.setAnswerXml(answerXml);
            model.process();
        } catch (Exception e) {
            logger.error(String.format("%s: %s", methodName, e.toString()));
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