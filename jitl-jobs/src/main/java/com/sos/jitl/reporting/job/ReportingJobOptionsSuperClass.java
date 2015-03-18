package com.sos.jitl.reporting.job;

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
 * \class ReportingJobOptionsSuperClass - Inventory
 * 
 * \brief An Options-Super-Class with all Options. This Class will be extended
 * by the "real" Options-class (\see InventoryJobOptions. The "real" Option
 * class will hold all the things, which are normaly overwritten at a new
 * generation of the super-class.
 * 
 */
@JSOptionClass(name = "ReportingJobOptionsSuperClass", description = "ReportingJobOptionsSuperClass")
public class ReportingJobOptionsSuperClass extends JSOptionsClass {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String conClassName = ReportingJobOptionsSuperClass.class
			.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ReportingJobOptionsSuperClass.class);

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
	@JSOptionDefinition(name = "connection_transaction_isolation", description = "", key = "connection_transaction_isolation", type = "SOSOptionInterval", mandatory = true)
	public SOSOptionInteger connection_transaction_isolation = new SOSOptionInteger(
			this, conClassName + ".connection_transaction_isolation", // HashMap-Key
			"", // Titel
			"2", // InitValue
			"2", // 1 = TRANSACTION_READ_UNCOMMITTED, 2 = TRANSACTION_READ_COMMITTED 
			true // isMandatory
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
	 * \var connection_autocommit :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "connection_autocommit", description = "", key = "connection_autocommit", type = "SOSOptionBoolean", mandatory = true)
	public SOSOptionBoolean connection_autocommit = new SOSOptionBoolean(
			this, conClassName + ".connection_autocommit", // HashMap-Key
			"", // Titel
			"false", // InitValue
			"false", // 
			true // isMandatory
	);

	/**
	 * \brief getconnection_autocommit :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionBoolean getconnection_autocommit() {
		return connection_autocommit;
	}

	/**
	 * \brief setconnection_autocommit :
	 * 
	 * \details
	 * 
	 * 
	 * @param connection_autocommit
	 *            :
	 */
	public void setconnection_autocommit(
			SOSOptionBoolean p_connection_autocommit) {
		this.connection_autocommit = p_connection_autocommit;
	}


	/**
	 * 
	 */
	public ReportingJobOptionsSuperClass() {
		this.objParentClass = this.getClass();
	}

	/**
	 * 
	 * @param listener
	 */
	public ReportingJobOptionsSuperClass(JSListener listener) {
		this();
		this.registerMessageListener(listener);
	}

	/**
	 * 
	 * @param jsSettings
	 * @throws Exception
	 */
	public ReportingJobOptionsSuperClass(HashMap<String, String> jsSettings)
			throws Exception {
		this();
		this.setAllOptions(jsSettings);
	}


	/**
	 * \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
	 * Optionen als String
	 * 
	 * \details
	 * 
	 * \see toString \see toOut
	 */
	@SuppressWarnings("unused")
	private String getAllOptionsAsString() {
		final String conMethodName = conClassName + "::getAllOptionsAsString";

		return conClassName + "\n" + this.toString();
	}

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
	 * @throws Exception
	 */
	public void setAllOptions(HashMap<String, String> pobjJSSettings) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setAllOptions";
		flgSetAllOptions = true;
		objSettings = pobjJSSettings;
		super.Settings(objSettings);
		super.setAllOptions(pobjJSSettings);
		flgSetAllOptions = false;
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
	 * \brief CommandLineArgs - Übernehmen der Options/Settings aus der
	 * Kommandozeile
	 * 
	 * \details Die in der Kommandozeile beim Starten der Applikation
	 * angegebenen Parameter werden hier in die HashMap übertragen und danach
	 * den Optionen als Wert zugewiesen.
	 * 
	 * \return void
	 * 
	 * @param args
	 * @throws Exception
	 */
	@Override
	public void CommandLineArgs(String[] args) {
		super.CommandLineArgs(args);
		this.setAllOptions(super.objSettings);
	}
}