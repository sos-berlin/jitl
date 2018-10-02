package com.sos.jitl.inventory.db;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBLayer;

public class InventoryCalendarUpdate {

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            SOSHibernateSession connection = null;
            boolean calledFromSetup = false;
            if (args.length > 1 && "-execute-from-setup".equals(args[1])) {
                calledFromSetup = true; 
            }
            try {
                if (calledFromSetup) {
                    System.err.print("... update Calendar tables: ");
                }
                Path hibernateConfigPath = Paths.get(args[0]);
                if (!Files.exists(hibernateConfigPath)) {
                    throw new FileNotFoundException(args[0]);
                }
                SOSHibernateFactory factory = new SOSHibernateFactory(hibernateConfigPath);
                factory.setAutoCommit(false);
                //factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                factory.addClassMapping(DBLayer.getInventoryClassMapping());
                factory.build();
                connection = factory.openStatelessSession(InventoryCalendarUpdate.class.getName());
                connection.beginTransaction();
                DBLayerInventory dbLayer = new DBLayerInventory(connection);
                boolean somethingToDo = dbLayer.repairCalendars();
                connection.commit();
                if (calledFromSetup) {
                    if (somethingToDo) {
                        System.err.println("successful processed");
                    } else {
                        System.err.println("nothing to do");
                    }
                }
            } catch (Exception e) {
                if (calledFromSetup) {
                    System.err.println(e.toString());
                } else {
                    e.printStackTrace(System.err);
                }
                System.exit(1);
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        } else {
            System.err.println("USAGE: java " + InventoryCalendarUpdate.class.getName() + " /path/to/reporting.hibernate.cfg.xml");
            System.err.println();
            System.exit(1);
        }
        System.exit(0);
    }

}
