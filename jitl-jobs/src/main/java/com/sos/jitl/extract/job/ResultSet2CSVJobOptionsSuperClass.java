package com.sos.jitl.extract.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "ResultSet2CSVJobOptionsSuperClass", description = "ResultSet2CSVJobOptionsSuperClass")
public class ResultSet2CSVJobOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final static String conClassName = ResultSet2CSVJobOptionsSuperClass.class.getSimpleName();

    @JSOptionDefinition(name = "hibernate_configuration_file", description = "", key = "hibernate_configuration_file", type = "SOSOptionString", mandatory = true)
    public SOSOptionString hibernate_configuration_file = new SOSOptionString(this, conClassName + ".hibernate_configuration_file", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    true // isMandatory
    );

    public SOSOptionString gethibernate_configuration_file() {
        return hibernate_configuration_file;
    }

    public void sethibernate_configuration_file(SOSOptionString val) {
        this.hibernate_configuration_file = val;
    }

    /** connection_transaction_isolation : Default 2 TRANSACTION_READ_COMMITTED
     * because of Oracle not have a 1 (TRANSACTION_READ_UNCOMMITTED) */
    @JSOptionDefinition(name = "connection_transaction_isolation", description = "", key = "connection_transaction_isolation", type = "SOSOptionInterval", mandatory = false)
    public SOSOptionInteger connection_transaction_isolation = new SOSOptionInteger(this, conClassName + ".connection_transaction_isolation", // HashMap-Key
    "", // Titel
    "2", // InitValue
    "2", //
    false // isMandatory
    );

    public SOSOptionInteger getconnection_transaction_isolation() {
        return connection_transaction_isolation;
    }

    public void setconnection_transaction_isolation(SOSOptionInteger val) {
        this.connection_transaction_isolation = val;
    }

    @JSOptionDefinition(name = "statement", description = "", key = "statement", type = "SOSOptionString", mandatory = true)
    public SOSOptionString statement = new SOSOptionString(this, conClassName + ".statement", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    true // isMandatory
    );

    public SOSOptionString getstatement() {
        return statement;
    }

    public void setstatement(SOSOptionString val) {
        statement = val;
    }

    @JSOptionDefinition(name = "output_file", description = "", key = "output_file", type = "SOSOptionString", mandatory = true)
    public SOSOptionString output_file = new SOSOptionString(this, conClassName + ".output_file", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    true // isMandatory
    );

    public SOSOptionString getoutput_file() {
        return output_file;
    }

    public void setoutput_file(SOSOptionString val) {
        output_file = val;
    }

    @JSOptionDefinition(name = "delimiter", description = "", key = "delimiter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString delimiter = new SOSOptionString(this, conClassName + ".delimiter", // HashMap-Key
    "", // Titel
    ";", // InitValue
    ";", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getdelimiter() {
        return delimiter;
    }

    public void setdelimiter(SOSOptionString val) {
        delimiter = val;
    }

    @JSOptionDefinition(name = "record_separator", description = "", key = "record_separator", type = "SOSOptionString", mandatory = false)
    public SOSOptionString record_separator = new SOSOptionString(this, conClassName + ".record_separator", // HashMap-Key
    "", // Titel
    "\r\n", // InitValue
    "\r\n", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getrecord_separator() {
        return record_separator;
    }

    public void setrecord_separator(SOSOptionString val) {
        record_separator = val;
    }

    @JSOptionDefinition(name = "skip_header", description = "", key = "skip_header", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean skip_header = new SOSOptionBoolean(this, conClassName + ".skip_header", // HashMap-Key
    "", // Titel
    "false", // InitValue
    "false", // DefaultValue
    false // isMandatory
    );

    public SOSOptionBoolean getskip_header() {
        return skip_header;
    }

    public void setskip_header(SOSOptionBoolean val) {
        skip_header = val;
    }

    @JSOptionDefinition(name = "quote_character", description = "", key = "quote_character", type = "SOSOptionString", mandatory = false)
    public SOSOptionString quote_character = new SOSOptionString(this, conClassName + ".quote_character", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getquote_character() {
        return quote_character;
    }

    public void setquote_character(SOSOptionString val) {
        quote_character = val;
    }

    @JSOptionDefinition(name = "escape_character", description = "", key = "escape_character", type = "SOSOptionString", mandatory = false)
    public SOSOptionString escape_character = new SOSOptionString(this, conClassName + ".escape_character", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getescape_character() {
        return escape_character;
    }

    public void setescape_character(SOSOptionString val) {
        escape_character = val;
    }

    @JSOptionDefinition(name = "null_string", description = "", key = "null_string", type = "SOSOptionString", mandatory = false)
    public SOSOptionString null_string = new SOSOptionString(this, conClassName + ".null_string", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getnull_string() {
        return null_string;
    }

    public void setnull_string(SOSOptionString val) {
        this.null_string = val;
    }

    @JSOptionDefinition(name = "large_result_fetch_size", description = "", key = "large_result_fetch_size", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger large_result_fetch_size = new SOSOptionInteger(this, conClassName + ".large_result_fetch_size", // HashMap-Key
    "", // Titel
    "1", // InitValue
    "1", // DefaultValue
    false // isMandatory
    );

    public SOSOptionInteger getlarge_result_fetch_size() {
        return large_result_fetch_size;
    }

    public void setlarge_result_fetch_size(SOSOptionInteger val) {
        this.large_result_fetch_size = val;
    }

    @JSOptionDefinition(name = "log_info_step", description = "", key = "log_info_step", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger log_info_step = new SOSOptionInteger(this, conClassName + ".log_info_step", // HashMap-Key
    "", // Titel
    "1000", // InitValue
    "1000", // DefaultValue
    false // isMandatory
    );

    public SOSOptionInteger getlog_info_step() {
        return log_info_step;
    }

    public void setlog_info_step(SOSOptionInteger val) {
        this.log_info_step = val;
    }
    
    public ResultSet2CSVJobOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public ResultSet2CSVJobOptionsSuperClass(JSListener listener) {
        this();
        this.registerMessageListener(listener);
    }

    public ResultSet2CSVJobOptionsSuperClass(HashMap<String, String> settings) throws Exception {
        this();
        this.setAllOptions(settings);
    }

    public void setAllOptions(HashMap<String, String> settings) {
        flgSetAllOptions = true;
        objSettings = settings;
        super.Settings(objSettings);
        super.setAllOptions(settings);
        flgSetAllOptions = false;
    }

    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
            , Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void CommandLineArgs(String[] args) {
        super.CommandLineArgs(args);
        this.setAllOptions(super.objSettings);
    }
}