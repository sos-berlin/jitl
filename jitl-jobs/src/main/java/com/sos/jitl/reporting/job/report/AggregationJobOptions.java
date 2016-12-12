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

@JSOptionClass(name = "AggregationJobOptions", description = "AggregationJobOptions")
public class AggregationJobOptions extends ReportingJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = AggregationJobOptions.class.getSimpleName();

    public final static String VARIABLE_EXECUTE_AGGREGATION = "execute_aggregation";
    public final static String VARIABLE_FACT_DATE_FROM = "fact_date_from";
    
    @JSOptionDefinition(name = "current_scheduler_id", description = "", key = "current_scheduler_id", type = "SOSOptionString", mandatory = true)
    public SOSOptionString current_scheduler_id = new SOSOptionString(this, conClassName + ".current_scheduler_id", "", "", "", true);

    public SOSOptionString getcurrent_scheduler_id() {
        return current_scheduler_id;
    }

    public void setcurrent_scheduler_id(SOSOptionString val) {
        this.current_scheduler_id = val;
    }

    @JSOptionDefinition(name = "fact_date_from", description = "", key = "fact_date_from", type = "SOSOptionString", mandatory = true)
    public SOSOptionString fact_date_from = new SOSOptionString(this, conClassName + ".fact_date_from", "", "", "", true);

    public SOSOptionString getfact_date_from() {
        return fact_date_from;
    }

    public void setfact_date_from(SOSOptionString val) {
        this.fact_date_from = val;
    }

    @JSOptionDefinition(name = "current_scheduler_http_port", description = "", key = "current_scheduler_http_port", type = "SOSOptionInteger", mandatory = true)
    public SOSOptionInteger current_scheduler_http_port = new SOSOptionInteger(this, conClassName + ".current_scheduler_http_port", "", "", "", true);

    public SOSOptionInteger getcurrent_scheduler_http_port() {
        return current_scheduler_http_port;
    }

    public void setcurrent_scheduler_http_port(SOSOptionInteger val) {
        this.current_scheduler_http_port = val;
    }
    
    @JSOptionDefinition(name = VARIABLE_EXECUTE_AGGREGATION, description = "", key = VARIABLE_EXECUTE_AGGREGATION, type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean execute_aggregation = new SOSOptionBoolean(this, conClassName + "." + VARIABLE_EXECUTE_AGGREGATION, "", "true", "true",
            false);

    public SOSOptionBoolean getexecute_aggregation() {
        return execute_aggregation;
    }

    public void setexecute_aggregation(SOSOptionBoolean val) {
        this.execute_aggregation = val;
    }

    @JSOptionDefinition(name = "batch_size", description = "", key = "batch_size", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger batch_size = new SOSOptionInteger(this, conClassName + ".batch_size", "", "100", "100", false);

    public SOSOptionInteger getbatch_size() {
        return batch_size;
    }

    public void setbatch_size(SOSOptionInteger val) {
        this.batch_size = val;
    }

    @JSOptionDefinition(name = "log_info_step", description = "", key = "log_info_step", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger log_info_step = new SOSOptionInteger(this, conClassName + ".log_info_step", "", "10000", "10000", false);

    public SOSOptionInteger getlog_info_step() {
        return log_info_step;
    }

    public void setlog_info_step(SOSOptionInteger val) {
        this.log_info_step = val;
    }

    @JSOptionDefinition(name = "force_update_from_inventory", description = "", key = "force_update_from_inventory", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean force_update_from_inventory = new SOSOptionBoolean(this, conClassName + ".force_update_from_inventory", "", "false",
            "false", false);

    public SOSOptionBoolean getforce_update_from_inventory() {
        return force_update_from_inventory;
    }

    public void setforce_update_from_inventory(SOSOptionBoolean val) {
        this.force_update_from_inventory = val;
    }

    public AggregationJobOptions() {
    }

    public AggregationJobOptions(JSListener listener) {
        super(listener);
    }

    public AggregationJobOptions(HashMap<String, String> settings) throws Exception {
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