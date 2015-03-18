package com.sos.jitl.reporting;

import org.apache.log4j.Logger;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.helper.EConfigFileExtensions;
import com.sos.jitl.reporting.job.report.AggregationJobOptions;
import com.sos.jitl.reporting.model.report.AggregationModel;

public class AggregationModelTest {

	private static Logger		logger			= Logger.getLogger(AggregationModelTest.class);
	
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

		AggregationJobOptions opt = new AggregationJobOptions();
		opt.hibernate_configuration_file
				.Value("D:/Arbeit/scheduler/jobscheduler_data/re-dell_4646_snap_1_8/config/hibernate_reporting.cfg.xml");
		
		AggregationModelTest imt = new AggregationModelTest(opt);

		try {
			imt.init();

			String db = imt.connection.getJdbcConnection().getMetaData().getDatabaseProductName();
			
			System.out.println("DB = "+db);
			
			AggregationModel model = new AggregationModel(imt.connection,imt.options);
			model.process();
	
			System.out.println("end");
			
			System.out.println(EConfigFileExtensions.ORDER.type());
			
		} catch (Exception ex) {
			throw ex;
		} finally {
			imt.exit();
		}

	}
}
