package com.sos.jitl.reporting;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.job.report.AggregationJobOptions;
import com.sos.jitl.reporting.model.report.AggregationModel;

public class AggregationModelTest {

    private SOSHibernateFactory factory;
    private SOSHibernateSession session;
    
    private AggregationJobOptions options;

    public AggregationModelTest(AggregationJobOptions opt) {
        options = opt;
    }

    public void init() throws Exception {
        
        factory = new SOSHibernateFactory(options.hibernate_configuration_file.getValue());
        factory.setAutoCommit(options.connection_autocommit.value());
        factory.setTransactionIsolation(options.connection_transaction_isolation.value());
        factory.addClassMapping(DBLayer.getInventoryClassMapping());
        factory.addClassMapping(DBLayer.getReportingClassMapping());
        factory.build();
        
        session = factory.openStatelessSession();
    }

    public void exit() {
        if (session != null) {
            session.close();
        }
        if (factory != null) {
            factory.close();
        }
    }

    public static void main(String[] args) throws Exception {
        String schedulerId = "re-dell_4444_jobscheduler.1.11x64-snapshot";
        String config = "D:/Arbeit/scheduler/jobscheduler_data/"+schedulerId+"/config";
        
        AggregationJobOptions opt = new AggregationJobOptions();
        opt.hibernate_configuration_file.setValue(config + "/hibernate.cfg.xml");
        opt.connection_autocommit.value(false);
        opt.current_scheduler_id.setValue(schedulerId);
        opt.current_scheduler_http_port.setValue("40444");
      
        AggregationModelTest imt = new AggregationModelTest(opt);

        try {
            imt.init();

            AggregationModel model = new AggregationModel(imt.session, imt.options);
            model.process();

        } catch (Exception ex) {
            throw ex;
        } finally {
            imt.exit();
        }

    }
}
