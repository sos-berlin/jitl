package com.sos.jitl.housekeeping.cleanupdb;

import java.util.HashMap;

import com.sos.JSHelper.Options.*;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener; 
import org.apache.log4j.Logger;


/**
 * \class 		JobSchedulerCleanupSchedulerDbOptionsSuperClass - Delete log entries in the Job Scheduler history Databaser tables
 *
 * \brief 
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see JobSchedulerCleanupSchedulerDbOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * see \see C:\Dokumente und Einstellungen\Uwe Risse\Lokale Einstellungen\Temp\scheduler_editor-7803311730891015050.html for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by C:\Dokumente und Einstellungen\Uwe Risse\Eigene Dateien\sos-berlin.com\jobscheduler\scheduler_ur_current\config\JOETemplates\java\xsl\JSJobDoc2JSOptionSuperClass.xsl from http://www.sos-berlin.com at 20121211162230 
 * \endverbatim
 * \section OptionsTable Tabelle der vorhandenen Optionen
 * 
 * Tabelle mit allen Optionen
 * 
 * MethodName
 * Title
 * Setting
 * Description
 * IsMandatory
 * DataType
 * InitialValue
 * TestValue
 * 
 * 
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JobSchedulerCleanupSchedulerDbOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "JobSchedulerCleanupSchedulerDbOptionsSuperClass", description = "JobSchedulerCleanupSchedulerDbOptionsSuperClass")
public class JobSchedulerCleanupSchedulerDbOptionsSuperClass extends JSOptionsClass {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String					conClassName						= "JobSchedulerCleanupSchedulerDbOptionsSuperClass";
		@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JobSchedulerCleanupSchedulerDbOptionsSuperClass.class);

		

/**
 * \var delete_daily_plan_interval : 
 * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
 *
 */
    @JSOptionDefinition(name = "delete_daily_plan_interval", 
    description = "", 
    key = "delete_daily_plan_interval", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString delete_daily_plan_interval = new SOSOptionString(this, conClassName + ".delete_daily_plan_interval", // HashMap-Key
                                                                "", // Titel
                                                                "0", // InitValue
                                                                "0", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getdelete_daily_plan_interval : 
 * 
 * \details
 * Items in the table DAYS_SCHEDULE which are older than the given number of days will be deleted.
 *
 * \return 
 *
 */
    public SOSOptionString  getdelete_daily_plan_interval() {
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
    public void setdelete_daily_plan_interval (SOSOptionString p_delete_daily_plan_interval) { 
        this.delete_daily_plan_interval = p_delete_daily_plan_interval;
    }

                        

/**
 * \var delete_ftp_history_interval : 
 * Items in the tables SOSFTP_FILES and SOSFTP_FILES_HISTORY which are older than the given number of days will be deleted.
 *
 */
    @JSOptionDefinition(name = "delete_ftp_history_interval", 
    description = "", 
    key = "delete_ftp_history_interval", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString delete_ftp_history_interval = new SOSOptionString(this, conClassName + ".delete_ftp_history_interval", // HashMap-Key
                                                                "", // Titel
                                                                "0", // InitValue
                                                                "0", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getdelete_ftp_history_interval : 
 * 
 * \details
 * Items in the tables SOSFTP_FILES and SOSFTP_FILES_HISTORY which are older than the given number of days will be deleted.
 *
 * \return 
 *
 */
    public SOSOptionString  getdelete_ftp_history_interval() {
        return delete_ftp_history_interval;
    }

/**
 * \brief setdelete_ftp_history_interval : 
 * 
 * \details
 * Items in the tables SOSFTP_FILES and SOSFTP_FILES_HISTORY which are older than the given number of days will be deleted.
 *
 * @param delete_ftp_history_interval : 
 */
    public void setdelete_ftp_history_interval (SOSOptionString p_delete_ftp_history_interval) { 
        this.delete_ftp_history_interval = p_delete_ftp_history_interval;
    }

                        

/**
 * \var delete_history_interval : 
 * Items in the tables SCHEDULER_HISTORY and SCHEDULER_ORDER_HISTORY which are older than the given number of days will be deleted.
 *
 */
    @JSOptionDefinition(name = "delete_history_interval", 
    description = "", 
    key = "delete_history_interval", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString delete_history_interval = new SOSOptionString(this, conClassName + ".delete_history_interval", // HashMap-Key
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
    public SOSOptionString  getdelete_history_interval() {
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
    public void setdelete_history_interval (SOSOptionString p_delete_history_interval) { 
        this.delete_history_interval = p_delete_history_interval;
    }

                        

/**
 * \var delete_interval : 
 * This parameter will be used if a table specific parameter is missing.
 *
 */
    @JSOptionDefinition(name = "delete_interval", 
    description = "", 
    key = "delete_interval", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString delete_interval = new SOSOptionString(this, conClassName + ".delete_interval", // HashMap-Key
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
    public SOSOptionString  getdelete_interval() {
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
    public void setdelete_interval (SOSOptionString p_delete_interval) { 
        this.delete_interval = p_delete_interval;
    }

                        

/**
 * \var hibernate_configuration_file : 
 * 
 *
 */
    @JSOptionDefinition(name = "hibernate_configuration_file", 
    description = "", 
    key = "hibernate_configuration_file", 
    type = "SOSOptionString", 
    mandatory = false)
    
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
    public SOSOptionString  gethibernate_configuration_file() {
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
    public void sethibernate_configuration_file (SOSOptionString p_hibernate_configuration_file) { 
        this.hibernate_configuration_file = p_hibernate_configuration_file;
    }

                        

/**
 * \var scheduler_id : 
 * 
 *
 */
    @JSOptionDefinition(name = "scheduler_id", 
    description = "", 
    key = "scheduler_id", 
    type = "SOSOptionString", 
    mandatory = false)
    
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
    public SOSOptionString  getscheduler_id() {
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
    public void setscheduler_id (SOSOptionString p_scheduler_id) { 
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

	public JobSchedulerCleanupSchedulerDbOptionsSuperClass (HashMap <String, String> JSSettings) throws Exception {
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
	public void setAllOptions(HashMap <String, String> pobjJSSettings) throws Exception {
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
	public void CommandLineArgs(String[] pstrArgs) throws Exception {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}
} // public class JobSchedulerCleanupSchedulerDbOptionsSuperClass