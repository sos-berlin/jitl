package com.sos.jitl.inventory.db;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;


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
    
    private void initDBConnection(String hibernateConfigPath, boolean autoCommit){
        try {
            SOSHibernateFactory factory = new SOSHibernateFactory(hibernateConfigPath);
            factory.setIdentifier("inventory");
            factory.setAutoCommit(autoCommit);
            factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            factory.addClassMapping(DBLayer.getInventoryClassMapping());
            factory.build();
            connection = factory.openSession();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public List<DBItemInventoryInstance> getInventoryInstances(SOSHibernateSession connection) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("from ").append(DBLayer.DBITEM_INVENTORY_INSTANCES);
        Query<DBItemInventoryInstance> query = connection.createQuery(sql.toString());
        return query.getResultList();
    }
    public static void main(String[] args) throws IOException {
        InventoryCleanup cleanup = new InventoryCleanup();
        String hibernateConfigPath = null;
        String schedulerId = null;
        String hostname = null;
        Integer port = null;
        if (args != null && args.length > 0){
          if (args.length == 2) {
              hibernateConfigPath = args[0];
              if (hibernateConfigPath != null) {
                  cleanup.initDBConnection(hibernateConfigPath, true);
                  if (cleanup.connection != null) {
                      try {
                          List<DBItemInventoryInstance> instances = cleanup.getInventoryInstances(cleanup.connection);
                          cleanup.connection.close();
                          cleanup.connection.getFactory().close();
                          for(DBItemInventoryInstance instance : instances) {
                              System.out.println(String.format("JobSchedulerId=%1$s | Host=%2$s | Port=%3$d", 
                                      instance.getSchedulerId(), instance.getHostname(), instance.getPort()));
                          }
                      } catch (Exception e) {
                          e.printStackTrace();
                          System.exit(1);
                      }
                  }
              } else {
                  System.err.println("No hibernate configuration file found!");
                  System.exit(1);
              }
          } else if (args.length == 4) {
              hibernateConfigPath = args[0];
              schedulerId = args[1];
              hostname = args[2];
              try {
                  port = Integer.parseInt(args[3]);
              } catch ( NumberFormatException e) {
                  System.err.println("Argument" + args[3] + " must be an integer.");
                  System.exit(1);
              }
              if (hibernateConfigPath != null) {
                  cleanup.initDBConnection(hibernateConfigPath, false);
                  if (cleanup.connection != null) {
                      try {
                          cleanup.connection.beginTransaction();
                          cleanup.cleanup(cleanup.connection, schedulerId, hostname, port);
                          cleanup.connection.commit();
                          cleanup.connection.close();
                          cleanup.connection.getFactory().close();
                      } catch (Exception e) {
                          e.printStackTrace();
                          System.exit(1);
                      }
                  }
              } else {
                  System.err.println("Argument" + args[3] + " must be an integer.");
                  System.exit(1);
              }
          }
        } else {
            System.out.println("USAGE:");
            System.out.println("Syntax: java " + InventoryCleanup.class.getName() + " [REPORTING_HIBERNATE_CFG_PATH] "
                    + "[info]");
            System.out.println("OR");
            System.out.println("Syntax: java " + InventoryCleanup.class.getName() + " [REPORTING_HIBERNATE_CFG_PATH] "
                    + "[JOBSCHEDULER_ID] [HOSTNAME] [HTTP_PORT]");
            System.out.println();
            System.out.println("Parameters:");
            System.out.printf("%-30s| %s", "REPORTING_HIBERNATE_CFG_PATH","Path to reporting.hibernate.cfg.xml\n");
            System.out.printf("%-30s| %s", "JOBSCHEDULER_ID","JobSchedulerId to remove from DB\n");
            System.out.printf("%-30s| %s", "HOSTNAME","hostname of the JobScheduler instance to remove from DB\n");
            System.out.printf("%-30s| %s", "HTTP_PORT","HTTP port of the JobScheduler instance to remove from DB\n");
            System.out.printf("%-30s| %s", "info","shows a list of existing JobScheduler instances in the DB\n");

        }
    }
    
}
