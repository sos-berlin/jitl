package com.sos.jitl.reporting.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.ClassList;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.db.SchedulerInstancesDBItem;
import com.sos.scheduler.history.db.SchedulerOrderHistoryDBItem;
import com.sos.scheduler.history.db.SchedulerOrderStepHistoryDBItem;
import com.sos.scheduler.history.db.SchedulerTaskHistoryDBItem;

public class DBLayer{
	final Logger logger = LoggerFactory.getLogger(DBLayer.class);

	public final static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	/** Table INVENTORY_INSTANCES */
	public final static String DBITEM_INVENTORY_INSTANCES = DBItemInventoryInstance.class.getSimpleName();
	public final static String TABLE_INVENTORY_INSTANCES = "INVENTORY_INSTANCES";
	public final static String TABLE_INVENTORY_INSTANCES_SEQUENCE = "REPORTING_II_ID_SEQ";
	
	/** Table INVENTORY_FILES */
	public final static String DBITEM_INVENTORY_FILES = DBItemInventoryFile.class.getSimpleName();
	public final static String TABLE_INVENTORY_FILES = "INVENTORY_FILES";
	public final static String TABLE_INVENTORY_FILES_SEQUENCE = "REPORTING_IF_ID_SEQ";
	
	/** Table INVENTORY_JOBS */
	public final static String DBITEM_INVENTORY_JOBS = DBItemInventoryJob.class.getSimpleName();
	public final static String TABLE_INVENTORY_JOBS = "INVENTORY_JOBS";
	public final static String TABLE_INVENTORY_JOBS_SEQUENCE = "REPORTING_IJ_ID_SEQ";
	
	/** Table INVENTORY_JOB_CHAINS */
	public final static String DBITEM_INVENTORY_JOB_CHAINS = DBItemInventoryJobChain.class.getSimpleName();
	public final static String TABLE_INVENTORY_JOB_CHAINS = "INVENTORY_JOB_CHAINS";
	public final static String TABLE_INVENTORY_JOB_CHAINS_SEQUENCE = "REPORTING_IJC_ID_SEQ";
	
	/** Table INVENTORY_JOB_CHAIN_NODES */
	public final static String DBITEM_INVENTORY_JOB_CHAIN_NODES = DBItemInventoryJobChainNode.class.getSimpleName();
	public final static String TABLE_INVENTORY_JOB_CHAIN_NODES = "INVENTORY_JOB_CHAIN_NODES";
	public final static String TABLE_INVENTORY_JOB_CHAIN_NODES_SEQUENCE = "REPORTING_IJCN_ID_SEQ";
	
	/** Table INVENTORY_JOB_CHAIN_ORDERS */
	public final static String DBITEM_INVENTORY_ORDERS = DBItemInventoryOrder.class.getSimpleName();
	public final static String TABLE_INVENTORY_ORDERS = "INVENTORY_ORDERS";
	public final static String TABLE_INVENTORY_ORDERS_SEQUENCE = "REPORTING_IO_ID_SEQ";
	
	/** Table REPORT_TRIGGERS */
	public final static String DBITEM_REPORT_TRIGGERS = DBItemReportTrigger.class.getSimpleName();
	public final static String TABLE_REPORT_TRIGGERS = "REPORT_TRIGGERS";
	public final static String TABLE_REPORT_TRIGGERS_SEQUENCE = "REPORTING_RT_ID_SEQ";
	
	/** Table REPORT_TRIGGER_RESULTS */
	public final static String DBITEM_REPORT_TRIGGER_RESULTS = DBItemReportTriggerResult.class.getSimpleName();
	public final static String TABLE_REPORT_TRIGGER_RESULTS = "REPORT_TRIGGER_RESULTS";
	public final static String TABLE_REPORT_TRIGGER_RESULTS_SEQUENCE = "REPORTING_RTR_ID_SEQ";
		
	/** Table REPORT_EXECUTIONS */
	public final static String DBITEM_REPORT_EXECUTIONS = DBItemReportExecution.class.getSimpleName();
	public final static String TABLE_REPORT_EXECUTIONS = "REPORT_EXECUTIONS";
	public final static String TABLE_REPORT_EXECUTIONS_SEQUENCE = "REPORTING_RE_ID_SEQ";
	
	/** Table REPORT_EXECUTION_DATES */
	public final static String DBITEM_REPORT_EXECUTION_DATES = DBItemReportExecutionDate.class.getSimpleName();
	public final static String TABLE_REPORT_EXECUTION_DATES = "REPORT_EXECUTION_DATES";
	public final static String TABLE_REPORT_EXECUTION_DATES_SEQUENCE = "REPORTING_RED_ID_SEQ";
	
	/** Table SCHEDULER_VARIABLES */
	public final static String DBITEM_SCHEDULER_VARIABLES = DBItemSchedulerVariableReporting.class.getSimpleName();
	public final static String TABLE_SCHEDULER_VARIABLES = "SCHEDULER_VARIABLES";
	public final static String TABLE_SCHEDULER_VARIABLES_REPORTING_VARIABLE = "reporting_date";
	
	private SOSHibernateConnection connection;
	
	public DBLayer(SOSHibernateConnection conn){
		connection = conn;
	}
	
	public SOSHibernateConnection getConnection(){
		return connection;
	}

	/**
	 * 
	 * @return
	 */
	public static ClassList getInventoryClassMapping(){
		ClassList cl = new ClassList();
		
		cl.add(DBItemInventoryFile.class);
		cl.add(DBItemInventoryInstance.class);
		cl.add(DBItemInventoryJobChainNode.class);
		cl.add(DBItemInventoryOrder.class);
		cl.add(DBItemInventoryJobChain.class);
		cl.add(DBItemInventoryJob.class);
		return cl;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static ClassList getReportingClassMapping(){
		ClassList cl = new ClassList();
		
		cl.add(DBItemReportTrigger.class);
		cl.add(DBItemReportExecution.class);
		cl.add(DBItemReportTriggerResult.class);
		cl.add(DBItemReportExecutionDate.class);

		return cl;
	}
	
	/**
	 * 
	 * @return
	 */
	public static ClassList getSchedulerClassMapping(){
		ClassList cl = new ClassList();
		
		cl.add(DBItemSchedulerVariableReporting.class);
		cl.add(DBItemSchedulerHistoryOrderStepReporting.class);
		
		cl.add(SchedulerInstancesDBItem.class);
		cl.add(SchedulerTaskHistoryDBItem.class);
		cl.add(SchedulerOrderHistoryDBItem.class);
		cl.add(SchedulerOrderStepHistoryDBItem.class);
		return cl;
	}
}
