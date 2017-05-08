package com.sos.jitl.inventory.db;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSException;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;

import sos.xml.SOSXMLXPath;


public class InventoryCleanup {
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryCleanup.class);
    private SOSHibernateSession connection;
    
    public void cleanup(SOSHibernateSession connection, String schedulerId, String hostName, Integer port) throws Exception {
        Long instanceId = null;
        DBLayerInventory inventoryLayer = new DBLayerInventory(connection);
        DBItemInventoryInstance instance = inventoryLayer.getInventoryInstance(schedulerId, hostName, port);
        instanceId = instance.getId();
        int deletedFiles = deleteItemsFromTable(instanceId, DBLayer.DBITEM_INVENTORY_FILES);
        LOGGER.debug(String.format("Number of Items deleted from %1$s table: %2$d", 
                DBLayer.TABLE_INVENTORY_FILES, deletedFiles));
        int deletedAgentClusterMembers = deleteItemsFromTable(instanceId, DBLayer.DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS);
        LOGGER.debug(String.format("Number of Items deleted from %1$s table: %2$d", 
                DBLayer.TABLE_INVENTORY_AGENT_CLUSTERMEMBERS, 
                deletedAgentClusterMembers));
        int deletedAgentClusters = deleteItemsFromTable(instanceId, DBLayer.DBITEM_INVENTORY_AGENT_CLUSTER);
        LOGGER.debug(String.format("Number of Items deleted from %1$s table: %2$d", 
                DBLayer.TABLE_INVENTORY_AGENT_CLUSTER, 
                deletedAgentClusters));
        int deletedAgentInstances = deleteItemsFromTable(instanceId, DBLayer.DBITEM_INVENTORY_AGENT_INSTANCES);
        LOGGER.debug(String.format("Number of Items deleted from %1$s table: %2$d", 
                DBLayer.TABLE_INVENTORY_AGENT_INSTANCES, 
                deletedAgentInstances));
        int deletedJobChainNodes = deleteItemsFromTable(instanceId, DBLayer.DBITEM_INVENTORY_JOB_CHAIN_NODES);
        LOGGER.debug(String.format("Number of Items deleted from %1$s table: %2$d", 
                DBLayer.TABLE_INVENTORY_JOB_CHAIN_NODES, 
                deletedJobChainNodes));
        int deletedJobChains = deleteItemsFromTable(instanceId, DBLayer.DBITEM_INVENTORY_JOB_CHAINS);
        LOGGER.debug(String.format("Number of Items deleted from %1$s table: %2$d", 
                DBLayer.TABLE_INVENTORY_JOB_CHAINS, 
                deletedJobChains));
        int deletedOrders = deleteItemsFromTable(instanceId, DBLayer.DBITEM_INVENTORY_ORDERS);
        LOGGER.debug(String.format("Number of Items deleted from %1$s table: %2$d", 
                DBLayer.TABLE_INVENTORY_ORDERS, 
                deletedOrders));
        int deletedSchedules = deleteItemsFromTable(instanceId, DBLayer.DBITEM_INVENTORY_SCHEDULES);
        LOGGER.debug(String.format("Number of Items deleted from %1$s table: %2$d", 
                DBLayer.TABLE_INVENTORY_SCHEDULES, 
                deletedSchedules));
        int deletedProcessClasses = deleteItemsFromTable(instanceId, DBLayer.DBITEM_INVENTORY_PROCESS_CLASSES);
        LOGGER.debug(String.format("Number of Items deleted from %1$s table: %2$d", 
                DBLayer.TABLE_INVENTORY_PROCESS_CLASSES, 
                deletedProcessClasses));
        int deletedAppliedLocks = deleteAppliedLocks(instanceId);
        LOGGER.debug(String.format("Number of Items deleted from %1$s table: %2$d", 
                DBLayer.TABLE_INVENTORY_APPLIED_LOCKS, 
                deletedAppliedLocks));
        int deletedJobs = deleteItemsFromTable(instanceId, DBLayer.DBITEM_INVENTORY_JOBS);
        LOGGER.debug(String.format("Number of Items deleted from %1$s table: %2$d", 
                DBLayer.TABLE_INVENTORY_JOBS, 
                deletedJobs));
        int deletedLocks = deleteItemsFromTable(instanceId, DBLayer.DBITEM_INVENTORY_LOCKS);
        LOGGER.debug(String.format("Number of Items deleted from %1$s table: %2$d", 
                DBLayer.TABLE_INVENTORY_LOCKS, 
                deletedLocks));
        connection.delete(instance);
        LOGGER.debug(String.format("instance with id %1$d deleted from %2$s table",
                instanceId,
                DBLayer.TABLE_INVENTORY_INSTANCES));
    }
    
    private int deleteItemsFromTable(Long instanceId, String tableName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ").append(tableName);
        sql.append(" where instanceId = :instanceId");
        Query query = connection.createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return query.executeUpdate();
    }
    
    private int deleteAppliedLocks (Long instanceId) throws Exception {
        StringBuilder sql = new StringBuilder();
//        DELETE FROM INVENTORY_APPLIED_LOCKS WHERE  JOB_ID IN (
//                SELECT ID FROM INVENTORY_JOBS WHERE INSTANCE_ID = @instanceId
//            ) AND LOCK_ID IN (
//                SELECT ID FROM INVENTORY_LOCKS WHERE INSTANCE_ID = @instanceId
//            );
        sql.append("delete from ").append(DBLayer.DBITEM_INVENTORY_APPLIED_LOCKS);
        sql.append(" where jobId in ");
        sql.append("(select id from ");
        sql.append(DBLayer.DBITEM_INVENTORY_JOBS);
        sql.append(" where instanceId = :instanceId) ");
        sql.append("and lockId in ");
        sql.append("(select id from ");
        sql.append(DBLayer.DBITEM_INVENTORY_LOCKS);
        sql.append(" where instanceId = :instanceId) ");
        Query query = connection.createQuery(sql.toString());
        query.setParameter("instanceId", instanceId);
        return query.executeUpdate();
    }
    
    private void initDBConnection(Path hibernateConfigPath, boolean autoCommit){
        SOSHibernateFactory factory = new SOSHibernateFactory(hibernateConfigPath);
        factory.setIdentifier("inventory");
        factory.setAutoCommit(autoCommit);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getInventoryClassMapping());
        factory.build();
        connection = factory.openSession();
    }
    
    public List<DBItemInventoryInstance> getInventoryInstances(SOSHibernateSession connection) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ").append(DBLayer.DBITEM_INVENTORY_INSTANCES);
        Query<DBItemInventoryInstance> query = connection.createQuery(sql.toString());
        return query.getResultList();
    }
    
    public String[] getArgsForUninstaller(String schedulerId, String schedulerData) throws Exception {
        String[] args = new String[4];
        Path schedulerDataPath = Paths.get(schedulerData);
        Path hibernateConfigPath = schedulerDataPath.resolve("config/reporting.hibernate.cfg.xml");
        args[0] = hibernateConfigPath.toString();
        args[1] = schedulerId;
        args[2] = InetAddress.getLocalHost().getHostName();
        Path schedulerXml = schedulerDataPath.resolve("config/scheduler.xml");
        if (Files.notExists(schedulerXml)) {
            throw new FileNotFoundException(schedulerXml.toString());
        }
        SOSXMLXPath xpath = new SOSXMLXPath(schedulerXml);
        args[3] = xpath.selectSingleNodeValue("/spooler/config/@http_port");
        return args;
    }
    
    public void cleanup(String[] args) throws Exception {
        Path hibernateConfigPath = Paths.get(args[0]);
        if (Files.notExists(hibernateConfigPath)) {
            throw new FileNotFoundException(hibernateConfigPath.toString());
        }
        String schedulerId = args[1];
        String hostname = args[2];
        Integer port = null;
        try {
            port = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            throw new SOSException("Argument " + args[3] + " must be an integer.");
        }
        initDBConnection(hibernateConfigPath, false);
        if (connection != null) {
            connection.beginTransaction();
            cleanup(connection, schedulerId, hostname, port);
            connection.commit();
            connection.close();
            connection.getFactory().close();
        }
    }
    
    public List<DBItemInventoryInstance> info(String[] args) throws Exception {
        List<DBItemInventoryInstance> instances = null;
        Path hibernateConfigPath = Paths.get(args[0]);
        if (Files.notExists(hibernateConfigPath)) {
            throw new FileNotFoundException(hibernateConfigPath.toString());
        }
        initDBConnection(hibernateConfigPath, true);
        if (connection != null) {
            instances = getInventoryInstances(connection);
            connection.close();
            connection.getFactory().close();
        }
        return instances;
    }
    
    public static void main(String[] args) throws Exception {
        InventoryCleanup cleanup = new InventoryCleanup();
        if (args != null && args.length > 0) {
            try {
                if ("remove".equals(args[0])) {
                    cleanup.cleanup(cleanup.getArgsForUninstaller(args[1], args[2]));
                } else if (args.length == 2) {
                    System.out.printf("%-32s | %-32s | %-5s%n", "JobSchedulerId", "Host", "Port");
                    System.out.println(String.format("%-75s", "-").replace(' ', '-'));
                    for (DBItemInventoryInstance instance : cleanup.info(args)) {
                        System.out.printf("%-32s | %-32s | %5d%n", instance.getSchedulerId(), instance
                                .getHostname(), instance.getPort());
                    }
                } else if (args.length == 4) {
                    cleanup.cleanup(args);
                }
            } catch (Exception e) {
                System.err.println(e.toString());
                e.printStackTrace(System.err);
                System.exit(1);
            }
        } else {
            System.out.println("USAGE:");
            System.out.println("Syntax: java " + InventoryCleanup.class.getName() + " REPORTING_HIBERNATE_CFG_PATH " + "info");
            System.out.println("OR");
            System.out.println("Syntax: java " + InventoryCleanup.class.getName() + " REPORTING_HIBERNATE_CFG_PATH "
                    + "JOBSCHEDULER_ID HOSTNAME HTTP_PORT");
            System.out.println();
            System.out.println("Parameters:");
            System.out.printf("%-30s| %s%n", "REPORTING_HIBERNATE_CFG_PATH", "Path to reporting.hibernate.cfg.xml");
            System.out.printf("%-30s| %s%n", "JOBSCHEDULER_ID", "JobSchedulerId to remove from DB");
            System.out.printf("%-30s| %s%n", "HOSTNAME", "hostname of the JobScheduler instance to remove from DB");
            System.out.printf("%-30s| %s%n", "HTTP_PORT", "HTTP port of the JobScheduler instance to remove from DB");
            System.out.printf("%-30s| %s%n", "info", "shows a list of existing JobScheduler instances in the DB");

        }
        System.exit(0);
    }
    
}
