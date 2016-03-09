package com.sos.jitl.reporting.job.inventory;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.jitl.reporting.job.ReportingJobOptionsSuperClass;

@JSOptionClass(name = "InventoryJobOptions", description = "InventoryJobOptions")
public class InventoryJobOptions extends ReportingJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = InventoryJobOptions.class.getSimpleName();

    @JSOptionDefinition(name = "current_scheduler_configuration_directory", description = "", key = "current_scheduler_configuration_directory", type = "SOSOptionString", mandatory = false)
    public SOSOptionString current_scheduler_configuration_directory = new SOSOptionString(this, conClassName + ".current_scheduler_configuration_directory", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getcurrent_scheduler_configuration_directory() {
        return current_scheduler_configuration_directory;
    }

    public void setcurrent_scheduler_configuration_directory(SOSOptionString val) {
        this.current_scheduler_configuration_directory = val;
    }

    @JSOptionDefinition(name = "current_scheduler_id", description = "", key = "current_scheduler_id", type = "SOSOptionString", mandatory = false)
    public SOSOptionString current_scheduler_id = new SOSOptionString(this, conClassName + ".current_scheduler_id", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getcurrent_scheduler_id() {
        return current_scheduler_id;
    }

    public void setcurrent_scheduler_id(SOSOptionString val) {
        this.current_scheduler_id = val;
    }

    @JSOptionDefinition(name = "current_scheduler_hostname", description = "", key = "current_scheduler_hostname", type = "SOSOptionString", mandatory = false)
    public SOSOptionString current_scheduler_hostname = new SOSOptionString(this, conClassName + ".current_scheduler_hostname", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getcurrent_scheduler_hostname() {
        return current_scheduler_hostname;
    }

    public void setcurrent_scheduler_hostname(SOSOptionString val) {
        this.current_scheduler_hostname = val;
    }

    @JSOptionDefinition(name = "current_scheduler_port", description = "", key = "current_scheduler_port", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger current_scheduler_port = new SOSOptionInteger(this, conClassName + ".current_scheduler_port", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionInteger getcurrent_scheduler_port() {
        return current_scheduler_port;
    }

    public void setcurrent_scheduler_port(SOSOptionInteger val) {
        this.current_scheduler_port = val;
    }

    public InventoryJobOptions() {
    }

    public InventoryJobOptions(JSListener listener) {
        this();
        this.registerMessageListener(listener);
    }

    public InventoryJobOptions(HashMap<String, String> settings) throws Exception {
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
