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

    @JSOptionDefinition(name = "hibernate_configuration_file_scheduler", description = "", key = "hibernate_configuration_file_scheduler", type = "SOSOptionString", mandatory = true)
    public SOSOptionString hibernate_configuration_file_scheduler = new SOSOptionString(this, conClassName
            + ".hibernate_configuration_file_scheduler", // HashMap-Key
    "", // Titel
    "config/hibernate.cfg.xml", // InitValue
    "config/hibernate.cfg.xml", // DefaultValue
    true // isMandatory
    );

    public SOSOptionString gethibernate_configuration_file_scheduler() {
        return hibernate_configuration_file_scheduler;
    }

    public void sethibernate_configuration_file_scheduler(SOSOptionString val) {
        this.hibernate_configuration_file_scheduler = val;
    }

    @JSOptionDefinition(name = "max_history_age", description = "", key = "max_history_age", type = "SOSOptionString", mandatory = false)
    public SOSOptionString max_history_age = new SOSOptionString(this, conClassName + ".max_history_age", // HashMap-Key
    "", // Titel
    "1w", // InitValue
    "1w", // DefaultValue 1 week (1w 1d 1h 1m)
    false // isMandatory
    );

    public SOSOptionString getmax_history_age() {
        return max_history_age;
    }

    public void setmax_history_age(SOSOptionString val) {
        this.max_history_age = val;
    }

    @JSOptionDefinition(name = "force_max_history_age", description = "", key = "force_max_history_age", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean force_max_history_age = new SOSOptionBoolean(this, conClassName + ".force_max_history_age", // HashMap-Key
    "", // Titel
    "false", // InitValue
    "false", // DefaultValue
    false // isMandatory
    );

    public SOSOptionBoolean getforce_max_history_age() {
        return force_max_history_age;
    }

    public void setforce_max_history_age(SOSOptionBoolean val) {
        this.force_max_history_age = val;
    }

    @JSOptionDefinition(name = "batch_size", description = "", key = "batch_size", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger batch_size = new SOSOptionInteger(this, conClassName + ".batch_size", // HashMap-Key
    "", // Titel
    "100", // InitValue
    "100", // DefaultValue
    false // isMandatory
    );

    public SOSOptionInteger getbatch_size() {
        return batch_size;
    }

    public void setbatch_size(SOSOptionInteger val) {
        this.batch_size = val;
    }

    @JSOptionDefinition(name = "log_info_step", description = "", key = "log_info_step", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger log_info_step = new SOSOptionInteger(this, conClassName + ".log_info_step", // HashMap-Key
    "", // Titel
    "10000", // InitValue
    "10000", // DefaultValue
    false // isMandatory
    );

    public SOSOptionInteger getlog_info_step() {
        return log_info_step;
    }

    public void setlog_info_step(SOSOptionInteger val) {
        this.log_info_step = val;
    }

    /** connection_transaction_isolation : Default 2 TRANSACTION_READ_COMMITTED
     * because of Oracle not have a 1 (TRANSACTION_READ_UNCOMMITTED) */
    @JSOptionDefinition(name = "connection_transaction_isolation_scheduler", description = "", key = "connection_transaction_isolation_scheduler", type = "SOSOptionInterval", mandatory = false)
    public SOSOptionInteger connection_transaction_isolation_scheduler = new SOSOptionInteger(this, conClassName
            + ".connection_transaction_isolation_scheduler", // HashMap-Key
    "", // Titel
    "2", // InitValue
    "2", //
    false // isMandatory
    );

    public SOSOptionInteger getconnection_transaction_isolation_scheduler() {
        return connection_transaction_isolation_scheduler;
    }

    public void setconnection_transaction_isolation_scheduler(SOSOptionInteger val) {
        this.connection_transaction_isolation_scheduler = val;
    }

    @JSOptionDefinition(name = "connection_autocommit_scheduler", description = "", key = "connection_autocommit_scheduler", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean connection_autocommit_scheduler = new SOSOptionBoolean(this, conClassName + ".connection_autocommit_scheduler", // HashMap-Key
    "", // Titel
    "true", // InitValue
    "true", //
    false // isMandatory
    );

    public SOSOptionBoolean getconnection_autocommit_scheduler() {
        return connection_autocommit_scheduler;
    }

    public void setconnection_autocommit_scheduler(SOSOptionBoolean val) {
        this.connection_autocommit_scheduler = val;
    }
    
    @JSOptionDefinition(name = "large_result_fetch_size_scheduler", description = "", key = "large_result_fetch_size_scheduler", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger large_result_fetch_size_scheduler = new SOSOptionInteger(this, conClassName + ".large_result_fetch_size_scheduler", // HashMap-Key
    "", // Titel
    "-1", // InitValue
    "-1", // DefaultValue
    false // isMandatory
    );

    public SOSOptionInteger getlarge_result_fetch_size_scheduler() {
        return large_result_fetch_size_scheduler;
    }

    public void setlarge_result_fetch_size_scheduler(SOSOptionInteger val) {
        this.large_result_fetch_size_scheduler = val;
    }


    /** orders with endTime null will be maked as uncompleted and will be
     * repeatedly synchronized. max Differenze between currentTime und startTime
     * in minutes to reduce the items with the "uncompleted" state. */
    @JSOptionDefinition(name = "max_uncompleted_age", description = "", key = "max_uncompleted_age", type = "SOSOptionString", mandatory = false)
    public SOSOptionString max_uncompleted_age = new SOSOptionString(this, conClassName + ".max_uncompleted_age", // HashMap-Key
    "", // Titel
    "1d", // InitValue
    "1d", // DefaultValue 1 day (1w 1d 1h 1m)
    false // isMandatory
    );

    public SOSOptionString getmax_uncompleted_age() {
        return max_uncompleted_age;
    }

    public void setmax_uncompleted_age(SOSOptionString val) {
        this.max_uncompleted_age = val;
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
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }
}
