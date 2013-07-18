

package com.sos.jitl.eventing.checkevents;

import java.util.HashMap;

import com.sos.JSHelper.Options.*;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener; 
import org.apache.log4j.Logger;

import com.sos.JSHelper.Options.*;

/**
 * \class 		JobSchedulerCheckEventsOptionsSuperClass - Check if events exist
 *
 * \brief 
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see JobSchedulerCheckEventsOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * 
 * \verbatim ;
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
	pobjHM.put ("		JobSchedulerCheckEventsOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "JobSchedulerCheckEventsOptionsSuperClass", description = "JobSchedulerCheckEventsOptionsSuperClass")
public class JobSchedulerCheckEventsOptionsSuperClass extends JSOptionsClass {
	private final String					conClassName						= "JobSchedulerCheckEventsOptionsSuperClass";
		@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JobSchedulerCheckEventsOptionsSuperClass.class);

		

/**
 * \var EventClassName : 
 * 
 *
 */
    @JSOptionDefinition(name = "EventClassName", 
    description = "", 
    key = "EventClassName", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString EventClassName = new SOSOptionString(this, conClassName + ".EventClassName", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getEventClassName : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionString  getEventClassName() {
        return EventClassName;
    }

/**
 * \brief setEventClassName : 
 * 
 * \details
 * 
 *
 * @param EventClassName : 
 */
    public void setEventClassName (SOSOptionString p_EventClassName) { 
        this.EventClassName = p_EventClassName;
    }

                        

/**
 * \var EventNames : 
 * 
 *
 */
    @JSOptionDefinition(name = "EventNames", 
    description = "", 
    key = "EventNames", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString EventNames = new SOSOptionString(this, conClassName + ".EventNames", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getEventNames : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionString  getEventNames() {
        return EventNames;
    }

/**
 * \brief setEventNames : 
 * 
 * \details
 * 
 *
 * @param EventNames : 
 */
    public void setEventNames (SOSOptionString p_EventNames) { 
        this.EventNames = p_EventNames;
    }

                        

/**
 * \var scheduler_event_class : 
 * 
 *
 */
    @JSOptionDefinition(name = "scheduler_event_class", 
    description = "", 
    key = "scheduler_event_class", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString scheduler_event_class = new SOSOptionString(this, conClassName + ".scheduler_event_class", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getscheduler_event_class : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionString  getscheduler_event_class() {
        return scheduler_event_class;
    }

/**
 * \brief setscheduler_event_class : 
 * 
 * \details
 * 
 *
 * @param scheduler_event_class : 
 */
    public void setscheduler_event_class (SOSOptionString p_scheduler_event_class) { 
        this.scheduler_event_class = p_scheduler_event_class;
    }

                        

/**
 * \var scheduler_event_exit_code : 
 * 
 *
 */
    @JSOptionDefinition(name = "scheduler_event_exit_code", 
    description = "", 
    key = "scheduler_event_exit_code", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString scheduler_event_exit_code = new SOSOptionString(this, conClassName + ".scheduler_event_exit_code", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getscheduler_event_exit_code : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionString  getscheduler_event_exit_code() {
        return scheduler_event_exit_code;
    }

/**
 * \brief setscheduler_event_exit_code : 
 * 
 * \details
 * 
 *
 * @param scheduler_event_exit_code : 
 */
    public void setscheduler_event_exit_code (SOSOptionString p_scheduler_event_exit_code) { 
        this.scheduler_event_exit_code = p_scheduler_event_exit_code;
    }

                        

/**
 * \var scheduler_event_handler_host : 
 * 
 *
 */
    @JSOptionDefinition(name = "scheduler_event_handler_host", 
    description = "", 
    key = "scheduler_event_handler_host", 
    type = "SOSOptionHostName", 
    mandatory = false)
    
    public SOSOptionHostName scheduler_event_handler_host = new SOSOptionHostName(this, conClassName + ".scheduler_event_handler_host", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getscheduler_event_handler_host : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionHostName  getscheduler_event_handler_host() {
        return scheduler_event_handler_host;
    }

/**
 * \brief setscheduler_event_handler_host : 
 * 
 * \details
 * 
 *
 * @param scheduler_event_handler_host : 
 */
    public void setscheduler_event_handler_host (SOSOptionHostName p_scheduler_event_handler_host) { 
        this.scheduler_event_handler_host = p_scheduler_event_handler_host;
    }

                        
    public SOSOptionHostName EventService =
    (SOSOptionHostName) scheduler_event_handler_host.SetAlias(conClassName + ".EventService");

/**
 * \var scheduler_event_handler_port : 
 * 
 *
 */
    @JSOptionDefinition(name = "scheduler_event_handler_port", 
    description = "", 
    key = "scheduler_event_handler_port", 
    type = "SOSOptionPortNumber", 
    mandatory = false)
    
    public SOSOptionPortNumber scheduler_event_handler_port = new SOSOptionPortNumber(this, conClassName + ".scheduler_event_handler_port", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getscheduler_event_handler_port : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionPortNumber  getscheduler_event_handler_port() {
        return scheduler_event_handler_port;
    }

/**
 * \brief setscheduler_event_handler_port : 
 * 
 * \details
 * 
 *
 * @param scheduler_event_handler_port : 
 */
    public void setscheduler_event_handler_port (SOSOptionPortNumber p_scheduler_event_handler_port) { 
        this.scheduler_event_handler_port = p_scheduler_event_handler_port;
    }

                        
    public SOSOptionPortNumber EventServicePort =
    (SOSOptionPortNumber) scheduler_event_handler_port.SetAlias(conClassName + ".EventServicePort");

/**
 * \var scheduler_event_id : 
 * 
 *
 */
    @JSOptionDefinition(name = "scheduler_event_id", 
    description = "", 
    key = "scheduler_event_id", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString scheduler_event_id = new SOSOptionString(this, conClassName + ".scheduler_event_id", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getscheduler_event_id : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionString  getscheduler_event_id() {
        return scheduler_event_id;
    }

/**
 * \brief setscheduler_event_id : 
 * 
 * \details
 * 
 *
 * @param scheduler_event_id : 
 */
    public void setscheduler_event_id (SOSOptionString p_scheduler_event_id) { 
        this.scheduler_event_id = p_scheduler_event_id;
    }

                        

/**
 * \var scheduler_event_job : 
 * 
 *
 */
    @JSOptionDefinition(name = "scheduler_event_job", 
    description = "", 
    key = "scheduler_event_job", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString scheduler_event_job = new SOSOptionString(this, conClassName + ".scheduler_event_job", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getscheduler_event_job : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionString  getscheduler_event_job() {
        return scheduler_event_job;
    }

/**
 * \brief setscheduler_event_job : 
 * 
 * \details
 * 
 *
 * @param scheduler_event_job : 
 */
    public void setscheduler_event_job (SOSOptionString p_scheduler_event_job) { 
        this.scheduler_event_job = p_scheduler_event_job;
    }

                        

/**
 * \var scheduler_event_jobchain : 
 * 
 *
 */
    @JSOptionDefinition(name = "scheduler_event_jobchain", 
    description = "", 
    key = "scheduler_event_jobchain", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString scheduler_event_jobchain = new SOSOptionString(this, conClassName + ".scheduler_event_jobchain", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getscheduler_event_jobchain : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionString  getscheduler_event_jobchain() {
        return scheduler_event_jobchain;
    }

/**
 * \brief setscheduler_event_jobchain : 
 * 
 * \details
 * 
 *
 * @param scheduler_event_jobchain : 
 */
    public void setscheduler_event_jobchain (SOSOptionString p_scheduler_event_jobchain) { 
        this.scheduler_event_jobchain = p_scheduler_event_jobchain;
    }

                        

/**
 * \var scheduler_event_xpath : 
 * 
 *
 */
    @JSOptionDefinition(name = "scheduler_event_xpath", 
    description = "", 
    key = "scheduler_event_xpath", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString scheduler_event_xpath = new SOSOptionString(this, conClassName + ".scheduler_event_xpath", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getscheduler_event_xpath : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionString  getscheduler_event_xpath() {
        return scheduler_event_xpath;
    }

/**
 * \brief setscheduler_event_xpath : 
 * 
 * \details
 * 
 *
 * @param scheduler_event_xpath : 
 */
    public void setscheduler_event_xpath (SOSOptionString p_scheduler_event_xpath) { 
        this.scheduler_event_xpath = p_scheduler_event_xpath;
    }

                        
        
        
	public JobSchedulerCheckEventsOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public JobSchedulerCheckEventsOptionsSuperClass

	public JobSchedulerCheckEventsOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerCheckEventsOptionsSuperClass

		//

	public JobSchedulerCheckEventsOptionsSuperClass (HashMap <String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public JobSchedulerCheckEventsOptionsSuperClass (HashMap JSSettings)
/**
 * \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
 * Optionen als String
 *
 * \details
 * 
 * \see toString 
 * \see toOut
 */
	private String getAllOptionsAsString() {
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
} // public class JobSchedulerCheckEventsOptionsSuperClass