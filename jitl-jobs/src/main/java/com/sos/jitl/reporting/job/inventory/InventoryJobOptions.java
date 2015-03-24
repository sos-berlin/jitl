package com.sos.jitl.reporting.job.inventory;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.jitl.reporting.job.ReportingJobOptionsSuperClass;

import org.apache.log4j.Logger;

/**
 * \class InventoryJobOptions - Inventory
 * 
 * \brief An Options as a container for the Options super class. The Option
 * class will hold all the things, which would be otherwise overwritten at a
 * re-creation of the super-class.
 * 
 */
@JSOptionClass(name = "InventoryJobOptions", description = "InventoryJobOptions")
public class InventoryJobOptions extends ReportingJobOptionsSuperClass {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private final String conClassName = InventoryJobOptions.class
			.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(InventoryJobOptions.class);

	/**
	 * Überschreibt ReportingJobOptionsSuperClass
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
	 * \var current_scheduler_configuration_directory : Prefix "current", weil
	 * einfacher "scheduler_xxx..." möglicherweise geschützt ist
	 * 
	 * Wird im InventoryJobJSAdapterClass ermittelt
	 * 
	 */
	@JSOptionDefinition(name = "current_scheduler_configuration_directory", description = "", key = "current_scheduler_configuration_directory", type = "SOSOptionString", mandatory = false)
	public SOSOptionString current_scheduler_configuration_directory = new SOSOptionString(
			this, conClassName + ".current_scheduler_configuration_directory", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getcurrent_scheduler_configuration_directory :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getcurrent_scheduler_configuration_directory() {
		return current_scheduler_configuration_directory;
	}

	/**
	 * \brief setcurrent_scheduler_configuration_directory :
	 * 
	 * \details
	 * 
	 * 
	 * @param current_scheduler_configuration_directory
	 *            :
	 */
	public void setcurrent_scheduler_configuration_directory(SOSOptionString val) {
		this.current_scheduler_configuration_directory = val;
	}

	/**
	 * \var current_scheduler_id :
	 * 
	 * Wird im InventoryJobJSAdapterClass ermittelt
	 * 
	 */
	@JSOptionDefinition(name = "current_scheduler_id", description = "", key = "current_scheduler_id", type = "SOSOptionString", mandatory = false)
	public SOSOptionString current_scheduler_id = new SOSOptionString(this,
			conClassName + ".current_scheduler_id", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getcurrent_scheduler_id :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getcurrent_scheduler_id() {
		return current_scheduler_id;
	}

	/**
	 * \brief setcurrent_scheduler_id :
	 * 
	 * \details
	 * 
	 * 
	 * @param current_scheduler_id
	 *            :
	 */
	public void setcurrent_scheduler_id(SOSOptionString val) {
		this.current_scheduler_id = val;
	}

	/**
	 * \var current_scheduler_hostname :
	 * 
	 * Wird im InventoryJobJSAdapterClass ermittelt
	 * 
	 */
	@JSOptionDefinition(name = "current_scheduler_hostname", description = "", key = "current_scheduler_hostname", type = "SOSOptionString", mandatory = false)
	public SOSOptionString current_scheduler_hostname = new SOSOptionString(
			this, conClassName + ".current_scheduler_hostname", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getcurrent_scheduler_hostname :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getcurrent_scheduler_hostname() {
		return current_scheduler_hostname;
	}

	/**
	 * \brief setcurrent_scheduler_hostname :
	 * 
	 * \details
	 * 
	 * 
	 * @param current_scheduler_id
	 *            :
	 */
	public void setcurrent_scheduler_hostname(SOSOptionString val) {
		this.current_scheduler_hostname = val;
	}

	/**
	 * \var current_scheduler_port :
	 * 
	 * Wird im InventoryJobJSAdapterClass ermittelt
	 * 
	 */
	@JSOptionDefinition(name = "current_scheduler_port", description = "", key = "current_scheduler_port", type = "SOSOptionInteger", mandatory = false)
	public SOSOptionInteger current_scheduler_port = new SOSOptionInteger(this,
			conClassName + ".current_scheduler_port", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getcurrent_scheduler_port :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionInteger getcurrent_scheduler_port() {
		return current_scheduler_port;
	}

	/**
	 * \brief setcurrent_scheduler_port :
	 * 
	 * \details
	 * 
	 * 
	 * @param current_scheduler_port
	 *            :
	 */
	public void setcurrent_scheduler_port(SOSOptionInteger val) {
		this.current_scheduler_port = val;
	}

	
	/**
	 * constructors
	 */

	public InventoryJobOptions() {
	}

	/**
	 * 
	 * @param listener
	 */
	public InventoryJobOptions(JSListener listener) {
		this();
		this.registerMessageListener(listener);
	}

	/**
	 * 
	 * @param jsSettings
	 * @throws Exception
	 */
	public InventoryJobOptions(HashMap<String, String> jsSettings)
			throws Exception {
		super(jsSettings);
	}

	/**
	 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
	 * 
	 * \details
	 * 
	 * @throws Exception
	 * 
	 * @throws Exception
	 *             - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
	 */
	@Override
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		} catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	}

}
