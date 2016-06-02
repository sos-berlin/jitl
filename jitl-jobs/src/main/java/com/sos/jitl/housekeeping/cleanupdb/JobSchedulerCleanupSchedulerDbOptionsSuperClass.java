package com.sos.jitl.housekeeping.cleanupdb;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "JobSchedulerCleanupSchedulerDbOptionsSuperClass", description = "JobSchedulerCleanupSchedulerDbOptionsSuperClass")
public class JobSchedulerCleanupSchedulerDbOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = "JobSchedulerCleanupSchedulerDbOptionsSuperClass";

    @JSOptionDefinition(name = "cleanup_daily_plan_execute", description = "", key = "cleanup_daily_plan_execute", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionBoolean cleanup_daily_plan_execute = new SOSOptionBoolean(this, conClassName + ".cleanup_daily_plan_execute", "", "true", "true",
            false);

    public SOSOptionBoolean getcleanup_daily_plan_execute() {
        return cleanup_daily_plan_execute;
    }

    public void setcleanup_daily_plan_execute(SOSOptionBoolean p_cleanup_daily_plan_execute) {
        this.cleanup_daily_plan_execute = p_cleanup_daily_plan_execute;
    }

    @JSOptionDefinition(name = "cleanup_jade_History_execute", description = "", key = "cleanup_jade_History_execute", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionBoolean cleanup_jade_history_execute = new SOSOptionBoolean(this, conClassName + ".cleanup_jade_History_execute", "", "true",
            "true", false);

    public SOSOptionBoolean getcleanup_jade_History_execute() {
        return cleanup_jade_history_execute;
    }

    public void setcleanup_jade_History_execute(SOSOptionBoolean p_cleanup_jade_History_execute) {
        this.cleanup_jade_history_execute = p_cleanup_jade_History_execute;
    }

    @JSOptionDefinition(name = "cleanup_JobScheduler_History_execute", description = "", key = "cleanup_JobScheduler_History_execute",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean cleanup_job_scheduler_history_execute = new SOSOptionBoolean(this,
            conClassName + ".cleanup_JobScheduler_History_execute", "", "true", "true", false);

    public SOSOptionBoolean getcleanup_JobScheduler_History_execute() {
        return cleanup_job_scheduler_history_execute;
    }

    public void setcleanup_JobScheduler_History_execute(SOSOptionBoolean p_cleanup_JobScheduler_History_execute) {
        this.cleanup_job_scheduler_history_execute = p_cleanup_JobScheduler_History_execute;
    }

    @JSOptionDefinition(name = "delete_daily_plan_interval", description = "", key = "delete_daily_plan_interval", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionInteger delete_daily_plan_interval =
            new SOSOptionInteger(this, conClassName + ".delete_daily_plan_interval", "", "0", "0", false);

    @JSOptionDefinition(name = "cleanup_daily_plan_limit", description = "", key = "cleanup_daily_plan_limit", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionInteger cleanup_daily_plan_limit = new SOSOptionInteger(this, conClassName + ".cleanup_daily_plan_limit", "", "0", "0", false);

    public SOSOptionInteger getcleanup_daily_plan_limit() {
        return cleanup_daily_plan_limit;
    }

    public void setcleanup_daily_plan_limit(SOSOptionInteger p_cleanup_daily_plan_limit) {
        this.cleanup_daily_plan_limit = p_cleanup_daily_plan_limit;
    }

    @JSOptionDefinition(name = "cleanup_jade_History_limit", description = "", key = "cleanup_jade_History_limit", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionInteger cleanup_jade_history_limit =
            new SOSOptionInteger(this, conClassName + ".cleanup_jade_History_limit", "", "0", "0", false);

    public SOSOptionInteger getcleanup_jade_history_limit() {
        return cleanup_jade_history_limit;
    }

    public void setcleanup_jade_history_limit(SOSOptionInteger p_cleanup_jade_History_limit) {
        this.cleanup_jade_history_limit = p_cleanup_jade_History_limit;
    }

    @JSOptionDefinition(name = "cleanup_JobScheduler_History_limit", description = "", key = "cleanup_JobScheduler_History_limit",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger cleanup_jobscheduler_history_limit = new SOSOptionInteger(this, conClassName + ".cleanup_JobScheduler_History_limit", "",
            "0", "0", false);

    public SOSOptionInteger getcleanup_jobscheduler_history_limit() {
        return cleanup_jobscheduler_history_limit;
    }

    public void setcleanup_jobscheduler_history_limit(SOSOptionInteger p_cleanup_jobscheduler_history_limit) {
        this.cleanup_jobscheduler_history_limit = p_cleanup_jobscheduler_history_limit;
    }

    public SOSOptionInteger getdelete_daily_plan_interval() {
        return delete_daily_plan_interval;
    }

    public void setdelete_daily_plan_interval(SOSOptionInteger p_delete_daily_plan_interval) {
        this.delete_daily_plan_interval = p_delete_daily_plan_interval;
    }

    @JSOptionDefinition(name = "delete_jade_history_interval", description = "", key = "delete_jade_history_interval", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionInteger delete_jade_history_interval = new SOSOptionInteger(this, conClassName + ".delete_jade_history_interval", "", "0", "0",
            false);

    public SOSOptionInteger getdelete_jade_history_interval() {
        return delete_jade_history_interval;
    }

    public void setdelete_jade_history_interval(SOSOptionInteger p_delete_jade_history_interval) {
        this.delete_jade_history_interval = p_delete_jade_history_interval;
    }

    @JSOptionDefinition(name = "delete_history_interval", description = "", key = "delete_history_interval", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionInteger delete_history_interval = new SOSOptionInteger(this, conClassName + ".delete_history_interval", "", "0", "0", false);

    public SOSOptionInteger getdelete_history_interval() {
        return delete_history_interval;
    }

    public void setdelete_history_interval(SOSOptionInteger p_delete_history_interval) {
        this.delete_history_interval = p_delete_history_interval;
    }

    @JSOptionDefinition(name = "delete_interval", description = "", key = "delete_interval", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger delete_interval = new SOSOptionInteger(this, conClassName + ".delete_interval", "", "0", "0", false);

    public SOSOptionInteger getdelete_interval() {
        return delete_interval;
    }

    public void setdelete_interval(SOSOptionInteger p_delete_interval) {
        this.delete_interval = p_delete_interval;
    }

    @JSOptionDefinition(name = "hibernate_configuration_file", description = "", key = "hibernate_configuration_file", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString hibernate_configuration_file = new SOSOptionString(this, conClassName + ".hibernate_configuration_file", "", " ", " ",
            false);

    public SOSOptionString gethibernate_configuration_file() {
        return hibernate_configuration_file;
    }

    public void sethibernate_configuration_file(SOSOptionString p_hibernate_configuration_file) {
        this.hibernate_configuration_file = p_hibernate_configuration_file;
    }

    @JSOptionDefinition(name = "scheduler_id", description = "", key = "scheduler_id", type = "SOSOptionString", mandatory = false)
    public SOSOptionString scheduler_id = new SOSOptionString(this, conClassName + ".scheduler_id", "", " ", " ", false);

    public SOSOptionString getscheduler_id() {
        return scheduler_id;
    }

    public void setscheduler_id(SOSOptionString p_scheduler_id) {
        this.scheduler_id = p_scheduler_id;
    }

    public JobSchedulerCleanupSchedulerDbOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerCleanupSchedulerDbOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerCleanupSchedulerDbOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        objSettings = pobjJSSettings;
        super.setAllOptions(pobjJSSettings);
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
        this.setAllOptions(super.objSettings);
    }

}