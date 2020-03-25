package com.sos.jitl.sync;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "JobSchedulerSynchronizeJobChainsOptionsSuperClass", description = "JobSchedulerSynchronizeJobChainsOptionsSuperClass")
public class JobSchedulerSynchronizeJobChainsOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 2529720765502955118L;
    private final String conClassName = "JobSchedulerSynchronizeJobChainsOptionsSuperClass";

    @JSOptionDefinition(name = "job_chain_required_orders", description = "", key = "job_chain_required_orders", type = "SOSOptionInteger",
            mandatory = false)
    public SOSOptionInteger job_chain_required_orders = new SOSOptionInteger(this, conClassName + ".job_chain_required_orders", "", "1", "1", false);

    public SOSOptionInteger getjob_chain_required_orders() {
        return job_chain_required_orders;
    }

    public void setjob_chain_required_orders(final SOSOptionInteger p_job_chain_required_orders) {
        job_chain_required_orders = p_job_chain_required_orders;
    }

    @JSOptionDefinition(name = "job_chain_state_required_orders", description = "", key = "job_chain_state_required_orders",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString job_chain_state_required_orders = new SOSOptionString(this, conClassName + ".job_chain_state_required_orders", "", "1",
            "1", false);

    public SOSOptionString getjob_chain_state_required_orders() {
        return job_chain_state_required_orders;
    }

    public void setjob_chain_state_required_orders(final SOSOptionString p_job_chain_state_required_orders) {
        job_chain_state_required_orders = p_job_chain_state_required_orders;
    }

    @JSOptionDefinition(name = "scheduler.ignore_sync_jobs_in_stopped_jobchains", description = "",
            key = "scheduler.ignore_sync_jobs_in_stopped_jobchains", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean ignore_stopped_jobchains = new SOSOptionBoolean(this, conClassName + ".scheduler.ignore_sync_jobs_in_stopped_jobchains",
            "", "", "false", false);

    public SOSOptionBoolean getignore_stopped_jobchains() {
        return ignore_stopped_jobchains;
    }

    public void setignore_stopped_jobchains(final SOSOptionBoolean p_ignore_stopped_jobchains) {
        ignore_stopped_jobchains = p_ignore_stopped_jobchains;
    }

    @JSOptionDefinition(name = "jobchains_answer", description = "", key = "jobchains_answer", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jobchains_answer = new SOSOptionString(this, conClassName + ".jobchains_answer", "", "", "", false);

    public SOSOptionString getjobchains_answer() {
        return jobchains_answer;
    }

    public void setjobchains_answer(final SOSOptionString p_jobchains_answer) {
        jobchains_answer = p_jobchains_answer;
    }

    @JSOptionDefinition(name = "jobpath", description = "", key = "jobpath", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jobpath = new SOSOptionString(this, conClassName + ".jobpath", "", "", "", false);

    public SOSOptionString getjobpath() {
        return jobpath;
    }

    public void setjobpath(final SOSOptionString p_jobpath) {
        jobpath = p_jobpath;
    }

    @JSOptionDefinition(name = "job_chain_name2synchronize", description = "", key = "job_chain_name2synchronize", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString job_chain_name2synchronize = new SOSOptionString(this, conClassName + ".job_chain_name2synchronize", "", "", "", false);

    public SOSOptionString getjob_chain_name2synchronize() {
        return job_chain_name2synchronize;
    }

    public void setjob_chain_name2synchronize(final SOSOptionString p_job_chain_name2synchronize) {
        job_chain_name2synchronize = p_job_chain_name2synchronize;
    }

    @JSOptionDefinition(name = "job_chain_state2synchronize", description = "", key = "job_chain_state2synchronize", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString job_chain_state2synchronize = new SOSOptionString(this, conClassName + ".job_chain_state2synchronize", "", "", "", false);

    public SOSOptionString getjob_chain_state2synchronize() {
        return job_chain_state2synchronize;
    }

    public void setjob_chain_state2synchronize(final SOSOptionString p_job_chain_state2synchronize) {
        job_chain_state2synchronize = p_job_chain_state2synchronize;
    }

    @JSOptionDefinition(name = "disable_sync_context", description = "", key = "disable_sync_context", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean disable_sync_context = new SOSOptionBoolean(this, conClassName + ".disable_sync_context", "", "", "false", false);

    public SOSOptionBoolean getdisable_sync_context() {
        return disable_sync_context;
    }

    public void setdisable_sync_context(final SOSOptionBoolean p_disable_sync_context) {
        disable_sync_context = p_disable_sync_context;
    }

    @JSOptionDefinition(name = "orders_answer", description = "", key = "orders_answer", type = "SOSOptionString", mandatory = false)
    public SOSOptionString orders_answer = new SOSOptionString(this, conClassName + ".orders_answer", "", "", "", false);

    public SOSOptionString getorders_answer() {
        return orders_answer;
    }

    public void setorders_answer(final SOSOptionString p_orders_answer) {
        orders_answer = p_orders_answer;
    }

    @JSOptionDefinition(name = "required_orders", description = "", key = "required_orders", type = "SOSOptionString", mandatory = false)
    public SOSOptionString required_orders = new SOSOptionString(this, conClassName + ".required_orders", "", "1", "1", false);

    public SOSOptionString getrequired_orders() {
        return required_orders;
    }

    public void setrequired_orders(final SOSOptionString p_required_orders) {
        required_orders = p_required_orders;
    }

    @JSOptionDefinition(name = "setback_count", description = "", key = "setback_count", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger setback_count = new SOSOptionInteger(this, conClassName + ".setback_count", "", "unbounded", "unbounded", false);

    public SOSOptionInteger getsetback_count() {
        return setback_count;
    }

    public void setsetback_count(final SOSOptionInteger p_setback_count) {
        setback_count = p_setback_count;
    }

    @JSOptionDefinition(name = "setback_interval", description = "", key = "setback_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger setback_interval = new SOSOptionInteger(this, conClassName + ".setback_interval", "", "600", "600", false);

    public SOSOptionInteger getsetback_interval() {
        return setback_interval;
    }

    public void setsetback_interval(final SOSOptionInteger p_setback_interval) {
        setback_interval = p_setback_interval;
    }

    @JSOptionDefinition(name = "setback_type", description = "", key = "setback_type", type = "SOSOptionSetBack", mandatory = false)
    public SOSOptionString setback_type = new SOSOptionString(this, conClassName + ".setback_type", "", "suspend", "suspend", false);

    public SOSOptionString getsetback_type() {
        return setback_type;
    }

    public void setsetback_type(final SOSOptionString p_setback_type) {
        setback_type = p_setback_type;
    }

    @JSOptionDefinition(name = "sync_session_id", description = "", key = "sync_session_id", type = "SOSOptionString", mandatory = false)
    public SOSOptionString sync_session_id = new SOSOptionString(this, conClassName + ".sync_session_id", "", "", "", false);

    public SOSOptionString getsync_session_id() {
        return sync_session_id;
    }

    public void setsync_session_id(final SOSOptionString p_sync_session_id) {
        sync_session_id = p_sync_session_id;
    }

    public JobSchedulerSynchronizeJobChainsOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerSynchronizeJobChainsOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerSynchronizeJobChainsOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    @Override
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
    public void commandLineArgs(final String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.getSettings());
    }

}