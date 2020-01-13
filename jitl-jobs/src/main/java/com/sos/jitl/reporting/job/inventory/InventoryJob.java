package com.sos.jitl.reporting.job.inventory;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.jitl.inventory.model.InventoryModel;
import com.sos.jitl.reporting.db.DBLayer;

public class InventoryJob extends JSJobUtilitiesClass<InventoryJobOptions> {

    private final String className = InventoryJob.class.getSimpleName();
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryJob.class);
    private SOSHibernateSession sosHibernateSession;
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
            factory.addClassMapping(DBLayer.getInventoryClassMapping());
            factory.build();
            sosHibernateSession = factory.openSession();
        } catch (Exception ex) {
            throw new Exception(String.format("init connection: %s", ex.toString()));
        }
    }

    public void exit() {
        if (factory != null) {
            factory.close();
        }
        if (sosHibernateSession != null) {
            sosHibernateSession.close();
        }
    }

    public InventoryJob execute() throws Exception {
        final String methodName = className + "::execute";

        LOGGER.debug(methodName);

        try {
            getOptions().checkMandatory();
            LOGGER.debug(getOptions().toString());

            InventoryModel model = new InventoryModel(factory, null, Paths.get(getOptions().getcurrent_scheduler_configuration_directory() + "scheduler.xml"));
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