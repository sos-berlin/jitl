package com.sos.jitl.eventing.checkevents;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "JobSchedulerCheckEventsOptionsSuperClass", description = "JobSchedulerCheckEventsOptionsSuperClass")
public class JobSchedulerCheckEventsOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = "JobSchedulerCheckEventsOptionsSuperClass";

    @JSOptionDefinition(name = "configuration_file", description = "", key = "configuration_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString configuration_file = new SOSOptionString(this, conClassName + ".configuration_file", "", "", "", false);

    public SOSOptionString getconfiguration_file() {
        return configuration_file;
    }

    public void setconfiguration_file(SOSOptionString p_configuration_file) {
        this.configuration_file = p_configuration_file;
    }

    @JSOptionDefinition(name = "event_class", description = "", key = "event_class", type = "SOSOptionString", mandatory = false)
    public SOSOptionString event_class = new SOSOptionString(this, conClassName + ".event_class", "", "", "", false);

    public SOSOptionString getevent_class() {
        return event_class;
    }

    public void setevent_class(SOSOptionString p_event_class) {
        this.event_class = p_event_class;
    }

    @JSOptionDefinition(name = "event_condition", description = "", key = "event_condition", type = "SOSOptionString", mandatory = false)
    public SOSOptionString event_condition = new SOSOptionString(this, conClassName + ".event_condition", "", "", "", false);

    public SOSOptionString getevent_condition() {
        return event_condition;
    }

    public void setevent_condition(SOSOptionString p_event_condition) {
        this.event_condition = p_event_condition;
    }

    @JSOptionDefinition(name = "event_exit_code", description = "", key = "event_exit_code", type = "SOSOptionString", mandatory = false)
    public SOSOptionString event_exit_code = new SOSOptionString(this, conClassName + ".event_exit_code", "", "", "", false);

    public SOSOptionString getevent_exit_code() {
        return event_exit_code;
    }

    public void setevent_exit_code(SOSOptionString p_event_exit_code) {
        this.event_exit_code = p_event_exit_code;
    }

    @JSOptionDefinition(name = "event_id", description = "", key = "event_id", type = "SOSOptionString", mandatory = false)
    public SOSOptionString event_id = new SOSOptionString(this, conClassName + ".event_id", "", "", "", false);

    public SOSOptionString getevent_id() {
        return event_id;
    }

    public void setevent_id(SOSOptionString p_event_id) {
        this.event_id = p_event_id;
    }

    @JSOptionDefinition(name = "event_job", description = "", key = "event_job", type = "SOSOptionString", mandatory = false)
    public SOSOptionString event_job = new SOSOptionString(this, conClassName + ".event_job", "", "", "", false);

    public SOSOptionString getevent_job() {
        return event_job;
    }

    public void setevent_job(SOSOptionString p_event_job) {
        this.event_job = p_event_job;
    }

    @JSOptionDefinition(name = "event_job_chain", description = "", key = "event_job_chain", type = "SOSOptionString", mandatory = false)
    public SOSOptionString event_job_chain = new SOSOptionString(this, conClassName + ".event_job_chain", "", "", "", false);

    public SOSOptionString getevent_job_chain() {
        return event_job_chain;
    }

    public void setevent_job_chain(SOSOptionString p_event_job_chain) {
        this.event_job_chain = p_event_job_chain;
    }

    @JSOptionDefinition(name = "event_order_id", description = "", key = "event_order_id", type = "SOSOptionString", mandatory = false)
    public SOSOptionString event_order_id = new SOSOptionString(this, conClassName + ".event_order_id", "", "", "", false);

    public SOSOptionString getevent_order_id() {
        return event_order_id;
    }

    public void setevent_order_id(SOSOptionString p_event_order_id) {
        this.event_order_id = p_event_order_id;
    }

    @JSOptionDefinition(name = "event_scheduler_id", description = "", key = "event_scheduler_id", type = "SOSOptionString", mandatory = false)
    public SOSOptionString event_scheduler_id = new SOSOptionString(this, conClassName + ".event_scheduler_id", "", "", "", false);

    public SOSOptionString getevent_scheduler_id() {
        return event_scheduler_id;
    }

    public void setevent_scheduler_id(SOSOptionString p_event_scheduler_id) {
        this.event_scheduler_id = p_event_scheduler_id;
    }

    @JSOptionDefinition(name = "handle_existing_as", description = "", key = "handle_existing_as", type = "SOSOptionString", mandatory = false)
    public SOSOptionString handle_existing_as = new SOSOptionString(this, conClassName + ".handle_existing_as", "", "", "", false);

    public SOSOptionString gethandle_existing_as() {
        return handle_existing_as;
    }

    public void sethandle_existing_as(SOSOptionString p_handle_existing_as) {
        this.handle_existing_as = p_handle_existing_as;
    }

    @JSOptionDefinition(name = "handle_not_existing_as", description = "", key = "handle_not_existing_as", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString handle_not_existing_as = new SOSOptionString(this, conClassName + ".handle_not_existing_as", "", "", "", false);

    public SOSOptionString gethandle_not_existing_as() {
        return handle_not_existing_as;
    }

    public void sethandle_not_existing_as(SOSOptionString p_handle_not_existing_as) {
        this.handle_not_existing_as = p_handle_not_existing_as;
    }

    @JSOptionDefinition(name = "remote_scheduler_host", description = "", key = "remote_scheduler_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionString remote_scheduler_host = new SOSOptionString(this, conClassName + ".remote_scheduler_host", "", "", "", false);

    public SOSOptionString getremote_scheduler_host() {
        return remote_scheduler_host;
    }

    public void setremote_scheduler_host(SOSOptionString p_remote_scheduler_host) {
        this.remote_scheduler_host = p_remote_scheduler_host;
    }

    @JSOptionDefinition(name = "remote_scheduler_port", description = "", key = "remote_scheduler_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionString remote_scheduler_port = new SOSOptionString(this, conClassName + ".remote_scheduler_port", "", "", "", false);

    public SOSOptionString getremote_scheduler_port() {
        return remote_scheduler_port;
    }

    public void setremote_scheduler_port(SOSOptionString p_remote_scheduler_port) {
        this.remote_scheduler_port = p_remote_scheduler_port;
    }

    public JobSchedulerCheckEventsOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerCheckEventsOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerCheckEventsOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> settings) {
        super.setAllOptions(settings);
    }

    @Override
    public void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void commandLineArgs(String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.getSettings());
    }

}