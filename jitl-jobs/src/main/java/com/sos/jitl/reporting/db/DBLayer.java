package com.sos.jitl.reporting.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.ClassList;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jade.db.DBItemYadeFiles;
import com.sos.jade.db.DBItemYadeProtocols;
import com.sos.jade.db.DBItemYadeTransfers;
import com.sos.jitl.dailyplan.db.DailyPlanDBItem;
import com.sos.jitl.joc.db.JocConfigurationDbItem;
import com.sos.jitl.schedulerhistory.db.SchedulerOrderHistoryDBItem;
import com.sos.jitl.schedulerhistory.db.SchedulerOrderHistoryLogDBItemPostgres;
import com.sos.jitl.schedulerhistory.db.SchedulerOrderStepHistoryDBItem;
import com.sos.jitl.schedulerhistory.db.SchedulerTaskHistoryDBItem;
import com.sos.jitl.schedulerhistory.db.SchedulerTaskHistoryLogDBItemPostgres;

import sos.jadehistory.db.JadeFilesHistoryDBItem;
import sos.jadehistory.db.JadeFilesDBItem;

public class DBLayer {

    final Logger logger = LoggerFactory.getLogger(DBLayer.class);

    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** Table INVENTORY_INSTANCES */
    public static final String DBITEM_INVENTORY_INSTANCES = DBItemInventoryInstance.class.getSimpleName();
    public static final String TABLE_INVENTORY_INSTANCES = "INVENTORY_INSTANCES";
    public static final String TABLE_INVENTORY_INSTANCES_SEQUENCE = "REPORTING_II_ID_SEQ";

    /** Table INVENTORY_FILES */
    public static final String DBITEM_INVENTORY_FILES = DBItemInventoryFile.class.getSimpleName();
    public static final String TABLE_INVENTORY_FILES = "INVENTORY_FILES";
    public static final String TABLE_INVENTORY_FILES_SEQUENCE = "REPORTING_IF_ID_SEQ";

    /** Table INVENTORY_JOBS */
    public static final String DBITEM_INVENTORY_JOBS = DBItemInventoryJob.class.getSimpleName();
    public static final String TABLE_INVENTORY_JOBS = "INVENTORY_JOBS";
    public static final String TABLE_INVENTORY_JOBS_SEQUENCE = "REPORTING_IJ_ID_SEQ";

    /** Table INVENTORY_JOB_CHAINS */
    public static final String DBITEM_INVENTORY_JOB_CHAINS = DBItemInventoryJobChain.class.getSimpleName();
    public static final String TABLE_INVENTORY_JOB_CHAINS = "INVENTORY_JOB_CHAINS";
    public static final String TABLE_INVENTORY_JOB_CHAINS_SEQUENCE = "REPORTING_IJC_ID_SEQ";

    /** Table INVENTORY_JOB_CHAIN_NODES */
    public static final String DBITEM_INVENTORY_JOB_CHAIN_NODES = DBItemInventoryJobChainNode.class.getSimpleName();
    public static final String TABLE_INVENTORY_JOB_CHAIN_NODES = "INVENTORY_JOB_CHAIN_NODES";
    public static final String TABLE_INVENTORY_JOB_CHAIN_NODES_SEQUENCE = "REPORTING_IJCN_ID_SEQ";

    /** Table INVENTORY_JOB_CHAIN_ORDERS */
    public static final String DBITEM_INVENTORY_ORDERS = DBItemInventoryOrder.class.getSimpleName();
    public static final String TABLE_INVENTORY_ORDERS = "INVENTORY_ORDERS";
    public static final String TABLE_INVENTORY_ORDERS_SEQUENCE = "REPORTING_IO_ID_SEQ";
    
    /** Table REPORTING_TRIGGERS */
    public static final String DBITEM_REPORT_TRIGGERS = DBItemReportTrigger.class.getSimpleName();
    public static final String TABLE_REPORT_TRIGGERS = "REPORTING_TRIGGERS";
    public static final String TABLE_REPORT_TRIGGERS_SEQUENCE = "REPORTING_SEQ_RT";
 
    /** Table REPORTING_EXECUTIONS */
    public static final String DBITEM_REPORT_EXECUTIONS = DBItemReportExecution.class.getSimpleName();
    public static final String TABLE_REPORT_EXECUTIONS = "REPORTING_EXECUTIONS";
    public static final String TABLE_REPORT_EXECUTIONS_SEQUENCE = "REPORTING_SEQ_RE";

