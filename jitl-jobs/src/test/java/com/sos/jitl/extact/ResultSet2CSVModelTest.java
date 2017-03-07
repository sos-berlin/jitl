package com.sos.jitl.extact;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateStatelessSession;
import com.sos.jitl.extract.job.ResultSet2CSVJobOptions;
import com.sos.jitl.extract.model.ResultSet2CSVModel;

public class ResultSet2CSVModelTest {

    private ResultSet2CSVJobOptions options;
    private SOSHibernateSession connection;

    public void init() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(options.hibernate_configuration_file.getValue());
        factory.setTransactionIsolation(options.connection_transaction_isolation.value());
        factory.build();
        connection = new SOSHibernateStatelessSession(factory);
        connection.connect();
    }

    public void exit() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public ResultSet2CSVModelTest(ResultSet2CSVJobOptions opt) {
        options = opt;
    }

    public static void main(String[] args) throws Exception {
        String config = "D:/scheduler/config";

        ResultSet2CSVJobOptions opt = new ResultSet2CSVJobOptions();
        opt.hibernate_configuration_file.setValue(config + "/hibernate_reporting.cfg.xml");
        opt.output_file.setValue(config + "/out[date: yyyyMMddHHmmss].csv");
        opt.statement.setValue("select * from SCHEDULER_ORDER_HISTORY limit 0,100");
        ResultSet2CSVModelTest test = new ResultSet2CSVModelTest(opt);
        try {
            test.init();
            ResultSet2CSVModel model = new ResultSet2CSVModel(test.connection, test.options);
            model.process();
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        } finally {
            test.exit();
        }
    }

}