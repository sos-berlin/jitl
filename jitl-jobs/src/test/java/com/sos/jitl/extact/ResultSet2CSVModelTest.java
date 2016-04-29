package com.sos.jitl.extact;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.extract.job.ResultSet2CSVJobOptions;
import com.sos.jitl.extract.model.ResultSet2CSVModel;

public class ResultSet2CSVModelTest {

    private ResultSet2CSVJobOptions options;
    private SOSHibernateConnection connection;

    public void init() throws Exception {
        connection = new SOSHibernateConnection(options.hibernate_configuration_file.Value());
        connection.setTransactionIsolation(options.connection_transaction_isolation.value());
        connection.setUseOpenStatelessSession(true);
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
        opt.hibernate_configuration_file.Value(config + "/hibernate_reporting.cfg.xml");
        opt.output_file.Value(config + "/out[date: yyyyMMddHHmmss].csv");
        opt.statement.Value("select * from SCHEDULER_ORDER_HISTORY limit 0,100");
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