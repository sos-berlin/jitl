package com.sos.jitl.reporting;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.job.report.AggregationJobOptions;
import com.sos.jitl.reporting.model.report.AggregationModel;

public class AggregationModelTest {

	private SOSHibernateConnection connection;
	private AggregationJobOptions options;

	public AggregationModelTest(AggregationJobOptions opt) {
		options = opt; 
	}

	public void init() throws Exception {
		connection = new SOSHibernateConnection(options.hibernate_configuration_file.Value());
		connection.setAutoCommit(options.connection_autocommit.value());
		connection.setTransactionIsolation(options.connection_transaction_isolation.value());
		connection.setIgnoreAutoCommitTransactions(true);
		connection.addClassMapping(DBLayer.getInventoryClassMapping());
		connection.addClassMapping(DBLayer.getReportingClassMapping());
		connection.connect();
	}

	public void exit() {
		if (connection != null) {
			connection.disconnect();
		}
	}

	public static void main(String[] args) throws Exception {
		String config = "D:/Arbeit/scheduler/jobscheduler_data/re-dell_4646_snap_1_8/config";
		
		AggregationJobOptions opt = new AggregationJobOptions();
		opt.hibernate_configuration_file.Value(config+"/hibernate_reporting.cfg.xml");
		
		AggregationModelTest imt = new AggregationModelTest(opt);

		try {
			imt.init();

			AggregationModel model = new AggregationModel(imt.connection,imt.options);
			model.process();
			
		} catch (Exception ex) {
			throw ex;
		} finally {
			imt.exit();
		}

	}
}
