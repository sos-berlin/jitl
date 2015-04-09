package com.sos.jitl.extract.job;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionString;

/**
 * 
 * @author Robert Ehrlich
 * 
 */
@JSOptionClass(name = "CSV2CSVJobOptionsSuperClass", description = "CSV2CSVJobOptionsSuperClass")
public class CSV2CSVJobOptionsSuperClass extends JSOptionsClass {
	private static final long serialVersionUID = 1L;
	private final static String conClassName = CSV2CSVJobOptionsSuperClass.class
			.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory
			.getLogger(CSV2CSVJobOptionsSuperClass.class);

	
	/**
	 * \var statement :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "output_file", 
			description = "", key = "output_file", 
			type = "SOSOptionString", mandatory = true)
	public SOSOptionString output_file = new SOSOptionString(this,
			conClassName + ".output_file", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief getoutput_file :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getoutput_file() {
		return output_file;
	}

	/**
	 * \brief setoutput_file :
	 * 
	 * \details
	 * 
	 * 
	 * @param statement
	 *            :
	 */
	public void setoutput_file(SOSOptionString val) {
		output_file = val;
	}

	/**
	 * \var input_file :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "input_file", 
			description = "", key = "input_file", 
			type = "SOSOptionString", mandatory = true)
	public SOSOptionString input_file = new SOSOptionString(this,
			conClassName + ".input_file", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief getinput_file :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getinput_file() {
		return input_file;
	}

	/**
	 * \brief setinput_file :
	 * 
	 * \details
	 * 
	 * 
	 * @param statement
	 *            :
	 */
	public void setinput_file(SOSOptionString val) {
		input_file = val;
	}
	
	/**
	 * \var input_file_delimiter :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "input_file_delimiter", 
			description = "", key = "input_file_delimiter", 
			type = "SOSOptionString", mandatory = false)
	public SOSOptionString input_file_delimiter = new SOSOptionString(this,
			conClassName + ".input_file_delimiter", // HashMap-Key
			"", // Titel
			";", // InitValue
			";", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getinput_file_delimiter :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getinput_file_delimiter() {
		return input_file_delimiter;
	}

	/**
	 * \brief setinput_file_delimiter :
	 * 
	 * \details
	 * 
	 * 
	 * @param input_file_delimiter
	 *            :
	 */
	public void setinput_file_delimiter(SOSOptionString val) {
		input_file_delimiter = val;
	}
	
	/**
	 * \var output_file_delimiter :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "delimiter", 
			description = "", key = "delimiter", 
			type = "SOSOptionString", mandatory = false)
	public SOSOptionString delimiter = new SOSOptionString(this,
			conClassName + ".delimiter", // HashMap-Key
			"", // Titel
			";", // InitValue
			";", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getdelimiter :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getdelimiter() {
		return delimiter;
	}

	/**
	 * \brief setdelimiter :
	 * 
	 * \details
	 * 
	 * 
	 * @param delimiter
	 *            :
	 */
	public void setdelimiter(SOSOptionString val) {
		delimiter = val;
	}
	
	
	/**
	 * \var record_separator :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "record_separator", 
			description = "", key = "record_separator", 
			type = "SOSOptionString", mandatory = false)
	public SOSOptionString record_separator = new SOSOptionString(this,
			conClassName + ".record_separator", // HashMap-Key
			"", // Titel
			"\r\n", // InitValue
			"\r\n", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getrecord_separator :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getrecord_separator() {
		return record_separator;
	}

	/**
	 * \brief setrecord_separator :
	 * 
	 * \details
	 * 
	 * 
	 * @param record_separator
	 *            :
	 */
	public void setrecord_separator(SOSOptionString val) {
		record_separator = val;
	}

	/**
	 * \var input_file_record_separator :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "input_file_record_separator", 
			description = "", key = "input_file_record_separator", 
			type = "SOSOptionString", mandatory = false)
	public SOSOptionString input_file_record_separator = new SOSOptionString(this,
			conClassName + ".input_file_record_separator", // HashMap-Key
			"", // Titel
			"\r\n", // InitValue
			"\r\n", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getinput_file_record_separator :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getinput_file_record_separator() {
		return input_file_record_separator;
	}

	/**
	 * \brief setinput_file_record_separator :
	 * 
	 * \details
	 * 
	 * 
	 * @param input_file_record_separator
	 *            :
	 */
	public void setinput_file_record_separator(SOSOptionString val) {
		input_file_record_separator = val;
	}

	
	/**
	 * \var skip_header :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "skip_header", description = "", key = "skip_header", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean skip_header = new SOSOptionBoolean(this,
			conClassName + ".skip_header", // HashMap-Key
			"", // Titel
			"false", // InitValue
			"false", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getskip_header :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionBoolean getskip_header() {
		return skip_header;
	}

	/**
	 * \brief setskip_header :
	 * 
	 * \details
	 * 
	 * 
	 * @param skip_header
	 *            :
	 */
	public void setskip_header(SOSOptionBoolean val) {
		skip_header = val;
	}

