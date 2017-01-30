package com.sos.jitl.reporting;

import java.sql.Connection;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateStatelessConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.job.report.FactJobOptions;
import com.sos.jitl.reporting.model.report.FactModel;

public class FactModelTest {

    private SOSHibernateConnection reportingConnection;
    private SOSHibernateConnection schedulerConnection;
    private SOSHibernateFactory reportingFactory;
    private SOSHibernateFactory schedulerFactory;

    private FactJobOptions options;

    public FactModelTest(FactJobOptions opt) {
        this.options = opt;
    }

    public void init() throws Exception {
        try {
        	reportingFactory = new SOSHibernateFactory(options.hibernate_configuration_file.getValue());
        	reportingFactory.setConnectionIdentifier("reporting");
        	reportingFactory.setAutoCommit(options.connection_autocommit.value());
        	reportingFactory.setTransactionIsolation(options.connection_transaction_isolation.value());
        	reportingFactory.addClassMapping(DBLayer.getReportingClassMapping());
        	reportingFactory.addClassMapping(DBLayer.getInventoryClassMapping());
        	reportingFactory.build();
            reportingConnection = new SOSHibernateStatelessConnection(reportingFactory);
            reportingConnection.connect();
        } catch (Exception ex) {
            throw new Exception(String.format("reporting connection: %s", ex.toString()));
        }

        try {
        	schedulerFactory = new SOSHibernateFactory(options.hibernate_configuration_file_scheduler.getValue());
        	schedulerFactory.setConnectionIdentifier("scheduler");
        	schedulerFactory.setAutoCommit(options.connection_autocommit_scheduler.value());
        	schedulerFactory.setTransactionIsolation(options.connection_transaction_isolation_scheduler.value());
        	schedulerFactory.addClassMapping(DBLayer.getSchedulerClassMapping());
        	schedulerFactory.build();
        	schedulerConnection = new SOSHibernateStatelessConnection(schedulerFactory);
        	schedulerConnection.connect();
        } catch (Exception ex) {
            throw new Exception(String.format("scheduler connection: %s", ex.toString()));
        }

    }

    public void exit() {
        if (reportingConnection != null) {
            reportingConnection.disconnect();
        }
        if (schedulerConnection != null) {
            schedulerConnection.disconnect();
        }
        if(reportingFactory != null){
        	reportingFactory.close();
        }
        if(schedulerFactory != null){
        	schedulerFactory.close();
        }
    }

    public static void main(String[] args) throws Exception {

        String schedulerId = "re-dell_4444_jobscheduler.1.11x64-snapshot";
        String config = "D:/Arbeit/scheduler/jobscheduler_data/"+schedulerId+"/config";
        FactJobOptions opt = new FactJobOptions();

        opt.hibernate_configuration_file.setValue(config + "/hibernate.cfg.xml");
        opt.connection_autocommit.value(false);

        opt.hibernate_configuration_file_scheduler.setValue(config + "/hibernate.cfg.xml");
        opt.connection_transaction_isolation.value(Connection.TRANSACTION_READ_COMMITTED);
        opt.connection_autocommit_scheduler.value(false);
        opt.connection_transaction_isolation_scheduler.value(Connection.TRANSACTION_READ_COMMITTED);

        opt.current_scheduler_id.setValue(schedulerId);
        opt.current_scheduler_hostname.setValue("re-dell");
        opt.current_scheduler_http_port.setValue("44540");
        opt.max_history_age.setValue("2h");
        opt.force_max_history_age.value(false);
        
        FactModelTest imt = new FactModelTest(opt);

        try {
            imt.init();

            FactModel model = new FactModel(imt.reportingConnection, imt.schedulerConnection, imt.options);
            model.process();
        } catch (Exception ex) {
            throw ex;
        } finally {
            imt.exit();
        }

    }
}
