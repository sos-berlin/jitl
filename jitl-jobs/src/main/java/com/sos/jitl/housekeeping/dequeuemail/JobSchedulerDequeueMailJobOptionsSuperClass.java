

package com.sos.jitl.housekeeping.dequeuemail;

import java.util.HashMap;

import com.sos.JSHelper.Options.*;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener; 
import org.apache.log4j.Logger;

import com.sos.JSHelper.Options.*;
 
@JSOptionClass(name = "JobSchedulerDequeueMailJobOptionsSuperClass", description = "JobSchedulerDequeueMailJobOptionsSuperClass")
public class JobSchedulerDequeueMailJobOptionsSuperClass extends JSOptionsClass {
	private final String					conClassName						= "JobSchedulerDequeueMailJobOptionsSuperClass";
		@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JobSchedulerDequeueMailJobOptionsSuperClass.class);

		

/**
 * \var db : 
 * This setting states that a database is used and that in order to update their shipping state, mails are to be sought in a table.
 *
 */
    @JSOptionDefinition(name = "db", 
    description = "", 
    key = "db", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString db = new SOSOptionString(this, conClassName + ".db", // HashMap-Key
                                                                "", // Titel
                                                                "false", // InitValue
                                                                "false", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getdb : 
 * 
 * \details
 * This setting states that a database is used and that in order to update their shipping state, mails are to be sought in a table.
 *
 * \return 
 *
 */
    public SOSOptionString  getdb() {
        return db;
    }

/**
 * \brief setdb : 
 * 
 * \details
 * This setting states that a database is used and that in order to update their shipping state, mails are to be sought in a table.
 *
 * @param db : 
 */
    public void setdb (SOSOptionString p_db) { 
        this.db = p_db;
    }

                        

/**
 * \var failed_prefix : prefix for failed mail files
 * prefix for failed mail files
 *
 */
    @JSOptionDefinition(name = "failed_prefix", 
    description = "prefix for failed mail files", 
    key = "failed_prefix", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString failed_prefix = new SOSOptionString(this, conClassName + ".failed_prefix", // HashMap-Key
                                                                "prefix for failed mail files", // Titel
                                                                "failed.", // InitValue
                                                                "failed.", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getfailed_prefix : prefix for failed mail files
 * 
 * \details
 * prefix for failed mail files
 *
 * \return prefix for failed mail files
 *
 */
    public SOSOptionString  getfailed_prefix() {
        return failed_prefix;
    }

/**
 * \brief setfailed_prefix : prefix for failed mail files
 * 
 * \details
 * prefix for failed mail files
 *
 * @param failed_prefix : prefix for failed mail files
 */
    public void setfailed_prefix (SOSOptionString p_failed_prefix) { 
        this.failed_prefix = p_failed_prefix;
    }

                        

/**
 * \var file : 
 * This parameter provides the name of the file containing a mail to be dequeued. The path is not specified with the filename but in the queue_directory job parameter.
 *
 */
    @JSOptionDefinition(name = "file", 
    description = "", 
    key = "file", 
    type = "SOSOptionString", 
    mandatory = true)
    
    public SOSOptionString file = new SOSOptionString(this, conClassName + ".file", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                true // isMandatory
                    );

/**
 * \brief getfile : 
 * 
 * \details
 * This parameter provides the name of the file containing a mail to be dequeued. The path is not specified with the filename but in the queue_directory job parameter.
 *
 * \return 
 *
 */
    public SOSOptionString  getfile() {
        return file;
    }

/**
 * \brief setfile : 
 * 
 * \details
 * This parameter provides the name of the file containing a mail to be dequeued. The path is not specified with the filename but in the queue_directory job parameter.
 *
 * @param file : 
 */
    public void setfile (SOSOptionString p_file) { 
        this.file = p_file;
    }

                        

/**
 * \var ini_path : 
 * 
 *
 */
    @JSOptionDefinition(name = "ini_path", 
    description = "", 
    key = "ini_path", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString ini_path = new SOSOptionString(this, conClassName + ".ini_path", // HashMap-Key
                                                                "", // Titel
                                                                "value from JobScheduler instance", // InitValue
                                                                "value from JobScheduler instance", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getini_path : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionString  getini_path() {
        return ini_path;
    }

/**
 * \brief setini_path : 
 * 
 * \details
 * 
 *
 * @param ini_path : 
 */
    public void setini_path (SOSOptionString p_ini_path) { 
        this.ini_path = p_ini_path;
    }

                        

/**
 * \var log_directory : 
 * 
 *
 */
    @JSOptionDefinition(name = "log_directory", 
    description = "", 
    key = "log_directory", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString log_directory = new SOSOptionString(this, conClassName + ".log_directory", // HashMap-Key
                                                                "", // Titel
                                                                "", // InitValue
                                                                "", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getlog_directory : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionString  getlog_directory() {
        return log_directory;
    }

/**
 * \brief setlog_directory : 
 * 
 * \details
 * 
 *
 * @param log_directory : 
 */
    public void setlog_directory (SOSOptionString p_log_directory) { 
        this.log_directory = p_log_directory;
    }

                        

/**
 * \var log_only : 
 * 
 *
 */
    @JSOptionDefinition(name = "log_only", 
    description = "", 
    key = "log_only", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionBoolean log_only = new SOSOptionBoolean(this, conClassName + ".log_only", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getlog_only : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionBoolean  getlog_only() {
        return log_only;
    }

/**
 * \brief setlog_only : 
 * 
 * \details
 * 
 *
 * @param log_only : 
 */
    public void setlog_only (SOSOptionBoolean p_log_only) { 
        this.log_only = p_log_only;
    }

                        

/**
 * \var max_delivery : 
 * This parameter specifies the maximum number of attempts to be made to send an email. If an email is sent then an X-Header named X-SOSMail-delivery-counter with the value of the current number of trials is created. If the value of this parameter is 0, then an infinite number of attempts may be made to send a mail. For other values the shipment will be cancelled once this number of attempts has been reached. In this case, the mail will be stored in the dequeue directory in a file with the prefix failed. .
 *
 */
    @JSOptionDefinition(name = "max_delivery", 
    description = "", 
    key = "max_delivery", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionInteger max_delivery = new SOSOptionInteger(this, conClassName + ".max_delivery", // HashMap-Key
                                                                "", // Titel
                                                                "0", // InitValue
                                                                "0", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getmax_delivery : 
 * 
 * \details
 * This parameter specifies the maximum number of attempts to be made to send an email. If an email is sent then an X-Header named X-SOSMail-delivery-counter with the value of the current number of trials is created. If the value of this parameter is 0, then an infinite number of attempts may be made to send a mail. For other values the shipment will be cancelled once this number of attempts has been reached. In this case, the mail will be stored in the dequeue directory in a file with the prefix failed. .
 *
 * \return 
 *
 */
    public SOSOptionInteger  getmax_delivery() {
        return max_delivery;
    }

/**
 * \brief setmax_delivery : 
 * 
 * \details
 * This parameter specifies the maximum number of attempts to be made to send an email. If an email is sent then an X-Header named X-SOSMail-delivery-counter with the value of the current number of trials is created. If the value of this parameter is 0, then an infinite number of attempts may be made to send a mail. For other values the shipment will be cancelled once this number of attempts has been reached. In this case, the mail will be stored in the dequeue directory in a file with the prefix failed. .
 *
 * @param max_delivery : 
 */
    public void setmax_delivery (SOSOptionInteger p_max_delivery) { 
        this.max_delivery = p_max_delivery;
    }

                        

/**
 * \var queue_directory : 
 * This parameter contains the name of the directory in which mails have been stored. If this value is left blank then the job will use the dequeueing directory that was configured for the JobScheduler and which is returned by the API Mail.dequeue() method.
 *
 */
    @JSOptionDefinition(name = "queue_directory", 
    description = "", 
    key = "queue_directory", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString queue_directory = new SOSOptionString(this, conClassName + ".queue_directory", // HashMap-Key
                                                                "", // Titel
                                                                "Mail.dequeue()", // InitValue
                                                                "Mail.dequeue()", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getqueue_directory : 
 * 
 * \details
 * This parameter contains the name of the directory in which mails have been stored. If this value is left blank then the job will use the dequeueing directory that was configured for the JobScheduler and which is returned by the API Mail.dequeue() method.
 *
 * \return 
 *
 */
    public SOSOptionString  getqueue_directory() {
        return queue_directory;
    }

/**
 * \brief setqueue_directory : 
 * 
 * \details
 * This parameter contains the name of the directory in which mails have been stored. If this value is left blank then the job will use the dequeueing directory that was configured for the JobScheduler and which is returned by the API Mail.dequeue() method.
 *
 * @param queue_directory : 
 */
    public void setqueue_directory (SOSOptionString p_queue_directory) { 
        this.queue_directory = p_queue_directory;
    }

                        

/**
 * \var queue_pattern : pattern for filenames of enqueued mails
 * pattern for filenames of enqueued mails
 *
 */
    @JSOptionDefinition(name = "queue_pattern", 
    description = "pattern for filenames of enqueued mails", 
    key = "queue_pattern", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString queue_pattern = new SOSOptionString(this, conClassName + ".queue_pattern", // HashMap-Key
                                                                "pattern for filenames of enqueued mails", // Titel
                                                                "yyyy-MM-dd.HHmmss.S", // InitValue
                                                                "yyyy-MM-dd.HHmmss.S", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getqueue_pattern : pattern for filenames of enqueued mails
 * 
 * \details
 * pattern for filenames of enqueued mails
 *
 * \return pattern for filenames of enqueued mails
 *
 */
    public SOSOptionString  getqueue_pattern() {
        return queue_pattern;
    }

/**
 * \brief setqueue_pattern : pattern for filenames of enqueued mails
 * 
 * \details
 * pattern for filenames of enqueued mails
 *
 * @param queue_pattern : pattern for filenames of enqueued mails
 */
    public void setqueue_pattern (SOSOptionString p_queue_pattern) { 
        this.queue_pattern = p_queue_pattern;
    }

                        

/**
 * \var queue_prefix : 
 * If an email cannot be sent due to mail server problems, then it will be stored as a file. This prefix is then used in the file name.
 *
 */
    @JSOptionDefinition(name = "queue_prefix", 
    description = "", 
    key = "queue_prefix", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString queue_prefix = new SOSOptionString(this, conClassName + ".queue_prefix", // HashMap-Key
                                                                "", // Titel
                                                                "sos.", // InitValue
                                                                "sos.", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getqueue_prefix : 
 * 
 * \details
 * If an email cannot be sent due to mail server problems, then it will be stored as a file. This prefix is then used in the file name.
 *
 * \return 
 *
 */
    public SOSOptionString  getqueue_prefix() {
        return queue_prefix;
    }

/**
 * \brief setqueue_prefix : 
 * 
 * \details
 * If an email cannot be sent due to mail server problems, then it will be stored as a file. This prefix is then used in the file name.
 *
 * @param queue_prefix : 
 */
    public void setqueue_prefix (SOSOptionString p_queue_prefix) { 
        this.queue_prefix = p_queue_prefix;
    }

                        

/**
 * \var queue_prefix_spec : 
 * This parameter contains a regular expression to specify the files that should be dequeued. The parameter is ignored if this job is triggered by an order.
 *
 */
    @JSOptionDefinition(name = "queue_prefix_spec", 
    description = "", 
    key = "queue_prefix_spec", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString queue_prefix_spec = new SOSOptionString(this, conClassName + ".queue_prefix_spec", // HashMap-Key
                                                                "", // Titel
                                                                "^(sos.*)(?&lt;!\\~)$", // InitValue
                                                                "^(sos.*)(?&lt;!\\~)$", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getqueue_prefix_spec : 
 * 
 * \details
 * This parameter contains a regular expression to specify the files that should be dequeued. The parameter is ignored if this job is triggered by an order.
 *
 * \return 
 *
 */
    public SOSOptionString  getqueue_prefix_spec() {
        return queue_prefix_spec;
    }

/**
 * \brief setqueue_prefix_spec : 
 * 
 * \details
 * This parameter contains a regular expression to specify the files that should be dequeued. The parameter is ignored if this job is triggered by an order.
 *
 * @param queue_prefix_spec : 
 */
    public void setqueue_prefix_spec (SOSOptionString p_queue_prefix_spec) { 
        this.queue_prefix_spec = p_queue_prefix_spec;
    }

                        

/**
 * \var smtp_host : 
 * 
 *
 */
    @JSOptionDefinition(name = "smtp_host", 
    description = "", 
    key = "smtp_host", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString smtp_host = new SOSOptionString(this, conClassName + ".smtp_host", // HashMap-Key
                                                                "", // Titel
                                                                "value from JobScheduler instance", // InitValue
                                                                "value from JobScheduler instance", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getsmtp_host : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionString  getsmtp_host() {
        return smtp_host;
    }

/**
 * \brief setsmtp_host : 
 * 
 * \details
 * 
 *
 * @param smtp_host : 
 */
    public void setsmtp_host (SOSOptionString p_smtp_host) { 
        this.smtp_host = p_smtp_host;
    }

                        
        
        
	public JobSchedulerDequeueMailJobOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public JobSchedulerDequeueMailJobOptionsSuperClass

	public JobSchedulerDequeueMailJobOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerDequeueMailJobOptionsSuperClass

		//

	public JobSchedulerDequeueMailJobOptionsSuperClass (HashMap <String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public JobSchedulerDequeueMailJobOptionsSuperClass (HashMap JSSettings)
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
} // public class JobSchedulerDequeueMailJobOptionsSuperClass