	/**
	 * \var quote_character :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "quote_character", description = "", key = "quote_character", type = "SOSOptionString", mandatory = false)
	public SOSOptionString quote_character = new SOSOptionString(this,
			conClassName + ".quote_character", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getquote_character :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getquote_character() {
		return quote_character;
	}

	/**
	 * \brief setquote_character :
	 * 
	 * \details
	 * 
	 * 
	 * @param quote_character
	 *            :
	 */
	public void setquote_character(SOSOptionString val) {
		quote_character = val;
	}

	/**
	 * \var input_file_quote_character :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "input_file_quote_character", description = "", key = "input_file_quote_character", type = "SOSOptionString", mandatory = false)
	public SOSOptionString input_file_quote_character = new SOSOptionString(this,
			conClassName + ".input_file_quote_character", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getinput_file_quote_character :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getinput_file_quote_character() {
		return input_file_quote_character;
	}

	/**
	 * \brief setinput_file_quote_character :
	 * 
	 * \details
	 * 
	 * 
	 * @param input_file_quote_character
	 *            :
	 */
	public void setinput_file_quote_character(SOSOptionString val) {
		input_file_quote_character = val;
	}

	
	/**
	 * \var escape_character :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "escape_character", description = "", key = "escape_character", type = "SOSOptionString", mandatory = false)
	public SOSOptionString escape_character = new SOSOptionString(this,
			conClassName + ".escape_character", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue /sos
			false // isMandatory
	);

	/**
	 * \brief getescape_character :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getescape_character() {
		return escape_character;
	}

	/**
	 * \brief setescape_character :
	 * 
	 * \details
	 * 
	 * 
	 * @param exclude_job_chains
	 *            :
	 */
	public void setescape_character(SOSOptionString val) {
		escape_character = val;
	}


	/**
	 * \var input_file_escape_character :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "input_file_escape_character", description = "", key = "input_file_escape_character", type = "SOSOptionString", mandatory = false)
	public SOSOptionString input_file_escape_character = new SOSOptionString(this,
			conClassName + ".input_file_escape_character", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue /sos
			false // isMandatory
	);

	/**
	 * \brief getinput_file_escape_character :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getinput_file_escape_character() {
		return input_file_escape_character;
	}

	/**
	 * \brief setinput_file_escape_character :
	 * 
	 * \details
	 * 
	 * 
	 * @param input_file_escape_character
	 *            :
	 */
	public void setinput_file_escape_character(SOSOptionString val) {
		input_file_escape_character = val;
	}

	
	/**
	 * \var null_string :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "null_string", description = "", key = "null_string", type = "SOSOptionString", mandatory = false)
	public SOSOptionString null_string = new SOSOptionString(this,
			conClassName + ".null_string", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getnull_string :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getnull_string() {
		return null_string;
	}

	/**
	 * \brief setnull_string :
	 * 
	 * \details
	 * 
	 * 
	 * @param null_string
	 *            :
	 */
	public void setnull_string(SOSOptionString val) {
		this.null_string = val;
	}

	
	/**
	 * \var input_file_null_string :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "input_file_null_string", description = "", key = "input_file_null_string", type = "SOSOptionString", mandatory = false)
	public SOSOptionString input_file_null_string = new SOSOptionString(this,
			conClassName + ".input_file_null_string", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getinput_file_null_string :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getinput_file_null_string() {
		return input_file_null_string;
	}

	/**
	 * \brief setinput_file_null_string :
	 * 
	 * \details
	 * 
	 * 
	 * @param input_file_null_string
	 *            :
	 */
	public void setinput_file_null_string(SOSOptionString val) {
		this.input_file_null_string = val;
	}

	
	/**
	 * \var fields :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "fields", description = "", key = "fields", type = "SOSOptionString", mandatory = false)
	public SOSOptionString fields = new SOSOptionString(this,
			conClassName + ".fields", // HashMap-Key
			"", // Titel
			"*", // InitValue
			"*", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getfields :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getfields() {
		return fields;
	}

	/**
	 * \brief setfields :
	 * 
	 * \details
	 * 
	 * 
	 * @param fields
	 *            :
	 */
	public void setfields(SOSOptionString val) {
		this.fields = val;
	}
	
	/**
     * 
     */
	public CSV2CSVJobOptionsSuperClass() {
		objParentClass = this.getClass();
	}

	/**
	 * 
	 * @param pobjListener
	 */
	public CSV2CSVJobOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	}

	/**
	 * 
	 * @param JSSettings
	 * @throws Exception
	 */
	public CSV2CSVJobOptionsSuperClass(HashMap<String, String> JSSettings)
			throws Exception {
		this();
		this.setAllOptions(JSSettings);
	}

	/**
	 * 
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
	 * 
	 */
	@Override
	public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
			, Exception {
		try {
			super.CheckMandatory();
		} catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	}

	/**
	 * 
	 */
	@Override
	public void CommandLineArgs(String[] pstrArgs) {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}
}