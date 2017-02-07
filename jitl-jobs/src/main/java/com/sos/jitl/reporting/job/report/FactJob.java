package com.sos.jitl.reporting.job.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateStatelessConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.model.report.FactModel;

public class FactJob extends JSJobUtilitiesClass<FactJobOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactJob.class);
    private SOSHibernateFactory factory;
    private SOSHibernateStatelessConnection reportingConnection;
    private SOSHibernateStatelessConnection schedulerConnection;
    private FactModel model;

    public FactJob() {
        super(new FactJobOptions());
    }

    public void init() throws Exception {
        factory = new SOSHibernateFactory(getOptions().hibernate_configuration_file.getValue());
        factory.setConnectionIdentifier("reporting");
        factory.setAutoCommit(getOptions().connection_autocommit.value());
        factory.setTransactionIsolation(getOptions().connection_transaction_isolation.value());
        factory.addClassMapping(DBLayer.getReportingClassMapping());
        factory.addClassMapping(DBLayer.getSchedulerClassMapping());
        factory.build();

        schedulerConnection = new SOSHibernateStatelessConnection(factory);
        reportingConnection = new SOSHibernateStatelessConnection(factory);
        reportingConnection.connect();
        schedulerConnection.connect();
    }

    public void exit() {
        if (reportingConnection != null) {
            reportingConnection.disconnect();
        }
        if (schedulerConnection != null) {
            schedulerConnection.disconnect();
        }
        if (factory != null) {
            factory.close();
        }
    }

    public FactJob execute() throws Exception {
        final String conMethodName = "FactJob::Execute";
        LOGGER.debug(conMethodName);
        try {
            getOptions().checkMandatory();
            LOGGER.debug(getOptions().toString());
            model = new FactModel(getOptions());
            model.setConnections(reportingConnection, schedulerConnection);
            model.process();
        } catch (Exception e) {
            LOGGER.error(String.format("%s: %s", conMethodName, e.toString()));
            throw e;
        }
        return this;
    }

    public FactModel getModel() {
        return model;
    }

    public FactJobOptions getOptions() {
        if (objOptions == null) {
            objOptions = new FactJobOptions();
        }
        return objOptions;
    }

}