package com.sos.jitl.reporting;

import java.nio.file.Paths;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.plugin.FactEventHandler;
import com.sos.jitl.reporting.plugin.SchedulerAnswer;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class FactEventHandlerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactEventHandlerTest.class);
	
	
	public static void main(String[] args) throws Exception {
		FactEventHandler f = new FactEventHandler();
		
		SchedulerXmlCommandExecutor xmlExecutor = null;
		VariableSet variables = null;
		
		SOSHibernateConnection connection = new SOSHibernateConnection();
		connection.setAutoCommit(true);
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        connection.setIgnoreAutoCommitTransactions(true);
        connection.addClassMapping(DBLayer.getReportingClassMapping());
		
		String configDir = "D:/Arbeit/scheduler/jobscheduler_data/re-dell_4444_jobscheduler.1.11x64-snapshot/config";
		SchedulerAnswer answer = new SchedulerAnswer();
		answer.setLiveDirectory(Paths.get(configDir+"/live"));
		answer.setHibernateConfigPath(Paths.get(configDir+"/hibernate.cfg.xml"));
		answer.setSchedulerXmlPath(Paths.get(configDir+"/scheduler.xml"));
		
		answer.setHttpPort("40444");
		answer.setMasterUrl("http://re-dell:"+answer.getHttpPort());
		answer.setXml(null);
		answer.setXpath(null);
		
		f.onPrepare(xmlExecutor, variables, connection, answer);
		
		f.onActivate();

		f.close();
	}

}
