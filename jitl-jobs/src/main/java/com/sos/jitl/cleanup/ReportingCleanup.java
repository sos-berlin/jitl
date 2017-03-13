package com.sos.jitl.cleanup;

import com.sos.hibernate.classes.SOSHibernateFactory;

import sos.connection.SOSConnection;

public class ReportingCleanup {

    public static void showUsage() {
        System.out.println("Usage: hibernateFile range [age]");

        System.out.println("- Remove entries older as n (14) days:");
        System.out.println("      <hibernateFile> all 14          Remove all entries (reporting, dailyplan, yade)");
        System.out.println("      <hibernateFile> reporting 14    Remove only reporting entries");
        System.out.println("      <hibernateFile> dailyplan 14    Remove only dailyplan entries");
        System.out.println("      <hibernateFile> yade 14         Remove only yade entries");
        System.out.println("");
        System.out.println("- Remove all entries:");
        System.out.println("      <hibernateFile> all             Remove all entries (reporting, dailyplan, yade)");
        System.out.println("      <hibernateFile> reporting       Remove only reporting entries");
        System.out.println("      <hibernateFile> dailyplan       Remove only dailyplan entries");
        System.out.println("      <hibernateFile> yade            Remove only yade entries");
    }

    public static Enum<SOSHibernateFactory.Dbms> getDbms(String hibernateFile) throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(hibernateFile);
        return factory.getDbmsBeforeBuild();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            ReportingCleanup.showUsage();
            return;
        }

        for (int i = 0; i < args.length; i++) {
            String param = args[i].trim();
            System.out.println(String.format("  %s) %s", i + 1, param));
        }
        String hibernateFile = args[0];
        String range = args[1];
        String age = args.length >= 3 ? args[2] : "0";
        System.out.println("");
        System.out.print("Remove " + range + " entries");
        if (!age.equals("0")) {
            System.out.print(" older as " + age + " days");
        }
        System.out.println("");

        SOSConnection conn = null;
        try {
            conn = SOSConnection.createInstance(hibernateFile);
            conn.connect();

            Enum<SOSHibernateFactory.Dbms> dbms = getDbms(hibernateFile);
            String stmt = null;
            if (dbms.equals(SOSHibernateFactory.Dbms.MSSQL)) {
                stmt = "EXEC REPORT_CLEANUP '" + range + "'," + age;
            } else if (dbms.equals(SOSHibernateFactory.Dbms.MYSQL)) {
                stmt = "CALL REPORT_CLEANUP('" + range + "'," + age + ")";
            } else if (dbms.equals(SOSHibernateFactory.Dbms.ORACLE)) {
                stmt = "CALL REPORT_CLEANUP('" + range + "'," + age + ")";
            } else if (dbms.equals(SOSHibernateFactory.Dbms.PGSQL)) {
                stmt = "SELECT REPORT_CLEANUP('" + range + "'," + age + ")";
            }

            System.out.println("Execute " + dbms + ": " + stmt);
            conn.execute(stmt);
            conn.commit();
            System.out.println("Entries removed");

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception ex) {
            }
            System.out.println("");
            ReportingCleanup.showUsage();
            System.out.println("");
            System.out.println("Exception: ");
            System.out.println(e.toString());
        } finally {
            try {
                conn.disconnect();
            } catch (Exception e) {
            }
        }
    }
}
