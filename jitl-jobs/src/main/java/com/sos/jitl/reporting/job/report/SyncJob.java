package com.sos.jitl.reporting.job.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.model.report.SyncModel;

/**
 * \class SyncJob - Workerclass for "Sync"
 * 
 * \brief AdapterClass of SyncJob for the SOSJobScheduler
 * 
 * This Class SyncJob is the worker-class.
 * 
 */
public class SyncJob extends JSJobUtilitiesClass<SyncJobOptions> {
	private final String conClassName = SyncJob.class.getSimpleName(); //$NON-NLS-1$
	private static Logger logger = LoggerFactory.getLogger(SyncJob.class); //Logger.getLogger(SyncJob.class);
	private SOSHibernateConnection reportingConnection; 
	private SOSHibernateConnection schedulerConnection;
	private SyncModel model;
	
	/**
	 * 
	 * \brief SyncJob
	 * 
	 * \details
	 * 
	 */
	public SyncJob() {
		super(new SyncJobOptions());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		
		logger.debug(conMethodName);
		
		try{
			reportingConnection = new SOSHibernateConnection(Options().hibernate_configuration_file.Value());
			reportingConnection.setConnectionIdentifier("reporting");
			reportingConnection.setAutoCommit(Options().connection_autocommit.value());
			reportingConnection.setIgnoreAutoCommitTransactions(true);
			reportingConnection.setTransactionIsolation(Options().connection_transaction_isolation.value());
			reportingConnection.setUseOpenStatelessSession(true);
			reportingConnection.addClassMapping(DBLayer.getInventoryClassMapping());
			reportingConnection.addClassMapping(DBLayer.getReportingClassMapping());
			reportingConnection.connect();
		}
		catch(Exception ex){
			throw new Exception(String.format("reporting connection: %s",
					ex.toString()));
		}
		
		try{
			schedulerConnection = new SOSHibernateConnection(Options().hibernate_configuration_file_scheduler.Value());
			schedulerConnection.setConnectionIdentifier("scheduler");
			schedulerConnection.setAutoCommit(Options().connection_autocommit_scheduler.value());
			schedulerConnection.setIgnoreAutoCommitTransactions(true);
			schedulerConnection.setTransactionIsolation(Options().connection_transaction_isolation_scheduler.value());
			schedulerConnection.setUseOpenStatelessSession(true);
			schedulerConnection.addClassMapping(DBLayer.getSchedulerClassMapping());
			schedulerConnection.connect();
		}
		catch(Exception ex){
			throw new Exception(String.format("scheduler connection: %s",
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
			reportingConnection.disconnect();
		} catch (Exception e) {
			logger.warn(String.format("%s:%s", conMethodName, e.toString()));
		}
		
		try {
			schedulerConnection.disconnect();
		} catch (Exception e) {
			logger.warn(String.format("%s:%s", conMethodName, e.toString()));
		}
		
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of SyncJob
	 * 
	 * @return
	 */
	public SyncJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

		logger.debug(conMethodName);

		try {
			Options().CheckMandatory();
			logger.debug(Options().toString());

			model = new SyncModel(reportingConnection,schedulerConnection,Options());
			model.process();
		} catch (Exception e) {
			logger.error(String.format("%s: %s", conMethodName, e.toString()));
			throw e;
		}

		return this;
	}
	
	public SyncModel getModel(){
		return model;
	}
	

	/**
	 * 
	 * \brief Options - returns the SyncJobOptionClass
	 * 
	 * \details The SyncJobOptionClass is used as a Container for all
	 * Options (Settings) which are needed.
	 * 
	 * \return SyncJobOptions
	 * 
	 */
	public SyncJobOptions Options() {

		@SuppressWarnings("unused")//$NON-NLS-1$
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new SyncJobOptions();
		}
		return objOptions;
	}
}