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

@JSOptionClass(name = "CSV2CSVJobOptionsSuperClass", description = "CSV2CSVJobOptionsSuperClass")
public class CSV2CSVJobOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final static String conClassName = CSV2CSVJobOptionsSuperClass.class.getSimpleName();

    @JSOptionDefinition(name = "output_file", description = "", key = "output_file", type = "SOSOptionString", mandatory = true)
    public SOSOptionString output_file = new SOSOptionString(this, conClassName + ".output_file", "", "", "", true);

    public SOSOptionString getoutput_file() {
        return output_file;
    }

    public void setoutput_file(SOSOptionString val) {
        output_file = val;
    }

    @JSOptionDefinition(name = "input_file", description = "", key = "input_file", type = "SOSOptionString", mandatory = true)
    public SOSOptionString input_file = new SOSOptionString(this, conClassName + ".input_file", "", "", "", true);

    public SOSOptionString getinput_file() {
        return input_file;
    }

    public void setinput_file(SOSOptionString val) {
        input_file = val;
    }

    @JSOptionDefinition(name = "input_file_delimiter", description = "", key = "input_file_delimiter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString input_file_delimiter = new SOSOptionString(this, conClassName + ".input_file_delimiter", "", ";", ";", false);

    public SOSOptionString getinput_file_delimiter() {
        return input_file_delimiter;
    }

    public void setinput_file_delimiter(SOSOptionString val) {
        input_file_delimiter = val;
    }

    @JSOptionDefinition(name = "delimiter", description = "", key = "delimiter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString delimiter = new SOSOptionString(this, conClassName + ".delimiter", "", ";", ";", false);

    public SOSOptionString getdelimiter() {
        return delimiter;
    }

    public void setdelimiter(SOSOptionString val) {
        delimiter = val;
    }

    @JSOptionDefinition(name = "record_separator", description = "", key = "record_separator", type = "SOSOptionString", mandatory = false)
    public SOSOptionString record_separator = new SOSOptionString(this, conClassName + ".record_separator", "", "\r\n", "\r\n", false);

    public SOSOptionString getrecord_separator() {
        return record_separator;
    }

    public void setrecord_separator(SOSOptionString val) {
        record_separator = val;
    }

    @JSOptionDefinition(name = "input_file_record_separator", description = "", key = "input_file_record_separator", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString input_file_record_separator = new SOSOptionString(this, conClassName + ".input_file_record_separator", "",
            "\r\n", "\r\n", false);

    public SOSOptionString getinput_file_record_separator() {
        return input_file_record_separator;
    }

    public void setinput_file_record_separator(SOSOptionString val) {
        input_file_record_separator = val;
    }

    @JSOptionDefinition(name = "skip_header", description = "", key = "skip_header", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean skip_header = new SOSOptionBoolean(this, conClassName + ".skip_header", "", "false", "false", false);

    public SOSOptionBoolean getskip_header() {
        return skip_header;
    }

    public void setskip_header(SOSOptionBoolean val) {
        skip_header = val;
    }

    @JSOptionDefinition(name = "quote_character", description = "", key = "quote_character", type = "SOSOptionString", mandatory = false)
    public SOSOptionString quote_character = new SOSOptionString(this, conClassName + ".quote_character", "", "", "", false);

    public SOSOptionString getquote_character() {
        return quote_character;
    }

    public void setquote_character(SOSOptionString val) {
        quote_character = val;
    }

    @JSOptionDefinition(name = "input_file_quote_character", description = "", key = "input_file_quote_character", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString input_file_quote_character = new SOSOptionString(this, conClassName + ".input_file_quote_character", "", "", "", false);

    public SOSOptionString getinput_file_quote_character() {
        return input_file_quote_character;
    }

    public void setinput_file_quote_character(SOSOptionString val) {
        input_file_quote_character = val;
    }

    @JSOptionDefinition(name = "escape_character", description = "", key = "escape_character", type = "SOSOptionString", mandatory = false)
    public SOSOptionString escape_character = new SOSOptionString(this, conClassName + ".escape_character", "", "", "", false);

    public SOSOptionString getescape_character() {
        return escape_character;
    }

    public void setescape_character(SOSOptionString val) {
        escape_character = val;
    }

    @JSOptionDefinition(name = "input_file_escape_character", description = "", key = "input_file_escape_character", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString input_file_escape_character = new SOSOptionString(this, conClassName + ".input_file_escape_character", "", "", "", false);

    public SOSOptionString getinput_file_escape_character() {
        return input_file_escape_character;
    }

    public void setinput_file_escape_character(SOSOptionString val) {
        input_file_escape_character = val;
    }

    @JSOptionDefinition(name = "null_string", description = "", key = "null_string", type = "SOSOptionString", mandatory = false)
    public SOSOptionString null_string = new SOSOptionString(this, conClassName + ".null_string", "", "", "", false);

    public SOSOptionString getnull_string() {
        return null_string;
    }

    public void setnull_string(SOSOptionString val) {
        this.null_string = val;
    }

    @JSOptionDefinition(name = "input_file_null_string", description = "", key = "input_file_null_string", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString input_file_null_string = new SOSOptionString(this, conClassName + ".input_file_null_string", "", "", "", false);

    public SOSOptionString getinput_file_null_string() {
        return input_file_null_string;
    }

    public void setinput_file_null_string(SOSOptionString val) {
        this.input_file_null_string = val;
    }

    @JSOptionDefinition(name = "fields", description = "", key = "fields", type = "SOSOptionString", mandatory = false)
    public SOSOptionString fields = new SOSOptionString(this, conClassName + ".fields", "", "*", "*", false);

    public SOSOptionString getfields() {
        return fields;
    }

    public void setfields(SOSOptionString val) {
        this.fields = val;
    }

    @JSOptionDefinition(name = "log_info_step", description = "", key = "log_info_step", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger log_info_step = new SOSOptionInteger(this, conClassName + ".log_info_step", "", "1000", "1000", false);

    public SOSOptionInteger getlog_info_step() {
        return log_info_step;
    }

    public void setlog_info_step(SOSOptionInteger val) {
        this.log_info_step = val;
    }

    public CSV2CSVJobOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public CSV2CSVJobOptionsSuperClass(JSListener listener) {
        this();
        this.registerMessageListener(listener);
    }

    public CSV2CSVJobOptionsSuperClass(HashMap<String, String> settings) throws Exception {
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
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void CommandLineArgs(String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

}