package com.sos.jitl.splitter;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionStringValueList;
 
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

 

    /**
     * \var create_sync_context :
     *
     *
     */
    @JSOptionDefinition(name = "create_sync_context", description = "", key = "create_sync_context", type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean  createSyncContext   = new SOSOptionBoolean(this, conClassName + ".create_sync_context", // HashMap-Key
                                                    "", // Titel
                                                    "", // InitValue
                                                    "true", // DefaultValue
                                                    false // isMandatory
                                            );

    /**
     * \brief getcreate_sync_context :
     *
     * \details
     *
     *
     * \return
     *
     */
    public SOSOptionBoolean getcreate_sync_context() {
        return createSyncContext;
    }

    /**
     * \brief setsync_state_name :
     *
     * \details
     *
     *
     * @param SyncStateName :
     */
    public void setcreate_sync_context(final SOSOptionBoolean p_create_sync_context) {
        createSyncContext = p_create_sync_context;
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
	 * \brief setAllOptions - �bernimmt die OptionenWerte aus der HashMap
	 *
	 * \details In der als Parameter anzugebenden HashMap sind Schl�ssel (Name)
	 * und Wert der jeweiligen Option als Paar angegeben. Ein Beispiel f�r den
	 * Aufbau einer solchen HashMap findet sich in der Beschreibung dieser
	 * Klasse (\ref TestData "setJobSchedulerSSHJobOptions"). In dieser Routine
	 * werden die Schl�ssel analysiert und, falls gefunden, werden die
	 * dazugeh�rigen Werte den Properties dieser Klasse zugewiesen.
	 *
	 * Nicht bekannte Schl�ssel werden ignoriert.
	 *
	 * \see JSOptionsClass::getItem
	 *
	 * @param pobjJSSettings
	 * @throws Exception
	 */
	@Override
	public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setAllOptions";
		flgSetAllOptions = true;
		objSettings = pobjJSSettings;
		super.Settings(objSettings);
		super.setAllOptions(pobjJSSettings);
		flgSetAllOptions = false;
	} // public void setAllOptions (HashMap <String, String> JSSettings)

	/**
	 * \brief CheckMandatory - pr�ft alle Muss-Optionen auf Werte
	 *
	 * \details
	 * @throws Exception
	 *
	 * @throws Exception
	 * - wird ausgel�st, wenn eine mandatory-Option keinen Wert hat
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
	 * \brief CommandLineArgs - �bernehmen der Options/Settings aus der
	 * Kommandozeile
	 *
	 * \details Die in der Kommandozeile beim Starten der Applikation
	 * angegebenen Parameter werden hier in die HashMap �bertragen und danach
	 * den Optionen als Wert zugewiesen.
	 *
	 * \return void
	 *
	 * @param pstrArgs
	 * @throws Exception
	 */
	@Override
	public void CommandLineArgs(final String[] pstrArgs) {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}
} // public class JobChainSplitterOptionsSuperClass