    /** Table REPORTING_TASKS */
    public static final String DBITEM_REPORT_TASKS = DBItemReportTask.class.getSimpleName();
    public static final String TABLE_REPORT_TASKS = "REPORTING_TASKS";
    public static final String TABLE_REPORT_TASKS_SEQUENCE = "REPORTING_SEQ_RTS";

    /** Table REPORTING_EXECUTION_DATES */
    public static final String DBITEM_REPORT_EXECUTION_DATES = DBItemReportExecutionDate.class.getSimpleName();
    public static final String TABLE_REPORT_EXECUTION_DATES = "REPORTING_EXECUTION_DATES";
    public static final String TABLE_REPORT_EXECUTION_DATES_SEQUENCE = "REPORTING_SEQ_RED";

    /** Table REPORTING_VARIABLES */
    public static final String DBITEM_REPORT_VARIABLES = DBItemReportVariable.class.getSimpleName();
    public static final String TABLE_REPORT_VARIABLES = "REPORTING_VARIABLES";
    
    /** Table INVENTORY_OPERATING_SYSTEM */
    public static final String DBITEM_INVENTORY_OPERATING_SYSTEMS = DBItemInventoryOperatingSystem.class.getSimpleName();
    public static final String TABLE_INVENTORY_OPERATING_SYSTEMS = "INVENTORY_OPERATING_SYSTEMS";
    public static final String TABLE_INVENTORY_OPERATING_SYSTEMS_SEQUENCE = "REPORTING_IOS_ID_SEQ";
    
    /** Table INVENTORY_PROCESS_CLASSES */
    public static final String DBITEM_INVENTORY_PROCESS_CLASSES = DBItemInventoryProcessClass.class.getSimpleName();
    public static final String TABLE_INVENTORY_PROCESS_CLASSES = "INVENTORY_PROCESS_CLASSES";
    public static final String TABLE_INVENTORY_PROCESS_CLASSES_SEQUENCE = "REPORTING_IPC_ID_SEQ";
    
    /** Table INVENTORY_AGENT_CLUSTER */
    public static final String DBITEM_INVENTORY_AGENT_CLUSTER = DBItemInventoryAgentCluster.class.getSimpleName();
    public static final String TABLE_INVENTORY_AGENT_CLUSTER = "INVENTORY_AGENT_CLUSTERS";
    public static final String TABLE_INVENTORY_AGENT_CLUSTER_SEQUENCE = "REPORTING_IAC_ID_SEQ";
    
    /** Table INVENTORY_AGENT_CLUSTER_MEMBERS */
    public static final String DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS = DBItemInventoryAgentClusterMember.class.getSimpleName();
    public static final String TABLE_INVENTORY_AGENT_CLUSTERMEMBERS = "INVENTORY_AGENT_CLUSTERMEMBERS";
    public static final String TABLE_INVENTORY_AGENT_CLUSTERMEMBERS_SEQUENCE = "REPORTING_IACM_ID_SEQ";
    
    /** Table INVENTORY_AGENT_INSTANCES */
    public static final String DBITEM_INVENTORY_AGENT_INSTANCES = DBItemInventoryAgentInstance.class.getSimpleName();
    public static final String TABLE_INVENTORY_AGENT_INSTANCES = "INVENTORY_AGENT_INSTANCES";
    public static final String TABLE_INVENTORY_AGENT_INSTANCES_SEQUENCE = "REPORTING_IAI_ID_SEQ";
    
    /** Table INVENTORY_SCHEDULES */
    public static final String DBITEM_INVENTORY_SCHEDULES = DBItemInventorySchedule.class.getSimpleName();
    public static final String TABLE_INVENTORY_SCHEDULES = "INVENTORY_SCHEDULES";
    public static final String TABLE_INVENTORY_SCHEDULES_SEQUENCE = "REPORTING_IS_ID_SEQ";
    
    /** Table INVENTORY_LOCKS */
    public static final String DBITEM_INVENTORY_LOCKS = DBItemInventoryLock.class.getSimpleName();
    public static final String TABLE_INVENTORY_LOCKS = "INVENTORY_LOCKS";
    public static final String TABLE_INVENTORY_LOCKS_SEQUENCE = "REPORTING_IL_ID_SEQ";
    
