package com.sos.jitl.cleanup;

import com.sos.hibernate.classes.SOSHibernateFactory;

import sos.connection.SOSConnection;

public class SchedulerCleanup {

    public static void showUsage() {
        System.out.println("Usage: hibernateFile age");

        System.out.println("- Remove entries older as n (14) days:");
        System.out.println("      hibernateFile 14");
        System.out.println("");
        System.out.println("- Remove all entries:");
        System.out.println("      hibernateFile 0");
    }

    public static Enum<SOSHibernateFactory.Dbms> getDbms(String hibernateFile) throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(hibernateFile);
        return factory.getDbmsBeforeBuild();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            SchedulerCleanup.showUsage();
            System.exit(0);
            return;
        }

        for (int i = 0; i < args.length; i++) {
            String param = args[i].trim();
            System.out.println(String.format("  %s) %s", i + 1, param));
        }
        String hibernateFile = args[0];
        String age = args[1];
        System.out.println("");
        if (age.equals("0")) {
            System.out.print("Remove all entries");
        } else {
            System.out.print("Remove entries older as " + age + " days");
        }
        System.out.println("");

        SOSConnection conn = null;
        try {
            conn = SOSConnection.createInstance(hibernateFile);
            conn.connect();

            Enum<SOSHibernateFactory.Dbms> dbms = getDbms(hibernateFile);
            String stmt = null;
            if (dbms.equals(SOSHibernateFactory.Dbms.MSSQL)) {
                stmt = "EXEC SCHEDULER_CLEANUP " + age;
            } else if (dbms.equals(SOSHibernateFactory.Dbms.MYSQL)) {
                stmt = "CALL SCHEDULER_CLEANUP(" + age + ")";
            } else if (dbms.equals(SOSHibernateFactory.Dbms.ORACLE)) {
                stmt = "CALL SCHEDULER_CLEANUP(" + age + ")";
            } else if (dbms.equals(SOSHibernateFactory.Dbms.PGSQL)) {
                stmt = "SELECT SCHEDULER_CLEANUP(" + age + ")";
            }

            System.out.println("Execute " + dbms + ": " + stmt);
            conn.execute(stmt);
            conn.commit();
            System.out.println("Entries removed");
            System.exit(0);

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception ex) {
            }
            System.out.println("");
            SchedulerCleanup.showUsage();
            System.out.println("");
            System.out.println("Exception: ");
            System.out.println(e.toString());
            System.exit(1);
        } finally {
            try {
                conn.disconnect();
            } catch (Exception e) {
            }
        }
    }

}
