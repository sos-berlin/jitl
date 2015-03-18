package com.sos.jitl.reporting.job.inventory;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.model.inventory.InventoryModel;

/**
 * \class InventoryJob - Workerclass for "Inventory"
 * 
 * \brief AdapterClass of InventoryJob for the SOSJobScheduler
 * 
 * This Class InventoryJob is the worker-class.
 * 
 */
public class InventoryJob extends JSJobUtilitiesClass<InventoryJobOptions> {
	private final String conClassName = InventoryJob.class.getSimpleName();
	private static Logger logger = Logger.getLogger(InventoryJob.class);
	private SOSHibernateConnection connection;

	/**
	 * 
	 * \brief InventoryJob
	 * 
	 * \details
	 * 
	 */
	public InventoryJob() {
		super(new InventoryJobOptions());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		connection = new SOSHibernateConnection(Options().hibernate_configuration_file.Value());
		connection.setAutoCommit(Options().connection_autocommit.value());
		connection.setTransactionIsolation(Options().connection_transaction_isolation.value());
		connection.setIgnoreAutoCommitTransactions(true);
		connection.addClassMapping(DBLayer.getInventoryClassMapping());
		connection.connect();
	}

	/**
	 * 
	 */
	public void exit() {
		final String conMethodName = conClassName + "::exit"; //$NON-NLS-1$
		try {
			connection.disconnect();
		} catch (Exception e) {
			logger.warn(String.format("%s:%s", conMethodName, e.toString()));
		}
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of InventoryJob
	 * 
	 * @return
	 */
	public InventoryJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

		logger.debug(conMethodName);

		try {
			Options().CheckMandatory();
			logger.debug(Options().toString());

			InventoryModel model = new InventoryModel(connection,Options());
			model.process();
		} catch (Exception e) {
			logger.error(String.format("%s: %s", conMethodName, e.toString()));
			throw e;
		}

		return this;
	}

	/**
	 * 
	 * \brief Options - returns the InventoryJobOptionClass
	 * 
	 * \details The InventoryJobOptionClass is used as a Container for all
	 * Options (Settings) which are needed.
	 * 
	 * \return InventoryJobOptions
	 * 
	 */
	public InventoryJobOptions Options() {

		@SuppressWarnings("unused")//$NON-NLS-1$
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new InventoryJobOptions();
		}
		return objOptions;
	}
}