package com.sos.jitl.extact;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.jitl.extract.job.ResultSet2CSVJobOptions;
import com.sos.jitl.extract.model.ResultSet2CSVModel;

public class ResultSet2CSVModelTest {

    private ResultSet2CSVJobOptions options;
    private SOSHibernateFactory factory;
    private SOSHibernateSession session;

    public ResultSet2CSVModelTest(ResultSet2CSVJobOptions opt) {
        options = opt;
    }

    public void init() throws Exception {
        factory = new SOSHibernateFactory(options.hibernate_configuration_file.getValue());
        factory.setTransactionIsolation(options.connection_transaction_isolation.value());
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
        String config = "D:/scheduler/config";
        ResultSet2CSVJobOptions opt = new ResultSet2CSVJobOptions();
        opt.hibernate_configuration_file.setValue(config + "/reporting.hibernate.cfg.xml");
        opt.output_file.setValue(config + "/out[date: yyyyMMddHHmmss].csv");
        opt.statement.setValue(
                "SELECT t.* FROM (SELECT @REPORT_START_DATE :='2016-01-01') startDate,(SELECT @REPORT_END_DATE :='2018-02-01') endDate,(SELECT @REPORT_COUNTER := 0) counter,REPORT_INSTALLED_JOB_OBJECTS t");
        ResultSet2CSVModelTest test = new ResultSet2CSVModelTest(opt);
        try {
            test.init();
            ResultSet2CSVModel model = new ResultSet2CSVModel(test.session, test.options);
            model.process();
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        } finally {
            test.exit();
        }
    }

}