    /** Table INVENTORY_APPLIED_LOCKS */
    public static final String DBITEM_INVENTORY_APPLIED_LOCKS = DBItemInventoryAppliedLock.class.getSimpleName();
    public static final String TABLE_INVENTORY_APPLIED_LOCKS = "INVENTORY_APPLIED_LOCKS";
    public static final String TABLE_INVENTORY_APPLIED_LOCKS_SEQUENCE = "REPORTING_IAL_ID_SEQ";

    /** Table AUIDT_LOG */
    public static final String DBITEM_AUDIT_LOG = DBItemAuditLog.class.getSimpleName();
    public static final String TABLE_AUDIT_LOG = "AUDIT_LOG";
    public static final String TABLE_AUDIT_LOG_SEQUENCE = "AUDIT_LOG_SEQ";
    
    /** Table JOC_CONFIGURATIONS */
    public static final String DBITEM_JOC_CONFIGURATIONS = JocConfigurationDbItem.class.getSimpleName();
    public static final String TABLE_JOC_CONFIGURATIONS = "JOC_CONFIGURATIONS";
    public static final String TABLE_JOC_CONFIGURATIONS_SEQUENCE = "JOC_CONFIGURATIONS_SEQ";
    
    /** Table CALENDARS */
    public static final String DBITEM_CALENDARS = DBItemCalendar.class.getSimpleName();
    public static final String TABLE_CALENDARS = "CALENDARS";
    public static final String TABLE_CALENDARS_SEQUENCE = "REPORTING_C_ID_SEQ";

    public static final String TABLE_DAILY_PLAN_SEQUENCE = "DAILY_PLAN_ID_SEQ";
    
    public static final String DEFAULT_NAME = ".";
    public static final String DEFAULT_FOLDER = "/";
    public static final Long DEFAULT_ID = 0L;

    private SOSHibernateSession session;
 
    public DBLayer(SOSHibernateSession session) {
        this.session = session;
    }
  
    public SOSHibernateSession getSession() {
        return this.session;
    }

    public static ClassList getInventoryClassMapping() {
        ClassList cl = new ClassList();
        cl.add(DBItemInventoryFile.class);
        cl.add(DBItemInventoryInstance.class);
        cl.add(DBItemInventoryJobChainNode.class);
        cl.add(DBItemInventoryOrder.class);
        cl.add(DBItemInventoryJobChain.class);
        cl.add(DBItemInventoryJob.class);
        cl.add(DBItemInventoryOperatingSystem.class);
        cl.add(DBItemInventoryProcessClass.class);
        cl.add(DBItemInventoryAgentCluster.class);
        cl.add(DBItemInventoryAgentClusterMember.class);
        cl.add(DBItemInventoryAgentInstance.class);
        cl.add(DBItemInventorySchedule.class);
        cl.add(DBItemInventoryLock.class);
        cl.add(DBItemInventoryAppliedLock.class);
        cl.add(DBItemAuditLog.class);
        return cl;
    }

    public static ClassList getReportingClassMapping() {
        ClassList cl = new ClassList();
        cl.add(DBItemReportTask.class);
        cl.add(DBItemReportTrigger.class);
        cl.add(DBItemReportExecution.class);
        cl.add(DBItemReportExecutionDate.class);
        cl.add(DBItemReportVariable.class);
        cl.add(DailyPlanDBItem.class);
        cl.add(JocConfigurationDbItem.class);
        cl.add(DBItemCalendar.class);
        return cl;
    }

    public static ClassList getSchedulerClassMapping() {
        ClassList cl = new ClassList();
        cl.add(DBItemSchedulerHistoryOrderStepReporting.class);
        cl.add(SchedulerTaskHistoryDBItem.class);
        cl.add(SchedulerTaskHistoryLogDBItemPostgres.class);
        cl.add(SchedulerOrderHistoryDBItem.class);
        cl.add(SchedulerOrderHistoryLogDBItemPostgres.class);
        cl.add(SchedulerOrderStepHistoryDBItem.class);
        cl.add(DBItemSchedulerHistory.class);
        cl.add(DBItemSchedulerOrderStepHistory.class);
        return cl;
    }

    public static ClassList getYadeClassMapping() {
        ClassList cl = new ClassList();
        cl.add(JadeFilesHistoryDBItem.class);
        cl.add(JadeFilesDBItem.class);
        cl.add(DBItemYadeFiles.class);
        cl.add(DBItemYadeProtocols.class);
        cl.add(DBItemYadeTransfers.class);
        return cl;
    }

}