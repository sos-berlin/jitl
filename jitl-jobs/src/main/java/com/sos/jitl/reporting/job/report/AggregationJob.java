package com.sos.jitl.reporting.job.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.model.report.AggregationModel;

public class AggregationJob extends JSJobUtilitiesClass<AggregationJobOptions> {

    private final String className = AggregationJob.class.getSimpleName();
    private static Logger logger = LoggerFactory.getLogger(AggregationJob.class);
    private SOSHibernateConnection connection;

    public AggregationJob() {
        super(new AggregationJobOptions());
    }

    public void init() throws Exception {
        try {
            connection = new SOSHibernateConnection(getOptions().hibernate_configuration_file.Value());
            connection.setAutoCommit(getOptions().connection_autocommit.value());
            connection.setIgnoreAutoCommitTransactions(true);
            connection.setTransactionIsolation(getOptions().connection_transaction_isolation.value());
            connection.setUseOpenStatelessSession(true);
            connection.addClassMapping(DBLayer.getInventoryClassMapping());
            connection.addClassMapping(DBLayer.getReportingClassMapping());
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

    public AggregationJob execute() throws Exception {
        final String methodName = className + "::execute";

        logger.debug(methodName);

        try {
            getOptions().checkMandatory();
            logger.debug(getOptions().toString());

            AggregationModel model = new AggregationModel(connection, getOptions());
            model.process();
        } catch (Exception e) {
            logger.error(String.format("%s: %s", methodName, e.toString()));
            throw e;
        }

        return this;
    }

    public AggregationJobOptions getOptions() {
        if (objOptions == null) {
            objOptions = new AggregationJobOptions();
        }
        return objOptions;
    }
}