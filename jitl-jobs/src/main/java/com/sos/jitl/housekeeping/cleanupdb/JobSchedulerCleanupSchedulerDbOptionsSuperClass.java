package com.sos.jitl.housekeeping.cleanupdb;

import java.util.HashMap;

import com.sos.JSHelper.Options.*;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;
 
@JSOptionClass(name = "JobSchedulerCleanupSchedulerDbOptionsSuperClass", description = "JobSchedulerCleanupSchedulerDbOptionsSuperClass")
public class JobSchedulerCleanupSchedulerDbOptionsSuperClass extends JSOptionsClass {
    /**
     * 
     */
    private static final long serialVersionUID          = 1L;
    private final String      conClassName              = "JobSchedulerCleanupSchedulerDbOptionsSuperClass";
    @SuppressWarnings("unused")
    private static Logger     logger                    = Logger.getLogger(JobSchedulerCleanupSchedulerDbOptionsSuperClass.class);

    /**
     * \var cleanup_daily_plan_execute : 
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     */
    @JSOptionDefinition(name = "cleanup_daily_plan_execute", description = "", key = "cleanup_daily_plan_execute", type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean   cleanup_daily_plan_execute = new SOSOptionBoolean(this, conClassName + ".cleanup_daily_plan_execute", // HashMap-Key
                                                                "", // Titel
                                                                "true", // InitValue
                                                                "true", // DefaultValue
                                                                false // isMandatory
                                                        );

    /**
     * \brief getcleanup_daily_plan_execute : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * \return 
     *
     */
    public SOSOptionBoolean getcleanup_daily_plan_execute() {
        return cleanup_daily_plan_execute;
    }

    /**
     * \brief setcleanup_daily_plan_execute : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * @param cleanup_daily_plan_execute : 
     */
    public void setcleanup_daily_plan_execute(SOSOptionBoolean p_cleanup_daily_plan_execute) {
        this.cleanup_daily_plan_execute = p_cleanup_daily_plan_execute;
    }

    /**
     * \var cleanup_jade_History_execute : 
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     */
    @JSOptionDefinition(name = "cleanup_jade_History_execute", description = "", key = "cleanup_jade_History_execute", type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean cleanup_jade_history_execute = new SOSOptionBoolean(this, conClassName + ".cleanup_jade_History_execute", // HashMap-Key
                                                               "", // Titel
                                                               "true", // InitValue
                                                               "true", // DefaultValue
                                                               false // isMandatory
                                                       );

    /**
     * \brief getcleanup_jade_History_execute : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * \return 
     *
     */
    public SOSOptionBoolean getcleanup_jade_History_execute() {
        return cleanup_jade_history_execute;
    }

    /**
     * \brief setcleanup_jade_History_execute : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * @param cleanup_jade_history_execute : 
     */
    public void setcleanup_jade_History_execute(SOSOptionBoolean p_cleanup_jade_History_execute) {
        this.cleanup_jade_history_execute = p_cleanup_jade_History_execute;
    }

    /**
     * \var cleanup_JobScheduler_History_execute : 
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     */
    @JSOptionDefinition(name = "cleanup_JobScheduler_History_execute", description = "", key = "cleanup_JobScheduler_History_execute", type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean cleanup_job_scheduler_history_execute = new SOSOptionBoolean(this, conClassName + ".cleanup_JobScheduler_History_execute", // HashMap-Key
                                                           "", // Titel
                                                           "true", // InitValue
                                                           "true", // DefaultValue
                                                           false // isMandatory
                                                   );

    /**
     * \brief getcleanup_JobScheduler_History_execute : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * \return 
     *
     */
    public SOSOptionBoolean getcleanup_JobScheduler_History_execute() {
        return cleanup_job_scheduler_history_execute;
    }

    /**
     * \brief setcleanup_JobScheduler_History_execute : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * @param cleanup_job_scheduler_history_execute : 
     */
    public void setcleanup_JobScheduler_History_execute(SOSOptionBoolean p_cleanup_JobScheduler_History_execute) {
        this.cleanup_job_scheduler_history_execute = p_cleanup_JobScheduler_History_execute;
    }

    /**
     * \var delete_daily_plan_interval : 
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     */
    @JSOptionDefinition(name = "delete_daily_plan_interval", description = "", key = "delete_daily_plan_interval", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger delete_daily_plan_interval = new SOSOptionInteger(this, conClassName + ".delete_daily_plan_interval", // HashMap-Key
                                                               "", // Titel
                                                               "0", // InitValue
                                                               "0", // DefaultValue
                                                               false // isMandatory
                                                       );

    /**
     * \var cleanup_daily_plan_limit : 
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     */
    @JSOptionDefinition(name = "cleanup_daily_plan_limit", description = "", key = "cleanup_daily_plan_limit", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger   cleanup_daily_plan_limit = new SOSOptionInteger(this, conClassName + ".cleanup_daily_plan_limit", // HashMap-Key
                                                                "", // Titel
                                                                "0", // InitValue
                                                                "0", // DefaultValue
                                                                false // isMandatory
                                                        );

    /**
     * \brief getcleanup_daily_plan_limit : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * \return 
     *
     */
    public SOSOptionInteger getcleanup_daily_plan_limit() {
        return cleanup_daily_plan_limit;
    }

    /**
     * \brief setcleanup_daily_plan_limit : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * @param cleanup_daily_plan_limit : 
     */
    public void setcleanup_daily_plan_limit(SOSOptionInteger p_cleanup_daily_plan_limit) {
        this.cleanup_daily_plan_limit = p_cleanup_daily_plan_limit;
    }

    /**
     * \var cleanup_jade_History_limit : 
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     */
    @JSOptionDefinition(name = "cleanup_jade_History_limit", description = "", key = "cleanup_jade_History_limit", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger cleanup_jade_history_limit = new SOSOptionInteger(this, conClassName + ".cleanup_jade_History_limit", // HashMap-Key
                                                               "", // Titel
                                                               "0", // InitValue
                                                               "0", // DefaultValue
                                                               false // isMandatory
                                                       );

    /**
     * \brief getcleanup_jade_History_limit : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * \return 
     *
     */
    public SOSOptionInteger getcleanup_jade_history_limit() {
        return cleanup_jade_history_limit;
    }

    /**
     * \brief setcleanup_jade_History_limit : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * @param cleanup_jade_history_limit : 
     */
    public void setcleanup_jade_history_limit(SOSOptionInteger p_cleanup_jade_History_limit) {
        this.cleanup_jade_history_limit = p_cleanup_jade_History_limit;
    }

    /**
     * \var cleanup_JobScheduler_History_limit : 
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     */
    @JSOptionDefinition(name = "cleanup_JobScheduler_History_limit", description = "", key = "cleanup_JobScheduler_History_limit", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger cleanup_jobscheduler_history_limit = new SOSOptionInteger(this, conClassName + ".cleanup_JobScheduler_History_limit", // HashMap-Key
                                                           "", // Titel
                                                           "0", // InitValue
                                                           "0", // DefaultValue
                                                           false // isMandatory
                                                   );

    /**
     * \brief getcleanup_JobScheduler_History_limit : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * \return 
     *
     */
    public SOSOptionInteger getcleanup_jobscheduler_history_limit() {
        return cleanup_jobscheduler_history_limit;
    }

    /**
     * \brief setcleanup_JobScheduler_History_limit : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * @param cleanup_jobscheduler_history_limit : 
     */
    public void setcleanup_jobscheduler_history_limit(SOSOptionInteger p_cleanup_jobscheduler_history_limit) {
        this.cleanup_jobscheduler_history_limit = p_cleanup_jobscheduler_history_limit;
    }

         
    
    /**
     * \brief getdelete_daily_plan_interval : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * \return 
     *
     */
    public SOSOptionInteger getdelete_daily_plan_interval() {
        return delete_daily_plan_interval;
    }

    /**
     * \brief setdelete_daily_plan_interval : 
     * 
     * \details
     * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
     *
     * @param delete_daily_plan_interval : 
     */
    public void setdelete_daily_plan_interval(SOSOptionInteger p_delete_daily_plan_interval) {
        this.delete_daily_plan_interval = p_delete_daily_plan_interval;
    }

    /**
     * \var delete_jade_history_interval : 
     * Items in the tables JADE_FILES and JADE_FILES_HISTORY which are older than the given number of days will be deleted.
     *
     */
    @JSOptionDefinition(name = "delete_jade_history_interval", description = "", key = "delete_jade_history_interval", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger delete_jade_history_interval = new SOSOptionInteger(this, conClassName + ".delete_jade_history_interval", // HashMap-Key
                                                                "", // Titel
                                                                "0", // InitValue
                                                                "0", // DefaultValue
                                                                false // isMandatory
                                                        );

    /**
     * \brief getdelete_jade_history_interval : 
     * 
     * \details
     * Items in the tables JADE_FILES and JADE_FILES_HISTORY which are older than the given number of days will be deleted.
     *
     * \return 
     *
     */
    public SOSOptionInteger getdelete_jade_history_interval() {
        return delete_jade_history_interval;
    }

    /**
     * \brief setdelete_jade_history_interval : 
     * 
     * \details
     * Items in the tables JADE_FILES and JADE_FILES_HISTORY which are older than the given number of days will be deleted.
     *
     * @param delete_jade_history_interval : 
     */
    public void setdelete_jade_history_interval(SOSOptionInteger p_delete_jade_history_interval) {
        this.delete_jade_history_interval = p_delete_jade_history_interval;
    }

    /**
     * \var delete_history_interval : 
     * Items in the tables SCHEDULER_HISTORY and SCHEDULER_ORDER_HISTORY which are older than the given number of days will be deleted.
     *
     */
    @JSOptionDefinition(name = "delete_history_interval", description = "", key = "delete_history_interval", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger delete_history_interval = new SOSOptionInteger(this, conClassName + ".delete_history_interval", // HashMap-Key
                                                            "", // Titel
                                                            "0", // InitValue
                                                            "0", // DefaultValue
                                                            false // isMandatory
                                                    );

    /**
     * \brief getdelete_history_interval : 
     * 
     * \details
     * Items in the tables SCHEDULER_HISTORY and SCHEDULER_ORDER_HISTORY which are older than the given number of days will be deleted.
     *
     * \return 
     *
     */
    public SOSOptionInteger getdelete_history_interval() {
        return delete_history_interval;
    }

    /**
     * \brief setdelete_history_interval : 
     * 
     * \details
     * Items in the tables SCHEDULER_HISTORY and SCHEDULER_ORDER_HISTORY which are older than the given number of days will be deleted.
     *
     * @param delete_history_interval : 
     */
    public void setdelete_history_interval(SOSOptionInteger p_delete_history_interval) {
        this.delete_history_interval = p_delete_history_interval;
    }

    /**
     * \var delete_interval : 
     * This parameter will be used if a table specific parameter is missing.
     *
     */
    @JSOptionDefinition(name = "delete_interval", description = "", key = "delete_interval", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger delete_interval = new SOSOptionInteger(this, conClassName + ".delete_interval", // HashMap-Key
                                                    "", // Titel
                                                    "0", // InitValue
                                                    "0", // DefaultValue
                                                    false // isMandatory
                                            );

    /**
     * \brief getdelete_interval : 
     * 
     * \details
     * This parameter will be used if a table specific parameter is missing.
     *
     * \return 
     *
     */
    public SOSOptionInteger getdelete_interval() {
        return delete_interval;
    }

    /**
     * \brief setdelete_interval : 
     * 
     * \details
     * This parameter will be used if a table specific parameter is missing.
     *
     * @param delete_interval : 
     */
    public void setdelete_interval(SOSOptionInteger p_delete_interval) {
        this.delete_interval = p_delete_interval;
    }

    /**
     * \var hibernate_configuration_file : 
     * 
     *
     */
    @JSOptionDefinition(name = "hibernate_configuration_file", description = "", key = "hibernate_configuration_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString hibernate_configuration_file = new SOSOptionString(this, conClassName + ".hibernate_configuration_file", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                                                        );

    /**
     * \brief gethibernate_configuration_file : 
     * 
     * \details
     * 
     *
     * \return 
     *
     */
    public SOSOptionString gethibernate_configuration_file() {
        return hibernate_configuration_file;
    }

    /**
     * \brief sethibernate_configuration_file : 
     * 
     * \details
     * 
     *
     * @param hibernate_configuration_file : 
     */
    public void sethibernate_configuration_file(SOSOptionString p_hibernate_configuration_file) {
        this.hibernate_configuration_file = p_hibernate_configuration_file;
    }

    /**
     * \var scheduler_id : 
     * 
     *
     */
    @JSOptionDefinition(name = "scheduler_id", description = "", key = "scheduler_id", type = "SOSOptionString", mandatory = false)
    public SOSOptionString scheduler_id = new SOSOptionString(this, conClassName + ".scheduler_id", // HashMap-Key
                                                "", // Titel
                                                " ", // InitValue
                                                " ", // DefaultValue
                                                false // isMandatory
                                        );

    /**
     * \brief getscheduler_id : 
     * 
     * \details
     * 
     *
     * \return 
     *
     */
    public SOSOptionString getscheduler_id() {
        return scheduler_id;
    }

    /**
     * \brief setscheduler_id : 
     * 
     * \details
     * 
     *
     * @param scheduler_id : 
     */
    public void setscheduler_id(SOSOptionString p_scheduler_id) {
        this.scheduler_id = p_scheduler_id;
    }

    public JobSchedulerCleanupSchedulerDbOptionsSuperClass() {
        objParentClass = this.getClass();
    } // public JobSchedulerCleanupSchedulerDbOptionsSuperClass

    public JobSchedulerCleanupSchedulerDbOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerCleanupSchedulerDbOptionsSuperClass

    //

    public JobSchedulerCleanupSchedulerDbOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    } // public JobSchedulerCleanupSchedulerDbOptionsSuperClass (HashMap JSSettings)

    /**
     * \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
     * Optionen als String
     *
     * \details
     * 
     * \see toString 
     * \see toOut
     */
    /*private String getAllOptionsAsString() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getAllOptionsAsString";
        String strT = conClassName + "\n";
        final StringBuffer strBuffer = new StringBuffer();
        // strT += IterateAllDataElementsByAnnotation(objParentClass, this,
        // JSOptionsClass.IterationTypes.toString, strBuffer);
        // strT += IterateAllDataElementsByAnnotation(objParentClass, this, 13,
        // strBuffer);
        strT += this.toString(); // fix
        //
        return strT;
    } // private String getAllOptionsAsString ()
    */
    /**
     * \brief setAllOptions - übernimmt die OptionenWerte aus der HashMap
     *
     * \details In der als Parameter anzugebenden HashMap sind Schlüssel (Name)
     * und Wert der jeweiligen Option als Paar angegeben. Ein Beispiel für den
     * Aufbau einer solchen HashMap findet sich in der Beschreibung dieser
     * Klasse (\ref TestData "setJobSchedulerSSHJobOptions"). In dieser Routine
     * werden die Schlüssel analysiert und, falls gefunden, werden die
     * dazugehörigen Werte den Properties dieser Klasse zugewiesen.
     *
     * Nicht bekannte Schlüssel werden ignoriert.
     *
     * \see JSOptionsClass::getItem
     *
     * @param pobjJSSettings
     * @throws Exception
     */
    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setAllOptions";
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    } // public void setAllOptions (HashMap <String, String> JSSettings)

    /**
     * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * @throws Exception
     *
     * @throws Exception
     * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
     */
    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
    , Exception {
        try {
            super.CheckMandatory();
        }
        catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()

    /**
     *
     * \brief CommandLineArgs - Übernehmen der Options/Settings aus der
     * Kommandozeile
     *
     * \details Die in der Kommandozeile beim Starten der Applikation
     * angegebenen Parameter werden hier in die HashMap übertragen und danach
     * den Optionen als Wert zugewiesen.
     *
     * \return void
     *
     * @param pstrArgs
     * @throws Exception
     */
    @Override
    public void CommandLineArgs(String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }
} // public class JobSchedulerCleanupSchedulerDbOptionsSuperClass