

package com.sos.jitl.checkblacklist;

import java.util.HashMap;

import com.sos.JSHelper.Options.*;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener; 
import org.apache.log4j.Logger;

import com.sos.JSHelper.Options.*;


@JSOptionClass(name = "JobSchedulerCheckBlacklistOptionsSuperClass", description = "JobSchedulerCheckBlacklistOptionsSuperClass")
public class JobSchedulerCheckBlacklistOptionsSuperClass extends JSOptionsClass {
	private final String					conClassName						= "JobSchedulerCheckBlacklistOptionsSuperClass";
		@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JobSchedulerCheckBlacklistOptionsSuperClass.class);

		

/**
 * \var granuality : 
 * Defines the start of the job or the job_chain order: for each order that is in a blacklist jobchain: for each job chain that has a blacklist. blacklist: One start when a blacklist exists.
 *
 */
    @JSOptionDefinition(name = "granuality", 
    description = "", 
    key = "granuality", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString granuality = new SOSOptionString(this, conClassName + ".granuality", // HashMap-Key
                                                                "", // Titel
                                                                "blacklist", // InitValue
                                                                "blacklist", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getgranuality : 
 * 
 * \details
 * Defines the start of the job or the job_chain order: for each order that is in a blacklist jobchain: for each job chain that has a blacklist. blacklist: One start when a blacklist exists.
 *
 * \return 
 *
 */
    public SOSOptionString  getgranuality() {
        return granuality;
    }

/**
 * \brief setgranuality : 
 * 
 * \details
 * Defines the start of the job or the job_chain order: for each order that is in a blacklist jobchain: for each job chain that has a blacklist. blacklist: One start when a blacklist exists.
 *
 * @param granuality : 
 */
    public void setgranuality (SOSOptionString p_granuality) { 
        this.granuality = p_granuality;
    }

                        

/**
 * \var job : 
 * The name of the job that should be startet Parameters of the job filename: name of the file that is in the blacklist job_chain: name of the job_chain that has a blacklist. created: creation time of the order which is in the blacklist
 *
 */
    @JSOptionDefinition(name = "job", 
    description = "", 
    key = "job", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString job = new SOSOptionString(this, conClassName + ".job", // HashMap-Key
                                                                "", // Titel
                                                                "", // InitValue
                                                                "", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getjob : 
 * 
 * \details
 * The name of the job that should be startet Parameters of the job filename: name of the file that is in the blacklist job_chain: name of the job_chain that has a blacklist. created: creation time of the order which is in the blacklist
 *
 * \return 
 *
 */
    public SOSOptionString  getjob() {
        return job;
    }

/**
 * \brief setjob : 
 * 
 * \details
 * The name of the job that should be startet Parameters of the job filename: name of the file that is in the blacklist job_chain: name of the job_chain that has a blacklist. created: creation time of the order which is in the blacklist
 *
 * @param job : 
 */
    public void setjob (SOSOptionString p_job) { 
        this.job = p_job;
    }

                        

/**
 * \var job_chain : The name of the job chain that should be startet Paramet
 * The name of the job chain that should be startet Parameters of the order filename: name of the file that is in the blacklist job_chain: name of the job_chain that has a blacklist. created: creation time of the order which is in the blacklist
 *
 */
    @JSOptionDefinition(name = "job_chain", 
    description = "The name of the job chain that should be startet Paramet", 
    key = "job_chain", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString job_chain = new SOSOptionString(this, conClassName + ".job_chain", // HashMap-Key
                                                                "The name of the job chain that should be startet Paramet", // Titel
                                                                "", // InitValue
                                                                "", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getjob_chain : The name of the job chain that should be startet Paramet
 * 
 * \details
 * The name of the job chain that should be startet Parameters of the order filename: name of the file that is in the blacklist job_chain: name of the job_chain that has a blacklist. created: creation time of the order which is in the blacklist
 *
 * \return The name of the job chain that should be startet Paramet
 *
 */
    public SOSOptionString  getjob_chain() {
        return job_chain;
    }

/**
 * \brief setjob_chain : The name of the job chain that should be startet Paramet
 * 
 * \details
 * The name of the job chain that should be startet Parameters of the order filename: name of the file that is in the blacklist job_chain: name of the job_chain that has a blacklist. created: creation time of the order which is in the blacklist
 *
 * @param job_chain : The name of the job chain that should be startet Paramet
 */
    public void setjob_chain (SOSOptionString p_job_chain) { 
        this.job_chain = p_job_chain;
    }

                        

/**
 * \var jobscheduler_answer : 
 * 
 *
 */
    @JSOptionDefinition(name = "jobscheduler_answer", 
    description = "", 
    key = "jobscheduler_answer", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString jobscheduler_answer = new SOSOptionString(this, conClassName + ".jobscheduler_answer", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getjobscheduler_answer : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionString  getjobscheduler_answer() {
        return jobscheduler_answer;
    }

/**
 * \brief setjobscheduler_answer : 
 * 
 * \details
 * 
 *
 * @param jobscheduler_answer : 
 */
    public void setjobscheduler_answer (SOSOptionString p_jobscheduler_answer) { 
        this.jobscheduler_answer = p_jobscheduler_answer;
    }

                        

/**
 * \var level : 
 * Specifies the log entry info: a info entry will be made warning: a warn entry will be made error: an error entry will be made
 *
 */
    @JSOptionDefinition(name = "level", 
    description = "", 
    key = "level", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString level = new SOSOptionString(this, conClassName + ".level", // HashMap-Key
                                                                "", // Titel
                                                                "info", // InitValue
                                                                "info", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getlevel : 
 * 
 * \details
 * Specifies the log entry info: a info entry will be made warning: a warn entry will be made error: an error entry will be made
 *
 * \return 
 *
 */
    public SOSOptionString  getlevel() {
        return level;
    }

/**
 * \brief setlevel : 
 * 
 * \details
 * Specifies the log entry info: a info entry will be made warning: a warn entry will be made error: an error entry will be made
 *
 * @param level : 
 */
    public void setlevel (SOSOptionString p_level) { 
        this.level = p_level;
    }

                        
        
        
	public JobSchedulerCheckBlacklistOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public JobSchedulerCheckBlacklistOptionsSuperClass

	public JobSchedulerCheckBlacklistOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerCheckBlacklistOptionsSuperClass

		//

	public JobSchedulerCheckBlacklistOptionsSuperClass (HashMap <String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public JobSchedulerCheckBlacklistOptionsSuperClass (HashMap JSSettings)
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
 */
	public void setAllOptions(HashMap <String, String> pobjJSSettings)   {
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
 */
	@Override
	public void CommandLineArgs(String[] pstrArgs)   {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}
} // public class JobSchedulerCheckBlacklistOptionsSuperClass