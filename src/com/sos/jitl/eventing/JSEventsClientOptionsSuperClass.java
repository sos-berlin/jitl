package com.sos.jitl.eventing;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionJSTransferMethod;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.Options.SOSOptionTimeHorizon;
import com.sos.scheduler.model.ISOSSchedulerSocket;

/**
 * \class 		JSEventsClientOptionsSuperClass - Submit and Delete Events
 *
 * \brief
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see JSEventsClientOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-4778075809216214864.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler\config\JOETemplates\java\xsl\JSJobDoc2JSOptionSuperClass.xsl from http://www.sos-berlin.com at 20130109134235
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
	pobjHM.put ("		JSEventsClientOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "JSEventsClientOptionsSuperClass", description = "JSEventsClientOptionsSuperClass")
public class JSEventsClientOptionsSuperClass extends JSOptionsClass implements ISOSSchedulerSocket {
	/**
	 *
	 */
	private static final long	serialVersionUID	= -6733730581916617748L;
    @SuppressWarnings("unused")
    private final String        conClassName    = this.getClass().getSimpleName();

    @SuppressWarnings("unused")
    private static final String conSVNVersion   = "$Id$";
    private final Logger        logger          = Logger.getLogger(this.getClass());

	/**
	 * \option UDPPortNumber
	 * \type SOSOptionPortNumber
	 * \brief UDPPortNumber - The tcp-port of the scheduler instance
	 *
	 * \details
	 * The scheduler communication port
	 *
	 * \mandatory: true
	 *
	 * \created 18.01.2011 13:23:19 by KB
	 */
	@JSOptionDefinition(name = "UDPPortNumber", description = "The scheduler communication port for UDP", key = "UDPPortNumber", type = "SOSOptionPortNumber", mandatory = true)
	public SOSOptionPortNumber	UDPPortNumber	= new SOSOptionPortNumber( // ...
														this, // ....
														conClassName + ".UDPPortNumber", // ...
														"The scheduler communication port for UDP", // ...
														"4444", // ...
														"4444", // ...
														true);

	/* (non-Javadoc)
	 * @see com.sos.scheduler.model.ISOSSchedulerSocket#getUDPPortNumber()
	 */
	@Override
	public int getUDPPortNumber() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getPortNumber";
		return UDPPortNumber.value();
	} // public String getUDPPortNumber

	/* (non-Javadoc)
	 * @see com.sos.scheduler.model.ISOSSchedulerSocket#setUDPPortNumber(java.lang.String)
	 */
	@Override
	public ISOSSchedulerSocket setUDPPortNumber(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setPortNumber";
		UDPPortNumber.Value(pstrValue);
		return this;
	} // public SchedulerObjectFactoryOptions setUDPPortNumber

	/**
	 * \option Event_Parameter
	 * \type SOSOptionString
	 * \brief Event_Parameter - Parameters which will pass to the event
	 *
	 * \details
	 * The Name of the Params which will passed to the events.
	 *
	 * \mandatory: false
	 *
	 * \created 05.02.2013 17:27:19 by KB
	 */
	@JSOptionDefinition(name = "Event_Parameter", description = "The Name of the Params which will passed to the events.", key = "Event_Parameter", type = "SOSOptionString", mandatory = false)
	public SOSOptionString		EventParameter		= new SOSOptionString( // ...
															this, // ....
															conClassName + ".Event_Parameter", // ...
															"The Name of the Params which will passed to the events.", // ...
															"", // ...
															"", // ...
															false);

	public String getEvent_Parameter() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getEvent_Parameter";

		return EventParameter.Value();
	} // public String getEvent_Parameter

	public JSEventsClientOptionsSuperClass setEvent_Parameter(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setEvent_Parameter";
		EventParameter.Value(pstrValue);
		return this;
	} // public JSEventsClientOptionsSuperClass setEvent_Parameter

	/**
	 * \var del_events :
	 * Event ID (arbitrary)
	 *
	 */
	@JSOptionDefinition(name = "del_events", description = "", key = "del_events", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	del_events	= new SOSOptionString(this, conClassName + ".del_events", // HashMap-Key
												"", // Titel
												"", // InitValue
												"", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getdel_events :
	 *
	 * \details
	 * Event ID (arbitrary)
	 *
	 * \return
	 *
	 */
	public SOSOptionString getdel_events() {
		return del_events;
	}

	/**
	 * \brief setdel_events :
	 *
	 * \details
	 * Event ID (arbitrary)
	 *
	 * @param del_events :
	 */
	public void setdel_events(final SOSOptionString p_del_events) {
		del_events = p_del_events;
	}

	/**
	 * \var scheduler_event_action :
	 * Action to be performed: add - add Event remove - remove Event(s) When removing an event, the parameters scheduler_event_job scheduler_event_host scheduler_event_port scheduler_event_exit_code (along with the parameters which are used for adding) can be used to specify the event.
	 *
	 */
	@JSOptionDefinition(name = "scheduler_event_action", description = "", key = "scheduler_event_action", type = "SOSOptionString", mandatory = true)
	public SOSOptionString	scheduler_event_action	= new SOSOptionString(this, conClassName + ".scheduler_event_action", // HashMap-Key
															"", // Titel
															"add", // InitValue
															"add", // DefaultValue
															true // isMandatory
													);

	/**
	 * \brief getscheduler_event_action :
	 *
	 * \details
	 * Action to be performed: add - add Event remove - remove Event(s) When removing an event, the parameters scheduler_event_job scheduler_event_host scheduler_event_port scheduler_event_exit_code (along with the parameters which are used for adding) can be used to specify the event.
	 *
	 * \return
	 *
	 */
	public SOSOptionString getscheduler_event_action() {
		return scheduler_event_action;
	}

	/**
	 * \brief setscheduler_event_action :
	 *
	 * \details
	 * Action to be performed: add - add Event remove - remove Event(s) When removing an event, the parameters scheduler_event_job scheduler_event_host scheduler_event_port scheduler_event_exit_code (along with the parameters which are used for adding) can be used to specify the event.
	 *
	 * @param scheduler_event_action :
	 */
	public void setscheduler_event_action(final SOSOptionString p_scheduler_event_action) {
		scheduler_event_action = p_scheduler_event_action;
	}

	public SOSOptionString	operation				= (SOSOptionString) scheduler_event_action.SetAlias(conClassName + ".operation");

	/**
	 * \var scheduler_event_class :
	 * Event class (arbitrary)
	 *
	 */
	@JSOptionDefinition(name = "scheduler_event_class", description = "", key = "scheduler_event_class", type = "SOSOptionString", mandatory = true)
	public SOSOptionString	scheduler_event_class	= new SOSOptionString(this, conClassName + ".scheduler_event_class", // HashMap-Key
															"", // Titel
															"", // InitValue
															"", // DefaultValue
															true // isMandatory
													);

	/**
	 * \brief getscheduler_event_class :
	 *
	 * \details
	 * Event class (arbitrary)
	 *
	 * \return
	 *
	 */
	public SOSOptionString getscheduler_event_class() {
		return scheduler_event_class;
	}

	/**
	 * \brief setscheduler_event_class :
	 *
	 * \details
	 * Event class (arbitrary)
	 *
	 * @param scheduler_event_class :
	 */
	public void setscheduler_event_class(final SOSOptionString p_scheduler_event_class) {
		scheduler_event_class = p_scheduler_event_class;
	}

	public SOSOptionString	EventClass					= (SOSOptionString) scheduler_event_class.SetAlias(conClassName + ".EventClass");

	/**
	 * \var scheduler_event_exit_code :
	 * JobName for which the event is valid
	 *
	 */
	@JSOptionDefinition(name = "scheduler_event_exit_code", description = "", key = "scheduler_event_exit_code", type = "SOSOptionInteger", mandatory = false)
	public SOSOptionInteger	scheduler_event_exit_code	= new SOSOptionInteger(this, conClassName + ".scheduler_event_exit_code", // HashMap-Key
																"", // Titel
																"0", // InitValue
																"0", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief getscheduler_event_exit_code :
	 *
	 * \details
	 * JobName for which the event is valid
	 *
	 * \return
	 *
	 */
	public SOSOptionInteger getscheduler_event_exit_code() {
		return scheduler_event_exit_code;
	}

	/**
	 * \brief setscheduler_event_exit_code :
	 *
	 * \details
	 * JobName for which the event is valid
	 *
	 * @param scheduler_event_exit_code :
	 */
	public void setscheduler_event_exit_code(final SOSOptionInteger p_scheduler_event_exit_code) {
		scheduler_event_exit_code = p_scheduler_event_exit_code;
	}

	public SOSOptionInteger	ExitCode							= (SOSOptionInteger) scheduler_event_exit_code.SetAlias(conClassName + ".ExitCode");

	/**
	 * \var scheduler_event_expiration_cycle :
	 * Similar to scheduler_event_expiration_period this parameter specifies a time (e.g. 06:00) when an event will expire. scheduler_event_expiration_cycle takes precedence over scheduler_event_expiration_period .
	 *
	 */
	@JSOptionDefinition(name = "scheduler_event_expiration_cycle", description = "", key = "scheduler_event_expiration_cycle", type = "SOSOptionTime", mandatory = false)
	public SOSOptionTime	scheduler_event_expiration_cycle	= new SOSOptionTime(this, conClassName + ".scheduler_event_expiration_cycle", // HashMap-Key
																		"", // Titel
																		"", // InitValue
																		"", // DefaultValue
																		false // isMandatory
																);

	/**
	 * \brief getscheduler_event_expiration_cycle :
	 *
	 * \details
	 * Similar to scheduler_event_expiration_period this parameter specifies a time (e.g. 06:00) when an event will expire. scheduler_event_expiration_cycle takes precedence over scheduler_event_expiration_period .
	 *
	 * \return
	 *
	 */
	public SOSOptionTime getscheduler_event_expiration_cycle() {
		return scheduler_event_expiration_cycle;
	}

	/**
	 * \brief setscheduler_event_expiration_cycle :
	 *
	 * \details
	 * Similar to scheduler_event_expiration_period this parameter specifies a time (e.g. 06:00) when an event will expire. scheduler_event_expiration_cycle takes precedence over scheduler_event_expiration_period .
	 *
	 * @param scheduler_event_expiration_cycle :
	 */
	public void setscheduler_event_expiration_cycle(final SOSOptionTime p_scheduler_event_expiration_cycle) {
		scheduler_event_expiration_cycle = p_scheduler_event_expiration_cycle;
	}

	public SOSOptionTime		ExpiresAt							= (SOSOptionTime) scheduler_event_expiration_cycle.SetAlias(conClassName + ".ExpiresAt");

	/**
	 * \var scheduler_event_expiration_period :
	 * This parameter specifies an expiration period for events.
	 *
	 */
	@JSOptionDefinition(name = "scheduler_event_expiration_period", description = "", key = "scheduler_event_expiration_period", type = "SOSOptionTimeRange", mandatory = false)
	public SOSOptionTimeHorizon	scheduler_event_expiration_period	= new SOSOptionTimeHorizon(this, conClassName + ".scheduler_event_expiration_period", // HashMap-Key
																			"", // Titel
																			"", // InitValue
																			"", // DefaultValue
																			false // isMandatory
																	);

	/**
	 * \brief getscheduler_event_expiration_period :
	 *
	 * \details
	 * This parameter specifies an expiration period for events.
	 *
	 * \return
	 *
	 */
	public SOSOptionTimeHorizon getscheduler_event_expiration_period() {
		return scheduler_event_expiration_period;
	}

	/**
	 * \brief setscheduler_event_expiration_period :
	 *
	 * \details
	 * This parameter specifies an expiration period for events.
	 *
	 * @param scheduler_event_expiration_period :
	 */
	public void setscheduler_event_expiration_period(final SOSOptionTimeHorizon p_scheduler_event_expiration_period) {
		scheduler_event_expiration_period = p_scheduler_event_expiration_period;
	}

	public SOSOptionTimeHorizon	LifeTime				= (SOSOptionTimeHorizon) scheduler_event_expiration_period.SetAlias(conClassName + ".LifeTime");

	/**
	 * \var scheduler_event_expires :
	 * Expiration date of the event (ISO-format yyyy-mm-dd hh:mm:ss) or "never"
	 *
	 */
	@JSOptionDefinition(name = "scheduler_event_expires", description = "", key = "scheduler_event_expires", type = "SOSOptionTime", mandatory = false)
	public SOSOptionTime		scheduler_event_expires	= new SOSOptionTime(this, conClassName + ".scheduler_event_expires", // HashMap-Key
																"", // Titel
																"", // InitValue
																"", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief getscheduler_event_expires :
	 *
	 * \details
	 * Expiration date of the event (ISO-format yyyy-mm-dd hh:mm:ss) or "never"
	 *
	 * \return
	 *
	 */
	public SOSOptionTime getscheduler_event_expires() {
		return scheduler_event_expires;
	}

	/**
	 * \brief setscheduler_event_expires :
	 *
	 * \details
	 * Expiration date of the event (ISO-format yyyy-mm-dd hh:mm:ss) or "never"
	 *
	 * @param scheduler_event_expires :
	 */
	public void setscheduler_event_expires(final SOSOptionTime p_scheduler_event_expires) {
		scheduler_event_expires = p_scheduler_event_expires;
	}

	public SOSOptionTime		ExpiryDate						= (SOSOptionTime) scheduler_event_expires.SetAlias(conClassName + ".ExpiryDate");

	/**
	 * \var scheduler_event_handler_host :
	 * Uses a JobScheduler (other than the supervisor) as event handler
	 *
	 */
	@JSOptionDefinition(name = "scheduler_event_handler_host", description = "", key = "scheduler_event_handler_host", type = "SOSOptionHostName", mandatory = false)
	public SOSOptionHostName	scheduler_event_handler_host	= new SOSOptionHostName(this, conClassName + ".scheduler_event_handler_host", // HashMap-Key
																		"", // Titel
																		"localhost", // InitValue
																		"localhost", // DefaultValue
																		false // isMandatory
																);

	/**
	 * \brief getscheduler_event_handler_host :
	 *
	 * \details
	 * Uses a JobScheduler (other than the supervisor) as event handler
	 *
	 * \return
	 *
	 */
	public SOSOptionHostName getscheduler_event_handler_host() {
		return scheduler_event_handler_host;
	}

	/**
	 * \brief setscheduler_event_handler_host :
	 *
	 * \details
	 * Uses a JobScheduler (other than the supervisor) as event handler
	 *
	 * @param scheduler_event_handler_host :
	 */
	public void setscheduler_event_handler_host(final SOSOptionHostName p_scheduler_event_handler_host) {
		scheduler_event_handler_host = p_scheduler_event_handler_host;
	}

	public SOSOptionHostName	EventService					= (SOSOptionHostName) scheduler_event_handler_host.SetAlias(conClassName + ".EventService");

	/**
	 * \option tcp_time_out
	 * \type SOSOptionInteger
	 * \brief tcp_time_out - The time out in seconds for a tcp connection
	 *
	 * \details
	 * The time out in seconds for a tcp connection
	 *
	 * \mandatory: false
	 *
	 * \created 27.08.2013 03:40:58 by KB
	 */
	@JSOptionDefinition(name = "tcp_time_out", description = "The time out in seconds for a tcp connection", key = "tcp_time_out", type = "SOSOptionInteger", mandatory = false)
	public SOSOptionInteger		TCPTimeoutValue		= new SOSOptionInteger( // ...
															this, // ....
															conClassName + ".tcp_time_out", // ...
															"The time out in seconds for a tcp connection", // ...
															"60", // ...
															"60", // ...
															false);

	public SOSOptionInteger TimeOut = (SOSOptionInteger) TCPTimeoutValue.SetAlias("time_out");

	@Override
	public int getTCPTimeoutValue() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::gettcp_time_out";

		return TCPTimeoutValue.value();
	} // public String gettcp_time_out

	@Override
	public ISOSSchedulerSocket setTCPTimeoutValue(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::settcp_time_out";
		TCPTimeoutValue.Value(pstrValue);
		return this;
	} // public SchedulerObjectFactoryOptions settcp_time_out

	/**
	 * \option TransferMethod
	 * \type SOSOptionJSTransferMethod
	 * \brief TransferMethod - How to communicate with the JobScheduler
	 *
	 * \details
	 * The technical method of how to communicate with the JobScheduler
	 *
	 * \mandatory: true
	 *
	 * \created 26.04.2011 12:22:06 by KB
	 */
	@JSOptionDefinition(name = "TransferMethod", description = "The technical method of how to communicate with the JobScheduler", key = "TransferMethod", type = "SOSOptionJSTransferMethod", mandatory = true)
	public SOSOptionJSTransferMethod	TransferMethod	= new SOSOptionJSTransferMethod( // ...
																this, // ....
																conClassName + ".TransferMethod", // ...
																"The technical method of how to communicate with the JobScheduler", // ...
																"tcp", // ...
																"tcp", // ...
																true);

	@Override
	public String getTransferMethod() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getTransferMethod";
		return TransferMethod.Value();
	} // public String getTransferMethod

	@Override
	public ISOSSchedulerSocket setTransferMethod(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setTransferMethod";
		TransferMethod.Value(pstrValue);
		return this;
	} // public SchedulerObjectFactoryOptions setTransferMethod


	/**
	 * \var scheduler_event_handler_port :
	 * Defines a JobScheduler (other than the supervisor) as event service.
	 *
	 */
	@JSOptionDefinition(name = "scheduler_event_handler_port", description = "", key = "scheduler_event_handler_port", type = "SOSOptionPortNumber", mandatory = false)
	public SOSOptionPortNumber	scheduler_event_handler_port	= new SOSOptionPortNumber(this, conClassName + ".scheduler_event_handler_port", // HashMap-Key
																		"", // Titel
																		"4444", // InitValue
																		"4444", // DefaultValue
																		false // isMandatory
																);

	/**
	 * \brief getscheduler_event_handler_port :
	 *
	 * \details
	 * Defines a JobScheduler (other than the supervisor) as event service.
	 *
	 * \return
	 *
	 */
	public SOSOptionPortNumber getscheduler_event_handler_port() {
		return scheduler_event_handler_port;
	}

	/**
	 * \brief setscheduler_event_handler_port :
	 *
	 * \details
	 * Defines a JobScheduler (other than the supervisor) as event service.
	 *
	 * @param scheduler_event_handler_port :
	 */
	public void setscheduler_event_handler_port(final SOSOptionPortNumber p_scheduler_event_handler_port) {
		scheduler_event_handler_port = p_scheduler_event_handler_port;
	}

	public SOSOptionPortNumber	EventServicePort	= (SOSOptionPortNumber) scheduler_event_handler_port.SetAlias(conClassName + ".EventServicePort");

	/**
	 * \var scheduler_event_id :
	 * Event ID (arbitrary)
	 *
	 */
	@JSOptionDefinition(name = "scheduler_event_id", description = "", key = "scheduler_event_id", type = "SOSOptionString", mandatory = false)
	public SOSOptionString		scheduler_event_id	= new SOSOptionString(this, conClassName + ".scheduler_event_id", // HashMap-Key
															"", // Titel
															"", // InitValue
															"", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief getscheduler_event_id :
	 *
	 * \details
	 * Event ID (arbitrary)
	 *
	 * \return
	 *
	 */
	public SOSOptionString getscheduler_event_id() {
		return scheduler_event_id;
	}

	/**
	 * \brief setscheduler_event_id :
	 *
	 * \details
	 * Event ID (arbitrary)
	 *
	 * @param scheduler_event_id :
	 */
	public void setscheduler_event_id(final SOSOptionString p_scheduler_event_id) {
		scheduler_event_id = p_scheduler_event_id;
	}

	public SOSOptionString	id					= (SOSOptionString) scheduler_event_id.SetAlias(conClassName + ".id");
	public SOSOptionString	EventID					= (SOSOptionString) scheduler_event_id.SetAlias(conClassName + ".event_id");

	/**
	 * \var scheduler_event_job :
	 * JobName for which the event is valid
	 *
	 */
	@JSOptionDefinition(name = "scheduler_event_job", description = "", key = "scheduler_event_job", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	scheduler_event_job	= new SOSOptionString(this, conClassName + ".scheduler_event_job", // HashMap-Key
														"", // Titel
														"", // InitValue
														"", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getscheduler_event_job :
	 *
	 * \details
	 * JobName for which the event is valid
	 *
	 * \return
	 *
	 */
	public SOSOptionString getscheduler_event_job() {
		return scheduler_event_job;
	}

	/**
	 * \brief setscheduler_event_job :
	 *
	 * \details
	 * JobName for which the event is valid
	 *
	 * @param scheduler_event_job :
	 */
	public void setscheduler_event_job(final SOSOptionString p_scheduler_event_job) {
		scheduler_event_job = p_scheduler_event_job;
	}

	public SOSOptionString			JobName					= (SOSOptionString) scheduler_event_job.SetAlias(conClassName + ".JobName");

	/**
	 * \var supervisor_job_chain :
	 * Jobchain for processing events in the supervisor
	 *
	 */
	@JSOptionDefinition(name = "supervisor_job_chain", description = "", key = "supervisor_job_chain", type = "SOSOptionCommandString", mandatory = false)
	public SOSOptionCommandString	supervisor_job_chain	= new SOSOptionCommandString(this, conClassName + ".supervisor_job_chain", // HashMap-Key
																	"", // Titel
																	"/sos/events/scheduler_event_service", // InitValue
																	"/sos/events/scheduler_event_service", // DefaultValue
																	false // isMandatory
															);

	/**
	 * \brief getsupervisor_job_chain :
	 *
	 * \details
	 * Jobchain for processing events in the supervisor
	 *
	 * \return
	 *
	 */
	public SOSOptionCommandString getsupervisor_job_chain() {
		return supervisor_job_chain;
	}

	/**
	 * \brief setsupervisor_job_chain :
	 *
	 * \details
	 * Jobchain for processing events in the supervisor
	 *
	 * @param supervisor_job_chain :
	 */
	public void setsupervisor_job_chain(final SOSOptionCommandString p_supervisor_job_chain) {
		supervisor_job_chain = p_supervisor_job_chain;
	}

	public JSEventsClientOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public JSEventsClientOptionsSuperClass

	public JSEventsClientOptionsSuperClass(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JSEventsClientOptionsSuperClass

	//

	public JSEventsClientOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public JSEventsClientOptionsSuperClass (HashMap JSSettings)

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


	@Override
	public int getPortNumber() {
		return scheduler_event_handler_port.value();
	}

	@Override
	public ISOSSchedulerSocket setPortNumber(final String pstrValue) {
		scheduler_event_handler_port.Value(pstrValue);
		return null;
	}

	@Override
	public String getServerName() {
		return scheduler_event_handler_host.Value();
	}

	@Override
	public ISOSSchedulerSocket setServerName(final String pstrValue) {
		scheduler_event_handler_host.Value(pstrValue);
		return null;
	}
} // public class JSEventsClientOptionsSuperClass