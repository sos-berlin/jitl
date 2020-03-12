package sos.scheduler.managed;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.connection.SOSConnection;
import sos.marshalling.SOSImport;
import sos.settings.SOSConnectionSettings;
import sos.util.SOSArguments;

/** @author Andreas Liebert */
public class ManagedJobChainImport extends SOSImport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedJobChainImport.class);
    private static SOSConnection conn;
    private String modelId = "";

    public ManagedJobChainImport(SOSConnection conn, String file_name, String package_id, String package_element, String package_value) {
        super(conn, file_name, package_id, package_element, package_value);
    }

    public static void showUsage() {
        LOGGER.info("usage:ManagedJobChainImport ");
        LOGGER.info("Argumente:");
        LOGGER.info("     -file=           Importdatei");
        LOGGER.info("     -v=              Loglevel (optional)");
        LOGGER.info("     -log=            LogDatei (optional)");
        LOGGER.info("     -settings=       factory.ini Datei (default:../config/factory.ini)");
        LOGGER.info("     -package=        zu importierende Pakete (package[+package[+...]] default: alle)");
        LOGGER.info("     -jobchain=       zu importierende jochains (jobchain[+jobchain[+...]] default: alle)");
    }

    public static void main(String[] args) {
        if (args.length == 0 || "-?".equals(args[0]) || "/?".equals(args[0]) || "-h".equals(args[0])) {
            showUsage();
            System.exit(0);
        }
        try {
            SOSArguments arguments = new SOSArguments(args);
            String xmlFile = "";
            String settingsFile = "";
            String packages = "";
            String jobchains = "";
            try {
                xmlFile = arguments.asString("-file=");
                settingsFile = arguments.asString("-settings=", "../config/factory.ini");
                jobchains = arguments.asString("-jobchain=", "");
                packages = arguments.asString("-package=", "");
            } catch (Exception e1) {
                LOGGER.error(e1.getMessage(), e1);
                showUsage();
                System.exit(0);
            }
            if (!packages.isEmpty() && !jobchains.isEmpty()) {
                LOGGER.info("jobchain und package dürfen nicht zusammen angegeben werden.");
                showUsage();
                System.exit(0);
            }
            conn = ManagedJobExport.getDBConnection(settingsFile);
            conn.connect();
            conn.setAutoCommit(false);
            if (packages != null && !packages.isEmpty()) {
                doMultipleImport(conn, xmlFile, "PACKAGE", packages);
            } else if (jobchains != null && !jobchains.isEmpty()) {
                doMultipleImport(conn, xmlFile, "NAME", jobchains);
            } else {
                ManagedJobChainImport imp = new ManagedJobChainImport(conn, xmlFile, null, null, null);
                imp.setUpdate(false);
                imp.setHandler(JobSchedulerManagedObject.getTableManagedModels(), "key_handler_MANAGED_MODELS", "", "NAME");
                imp.setHandler(JobSchedulerManagedObject.getTableManagedJobs(), "key_handler_MANAGED_JOBS", "rec_handler_MANAGED_JOBS", null);
                imp.setHandler(JobSchedulerManagedObject.getTableManagedOrders(), "key_handler_MANAGED_ORDERS", null, null);
                imp.doImport(conn, xmlFile);
            }
            conn.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                conn.disconnect();
            } catch (Exception e) {
            }
        }
    }

    private static void doMultipleImport(SOSConnection conn, String xmlFile, String field, String identifiers) throws Exception {
        String[] identArray = identifiers.split("\\+");
        for (int i = 0; i < identArray.length; i++) {
            String identifier = identArray[i];
            ManagedJobChainImport imp = new ManagedJobChainImport(conn, xmlFile, JobSchedulerManagedObject.getTableManagedModels(), field,
                    identifier);
            imp.setUpdate(false);
            imp.setHandler(JobSchedulerManagedObject.getTableManagedModels(), "key_handler_MANAGED_MODELS", "", "NAME");
            imp.setHandler(JobSchedulerManagedObject.getTableManagedJobs(), "key_handler_MANAGED_JOBS", "rec_handler_MANAGED_JOBS", null);
            imp.setHandler(JobSchedulerManagedObject.getTableManagedOrders(), "key_handler_MANAGED_ORDERS", null, null);
            imp.doImport(conn, xmlFile);
        }
    }

    public HashMap key_handler_MANAGED_JOBS(HashMap keys) throws Exception {
        SOSConnectionSettings sosSettings = new SOSConnectionSettings(conn, JobSchedulerManagedObject.getTableSettings());
        int key = sosSettings.getLockedSequence("scheduler", "counter", "scheduler_managed_jobs.id");
        keys.put("ID", String.valueOf(key));
        return keys;
    }

    public HashMap key_handler_MANAGED_MODELS(HashMap keys) throws Exception {
        SOSConnectionSettings sosSettings = new SOSConnectionSettings(conn, JobSchedulerManagedObject.getTableSettings());
        int key = sosSettings.getLockedSequence("scheduler", "counter", "scheduler_managed_models.id");
        keys.put("ID", String.valueOf(key));
        modelId = String.valueOf(key);
        return keys;
    }

    public HashMap key_handler_MANAGED_ORDERS(HashMap keys) throws Exception {
        SOSConnectionSettings sosSettings = new SOSConnectionSettings(conn, JobSchedulerManagedObject.getTableSettings());
        int key = sosSettings.getLockedSequence("scheduler", "counter", "scheduler_managed_orders.id");
        keys.put("ID", String.valueOf(key));
        return keys;
    }

    public HashMap rec_handler_MANAGED_JOBS(HashMap keys, HashMap record, String record_identifier) throws Exception {
        record.put("MODEL", modelId);
        return record;
    }

}
