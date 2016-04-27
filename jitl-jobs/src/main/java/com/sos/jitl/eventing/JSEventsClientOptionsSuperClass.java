package com.sos.jitl.eventing;

import java.util.HashMap;

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

@JSOptionClass(name = "JSEventsClientOptionsSuperClass", description = "JSEventsClientOptionsSuperClass")
public class JSEventsClientOptionsSuperClass extends JSOptionsClass implements ISOSSchedulerSocket {

    private static final long serialVersionUID = -6733730581916617748L;
    private final String conClassName = this.getClass().getSimpleName();

    @JSOptionDefinition(name = "UDPPortNumber", description = "The scheduler communication port for UDP", key = "UDPPortNumber",
            type = "SOSOptionPortNumber", mandatory = true)
    public SOSOptionPortNumber UDPPortNumber = new SOSOptionPortNumber(this, conClassName + ".UDPPortNumber",
            "The scheduler communication port for UDP", "4444", "4444", true);

    @Override
    public int getUDPPortNumber() {
        return UDPPortNumber.value();
    }

    @Override
    public ISOSSchedulerSocket setUDPPortNumber(final String pstrValue) {
        UDPPortNumber.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Event_Parameter", description = "The Name of the Params which will passed to the events.", key = "Event_Parameter",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString EventParameter = new SOSOptionString(this, conClassName + ".Event_Parameter",
            "The Name of the Params which will passed to the events.", "", "", false);

    public String getEvent_Parameter() {
        return EventParameter.Value();
    }

    public JSEventsClientOptionsSuperClass setEvent_Parameter(final String pstrValue) {
        EventParameter.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "del_events", description = "", key = "del_events", type = "SOSOptionString", mandatory = false)
    public SOSOptionString del_events = new SOSOptionString(this, conClassName + ".del_events", "", "", "", false);

    public SOSOptionString getdel_events() {
        return del_events;
    }

    public void setdel_events(final SOSOptionString p_del_events) {
        del_events = p_del_events;
    }

    @JSOptionDefinition(name = "scheduler_event_action", description = "", key = "scheduler_event_action", type = "SOSOptionString", mandatory = true)
    public SOSOptionString scheduler_event_action = new SOSOptionString(this, conClassName + ".scheduler_event_action", "", "add", "add", true);
    public SOSOptionString operation = (SOSOptionString) scheduler_event_action.SetAlias(conClassName + ".operation");

    public SOSOptionString getscheduler_event_action() {
        return scheduler_event_action;
    }

    public void setscheduler_event_action(final SOSOptionString p_scheduler_event_action) {
        scheduler_event_action = p_scheduler_event_action;
    }

    @JSOptionDefinition(name = "scheduler_event_class", description = "", key = "scheduler_event_class", type = "SOSOptionString", mandatory = true)
    public SOSOptionString scheduler_event_class = new SOSOptionString(this, conClassName + ".scheduler_event_class", "", "", "", true);
    public SOSOptionString EventClass = (SOSOptionString) scheduler_event_class.SetAlias(conClassName + ".EventClass");

    public SOSOptionString getscheduler_event_class() {
        return scheduler_event_class;
    }

    public void setscheduler_event_class(final SOSOptionString p_scheduler_event_class) {
        scheduler_event_class = p_scheduler_event_class;
    }

    @JSOptionDefinition(name = "scheduler_event_exit_code", description = "", key = "scheduler_event_exit_code", type = "SOSOptionInteger",
            mandatory = false)
    public SOSOptionInteger scheduler_event_exit_code = new SOSOptionInteger(this, conClassName + ".scheduler_event_exit_code", "", "0", "0", false);
    public SOSOptionInteger ExitCode = (SOSOptionInteger) scheduler_event_exit_code.SetAlias(conClassName + ".ExitCode");

    public SOSOptionInteger getscheduler_event_exit_code() {
        return scheduler_event_exit_code;
    }

    public void setscheduler_event_exit_code(final SOSOptionInteger p_scheduler_event_exit_code) {
        scheduler_event_exit_code = p_scheduler_event_exit_code;
    }

