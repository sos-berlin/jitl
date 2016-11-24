package com.sos.jitl.inventory.job;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;

public class InventoryEventUpdateJobJSAdapterClass extends JobSchedulerJobAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEventUpdateJobJSAdapterClass.class);
	private static final String HIBERNATE_CFG = "hibernate.cfg.xml";

	@Override
	public boolean spooler_init() {
        try {
            doProcessing();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
		return super.spooler_init();
	}

	@Override
	public void spooler_exit() {
		// TODO: implement spooler_exit here if needed
		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
		Path hibernateCfgPath = Paths.get(spooler.configuration_directory(), "config", HIBERNATE_CFG);
		String url = spooler.uri();
		LOGGER.debug(url);
        InventoryEventUpdateJob updateJob = new InventoryEventUpdateJob(url, initDbConnection(hibernateCfgPath));
		updateJob.execute();
	}  

	private SOSHibernateConnection initDbConnection(Path hibernateCfgPath) throws Exception {
        SOSHibernateConnection connection = new SOSHibernateConnection(hibernateCfgPath.toString());
        connection.setAutoCommit(false);
        connection.setIgnoreAutoCommitTransactions(true);
        connection.addClassMapping(DBLayer.getInventoryClassMapping());
        connection.connect();
        return connection;
	}
}