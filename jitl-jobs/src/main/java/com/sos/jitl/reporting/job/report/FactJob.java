package com.sos.jitl.reporting.job.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.model.report.FactModel;

public class FactJob extends JSJobUtilitiesClass<FactJobOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactJob.class);
    private SOSHibernateConnection reportingConnection;
    private SOSHibernateConnection schedulerConnection;
    private FactModel model;

    public FactJob() {
        super(new FactJobOptions());
    }

    public void init() throws Exception {
        reportingConnection = new SOSHibernateConnection(getOptions().hibernate_configuration_file.getValue());
        reportingConnection.setConnectionIdentifier("reporting");
        reportingConnection.setAutoCommit(getOptions().connection_autocommit.value());
        reportingConnection.setIgnoreAutoCommitTransactions(true);
        reportingConnection.setTransactionIsolation(getOptions().connection_transaction_isolation.value());
        reportingConnection.setUseOpenStatelessSession(true);
        reportingConnection.addClassMapping(DBLayer.getReportingClassMapping());
        reportingConnection.connect();
        schedulerConnection = new SOSHibernateConnection(getOptions().hibernate_configuration_file_scheduler.getValue());
        schedulerConnection.setConnectionIdentifier("scheduler");
        schedulerConnection.setAutoCommit(getOptions().connection_autocommit_scheduler.value());
        schedulerConnection.setIgnoreAutoCommitTransactions(true);
        schedulerConnection.setTransactionIsolation(getOptions().connection_transaction_isolation_scheduler.value());
        schedulerConnection.setUseOpenStatelessSession(true);
        schedulerConnection.addClassMapping(DBLayer.getSchedulerClassMapping());
        schedulerConnection.connect();
    }

    public void exit() {
        if (reportingConnection != null) {
            reportingConnection.disconnect();
        }
        if (schedulerConnection != null) {
            schedulerConnection.disconnect();
        }
    }

    public FactJob execute() throws Exception {
        final String conMethodName = "FactJob::Execute";
        LOGGER.debug(conMethodName);
        try {
            getOptions().checkMandatory();
            LOGGER.debug(getOptions().toString());
            model = new FactModel(reportingConnection, schedulerConnection, getOptions());
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