    @JSOptionDefinition(name = "scheduler_event_expiration_cycle", description = "", key = "scheduler_event_expiration_cycle",
            type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime scheduler_event_expiration_cycle = new SOSOptionTime(this, conClassName + ".scheduler_event_expiration_cycle", "", "",
            "", false);
    public SOSOptionTime ExpiresAt = (SOSOptionTime) scheduler_event_expiration_cycle.SetAlias(conClassName + ".ExpiresAt");

    public SOSOptionTime getscheduler_event_expiration_cycle() {
        return scheduler_event_expiration_cycle;
    }

    public void setscheduler_event_expiration_cycle(final SOSOptionTime p_scheduler_event_expiration_cycle) {
        scheduler_event_expiration_cycle = p_scheduler_event_expiration_cycle;
    }

    @JSOptionDefinition(name = "scheduler_event_expiration_period", description = "", key = "scheduler_event_expiration_period",
            type = "SOSOptionTimeRange", mandatory = false)
    public SOSOptionTimeHorizon scheduler_event_expiration_period = new SOSOptionTimeHorizon(this,
            conClassName + ".scheduler_event_expiration_period", "", "", "", false);
    public SOSOptionTimeHorizon LifeTime = (SOSOptionTimeHorizon) scheduler_event_expiration_period.SetAlias(conClassName + ".LifeTime");

    public SOSOptionTimeHorizon getscheduler_event_expiration_period() {
        return scheduler_event_expiration_period;
    }

    public void setscheduler_event_expiration_period(final SOSOptionTimeHorizon p_scheduler_event_expiration_period) {
        scheduler_event_expiration_period = p_scheduler_event_expiration_period;
    }

    @JSOptionDefinition(name = "scheduler_event_expires", description = "", key = "scheduler_event_expires", type = "SOSOptionTime",
            mandatory = false)
    public SOSOptionTime scheduler_event_expires = new SOSOptionTime(this, conClassName + ".scheduler_event_expires", "", "", "", false);
    public SOSOptionTime ExpiryDate = (SOSOptionTime) scheduler_event_expires.SetAlias(conClassName + ".ExpiryDate");

    public SOSOptionTime getscheduler_event_expires() {
        return scheduler_event_expires;
    }

    public void setscheduler_event_expires(final SOSOptionTime p_scheduler_event_expires) {
        scheduler_event_expires = p_scheduler_event_expires;
    }

    @JSOptionDefinition(name = "scheduler_event_handler_host", description = "", key = "scheduler_event_handler_host", type = "SOSOptionHostName",
            mandatory = false)
    public SOSOptionHostName scheduler_event_handler_host = new SOSOptionHostName(this, conClassName + ".scheduler_event_handler_host", "",
            "localhost", "localhost", false);
    public SOSOptionHostName EventService = (SOSOptionHostName) scheduler_event_handler_host.SetAlias(conClassName + ".EventService");

    public SOSOptionHostName getscheduler_event_handler_host() {
        return scheduler_event_handler_host;
    }

    public void setscheduler_event_handler_host(final SOSOptionHostName p_scheduler_event_handler_host) {
        scheduler_event_handler_host = p_scheduler_event_handler_host;
    }

    @JSOptionDefinition(name = "tcp_time_out", description = "The time out in seconds for a tcp connection", key = "tcp_time_out",
            type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger TCPTimeoutValue = new SOSOptionInteger(this, conClassName + ".tcp_time_out",
            "The time out in seconds for a tcp connection", "60", "60", false);
    public SOSOptionInteger TimeOut = (SOSOptionInteger) TCPTimeoutValue.SetAlias("time_out");

    @Override
    public int getTCPTimeoutValue() {
        return TCPTimeoutValue.value();
    }

    @Override
    public ISOSSchedulerSocket setTCPTimeoutValue(final String pstrValue) {
        TCPTimeoutValue.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "TransferMethod", description = "The technical method of how to communicate with the JobScheduler",
            key = "TransferMethod", type = "SOSOptionJSTransferMethod", mandatory = true)
    public SOSOptionJSTransferMethod TransferMethod = new SOSOptionJSTransferMethod(this, conClassName + ".TransferMethod",
            "The technical method of how to communicate with the JobScheduler", "tcp", "tcp", true);

    @Override
    public String getTransferMethod() {
        return TransferMethod.Value();
    }

    @Override
    public ISOSSchedulerSocket setTransferMethod(final String pstrValue) {
        TransferMethod.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "scheduler_event_handler_port", description = "", key = "scheduler_event_handler_port", type = "SOSOptionPortNumber",
            mandatory = false)
    public SOSOptionPortNumber scheduler_event_handler_port = new SOSOptionPortNumber(this, conClassName + ".scheduler_event_handler_port",
            "", "4444", "4444", false);
    public SOSOptionPortNumber EventServicePort = (SOSOptionPortNumber) scheduler_event_handler_port.SetAlias(conClassName + ".EventServicePort");

    public SOSOptionPortNumber getscheduler_event_handler_port() {
        return scheduler_event_handler_port;
    }

    public void setscheduler_event_handler_port(final SOSOptionPortNumber p_scheduler_event_handler_port) {
        scheduler_event_handler_port = p_scheduler_event_handler_port;
    }

    @JSOptionDefinition(name = "scheduler_event_id", description = "", key = "scheduler_event_id", type = "SOSOptionString", mandatory = false)
    public SOSOptionString scheduler_event_id = new SOSOptionString(this, conClassName + ".scheduler_event_id", "", "", "", false);
    public SOSOptionString id = (SOSOptionString) scheduler_event_id.SetAlias(conClassName + ".id");
    public SOSOptionString EventID = (SOSOptionString) scheduler_event_id.SetAlias(conClassName + ".event_id");

    public SOSOptionString getscheduler_event_id() {
        return scheduler_event_id;
    }

    public void setscheduler_event_id(final SOSOptionString p_scheduler_event_id) {
        scheduler_event_id = p_scheduler_event_id;
    }

    @JSOptionDefinition(name = "scheduler_event_job", description = "", key = "scheduler_event_job", type = "SOSOptionString", mandatory = false)
    public SOSOptionString scheduler_event_job = new SOSOptionString(this, conClassName + ".scheduler_event_job", "", "", "", false);
    public SOSOptionString JobName = (SOSOptionString) scheduler_event_job.SetAlias(conClassName + ".JobName");

    public SOSOptionString getscheduler_event_job() {
        return scheduler_event_job;
    }

    public void setscheduler_event_job(final SOSOptionString p_scheduler_event_job) {
        scheduler_event_job = p_scheduler_event_job;
    }

    @JSOptionDefinition(name = "supervisor_job_chain", description = "", key = "supervisor_job_chain", type = "SOSOptionCommandString",
            mandatory = false)
    public SOSOptionCommandString supervisor_job_chain = new SOSOptionCommandString(this, conClassName + ".supervisor_job_chain", "",
            "/sos/events/scheduler_event_service", "/sos/events/scheduler_event_service", false);

    public SOSOptionCommandString getsupervisor_job_chain() {
        return supervisor_job_chain;
    }

    public void setsupervisor_job_chain(final SOSOptionCommandString p_supervisor_job_chain) {
        supervisor_job_chain = p_supervisor_job_chain;
    }

    public JSEventsClientOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JSEventsClientOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JSEventsClientOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
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
    public void CommandLineArgs(final String[] pstrArgs) {
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

}