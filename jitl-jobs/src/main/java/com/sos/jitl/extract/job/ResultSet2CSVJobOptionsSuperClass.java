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
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;

/**
 * 
 * @author Robert Ehrlich
 * 
 */
@JSOptionClass(name = "ResultSet2CSVJobOptionsSuperClass", description = "ResultSet2CSVJobOptionsSuperClass")
public class ResultSet2CSVJobOptionsSuperClass extends JSOptionsClass {
	private static final long serialVersionUID = 1L;
	private final static String conClassName = ResultSet2CSVJobOptionsSuperClass.class
			.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory
			.getLogger(ResultSet2CSVJobOptionsSuperClass.class);

	
	/**
	 * \var hibernate_configuration_file :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "hibernate_configuration_file", description = "", key = "hibernate_configuration_file", type = "SOSOptionString", mandatory = true)
	public SOSOptionString hibernate_configuration_file = new SOSOptionString(
			this, conClassName + ".hibernate_configuration_file", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			true // isMandatory
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
	public SOSOptionString gethibernate_configuration_file() {
		return hibernate_configuration_file;
	}

	/**
	 * \brief sethibernate_configuration_file :
	 * 
	 * \details
	 * 
	 * 
	 * @param hibernate_configuration_file
	 *            :
	 */
	public void sethibernate_configuration_file(SOSOptionString val) {
		this.hibernate_configuration_file = val;
	}

	/**
	 * \var connection_transaction_isolation :
	 * Default 2 wegen Oracle, weil Oracle kein TRANSACTION_READ_UNCOMMITTED unterstützt, sonst wäre 1
	 * 
	 */
	@JSOptionDefinition(name = "connection_transaction_isolation", description = "", key = "connection_transaction_isolation", type = "SOSOptionInterval", mandatory = false)
	public SOSOptionInteger connection_transaction_isolation = new SOSOptionInteger(
			this, conClassName + ".connection_transaction_isolation", // HashMap-Key
			"", // Titel
			"2", // InitValue
			"2", // 1 = TRANSACTION_READ_UNCOMMITTED, 2 = TRANSACTION_READ_COMMITTED 
			false // isMandatory
	);

	/**
	 * \brief getconnection_transaction_isolation :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionInteger getconnection_transaction_isolation() {
		return connection_transaction_isolation;
	}

	/**
	 * \brief setconnection_transaction_isolation :
	 * 
	 * \details
	 * 
	 * 
	 * @param connection_transaction_isolation
	 *            :
	 */
	public void setconnection_transaction_isolation(
			SOSOptionInteger p_connection_transaction_isolation) {
		this.connection_transaction_isolation = p_connection_transaction_isolation;
	}

	/**
	 * \var statement :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "statement", 
			description = "", key = "statement", 
			type = "SOSOptionString", mandatory = true)
	public SOSOptionString statement = new SOSOptionString(this,
			conClassName + ".statement", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief getstatement :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getstatement() {
		return statement;
	}

	/**
	 * \brief setstatement :
	 * 
	 * \details
	 * 
	 * 
	 * @param statement
	 *            :
	 */
	public void setstatement(SOSOptionString val) {
		statement = val;
	}

	
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
	 * \var delimiter :
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
     * 
     */
	public ResultSet2CSVJobOptionsSuperClass() {
		objParentClass = this.getClass();
	}

	/**
	 * 
	 * @param pobjListener
	 */
	public ResultSet2CSVJobOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	}

	/**
	 * 
	 * @param JSSettings
	 * @throws Exception
	 */
	public ResultSet2CSVJobOptionsSuperClass(HashMap<String, String> JSSettings)
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