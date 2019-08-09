package com.sos.jitl.reporting.job.report;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.jitl.reporting.job.ReportingJobOptionsSuperClass;

@JSOptionClass(name = "FactJobOptions", description = "FactJobOptions")
public class FactJobOptions extends ReportingJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = FactJobOptions.class.getSimpleName();

    @JSOptionDefinition(name = "execute_notification_plugin", description = "", key = "execute_notification_plugin", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean execute_notification_plugin = new SOSOptionBoolean(this, conClassName + ".execute_notification_plugin", "", "", "",
            false);

    public SOSOptionBoolean getexecute_notification_plugin() {
        return execute_notification_plugin;
    }

    public void setexecute_notification_plugin(SOSOptionBoolean val) {
        this.execute_notification_plugin = val;
    }

    @JSOptionDefinition(name = "current_scheduler_id", description = "", key = "current_scheduler_id", type = "SOSOptionString", mandatory = true)
    public SOSOptionString current_scheduler_id = new SOSOptionString(this, conClassName + ".current_scheduler_id", "", "", "", true);

    public SOSOptionString getcurrent_scheduler_id() {
        return current_scheduler_id;
    }

    public void setcurrent_scheduler_id(SOSOptionString val) {
        this.current_scheduler_id = val;
    }

    @JSOptionDefinition(name = "current_scheduler_hostname", description = "", key = "current_scheduler_hostname", type = "SOSOptionString", mandatory = true)
    public SOSOptionString current_scheduler_hostname = new SOSOptionString(this, conClassName + ".current_scheduler_hostname", "", "", "", true);

    public SOSOptionString getcurrent_scheduler_hostname() {
        return current_scheduler_hostname;
    }

    public void setcurrent_scheduler_hostname(SOSOptionString val) {
        this.current_scheduler_hostname = val;
    }

    @JSOptionDefinition(name = "current_scheduler_http_port", description = "", key = "current_scheduler_http_port", type = "SOSOptionInteger", mandatory = true)
    public SOSOptionInteger current_scheduler_http_port = new SOSOptionInteger(this, conClassName + ".current_scheduler_http_port", "", "", "", true);

    public SOSOptionInteger getcurrent_scheduler_http_port() {
        return current_scheduler_http_port;
    }

    public void setcurrent_scheduler_http_port(SOSOptionInteger val) {
        this.current_scheduler_http_port = val;
    }

    @JSOptionDefinition(name = "hibernate_configuration_file_scheduler", description = "", key = "hibernate_configuration_file_scheduler", type = "SOSOptionString", mandatory = true)
    public SOSOptionString hibernate_configuration_file_scheduler = new SOSOptionString(this, conClassName
            + ".hibernate_configuration_file_scheduler", "", "config/hibernate.cfg.xml", "config/hibernate.cfg.xml", true);

    public SOSOptionString gethibernate_configuration_file_scheduler() {
        return hibernate_configuration_file_scheduler;
    }

    public void sethibernate_configuration_file_scheduler(SOSOptionString val) {
        this.hibernate_configuration_file_scheduler = val;
    }

    @JSOptionDefinition(name = "max_history_age", description = "", key = "max_history_age", type = "SOSOptionString", mandatory = false)
    public SOSOptionString max_history_age = new SOSOptionString(this, conClassName + ".max_history_age", "", "1w", "1w", false);

    public SOSOptionString getmax_history_age() {
        return max_history_age;
    }

    public void setmax_history_age(SOSOptionString val) {
        this.max_history_age = val;
    }

    @JSOptionDefinition(name = "max_history_tasks", description = "", key = "max_history_tasks", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger max_history_tasks = new SOSOptionInteger(this, conClassName + ".max_history_tasks", "", "100000", "100000", false);

    public SOSOptionInteger getmax_history_tasks() {
        return max_history_tasks;
    }

    public void setmax_history_tasks(SOSOptionInteger val) {
        this.max_history_tasks = val;
    }

    @JSOptionDefinition(name = "wait_interval", description = "", key = "wait_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger wait_interval = new SOSOptionInteger(this, conClassName + ".wait_interval", "", "2", "2", false);

    public SOSOptionInteger getwait_interval() {
        return wait_interval;
    }

    public void setwait_interval(SOSOptionInteger val) {
        this.wait_interval = val;
    }

    @JSOptionDefinition(name = "force_max_history_age", description = "", key = "force_max_history_age", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean force_max_history_age = new SOSOptionBoolean(this, conClassName + ".force_max_history_age", "", "false", "false", false);

    public SOSOptionBoolean getforce_max_history_age() {
        return force_max_history_age;
    }

    public void setforce_max_history_age(SOSOptionBoolean val) {
        this.force_max_history_age = val;
    }

    @JSOptionDefinition(name = "log_info_step", description = "", key = "log_info_step", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger log_info_step = new SOSOptionInteger(this, conClassName + ".log_info_step", "", "10000", "10000", false);

    public SOSOptionInteger getlog_info_step() {
        return log_info_step;
    }

    public void setlog_info_step(SOSOptionInteger val) {
        this.log_info_step = val;
    }

    @JSOptionDefinition(name = "connection_transaction_isolation_scheduler", description = "", key = "connection_transaction_isolation_scheduler", type = "SOSOptionInterval", mandatory = false)
    public SOSOptionInteger connection_transaction_isolation_scheduler = new SOSOptionInteger(this, conClassName
            + ".connection_transaction_isolation_scheduler", "", "2", "2", false);

    public SOSOptionInteger getconnection_transaction_isolation_scheduler() {
        return connection_transaction_isolation_scheduler;
    }

    public void setconnection_transaction_isolation_scheduler(SOSOptionInteger val) {
        this.connection_transaction_isolation_scheduler = val;
    }

    @JSOptionDefinition(name = "connection_autocommit_scheduler", description = "", key = "connection_autocommit_scheduler", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean connection_autocommit_scheduler = new SOSOptionBoolean(this, conClassName + ".connection_autocommit_scheduler", "",
            "true", "true", false);

    public SOSOptionBoolean getconnection_autocommit_scheduler() {
        return connection_autocommit_scheduler;
    }

    public void setconnection_autocommit_scheduler(SOSOptionBoolean val) {
        this.connection_autocommit_scheduler = val;
    }

    @JSOptionDefinition(name = "large_result_fetch_size_scheduler", description = "", key = "large_result_fetch_size_scheduler", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger large_result_fetch_size_scheduler = new SOSOptionInteger(this, conClassName + ".large_result_fetch_size_scheduler", "",
            "-1", "-1", false);

    public SOSOptionInteger getlarge_result_fetch_size_scheduler() {
        return large_result_fetch_size_scheduler;
    }

    public void setlarge_result_fetch_size_scheduler(SOSOptionInteger val) {
        this.large_result_fetch_size_scheduler = val;
    }

    @JSOptionDefinition(name = "task_history_max_uncompleted_age", description = "", key = "task_history_max_uncompleted_age", type = "SOSOptionString", mandatory = false)
    public SOSOptionString task_history_max_uncompleted_age = new SOSOptionString(this, conClassName + ".task_history_max_uncompleted_age", "", "1d",
            "2d", false);

    public SOSOptionString gettask_history_max_uncompleted_age() {
        return task_history_max_uncompleted_age;
    }

    public void settask_history_max_uncompleted_age(SOSOptionString val) {
        this.task_history_max_uncompleted_age = val;
    }

    @JSOptionDefinition(name = "order_history_max_uncompleted_age", description = "", key = "order_history_max_uncompleted_age", type = "SOSOptionString", mandatory = false)
    public SOSOptionString order_history_max_uncompleted_age = new SOSOptionString(this, conClassName + ".order_history_max_uncompleted_age", "",
            "180d", "180d", false);

    public SOSOptionString getorder_history_max_uncompleted_age() {
        return order_history_max_uncompleted_age;
    }

    public void setorder_history_max_uncompleted_age(SOSOptionString val) {
        this.order_history_max_uncompleted_age = val;
    }

    public FactJobOptions() {
    }

    public FactJobOptions(JSListener listener) {
        super(listener);
    }

    public FactJobOptions(HashMap<String, String> settings) throws Exception {
        super(settings);
    }

    @Override
    public void checkMandatory() {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

}