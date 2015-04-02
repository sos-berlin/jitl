package com.sos.jitl.reporting;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.PropertyValueException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.exception.SQLGrammarException;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.job.report.AggregationJobOptions;
import com.sos.jitl.reporting.model.report.AggregationModel;

public class AggregationModelTestTemp {

	private SOSHibernateConnection connection;
	private AggregationJobOptions options;

	public AggregationModelTestTemp(AggregationJobOptions opt) {
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
	
	
	public DBItemInventoryInstance getInventoryInstance(
			String schedulerId,
			String schedulerHost,
			Long schedulerPort) throws Exception{
		
		try{
			StringBuffer sql = new StringBuffer("from ")
			.append(DBLayer.DBITEM_INVENTORY_INSTANCES)
			.append(" where upper(schedulerIdX) = :schedulerId")
			.append(" and upper(hostname) = :hostname")
			.append(" and port = :port")
			.append(" order by id asc");
			
			Query query = connection.createQuery(sql.toString());
			query.setParameter("schedulerId",schedulerId.toUpperCase());
			query.setParameter("hostname",schedulerHost.toUpperCase());
			query.setParameter("port",schedulerPort);
			
			List<DBItemInventoryInstance> result = query.list();
			if(result.size() > 0){
				return result.get(0);
			}
		}
		catch(Exception ex){
			throw new Exception(AggregationModelTestTemp.getException(ex));
			//throw new Exception(SOSHibernateConnection.getException(ex));
		}
		return null;
	}
	
	public void xxxx() throws Exception{
		try{
			DBItemInventoryInstance ii = new DBItemInventoryInstance();
			connection.save(ii);
		}
		catch(Exception ex){
			throw new Exception(AggregationModelTestTemp.getException(ex));
			//throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}

	public static Throwable getException(Throwable ex){
		if(ex instanceof SQLGrammarException){
			SQLGrammarException sqlGrEx = (SQLGrammarException)ex;
			SQLException sqlEx = sqlGrEx.getSQLException();
						
			return new Exception(String.format("%s [exception: %s, sql: %s]",
					ex.getMessage(),
					sqlEx == null ? "" : sqlEx.getMessage(),
					sqlGrEx.getSQL()),
					sqlEx);
		}/**
		else if(ex instanceof PropertyValueException){
			PropertyValueException pvEx = (PropertyValueException)ex;
			
			return new Exception(String.format("%s [property: %s]",
					ex.getMessage(),
					pvEx.getPropertyName()),
					pvEx);
		}*/
		else if(ex.getCause() != null){
			return ex.getCause();
		}
		return ex;
	}
	
	public static void main(String[] args) throws Throwable {
		String config = "D:/Arbeit/scheduler/jobscheduler_data/re-dell_4646_snap_1_8/config";
		
		AggregationJobOptions opt = new AggregationJobOptions();
		opt.hibernate_configuration_file.Value(config+"/hibernate_reporting.cfg.xml");
		
		AggregationModelTestTemp imt = new AggregationModelTestTemp(opt);

		try {
			imt.init();

			//imt.getInventoryInstance("", "", new Long(12345));
			imt.xxxx();
			//AggregationModel model = new AggregationModel(imt.connection,imt.options);
			//model.process();
			
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());		
		} finally {
			imt.exit();
		}

	}
}
