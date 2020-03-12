/*
 * ManagedJobImport.java Created on 13.10.2005
 */
package sos.scheduler.managed;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.connection.SOSConnection;
import sos.marshalling.SOSImport;
import sos.settings.SOSConnectionSettings;
import sos.util.SOSArguments;

/** @author Andreas Liebert */
public class ManagedJobImport extends SOSImport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedJobImport.class);
    private static SOSConnection conn;
    private int workflow = -1;
    private boolean jobExists = false;
    private boolean modelExists = true;

    public ManagedJobImport(SOSConnection conn, String file_name, String package_id, String package_element, String package_value) {
        super(conn, file_name, package_id, package_element, package_value);
    }

    public static void main(String[] args) {
        if (args.length == 0 || "-?".equals(args[0]) || "-h".equals(args[0])) {
            showUsage();
            System.exit(0);
        }
        try {
            SOSArguments arguments = new SOSArguments(args);
            String xmlFile = "";
            String settingsFile = "";
            int template = 0;
            int model = 0;
            try {
                xmlFile = arguments.asString("-file=");
                settingsFile = arguments.asString("-settings=", "../config/factory.ini");
                model = arguments.asInt("-jobchain=", -1);
            } catch (Exception e1) {
                LOGGER.info(e1.getMessage());
                showUsage();
                System.exit(0);
            }

            conn = ManagedJobExport.getDBConnection(settingsFile);
            conn.connect();
            conn.setAutoCommit(false);
            ManagedJobImport imp = new ManagedJobImport(conn, xmlFile, null, null, null);
            imp.setWorkflow(model);
            imp.setUpdate(false);
            imp.setHandler(JobSchedulerManagedObject.getTableManagedJobs(), "key_handler_MANAGED_JOBS", "rec_handler_MANAGED_JOBS", null);
            imp.doImport(conn, xmlFile);
            if (imp.jobExists()) {
                conn.rollback();
                LOGGER.warn("Job already exists.");
            } else if (!imp.modelExists()) {
                conn.rollback();
                LOGGER.warn("Jobchain doesn't exist. Please specify a jobchain using the -jobchain option.");
            }
            conn.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            try {
                conn.disconnect();
            } catch (Exception e) {
            }
        }
    }

    public static void showUsage() {
        LOGGER.info("usage:ManagedJobImport ");
        LOGGER.info("Argumente:");
        LOGGER.info("     -file=           Importdatei");
        LOGGER.info("     -v=              Loglevel (optional)");
        LOGGER.info("     -log=            LogDatei (optional)");
        LOGGER.info("     -settings=       factory.ini Datei (default:../config/factory.ini)");
        LOGGER.info("     -jobchain=       neue Jobkette (ID) f�r importierten Job (optional)");
    }

    public int getWorkflow() {
        return workflow;
    }

    public void setWorkflow(int workflow) {
        this.workflow = workflow;
    }

    public HashMap key_handler_MANAGED_JOBS(HashMap keys) throws Exception {
        SOSConnectionSettings sosSettings = new SOSConnectionSettings(conn, JobSchedulerManagedObject.getTableSettings());
        int key = sosSettings.getLockedSequence("scheduler", "counter", "scheduler_managed_jobs.id");
        keys.put("ID", String.valueOf(key));
        return keys;
    }

    public HashMap rec_handler_MANAGED_JOBS(HashMap keys, HashMap record, String record_identifier) throws Exception {
        String model = record.get("MODEL").toString();
        if (workflow > -1) {
            model = "" + workflow;
        }
        String test = conn.getSingleValue("SELECT \"ID\" FROM " + JobSchedulerManagedObject.getTableManagedJobs() + " WHERE \"MODEL\"=" + model
                + " AND \"JOB_NAME\"='" + record.get("JOB_NAME").toString() + "'");
        if (test != null && !test.isEmpty()) {
            jobExists = true;
        }
        String modelTest = conn.getSingleValue("SELECT \"ID\" FROM " + JobSchedulerManagedObject.getTableManagedModels() + " WHERE \"ID\"=" + model);
        if (modelTest == null || modelTest.isEmpty()) {
            modelExists = false;
        }
        record.put("MODEL", model);
        return record;
    }

    public boolean jobExists() {
        return jobExists;
    }

    public boolean modelExists() {
        return modelExists;
    }

}