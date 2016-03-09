package com.sos.jitl.httppost;

import java.util.HashMap;

import com.sos.JSHelper.Options.*;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Options.*;

@JSOptionClass(name = "JobSchedulerHttpPostJobOptionsSuperClass", description = "JobSchedulerHttpPostJobOptionsSuperClass")
public class JobSchedulerHttpPostJobOptionsSuperClass extends JSOptionsClass {

    private final String conClassName = "JobSchedulerHttpPostJobOptionsSuperClass";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerHttpPostJobOptionsSuperClass.class);

    /** \var content_type : The content type and character set encoding are
     * retrieved from the content of xml and html input files by default. This
     * parameter, however, overwrites any content type given in input files. */
    @JSOptionDefinition(name = "content_type", description = "", key = "content_type", type = "SOSOptionString", mandatory = false)
    public SOSOptionString content_type = new SOSOptionString(this, conClassName + ".content_type", // HashMap-Key
    "", // Titel
    " ", // InitValue
    " ", // DefaultValue
    false // isMandatory
    );

    /** \brief getcontent_type :
     * 
     * \details The content type and character set encoding are retrieved from
     * the content of xml and html input files by default. This parameter,
     * however, overwrites any content type given in input files.
     *
     * \return */
    public SOSOptionString getcontent_type() {
        return content_type;
    }

    /** \brief setcontent_type :
     * 
     * \details The content type and character set encoding are retrieved from
     * the content of xml and html input files by default. This parameter,
     * however, overwrites any content type given in input files.
     *
     * @param content_type : */
    public void setcontent_type(SOSOptionString p_content_type) {
        this.content_type = p_content_type;
    }

    /** \var encoding : This parameter provides the character set encoding value.
     * By default the encoding of xml and html input files is retrieved from the
     * content of these files. */
    @JSOptionDefinition(name = "encoding", description = "", key = "encoding", type = "SOSOptionString", mandatory = false)
    public SOSOptionString encoding = new SOSOptionString(this, conClassName + ".encoding", // HashMap-Key
    "", // Titel
    " ", // InitValue
    " ", // DefaultValue
    false // isMandatory
    );

    /** \brief getencoding :
     * 
     * \details This parameter provides the character set encoding value. By
     * default the encoding of xml and html input files is retrieved from the
     * content of these files.
     *
     * \return */
    public SOSOptionString getencoding() {
        return encoding;
    }

    /** \brief setencoding :
     * 
     * \details This parameter provides the character set encoding value. By
     * default the encoding of xml and html input files is retrieved from the
     * content of these files.
     *
     * @param encoding : */
    public void setencoding(SOSOptionString p_encoding) {
        this.encoding = p_encoding;
    }

    /** \var input : This parameter contains a valid directory name or file name.
     * If a directory name is specified, then all files contained in this
     * directory will be posted in indeterminate order. If a file name is given,
     * then only the file with this name will be posted. */
    @JSOptionDefinition(name = "input", description = "", key = "input", type = "SOSOptionString", mandatory = true)
    public SOSOptionString input = new SOSOptionString(this, conClassName + ".input", // HashMap-Key
    "", // Titel
    " ", // InitValue
    " ", // DefaultValue
    true // isMandatory
    );
    public SOSOptionString input_directory = (SOSOptionString) input.SetAlias(conClassName + ".input");

    /** \brief getinput :
     * 
     * \details This parameter contains a valid directory name or file name. If
     * a directory name is specified, then all files contained in this directory
     * will be posted in indeterminate order. If a file name is given, then only
     * the file with this name will be posted.
     *
     * \return */
    public SOSOptionString getinput() {
        return input;
    }

    /** \brief setinput :
     * 
     * \details This parameter contains a valid directory name or file name. If
     * a directory name is specified, then all files contained in this directory
     * will be posted in indeterminate order. If a file name is given, then only
     * the file with this name will be posted.
     *
     * @param input : */
    public void setinput(SOSOptionString p_input) {
        this.input = p_input;
    }

    /** \var input_filespec : A regular expression may be specified as a filter
     * for the input files in a directory. */
    @JSOptionDefinition(name = "input_filespec", description = "", key = "input_filespec", type = "SOSOptionString", mandatory = false)
    public SOSOptionString input_filespec = new SOSOptionString(this, conClassName + ".input_filespec", // HashMap-Key
    "", // Titel
    "^(.*)$", // InitValue
    "^(.*)$", // DefaultValue
    false // isMandatory
    );

    /** \brief getinput_filespec :
     * 
     * \details A regular expression may be specified as a filter for the input
     * files in a directory.
     *
     * \return */
    public SOSOptionString getinput_filespec() {
        return input_filespec;
    }

    /** \brief setinput_filespec :
     * 
     * \details A regular expression may be specified as a filter for the input
     * files in a directory.
     *
     * @param input_filespec : */
    public void setinput_filespec(SOSOptionString p_input_filespec) {
        this.input_filespec = p_input_filespec;
    }

    /** \var output : This parameter contains a valid directory name or file name
     * to store the output from the URL to which the original input file(s) were
     * posted. If a directory name is given then the output file names will
     * match the input file names. If a file name is given then all output will
     * be stored in this file. */
    @JSOptionDefinition(name = "output", description = "", key = "output", type = "SOSOptionString", mandatory = false)
    public SOSOptionString output = new SOSOptionString(this, conClassName + ".output", // HashMap-Key
    "", // Titel
    " ", // InitValue
    " ", // DefaultValue
    false // isMandatory
    );
    public SOSOptionString output_directory = (SOSOptionString) output.SetAlias(conClassName + ".output");

    /** \brief getoutput :
     * 
     * \details This parameter contains a valid directory name or file name to
     * store the output from the URL to which the original input file(s) were
     * posted. If a directory name is given then the output file names will
     * match the input file names. If a file name is given then all output will
     * be stored in this file.
     *
     * \return */
    public SOSOptionString getoutput() {
        return output;
    }

    /** \brief setoutput :
     * 
     * \details This parameter contains a valid directory name or file name to
     * store the output from the URL to which the original input file(s) were
     * posted. If a directory name is given then the output file names will
     * match the input file names. If a file name is given then all output will
     * be stored in this file.
     *
     * @param output : */
    public void setoutput(SOSOptionString p_output) {
        this.output = p_output;
    }

    @JSOptionDefinition(name = "timeout", description = "", key = "timeout", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger timeout = new SOSOptionInteger(this, conClassName + ".timeout", // HashMap-Key
    "", // Titel
    "0", // InitValue
    "0", // DefaultValue
    false // isMandatory
    );

    public SOSOptionInteger gettimeout() {
        return timeout;
    }

    public void settimeout(SOSOptionInteger p_timeout) {
        this.timeout = p_timeout;
    }

    @JSOptionDefinition(name = "url", description = "", key = "url", type = "SOSOptionString", mandatory = true)
    public SOSOptionString url = new SOSOptionString(this, conClassName + ".url", // HashMap-Key
    "", // Titel
    " ", // InitValue
    " ", // DefaultValue
    true // isMandatory
    );

    /** \brief geturl :
     * 
     * \details This parameter specifies the URL to which the given files are to
     * be posted.
     *
     * \return */
    public SOSOptionString geturl() {
        return url;
    }

    /** \brief seturl :
     * 
     * \details This parameter specifies the URL to which the given files are to
     * be posted.
     *
     * @param url : */
    public void seturl(SOSOptionString p_url) {
        this.url = p_url;
    }

    public JobSchedulerHttpPostJobOptionsSuperClass() {
        objParentClass = this.getClass();
    } // public JobSchedulerHttpPostJobOptionsSuperClass

    public JobSchedulerHttpPostJobOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerHttpPostJobOptionsSuperClass

    //

    public JobSchedulerHttpPostJobOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    } // public JobSchedulerHttpPostJobOptionsSuperClass (HashMap JSSettings)

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
} // public class JobSchedulerHttpPostJobOptionsSuperClass