package com.sos.jitl.reporting.job.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.model.report.AggregationModel;

/**
 * \class AggregationJob - Workerclass for "Aggregation"
 * 
 * \brief AdapterClass of AggregationJob for the SOSJobScheduler
 * 
 * This Class AggregationJob is the worker-class.
 * 
 */
public class AggregationJob extends JSJobUtilitiesClass<AggregationJobOptions> {
	private final String conClassName = AggregationJob.class.getSimpleName(); //$NON-NLS-1$
	private static Logger logger = LoggerFactory.getLogger(AggregationJob.class); //Logger.getLogger(FactJob.class);
	private SOSHibernateConnection connection; 
	
	/**
	 * 
	 * \brief AggregationJob
	 * 
	 * \details
	 * 
	 */
	public AggregationJob() {
		super(new AggregationJobOptions());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		
		logger.debug(conMethodName);
		
		try{
			connection = new SOSHibernateConnection(Options().hibernate_configuration_file.Value());
			connection.setAutoCommit(Options().connection_autocommit.value());
			connection.setIgnoreAutoCommitTransactions(true);
			connection.setTransactionIsolation(Options().connection_transaction_isolation.value());
			connection.setUseOpenStatelessSession(true);
			connection.addClassMapping(DBLayer.getInventoryClassMapping());
			connection.addClassMapping(DBLayer.getReportingClassMapping());
			connection.connect();
		}
		catch(Exception ex){
			throw new Exception(String.format("reporting connection: %s",
					ex.toString()));
		}
	}

	/**
	 * 
	 */
	public void exit() {
		final String conMethodName = conClassName + "::exit"; //$NON-NLS-1$
		
		logger.debug(conMethodName);
		try {
			connection.disconnect();
		} catch (Exception e) {
			logger.warn(String.format("%s:%s", conMethodName, e.toString()));
		}
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of AggregationJob
	 * 
	 * @return
	 */
	public AggregationJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

		logger.debug(conMethodName);

		try {
			Options().CheckMandatory();
			logger.debug(Options().toString());

			AggregationModel model = new AggregationModel(connection,Options());
			model.process();
		} catch (Exception e) {
			logger.error(String.format("%s: %s", conMethodName, e.toString()));
			throw e;
		}

		return this;
	}

	/**
	 * 
	 * \brief Options - returns the AggregationJobOptionClass
	 * 
	 * \details The AggregationJobOptionClass is used as a Container for all
	 * Options (Settings) which are needed.
	 * 
	 * \return AggregationJobOptions
	 * 
	 */
	public AggregationJobOptions Options() {

		@SuppressWarnings("unused")//$NON-NLS-1$
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new AggregationJobOptions();
		}
		return objOptions;
	}
}