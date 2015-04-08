package com.sos.jitl.extract.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.extract.model.ResultSet2CSVModel;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class ResultSet2CSVJob extends JSJobUtilitiesClass<ResultSet2CSVJobOptions> {
	private final String conClassName = ResultSet2CSVJob.class.getSimpleName(); //$NON-NLS-1$
	private static Logger logger = LoggerFactory.getLogger(ResultSet2CSVJob.class); //Logger.getLogger(FactJob.class);
	private SOSHibernateConnection connection;

	/**
	 * 
	 */
	public ResultSet2CSVJob() {
		super(new ResultSet2CSVJobOptions());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		connection = new SOSHibernateConnection(Options().hibernate_configuration_file.Value());
		connection.setTransactionIsolation(Options().connection_transaction_isolation.value());
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
	 * @return
	 * @throws Exception
	 */
	public ResultSet2CSVJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

		logger.debug(conMethodName);

		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
			
			ResultSet2CSVModel model = new ResultSet2CSVModel(connection,Options());
			model.process();
			
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format("%s: %s", conMethodName, e.toString()));
			throw e;			
		}
		
		return this;
	}
	
	/**
	 * 
	 */
	public ResultSet2CSVJobOptions Options() {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new ResultSet2CSVJobOptions();
		}
		return objOptions;
	}

}  