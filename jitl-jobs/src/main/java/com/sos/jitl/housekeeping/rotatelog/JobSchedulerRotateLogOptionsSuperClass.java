package com.sos.jitl.housekeeping.rotatelog;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTime;

@JSOptionClass(name = "JobSchedulerRotateLogOptionsSuperClass", description = "JobSchedulerRotateLogOptionsSuperClass")
public class JobSchedulerRotateLogOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = -6542456636928445160L;
    private final String conClassName = "JobSchedulerRotateLogOptionsSuperClass";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerRotateLogOptionsSuperClass.class);

    /** \option JobSchedulerID \type SOSOptionString \brief JobSchedulerID - The
     * ID of the JobScheduler
     *
     * \details The ID of the JobScheduler
     *
     * \mandatory: true
     *
     * \created 10.09.2014 09:44:34 by KB */
    @JSOptionDefinition(name = "JobSchedulerID", description = "The ID of the JobScheduler", key = "JobSchedulerID", type = "SOSOptionString", mandatory = true)
    public SOSOptionString jobSchedulerID = new SOSOptionString( // ...
    this, // ....
    conClassName + ".JobSchedulerID", // ...
    "The ID of the JobScheduler", // ...
    "", // ...
    "scheduler", // ...
    true);

    public SOSOptionString getJobSchedulerID() {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getJobSchedulerID";

        return jobSchedulerID;
    } // public String getJobSchedulerID

    public JobSchedulerRotateLogOptionsSuperClass setJobSchedulerID(final SOSOptionString pstrValue) {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setJobSchedulerID";
        jobSchedulerID = pstrValue;
        return this;
    } // public JobSchedulerRotateLogOptionsSuperClass setJobSchedulerID

    /** \var delete_file_age : This parameter determines the minimum age at which
     * archived files will be deleted. All files with names that follow the
     * pattern scheduler-[yyyy-mm-dd-hhMMss].[schedulerId].log.gz and which are
     * at least delete_file_age days old will be deleted. The value 0 means
     * "Do not delete" and is the default value when this parameter is not
     * specified. */
    @JSOptionDefinition(name = "delete_file_age", description = "", key = "delete_file_age", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime delete_file_age = new SOSOptionTime(this, conClassName + ".delete_file_age", // HashMap-Key
    "", // Titel
    "0", // InitValue
    "0", // DefaultValue
    false // isMandatory
    );

    /** \brief getdelete_file_age :
     * 
     * \details This parameter determines the minimum age at which archived
     * files will be deleted. All files with names that follow the pattern
     * scheduler-[yyyy-mm-dd-hhMMss].[schedulerId].log.gz and which are at least
     * delete_file_age days old will be deleted. The value 0 means
     * "Do not delete" and is the default value when this parameter is not
     * specified.
     *
     * \return */
    public SOSOptionTime getdelete_file_age() {
        return delete_file_age;
    }

    public SOSOptionTime delete_file_age() {
        return delete_file_age;
    }

    /** \brief setdelete_file_age :
     * 
     * \details This parameter determines the minimum age at which archived
     * files will be deleted. All files with names that follow the pattern
     * scheduler-[yyyy-mm-dd-hhMMss].[schedulerId].log.gz and which are at least
     * delete_file_age days old will be deleted. The value 0 means
     * "Do not delete" and is the default value when this parameter is not
     * specified.
     *
     * @param delete_file_age : */
    public void setdelete_file_age(SOSOptionTime p_delete_file_age) {
        this.delete_file_age = p_delete_file_age;
    }

    public void delete_file_age(SOSOptionTime p_delete_file_age) {
        this.delete_file_age = p_delete_file_age;
    }

    /** \var delete_file_specification : This value of this parameter specifies a
     * regular expression for the log files of the JobScheduler which will be
     * deleted. Changing the default value of this regular expression allows,
     * for example, the log files for a specific JobScheduler to be deleted,
     * should multiple JobSchedulers be logging into the same directory. Note
     * that log files are named according to the pattern
     * scheduler_yyyy-mm-dd-hhmmss.<scheduler_id>.log , where <scheduler_id> is
     * an identifier defined in the JobScheduler XML configuration file. */
    @JSOptionDefinition(name = "delete_file_specification", description = "", key = "delete_file_specification", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp delete_file_specification = new SOSOptionRegExp(this, conClassName + ".delete_file_specification", // HashMap-Key
    "", // Titel
    "^(scheduler)([0-9\\-]+).*(\\.log)(\\.gz)?$", // InitValue
    "^(scheduler)([0-9\\-]+).*(\\.log)(\\.gz)?$", // DefaultValue
    false // isMandatory
    );

    /** \brief getdelete_file_specification :
     * 
     * \details This value of this parameter specifies a regular expression for
     * the log files of the JobScheduler which will be deleted. Changing the
     * default value of this regular expression allows, for example, the log
     * files for a specific JobScheduler to be deleted, should multiple
     * JobSchedulers be logging into the same directory. Note that log files are
     * named according to the pattern
     * scheduler_yyyy-mm-dd-hhmmss.<scheduler_id>.log , where <scheduler_id> is
     * an identifier defined in the JobScheduler XML configuration file.
     *
     * \return */
    public SOSOptionRegExp getdelete_file_specification() {
        return delete_file_specification;
    }

    public SOSOptionRegExp delete_file_specification() {
        return delete_file_specification;
    }

    /** \brief setdelete_file_specification :
     * 
     * \details This value of this parameter specifies a regular expression for
     * the log files of the JobScheduler which will be deleted. Changing the
     * default value of this regular expression allows, for example, the log
     * files for a specific JobScheduler to be deleted, should multiple
     * JobSchedulers be logging into the same directory. Note that log files are
     * named according to the pattern
     * scheduler_yyyy-mm-dd-hhmmss.<scheduler_id>.log , where <scheduler_id> is
     * an identifier defined in the JobScheduler XML configuration file.
     *
     * @param delete_file_specification : */
    public void setdelete_file_specification(SOSOptionRegExp p_delete_file_specification) {
        this.delete_file_specification = p_delete_file_specification;
    }

    public void delete_file_specification(SOSOptionRegExp p_delete_file_specification) {
        this.delete_file_specification = p_delete_file_specification;
    }

    /** \var file_age : This parameter determines the minimum age at which files
     * will be compressed and saved as archives. All files with names following
     * the pattern scheduler-[yyyy-mm-dd-hhMMss].[schedulerId].log and which are
     * at least file_age days old will be compressed. */
    @JSOptionDefinition(name = "file_age", description = "", key = "file_age", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime file_age = new SOSOptionTime(this, conClassName + ".file_age", // HashMap-Key
    "", // Titel
    "14d", // InitValue
    "14d", // DefaultValue
    false // isMandatory
    );
    public SOSOptionTime compressFileAge = (SOSOptionTime) file_age.SetAlias(conClassName + ".compress_file_age");
    public SOSOptionTime compress_file_age = (SOSOptionTime) file_age.SetAlias(conClassName + ".compress_file_age");

    /** \brief getfile_age :
     * 
     * \details This parameter determines the minimum age at which files will be
     * compressed and saved as archives. All files with names following the
     * pattern scheduler-[yyyy-mm-dd-hhMMss].[schedulerId].log and which are at
     * least file_age days old will be compressed.
     *
     * \return */
    public SOSOptionTime getfile_age() {
        return file_age;
    }

    public SOSOptionTime file_age() {
        return file_age;
    }

    /** \brief setfile_age :
     * 
     * \details This parameter determines the minimum age at which files will be
     * compressed and saved as archives. All files with names following the
     * pattern scheduler-[yyyy-mm-dd-hhMMss].[schedulerId].log and which are at
     * least file_age days old will be compressed.
     *
     * @param file_age : */
    public void setfile_age(SOSOptionTime p_file_age) {
        this.file_age = p_file_age;
    }

    public void file_age(SOSOptionTime p_file_age) {
        this.file_age = p_file_age;
    }

    /** \var file_path : This parameter specifies a directory for the
     * JobScheduler log files. If this parameter is not specified, then the
     * current log directory of the JobScheduler will be used. */
    @JSOptionDefinition(name = "file_path", description = "", key = "file_path", type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName file_path = new SOSOptionFolderName(this, conClassName + ".file_path", // HashMap-Key
    "directory for the JobScheduler log files", // Titel
    "${SCHEDULER_DATA}/logs", // InitValue
    "${SCHEDULER_DATA}/logs", // DefaultValue
    true // isMandatory
    );
    public SOSOptionFolderName jobSchedulerLogFilesPath = (SOSOptionFolderName) file_path.SetAlias(".JobScheduler_LogFiles_Path");

    /** \brief getfile_path :
     * 
     * \details This parameter specifies a directory for the JobScheduler log
     * files. If this parameter is not specified, then the current log directory
     * of the JobScheduler will be used.
     *
     * \return */
    public SOSOptionFolderName getfile_path() {
        return file_path;
    }

    public SOSOptionFolderName file_path() {
        return file_path;
    }

    public SOSOptionFolderName JobSchedulerLogFilesPath() {
        return file_path;
    }

    /** \brief setfile_path :
     * 
     * \details This parameter specifies a directory for the JobScheduler log
     * files. If this parameter is not specified, then the current log directory
     * of the JobScheduler will be used.
     *
     * @param file_path : */
    public void setfile_path(SOSOptionFolderName p_file_path) {
        this.file_path = p_file_path;
    }

    public void file_path(SOSOptionFolderName p_file_path) {
        this.file_path = p_file_path;
    }

    /** \var file_specification : This parameter specifies a regular expression
     * for the log files of the JobScheduler. Changing the default value of this
     * regular expression allows, for example, the log files for a specific
     * JobScheduler to be rotated, should multiple JobSchedulers be logging into
     * the same directory. Note that log files are named according to the
     * pattern scheduler_yyyy-mm-dd-hhmmss.<scheduler_id>.log , where
     * <scheduler_id> is an identifier defined in the JobScheduler XML
     * configuration file. */
    @JSOptionDefinition(name = "file_specification", description = "", key = "file_specification", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp file_specification = new SOSOptionRegExp(this, conClassName + ".file_specification", // HashMap-Key
    "", // Titel
    "^(scheduler).*([0-9\\-]+).*(\\.log)$", // InitValue
    "^(scheduler).*([0-9\\-]+).*(\\.log)$", // DefaultValue
    false // isMandatory
    );

    /** \brief getfile_specification :
     * 
     * \details This parameter specifies a regular expression for the log files
     * of the JobScheduler. Changing the default value of this regular
     * expression allows, for example, the log files for a specific JobScheduler
     * to be rotated, should multiple JobSchedulers be logging into the same
     * directory. Note that log files are named according to the pattern
     * scheduler_yyyy-mm-dd-hhmmss.<scheduler_id>.log , where <scheduler_id> is
     * an identifier defined in the JobScheduler XML configuration file.
     *
     * \return */
    public SOSOptionRegExp getfile_specification() {
        return file_specification;
    }

    public SOSOptionRegExp file_specification() {
        return file_specification;
    }

    /** \brief setfile_specification :
     * 
     * \details This parameter specifies a regular expression for the log files
     * of the JobScheduler. Changing the default value of this regular
     * expression allows, for example, the log files for a specific JobScheduler
     * to be rotated, should multiple JobSchedulers be logging into the same
     * directory. Note that log files are named according to the pattern
     * scheduler_yyyy-mm-dd-hhmmss.<scheduler_id>.log , where <scheduler_id> is
     * an identifier defined in the JobScheduler XML configuration file.
     *
     * @param file_specification : */
    public void setfile_specification(SOSOptionRegExp p_file_specification) {
        this.file_specification = p_file_specification;
    }

    public void file_specification(SOSOptionRegExp p_file_specification) {
        this.file_specification = p_file_specification;
    }

    public SOSOptionRegExp compress_file_spec = (SOSOptionRegExp) file_specification.SetAlias(conClassName + ".compress_file_spec");

    public JobSchedulerRotateLogOptionsSuperClass() {
        objParentClass = this.getClass();
    } // public JobSchedulerRotateLogOptionsSuperClass

    public JobSchedulerRotateLogOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerRotateLogOptionsSuperClass

    //

    public JobSchedulerRotateLogOptionsSuperClass(HashMap<String, String> JSSettings) {
        this();
        this.setAllOptions(JSSettings);
    } // public JobSchedulerRotateLogOptionsSuperClass (HashMap JSSettings)

    /** \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
     * Optionen als String
     *
     * \details
     * 
     * \see toString \see toOut */
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

    /** \brief setAllOptions - übernimmt die OptionenWerte aus der HashMap
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
     * @param pobjJSSettings */
    @Override
    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setAllOptions";
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    } // public void setAllOptions (HashMap <String, String> JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
            , Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()

    /** \brief CommandLineArgs - Übernehmen der Options/Settings aus der
     * Kommandozeile
     *
     * \details Die in der Kommandozeile beim Starten der Applikation
     * angegebenen Parameter werden hier in die HashMap übertragen und danach
     * den Optionen als Wert zugewiesen.
     *
     * \return void
     *
     * @param pstrArgs */
    @Override
    public void CommandLineArgs(String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }
} // public class JobSchedulerRotateLogOptionsSuperClass