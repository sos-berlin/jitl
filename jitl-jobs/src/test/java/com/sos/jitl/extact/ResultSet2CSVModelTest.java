package com.sos.jitl.extact;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.extract.job.ResultSet2CSVJobOptions;
import com.sos.jitl.extract.model.ResultSet2CSVModel;

public class ResultSet2CSVModelTest {
	private ResultSet2CSVJobOptions options;
	private SOSHibernateConnection connection;
	
	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		connection = new SOSHibernateConnection(options.hibernate_configuration_file.Value());
		connection.setTransactionIsolation(options.connection_transaction_isolation.value());
		connection.setUseOpenStatelessSession(true);
		connection.connect();
	}

	/**
	 * 
	 */
	public void exit() {
		if (connection != null) {
			connection.disconnect();
		}
	}
	
	/**
	 * 
	 * @param opt
	 */
	public ResultSet2CSVModelTest(ResultSet2CSVJobOptions opt){
		options = opt;
	}
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		String config = "D:/Arbeit/scheduler/jobscheduler_data/re-dell_4646_snap_1_8/config";
		
		ResultSet2CSVJobOptions opt = new ResultSet2CSVJobOptions();
		opt.hibernate_configuration_file.Value(config+"/hibernate_reporting.cfg.xml");
		opt.output_file.Value(config+"/out[date: yyyyMMddHHmmss].csv");
		//opt.output_file.Value(config+"/out.csv");
		opt.statement.Value("select * from SCHEDULER_ORDER_HISTORY limit 0,100");
		
		//opt.statement.Value("SELECT t.* FROM (SELECT @REPORT_START_DATE :='2014-01-12') startDate, (SELECT @REPORT_END_DATE   :='2016-01-13') endDate, REPORT_INSTALLED_OBJECTS t");
		
		//opt.delimiter.Value(",");
		//opt.skip_header.value(true);
		
		ResultSet2CSVModelTest test = new ResultSet2CSVModelTest(opt);

		try {
			test.init();

			ResultSet2CSVModel model = new ResultSet2CSVModel(test.connection,test.options);
			model.process();
			
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());		
		} finally {
			test.exit();
		}
	}

}
