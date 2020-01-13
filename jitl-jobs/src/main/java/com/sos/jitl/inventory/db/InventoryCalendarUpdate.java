package com.sos.jitl.inventory.db;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBLayer;

import sos.util.SOSString;

public class InventoryCalendarUpdate {

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            SOSHibernateSession connection = null;
            SOSHibernateFactory factory = null;
            boolean calledFromSetup = false;
            if (args.length > 1 && "-execute-from-setup".equals(args[1])) {
                calledFromSetup = true; 
            }
            //JITL-589 - scheduler_intstall_tables script and relative paths in the hibernate configuration file
            String ud = null;
            String appdata = System.getenv("APPDATA_PATH");
            if (!SOSString.isEmpty(appdata)) {
                ud = System.getProperty("user.dir");
                System.setProperty("user.dir", appdata);
            }
            try {
                if (calledFromSetup) {
                    System.err.print("... update Calendar tables: ");
                }
                Path hibernateConfigPath = Paths.get(args[0]);
                if (!Files.exists(hibernateConfigPath)) {
                    throw new FileNotFoundException(args[0]);
                }
                factory = new SOSHibernateFactory(hibernateConfigPath);
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
                        System.err.println("processed successfully");
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
                if (!SOSString.isEmpty(ud)) {
                    System.setProperty("user.dir", ud);
                }
                if (connection != null) {
                    connection.close();
                }
                if (factory != null) {
                    factory.close();
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
