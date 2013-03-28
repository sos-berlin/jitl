package com.sos.jitl.splitter;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionStringValueList;

/**
 * \class 		JobChainSplitterOptionsSuperClass - Start a parallel processing in a jobchain
 *
 * \brief
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see JobChainSplitterOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-121986169113382203.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler_4446\config\JOETemplates\java\xsl\JSJobDoc2JSOptionSuperClass.xsl from http://www.sos-berlin.com at 20130315155436
 * \endverbatim
 * \section OptionsTable Tabelle der vorhandenen Optionen
 *
 * Tabelle mit allen Optionen
 *
 * MethodName
 * Title
 * Setting
 * Description
 * IsMandatory
 * DataType
 * InitialValue
 * TestValue
 *
 *
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JobChainSplitterOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "JobChainSplitterOptionsSuperClass", description = "JobChainSplitterOptionsSuperClass")
public class JobChainSplitterOptionsSuperClass extends JSOptionsClass {
	/**
	 *
	 */
	private static final long	serialVersionUID	= -5275742216092117420L;
	private final String	conClassName	= "JobChainSplitterOptionsSuperClass";
	@SuppressWarnings("unused")
	private static Logger	logger			= Logger.getLogger(JobChainSplitterOptionsSuperClass.class);
	@SuppressWarnings("unused")
	private final String					conSVNVersion	= "$Id: JSEventsClient.java 18220 2012-10-18 07:46:10Z kb $";

	/**
	 * \var next_state_name :
	 *
	 *
	 */
	@JSOptionDefinition(name = "next_state_name", description = "", key = "next_state_name", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	next_state_name	= new SOSOptionString(this, conClassName + ".next_state_name", // HashMap-Key
													"", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getnext_state_name :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public SOSOptionString getnext_state_name() {
		return next_state_name;
	}

	/**
	 * \brief setnext_state_name :
	 *
	 * \details
	 *
	 *
	 * @param next_state_name :
	 */
	public void setnext_state_name(final SOSOptionString p_next_state_name) {
		next_state_name = p_next_state_name;
	}

	/**
	 * \var state_names :
	 *
	 *
	 */
	@JSOptionDefinition(name = "state_names", description = "", key = "state_names", type = "SOSOptionString", mandatory = true)
	public SOSOptionStringValueList	StateNames	= new SOSOptionStringValueList(this, conClassName + ".state_names", // HashMap-Key
												"", // Titel
												"", // InitValue
												"", // DefaultValue
												true // isMandatory
										);

	/**
	 * \brief getstate_names :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public SOSOptionStringValueList getStateNames() {
		return StateNames;
	}

	/**
	 * \brief setstate_names :
	 *
	 * \details
	 *
	 *
	 * @param StateNames :
	 */
	public void setStateNames(final SOSOptionStringValueList p_state_names) {
		StateNames = p_state_names;
	}

	/**
	 * \var sync_state_name :
	 *
	 *
	 */
	@JSOptionDefinition(name = "sync_state_name", description = "", key = "sync_state_name", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	SyncStateName	= new SOSOptionString(this, conClassName + ".sync_state_name", // HashMap-Key
													"", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getsync_state_name :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public SOSOptionString getsync_state_name() {
		return SyncStateName;
	}

	/**
	 * \brief setsync_state_name :
	 *
	 * \details
	 *
	 *
	 * @param SyncStateName :
	 */
	public void setsync_state_name(final SOSOptionString p_sync_state_name) {
		SyncStateName = p_sync_state_name;
	}

	public JobChainSplitterOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public JobChainSplitterOptionsSuperClass

	public JobChainSplitterOptionsSuperClass(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobChainSplitterOptionsSuperClass

	//

	public JobChainSplitterOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public JobChainSplitterOptionsSuperClass (HashMap JSSettings)

	/**
	 * \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
	 * Optionen als String
	 *
	 * \details
	 *
	 * \see toString
	 * \see toOut
	 */
	@SuppressWarnings("unused")
	private String getAllOptionsAsString() {
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
	 * @throws Exception
	 */
	@Override
	public void setAllOptions(final HashMap<String, String> pobjJSSettings) throws Exception {
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
	 * @throws Exception
	 */
	@Override
	public void CommandLineArgs(final String[] pstrArgs) throws Exception {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}
} // public class JobChainSplitterOptionsSuperClass