package com.sos.jitl.textprocessor;

import java.util.HashMap;

import com.sos.JSHelper.Options.*;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Options.*;

@JSOptionClass(name = "JobSchedulerTextProcessorOptionsSuperClass", description = "JobSchedulerTextProcessorOptionsSuperClass")
public class JobSchedulerTextProcessorOptionsSuperClass extends JSOptionsClass {

    private final String conClassName = "JobSchedulerTextProcessorOptionsSuperClass";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerTextProcessorOptionsSuperClass.class);

    /** \var command : Command: count: counts the hits of a string add: adds a
     * string at the end of the file. read: reads line -n. Possible value for n
     * are numbers and first/last The command can contain the param. Samples:
     * count test add xxxx read 6 read last */
    @JSOptionDefinition(name = "command", description = "", key = "command", type = "SOSOptionString", mandatory = true)
    public SOSOptionString command = new SOSOptionString(this, conClassName + ".command", // HashMap-Key
    "", // Titel
    " ", // InitValue
    " ", // DefaultValue
    true // isMandatory
    );

    /** \brief getcommand :
     * 
     * \details Command: count: counts the hits of a string add: adds a string
     * at the end of the file. read: reads line -n. Possible value for n are
     * numbers and first/last The command can contain the param. Samples: count
     * test add xxxx read 6 read last
     *
     * \return */
    public SOSOptionString getcommand() {
        return command;
    }

    /** \brief setcommand :
     * 
     * \details Command: count: counts the hits of a string add: adds a string
     * at the end of the file. read: reads line -n. Possible value for n are
     * numbers and first/last The command can contain the param. Samples: count
     * test add xxxx read 6 read last
     *
     * @param command : */
    public void setcommand(SOSOptionString p_command) {
        this.command = p_command;
    }

    /** \var filename : Name of the file. */
    @JSOptionDefinition(name = "filename", description = "", key = "filename", type = "SOSOptionString", mandatory = true)
    public SOSOptionString filename = new SOSOptionString(this, conClassName + ".filename", // HashMap-Key
    "", // Titel
    " ", // InitValue
    " ", // DefaultValue
    true // isMandatory
    );

    /** \brief getfilename :
     * 
     * \details Name of the file.
     *
     * \return */
    public SOSOptionString getfilename() {
        return filename;
    }

    /** \brief setfilename :
     * 
     * \details Name of the file.
     *
     * @param filename : */
    public void setfilename(SOSOptionString p_filename) {
        this.filename = p_filename;
    }

    /** \var param : */
    @JSOptionDefinition(name = "param", description = "", key = "param", type = "SOSOptionString", mandatory = false)
    public SOSOptionString param = new SOSOptionString(this, conClassName + ".param", // HashMap-Key
    "", // Titel
    " ", // InitValue
    " ", // DefaultValue
    false // isMandatory
    );

    /** \brief getparam :
     * 
     * \details
     * 
     *
     * \return */
    public SOSOptionString getparam() {
        return param;
    }

    /** \brief setparam :
     * 
     * \details
     * 
     *
     * @param param : */
    public void setparam(SOSOptionString p_param) {
        this.param = p_param;
    }

    /** \var result : */
    @JSOptionDefinition(name = "result", description = "", key = "result", type = "SOSOptionString", mandatory = false)
    public SOSOptionString result = new SOSOptionString(this, conClassName + ".result", // HashMap-Key
    "", // Titel
    " ", // InitValue
    " ", // DefaultValue
    false // isMandatory
    );

    /** \brief getresult :
     * 
     * \details
     * 
     *
     * \return */
    public SOSOptionString getresult() {
        return result;
    }

    /** \brief setresult :
     * 
     * \details
     * 
     *
     * @param result : */
    public void setresult(SOSOptionString p_result) {
        this.result = p_result;
    }

    /** \var scheduler_textprocessor_result : Command: Return value: count:
     * counted number of char countCaseSensitive: counted number of char add:
     * param read: the readed line insert: param */
    @JSOptionDefinition(name = "scheduler_textprocessor_result", description = "", key = "scheduler_textprocessor_result", type = "SOSOptionString", mandatory = false)
    public SOSOptionString scheduler_textprocessor_result = new SOSOptionString(this, conClassName + ".scheduler_textprocessor_result", // HashMap-Key
    "", // Titel
    " ", // InitValue
    " ", // DefaultValue
    false // isMandatory
    );

    /** \brief getscheduler_textprocessor_result :
     * 
     * \details Command: Return value: count: counted number of char
     * countCaseSensitive: counted number of char add: param read: the readed
     * line insert: param
     *
     * \return */
    public SOSOptionString getscheduler_textprocessor_result() {
        return scheduler_textprocessor_result;
    }

    /** \brief setscheduler_textprocessor_result :
     * 
     * \details Command: Return value: count: counted number of char
     * countCaseSensitive: counted number of char add: param read: the readed
     * line insert: param
     *
     * @param scheduler_textprocessor_result : */
    public void setscheduler_textprocessor_result(SOSOptionString p_scheduler_textprocessor_result) {
        this.scheduler_textprocessor_result = p_scheduler_textprocessor_result;
    }

    public JobSchedulerTextProcessorOptionsSuperClass() {
        objParentClass = this.getClass();
    } // public JobSchedulerTextProcessorOptionsSuperClass

    public JobSchedulerTextProcessorOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerTextProcessorOptionsSuperClass

    //

    public JobSchedulerTextProcessorOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    } // public JobSchedulerTextProcessorOptionsSuperClass (HashMap JSSettings)

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
} // public class JobSchedulerTextProcessorOptionsSuperClass